package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.content.Context;
import android.content.Intent;
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

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.framework.ContentWebViewActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by aaron on 2016/6/30.
 */
public class TrendsCastTabFragment extends Fragment {

    private TrendsObject trendsObject;
    private Gson gson = new Gson();

    public static TrendsCastTabFragment newInstance(TrendsObject trendsObject) {
        TrendsCastTabFragment fragment = new TrendsCastTabFragment();
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
                getCastlist()));
//        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(recyclerView.getContext()).build());
    }

    private ArrayList<TrendsObject.CastItem> getCastlist() {
        ArrayList<TrendsObject.CastItem> castItems = new ArrayList<> ();
        JsonArray castInfo = new JsonParser().parse(trendsObject.getCast()).getAsJsonArray();
        JsonElement jsonElement = null;
        TrendsObject.CastItem castItem = null;
        for(int i=0; i<castInfo.size(); i++ ) {
            jsonElement = castInfo.get(i);
            castItem = gson.fromJson(jsonElement, TrendsObject.CastItem.class);
            castItems.add(castItem);
        }
        return castItems;
    }

    public static class SimpleStringRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleStringRecyclerViewAdapter.ViewHolder> {

        private final TypedValue mTypedValue = new TypedValue();
        private ArrayList<TrendsObject.CastItem> mItems;
        private int mPlaceholderSize = 0; //default value
        private int mBackground;

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public TrendsObject.CastItem castItem;

            public final View mView;
            public final ImageView mImageView;
            public final TextView mCastView, mAsView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mImageView = (ImageView) view.findViewById(R.id.avatar);
                mCastView = (TextView) view.findViewById(R.id.cast);
                mAsView = (TextView) view.findViewById(R.id.as);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mCastView.getText();
            }
        }

        public TrendsObject.CastItem getValueAt(int position) {
            return mItems.get(position);
        }

        public SimpleStringRecyclerViewAdapter(Context context, ArrayList<TrendsObject.CastItem> items) {
            context.getTheme().resolveAttribute(R.attr.selectableItemBackground, mTypedValue, true);
            mBackground = mTypedValue.resourceId;
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cast_list_item, parent, false);
            view.setBackgroundResource(mBackground);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final TrendsObject.CastItem item = mItems.get(position - mPlaceholderSize);
            holder.mCastView.setText(item.getCast());
            holder.mAsView.setText(item.getAs());
            holder.mAsView.setTextIsSelectable(true);

            holder.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ContentWebViewActivity.class);
                    intent.putExtra("url", item.getUrl());
                    context.startActivity(intent);
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
