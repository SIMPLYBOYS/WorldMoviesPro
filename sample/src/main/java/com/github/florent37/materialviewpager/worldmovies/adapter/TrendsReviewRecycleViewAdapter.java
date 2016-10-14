package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.ReviewItem;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by aaron on 2016/7/24.
 */
public class TrendsReviewRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final TypedValue mTypedValue = new TypedValue();
    private ArrayList<ReviewItem> reviewList;
    private int mPlaceholderSize = 0; //default value
    private int mBackground;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public static int visibleThreshold = 2;
    public static int lastVisibleItem, totalItemCount;
    public static boolean loading;

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
        public TrendsObject.CastItem castItem;

        public final View mView;
        public final ImageView mImageView;
        public final TextView mReviewerView, mDateView, mPointView, mTopicView, mContentView;

        public ContentViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.avatar);
            mReviewerView = (TextView) view.findViewById(R.id.viewer);
            mDateView = (TextView) view.findViewById(R.id.date);
            mPointView = (TextView) view.findViewById(R.id.points_text);
            mTopicView = (TextView) view.findViewById(R.id.topic_text);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mReviewerView.getText();
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    public ReviewItem getValueAt(int position) {
        return reviewList.get(position);
    }

    public TrendsReviewRecycleViewAdapter(Context context, ArrayList<ReviewItem> items) {
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
        mBackground = mTypedValue.resourceId;
        reviewList = items;
    }

    @Override
    public RecyclerView.ViewHolder  onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final Context context = viewGroup.getContext();
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.review_list_item, viewGroup, false);
            view.setBackgroundResource(mBackground);
            return new ContentViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.loading_item, viewGroup, false);
            return new ProgressViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return reviewList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ContentViewHolder) {
            final ReviewItem item = reviewList.get(position - mPlaceholderSize);
            ContentViewHolder myHolder = (ContentViewHolder) viewHolder;
            myHolder.mReviewerView.setText(item.getViewer());
            myHolder.mDateView.setText(item.getDate());
            myHolder.mPointView.setText(String.valueOf(item.getPoint()));
            myHolder.mContentView.setText(item.getContent());
            myHolder.mContentView.setTextIsSelectable(true);

            myHolder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Context context = v.getContext();
                    Intent intent = new Intent(context, ContentWebViewActivity.class);
                    intent.putExtra("url", item.getUrl());
                    context.startActivity(intent);*/
                }
            });

            if (item.getTopic().compareTo("null") != 0) {
                myHolder.mTopicView.setText(item.getTopic());
                myHolder.mTopicView.setTextIsSelectable(true);
            }

            Picasso.with(myHolder.mImageView.getContext()).load(item.getAvatar()).placeholder(R.drawable.person_image_empty)
                    .fit()
                    .centerCrop()
                    .into(myHolder.mImageView);
        } else {
            ProgressViewHolder progressViewHolder = (ProgressViewHolder) viewHolder;
            progressViewHolder.progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return reviewList == null ? 0 : reviewList.size();
    }
}
