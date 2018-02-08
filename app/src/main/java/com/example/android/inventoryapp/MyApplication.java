package com.example.android.inventoryapp;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    //static method exists solely to get application context from anywhere within the app
    public static Context getAppContext() {
        return MyApplication.context;
    }
}