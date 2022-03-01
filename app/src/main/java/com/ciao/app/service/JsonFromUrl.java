package com.ciao.app.service;

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
     * URL
     */
    private String url;

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
        if (intent != null) {
            arguments = (Map<String, String>) intent.getSerializableExtra("arguments");
            target = intent.getStringExtra("target");
            url = intent.getStringExtra("url");
            Thread thread = new Thread(this);
            thread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Run
     */
    @Override
    public void run() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Intent intent = new Intent(target);
        int method;
        if (arguments == null) {
            method = Request.Method.GET;
        } else {
            method = Request.Method.POST;
        }
        StringRequest stringRequest = new StringRequest(method, url, new Response.Listener<String>() {
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