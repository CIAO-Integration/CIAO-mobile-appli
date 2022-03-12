package com.ciao.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.ciao.app.Database;
import com.ciao.app.R;
import com.ciao.app.activity.Production;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Widget
 */
public class Widget extends AppWidgetProvider {
    /**
     * On update
     *
     * @param context          Context
     * @param appWidgetManager App widget manager
     * @param appWidgetIds     App widget ids
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Database database = new Database(context);
        ArrayList<HashMap<String, String>> rows = database.getRowsByFilter(null, null);
        database.close();

        int[] items = new int[]{R.id.widget_item0, R.id.widget_item1, R.id.widget_item2};
        int[] titles = new int[]{R.id.widget_title0, R.id.widget_title1, R.id.widget_title2};
        int[] images = new int[]{R.id.widget_image0, R.id.widget_image1, R.id.widget_image2};

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

            for (int i = 0; i < items.length; i++) {
                if (i < rows.size()) {
                    HashMap<String, String> row = rows.get(i);
                    String url = row.get("thumbnail");
                    if (!url.startsWith("http")) {
                        url = context.getString(R.string.STORAGE_SERVER_URL) + url;
                    }
                    Intent intent = new Intent(context, Production.class);
                    intent.putExtra("productionId", row.get("id"));
                    intent.putExtra("external", true);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) (Math.random() * 1000), intent, PendingIntent.FLAG_IMMUTABLE);
                    views.setOnClickPendingIntent(items[i], pendingIntent);
                    views.setTextViewText(titles[i], row.get("title"));
                    Glide.with(context).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).load(url).into(new AppWidgetTarget(context, images[i], views, appWidgetId));
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
