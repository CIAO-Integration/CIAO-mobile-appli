package com.ciao.app.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.ciao.app.ArticleBuilder;
import com.ciao.app.BuildConfig;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.service.JsonFromUrl;
import com.ciao.app.service.TextFromUrl;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity showing an article
 */
public class Production extends AppCompatActivity {
    /**
     * Target for broadcast receiver
     */
    public static final String TARGET = "Production";
    /**
     * Type of production
     */
    private String type;
    /**
     * Link to the web version
     */
    private String link;
    /**
     * Progress dialog
     */
    private Dialog progressDialog;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production);

        ImageView back = findViewById(R.id.actionbar_logo);
        back.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.back));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageView share = findViewById(R.id.actionbar_avatar);
        share.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.share));
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, link);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, null));
            }
        });
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                back.setColorFilter(Color.LTGRAY);
                share.setColorFilter(Color.LTGRAY);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                back.setColorFilter(Color.WHITE);
                share.setColorFilter(Color.WHITE);
                break;
        }
        CardView cardView = findViewById(R.id.actionbar_cardview);
        cardView.setElevation(0);


        String id = getIntent().getStringExtra("id");
        if (id.startsWith("art")) {
            type = "article";
        } else if (id.startsWith("vid")) {
            type = "video";
        }

        Boolean connected = Functions.checkConnection(this);
        if (connected) {
            registerReceiver(new ProductionReceiver(), new IntentFilter(TARGET));
            Map<String, String> arguments = new HashMap<>();
            arguments.put("request", "production");
            arguments.put("id", id);
            Intent intent = new Intent(this, JsonFromUrl.class);
            intent.putExtra("arguments", (Serializable) arguments);
            intent.putExtra("target", TARGET);
            startService(intent);
            progressDialog = Functions.makeLoadingDialog(this);
            progressDialog.show();
        } else {
            Functions.showErrorDialog(this, getString(R.string.error_network));
        }
    }

    /**
     * Broadcast receiver for JsonFromUrl
     */
    private class ProductionReceiver extends BroadcastReceiver {
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
                        String path = json.getString("path");
                        String title = json.getString("title");
                        link = json.getString("link");

                        TextView actionBarTitle = findViewById(R.id.actionbar_title);
                        if (type.equals("article")) {
                            actionBarTitle.setText(getString(R.string.article));
                            if (!path.equals("null")) {
                                registerReceiver(new TextReceiver(), new IntentFilter(TARGET));
                                Intent intent1 = new Intent(context, TextFromUrl.class);
                                intent1.putExtra("path", BuildConfig.STORAGE_SERVER_URL + path);
                                intent1.putExtra("target", TARGET);
                                startService(intent1);
                            }
                        } else if (type.equals("video")) {
                            actionBarTitle.setText(getString(R.string.video));
                            TextView videoTitle = findViewById(R.id.video_title);
                            videoTitle.setText(title);
                            if (!path.equals("null")) {
                                findViewById(R.id.article).setVisibility(View.GONE);
                                findViewById(R.id.video).setVisibility(View.VISIBLE);
                                VideoView videoView = findViewById(R.id.video_content);
                                if (!path.startsWith("http")) {
                                    path = BuildConfig.STORAGE_SERVER_URL + path;
                                }
                                videoView.setVideoPath(path);
                                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                    @Override
                                    public boolean onError(MediaPlayer mp, int what, int extra) {
                                        progressDialog.cancel();
                                        Functions.showErrorDialog(context, getString(R.string.error_video));
                                        return true;
                                    }
                                });
                                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mediaPlayer) {
                                        MediaController mediaController = new MediaController(context);
                                        videoView.setMediaController(mediaController);
                                        mediaController.setAnchorView(videoView);
                                        progressDialog.cancel();
                                        videoView.start();
                                    }
                                });
                            }
                        }
                    } else {
                        progressDialog.cancel();
                        Functions.showErrorDialog(context, getString(R.string.error_message, status, json.getString("message")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.cancel();
                    Functions.showErrorDialog(context, e.toString());
                }
            }
        }
    }

    /**
     * Broadcast receiver for TextFromUrl
     */
    private class TextReceiver extends BroadcastReceiver {
        /**
         * On receive
         *
         * @param context Context
         * @param intent  Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(this);
            String text = intent.getStringExtra("text");
            if (text == null) {
                progressDialog.cancel();
                Functions.showErrorDialog(context, getString(R.string.error_article));
            } else {
                ArticleBuilder articleBuilder = new ArticleBuilder(context, findViewById(R.id.article_content), text);
                articleBuilder.build();
                progressDialog.cancel();
            }
        }
    }
}
