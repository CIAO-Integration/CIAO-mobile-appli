package com.ciao.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.ciao.app.Functions;
import com.ciao.app.R;
import com.ciao.app.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class Main extends AppCompatActivity {
    public static final String BROADCAST_TARGET = "Main";
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private ImageDownloaderReceiver imageDownloaderReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageDownloaderReceiver = new ImageDownloaderReceiver();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = sharedPreferences.getString("theme", "light");
        Functions.setTheme(theme);

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(R.layout.loading_screen);

        refresh();
    }

    public void openDrawer() {
        drawer.open();
    }

    public void settings() {
        startActivity(new Intent(this, Settings.class));
    }

    public void search(View view) {
        EditText editText = findViewById(R.id.main_searchBar);
        String text = editText.getText().toString();
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

    public void refresh() {
        ArrayList<String[]> data = Functions.getData(R.string.sport);
        ArrayList<String> sources = new ArrayList<>();
        for (String[] d : data) {
            sources.add(d[1]);
        }
        Functions.downloadImgs(this, sources, Main.BROADCAST_TARGET);
    }

    public void init() {
        setContentView(binding.getRoot());

        drawer = binding.drawerLayout;

        NavigationView navigationView = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                String text = null;
                switch (destination.getId()) {
                    case R.id.nav_browse:
                        text = getString(R.string.browse);
                        break;
                    case R.id.nav_around:
                        text = getString(R.string.around);
                        break;
                    case R.id.nav_news:
                        text = getString(R.string.news);
                        break;
                    case R.id.nav_popularization:
                        text = getString(R.string.popularization);
                        break;
                    case R.id.nav_digital:
                        text = getString(R.string.digital);
                        break;
                    case R.id.nav_science:
                        text = getString(R.string.science);
                        break;
                    case R.id.nav_culture:
                        text = getString(R.string.culture);
                        break;
                    case R.id.nav_history:
                        text = getString(R.string.history);
                        break;
                    case R.id.nav_geography:
                        text = getString(R.string.geography);
                        break;
                    case R.id.nav_politics:
                        text = getString(R.string.politics);
                        break;
                    case R.id.nav_sport:
                        text = getString(R.string.sport);
                        break;
                }
                TextView title = findViewById(R.id.actionbar_title);
                title.setText(text);
            }
        });
        NavigationUI.setupWithNavController(navigationView, navController);

        /*if(!connected){
            Menu menu = navigationView.getMenu();
            menu.findItem(R.id.nav_around).setVisible(false);
        }*/

        findViewById(R.id.actionbar_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer();
            }
        });
        findViewById(R.id.actionbar_avatar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings();
            }
        });
    }

    private class ImageDownloaderReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            init();
        }
    }
}