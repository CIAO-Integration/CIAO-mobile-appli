package com.ciao.app.activity;

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

/**
 * Activity showing an article
 */
public class Article extends AppCompatActivity {
    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

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
                "<h2>Image</h2>" +
                "<p>Here is an image loaded from an <b><i><u>URL</u></i><b/></p>" +
                "<img src=\"https://interactive-examples.mdn.mozilla.net/media/cc0-images/elephant-660-480.jpg\"/>" +
                "<figcaption>Image</figcaption>";
        ArticleBuilder articleBuilder = new ArticleBuilder(this, findViewById(R.id.article_content), text);
        articleBuilder.build();
    }
}
