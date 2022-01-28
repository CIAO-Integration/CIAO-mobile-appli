package com.ciao.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.ciao.app.ArticleBuilder;
import com.ciao.app.R;

import java.util.ArrayList;

public class Article extends AppCompatActivity {
    public static final String BROADCAST_TARGET = "Article";
    private ArticleBuilder articleBuilder;
    private ImageDownloaderReceiver imageDownloaderReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        imageDownloaderReceiver = new ImageDownloaderReceiver();

        findViewById(R.id.actionbar_avatar).setVisibility(View.GONE);
        findViewById(R.id.actionbar_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageView back = findViewById(R.id.actionbar_logo);
        back.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.back));
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                back.setColorFilter(Color.LTGRAY);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                back.setColorFilter(Color.WHITE);
                break;
        }

        TextView title = findViewById(R.id.actionbar_title);
        title.setText(getString(R.string.article));
        //title.setText(getString(R.string.video));
        String text = "<h1>h1</h1>" +
                "<h2>h2</h2>" +
                "<p>p</p>" +
                "<h3>h3</h3>" +
                "<p>p</p>" +
                "<h4>h4</h4>" +
                "<p>p</p>" +
                "<h5>h5</h5>" +
                "<p>p</p>" +
                "<h6>h6</h6>" +
                "<p>p</p>" +
                "<img src=\"https://interactive-examples.mdn.mozilla.net/media/cc0-images/elephant-660-480.jpg\"/>" +
                "<figcaption>figcaption</figcaption>" +
                "<img src=\"https://upload.wikimedia.org/wikipedia/commons/thumb/b/b6/Image_created_with_a_mobile_phone.png/640px-Image_created_with_a_mobile_phone.png\"/>" +
                "<img src=\"https://interactive-examples.mdn.mozilla.net/media/cc0-images/grapefruit-slice-332-332.jpg\"/>" +
                "<img src=\"https://cdn.futura-sciences.com/buildsv6/images/mediumoriginal/6/5/2/652a7adb1b_98148_01-intro-773.jpg\"/>" +
                "<img src=\"https://www.akamai.com/content/dam/site/im-demo/perceptual-standard.jpg\"/>";
        articleBuilder = new ArticleBuilder(this, findViewById(R.id.article_content), text);
        articleBuilder.build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(imageDownloaderReceiver, new IntentFilter(BROADCAST_TARGET));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(imageDownloaderReceiver);
    }

    private class ImageDownloaderReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> fileNames = intent.getStringArrayListExtra("fileNames");
            articleBuilder.setImgs(fileNames);
        }
    }
}
