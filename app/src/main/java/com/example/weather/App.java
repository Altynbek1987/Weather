package com.example.weather;

import android.app.Application;

import com.example.weather.save.PreferencesUtils;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PreferencesUtils.initPreferences(this);
    }
}
