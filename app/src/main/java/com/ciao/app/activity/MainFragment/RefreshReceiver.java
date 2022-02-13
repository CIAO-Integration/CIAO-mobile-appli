package com.ciao.app.activity.MainFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.Database;
import com.ciao.app.Functions;
import com.ciao.app.activity.Main;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Broadcast receiver for refreshing timeline
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
     * Constructor
     *
     * @param filter             Filter
     * @param location           Location
     * @param recyclerView       RecyclerView
     * @param swipeRefreshLayout SwiperRefreshLayout
     */
    public RefreshReceiver(String filter, String location, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout) {
        this.filter = filter;
        this.location = location;
        this.recyclerView = recyclerView;
        this.swipeRefreshLayout = swipeRefreshLayout;
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
                    Functions.storeTimeline(context, array);
                    Database database = new Database(context);
                    Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(context, database.getRows(filter, location));
                    database.close();
                    recyclerView.setAdapter(recyclerViewAdapter);
                } else {
                    //error
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }
}
