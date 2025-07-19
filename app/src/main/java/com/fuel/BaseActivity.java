package com.fuel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        SharedPreferences prefs = base.getSharedPreferences("fuel_preferences", MODE_PRIVATE);
        String lang = prefs.getString("language", "ita");
        System.out.println("@@@ BaseActivity - read language: " + lang);

        Locale locale = new Locale(lang.equals("en") ? "en" : "it");
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        Context updatedContext = base.createConfigurationContext(config);
        super.attachBaseContext(updatedContext);
    }
}
