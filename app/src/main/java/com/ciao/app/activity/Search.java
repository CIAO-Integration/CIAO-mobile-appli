package com.ciao.app.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.Database;
import com.ciao.app.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity showing results of search
 */
public class Search extends AppCompatActivity {
    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.main_refresh);
        swipeRefreshLayout.setEnabled(false);

        Intent intent = getIntent();
        String search = intent.getStringExtra("search");

        TextView textView = findViewById(R.id.actionbar_title);
        textView.setText(getString(R.string.search_results, search));

        findViewById(R.id.actionbar_avatar).setVisibility(View.GONE);
        ImageView back = findViewById(R.id.actionbar_logo);
        back.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.back));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                back.setColorFilter(Color.LTGRAY);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                back.setColorFilter(Color.WHITE);
                break;
        }

        Database database = new Database(this);
        ArrayList<HashMap<String, String>> data = database.getRowsBySearch(search);
        database.close();

        RecyclerView recyclerView = findViewById(R.id.main_list);
        Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(Search.this, data);
        recyclerView.setAdapter(recyclerViewAdapter);
    }
}
