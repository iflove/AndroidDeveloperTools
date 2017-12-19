package com.android.developer.tools.activity

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.developer.tools.R
import com.android.developer.tools.handler.HandlerTaskTimer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG_FETCH_DATA = "FetchData"
        private const val TAG_DELAY_TASK = "DelayTask"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startDelayTask()
        startFetchData()
    }

    private fun startDelayTask() {
        HandlerTaskTimer.newBuilder()
                .tag(TAG_DELAY_TASK)
                .initialDelay(1, TimeUnit.SECONDS)
                .delayExecute()
                .accept(action = { helloTextView.setTextColor(Color.RED) }).start()
    }


    private fun startFetchData() {
        HandlerTaskTimer.newBuilder()
                .tag(TAG_FETCH_DATA)
                .period(1, 3, TimeUnit.SECONDS)
                .loopExecute()
                .accept(action = { helloTextView.text = String.format("update at %s", Date().toString()) })
                .start()
    }

    override fun onPause() {
        super.onPause()
        HandlerTaskTimer.cancel(TAG_FETCH_DATA).cancel(TAG_DELAY_TASK)
    }
}
