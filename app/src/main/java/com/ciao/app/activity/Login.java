package com.ciao.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.ciao.app.BuildConfig;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.service.JsonFromUrl;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity showing login/register screen
 */
public class Login extends AppCompatActivity {
    /**
     * Target for broadcast receiver
     */
    private final String TARGET = "Login";
    /**
     * Email pattern
     */
    private final Pattern emailPattern = Pattern.compile("^[A-Za-z0-9.]+@[A-Za-z]+[.][a-z]{2,3}$");
    /**
     * Password pattern
     */
    private final Pattern passwordPattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!*@#$&]).{8,}$");
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * Shared preferences editor
     */
    private SharedPreferences.Editor editor;
    /**
     * Username valid
     */
    private boolean registerUsernameValid = false;
    /**
     * Email valid
     */
    private boolean registerEmailValid = false;
    /**
     * Password valid
     */
    private boolean registerPasswordValid = false;
    /**
     * Confirmation password valid
     */
    private boolean registerConfPasswordValid = false;
    /**
     * Login email
     */
    private EditText loginEmail;
    /**
     * Login password
     */
    private EditText loginPassword;
    /**
     * Register username
     */
    private EditText registerUsername;
    /**
     * Register email
     */
    private EditText registerEmail;
    /**
     * Register password
     */
    private EditText registerPassword;
    /**
     * Register confirmation password
     */
    private EditText registerConfPassword;
    /**
     * Login
     */
    private LinearLayout login;
    /**
     * Register
     */
    private LinearLayout register;

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

        loginEmail = findViewById(R.id.login_login_email);
        loginPassword = findViewById(R.id.login_login_password);
        registerUsername = findViewById(R.id.login_register_username);
        registerEmail = findViewById(R.id.login_register_email);
        registerPassword = findViewById(R.id.login_register_password);
        registerConfPassword = findViewById(R.id.login_register_confirm_password);
        login = findViewById(R.id.login_login);
        register = findViewById(R.id.login_register);

        registerUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    registerUsername.setBackgroundColor(Color.WHITE);
                    registerUsernameValid = true;
                } else {
                    registerUsername.setBackgroundColor(Color.RED);
                    registerUsernameValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        registerEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Matcher matcher = emailPattern.matcher(s.toString());
                if (matcher.matches()) {
                    registerEmail.setBackgroundColor(Color.WHITE);
                    registerEmailValid = true;
                } else {
                    registerEmail.setBackgroundColor(Color.RED);
                    registerEmailValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        registerPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Matcher matcher = passwordPattern.matcher(s.toString());
                if (matcher.matches()) {
                    registerPassword.setBackgroundColor(Color.WHITE);
                    registerPasswordValid = true;
                } else {
                    registerPassword.setBackgroundColor(Color.RED);
                    registerPasswordValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        registerConfPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (registerPassword.getText().toString().equals(s.toString())) {
                    registerConfPassword.setBackgroundColor(Color.WHITE);
                    registerConfPasswordValid = true;
                } else {
                    registerConfPassword.setBackgroundColor(Color.RED);
                    registerConfPasswordValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Try to login
     *
     * @param view View
     */
    public void login(View view) {
        if (Functions.checkConnection(this)) {
            String email = loginEmail.getText().toString();
            String password = loginPassword.getText().toString();
            Log.d("email", email);
            Map<String, String> arguments = new HashMap<>();
            arguments.put("request", "login");
            arguments.put("email", email);
            arguments.put("password", password);
            registerReceiver(new LoginReceiver(), new IntentFilter(TARGET));
            Intent intent = new Intent(this, JsonFromUrl.class);
            intent.putExtra("arguments", (Serializable) arguments);
            intent.putExtra("target", TARGET);
            intent.putExtra("url", BuildConfig.WEB_SERVER_URL);
            startService(intent);
        } else {
            Functions.makeErrorDialog(this, getString(R.string.error_network)).show();
        }
    }

    /**
     * Try to register
     *
     * @param view View
     */
    public void register(View view) {
        if (Functions.checkConnection(this)) {
            if (!registerUsernameValid) {
                Functions.makeErrorDialog(this, getString(R.string.not_valid_username)).show();
            } else if (!registerEmailValid) {
                Functions.makeErrorDialog(this, getString(R.string.not_valid_email)).show();
            } else if (!registerPasswordValid) {
                Functions.makeErrorDialog(this, getString(R.string.not_valid_password)).show();
            } else if (!registerConfPasswordValid) {
                Functions.makeErrorDialog(this, getString(R.string.not_valid_conf_password)).show();
            } else {
                String username = registerUsername.getText().toString();
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String confPassword = registerConfPassword.getText().toString();
                Log.d("username", username);
                Log.d("email", email);
                Map<String, String> arguments = new HashMap<>();
                arguments.put("request", "register");
                arguments.put("username", username);
                arguments.put("email", email);
                arguments.put("password", password);
                arguments.put("conf_password", confPassword);
                registerReceiver(new RegisterReceiver(), new IntentFilter(TARGET));
                Intent intent = new Intent(this, JsonFromUrl.class);
                intent.putExtra("arguments", (Serializable) arguments);
                intent.putExtra("target", TARGET);
                intent.putExtra("url", BuildConfig.WEB_SERVER_URL);
                startService(intent);
            }
        } else {
            Functions.makeErrorDialog(this, getString(R.string.error_network)).show();
        }
    }

    /**
     * Switch to register screen
     *
     * @param view View
     */
    public void no_account(View view) {
        login.setVisibility(View.GONE);
        register.setVisibility(View.VISIBLE);
    }

    /**
     * Switch to login screen
     *
     * @param view View
     */
    public void have_account(View view) {
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
                        Functions.makeErrorDialog(context, getString(R.string.error_message, status, json.getString("message"))).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.makeErrorDialog(context, e.toString()).show();
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
                        Functions.makeErrorDialog(context, getString(R.string.error_message, status, json.getString("message"))).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Functions.makeErrorDialog(context, e.toString()).show();
                }
            }
        }
    }
}
