package com.ciao.app;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.activity.Main;
import com.ciao.app.activity.Production;
import com.ciao.app.databinding.FragmentMainBinding;
import com.ciao.app.receiver.RefreshReceiver;
import com.ciao.app.service.JsonFromUrl;
import com.ciao.app.service.NotificationJob;
import com.ciao.app.view.Tag;

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
     * @param activity Activity
     * @param binding  Binding to get Views
     * @param which    Which Fragment
     */
    public static void initFragment(AppCompatActivity activity, FragmentMainBinding binding, int which) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        Spinner spinner = binding.mainSort;
        RecyclerView recyclerView = binding.mainList;
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        Database database = new Database(activity);
        String filter = null;
        String location = null;
        switch (which) {
            case R.string.browse:
                break;
            case R.string.nearby:
                location = sharedPreferences.getString("location", null);
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
        Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(activity, data);
        recyclerView.setAdapter(recyclerViewAdapter);
        SwipeRefreshLayout swipeRefreshLayout = binding.mainRefresh;
        String finalFilter = filter;
        String finalLocation = location;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTimeline(activity, new RefreshReceiver(finalFilter, finalLocation, recyclerView, swipeRefreshLayout, spinner, activity), "Refresh");
            }
        });

        spinner.setAdapter(ArrayAdapter.createFromResource(activity, R.array.sort, android.R.layout.simple_spinner_item));
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
                recyclerView.setAdapter(new Main.RecyclerViewAdapter(activity, data));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (sharedPreferences.getString("key", null) != null && which != R.string.browse && which != R.string.nearby) {
            LinearLayout linearLayout = binding.mainPlaceholder;
            linearLayout.addView(new Tag(activity, filter));
        }
    }

    /**
     * Get data to populate timeline
     *
     * @param activity          Activity
     * @param broadcastReceiver Broadcast receiver
     * @param target            Target
     */
    public static void refreshTimeline(AppCompatActivity activity, BroadcastReceiver broadcastReceiver, String target) {
        Map<String, String> arguments = new HashMap();
        arguments.put("request", "timeline");
        activity.registerReceiver(broadcastReceiver, new IntentFilter(target));
        Intent intent = new Intent(activity, JsonFromUrl.class);
        intent.putExtra("arguments", (Serializable) arguments);
        intent.putExtra("target", target);
        intent.putExtra("url", BuildConfig.WEB_SERVER_URL);
        activity.startService(intent);
    }

    /**
     * Store data into database
     *
     * @param context Context
     * @param array   Data
     * @throws JSONException Bad JSON
     */
    public static void storeTimeline(Context context, JSONArray array, String table) throws JSONException {
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
            row.put("type", item.getString("type"));
            row.put("path", item.getString("path"));
            row.put("link", item.getString("link"));
            row.put("description", item.getString("description"));
            row.put("author", item.getString("author"));
            rows.add(row);
        }
        Database database = new Database(context);
        database.clear(table);
        database.insertInto(table, rows);
        database.close();
    }

    /**
     * Set app theme
     *
     * @param context Context
     */
    public static void setTheme(Context context) {
        switch (PreferenceManager.getDefaultSharedPreferences(context).getString("theme", "light")) {
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
     * @return Loading dialog
     */
    public static Dialog makeDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return builder.create();
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
        int padding = (int) context.getResources().getDimension(R.dimen.dialog_padding);
        linearLayout.setPadding(padding, padding, padding, padding);
        linearLayout.addView(progressBar);
        linearLayout.addView(textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.loading));
        builder.setView(linearLayout);
        builder.setCancelable(false);
        return builder.create();
    }

    /**
     * Check connection
     *
     * @param context Context
     * @return Connected
     */
    public static boolean checkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Check if one version is greater than another version
     *
     * @param version1 Version 1
     * @param version2 Version 2
     * @return Is greater
     */
    public static boolean isGreater(String version1, String version2) {
        int[] v1 = stringToInt(version1.split("\\."));
        int[] v2 = stringToInt(version2.split("\\."));
        if (v1[0] > v2[0]) {
            return true;
        } else if (v1[0] == v2[0]) {
            if (v1[1] > v2[1]) {
                return true;
            } else if (v1[1] == v2[1]) {
                if (v1[2] > v2[2]) {
                    return true;
                } else if (v1[2] == v2[2]) {
                    return false;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Convert an array of String of an array of Integer
     *
     * @param string Array of String
     * @return Array of Integer
     */
    public static int[] stringToInt(String[] string) {
        int[] integer = new int[3];
        for (int i = 0; i < string.length; i++) {
            integer[i] = Integer.parseInt(string[i]);
        }
        return integer;
    }

    /**
     * Initiate notification system
     *
     * @param context Context
     */
    public static void initNotifications(Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getString("key", null) != null) {
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            boolean found = false;
            for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
                if (jobInfo.getId() == 0) {
                    found = true;
                    Log.d("Notifications", "service already running");
                    break;
                }
            }
            if (!found) {
                Log.d("Notifications", "started service");
                createNotificationChannel(context);
                ComponentName componentName = new ComponentName(context, NotificationJob.class);
                JobInfo.Builder builder = new JobInfo.Builder(0, componentName);
                builder.setPeriodic(1000 * 60 * 60);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                builder.setRequiresCharging(false);
                builder.setRequiresDeviceIdle(false);
                builder.setPersisted(true);
                jobScheduler.schedule(builder.build());
            }
        }
    }

    /**
     * Create notification channel
     *
     * @param context Context
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CIAO", context.getString(R.string.app_name) + " " + context.getString(R.string.notifications), NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(context.getString(R.string.notifications_description));
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Make a notification
     *
     * @param context      Context
     * @param title        Title
     * @param text         Text
     * @param productionId Id of production
     */
    public static void makeNotification(Context context, String title, String text, String productionId) {
        Intent intent = new Intent(context, Production.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("productionId", productionId);
        intent.putExtra("external", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) (Math.random() * 1000), intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CIAO")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) (Math.random() * 1000), builder.build());
    }
}
