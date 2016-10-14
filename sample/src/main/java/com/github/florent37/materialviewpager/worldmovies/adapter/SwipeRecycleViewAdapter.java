package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.Movie;

import java.util.List;

/**
 * Created by aaron on 2016/3/22.
 */
public class SwipeRecycleViewAdapter extends RecyclerView.Adapter<SwipeRecycleViewAdapter.ViewHolder> {
    private Activity activity;
    private LayoutInflater inflater;
    List<Movie> movieList;
    private String[] bgColors;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyViewHolderClick mListener;
        private TextView serial;
        private TextView title;

        public ViewHolder(View itemView, MyViewHolderClick listener){
            super(itemView);
            mListener = listener;
            serial = (TextView) itemView.findViewById(R.id.serial);
            title = (TextView) itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            mListener.clickOnView(v, getLayoutPosition());
        }

        public interface MyViewHolderClick {
            void clickOnView(View v, int position);
        }

        public void bind(Movie movie, String color){
            serial.setText(String.valueOf(movie.getId()));
            title.setText(movie.getTitle());
            serial.setBackgroundColor(Color.parseColor(color));
        }
    }

    public SwipeRecycleViewAdapter(Activity activity, List<Movie> movieList){
        this.activity = activity;
        this.movieList = movieList;
        this.bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.d("0322", "onCreateViewHolder");
        Context context = viewGroup.getContext();
        View convertView = LayoutInflater.from(context).inflate(R.layout.list_item_movie, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(convertView, new ViewHolder.MyViewHolderClick() {
            @Override
            public void clickOnView(View v, int position) {
                Movie movie = movieList.get(position);
                Snackbar.make(v, movie.title, Snackbar.LENGTH_LONG).show();
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.d("0322", "onBindViewHolder: " + position);
        Movie movie = movieList.get(position);
        String color = bgColors[position % bgColors.length];
        viewHolder.bind(movie, color);
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

}
