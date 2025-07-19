package com.fuel;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.graphics.Color;
/*-----------------------------------------------------------------------------
I dati cost_liter_value_diesel, cost_liter_value_gasoline e language,
vengono salvati in /data/data/tuo.package.name/shared_prefs/fuel_preferences.xml
SharedPreferences prefs = getSharedPreferences("fuel_preferences", MODE_PRIVATE
esempio di struttura del file:
<map>
    <string name="cost_liter_value_diesel">1.650</string>
    <string name="cost_liter_value_gasoline">2.852</string>
    <string name="language">ita</string>
</map>


-----------------------------------------------------------------------------*/
public class ExportConfigActivity extends BaseActivity {

    private RecyclerView recyclerView;
    public ExportFieldAdapter adapter;
    private FuelDatabaseHelper dbHelper;
    private List<ExportField> filteredFields;
    private TextInputEditText editTextCostDiesel;
    private TextInputEditText editTextCostGasoline;
    private TextInputEditText editTextLanguage;
    private AutoCompleteTextView dropdownLanguage;
    private Map<String, String> fieldTranslations;
    private String currentLang;
    //--------------------------------------------------------------------------- onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_config);

        SharedPreferences prefs = getSharedPreferences("fuel_preferences", MODE_PRIVATE);

        //------------------------------------ Traduzione campi tabella fuel per RecyclerView
        //SharedPreferences prefs = getSharedPreferences("fuel_preferences", MODE_PRIVATE);
        currentLang = prefs.getString("language", "ita");
        setupFieldTranslations(currentLang);

        //------------------------------------ Inizializza views
        recyclerView = findViewById(R.id.recyclerViewExportConfig);
        editTextCostDiesel = findViewById(R.id.editTextCostDiesel);
        editTextCostGasoline = findViewById(R.id.editTextCostGasoline);
        dropdownLanguage = (AutoCompleteTextView) findViewById(R.id.dropdownLanguage);

       //------------------------------------ Carica preferenze salvate cost liter and language
        editTextCostDiesel.setText(prefs.getString("cost_liter_value_diesel", ""));
        editTextCostGasoline.setText(prefs.getString("cost_liter_value_gasoline", ""));
        String savedLang = prefs.getString("language", "ita"); // fallback su "ita"
        System.out.println("@@@ read language: " + savedLang);
        dropdownLanguage.setText(savedLang, false);

        //------------------------------------ Impost dropdown Language
        String[] languages = new String[] { "ita", "en" };
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                languages
        );
        dropdownLanguage.setAdapter(languageAdapter);
        dropdownLanguage.setTextColor(Color.BLACK); // visibilità

        //------------------------------------ Salva nuova selezione language
        dropdownLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLang = (String) parent.getItemAtPosition(position);
            dropdownLanguage.setText(selectedLang, false);
            prefs.edit().putString("language", selectedLang).apply();
        });

        //------------------------------------ Buttons
        ImageButton buttonBack = findViewById(R.id.goBackButton);
        Button buttonSave = findViewById(R.id.buttonSaveConfig);
        Button buttonExportToCSV = findViewById(R.id.buttonExportToCSV);

        //------------------------------------ Dati database e RecyclerView
        // Filtra per escludere i campi "Cost Liter value diesel/gasoline"
        dbHelper = new FuelDatabaseHelper(this);
        List<ExportField> allFields = loadExportFieldsFromDB();
        filteredFields = new ArrayList<>();
        //---------------- filter field
        for (ExportField field : allFields) {
            String name = field.getFieldName();
            if (!name.equals("Cost Liter value diesel") && !name.equals("Cost Liter value gasoline") && !name.equals("Language")) {
                filteredFields.add(field);
            }
        }
        //---------------- translate field name in ita language for RecyclerView
        for (ExportField field : filteredFields) {
            String translated = fieldTranslations.getOrDefault(field.getFieldName(), field.getFieldName());
            field.setDisplayName(translated);
        }

        adapter = new ExportFieldAdapter(filteredFields);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //------------------------------------ Abilita drag & drop
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //------------------------------------ Button
        buttonSave.setOnClickListener(this::saveConfig);
        buttonBack.setOnClickListener(this::back);
        buttonExportToCSV.setOnClickListener(this::exportToCSV);

    }
    //--------------------------------------------------------------------------- Metodi

    private List<ExportField> loadExportFieldsFromDB() {
        List<ExportField> fields = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //--------------------------------------------------------- db version
        // vedere metodo onUpgrade()
        int version = db.getVersion();
        //System.out.println("@@@ DB_VERSION - La versione del database è: " + version);

        Cursor cursor = db.query("export_fields", null, null, null, null, null, "order_index ASC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fieldName = cursor.getString(cursor.getColumnIndexOrThrow("field_name"));
                int orderIndex = cursor.getInt(cursor.getColumnIndexOrThrow("order_index"));
                int enabled = cursor.getInt(cursor.getColumnIndexOrThrow("enabled"));
                fields.add(new ExportField(id, fieldName, orderIndex, enabled == 1));
            }
            cursor.close();
        }
        return fields;
    }

    //------------------------------------ Map traduzione field db in italiano
    private void setupFieldTranslations(String lang) {
        fieldTranslations = new HashMap<>();

        if (lang.equals("ita")) {
            fieldTranslations.put("date", "Data");
            fieldTranslations.put("fuel_type", "Tipo carburante");
            fieldTranslations.put("cost_liter", "Costo/Litro");
            fieldTranslations.put("supply_cost", "Costo rifornimento");
            fieldTranslations.put("supply_liters", "Litri riforniti");
            fieldTranslations.put("km_autonomy", "Autonomia km");
            fieldTranslations.put("km_tachometer", "Km tachimetro totali");
            fieldTranslations.put("km_done", "Km percorsi");
            fieldTranslations.put("time", "Tempo (h:min)");
            fieldTranslations.put("liters_100km", "Litri/100km");
            fieldTranslations.put("speed", "Velocità media");
            fieldTranslations.put("km_liter", "Km/Litro");
            fieldTranslations.put("cost_km_done", "Costo km percorsi");
            fieldTranslations.put("note", "Nota");
            // Aggiungi altri campi necessari
        }else if (lang.equals("en")) {
            fieldTranslations.put("date", "Date");
            fieldTranslations.put("fuel_type", "Fuel type");
            fieldTranslations.put("cost_liter", "Cost/Liter");
            fieldTranslations.put("supply_cost", "Cost Supply");
            fieldTranslations.put("supply_liters", "liters Supply");
            fieldTranslations.put("km_autonomy", "Autonomy Km");
            fieldTranslations.put("km_tachometer", "total Km tachometer");
            fieldTranslations.put("km_done", "Km done");
            fieldTranslations.put("time", "Time (h:min)");
            fieldTranslations.put("liters_100km", "Liters/100km");
            fieldTranslations.put("speed", "Speed average");
            fieldTranslations.put("km_liter", "Km/Liter");
            fieldTranslations.put("cost_km_done", "Cost Km Done");
            fieldTranslations.put("note", "Note");
        }
    }

    //--------------------------------------------------------------------------- 1 - Save Config
    public void saveConfig(View view) {

        //---------------------------------- 1 - save parameters
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (int i = 0; i < filteredFields.size(); i++) {
            ExportField field = filteredFields.get(i);
            ContentValues values = new ContentValues();
            values.put("order_index", i);
            values.put("enabled", field.isEnabled() ? 1 : 0);
            db.update("export_fields", values, "id=?", new String[]{String.valueOf(field.getId())});
        }

        //---------------------------------- 2 - save cost liter and language
        // I dati vengono salvati in /data/data/tuo.package.name/shared_prefs/fuel_preferences.xml
        SharedPreferences prefs = getSharedPreferences("fuel_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("cost_liter_value_diesel", editTextCostDiesel.getText().toString());
        editor.putString("cost_liter_value_gasoline", editTextCostGasoline.getText().toString());
        editor.putString("language", dropdownLanguage.getText().toString());
        editor.apply();

        Alert.alertDialog(ExportConfigActivity.this,"Configurazione salvata");
    }

    //---------------------------------- 2 - save cost liter and language
    private final ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getBindingAdapterPosition();
            int toPosition = target.getBindingAdapterPosition();
            if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
                return false;
            }
            Collections.swap(filteredFields, fromPosition, toPosition);
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // Non usato
        }
    };

    //--------------------------------------------------------------------------- 2 - Export to CSV
    public void exportToCSV(View view) {
        File exportDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (exportDir != null && !exportDir.exists()) {
            exportDir.mkdirs();
        }
        //-------------------------- file name csv with time stamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy-HH_mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        String fileName = "fuel_export_" + timestamp + ".csv";

        File file = new File(exportDir, fileName);

        try (FileWriter fw = new FileWriter(file);
             BufferedWriter bw = new BufferedWriter(fw)) {

            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<String> selectedFields = new ArrayList<>();
            Cursor cursor = db.rawQuery("SELECT field_name FROM export_fields WHERE enabled = 1 ORDER BY order_index ASC", null);
            while (cursor.moveToNext()) {
                selectedFields.add(cursor.getString(0));
            }
            cursor.close();

            if (selectedFields.isEmpty()) {
                Toast.makeText(this, "Nessun campo selezionato per esportazione.", Toast.LENGTH_SHORT).show();
                return;
            }
            //---------------------------------- Preparazione file csv
            // separatore csv, paesi englofoni: ",", Italia: ";"

            List<String> previewLines = new ArrayList<>();
            
            // Scrivi intestazione
            String header = String.join(";", selectedFields);
            bw.write(header);
            bw.newLine();
            previewLines.add(header);

            Cursor dataCursor = db.rawQuery("SELECT * FROM fuel ORDER BY id ASC", null);
            while (dataCursor.moveToNext()) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < selectedFields.size(); i++) {
                    String columnName = selectedFields.get(i).replace(" ", "_").toLowerCase();
                    int columnIndex = dataCursor.getColumnIndex(columnName);
                    if (columnIndex >= 0) {
                        String value = dataCursor.getString(columnIndex);
                        line.append(value != null ? value.replace(";", ".") : "");      // separatore ";"
                    }
                    if (i < selectedFields.size() - 1) {
                        line.append(";");       // separatore ";"
                    }
                }
                String csvLine = line.toString();
                bw.write(line.toString());
                bw.newLine();
                previewLines.add(csvLine); // aggiunta per anteprima
            }
            dataCursor.close();
            bw.flush();
            db.close();

            //---------------------------------- Messaggio di Alert
            StringBuilder previewText = new StringBuilder();
            for (int i = 0; i < Math.min(previewLines.size(), 10); i++) { // mostra max 10 righe
                previewText.append(previewLines.get(i)).append("\n");
            }
            //---------------------------------- Alert di anteprima prima dell'invio
            new AlertDialog.Builder(this)
                    .setTitle("Anteprima CSV")
                    .setMessage(previewText.toString())
                    .setPositiveButton("Procedi", (dialog, which) -> {
                        //------------------------ Invio file csv
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/csv");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Esportazione Fuel CSV");
                        intent.putExtra(Intent.EXTRA_TEXT, "In allegato il file CSV esportato da Fuel.");
                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                ExportConfigActivity.this,
                                getPackageName() + ".provider",
                                file
                        ));
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "Invia CSV tramite"));
                    })
                    .setNegativeButton("Annulla", null)
                    .show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Errore durante l'esportazione CSV.", Toast.LENGTH_SHORT).show();
        }
    }

    //--------------------------------------------------------------------------- 3 - Go to Back
    public void back(View view) {
        finish();
    }
}
