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
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        key = sharedPreferences.getString("key", null);

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
        private ActivityResultLauncher<String> requestPermissionLauncher;
        /**
         * Location preference
         */
        private Preference location;
        /**
         * Progress dialog
         */
        private Dialog progressDialog;

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
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            editor = sharedPreferences.edit();
            key = sharedPreferences.getString("key", null);

            Preference logout = findPreference("logout");
            Preference login = findPreference("login");
            Preference location_mode = findPreference("location_mode");
            location = findPreference("location");
            Preference authors = findPreference("authors");
            Preference source = findPreference("source");
            Preference version = findPreference("version");

            requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.perm_granted), Snackbar.LENGTH_SHORT).show();
                    getLocation();
                } else {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), getString(R.string.perm_denied), Snackbar.LENGTH_SHORT).show();
                }
            });

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
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        getLocation();
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                                    }
                                    break;
                                case 1:
                                    String[] locations = sharedPreferences.getString("locations", "").split(";");
                                    Spinner spinner = new Spinner(context);
                                    spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, locations));
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

            onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    switch (key) {
                        case "theme":
                            String theme = sharedPreferences.getString(key, "light");
                            Functions.setTheme(theme);
                            break;
                        case "location_mode":
                            Boolean location_mode = sharedPreferences.getBoolean("location_mode", false);
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
                                    context.startService(intent);
                                }
                            }
                            break;
                    }
                }
            };
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
                    context.startService(intent);

                    startActivity(new Intent(getContext(), Loading.class));
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
                    //easter egg
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
            Boolean connected = Functions.checkConnection(context);
            if (connected) {
                progressDialog = Functions.makeLoadingDialog(context);
                progressDialog.show();
                context.registerReceiver(new GpsReceiver(), new IntentFilter(GPS_TARGET));
                Intent intent = new Intent(context, Gps.class);
                intent.putExtra("target", GPS_TARGET);
                context.startService(intent);
            } else {
                Functions.showErrorDialog(context, getString(R.string.error_network));
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
                Double latitude = intent.getDoubleExtra("latitude", -1);
                Double longitude = intent.getDoubleExtra("longitude", -1);
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
                    context.startService(intent1);
                } catch (IOException e) {
                    e.printStackTrace();
                    Functions.showErrorDialog(context, e.toString());
                }
                progressDialog.cancel();
            }
        }
    }
}