package com.android.developer.tools;

import android.app.Application;


/**
 * @author tianlong
 * @date 2017/12/18.
 */
public class MyApplication extends Application {
    public static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}