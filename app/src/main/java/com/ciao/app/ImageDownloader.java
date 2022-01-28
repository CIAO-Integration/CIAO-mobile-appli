package com.ciao.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class ImageDownloader extends Service implements Runnable {
    private ArrayList<String> sources;
    private String target;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sources = intent.getStringArrayListExtra("sources");
        target = intent.getStringExtra("target");
        Thread thread = new Thread(this);
        thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void run() {
        ArrayList<String> fileNames = new ArrayList<>();
        for (int i = 0; i < sources.size(); i++) {
            if (sources.get(i) != null) {
                String source = sources.get(i);
                String fileName = source.split("/")[source.split("/").length - 1];
                fileNames.add(fileName);
                try {
                    if (!new File(getCacheDir(), fileName).exists()) {
                        URL url = new URL(source);
                        InputStream inputStream = new BufferedInputStream(url.openStream());
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, read);
                        }
                        byteArrayOutputStream.close();
                        inputStream.close();
                        byte[] data = byteArrayOutputStream.toByteArray();
                        FileOutputStream fileOutputStream = new FileOutputStream(new File(getCacheDir(), fileName));
                        fileOutputStream.write(data);
                        fileOutputStream.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                fileNames.add(null);
            }
        }
        Intent intent = new Intent(target);
        intent.putExtra("fileNames", fileNames);
        sendBroadcast(intent);
    }
}
