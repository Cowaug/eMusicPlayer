package project.exos.emusicplayer.GUI;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;

import project.exos.emusicplayer.Entity.MediaController;
import project.exos.emusicplayer.R;

public class Popup_Timer extends Dialog implements android.view.View.OnClickListener {
    private Activity activity;
    private EditText editText_TimerMinute;
    private TextView text_TimerRemainingTime;
    private long countDownTime = -1;
    private Handler handler = new Handler();

    Popup_Timer(Activity activity) {
        super(activity);
        this.activity=activity;
    }

    private void Timer() {
        long remainingTime = MediaController.GetTimerRemainingTime();
        text_TimerRemainingTime.setText(
                remainingTime <= 0 ?
                        "No timer set" :
                        "Timer was set\n" + (remainingTime >= 60 * 60 * 1000 ?
                                remainingTime / (60 * 60 * 1000) + " h " + (remainingTime % (60 * 60 * 1000)) / (60 * 1000) + " m remaining" :
                                remainingTime / (60 * 1000) > 0 ?
                                        remainingTime / (60 * 1000) + " m " + remainingTime % (60 * 1000) / 1000 + " s remaining" :
                                        remainingTime / 1000 + " second(s) remaining")

        );
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Timer();
            }
        }, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_timer);
        findViewById(R.id.button_TimerNull).setOnClickListener(this);
        findViewById(R.id.button_Timer10m).setOnClickListener(this);
        findViewById(R.id.button_Timer30m).setOnClickListener(this);
        findViewById(R.id.button_Timer1h).setOnClickListener(this);
        findViewById(R.id.button_Timer2h).setOnClickListener(this);
        findViewById(R.id.button_TimerCustom).setOnClickListener(this);
        findViewById(R.id.button_TimerSet).setOnClickListener(this);
        findViewById(R.id.button_TimerCancel).setOnClickListener(this);
        editText_TimerMinute = findViewById(R.id.editText_TimerMinute);
        text_TimerRemainingTime = findViewById(R.id.text_TimerRemainingTime);
        Timer();
    }

    @Override
    public void onClick(View view) {
        editText_TimerMinute.setEnabled(false);
        switch (view.getId()) {
            case R.id.button_TimerNull: {
                countDownTime = -1;
                break;
            }
            case R.id.button_Timer10m: {
                countDownTime = 10 * 60 * 1000;
                break;
            }
            case R.id.button_Timer30m: {
                countDownTime = 30 * 60 * 1000;
                break;
            }
            case R.id.button_Timer1h: {
                countDownTime = 60 * 60 * 1000;
                break;
            }
            case R.id.button_Timer2h: {
                countDownTime = 120 * 60 * 1000;
                break;
            }
            case R.id.button_TimerCustom: {
                editText_TimerMinute.setEnabled(true);

                editText_TimerMinute.requestFocus();
                ((InputMethodManager) Objects.requireNonNull(activity.getSystemService(Context.INPUT_METHOD_SERVICE)))
                        .showSoftInput(editText_TimerMinute, InputMethodManager.SHOW_FORCED);

                editText_TimerMinute.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!editText_TimerMinute.getText().toString().equals(""))
                            countDownTime = Long.valueOf(editText_TimerMinute.getText().toString()) * 60 * 1000;
                    }
                });
                break;
            }

            case R.id.button_TimerSet: {
                MediaController.SetTimer(countDownTime);
            }
            case R.id.button_TimerCancel: {
                handler.removeCallbacksAndMessages(null);
                dismiss();
                break;
            }
        }
    }
}
