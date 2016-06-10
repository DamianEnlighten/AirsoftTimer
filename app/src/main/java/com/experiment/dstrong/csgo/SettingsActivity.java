package com.experiment.dstrong.csgo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {
    private static final String settings_name = "APP_SETTINGS";
    private static final String plant_state = "PLANT";
    private static final String defuse_state = "DEFUSE";
    private static final String detonate_state = "DETONATE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button save_button = (Button) findViewById(R.id.save_button);
        save_button.setOnClickListener(saveListener);

        Button cancel_button = (Button) findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener(cancelListener);

        Button default_button = (Button) findViewById(R.id.default_button);
        default_button.setOnClickListener(defaultListener);


        EditText defuse = (EditText) findViewById(R.id.defuse_time);
        EditText plant = (EditText) findViewById(R.id.plant_time);
        EditText detonation = (EditText) findViewById(R.id.detonation_time);
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(settings_name, 0);
        defuse.setText(String.valueOf(settings.getInt("defuse_time", getResources().getInteger(R.integer.defuse_time))));
        plant.setText(String.valueOf(settings.getInt("plant_time", getResources().getInteger(R.integer.plant_time))));
        detonation.setText(String.valueOf(settings.getInt("detonation_time", getResources().getInteger(R.integer.detonation_time))));
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        EditText defuse = (EditText) findViewById(R.id.defuse_time);
        EditText plant = (EditText) findViewById(R.id.plant_time);
        EditText detonation = (EditText) findViewById(R.id.detonation_time);

        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(savedInstanceState);
        // Save our own state now
        try {
            savedInstanceState.putInt(plant_state, Integer.parseInt(plant.getText().toString()));
        } catch (NumberFormatException ex) {
            savedInstanceState.putInt(plant_state, 0);
        }
        try {
            savedInstanceState.putInt(defuse_state, Integer.parseInt(defuse.getText().toString()));
        } catch (NumberFormatException ex) {
            savedInstanceState.putInt(defuse_state, 0);
        }
        try {
            savedInstanceState.putInt(detonate_state, Integer.parseInt(detonation.getText().toString()));
        } catch (NumberFormatException ex) {
            savedInstanceState.putInt(detonate_state, 0);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        EditText defuse = (EditText) findViewById(R.id.defuse_time);
        EditText plant = (EditText) findViewById(R.id.plant_time);
        EditText detonation = (EditText) findViewById(R.id.detonation_time);

        defuse.setText(String.valueOf(savedInstanceState.getInt(defuse_state)));
        plant.setText(String.valueOf(savedInstanceState.getInt(plant_state)));
        detonation.setText(String.valueOf(savedInstanceState.getInt(detonate_state)));
    }

    private View.OnClickListener saveListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (ValidateSettings()) {
                SharedPreferences settings = getSharedPreferences(settings_name, 0);
                SharedPreferences.Editor editor = settings.edit();

                EditText defuse = (EditText) findViewById(R.id.defuse_time);
                EditText plant = (EditText) findViewById(R.id.plant_time);
                EditText detonation = (EditText) findViewById(R.id.detonation_time);

                editor.putInt("plant_time", Integer.parseInt(plant.getText().toString()));
                editor.putInt("defuse_time", Integer.parseInt(defuse.getText().toString()));
                editor.putInt("detonation_time", Integer.parseInt(detonation.getText().toString()));
                editor.commit();

                Intent intent = new Intent(v.getContext(), MainMenu.class);
                startActivity(intent);
            }
        }
    };
    private View.OnClickListener defaultListener = new View.OnClickListener() {
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EditText defuse = (EditText) findViewById(R.id.defuse_time);
                    EditText plant = (EditText) findViewById(R.id.plant_time);
                    EditText detonation = (EditText) findViewById(R.id.detonation_time);

                    plant.setText(String.valueOf(getResources().getInteger(R.integer.plant_time)));
                    defuse.setText(String.valueOf(getResources().getInteger(R.integer.defuse_time)));
                    detonation.setText(String.valueOf(getResources().getInteger(R.integer.detonation_time)));
                }
            });
        }
    };
    private View.OnClickListener cancelListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), MainMenu.class);
            startActivity(intent);
        }
    };

    private boolean ValidateSettings() {
        EditText defuse = (EditText) findViewById(R.id.defuse_time);
        EditText plant = (EditText) findViewById(R.id.plant_time);
        EditText detonation = (EditText) findViewById(R.id.detonation_time);

        if (Integer.parseInt(plant.getText().toString()) < 1) {
            plant.setError("Time to plant must be more than 0");
            return false;
        }
        if (Integer.parseInt(defuse.getText().toString()) < 1) {
            defuse.setError("Time to defuse must be more than 0");
            return false;
        }
        if (Integer.parseInt(detonation.getText().toString()) < 1) {
            detonation.setError("Time to explosion must be more than 0");
            return false;
        }

        return true;
    }
}
