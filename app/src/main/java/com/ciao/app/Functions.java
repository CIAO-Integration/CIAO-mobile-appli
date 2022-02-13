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
        SwipeRefreshLayout swipeRefreshLayout = binding.mainRefresh;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        Database database = new Database(context);
        ArrayList<HashMap<String, String>> data = null;
        switch (which) {
            case R.string.browse:
                data = database.getRows(null, null);
                break;
            case R.string.around:
                //data = database.getRows();
                break;
            case R.string.news:
                data = database.getRows("actualite", null);
                break;
            case R.string.popularization:
                data = database.getRows("vulgarisation", null);
                break;
            case R.string.digital:
                data = database.getRows("numerique", null);
                break;
            case R.string.science:
                data = database.getRows("science", null);
                break;
            case R.string.culture:
                data = database.getRows("culture", null);
                break;
            case R.string.history:
                data = database.getRows("histoire", null);
                break;
            case R.string.geography:
                data = database.getRows("geographie", null);
                break;
            case R.string.politics:
                data = database.getRows("politique", null);
                break;
            case R.string.sport:
                data = database.getRows("sport", null);
                break;
        }
        database.close();
        Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(context, data);
        recyclerView.setAdapter(recyclerViewAdapter);
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
