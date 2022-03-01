package com.ciao.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.ciao.app.Functions;
import com.ciao.app.R;

/**
 * Broadcast receiver for initiating notification system on boot
 */
public class NotificationRestorer extends BroadcastReceiver {
    /**
     * On receive
     *
     * @param context Context
     * @param intent  Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getString("key", null) != null) {
            Functions.initNotifications(context, true);
            Toast.makeText(context, context.getString(R.string.notifications_restored), Toast.LENGTH_SHORT).show();
        }
    }
}
