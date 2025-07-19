package com.fuel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ViewArchiveActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_archive);

        TextView textViewData = findViewById(R.id.textView_data);
        Intent intent = getIntent();
        String text = intent.getStringExtra("text");
        textViewData.setText(text);
    }

    //================================================================== 1 - Back to MainActivity
    // Torna alla pagina principale
    public void back(View view) {
        Intent intent = new Intent(ViewArchiveActivity.this, ArchiveActivity.class);
        startActivity(intent);
    }
}
