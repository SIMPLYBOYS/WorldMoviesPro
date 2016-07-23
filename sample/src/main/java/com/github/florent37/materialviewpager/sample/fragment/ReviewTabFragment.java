package com.github.florent37.materialviewpager.sample.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.model.TrendsObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by aaron on 2016/7/2.
 */
public class ReviewTabFragment extends Fragment {

    private TrendsObject trendsObject;
    private Gson gson = new Gson();

    public static ReviewTabFragment newInstance(TrendsObject trendsObject) {
        ReviewTabFragment fragment = new ReviewTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("trends", trendsObject);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        trendsObject = (TrendsObject) getArguments().getSerializable("trends");
        RecyclerView rv = (RecyclerView) inflater.inflate(
                R.layout.fragment_recyclerview, container, false);
        setupRecyclerView(rv);
        return rv;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(),
                getReviewlist()));
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(recyclerView.getContext()).build());
    }

    private ArrayList<TrendsObject.ReviewItem> getReviewlist() {
        ArrayList<TrendsObject.ReviewItem> reviewItems = new ArrayList<> ();
        JsonArray reviewInfo = new JsonParser().parse(trendsObject.getReview()).getAsJsonArray();
        JsonElement jsonElement = null;
        TrendsObject.ReviewItem reviewItem = null;
        for(int i=0; i<reviewInfo.size(); i++ ) {
            jsonElement = reviewInfo.get(i);
            reviewItem = gson.fromJson(jsonElement, TrendsObject.ReviewItem.class);
            reviewItems.add(reviewItem);
        }
        return reviewItems;
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private ArrayList<TrendsObject.ReviewItem> mItems;
        private int mPlaceholderSize = 0; //default value
        private int mBackground;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TrendsObject.CastItem castItem;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mReviewerView, mDateView, mPointView, mTopicView, mContentView;

            public ViewHolder(View view) {
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

        public TrendsObject.ReviewItem getValueAt(int position) {
            return mItems.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context, ArrayList<TrendsObject.ReviewItem> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.review_list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final TrendsObject.ReviewItem item = mItems.get(position - mPlaceholderSize);
            holder.mReviewerView.setText(item.getViewer());
            holder.mDateView.setText(item.getDate());
            holder.mPointView.setText(String.valueOf(item.getPoint()));
            holder.mTopicView.setText(item.getTopic());
            holder.mTopicView.setTextIsSelectable(true);
            holder.mContentView.setText(item.getContent());
            holder.mContentView.setTextIsSelectable(true);
            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Context context = v.getContext();
                    Intent intent = new Intent(context, TrendsWebViewActivity.class);
                    intent.putExtra("url", item.getUrl());
                    context.startActivity(intent);*/
                }
            });

            Picasso.with(holder.mImageView.getContext()).load(item.getAvatar()).placeholder(R.drawable.person_image_empty)
                    .fit()
                    .centerCrop()
                    .into(holder.mImageView);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }
}
