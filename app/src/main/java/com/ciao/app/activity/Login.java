package com.ciao.app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.ciao.app.R;
import com.google.android.material.snackbar.Snackbar;

/**
 * Activity showing login/register screen
 */
public class Login extends AppCompatActivity {
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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        setContentView(R.layout.activity_login);
    }

    /**
     * Try to connect
     *
     * @param view View
     */
    public void connect(View view) {
        EditText email = findViewById(R.id.login_login_email);
        EditText password = findViewById(R.id.login_login_password);
        Log.d("email", email.getText().toString());
        Log.d("password", password.getText().toString());
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.wrong_credentials), Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Try to register
     *
     * @param view View
     */
    public void register(View view) {
        EditText username = findViewById(R.id.login_register_username);
        EditText email = findViewById(R.id.login_register_email);
        EditText password = findViewById(R.id.login_register_password);
        EditText confirmPassword = findViewById(R.id.login_register_confirm_password);
        Log.d("username", username.getText().toString());
        Log.d("email", email.getText().toString());
        Log.d("password", password.getText().toString());
        Log.d("confirmPassword", confirmPassword.getText().toString());
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.wrong_credentials), Snackbar.LENGTH_SHORT).show();
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
        finish();
        startActivity(new Intent(this, Loading.class));
    }
}
