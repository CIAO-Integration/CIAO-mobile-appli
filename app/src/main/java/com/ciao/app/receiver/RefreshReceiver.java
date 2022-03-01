package com.ciao.app.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Spinner;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.Database;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.activity.Main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Broadcast receiver for JsonFromUrl
 */
public class RefreshReceiver extends BroadcastReceiver {
    /**
     * Timeline filter
     */
    private final String filter;
    /**
     * Timeline location
     */
    private final String location;
    /**
     * Recycler view
     */
    private final RecyclerView recyclerView;
    /**
     * Swipe refresh layout
     */
    private final SwipeRefreshLayout swipeRefreshLayout;
    /**
     * Spinner
     */
    private final Spinner spinner;

    /**
     * Constructor
     *
     * @param filter             Filter
     * @param location           Location
     * @param recyclerView       RecyclerView
     * @param swipeRefreshLayout SwiperRefreshLayout
     * @param spinner            Spinner
     */
    public RefreshReceiver(String filter, String location, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout, Spinner spinner) {
        this.filter = filter;
        this.location = location;
        this.recyclerView = recyclerView;
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.spinner = spinner;
    }

    /**
     * On receive
     *
     * @param context Context
     * @param intent  Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        context.unregisterReceiver(this);
        if (intent.getStringExtra("json") != null) {
            try {
                JSONObject json = new JSONObject(intent.getStringExtra("json"));
                String status = json.getString("status");
                if (status.equals("200")) {
                    JSONArray array = json.getJSONArray("list");
                    Functions.storeTimeline(context, array, Database.TABLE_NAME);
                    Database database = new Database(context);
                    Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(context, database.getRowsByFilter(filter, location));
                    database.close();
                    recyclerView.setAdapter(recyclerViewAdapter);
                } else {
                    Functions.makeDialog(context, context.getString(R.string.error), context.getString(R.string.error_message, status, json.getString("message"))).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Functions.makeDialog(context, context.getString(R.string.error), e.toString()).show();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
        spinner.setSelection(0);
    }
}
