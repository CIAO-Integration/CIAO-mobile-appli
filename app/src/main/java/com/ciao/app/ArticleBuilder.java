package com.ciao.app;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * Build an Article from a text
 */
public class ArticleBuilder {
    /**
     * Size of h1
     */
    private final int H1_SIZE = 26;
    /**
     * Padding of h1
     */
    private final int H1_PADDING = 48;
    /**
     * Size of h2
     */
    private final int H2_SIZE = 24;
    /**
     * Padding of h2
     */
    private final int H2_PADDING = 40;
    /**
     * Size of h3
     */
    private final int H3_SIZE = 22;
    /**
     * Padding of h3
     */
    private final int H3_PADDING = 32;
    /**
     * Size of h4
     */
    private final int H4_SIZE = 20;
    /**
     * Padding of h4
     */
    private final int H4_PADDING = 24;
    /**
     * Size of h5
     */
    private final int H5_SIZE = 18;
    /**
     * Padding of h5
     */
    private final int H5_PADDING = 16;
    /**
     * Size of h6
     */
    private final int H6_SIZE = 16;
    /**
     * Padding of h6
     */
    private final int H6_PADDING = 8;
    /**
     * Size of p
     */
    private final int P_SIZE = 16;
    /**
     * Size of figcaption
     */
    private final int FIGCAPTION_SIZE = 12;
    /**
     * Context
     */
    private Context context;
    /**
     * Article
     */
    private LinearLayout article;
    /**
     * Text
     */
    private String text;

    /**
     * Constructor
     *
     * @param context Context
     * @param article Article
     * @param text    Text
     */
    public ArticleBuilder(Context context, LinearLayout article, String text) {
        this.context = context;
        this.article = article;
        this.text = text;
    }

    /**
     * Parse and build the article
     */
    public void build() {
        int i = 0;
        while (i < text.length()) {
            if (String.valueOf(text.charAt(i)).equals("<")) {
                String tagBegin = text.substring(i, text.indexOf(">", i) + 1);
                Log.d("tag", tagBegin);
                if (tagBegin.contains("img") && tagBegin.contains("src")) {
                    char quote;
                    if (tagBegin.contains("'")) {
                        quote = '\'';
                    } else {
                        quote = '\"';
                    }
                    int startIndex = tagBegin.indexOf("src=" + quote) + 5;
                    int endIndex = tagBegin.indexOf(quote, startIndex);
                    String src = tagBegin.substring(startIndex, endIndex);
                    Log.d("src", src);
                    addImg(src);
                    i += tagBegin.length();
                    continue;
                }
                String tagEnd = tagBegin.charAt(0) + "/" + tagBegin.substring(1);
                int endIndex = text.indexOf(tagEnd, i) + tagEnd.length();
                String content = text.substring(i, endIndex).replace(tagBegin, "").replace(tagEnd, "");
                Log.d("content", content);
                switch (tagBegin) {
                    case "<h1>":
                        addH(1, content);
                        break;
                    case "<h2>":
                        addH(2, content);
                        break;
                    case "<h3>":
                        addH(3, content);
                        break;
                    case "<h4>":
                        addH(4, content);
                        break;
                    case "<h5>":
                        addH(5, content);
                        break;
                    case "<h6>":
                        addH(6, content);
                        break;
                    case "<p>":
                        addP(content);
                        break;
                    case "<figcaption>":
                        addFigcaption(content);
                }
                i = endIndex;
            } else {
                i++;
            }
        }
    }

    /**
     * Add a h tag to the article
     *
     * @param type Type of h tag
     * @param text Text
     */
    public void addH(int type, String text) {
        int size = 0;
        int padding = 0;
        int gravity = Gravity.NO_GRAVITY;
        switch (type) {
            case 1:
                size = H1_SIZE;
                padding = H1_PADDING;
                gravity = Gravity.CENTER;
                break;
            case 2:
                size = H2_SIZE;
                padding = H2_PADDING;
                break;
            case 3:
                size = H3_SIZE;
                padding = H3_PADDING;
                break;
            case 4:
                size = H4_SIZE;
                padding = H4_PADDING;
                break;
            case 5:
                size = H5_SIZE;
                padding = H5_PADDING;
                break;
            case 6:
                size = H6_SIZE;
                padding = H6_PADDING;
                break;
        }
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setGravity(gravity);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setPadding(textView.getPaddingLeft(), padding, textView.getPaddingRight(), textView.getPaddingBottom());
        article.addView(textView);
    }

    /**
     * Add a p tag to the article
     *
     * @param text Text
     */
    public void addP(String text) {
        TextView textView = new TextView(context);
        textView.setText(HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, P_SIZE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        article.addView(textView);
    }

    /**
     * Add a img tag to the article
     *
     * @param src Source of image
     */
    public void addImg(String src) {
        ImageView imageView = new ImageView(context);
        imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.no_image));
        imageView.setAdjustViewBounds(true);
        article.addView(imageView);
        Drawable placeholder = AppCompatResources.getDrawable(context, R.drawable.no_image);
        Glide.with(context).load(src).placeholder(placeholder).error(placeholder).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
    }

    /**
     * Add a figcaption to the article
     *
     * @param text Text
     */
    public void addFigcaption(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, FIGCAPTION_SIZE);
        textView.setTypeface(textView.getTypeface(), Typeface.ITALIC);
        article.addView(textView);
    }
}
