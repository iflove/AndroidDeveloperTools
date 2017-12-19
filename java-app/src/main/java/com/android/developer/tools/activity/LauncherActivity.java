package com.android.developer.tools.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.android.developer.tools.R;
import com.android.developer.tools.handler.HandlerTaskTimer;
import com.android.developer.tools.interfaces.Action;
import com.android.developer.tools.interfaces.Consumer;

import java.util.concurrent.TimeUnit;


public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG_COUNTDOWN = "CountDown";
    private static final long DELAY_LAUNCH_DEFAULT = 30;

    private Button activityMainButtonLaunch;
    private Button activityMainButtonDelayLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        initView();

    }

    private void initView() {
        activityMainButtonLaunch = findViewById(R.id.activity_main_button_launch);
        activityMainButtonDelayLaunch = findViewById(R.id.activity_main_button_delay_launch);
        activityMainButtonLaunch.setOnClickListener(this);
        activityMainButtonDelayLaunch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_button_delay_launch:
                if (v.getTag() == null) {
                    HandlerTaskTimer.getInstance().pause(TAG_COUNTDOWN);
                    activityMainButtonDelayLaunch.setText(getResources().getString(R.string.activity_main_button_delay_launch));
                    v.setTag(true);
                } else {
                    HandlerTaskTimer.getInstance().resume(TAG_COUNTDOWN);
                    v.setTag(null);
                }
                break;
            case R.id.activity_main_button_launch:
                performLaunchButtonClick();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLaunchCountDown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelLaunchCountDown();
    }

    void cancelLaunchCountDown() {
        HandlerTaskTimer.getInstance().cancel(TAG_COUNTDOWN);
    }


    private void startLaunchCountDown() {
        HandlerTaskTimer.getInstance().newBuilder()
                .tag(TAG_COUNTDOWN)
                .period(1, TimeUnit.SECONDS)
                .takeWhile(LauncherActivity.DELAY_LAUNCH_DEFAULT)
                .countDown()
                .accept(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        updateCancelLaunchButton(aLong);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        performLaunchButtonClick();
                    }
                }).start();
    }

    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    private void updateCancelLaunchButton(long count) {
        activityMainButtonDelayLaunch.setText(String.format(getResources().getString(R.string.activity_main_text_view_delay_time), count));
    }


    private void performLaunchButtonClick() {
        cancelLaunchCountDown();
        startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        finish();
    }

}
