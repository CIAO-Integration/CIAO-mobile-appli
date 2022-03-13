package com.ciao.app.activity;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.ciao.app.Database;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.service.JsonFromUrl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity showing a loading screen
 */
public class Loading extends AppCompatActivity {
    /**
     * Target for broadcast receiver
     */
    private final String USERINFO_TARGET = "UserInfo";
    /**
     * Target for broadcast receiver
     */
    private final String TIMELINE_TARGET = "Timeline";
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * Shared preferences editor
     */
    private SharedPreferences.Editor editor;
    /**
     * Opened from link
     */
    private String link;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();
        String action = intent.getAction();

        if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
            link = data.toString();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        Functions.setTheme(this);

        if (sharedPreferences.getBoolean("firstTime", true)) {
            startActivity(new Intent(this, Login.class));
            finish();
        } else {
            setContentView(R.layout.activity_loading);

            if (Functions.checkConnection(this)) {
                String key = sharedPreferences.getString("key", null);
                if (key != null) {
                    Map<String, String> arguments = new HashMap<>();
                    arguments.put("request", "userInfo");
                    arguments.put("key", key);
                    registerReceiver(new UserInfoReceiver(), new IntentFilter(USERINFO_TARGET));
                    Intent intent1 = new Intent(this, JsonFromUrl.class);
                    intent1.putExtra("arguments", (Serializable) arguments);
                    intent1.putExtra("target", USERINFO_TARGET);
                    intent1.putExtra("url", getString(R.string.WEB_SERVER_URL));
                    startService(intent1);
                } else {
                    Functions.refreshTimeline(this, new TimelineReceiver(), TIMELINE_TARGET);
                }
            } else {
                Functions.makeDialog(this, getString(R.string.error), getString(R.string.error_network)).show();
            }
        }
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    private class TimelineReceiver extends BroadcastReceiver {
        /**
         * On receive
         *
         * @param context Context
         * @param intent  Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(this);
            if (intent.getStringExtra("json") != null) {
                try {
                    JSONObject json = new JSONObject(intent.getStringExtra("json"));
                    String status = json.getString("status");
                    if (status.equals("200")) {
                        JSONArray array = json.getJSONArray("list");
                        Functions.storeTimeline(context, array, Database.TABLE_NAME);
                        Intent intent1 = new Intent(context, Main.class);
                        if (link != null) {
                            intent1.putExtra("link", link);
                        }
                        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(Loading.this, new Pair<>(findViewById(R.id.loading_logo), "logo"));
                        startActivity(intent1, activityOptions.toBundle());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 500);
                    } else {
                        Functions.makeDialog(context, getString(R.string.error), getString(R.string.error_message, status, json.getString("message"))).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.makeDialog(context, getString(R.string.error), e.toString()).show();
                }
            }
        }
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    private class UserInfoReceiver extends BroadcastReceiver {
        /**
         * On receive
         *
         * @param context Context
         * @param intent  Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(this);
            if (intent.getStringExtra("json") != null) {
                try {
                    JSONObject json = new JSONObject(intent.getStringExtra("json"));
                    String status = json.getString("status");
                    if (status.equals("200")) {
                        JSONObject object = json.getJSONObject("infos");
                        String username = object.getString("username");
                        String email = object.getString("email");
                        String avatar = object.getString("avatar");
                        String location = object.getString("location");
                        String ci = object.getString("ci");
                        String locations = object.getString("locations");
                        editor.putString("username", username);
                        editor.putString("email", email);
                        editor.putString("avatar", avatar);
                        if (location.equals("null")) {
                            editor.putBoolean("location_mode", false);
                            editor.remove("location");
                        } else {
                            editor.putBoolean("location_mode", true);
                            editor.putString("location", location);
                        }
                        editor.putString("ci", ci);
                        editor.putString("locations", locations);
                    } else if (status.equals("401")) {
                        editor.remove("username");
                        editor.remove("email");
                        editor.remove("key");
                        editor.remove("avatar");
                        editor.remove("ci");
                        editor.remove("location");
                        editor.remove("location_mode");
                        editor.remove("locations");
                    } else {
                        Functions.makeDialog(context, getString(R.string.error), getString(R.string.error_message, status, json.getString("message"))).show();
                    }
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.makeDialog(context, getString(R.string.error), e.toString()).show();
                }
            }
            Functions.refreshTimeline(Loading.this, new TimelineReceiver(), TIMELINE_TARGET);
        }
    }
}
