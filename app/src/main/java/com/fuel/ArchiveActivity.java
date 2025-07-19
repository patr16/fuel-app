package com.fuel;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ArchiveActivity extends BaseActivity {

    private FuelDatabaseHelper dbHelper;
    private TextView textView_data;
    private Spinner spinnerData;
    private int selectedRecordId = -1;
    private ArrayAdapter<String> adapter;
    private String lastInsertedData = null;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        dbHelper = new FuelDatabaseHelper(this);
        tableLayout = findViewById(R.id.tableLayout);
        textView_data = findViewById(R.id.textView_data);

        //-----------------------------------------------
        // Recupera i dati passati da MainActivity
        String date = getIntent().getStringExtra("date");
        String fuel_type = getIntent().getStringExtra("fuel_type");
        String cost_liter = getIntent().getStringExtra("cost_liter");
        String supply_cost = getIntent().getStringExtra("supply_cost");
        String supply_liters = getIntent().getStringExtra("supply_liters");
        String km_autonomy = getIntent().getStringExtra("km_autonomy");
        String km_tachometer  = getIntent().getStringExtra("km_tachometer");
        String km_done = getIntent().getStringExtra("km_done");
        String time = getIntent().getStringExtra("time");
        String liters_100km = getIntent().getStringExtra("liters_100km");
        String speed = getIntent().getStringExtra("speed");
        String km_liter = getIntent().getStringExtra("km_liter");
        String cost_km_done = getIntent().getStringExtra("cost_km_done");
        String note = getIntent().getStringExtra("note");

        if (date != null && supply_cost != null) {
            lastInsertedData = date + "; " + fuel_type +"; "  + cost_liter + "; " + supply_cost + "; " + supply_liters + "; " +
                    km_autonomy + "; " + km_tachometer + "; " + km_done + "; " +
                    time + "; " + liters_100km + "; " + speed + "; " + km_liter + "; " + cost_km_done + "; " + note;
            textView_data.setText(lastInsertedData);
        }

        //-----------------------------------------------
        // Spinner
        spinnerData = findViewById(R.id.spinner_data);
        tableLayout = findViewById(R.id.tableLayout); // SERVE???????????????

        //--------------------------------- Seleziona il record appena salvato
        int preselectId = getIntent().getIntExtra("preselectRecordId", -1);
        updateSpinnerData(preselectId);

        spinnerData.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedId = recordIds.get(position);

                if (selectedId == -1) {
                    // voce dummy, non fare nulla
                    tableLayout.removeAllViews();
                    return;
                }

                selectedRecordId = selectedId;
                extractDataForSelectedId(selectedRecordId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }

    //---------------------------------------------- removeLine
    public void removeLine(View view) {
        Alert.alertDialog(this, "Record selezionato: id - " + selectedRecordId);

        int deletedRows = dbHelper.deleteFuelRecordById(selectedRecordId);
        if (deletedRows > 0) {
            Alert.alertDialog(this, " Record eliminato con successo: id - " + selectedRecordId);
        } else {
            Alert.alertDialog(this, "Nessun record trovato con quell'ID: id - " + selectedRecordId);
        }

        updateSpinnerData(-1);
        tableLayout.removeAllViews();
    }

    //---------------------------------------------- updateSpinnerData
    private List<Integer> recordIds = new ArrayList<>(); // ID dei record reali, incluso dummy
    private List<String> displayItems = new ArrayList<>();

    private void updateSpinnerData(int preselectRecordId) {
        displayItems.clear();
        recordIds.clear();

        // Voce iniziale fittizia
        displayItems.add("— seleziona —");
        recordIds.add(-1);

        // Aggiungi i record reali
        Cursor cursor = dbHelper.queryAllBasicRecords(); // id, date, supply_cost
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow("supply_cost"));

                String label = date + " - €" + String.format("%.2f", cost);
                displayItems.add(label);
                recordIds.add(id);
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                displayItems
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerData.setAdapter(adapter);

        // Pre-seleziona solo se c’è un ID valido
        if (preselectRecordId != -1) {
            int index = recordIds.indexOf(preselectRecordId);
            if (index >= 0) {
                spinnerData.setSelection(index);
                selectedRecordId = preselectRecordId;
                extractDataForSelectedId(selectedRecordId);
            }
        } else {
            spinnerData.setSelection(0); // voce dummy
            selectedRecordId = -1;
        }
    }


    public void extractDataForSelectedId(int selectedId) {
        System.out.println("extractDataForSelectedId: " + selectedId);
        tableLayout.removeAllViews();

        Cursor cursor = dbHelper.getRecordById(selectedId);
        if (cursor != null && cursor.moveToFirst()) {
            String[] labels = {"date", "fuel_type", "cost_liter", "supply_cost", "supply_liters", "km_autonomy",
                    "km_tachometer", "km_done", "time", "liters_100km", "speed",
                    "km_liter", "cost_km_done", "note"};

            for (int i = 0; i < labels.length; i++) {
                TableRow row = new TableRow(this);
                TextView labelTextView = new TextView(this);
                TextView valueTextView = new TextView(this);

                labelTextView.setText(labels[i]);
                labelTextView.setPadding(8, 8, 8, 8);
                labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                String value = cursor.getString(cursor.getColumnIndexOrThrow(labels[i]));
                valueTextView.setText(value != null ? value : "");
                valueTextView.setPadding(8, 8, 8, 8);
                valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

                row.addView(labelTextView);
                row.addView(valueTextView);
                tableLayout.addView(row);
            }
        } else {
            System.out.println("Nessun record trovato per id: " + selectedId);
        }

        if (cursor != null) cursor.close();
    }

    //---------------------------------------------- exportData
    public void exportData(View view) {
        startActivity(new Intent(ArchiveActivity.this, ExportConfigActivity.class));
    }

    //---------------------------------------------- back
    public void back(View view) {
        startActivity(new Intent(ArchiveActivity.this, MainActivity.class));
    }
}
