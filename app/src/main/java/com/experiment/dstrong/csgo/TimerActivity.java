package com.experiment.dstrong.csgo;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;


public class TimerActivity extends AppCompatActivity {
    private static final String settings_name = "APP_SETTINGS";
    private static final String activity_state = "STATE";
    private static final String timer_state = "TIMER";
    private static final String timer_text = "TIMER_TEXT";
    private boolean pressed;
    private int time;
    private int defuse_time;
    private int plant_time;
    private int detonation_time;
    private MediaPlayer mp;

    Timer bombTimer;
    Timer progress;
    Timer plant;
    Timer defuse;

    enum status {
        ACTIVE,
        INACTIVE,
        PLANTING,
        DEFUSING
    }

    ;
    status state;

    Button plant_button;
    Button defuse_button;
    Button reset_button;

    Handler longPress = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        plant_button = (Button) findViewById(R.id.plant_button);
        defuse_button = (Button) findViewById(R.id.defuse_button);
        reset_button = (Button) findViewById(R.id.reset_button);

        plant_button.setOnTouchListener(plantListener);
        defuse_button.setOnTouchListener(defuseListener);
        reset_button.setOnClickListener(resetListener);

        defuse_button.setVisibility(View.INVISIBLE);

        state = status.INACTIVE;

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(settings_name, 0);
        defuse_time = settings.getInt("defuse_time", getResources().getInteger(R.integer.defuse_time));
        plant_time = settings.getInt("plant_time", getResources().getInteger(R.integer.plant_time));
        detonation_time = settings.getInt("detonation_time", getResources().getInteger(R.integer.detonation_time));
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(savedInstanceState);
        // Save our own state now

        TextView timerText = (TextView) findViewById(R.id.timer_text);

        savedInstanceState.putString(timer_text, (String) timerText.getText());
        savedInstanceState.putInt(timer_state, time);
        savedInstanceState.putSerializable(activity_state, state);

        //cancel timers
        if (bombTimer != null) {
            bombTimer.cancel();
            bombTimer.purge();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        TextView timerText = (TextView) findViewById(R.id.timer_text);
        timerText.setText(savedInstanceState.getString(timer_text));
        state = (status) savedInstanceState.getSerializable(activity_state);
        time = savedInstanceState.getInt(timer_state);
        if (state == status.ACTIVE) {
            startBombTask();
        }
    }

    private View.OnTouchListener plantListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int max = plant_time * 1000;
            ProgressBar progBar = (ProgressBar) findViewById(R.id.progress);
            progBar.setMax(max);
            //start progress on defuse while pressed
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    playSound("planting");
                    progress = new Timer(true);
                    plant = new Timer(true);
                    pressed = true;
                    state = status.PLANTING;

                    final int eventTime = (int) System.currentTimeMillis();
                    progress.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            handleProgress(eventTime, max);
                        }
                    }, 0, 10);
                    plant.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runPlant();
                        }
                    }, max);
                    break;
                case MotionEvent.ACTION_UP:
                    plant.cancel();
                    plant.purge();
                    progress.cancel();
                    progress.purge();
                    pressed = false;
                    if (state != status.ACTIVE) {
                        state = status.INACTIVE;
                    }
                    break;

            }
            return true;
        }
    };
    private View.OnTouchListener defuseListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int max = defuse_time * 1000;
            ProgressBar progBar = (ProgressBar) findViewById(R.id.progress);
            progBar.setMax(max);
            //start progress on defuse while pressed

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    playSound("defusing");
                    progress = new Timer(true);
                    defuse = new Timer(true);
                    pressed = true;
                    state = status.DEFUSING;
                    final int eventTime = (int) System.currentTimeMillis();
                    progress.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            handleProgress(eventTime, max);
                        }
                    }, 0, 10);
                    defuse.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runDefuse();
                        }
                    }, max);
                    break;
                case MotionEvent.ACTION_UP:
                    defuse.cancel();
                    defuse.purge();
                    pressed = false;
                    progress.cancel();
                    progress.purge();
                    if (state != status.INACTIVE) {
                        state = status.ACTIVE;
                    }
                    break;
            }
            return true;
        }
    };
    private View.OnClickListener resetListener = new View.OnClickListener() {
        public void onClick(View v) {
            //stop any current countdown
            if (bombTimer != null) {
                bombTimer.cancel();
                bombTimer.purge();
            }
            pressed = false;
            //reset timer to 00:00
            resetView();
            //reset state
            state = status.INACTIVE;
        }
    };

    private void runPlant() {
        playSound("planted");
        state = status.ACTIVE;
        time = detonation_time;
        startBombTask();

    }

    public void startBombTask(){
        //TODO handle beeping
        bombTimer = new Timer(true);
        bombTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                time = time - 1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView timerText = (TextView) findViewById(R.id.timer_text);
                        timerText.setText(String.format("%02d:%02d", time / 60, time % 60));
                    }
                });
                if (time <= 0) {
                    //stop in progress defusal and bomb timer
                    if (defuse != null) {
                        defuse.cancel();
                        defuse.purge();
                    }
                    pressed = false;
                    progress.cancel();
                    progress.purge();
                    bombTimer.cancel();
                    bombTimer.purge();
                    playSound("explode");
                    resetView();
                    boomView();
                }
            }
        }, 0, 1000);

        plantView();
    }

    private void runDefuse() {
        playSound("defused");
        state = status.INACTIVE;
        bombTimer.cancel();
        bombTimer.purge();
        resetView();
        defuseView();
    }

    ;

    private void handleProgress(int eventTime, int max) {
        ProgressBar progBar = (ProgressBar) findViewById(R.id.progress);
        int downTime = (int) System.currentTimeMillis();
        int progress = (downTime - eventTime);
        if (pressed && progress < max) {
            progBar.setProgress(progress);
        } else {
            progBar.setProgress(0);
        }
    }

    private void resetView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar progBar = (ProgressBar) findViewById(R.id.progress);
                TextView timerText = (TextView) findViewById(R.id.timer_text);
                plant_button.setVisibility(View.VISIBLE);
                defuse_button.setVisibility(View.INVISIBLE);
                timerText.setText("00:00");
                progBar.setProgress(0);
            }
        });
    }

    private void defuseView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView timerText = (TextView) findViewById(R.id.timer_text);
                timerText.setText("DEFUSED");
            }
        });
    }

    private void boomView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView timerText = (TextView) findViewById(R.id.timer_text);
                timerText.setText("Boom");
                plant_button.setVisibility(View.INVISIBLE);
                defuse_button.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void plantView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ProgressBar progBar = (ProgressBar) findViewById(R.id.progress);
                plant_button.setVisibility(View.INVISIBLE);
                defuse_button.setVisibility(View.VISIBLE);
                progBar.setProgress(0);
            }
        });
    }

    private void playSound(String sound) {
        //TODO add media resources
        int file = 0;
        switch (sound) {
            case "defusing":
                break;

            case "planting":
                break;

            case "planted":
                break;

            case "defused":
                break;

            case "explode":
                break;
        }
        if (file != 0) {
            mp = MediaPlayer.create(getApplicationContext(), file);
            mp.start();
        }
    }

}
