package com.ciao.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Upload an image
 */
public class ImageUploader extends Service implements Runnable {
    /**
     * File path
     */
    private String path;
    /**
     * URL
     */
    private String url;
    /**
     * POST arguments
     */
    private Map<String, String> arguments;
    /**
     * Target for broadcast receiver
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
        if (intent != null) {
            path = intent.getStringExtra("path");
            url = intent.getStringExtra("url");
            arguments = (Map<String, String>) intent.getSerializableExtra("arguments");
            target = intent.getStringExtra("target");
            Thread thread = new Thread(this);
            thread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Run
     */
    @Override
    public void run() {
        String twoHyphens = "--";
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        String lineEnd = "\r\n";

        HttpURLConnection httpURLConnection;
        DataOutputStream outputStream;
        InputStream inputStream;

        int bytesRead;
        int bytesAvailable;
        int bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        String[] pathSplit = path.split("/");
        String filename = pathSplit[pathSplit.length - 1];
        String type = null;
        if (filename.endsWith(".png")) {
            type = "image/png";
        } else if (filename.endsWith(".jpg")) {
            type = "image/jpeg";
        } else if (filename.endsWith((".gif"))) {
            type = "image/gif";
        }

        Intent intent = new Intent(target);
        try {
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL(this.url);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            outputStream = new DataOutputStream(httpURLConnection.getOutputStream());

            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "avatar" + "\"; filename=\"" + filename + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + type + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            for (String key : arguments.keySet()) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(arguments.get(key));
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String result = stringBuilder.toString();

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            Log.d("ImageUploader", result);
            intent.putExtra("json", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendBroadcast(intent);
    }
}
