package com.ciao.app.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.ciao.app.BuildConfig;
import com.ciao.app.Functions;
import com.ciao.app.R;

/**
 * Activity showing settings
 */
public class Settings extends AppCompatActivity {
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

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                switch (key) {
                    case "theme":
                        String theme = sharedPreferences.getString(key, "light");
                        Functions.setTheme(theme);
                        break;
                }
            }
        });
    }

    /**
     * Actual settings
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            Preference authors = findPreference("authors");
            Preference source = findPreference("source");
            Preference version = findPreference("version");

            authors.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    Functions.openUrl(getContext(), "https://github.com/CIAO-Integration");
                    return false;
                }
            });

            source.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    Functions.openUrl(getContext(), "https://github.com/CIAO-Integration/CIAO-mobile-appli");
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
        }
    }
}