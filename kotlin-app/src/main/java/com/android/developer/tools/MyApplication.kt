package com.android.developer.tools

import android.app.Application

/**
 * @author tianlong
 * @date  2017/12/18.
 */
class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}