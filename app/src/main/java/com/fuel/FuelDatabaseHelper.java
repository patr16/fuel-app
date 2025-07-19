package com.fuel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class FuelDatabaseHelper extends SQLiteOpenHelper {
    /*
    Questo file non viene normalmente letto.
    Il sistema legge unicamente private static final int DATABASE_VERSION = n
    Se il valore è superiore alla attuale versione allora viene eseguito l'OnUpgrade
    e ricreato il database
    */

    private static final String DATABASE_NAME = "fuel.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "fuel";

    public FuelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //--------------------------------------------------------- db version
        // vedere metodo onUpgrade()
        // questo messaggio qui non viene mai eseguito,
        // vedere onCreate di ExportConfigActivity
        int version = db.getVersion();
        System.out.println("@@@ DB_VERSION - La versione del database è: " + version);

        //--------------------------------------------------------- create tables
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "fuel_type TEXT, " +
                "cost_liter TEXT, " +
                "supply_cost TEXT, " +
                "supply_liters TEXT, " +
                "km_autonomy TEXT, " +
                "km_tachometer TEXT, " +
                "km_done TEXT, " +
                "time TEXT, " +
                "liters_100km TEXT, " +
                "speed TEXT, " +
                "km_liter TEXT, " +
                "cost_km_done TEXT, " +
                "note TEXT)";
        db.execSQL(createTable);

        //--------------------------------------------------------- Export Fields
        db.execSQL("CREATE TABLE IF NOT EXISTS export_fields (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "field_name TEXT," +
                "order_index INTEGER," +
                "enabled INTEGER DEFAULT 1," +
                "value TEXT" +
                ");");

        // Popola default se vuota
        String[] defaultFields = {
                "date", "fuel_type", "cost_liter", "supply_cost", "supply_liters", "km_autonomy",
                "km_tachometer", "km_done", "time", "liters_100km", "speed",
                "km_liter", "cost_km_done", "note"
        };
        for (int i = 0; i < defaultFields.length; i++) {
            db.execSQL("INSERT INTO export_fields (field_name, order_index, enabled) VALUES ('" +
                    defaultFields[i] + "', " + i + ", 1);");
        }

    }

    //--------------------------------------------------------- Inserisci un nuovo record
    public int insertFuelRecord(
            String date,
            String fuel_type,
            String cost_liter,
            String supply_cost,
            String supply_liters,
            String km_autonomy,
            String km_tachometer,
            String km_done,
            String time,
            String liters_100km,
            String speed,
            String km_liter,
            String cost_km_done,
            String note
    ) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("date", date);
        values.put("fuel_type", fuel_type);
        values.put("cost_liter", cost_liter);
        values.put("supply_cost", supply_cost);
        values.put("supply_liters", supply_liters);
        values.put("km_autonomy", km_autonomy);
        values.put("km_tachometer", km_tachometer);
        values.put("km_done", km_done);
        values.put("time", time);
        values.put("liters_100Km", liters_100km);
        values.put("speed", speed);
        values.put("km_liter", km_liter);
        values.put("cost_km_done", cost_km_done);
        values.put("note", note);

        // Inserisce e restituisce l'id appena creato
        long insertedId = db.insert("fuel", null, values);
        return (int) insertedId;
    }

      //--------------------------------------------------------- Cancella un record per id
    public int deleteFuelRecordById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(id)});
    }


    //--------------------------------------------------------- Recupera le date distinte
    /*
    public List<String> getDistinctDates() {

        List<String> dates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT date FROM " + TABLE_NAME + " ORDER BY date DESC", null);
        while (cursor.moveToNext()) {
            dates.add(cursor.getString(0));
        }
        cursor.close();
        return dates;
    }
*/
    public List<String> getFormattedRecordsForSpinner(List<Integer> idList) {
        List<String> displayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT id, date, supply_cost FROM " + TABLE_NAME + " ORDER BY date DESC", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            double cost = cursor.getDouble(cursor.getColumnIndexOrThrow("supply_cost"));

            // Aggiungi l'id alla lista separata (passata come parametro)
            idList.add(id);

            // Crea la stringa da mostrare
            String display = date + " - €" + String.format("%.2f", cost);
            displayList.add(display);
        }

        cursor.close();
        return displayList;
    }


    //--------------------------------------------------------- Recupera un record completo per data
    public Cursor queryAllBasicRecords() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                "fuel",
                new String[]{"id", "date", "supply_cost"},
                null,
                null,
                null,
                null,
                "date DESC"
        );
    }
    /*
    public Cursor getRecordByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, null, "date = ?", new String[]{date}, null, null, null);
    }
*/


    //--------------------------------------------------------- Recupera un record completo per data
    public Cursor getRecordById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("fuel", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
    }




    //--------------------------------------------------------- Update structure database
    /*
     se si vuole aggiornare la struttura del database o inizializzare tutto il database
     incrementare il valore della versione del db, questo determinerò l'upgrade
     - attuale versione = 1
     - inizializziamo nuova versione con DATABASE_VERSION = 2
     - Android rileva che versione attuale (1) è inferiore a nuova versione (2)
     - viene attivato l'onUpgrade() automaticamente al prossimo avvio
    */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DATABASE_VERSION) {
            // Crea la tabella export_fields se non esiste
            db.execSQL("CREATE TABLE IF NOT EXISTS export_fields (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "field_name TEXT," +
                    "order_index INTEGER," +
                    "enabled INTEGER DEFAULT 1" +
                    ");");

            // Popola i valori di default se vuoi (opzionale, controlla che non ci siano già)
            String[] defaultFields = {
                    "date", "fuel_type", "cost_liter", "supply_cost", "supply_liters", "km_autonomy",
                    "km_tachometer", "km_done", "time", "liters_100km", "speed",
                    "km_liter", "cost_km_done", "note"
            };
            for (int i = 0; i < defaultFields.length; i++) {
                db.execSQL("INSERT INTO export_fields (field_name, order_index, enabled) VALUES ('" +
                        defaultFields[i] + "', " + i + ", 1);");
            }
        }
    }

    //--------------------------------------------------------- Reset Database
    /*
     Ricrea interamente le tabelle del database
     NB: prima di attivare il metodo bisogna resettare a mano private static final int DATABASE_VERSION = 1;
     Viene chiamato nella MainActivity()
     */
    public static void resetDatabase(Context context) {
        // Elimina il file del database
        context.deleteDatabase("fuel.db");

        // Forza la ricreazione chiamando getWritableDatabase()
        FuelDatabaseHelper dbHelper = new FuelDatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Verifica con log
        System.out.println("@@@ Database ricreato con versione: " + db.getVersion());
    }
}


