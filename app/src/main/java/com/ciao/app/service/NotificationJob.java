package com.ciao.app.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.ciao.app.Database;
import com.ciao.app.Functions;
import com.ciao.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Check for new production every hours
 */
public class NotificationJob extends JobService {
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;

    /**
     * On start
     *
     * @param params JobParameters
     * @return True
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.getString("key", null) != null) {
            Functions.refreshTimeline(getApplicationContext(), new TimelineReceiver(), "Notifications");
            Functions.initNotifications(getApplicationContext(), false);
        }
        return true;
    }

    /**
     * On stop
     *
     * @param params JobParameters
     * @return True
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
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
            context.getApplicationContext().unregisterReceiver(this);
            if (intent.getStringExtra("json") != null) {
                try {
                    JSONObject json = new JSONObject(intent.getStringExtra("json"));
                    String status = json.getString("status");
                    if (status.equals("200")) {
                        JSONArray array = json.getJSONArray("list");
                        Functions.storeTimeline(context, array, Database.TMP_TABLE_NAME);
                        Database database = new Database(context);
                        ArrayList<HashMap<String, String>> rows = database.compareTables();
                        database.close();
                        if (rows.size() > 0) {
                            Functions.storeTimeline(context, array, Database.TABLE_NAME);
                            for (HashMap<String, String> row : rows) {
                                String[] productionCi = row.get("tags").split(",");
                                String[] userCi = sharedPreferences.getString("ci", "").split(",");
                                boolean found = false;
                                for (String p : productionCi) {
                                    for (String u : userCi) {
                                        if (p.equals(u)) {
                                            String title = row.get("title");
                                            String type = row.get("type");
                                            String text = "";
                                            if (type.equals("video")) {
                                                text = "(" + context.getString(R.string.video) + ") " + title;
                                            } else if (type.equals("article")) {
                                                text = "(" + context.getString(R.string.article) + ") " + title;
                                            }
                                            String id = row.get("id");
                                            Functions.makeNotification(context, context.getString(R.string.new_production), text, id);
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (found) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
