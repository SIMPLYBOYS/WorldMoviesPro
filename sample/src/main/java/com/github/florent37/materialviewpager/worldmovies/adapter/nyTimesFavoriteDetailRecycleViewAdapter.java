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
 * Created by aaron on 2016/9/18.
 */
public class nyTimesFavoriteDetailRecycleViewAdapter extends RecyclerView.Adapter<nyTimesFavoriteDetailRecycleViewAdapter.NyTimesItemHolder> {
    private List<nyTimesMovie> mItems;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private int mPlaceholderSize = 0; //default value
    private ProgressBar mProgressBar;

    public nyTimesFavoriteDetailRecycleViewAdapter(List<nyTimesMovie> movieList) {
        mItems = movieList;
    }

    @Override
    public NyTimesItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = null;
        root = inflater.inflate(R.layout.nytimes_favorite_item_detail, container, false);
        mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
        return new NyTimesItemHolder(root, this);
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

    private void onItemHolderClick(NyTimesItemHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.pictureView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    @Override
    public void onBindViewHolder(NyTimesItemHolder itemHolder, int position) {
        nyTimesMovie item = mItems.get(position - mPlaceholderSize);
        if (item!= null) {
            final String Url = item.getPicUrl();
            if (!Url.isEmpty()) {
                mProgressBar.setVisibility(View.VISIBLE);
                itemHolder.bind(itemHolder, item, mProgressBar);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class NyTimesItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private nyTimesFavoriteDetailRecycleViewAdapter mAdapter;
        private ImageView pictureView;
        private TextView review;

        public NyTimesItemHolder(View itemView, nyTimesFavoriteDetailRecycleViewAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);
            mAdapter = adapter;
            pictureView = (ImageView) itemView.findViewById(R.id.picture);
            review = (TextView) itemView.findViewById(R.id.review_title);
        }

        public void bind(NyTimesItemHolder itemHolder, nyTimesMovie item, final ProgressBar mProgressBar) {

            Picasso.with(pictureView.getContext()).load(item.getPicUrl()).placeholder(R.drawable.placeholder)
                    .transform(ImageTrasformation.getTransformation(itemHolder.pictureView))
                    .into(pictureView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {}
                    });
            review.setText(item.getHeadline());
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }
}
