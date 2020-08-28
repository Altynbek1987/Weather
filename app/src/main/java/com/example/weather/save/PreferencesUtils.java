package com.example.weather.save;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.weather.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PreferencesUtils {
    private static SharedPreferences preferences;
    private static Context ctx;
    public static final String LOCATION_KEY = "LatLng";

    public static void initPreferences(Context context) {
        ctx = context;
        preferences = context.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
    }

    public static void saveLocation(List<LatLng> latLng) {
        preferences = ctx.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(latLng);
        editor.putString(LOCATION_KEY, json);
        editor.apply();
        Toast.makeText(ctx, "Данные сохранены!", Toast.LENGTH_SHORT).show();
    }


    public static void clear() {
        preferences = ctx.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = preferences.edit();
        editor.clear().apply();
    }


    public static List<LatLng> getLocation(List<LatLng> latLngs) {
        preferences = ctx.getSharedPreferences(Config.PREF, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString(LOCATION_KEY, null);
        Type type = new TypeToken<List<LatLng>>() {
        }.getType();
        latLngs = gson.fromJson(json, type);
        if (latLngs == null) {
            latLngs = new ArrayList<>();
        }
        return latLngs;
    }

}
