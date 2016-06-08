package com.experiment.dstrong.csgo;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button start_button = (Button) findViewById(R.id.start_button);
        start_button.setOnClickListener(startListener);

        Button settings_button = (Button) findViewById(R.id.settings_button);
        settings_button.setOnClickListener(settingsListener);

        StrictMode.enableDefaults();
    }

    private View.OnClickListener startListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), TimerActivity.class);
            startActivity(intent);
        }
    };


    private View.OnClickListener settingsListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), SettingsActivity.class);
            startActivity(intent);
        }
    };
}
