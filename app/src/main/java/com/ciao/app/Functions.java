package com.ciao.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.activity.Main;
import com.ciao.app.databinding.FragmentMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        Spinner spinner = binding.mainSort;
        RecyclerView recyclerView = binding.mainList;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Database database = new Database(context);
        String filter = null;
        String location = null;
        switch (which) {
            case R.string.browse:
                break;
            case R.string.around:
                location = PreferenceManager.getDefaultSharedPreferences(context).getString("location", null);
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
        ArrayList<HashMap<String, String>> data = database.getRowsByFilter(filter, location);
        database.close();
        Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(context, data);
        recyclerView.setAdapter(recyclerViewAdapter);
        SwipeRefreshLayout swipeRefreshLayout = binding.mainRefresh;
        String finalFilter = filter;
        String finalLocation = location;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTimeline(context, new Main.RefreshReceiver(finalFilter, finalLocation, recyclerView, swipeRefreshLayout, spinner), "Refresh");
            }
        });

        spinner.setAdapter(ArrayAdapter.createFromResource(context, R.array.sort, android.R.layout.simple_spinner_item));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Main.RecyclerViewAdapter recyclerViewAdapter = (Main.RecyclerViewAdapter) recyclerView.getAdapter();
                ArrayList<HashMap<String, String>> data = recyclerViewAdapter.getData();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                switch (position) {
                    case 0:
                        Collections.sort(data, new Comparator<HashMap<String, String>>() {
                            @Override
                            public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                                long date1 = 0;
                                long date2 = 0;
                                try {
                                    date1 = simpleDateFormat.parse(o1.get("date")).getTime();
                                    date2 = simpleDateFormat.parse(o2.get("date")).getTime();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return (int) (date2 - date1);
                            }
                        });
                        break;
                    case 1:
                        Collections.sort(data, new Comparator<HashMap<String, String>>() {
                            @Override
                            public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                                long date1 = 0;
                                long date2 = 0;
                                try {
                                    date1 = simpleDateFormat.parse(o1.get("date")).getTime();
                                    date2 = simpleDateFormat.parse(o2.get("date")).getTime();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return (int) (date1 - date2);
                            }
                        });
                        break;
                    case 2:
                        Collections.sort(data, new Comparator<HashMap<String, String>>() {
                            @Override
                            public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                                String title1 = o1.get("title");
                                String title2 = o2.get("title");
                                return title1.compareTo(title2);
                            }
                        });
                        break;
                    case 3:
                        Collections.sort(data, new Comparator<HashMap<String, String>>() {
                            @Override
                            public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                                String title1 = o1.get("title");
                                String title2 = o2.get("title");
                                return title2.compareTo(title1);
                            }
                        });
                        break;
                }
                recyclerView.setAdapter(new Main.RecyclerViewAdapter(context, data));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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

    /**
     * Show error dialog
     *
     * @param context Context
     * @param message Message
     */
    public static void showErrorDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.error));
        builder.setMessage(message);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Make progress dialog
     *
     * @param context Context
     * @return Progress dialog
     */
    public static Dialog makeLoadingDialog(Context context) {
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        TextView textView = new TextView(context);
        textView.setText(context.getString(R.string.loading_message));
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(progressBar);
        linearLayout.addView(textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.loading));
        builder.setView(linearLayout);
        builder.setCancelable(false);
        return builder.create();
    }
}
