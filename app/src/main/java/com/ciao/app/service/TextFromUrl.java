package com.ciao.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

/**
 * Get text from URL
 */
public class TextFromUrl extends Service implements Runnable {
    /**
     * Path of text file
     */
    private String path;
    /**
     * Target for Broadcast receiver
     */
    private String target;

    /**
     * On bind
     *
     * @param intent Intent
     * @return null
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
     * @param startId Start ID
     * @return On start command
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        path = intent.getStringExtra("path");
        target = intent.getStringExtra("target");
        Thread thread = new Thread(this);
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Run
     */
    @Override
    public void run() {
        Intent intent = new Intent(target);
        try {
            URL url = new URL(path);
            Scanner scanner = new Scanner(url.openStream());
            String text = "";
            while (scanner.hasNextLine()) {
                text += scanner.nextLine();
            }
            scanner.close();
            intent.putExtra("text", text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendBroadcast(intent);
    }
}
