package com.ciao.app.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ciao.app.BuildConfig;
import com.ciao.app.Database;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.service.JsonFromUrl;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity showing the main screen
 */
public class Main extends AppCompatActivity {
    /**
     * Target for broadcast receiver
     */
    private final String TARGET = "Main";
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * API key
     */
    private String key;
    /**
     * Navigation view
     */
    private NavigationView navigationView;
    /**
     * Search bar
     */
    private EditText searchBar;
    /**
     * Progress dialog
     */
    private Dialog progressDialog;
    /**
     * Filename
     */
    private String name;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        checkUpdate();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        key = sharedPreferences.getString("key", null);
        navigationView = findViewById(R.id.nav_view);
        searchBar = navigationView.getHeaderView(0).findViewById(R.id.main_searchBar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                String text = null;
                switch (destination.getId()) {
                    case R.id.nav_browse:
                        text = getString(R.string.browse);
                        break;
                    case R.id.nav_around:
                        text = getString(R.string.around);
                        break;
                    case R.id.nav_news:
                        text = getString(R.string.news);
                        break;
                    case R.id.nav_popularization:
                        text = getString(R.string.popularization);
                        break;
                    case R.id.nav_digital:
                        text = getString(R.string.digital);
                        break;
                    case R.id.nav_science:
                        text = getString(R.string.science);
                        break;
                    case R.id.nav_culture:
                        text = getString(R.string.culture);
                        break;
                    case R.id.nav_history:
                        text = getString(R.string.history);
                        break;
                    case R.id.nav_geography:
                        text = getString(R.string.geography);
                        break;
                    case R.id.nav_politics:
                        text = getString(R.string.politics);
                        break;
                    case R.id.nav_sport:
                        text = getString(R.string.sport);
                        break;
                }
                TextView title = findViewById(R.id.actionbar_title);
                title.setText(text);
            }
        });
        NavigationUI.setupWithNavController(navigationView, navController);

        findViewById(R.id.actionbar_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer();
            }
        });
        findViewById(R.id.actionbar_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings();
            }
        });

        if (key != null) {
            String avatar = sharedPreferences.getString("avatar", null);
            if (avatar != null && !avatar.equals("null")) {
                ImageView imageView = findViewById(R.id.actionbar_avatar);
                if (!avatar.startsWith("http")) {
                    avatar = BuildConfig.STORAGE_SERVER_URL + avatar;
                }
                Drawable placeholder = AppCompatResources.getDrawable(this, R.drawable.no_avatar);
                Glide.with(this).load(avatar).placeholder(placeholder).error(placeholder).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
            }
        }

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search(null);
                return false;
            }
        });
    }

    /**
     * On resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (key != null) {
            Menu menu = navigationView.getMenu();
            if (sharedPreferences.getBoolean("location_mode", false) && sharedPreferences.getString("location", null) != null) {
                menu.findItem(R.id.nav_around).setVisible(true);
            } else {
                menu.findItem(R.id.nav_around).setVisible(false);
            }
        }
    }

    /**
     * Check update from GitHub
     */
    public void checkUpdate() {
        registerReceiver(new UpdateReceiver(), new IntentFilter(TARGET));
        Intent intent = new Intent(this, JsonFromUrl.class);
        intent.putExtra("target", TARGET);
        intent.putExtra("url", "https://api.github.com/repos/CIAO-Integration/CIAO-mobile-appli/releases/latest");
        startService(intent);
    }

    /**
     * Open navigation drawer
     */
    public void openDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.open();
    }

    /**
     * Start Settings Activity
     */
    public void settings() {
        startActivity(new Intent(this, Settings.class));
    }

    /**
     * Search
     *
     * @param view View
     */
    public void search(View view) {
        Intent intent = new Intent(this, Search.class);
        intent.putExtra("search", searchBar.getText().toString());
        startActivity(intent);
    }

    /**
     * Adapter for RecyclerView
     */
    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        /**
         * Context
         */
        private final Context context;
        /**
         * Data
         */
        private final ArrayList<HashMap<String, String>> data;

        /**
         * Constructor
         *
         * @param context Context
         * @param data    Data
         */
        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> data) {
            this.context = context;
            this.data = data;
        }

        /**
         * Action on creation of ViewHolder
         *
         * @param parent   ViewGroup
         * @param viewType Type of view
         * @return ViewHolder
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.list_item_main, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
        }

        /**
         * Action on bind of ViewHolder
         *
         * @param holder   ViewHolder
         * @param position Position
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.title.setText(data.get(position).get("title"));
            if (data.get(position).get("thumbnail") != null) {
                String source = data.get(position).get("thumbnail");
                if (!source.startsWith("http")) {
                    source = BuildConfig.STORAGE_SERVER_URL + source;
                }
                Drawable placeholder = AppCompatResources.getDrawable(context, R.drawable.no_image);
                Glide.with(context).load(source).placeholder(placeholder).error(placeholder).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.image);
            }
            if (data.get(position).get("type").equals("video")) {
                holder.image.setForeground(AppCompatResources.getDrawable(context, android.R.drawable.ic_media_play));
            }
            String id = data.get(position).get("id");
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean connected = Functions.checkConnection(context);
                    if (connected) {
                        Intent intent = new Intent(context, Production.class);
                        intent.putExtra("id", id);
                        context.startActivity(intent);
                    } else {
                        Functions.makeErrorDialog(context, context.getString(R.string.error_network)).show();
                    }
                }
            });
        }

        /**
         * Get number of items
         *
         * @return Number of items
         */
        @Override
        public int getItemCount() {
            return data.size();
        }

        /**
         * Get data
         *
         * @return Data
         */
        public ArrayList<HashMap<String, String>> getData() {
            return data;
        }

        /**
         * ViewHolder
         */
        private class ViewHolder extends RecyclerView.ViewHolder {
            /**
             * Card
             */
            private final CardView card;
            /**
             * Image of item
             */
            private final ImageView image;
            /**
             * Title of item
             */
            private final TextView title;

            /**
             * Constructor
             *
             * @param view View
             */
            public ViewHolder(View view) {
                super(view);
                card = view.findViewById(R.id.item_card);
                image = view.findViewById(R.id.item_image);
                title = view.findViewById(R.id.item_title);
            }
        }
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    public static class RefreshReceiver extends BroadcastReceiver {
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
                        Functions.storeTimeline(context, array);
                        Database database = new Database(context);
                        Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(context, database.getRowsByFilter(filter, location));
                        database.close();
                        recyclerView.setAdapter(recyclerViewAdapter);
                    } else {
                        Functions.makeErrorDialog(context, context.getString(R.string.error_message, status, json.getString("message"))).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.makeErrorDialog(context, e.toString()).show();
                }
            }
            swipeRefreshLayout.setRefreshing(false);
            spinner.setSelection(0);
        }
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    private class UpdateReceiver extends BroadcastReceiver {
        /**
         * On receive
         *
         * @param context Context
         * @param intent  Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(this);
            if (intent.getStringExtra("json") != null) {
                try {
                    JSONObject json = new JSONObject(intent.getStringExtra("json"));
                    String currentVersion = BuildConfig.VERSION_NAME;
                    String remoteVersion = json.getString("tag_name");
                    if (Functions.isGreater(remoteVersion, currentVersion)) {
                        JSONArray array = json.getJSONArray("assets");
                        for (int i = 0; i < array.length(); i++) {
                            String type = array.getJSONObject(0).getString("content_type");
                            if (type.equals("application/vnd.android.package-archive")) { //contains apk
                                name = array.getJSONObject(0).getString("name");
                                String url = array.getJSONObject(0).getString("browser_download_url");
                                String changelog = json.getString("body");

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle(getString(R.string.new_update));
                                builder.setMessage(getString(R.string.update_message, currentVersion + " -> " + remoteVersion, changelog));
                                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
                                        progressBar.setIndeterminate(false);
                                        TextView percentage = new TextView(context);
                                        percentage.setText("0%");
                                        LinearLayout linearLayout = new LinearLayout(context);
                                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                                        linearLayout.setGravity(Gravity.START);
                                        linearLayout.addView(progressBar);
                                        linearLayout.addView(percentage);
                                        int padding = (int) getResources().getDimension(R.dimen.dialog_padding);
                                        linearLayout.setPadding(padding, padding, padding, padding);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setTitle(context.getString(R.string.downloading));
                                        builder.setView(linearLayout);
                                        builder.setCancelable(false);
                                        progressDialog = builder.create();
                                        progressDialog.show();

                                        registerReceiver(new DownloadManagerReceiver(), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                        long DOWNLOAD_ID = downloadManager.enqueue(new DownloadManager.Request(Uri.parse(url))
                                                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name));

                                        //download progress
                                        Thread thread = new Thread(new Runnable() {
                                            @SuppressLint("Range")
                                            @Override
                                            public void run() {
                                                boolean running = true;
                                                long total;
                                                long downloaded;
                                                int progress = 0;
                                                while (running) {
                                                    Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(DOWNLOAD_ID));
                                                    cursor.moveToFirst();
                                                    total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                                    downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                                    progress = (int) ((downloaded * 100) / total);
                                                    progressBar.setProgress(progress);
                                                    percentage.setText(progress + "%");
                                                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                                        name = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                                                    }
                                                    try {
                                                        Thread.sleep(100);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (downloaded >= total && total != -1) {
                                                        running = false;
                                                    }
                                                }
                                            }
                                        });
                                        thread.start();
                                    }
                                });
                                builder.setNegativeButton(getString(R.string.later), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                                break;
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.makeErrorDialog(context, e.toString()).show();
                }
            }
        }
    }

    /**
     * Broadcast receiver for DownloadManager
     */
    private class DownloadManagerReceiver extends BroadcastReceiver {
        /**
         * On receive
         *
         * @param context Context
         * @param intent  Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(this);
            progressDialog.cancel();
            Intent intent1 = new Intent(Intent.ACTION_VIEW);
            intent1.setDataAndType(FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name)), "application/vnd.android.package-archive");
            intent1.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        }
    }
}