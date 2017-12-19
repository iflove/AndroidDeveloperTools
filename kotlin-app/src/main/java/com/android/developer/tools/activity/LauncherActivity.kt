package com.android.developer.tools.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button

import com.android.developer.tools.R
import com.android.developer.tools.handler.HandlerTaskTimer


import java.util.concurrent.TimeUnit


class LauncherActivity : AppCompatActivity(), View.OnClickListener {

    private var activityMainButtonLaunch: Button? = null
    private var activityMainButtonDelayLaunch: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        initView()

    }

    private fun initView() {
        activityMainButtonLaunch = findViewById(R.id.activity_main_button_launch)
        activityMainButtonDelayLaunch = findViewById(R.id.activity_main_button_delay_launch)
        activityMainButtonLaunch!!.setOnClickListener(this)
        activityMainButtonDelayLaunch!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.activity_main_button_delay_launch -> if (v.tag == null) {
                HandlerTaskTimer.pause(TAG_COUNTDOWN)
                activityMainButtonDelayLaunch!!.text = resources.getString(R.string.activity_main_button_delay_launch)
                v.tag = true
            } else {
                HandlerTaskTimer.resume(TAG_COUNTDOWN)
                v.tag = null
            }
            R.id.activity_main_button_launch -> performLaunchButtonClick()
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLaunchCountDown()
    }

    override fun onPause() {
        super.onPause()
        cancelLaunchCountDown()
    }

    internal fun cancelLaunchCountDown() {
        HandlerTaskTimer.cancel(TAG_COUNTDOWN)
    }


    private fun startLaunchCountDown() {
        HandlerTaskTimer.newBuilder()
                .tag(TAG_COUNTDOWN)
                .period(1, TimeUnit.SECONDS)
                .takeWhile(LauncherActivity.DELAY_LAUNCH_DEFAULT)
                .countDown()
                .accept({ updateCancelLaunchButton(it) }, { performLaunchButtonClick() })
                .start();
    }

    @SuppressLint("SetTextI18n", "StringFormatMatches")
    private fun updateCancelLaunchButton(count: Long) {
        activityMainButtonDelayLaunch!!.text = String.format(resources.getString(R.string.activity_main_text_view_delay_time), count)
    }


    private fun performLaunchButtonClick() {
        cancelLaunchCountDown()
        startActivity(Intent(this@LauncherActivity, MainActivity::class.java))
        finish()
    }

    companion object {
        private val TAG_COUNTDOWN = "CountDown"
        private val DELAY_LAUNCH_DEFAULT: Long = 30
    }

}
