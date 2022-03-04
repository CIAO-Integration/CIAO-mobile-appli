package com.ciao.app.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ciao.app.BuildConfig;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.service.Gps;
import com.ciao.app.service.JsonFromUrl;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity showing settings
 */
public class Settings extends AppCompatActivity {
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * API key
     */
    private String key;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        key = sharedPreferences.getString("key", null);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        if (key != null) {
            String avatar = sharedPreferences.getString("avatar", null);
            if (avatar != null && !avatar.equals("null")) {
                ImageView imageView = findViewById(R.id.settings_avatar);
                Drawable placeholder = AppCompatResources.getDrawable(this, R.drawable.no_avatar);
                if (!avatar.startsWith("http")) {
                    avatar = BuildConfig.STORAGE_SERVER_URL + avatar;
                }
                Glide.with(this).load(avatar).placeholder(placeholder).error(placeholder).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
            }

            String username = sharedPreferences.getString("username", null);
            if (username != null) {
                TextView textView = findViewById(R.id.settings_name);
                textView.setText(username);
            }

            String email = sharedPreferences.getString("email", null);
            if (email != null) {
                TextView textView = findViewById(R.id.settings_email);
                textView.setText(email);
            }
        }
    }

    /**
     * Actual settings
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        /**
         * Target for broadcast receiver
         */
        private final String GPS_TARGET = "GPS";
        /**
         * Context
         */
        private Context context;
        /**
         * Shared preferences
         */
        private SharedPreferences sharedPreferences;
        /**
         * Shared preferences editor
         */
        private SharedPreferences.Editor editor;
        /**
         * On shared preferences change listener
         */
        private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
        /**
         * API key
         */
        private String key;
        /**
         * On permission request
         */
        private ActivityResultLauncher<String[]> requestPermissionLauncher;
        /**
         * Location preference
         */
        private Preference location;
        /**
         * Progress dialog
         */
        private Dialog progressDialog;
        /**
         * GPS loading
         */
        private boolean loading = false;
        /**
         * Broadcast receiver for Gps
         */
        private GpsReceiver gpsReceiver;
        /**
         * Counter for easter egg
         */
        private int easterEggCounter = 0;
        /**
         * Date for easter egg
         */
        private Date easterEggDate = null;

        /**
         * Create Fragment
         *
         * @param savedInstanceState Not used
         * @param rootKey            Root key
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            context = getContext();
            location = findPreference("location");
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            editor = sharedPreferences.edit();
            key = sharedPreferences.getString("key", null);
            requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                if (fineLocationGranted || coarseLocationGranted) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.perm_granted), Snackbar.LENGTH_SHORT).show();
                    getLocation();
                } else {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.perm_denied), Snackbar.LENGTH_SHORT).show();
                }
            });

            onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    switch (key) {
                        case "theme":
                            Functions.setTheme(context);
                            break;
                        case "location_mode":
                            boolean location_mode = sharedPreferences.getBoolean("location_mode", false);
                            if (location_mode) {
                                location.setSummary(sharedPreferences.getString("location", context.getString(R.string.undefined)));
                                location.setVisible(true);
                            } else {
                                location.setVisible(false);
                                if (sharedPreferences.getString("location", null) != null) {
                                    editor.remove("location");
                                    editor.apply();
                                    Map<String, String> arguments = new HashMap<>();
                                    arguments.put("request", "location");
                                    arguments.put("location", "null");
                                    arguments.put("key", sharedPreferences.getString("key", null));
                                    Intent intent = new Intent(context, JsonFromUrl.class);
                                    intent.putExtra("arguments", (Serializable) arguments);
                                    intent.putExtra("url", BuildConfig.WEB_SERVER_URL);
                                    context.startService(intent);
                                }
                            }
                            break;
                    }
                }
            };

            Preference logout = findPreference("logout");
            Preference login = findPreference("login");
            Preference location_mode = findPreference("location_mode");
            Preference authors = findPreference("authors");
            Preference source = findPreference("source");
            Preference version = findPreference("version");

            location.setVisible(sharedPreferences.getBoolean("location_mode", false));
            location.setSummary(sharedPreferences.getString("location", context.getString(R.string.undefined)));
            location.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.set_location));
                    builder.setSingleChoiceItems(new CharSequence[]{getString(R.string.gps), getString(R.string.manual)}, 0, null);
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int choice = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            switch (choice) {
                                case 0:
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        getLocation();
                                    } else {
                                        requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
                                    }
                                    break;
                                case 1:
                                    String[] locations = sharedPreferences.getString("locations", "").split(";");
                                    Spinner spinner = new Spinner(context);
                                    spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, locations));
                                    int padding = (int) getResources().getDimension(R.dimen.dialog_padding);
                                    spinner.setPadding(padding, padding, padding, padding);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setView(spinner);
                                    builder.setTitle(getString(R.string.set_location));
                                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String selectedItem = spinner.getSelectedItem().toString();
                                            editor.putString("location", selectedItem);
                                            editor.apply();
                                            location.setSummary(selectedItem);
                                            Map<String, String> arguments = new HashMap<>();
                                            arguments.put("request", "location");
                                            arguments.put("location", selectedItem);
                                            arguments.put("key", key);
                                            Intent intent = new Intent(context, JsonFromUrl.class);
                                            intent.putExtra("arguments", (Serializable) arguments);
                                            intent.putExtra("url", BuildConfig.WEB_SERVER_URL);
                                            context.startService(intent);
                                        }
                                    });
                                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                    break;
                            }
                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                    return false;
                }
            });

            sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

            login.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    startActivity(new Intent(context, Login.class));
                    getActivity().finishAffinity();
                    return false;
                }
            });

            logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    editor.remove("username");
                    editor.remove("email");
                    editor.remove("key");
                    editor.remove("avatar");
                    editor.remove("ci");
                    editor.remove("location");
                    editor.putBoolean("location_mode", false);
                    editor.remove("locations");
                    editor.apply();

                    Map<String, String> arguments = new HashMap<>();
                    arguments.put("request", "logout");
                    arguments.put("key", key);
                    Intent intent = new Intent(context, JsonFromUrl.class);
                    intent.putExtra("arguments", (Serializable) arguments);
                    intent.putExtra("url", BuildConfig.WEB_SERVER_URL);
                    context.startService(intent);

                    startActivity(new Intent(getContext(), Main.class));
                    getActivity().finishAffinity();
                    return false;
                }
            });

            authors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    Functions.openUrl(context, "https://github.com/CIAO-Integration");
                    return false;
                }
            });

            source.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    Functions.openUrl(context, "https://github.com/CIAO-Integration/CIAO-mobile-appli");
                    return false;
                }
            });

            version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    Date now = Calendar.getInstance().getTime();
                    if (easterEggDate == null || now.getTime() - easterEggDate.getTime() > 5 * 1000) { //5s
                        easterEggDate = Calendar.getInstance().getTime();
                        easterEggCounter = 1;
                    } else if (easterEggCounter == 4) {
                        easterEggDate = null;
                        easterEggCounter = 0;
                        Functions.openUrl(context, "https://github.com/CIAO-Integration");
                    } else {
                        easterEggCounter++;
                    }
                    return false;
                }
            });
            version.setSummary(BuildConfig.VERSION_NAME);

            if (key != null) {
                login.setVisible(false);

            } else {
                logout.setVisible(false);
                location_mode.setVisible(false);
            }
        }

        /**
         * Get location from GPS sensor
         */
        public void getLocation() {
            if (Functions.checkConnection(context)) {
                progressDialog = Functions.makeLoadingDialog(context);
                progressDialog.show();
                gpsReceiver = new GpsReceiver();
                context.registerReceiver(gpsReceiver, new IntentFilter(GPS_TARGET));
                Intent intent = new Intent(context, Gps.class);
                intent.putExtra("target", GPS_TARGET);
                context.startService(intent);
                loading = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (loading) {
                            context.unregisterReceiver(gpsReceiver);
                            progressDialog.cancel();
                            loading = false;
                            Functions.makeDialog(context, getString(R.string.error), getString(R.string.error_retry)).show();
                        }
                    }
                }, 10000);
            } else {
                Functions.makeDialog(context, getString(R.string.error), getString(R.string.error_network)).show();
            }
        }

        /**
         * Broadcast receiver for Gps
         */
        private class GpsReceiver extends BroadcastReceiver {
            /**
             * On receive
             *
             * @param context Context
             * @param intent  Intent
             */
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                loading = false;
                double latitude = intent.getDoubleExtra("latitude", -1);
                double longitude = intent.getDoubleExtra("longitude", -1);
                if (latitude != -1 && longitude != -1) {
                    Geocoder geocoder = new Geocoder(context);
                    try {
                        List<Address> list = geocoder.getFromLocation(latitude, longitude, 1);
                        String admin = list.get(0).getAdminArea() + "," + list.get(0).getCountryName();
                        editor.putString("location", admin);
                        editor.apply();
                        location.setSummary(admin);

                        Map<String, String> arguments = new HashMap<>();
                        arguments.put("request", "location");
                        arguments.put("location", admin);
                        arguments.put("key", key);
                        Intent intent1 = new Intent(context, JsonFromUrl.class);
                        intent1.putExtra("arguments", (Serializable) arguments);
                        intent1.putExtra("url", BuildConfig.WEB_SERVER_URL);
                        context.startService(intent1);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Functions.makeDialog(context, getString(R.string.error), e.toString()).show();
                    }
                    progressDialog.cancel();
                } else {
                    progressDialog.cancel();
                    Functions.makeDialog(context, getString(R.string.error), getString(R.string.error_gps)).show();
                }
            }
        }
    }
}