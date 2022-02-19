package com.ciao.app.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Get location from GPS sensor
 */
public class Gps extends Service {
    /**
     * Target for broadcast receiver
     */
    private String target;
    /**
     * Location manager
     */
    private LocationManager locationManager;

    /**
     * On bind
     *
     * @param intent Intent
     * @return IBinder
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * On start command
     *
     * @param intent  Intent
     * @param flags   Flags
     * @param startId ID
     * @return Success
     */
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        target = intent.getStringExtra("target");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, new MyLocationListener());
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * LocationListener
     */
    private class MyLocationListener implements LocationListener {
        /**
         * On location changed
         *
         * @param location Location
         */
        @Override
        public void onLocationChanged(@NonNull Location location) {
            locationManager.removeUpdates(this);
            Intent intent = new Intent(target);
            intent.putExtra("latitude", location.getLatitude());
            intent.putExtra("longitude", location.getLongitude());
            sendBroadcast(intent);
        }
    }
}