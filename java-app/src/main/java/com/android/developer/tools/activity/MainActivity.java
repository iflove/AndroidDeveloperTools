package com.android.developer.tools.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.android.developer.tools.R;
import com.android.developer.tools.handler.HandlerTaskTimer;
import com.android.developer.tools.interfaces.Action;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_FETCH_DATA = "FetchData";
    private static final String TAG_DELAY_TASK = "DelayTask";
    private TextView helloTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        startDelayTask();
        startFetchData();
    }

    private void startDelayTask() {
        HandlerTaskTimer.getInstance().newBuilder()
                .tag(TAG_DELAY_TASK)
                .initialDelay(1, TimeUnit.SECONDS)
                .delayExecute()
                .accept(new Action() {
                    @Override
                    public void run() throws Exception {
                        helloTextView.setTextColor(Color.RED);
                    }
                }).start();
    }

    private void initView() {
        helloTextView = findViewById(R.id.helloTextView);
    }

    public void startFetchData() {
        HandlerTaskTimer.getInstance().newBuilder()
                .tag(TAG_FETCH_DATA)
                .period(1, 3, TimeUnit.SECONDS)
                .loopExecute()
                .accept(new Action() {
                    @Override
                    public void run() throws Exception {
                        helloTextView.setText(String.format("update at %s", new Date().toString()));
                    }
                }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        HandlerTaskTimer.getInstance().cancel(TAG_FETCH_DATA);
        HandlerTaskTimer.getInstance().cancel(TAG_DELAY_TASK);
    }
}
