package com.ciao.app;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.databinding.FragmentBinding;

import java.util.ArrayList;

public class Functions {
    public static void initFragment(Context context, FragmentBinding binding, int which) {
        RecyclerView recyclerView = binding.mainList;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new RecyclerViewAdapter(context, getData(which)));
        SwipeRefreshLayout swipeRefreshLayout = binding.mainRefresh;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                RecyclerView recyclerView = binding.mainList;
                recyclerView.setAdapter(new RecyclerViewAdapter(context, getData(which)));
            }
        });
    }

    public static ArrayList<String[]> getData(int which) {
        ArrayList<String[]> data = new ArrayList<>();
        switch (which) {
            case R.string.browse:
                data.add(new String[]{"000", null, "Hello", "browse"});
                break;
            case R.string.around:
                data.add(new String[]{"000", null, "Hello", "around"});
                break;
            case R.string.news:
                data.add(new String[]{"001", null, "Hello", "news"});
                break;
            case R.string.popularization:
                data.add(new String[]{"002", null, "Hello", "popularization"});
                break;
            case R.string.digital:
                data.add(new String[]{"003", null, "Hello", "digital"});
                break;
            case R.string.science:
                data.add(new String[]{"004", null, "Hello", "science"});
                break;
            case R.string.culture:
                data.add(new String[]{"005", null, "Hello", "culture"});
                break;
            case R.string.history:
                data.add(new String[]{"006", null, "Hello", "history"});
                break;
            case R.string.geography:
                data.add(new String[]{"007", null, "Hello", "geography"});
                break;
            case R.string.politics:
                data.add(new String[]{"008", null, "Hello", "politics"});
                break;
            case R.string.sport:
                data.add(new String[]{"009", null, "Hello", "sport"});
                break;
        }
        data.add(new String[]{"010", null, "Hello", "World"});
        data.add(new String[]{"011", null, "Hello", "World"});
        data.add(new String[]{"012", null, "Hello", "World"});
        data.add(new String[]{"013", null, "Hello", "World"});
        data.add(new String[]{"014", null, "Hello", "World"});
        return data;
    }
}
