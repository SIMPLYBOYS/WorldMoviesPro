package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.ImageTrasformation;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by aaron on 2016/9/19.
 */
public class FacebookFavoriteRecycleViewAdapter extends RecyclerView.Adapter<FacebookFavoriteRecycleViewAdapter.FacebookMovieHolder> {
    private List<nyTimesMovie> mItems;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private int mPlaceholderSize = 0; //default value
    private ProgressBar mProgressBar;

    public FacebookFavoriteRecycleViewAdapter(List<nyTimesMovie> movieList) {
        mItems = movieList;
    }

    @Override
    public FacebookMovieHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = null;
        root = inflater.inflate(R.layout.facebook_favorite_item, container, false);
        mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
        return new FacebookMovieHolder(root, this);
    }

    public void setItemCount(int count) {
        mItems.clear();
//        mItems.addAll(generateDummyData(count));
        notifyDataSetChanged();
    }

    public void addItem(int position, nyTimesMovie Item) {
        if (position > mItems.size()) return;
        mItems.add(position, Item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        if (position >= mItems.size()) return;

        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private void onItemHolderClick(FacebookMovieHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.pictureView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    @Override
    public void onBindViewHolder(FacebookMovieHolder itemHolder, int position) {
        nyTimesMovie item = mItems.get(position - mPlaceholderSize);
        if (item!= null) {
            final String Url = item.getPicUrl();
            /*if (!Url.isEmpty()) {
                mProgressBar.setVisibility(View.VISIBLE);
                itemHolder.bind(itemHolder, item, mProgressBar);
            }*/
            mProgressBar.setVisibility(View.VISIBLE);
            itemHolder.bind(itemHolder, item, mProgressBar);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class FacebookMovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FacebookFavoriteRecycleViewAdapter mAdapter;
        private ImageView pictureView;
        private TextView titleView;

        public FacebookMovieHolder(View itemView, FacebookFavoriteRecycleViewAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);
            mAdapter = adapter;
            pictureView = (ImageView) itemView.findViewById(R.id.picture);
            pictureView.setScaleType(ImageView.ScaleType.CENTER);
            titleView = (TextView) itemView.findViewById(R.id.movie_title);
        }

        public void bind(FacebookMovieHolder itemHolder, nyTimesMovie item, final ProgressBar mProgressBar) {

            Picasso.with(pictureView.getContext()).load("http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg").placeholder(R.drawable.placeholder)
                    .transform(ImageTrasformation.getTransformation(itemHolder.pictureView))
                    .into(pictureView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {}
                    });
            titleView.setText(item.getHeadline());
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }
}
