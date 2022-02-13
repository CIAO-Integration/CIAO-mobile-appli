package com.ciao.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.ciao.app.Functions;
import com.ciao.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity showing a loading screen
 */
public class Loading extends AppCompatActivity {
    /**
     * Target for broadcast receiver
     */
    private final String TARGET = "Loading";
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * Broadcast receiver
     */
    private JsonReceiver jsonReceiver;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Boolean firstTime = sharedPreferences.getBoolean("firstTime", true);
        if (firstTime) {
            finish();
            startActivity(new Intent(this, Login.class));
        }

        String theme = sharedPreferences.getString("theme", "light");
        Functions.setTheme(theme);

        setContentView(R.layout.activity_loading);

        jsonReceiver = new JsonReceiver();
        Functions.refreshTimeline(this, jsonReceiver, TARGET);
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    private class JsonReceiver extends BroadcastReceiver {
        /**
         * On receive
         *
         * @param context Context
         * @param intent  Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(jsonReceiver);
            if (intent.getStringExtra("json") != null) {
                try {
                    JSONObject json = new JSONObject(intent.getStringExtra("json"));
                    String status = json.getString("status");
                    if (status.equals("200")) {
                        JSONArray array = json.getJSONArray("list");
                        Functions.storeTimeline(context, array);
                        startActivity(new Intent(context, Main.class));
                        finish();
                    } else {
                        //error
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
