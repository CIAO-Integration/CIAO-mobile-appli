package com.ciao.app.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ciao.app.Database;
import com.ciao.app.Functions;
import com.ciao.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Check for new production
 */
public class NotificationJob extends JobService {
    /**
     * On start
     *
     * @param params JobParameters
     * @return True
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("key", null) != null) {
            Map<String, String> arguments = new HashMap();
            arguments.put("request", "timeline");
            String url = getString(R.string.WEB_SERVER_URL);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("JsonFromUrl", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.getString("status");
                        if (status.equals("200")) {
                            JSONArray array = json.getJSONArray("list");
                            Functions.storeTimeline(NotificationJob.this, array, Database.TMP_TABLE_NAME);
                            Database database = new Database(NotificationJob.this);
                            ArrayList<HashMap<String, String>> rows = database.compareTables();
                            database.close();
                            if (rows.size() > 0) {
                                Functions.storeTimeline(NotificationJob.this, array, Database.TABLE_NAME);
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
                                                    text = "(" + NotificationJob.this.getString(R.string.video) + ") " + title;
                                                } else if (type.equals("article")) {
                                                    text = "(" + NotificationJob.this.getString(R.string.article) + ") " + title;
                                                }
                                                String id = row.get("id");
                                                Functions.makeNotification(NotificationJob.this, NotificationJob.this.getString(R.string.new_production), text, id);
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
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }) {
                @Nullable
                @Override
                protected Map<String, String> getParams() {
                    return arguments;
                }
            };
            requestQueue.add(stringRequest);
        } else {
            jobFinished(params, false);
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
}
