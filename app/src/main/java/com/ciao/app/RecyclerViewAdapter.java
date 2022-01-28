package com.ciao.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ciao.app.activity.Article;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String[]> data;

    public RecyclerViewAdapter(Context context, ArrayList<String[]> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_main, parent, false);
        RecyclerViewAdapter.ViewHolder viewHolder = new RecyclerViewAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(data.get(position)[2]);
        holder.description.setText(data.get(position)[3]);

        if (data.get(position)[1] != null) {
            String source = data.get(position)[1];
            Glide.with(context).load(source).diskCacheStrategy(DiskCacheStrategy.ALL).into(holder.image);
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private CardView card;
        private ImageView image;
        private TextView title;
        private TextView description;

        public ViewHolder(View view) {
            super(view);
            card = view.findViewById(R.id.item_card);
            image = view.findViewById(R.id.item_image);
            title = view.findViewById(R.id.item_title);
            description = view.findViewById(R.id.item_description);
        }
    }
}
