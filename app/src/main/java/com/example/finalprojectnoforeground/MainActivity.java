/*-------------NOTES----------------

no foregrounding

*/
package com.example.finalprojectnoforeground;


import androidx.appcompat.app.AppCompatActivity;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private long startTimeinMilliseconds;
    private long startBreakTimerMilliseconds;
    private long timeLeftMilliseconds;
    private long endTime;

    private CountDownTimer countDownTimer;

    private boolean timerRunning;
    private String breakOrStudyTimer = "study";

    private TextView countDownView;

    private Button startPauseButton;
    private Button resetButton;
    private Button breakStudyTimeButton;
    private Button breakSetButton;
    private Button studySetButton;

    private EditText studyEditTextTimer;
    private EditText breakEditTextTimer;

    private SoundPool soundPool;
    private int necoarc;

    //-----------------------------------ACTIONS_MADE------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//-----------------------------------RETRIEVING IDS------------------------------------------
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countDownView = findViewById(R.id.countdown_text);

        startPauseButton = findViewById(R.id.start_pause_button);
        resetButton = findViewById(R.id.reset_button);
        breakStudyTimeButton = findViewById(R.id.break_time_button);

        breakSetButton = findViewById(R.id.break_timer_set);
        studySetButton = findViewById(R.id.study_timer_set);

        studyEditTextTimer = findViewById(R.id.study_timer_input);
        breakEditTextTimer = findViewById(R.id.break_timer_input);

        //---------------ALARM SOUND
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            soundPool = new SoundPool.Builder().setMaxStreams(1).setAudioAttributes(audioAttributes).build();
        }else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
        necoarc = soundPool.load(this, R.raw.necoarc, 1);

        //-----------------------------------ACTIONS_OF_BUTTONS------------------------------------------
        //---------------STUDY CUSTOM SETTER
        studySetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = studyEditTextTimer.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(MainActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                long studyInput = Long.parseLong(input) * 60000;
                if (studyInput == 0) {
                    Toast.makeText(MainActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }

                setStudyTime(studyInput);
                studyEditTextTimer.setHint(input + " Minutes");
                studyEditTextTimer.setText("");

            }
        });

        //---------------BREAK CUSTOM SETTER
        breakSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = breakEditTextTimer.getText().toString();
                if (input.length() == 0) {
                    Toast.makeText(MainActivity.this, "Enter a valid number", Toast.LENGTH_SHORT).show();
                    return;
                }

                long breakInput = Long.parseLong(input) * 60000;
                if (breakInput == 0) {
                    Toast.makeText(MainActivity.this, "Enter a number greater than 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                setBreakTime(breakInput);
                breakEditTextTimer.setHint(input + " Minutes");
                breakEditTextTimer.setText("");

            }
        });

        //---------------START AND PAUSE
        startPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timerRunning || timeLeftMilliseconds == 0){
                    pauseTimer();

                }else if(timerRunning == false){
                    startTimer();

                }
            }
        });

        //---------------BREAK AND STUDY
        breakStudyTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(breakOrStudyTimer == "break" ){
                    studyTimerStart();
                }else{
                    breakTimerStart();
                }
            }
        });

        //---------------RESET
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTimer();
            }
        });

        updateCountDownText();
    }

    //-----------------------------------METHODS------------------------------------------

    //--------------SET CUSTOM STUDY TIME
    private void setStudyTime(long milliseconds) {
        startTimeinMilliseconds = milliseconds;
        if(breakOrStudyTimer == "study"){
            resetTimer();
        }
        closeKeyboard();
    }
    //--------------SET CUSTOM BREAK TIME
    private void setBreakTime(long milliseconds) {
        startBreakTimerMilliseconds = milliseconds;
        if(breakOrStudyTimer == "break"){
            resetTimer();
        }
        closeKeyboard();
    }

    //--------------CLOSES KEYBOARD
    private void closeKeyboard(){

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // ---------------UPDATES TEXT AND SETS FORMAT
    private void updateCountDownText(){

        int hours = (int) (timeLeftMilliseconds/1000)/3600;
        int minutes = (int) ((timeLeftMilliseconds/1000)%3600)/60;
        int seconds = (int) (timeLeftMilliseconds/1000)%60;

        String timeLeftFormatted;

        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%02d:%02d", minutes, seconds);
        }
        countDownView.setText(timeLeftFormatted);
    }

    // ---------------STARTS TIMER
    private void startTimer(){

        countDownTimer = new CountDownTimer(timeLeftMilliseconds, 1000) {

            @Override
            public void onTick(long timeLeftUntilFinished) { // COUNTDOWN HAPPENS

                timerRunning = true;
                timeLeftMilliseconds = timeLeftUntilFinished;
                updateCountDownText();

            }
            @Override
            public void onFinish() { // What Timer does after it's done
                soundPool.play(necoarc, 1, 1, 0, 0, 1);

                if(breakOrStudyTimer == "break"){ // if break timer ends, automatically start study timer

                    studyTimerStart();
                    startTimer();
                    breakOrStudyTimer = "study";
                    updateButtons();
                    updateCountDownText();

                }else{// if start timer ends, automatically start break timer

                    breakOrStudyTimer = "break";
                    breakTimerStart();
                    startTimer();
                    updateButtons();
                    updateCountDownText();

                }
            }
        }
        .start();
        timerRunning = true;
        updateButtons();
    }

    // --------------- PAUSES TIMER
    private void pauseTimer(){

        cancelTimerBugFix();
        timerRunning = false;
        updateButtons();
    }

    // --------------- RESETS TIMER
    private void resetTimer(){

        if(breakOrStudyTimer == "break"){

            timeLeftMilliseconds = startBreakTimerMilliseconds;

        }else if (breakOrStudyTimer == "study"){

            timeLeftMilliseconds = startTimeinMilliseconds;

        }

        updateCountDownText();
        cancelTimerBugFix();
        timerRunning = false;

        updateButtons();

    }

    // --------------- BREAK TIMER SWITCHES
    private void breakTimerStart(){

        endTime = System.currentTimeMillis() + timeLeftMilliseconds;

        timeLeftMilliseconds = startBreakTimerMilliseconds; // setting timer to the break time

        updateCountDownText();
        cancelTimerBugFix();

        breakOrStudyTimer = "break";
        timerRunning = false;

        updateButtons();

    }

    // --------------- STUDY TIMER SWITCHES BACK
    private void studyTimerStart(){

        endTime = System.currentTimeMillis() + timeLeftMilliseconds;

        timeLeftMilliseconds = startTimeinMilliseconds; // setting timer back to study time

        updateCountDownText();
        cancelTimerBugFix();
        breakOrStudyTimer = "study";
        timerRunning = false;
        updateButtons();

    }

    // --------------- UPDATES BUTTONS
    private void updateButtons(){

        if(timerRunning){

            startPauseButton.setText("Pause");

        }else if(timerRunning == false){

            startPauseButton.setText("Start");

        }

        if(breakOrStudyTimer == "break"){

            breakStudyTimeButton.setText("Study TIme");

        }else if(breakOrStudyTimer == "study"){

            breakStudyTimeButton.setText("Break Time");

        }

    }

    // --------------- CANCEL BUTTON BUG FIXES
    private void cancelTimerBugFix(){
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
    }
}