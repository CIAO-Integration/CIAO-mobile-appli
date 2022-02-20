package com.ciao.app.activity;

import android.app.Activity;
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
import android.view.KeyEvent;
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
import com.ciao.app.Database;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.service.TextFromUrl;

import java.util.HashMap;

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
    private LinearLayout fullscreen;
    /**
     * Content
     */
    private LinearLayout content;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_production);

        videoView = findViewById(R.id.production_video);
        fullscreen = findViewById(R.id.production_fullscreen);
        content = findViewById(R.id.production_content);

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

        progressDialog = Functions.makeLoadingDialog(this);
        progressDialog.show();

        Database database = new Database(this);
        HashMap<String, String> row = database.getRowById(getIntent().getStringExtra("id"));
        database.close();

        type = row.get("type");
        link = row.get("link");
        TextView actionBarTitle = findViewById(R.id.actionbar_title);
        TextView productionTitle = findViewById(R.id.production_title);
        productionTitle.setText(row.get("title"));
        String path = row.get("path");
        if (type.equals("article")) {
            actionBarTitle.setText(getString(R.string.article));
            if (!path.equals("null")) {
                registerReceiver(new ArticleReceiver(), new IntentFilter(TARGET));
                Intent intent1 = new Intent(this, TextFromUrl.class);
                intent1.putExtra("path", BuildConfig.STORAGE_SERVER_URL + path);
                intent1.putExtra("target", TARGET);
                startService(intent1);
            }
        } else if (type.equals("video")) {
            actionBarTitle.setText(getString(R.string.video));
            videoView.setVisibility(View.VISIBLE);
            if (!path.equals("null")) {
                if (!path.startsWith("http")) {
                    path = BuildConfig.STORAGE_SERVER_URL + path;
                }
                videoView.setVideoPath(path);
                videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        progressDialog.cancel();
                        Functions.makeErrorDialog(Production.this, getString(R.string.error_video)).show();
                        return true;
                    }
                });
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        MediaController mediaController = new MediaController(Production.this);
                        videoView.setMediaController(mediaController);
                        mediaController.setAnchorView(videoView);
                        progressDialog.cancel();
                        videoView.start();
                    }
                });
            }
            ArticleBuilder articleBuilder = new ArticleBuilder(Production.this, content, "<p>" + row.get("description") + "</p>");
            articleBuilder.build();
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && type.equals("video")) {
            WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            fullscreen.setVisibility(View.VISIBLE);
            content.removeView(videoView);
            fullscreen.addView(videoView, 0);
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
                    fullscreen.setVisibility(View.VISIBLE);
                    content.removeView(videoView);
                    fullscreen.addView(videoView, 0);
                } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
                    fullscreen.removeView(videoView);
                    content.addView(videoView, content.getChildCount() - 1);
                    fullscreen.setVisibility(View.GONE);
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
     * Broadcast receiver for TextFromUrl
     */
    private class ArticleReceiver extends BroadcastReceiver {
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
                ArticleBuilder articleBuilder = new ArticleBuilder(context, content, text);
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
            button.setForeground(AppCompatResources.getDrawable(context, R.drawable.fullscreen));
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    rotate();
                }
            });
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.END;
            addView(button, layoutParams);
        }

        /**
         * Finish on back pressed
         *
         * @param event Event
         * @return dispatchKeyEvent
         */
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                ((Activity) context).finish();
            }
            return super.dispatchKeyEvent(event);
        }
    }
}
