package com.ciao.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.activity.Main;
import com.ciao.app.activity.MainFragment.RefreshReceiver;
import com.ciao.app.databinding.FragmentMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Functions
 */
public class Functions {
    /**
     * Init a Fragment of Main Activity
     *
     * @param context Context
     * @param binding Binding to get Views
     * @param which   Which Fragment
     */
    public static void initFragment(Context context, FragmentMainBinding binding, int which) {
        RecyclerView recyclerView = binding.mainList;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Database database = new Database(context);
        String filter = null;
        String location = null;
        switch (which) {
            case R.string.browse:
                break;
            case R.string.around:
                //location = ;
                break;
            case R.string.news:
                filter = "actualite";
                break;
            case R.string.popularization:
                filter = "vulgarisation";
                break;
            case R.string.digital:
                filter = "numerique";
                break;
            case R.string.science:
                filter = "science";
                break;
            case R.string.culture:
                filter = "culture";
                break;
            case R.string.history:
                filter = "histoire";
                break;
            case R.string.geography:
                filter = "geographie";
                break;
            case R.string.politics:
                filter = "politique";
                break;
            case R.string.sport:
                filter = "sport";
                break;
        }
        ArrayList<HashMap<String, String>> data = database.getRows(filter, location);
        database.close();
        Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(context, data);
        recyclerView.setAdapter(recyclerViewAdapter);
        SwipeRefreshLayout swipeRefreshLayout = binding.mainRefresh;
        String finalFilter = filter;
        String finalLocation = location;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTimeline(context, new RefreshReceiver(finalFilter, finalLocation, recyclerView, swipeRefreshLayout), "Refresh");
            }
        });
    }

    /**
     * Get data to populate timeline
     *
     * @param context           Context
     * @param broadcastReceiver Broadcast receiver
     * @param target            Target
     */
    public static void refreshTimeline(Context context, BroadcastReceiver broadcastReceiver, String target) {
        Map<String, String> arguments = new HashMap();
        arguments.put("request", "timeline");
        context.registerReceiver(broadcastReceiver, new IntentFilter(target));
        Intent intent = new Intent(context, JsonFromUrl.class);
        intent.putExtra("arguments", (Serializable) arguments);
        intent.putExtra("target", target);
        context.startService(intent);
    }

    /**
     * Store data into database
     *
     * @param context Context
     * @param array   Data
     * @throws JSONException Bad JSON
     */
    public static void storeTimeline(Context context, JSONArray array) throws JSONException {
        ArrayList<HashMap<String, String>> rows = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);
            HashMap<String, String> row = new HashMap<>();
            row.put("id", item.getString("id"));
            row.put("thumbnail", item.getString("thumbnail"));
            row.put("title", item.getString("title"));
            row.put("tags", item.getString("tags"));
            row.put("date", item.getString("date"));
            row.put("location", item.getString("location"));
            rows.add(row);
        }
        Database database = new Database(context);
        database.clear();
        database.insertInto(rows);
        database.close();
    }

    /**
     * Set app theme
     *
     * @param theme Theme
     */
    public static void setTheme(String theme) {
        switch (theme) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    /**
     * Open an URL in web browser
     *
     * @param context Context
     * @param url     Url
     */
    public static void openUrl(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}
