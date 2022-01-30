package com.ciao.app.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ciao.app.Functions;
import com.ciao.app.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

/**
 * Activity showing the main screen
 */
public class Main extends AppCompatActivity {
    /**
     * SharedPreferences
     */
    private SharedPreferences sharedPreferences;
    /**
     * SharedPreferences editor
     */
    private SharedPreferences.Editor editor;

    /**
     * Create Activity
     *
     * @param savedInstanceState Not used
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        Boolean firstTime = sharedPreferences.getBoolean("firstTime", true);
        if (firstTime) {
            finish();
            startActivity(new Intent(this, Login.class));
        }

        String theme = sharedPreferences.getString("theme", "light");
        Functions.setTheme(theme);

        //setContentView(R.layout.loading_screen);

        setContentView(R.layout.activity_main);

        NavigationView navigationView = findViewById(R.id.nav_view);
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

    /**
     * Open navigation drawer
     */
    public void openDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.open();
    }

    /**
     * Start Settings Activity
     */
    public void settings() {
        startActivity(new Intent(this, Settings.class));
    }

    /**
     * Search
     *
     * @param view View
     */
    public void search(View view) {
        EditText editText = findViewById(R.id.main_searchBar);
        String text = editText.getText().toString();
    }

    /**
     * Adapter for RecyclerView
     */
    public static class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        /**
         * Context
         */
        private Context context;
        /**
         * Data
         */
        private ArrayList<String[]> data;

        /**
         * Constructor
         *
         * @param context Context
         * @param data    Data
         */
        public RecyclerViewAdapter(Context context, ArrayList<String[]> data) {
            this.context = context;
            this.data = data;
        }

        /**
         * Action on creation of ViewHolder
         *
         * @param parent   ViewGroup
         * @param viewType Type of view
         * @return ViewHolder
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.list_item_main, parent, false);
            return new RecyclerViewAdapter.ViewHolder(view);
        }

        /**
         * Action on bind of ViewHolder
         *
         * @param holder   ViewHolder
         * @param position Position
         */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.title.setText(data.get(position)[2]);
            holder.description.setText(data.get(position)[3]);
            if (data.get(position)[1] != null) {
                String source = data.get(position)[1];
                Drawable placeholder = AppCompatResources.getDrawable(context, R.drawable.no_image);
                Glide.with(context).load(source).placeholder(placeholder).error(placeholder).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.image);
            }
            String id = data.get(position)[0];
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, Article.class);
                    intent.putExtra("id", id);
                    context.startActivity(intent);
                }
            });
        }

        /**
         * Get number of items
         *
         * @return Number of items
         */
        @Override
        public int getItemCount() {
            return data.size();
        }

        /**
         * ViewHolder
         */
        private class ViewHolder extends RecyclerView.ViewHolder {
            /**
             * Card
             */
            private CardView card;
            /**
             * Image of item
             */
            private ImageView image;
            /**
             * Title of item
             */
            private TextView title;
            /**
             * Description of item
             */
            private TextView description;

            /**
             * Constructor
             *
             * @param view View
             */
            public ViewHolder(View view) {
                super(view);
                card = view.findViewById(R.id.item_card);
                image = view.findViewById(R.id.item_image);
                title = view.findViewById(R.id.item_title);
                description = view.findViewById(R.id.item_description);
            }
        }
    }
}