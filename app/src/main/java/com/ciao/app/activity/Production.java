package com.ciao.app.activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

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
     * Video view
     */
    private VideoView videoView;
    /**
     * Production
     */
    private LinearLayout production;
    /**
     * Video
     */
    private LinearLayout video;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production);

        videoView = findViewById(R.id.video_content);
        production = findViewById(R.id.production);
        video = findViewById(R.id.video);

        String id = getIntent().getStringExtra("id");
        if (id.startsWith("art")) {
            type = "article";
        } else if (id.startsWith("vid")) {
            type = "video";
        }

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

        if (Functions.checkConnection(this)) {
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
            Functions.makeErrorDialog(this, getString(R.string.error_network)).show();
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            video.removeView(videoView);
            production.addView(videoView, 0);
        }
    }

    /**
     * On rotation changed
     *
     * @param newConfig New configuration
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (type.equals("video")) {
            int orientation = newConfig.orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE || orientation == Configuration.ORIENTATION_PORTRAIT) {
                int position = videoView.getCurrentPosition();
                WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
                windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
                    video.removeView(videoView);
                    production.addView(videoView, 0);
                } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
                    production.removeView(videoView);
                    video.addView(videoView, video.getChildCount());
                }
                videoView.seekTo(position);
            }
        }
    }

    /**
     * Rotate screen
     */
    public void rotate() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }, 2500);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }, 2500);
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
                                video.setVisibility(View.VISIBLE);
                                if (!path.startsWith("http")) {
                                    path = BuildConfig.STORAGE_SERVER_URL + path;
                                }
                                videoView.setVideoPath(path);
                                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                    @Override
                                    public boolean onError(MediaPlayer mp, int what, int extra) {
                                        progressDialog.cancel();
                                        Functions.makeErrorDialog(context, getString(R.string.error_video)).show();
                                        return true;
                                    }
                                });
                                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mediaPlayer) {
                                        Production.MediaController mediaController = new Production.MediaController(context);
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
                        Functions.makeErrorDialog(context, getString(R.string.error_message, status, json.getString("message"))).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.cancel();
                    Functions.makeErrorDialog(context, e.toString()).show();
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
                Functions.makeErrorDialog(context, getString(R.string.error_article)).show();
            } else {
                ArticleBuilder articleBuilder = new ArticleBuilder(context, findViewById(R.id.article_content), text);
                articleBuilder.build();
                progressDialog.cancel();
            }
        }
    }

    /**
     * Media controller
     */
    private class MediaController extends android.widget.MediaController {
        /**
         * Context
         */
        private Context context;

        /**
         * Constructor
         *
         * @param context Context
         */
        public MediaController(Context context) {
            super(context);
            this.context = context;
        }

        /**
         * Set anchor view
         *
         * @param view View
         */
        @Override
        public void setAnchorView(View view) {
            super.setAnchorView(view);
            Button button = new Button(context, null, R.attr.borderlessButtonStyle);
            button.setForeground(AppCompatResources.getDrawable(context, android.R.drawable.ic_menu_always_landscape_portrait));
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    rotate();
                }
            });
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.END | Gravity.TOP;
            addView(button, layoutParams);
        }
    }
}
