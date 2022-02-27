package com.ciao.app.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import com.ciao.app.BuildConfig;
import com.ciao.app.R;
import com.ciao.app.service.JsonFromUrl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tag
 */
public class Tag extends CardView {
    /**
     * Shared preferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * Shared preferences editor
     */
    private SharedPreferences.Editor editor;
    /**
     * Status
     */
    private Boolean status;
    /**
     * Status text
     */
    private TextView statusTextView;
    /**
     * API key
     */
    private String key;
    /**
     * CI
     */
    private ArrayList<String> ci;
    /**
     * Tag text
     */
    private String text;
    /**
     * Context
     */
    private Context context;

    /**
     * Constructor
     *
     * @param context Context
     * @param text    Text
     */
    public Tag(Context context, String text) {
        super(context);
        this.context = context;
        this.text = text;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView textView = new TextView(context);
        textView.setPadding(8, 4, 8, 4);
        textView.setAllCaps(true);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setText(text);
        linearLayout.addView(textView);
        addView(linearLayout);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        key = sharedPreferences.getString("key", null);
        if (key != null) {
            editor = sharedPreferences.edit();

            setClickable(true);
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            setForeground(AppCompatResources.getDrawable(context, outValue.resourceId));

            statusTextView = new TextView(context);
            statusTextView.setPadding(0, 4, 8, 4);
            statusTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            statusTextView.setText(context.getString(R.string.tag_checked));

            ci = new ArrayList<>(Arrays.asList(sharedPreferences.getString("ci", "").split(",")));
            if (ci.contains(text)) {
                status = true;
            } else {
                status = false;
                statusTextView.setVisibility(GONE);
            }
            linearLayout.addView(statusTextView);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleStatus();
                }
            });
        }
    }

    /**
     * Toggle status of tag
     */
    private void toggleStatus() {
        if (status != null && statusTextView != null) {
            ci = new ArrayList<>(Arrays.asList(sharedPreferences.getString("ci", "").split(",")));
            if (status) {
                status = false;
                statusTextView.setVisibility(GONE);
                ci.remove(text);
            } else {
                status = true;
                statusTextView.setVisibility(VISIBLE);
                ci.add(text);
            }
        }
        String ciString = TextUtils.join(",", ci);
        editor.putString("ci", ciString);
        editor.apply();
        Map<String, String> arguments = new HashMap<>();
        arguments.put("request", "ci");
        arguments.put("ci", ciString);
        arguments.put("key", key);
        Intent intent = new Intent(context, JsonFromUrl.class);
        intent.putExtra("arguments", (Serializable) arguments);
        intent.putExtra("url", BuildConfig.WEB_SERVER_URL);
        context.startService(intent);
    }
}