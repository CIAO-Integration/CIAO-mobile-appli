package com.ciao.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.service.JsonFromUrl;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity showing login/register screen
 */
public class Login extends AppCompatActivity {
    /**
     * Target for broadcast receiver
     */
    private final String TARGET = "Login";
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * Shared preferences editor
     */
    private SharedPreferences.Editor editor;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
    }

    /**
     * Try to login
     *
     * @param view View
     */
    public void login(View view) {
        Boolean connected = Functions.checkConnection(this);
        if (connected) {
            EditText email = findViewById(R.id.login_login_email);
            EditText password = findViewById(R.id.login_login_password);
            Log.d("email", email.getText().toString());
            Map<String, String> arguments = new HashMap<>();
            arguments.put("request", "login");
            arguments.put("email", email.getText().toString());
            arguments.put("password", password.getText().toString());
            registerReceiver(new LoginReceiver(), new IntentFilter(TARGET));
            Intent intent = new Intent(this, JsonFromUrl.class);
            intent.putExtra("arguments", (Serializable) arguments);
            intent.putExtra("target", TARGET);
            startService(intent);
        } else {
            Functions.showErrorDialog(this, getString(R.string.error_network));
        }
    }

    /**
     * Try to register
     *
     * @param view View
     */
    public void register(View view) {
        Boolean connected = Functions.checkConnection(this);
        if (connected) {
            EditText username = findViewById(R.id.login_register_username);
            EditText email = findViewById(R.id.login_register_email);
            EditText password = findViewById(R.id.login_register_password);
            EditText confirmPassword = findViewById(R.id.login_register_confirm_password);
            Log.d("username", username.getText().toString());
            Log.d("email", email.getText().toString());
            Map<String, String> arguments = new HashMap<>();
            arguments.put("request", "register");
            arguments.put("username", username.getText().toString());
            arguments.put("email", email.getText().toString());
            arguments.put("password", password.getText().toString());
            arguments.put("conf_password", confirmPassword.getText().toString());
            registerReceiver(new RegisterReceiver(), new IntentFilter(TARGET));
            Intent intent = new Intent(this, JsonFromUrl.class);
            intent.putExtra("arguments", (Serializable) arguments);
            intent.putExtra("target", TARGET);
            startService(intent);
        } else {
            Functions.showErrorDialog(this, getString(R.string.error_network));
        }
    }

    /**
     * Switch to register screen
     *
     * @param view View
     */
    public void no_account(View view) {
        LinearLayout login = findViewById(R.id.login_login);
        LinearLayout register = findViewById(R.id.login_register);
        login.setVisibility(View.GONE);
        register.setVisibility(View.VISIBLE);
    }

    /**
     * Switch to login screen
     *
     * @param view View
     */
    public void have_account(View view) {
        LinearLayout login = findViewById(R.id.login_login);
        LinearLayout register = findViewById(R.id.login_register);
        login.setVisibility(View.VISIBLE);
        register.setVisibility(View.GONE);
    }

    /**
     * Continue as Guest
     *
     * @param view View
     */
    public void guest(View view) {
        next();
    }

    /**
     * Continue to next screen (Main screen)
     */
    public void next() {
        editor.putBoolean("firstTime", false);
        editor.apply();
        startActivity(new Intent(this, Loading.class));
        finish();
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    private class LoginReceiver extends BroadcastReceiver {
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
                    String status = json.getString("status");
                    if (status.equals("200")) {
                        String key = json.getString("key");
                        editor.putString("key", key);
                        editor.apply();
                        next();
                    } else if (status.equals("500")) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.server_error), Snackbar.LENGTH_SHORT).show();
                    } else if (status.equals("406")) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.wrong_password), Snackbar.LENGTH_SHORT).show();
                    } else if (status.equals("404")) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.user_not_found), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Functions.showErrorDialog(context, getString(R.string.error_message, status, json.getString("message")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.showErrorDialog(context, e.toString());
                }
            }
        }
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    private class RegisterReceiver extends BroadcastReceiver {
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
                    String status = json.getString("status");
                    if (status.equals("200")) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.check_email), Snackbar.LENGTH_SHORT).show();
                    } else if (status.equals("500")) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.server_error), Snackbar.LENGTH_SHORT).show();
                    } else if (status.equals("406")) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.invalid_username_email), Snackbar.LENGTH_SHORT).show();
                    } else if (status.equals("401")) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.user_exists), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Functions.showErrorDialog(context, getString(R.string.error_message, status, json.getString("message")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.showErrorDialog(context, e.toString());
                }
            }
        }
    }
}
