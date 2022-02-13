package com.ciao.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Map;

/**
 * Get JSON from URL
 */
public class JsonFromUrl extends Service implements Runnable {
    /**
     * POST arguments
     */
    private Map<String, String> arguments;
    /**
     * Target for broadcast receiver
     */
    private String target;

    /**
     * On bind
     *
     * @param intent Intent
     * @return null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * On start command
     *
     * @param intent  Intent
     * @param flags   Flags
     * @param startId Start ID
     * @return On start command
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        arguments = (Map<String, String>) intent.getSerializableExtra("arguments");
        target = intent.getStringExtra("target");
        Thread thread = new Thread(this);
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Run
     */
    @Override
    public void run() {
        String url = BuildConfig.WEB_SERVER_URL;
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Intent intent = new Intent(target);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("JsonFromUrl", response);
                intent.putExtra("json", response);
                sendBroadcast(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendBroadcast(intent);
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return arguments;
            }
        };
        requestQueue.add(stringRequest);
    }
}
