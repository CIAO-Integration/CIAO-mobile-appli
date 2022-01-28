package com.ciao.app;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ciao.app.activity.Main;
import com.ciao.app.databinding.FragmentMainBinding;

import java.util.ArrayList;

/**
 * Functions
 */
public class Functions {
    /**
     * Init a Fragment of Main Activity
     *
     * @param context Context
     * @param binding Binding to get Views
     * @param which   Which Fragment
     */
    public static void initFragment(Context context, FragmentMainBinding binding, int which) {
        ArrayList<String[]> data = getData(which);
        RecyclerView recyclerView = binding.mainList;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        Main.RecyclerViewAdapter recyclerViewAdapter = new Main.RecyclerViewAdapter(context, data);
        recyclerView.setAdapter(recyclerViewAdapter);
        SwipeRefreshLayout swipeRefreshLayout = binding.mainRefresh;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                RecyclerView recyclerView = binding.mainList;
                recyclerView.setAdapter(new Main.RecyclerViewAdapter(context, data));
            }
        });
    }

    /**
     * Get data to populate Main Activity
     *
     * @param which Which Fragment
     * @return Data
     */
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
        data.add(new String[]{"010", "https://www.akamai.com/content/dam/site/im-demo/perceptual-standard.jpg?imbypass=true", "Hello", "World"});
        data.add(new String[]{"011", "https://images.lanouvellerepublique.fr/image/upload/618f18685870969b0a8b45ab.jpg", "Hello", "World"});
        data.add(new String[]{"012", "https://www.largus.fr/images/images/xv-illu-avg.jpg", "Hello", "World"});
        data.add(new String[]{"013", "https://media.sudouest.fr/8040046/1000x500/12630a98-5c97-4c19-ad90-c207ac7f4808.jpg?v=1643222191", "Hello", "World"});
        data.add(new String[]{"014", "https://www.subaru-global.com/ebrochure/XV/2021my/HAFR/exterior/assets/FHI/MY21/XV/HAFR/Exterior/mp4-svg-360/exterior_360/d_exterior_360_01_01.jpg?40589e3e6f3ee2fc4f55e2bacd02532f", "Hello", "World"});
        return data;
    }

    /**
     * Set app theme
     *
     * @param theme Theme
     */
    public static void setTheme(String theme) {
        switch (theme) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }
}
