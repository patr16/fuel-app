package com.fuel;
/*----------------------------------------------------------------------------------
- create (copy) 17/05/2022
  https://www.geeksforgeeks.org/external-storage-in-android-with-example/
- new version 20/02/2024 - v.1.0-02-2024
- new version 06/07/2025 - v.2.0-07-2025
- new version 18/07/2025 - v.2.1-07-2025
----------------------------------------------------------------------------------*/

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
//----------------------------------------- Widget Date
import android.app.DatePickerDialog;
import android.widget.DatePicker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Calendar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends  BaseActivity {

    // After API 23 the permission request for accessing external storage is changed
    // Before API 23 permission request is asked by the user during installation of app
    // After API 23 permission request is asked at runtime
    // private int EXTERNAL_STORAGE_PERMISSION_CODE = 23;
    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 123;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private RadioGroup radioGroupFuelType;
    private RadioButton radioButtonDiesel, radioButtonGasoline;
    EditText editText_date;
    String fuel_type;
    EditText editText_cost_liter;
    EditText editText_supply_cost;
    EditText editText_supply_liters;
    EditText editText_km_autonomy;
    EditText editText_km_tachometer;
    EditText editText_km_done;
    EditText editText_time;
    EditText editText_liters_100km;
    EditText editText_speed;
    EditText editText_km_liter;
    EditText editText_cost_km_done;
    EditText editText_note;

    //================================================================== onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //============================================================== Reset Database
        // Metodo distruttivo, viene ricreato interamente tutto il database
        // prima di attivare la procedura mettere in FuelDatabaseHelper ---> DATABASE_VERSION = 1
        //Alert.alertDialog(MainActivity.this, "removeLine");
        //FuelDatabaseHelper.resetDatabase(this);
/*
        String[] flagButton = new String[] { "1" };

        Alert.alertDialogFlag(MainActivity.this, "Vuoi davvero resettare il database?", flagButton, new Alert.FlagButtonCallback() {
            @Override
            public void onFlagButtonChanged(String flag) {
                if (flag.equals("2")) {
                    FuelDatabaseHelper.resetDatabase(MainActivity.this);
                    Toast.makeText(MainActivity.this, "Database ricreato!", Toast.LENGTH_SHORT).show();
                }
            }
        });
*/
        //==============================================================

        // Verify permission
        checkStoragePermission();

        //-------------------------------------------------------------- Load SharedPreferences
        SharedPreferences prefs = getSharedPreferences("fuel_preferences", MODE_PRIVATE);
        //String savedLang = prefs.getString("language", "ita"); // fallback su "ita"
        //System.out.println("@@@ read language: " + savedLang);

        //-------------------------------------------------------------- Cost liter and fuel type
        // findViewById return a view, we need to cast it to EditText View
        editText_cost_liter = findViewById(R.id.editText_cost_liter);

        radioGroupFuelType = findViewById(R.id.radioGroupFuelType);
        radioButtonDiesel = findViewById(R.id.radioButtonDiesel);
        radioButtonGasoline = findViewById(R.id.radioButtonGasoline);
        radioButtonDiesel.setChecked(true);

        //-------------------------------------------------------------- Cost liter defaul value
        //--- by default load diesel cost
        String dieselCost = prefs.getString("cost_liter_value_diesel", "");
        if (!dieselCost.isEmpty()) {
            editText_cost_liter.setText(dieselCost);
        }
        //--- checked button
        radioGroupFuelType.setOnCheckedChangeListener((group, checkedId) -> {
        if (checkedId == R.id.radioButtonDiesel) {
            fuel_type = "diesel";
            String cost = prefs.getString("cost_liter_value_diesel", "");
            if (!cost.isEmpty()) {
                editText_cost_liter.setText(cost);
                System.out.println("@@@ cost Diesel: " + cost);
            }
        } else if (checkedId == R.id.radioButtonGasoline) {
            fuel_type = "benzina";
            String cost = prefs.getString("cost_liter_value_gasoline", "");
            if (!cost.isEmpty()) {
                editText_cost_liter.setText(cost);
                System.out.println("@@@ cost Gasoline: " + cost);
            }
        }
        });
        //--------------------------------------------------------------
        editText_date = findViewById(R.id.editText_date);
        editText_cost_liter = findViewById(R.id.editText_cost_liter);
        editText_supply_cost = findViewById(R.id.editText_supply_cost);
        editText_supply_liters = findViewById(R.id.editText_supply_liters);
        editText_km_tachometer = findViewById(R.id.editText_km_tachometer);
        editText_km_done = findViewById(R.id.editText_km_done);
        editText_km_autonomy = findViewById(R.id.editText_km_autonomy);
        editText_time = findViewById(R.id.editText_time);
        editText_speed = findViewById(R.id.editText_speed);
        editText_liters_100km = findViewById(R.id.editText_liters_100km);
        editText_km_liter = findViewById(R.id.editText_km_liter);
        editText_cost_km_done = findViewById(R.id.editText_cost_km_done);
        editText_note = findViewById(R.id.editText_note);

        editText_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        ImageButton buttonHideKeyboard = findViewById(R.id.buttonHideKeyboard);
        buttonHideKeyboard.setOnClickListener(v -> hideKeyboard());

         //================================================================== 1 - onCreate - Button Save
        Button buttonSave = findViewById(R.id.saveButton);
        buttonSave.setOnClickListener(v -> {
            // Leggi tutti i valori dagli EditText
            String date = editText_date.getText().toString();
            String cost_liter = editText_cost_liter.getText().toString();
            String supply_cost = editText_supply_cost.getText().toString();
            String supply_liters = editText_supply_liters.getText().toString();
            String km_autonomy = editText_km_autonomy.getText().toString();
            String km_tachometer = editText_km_tachometer.getText().toString();
            String km_done = editText_km_done.getText().toString();
            String time = editText_time.getText().toString();
            String liters_100km = editText_liters_100km.getText().toString();
            String speed = editText_speed.getText().toString();
            String km_liter = editText_km_liter.getText().toString();
            String cost_km_done = editText_cost_km_done.getText().toString();
            String note = editText_note.getText().toString();

            // Validazione base
            if (date.trim().isEmpty()) {
                Alert.alertDialog(MainActivity.this, "Inserisci una data");
                return;
            }

            // Salvataggio nel database
            FuelDatabaseHelper dbHelper = new FuelDatabaseHelper(this);
            int newRecordId = dbHelper.insertFuelRecord(
                    date, fuel_type, cost_liter, supply_cost, supply_liters,
                    km_autonomy, km_tachometer, km_done, time, liters_100km,
                    speed, km_liter, cost_km_done, note
            );
            Intent intent = new Intent(MainActivity.this, ArchiveActivity.class);
            intent.putExtra("preselectRecordId", newRecordId);
            startActivity(intent);
            Alert.alertDialog(MainActivity.this, "Record salvato! ID: " + newRecordId);
        });

        //================================================================== 1 - onCreate - Button goto Archive
        Button buttonArchive = findViewById(R.id.archiveButton);
        buttonArchive.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ArchiveActivity.class);
            intent.putExtra("preselectRecordId", -1); // nessuna selezione iniziale
            startActivity(intent);
        });

    }

    //================================================================== 3 - checkStoragePermission
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Se il permesso di scrittura non è stato concesso, richiedi il permesso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_PERMISSION_CODE);
        } else {
            // Il permesso è già stato concesso, esegui le operazioni necessarie
            verifyFolderFile();
        }
    }

    //================================================================== 4 - hideKeyboard
    // nasconde la keyboar virtuale del cellulare
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Il permesso è stato concesso, esegui le operazioni necessarie
                verifyFolderFile();
            } else {
                // Il permesso non è stato concesso, gestisci di conseguenza (ad esempio, mostra un messaggio all'utente)
                Toast.makeText(this, "Permesso di scrittura negato.", Toast.LENGTH_SHORT).show();
                // Esci dall'Activity se il permesso non è stato concesso
                finish();
            }
        }
    }

    //================================================================== 4 - verifyFolderFile
    private void verifyFolderFile() {
        // View -> Tools Windows -> Device File Explorer:
        //	    Storage -> Emulated -> 0 -> Download -> <file>
        String header = "Data; Costo Carburante; Litri Carburante; Costo Litro; Km Tachimetro; Km Autonomia; Km Effettuati; " +
                " Tempo; Litri/100 Km; Velocità; km/Litro; Costo calcolato; Nota";

        //--------------------- Verify Folder
        //File folder = getExternalFilesDir("GeeksForGeeks");
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            System.out.println("OK - la cartella esiste.");
        }
        File file = new File(folder, "fuel.txt");
        //--------------------- Verify File
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("Il file è stato creato con successo.");
                    //--------- Load header row
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(file);
                        if (header != null && !header.isEmpty()) {
                            fileOutputStream.write(header.getBytes());
                            fileOutputStream.write("\n".getBytes());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //---------
                } else {
                    System.out.println("Non è stato possibile creare il file.");
                    finish();
                }
            } catch (IOException e) {
                System.out.println("Si è verificato un'errore durante la creazione del file:");
                e.printStackTrace();
                finish();
            }
        } else {
            //-------------------------------------------------------------- Verify Header
            // Se il file esiste verifica se il file è vuoto o è presente solo la riga dell'header
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                int count = 0;
                while ((bufferedReader.readLine()) != null) {
                    count ++;
                }
                bufferedReader.close();
                //----------------- se il file è vuoto o vi è solo la riga dell'header
                if(count < 2){
                    //----------------------- Message
                    String message = "- Nell'archivio non sono presenti dati, si inizializzano i parametri. \n- Per attivare l'archivio, inserire la prima riga di dati.";
                    Alert.alertDialog(MainActivity.this, message);
                    //------------------- Cancella tutto il contenuto del file
                    try {
                        // Sovrascrive il contenuto del file con una stringa vuota
                        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                        writer.write(""); // Scrive una stringa vuota
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //------------------- Inserisce l'Header
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileOutputStream = new FileOutputStream(file);
                        if (header != null && !header.isEmpty()) {
                            fileOutputStream.write(header.getBytes());
                            fileOutputStream.write("\n".getBytes());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                bufferedReader.close();
            } catch(FileNotFoundException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    //================================================================== 5 - Widget Calendar for Date
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        String selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        editText_date.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth);
        datePickerDialog.show();
    }

    //================================================================== 6 - Calcolo Costo Carburante
    /*
     Funzione Button Costo
     editText_cost_liter: costo carburante al litro (attuale rifornimento)
     editText_km_liter: km effettuati con un litro (km a consuntivo precedente rifornimento)
     editText_cost_km_done: costo per i km effettuati (costo km effettuati a consuntivo precedente rifornimento)
     editText_supply_cost: costo attuale rifornimento.

     1 - In base alla media dei litri/100 km del precedente rifornimento (dato registarto a consuntivo nel momento del nuovo rifornimento)
     considerando il costo attuale al litro si calcola il costo per 100 km a quella velocità media.
     Costo per i litri di carburante per fare 100 km = <costo al litro> * (<litri/100 km> * <km percorsi>/100)

     2 - In base ai litri per 100 km si calcola i km percorsi con 1 litro
     km effettuati con 1 litro = 100*1/<litri/100 km>
    */

    public void fuelCostCalculate(View view) {
        editText_cost_liter = (EditText) findViewById(R.id.editText_cost_liter);
        editText_km_done = (EditText) findViewById(R.id.editText_km_done);
        editText_liters_100km = (EditText) findViewById(R.id.editText_liters_100km);
        editText_supply_cost = (EditText) findViewById(R.id.editText_supply_cost);
        //------------------------------------
        String message ="";
        //-------------------------------- Verify Parameters
        if(VerifyNumber(editText_cost_liter) !=""){
            message = message + " Costo al litro: " + VerifyNumber(editText_cost_liter) + "\n";
        }
        if(VerifyNumber(editText_liters_100km) !=""){
            message = message + "Litri/100 km: " + VerifyNumber(editText_liters_100km) + "\n";
        }
        if(VerifyNumber(editText_supply_cost) !=""){
            message = message + "Km percorsi: " + VerifyNumber(editText_supply_cost) ;
        }
        //-------------------------------- Calculation of Cost
        if (message == ""){
            double n_cost_liter = Double.parseDouble(editText_cost_liter.getText().toString());
            double n_cost_supply_cost = Double.parseDouble(editText_supply_cost.getText().toString());
            double n_liters_100km = Double.parseDouble(editText_liters_100km.getText().toString());

            // 1 - Costo per i <km> effettuati = <costo al litro> * (<litri/100 km> * <costo attuale>/100)
            Double cost = n_cost_liter * (n_liters_100km * n_cost_supply_cost / 100);
            editText_cost_km_done.setText(String.format("%.2f", cost));
            // 2 - Costo per i <km> effettuati = 100*1/<litri/100 km>
            Double km_liter = 100*1/n_liters_100km;
            editText_km_liter.setText(String.format("%.2f", km_liter));
            //System.out.println("costo: " + cost);
        }else{
            if (message.length() < 17){ // lunghezza massima di un singolo parametro
                message = "Parametro richiesto:\n " + message;
            } else{
                message = "Parametri richiesti:\n " + message;
            }
            Alert.alertDialog(MainActivity.this, message);
        }
    }

    //----------------------------------------------- VerifyNumber
    private String VerifyNumber(EditText editText) {
        String text = editText.getText().toString();
        String message;
        if (!text.isEmpty()) { // Controlla se l'EditText non è vuoto
            try {
                double n_done_km = Double.parseDouble(text);
                if (n_done_km > 0) { // Controlla se il numero è maggiore di zero
                    message="";
                } else {
                    message = "il numero deve essere maggiore di zero";
                }
            } catch (NumberFormatException e) {
                message = "inserisci un numero valido";
            }
        } else {
            message = "inserisci un numero";
        }
        return message;
    }
}
