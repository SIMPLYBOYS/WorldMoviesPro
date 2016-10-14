package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/3/31.
 */
public class ImdbCardRecycleViewAdapter extends RecyclerView.Adapter<ImdbCardRecycleViewAdapter.ContentViewHolder> {

    private final List<ImdbObject> mContentItems;
    private final int TYPE_HEADER = 0;
    private final int TYPE_CELL = 1;
    private static String VIDEO_KEY;
    private final Context context;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private ProgressBar mProgressBar;

    //the constants value of the header view
    static final int TYPE_PLACEHOLDER = Integer.MIN_VALUE;

    private boolean loading;

    //the size taken by the header
    private int mPlaceholderSize = 1; //default value

    public ImdbCardRecycleViewAdapter(Context context) {
        mContentItems = new ArrayList<>();
        this.context = context;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void clearItem() {
        mContentItems.clear();
    }

    public List <ImdbObject> getItem() {
        return mContentItems;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /*
    * Inserting a new item at the head of the list. This uses a specialized
    * RecyclerView method, notifyItemInserted(), to trigger any enabled item
    * animations in addition to updating the view.
    */
    public void addItem(int position, ImdbObject object) {
        Log.d("0414", "mContentItems size: " + mContentItems.size() + " position: " +position);
        if (position > mContentItems.size()) return;
        mContentItems.add(position, object);
//        notifyItemInserted(mContentItems.size());
    }

    /*
     * Inserting a new item at the head of the list. This uses a specialized
     * RecyclerView method, notifyItemRemoved(), to trigger any enabled item
     * animations in addition to updating the view.
     */
    public void removeItem(int position) {
        if (position >= mContentItems.size()) return;

        mContentItems.remove(position);
//        notifyItemRemoved(position);
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public View mView;
        private TextView titleView;
        private TextView topView;
        private TextView yearView;
        private TextView desciptionView;
        private TextView rattingView;
        private TextView votesView;
        private TextView deltaView;
        private ImageView posterView, arrowView;
        private ImdbCardRecycleViewAdapter mAdapter;
        private ImdbObject imdbObject;

        public ContentViewHolder(View itemView, ImdbCardRecycleViewAdapter adapter) {
            super(itemView);
            int tag = (Integer) itemView.getTag();
            if (tag != TYPE_PLACEHOLDER) {
                mView = itemView;
                mView.setOnClickListener(this);
                this.mAdapter = adapter;
                titleView = (TextView) itemView.findViewById(R.id.title);
                topView = (TextView) itemView.findViewById(R.id.top);
                rattingView = (TextView) itemView.findViewById(R.id.rating);
                arrowView = (ImageView) itemView.findViewById(R.id.arrow);
                yearView = (TextView)itemView.findViewById(R.id.year);
                votesView = (TextView) itemView.findViewById(R.id.votes);
                deltaView = (TextView) itemView.findViewById(R.id.delta);
                desciptionView = (TextView) itemView.findViewById(R.id.description);
                posterView = (ImageView) itemView.findViewById(R.id.poster);
                ImageView lineColorCode = (ImageView)itemView.findViewById(R.id.thumbnail);
                int color = Color.parseColor("#F3CE13"); //The color u want
                lineColorCode.setColorFilter(color);
                Picasso.with(posterView.getContext()).load(R.drawable.parisguidetower).into(posterView);
            }
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);;
        }

        public void bind(final ImdbObject imdbObject, final ProgressBar mProgressBar, final Context context) {
            String title = "";
            String description = "";
            int delta = Math.abs(imdbObject.getDelta());
            this.imdbObject = imdbObject;
            Gson gson = new Gson();
            ImdbObject.RatingItem ratingItem = null;
            JsonElement jsonElement = null;
            JsonObject ratingInfo = new JsonParser().parse(imdbObject.getRating()).getAsJsonObject();
            jsonElement = ratingInfo.getAsJsonObject();
            ratingItem = gson.fromJson(jsonElement, ImdbObject.RatingItem.class);

            if (titleView == null) return;

            title = imdbObject.getTitle();
            description = imdbObject.getDescription();
            titleView.setText(title);
            desciptionView.setText(description);
            topView.setText(imdbObject.getTop());
            yearView.setText(imdbObject.getYear());
            rattingView.setText(ratingItem.getScore());
            votesView.setText(ratingItem.getVotes());

            if (delta != 0) {
                deltaView.setText(String.valueOf(delta));
                arrowView.setVisibility(View.VISIBLE);
                if (imdbObject.getDelta() > 0)
                    arrowView.setImageResource(R.drawable.ic_trending_up);
                else
                    arrowView.setImageResource(R.drawable.ic_trending_down);
            } else if (delta == 0) {
                deltaView.setText("");
                arrowView.setVisibility(View.GONE);
            }

            Picasso.with(posterView.getContext()).load(imdbObject.getPosterUrl()).placeholder(R.drawable.placeholder).centerCrop().fit()
                    .into(posterView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });

            posterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent lightBoxIntent = new Intent(v.getContext(), CustomLightBoxActivity.class);
                    VIDEO_KEY = imdbObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
                    lightBoxIntent.putExtra(CustomLightBoxActivity.KEY_VIDEO_ID, VIDEO_KEY);
                    context.startActivity(lightBoxIntent);*/
                    Toast.makeText(context, "yes this icon could click!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mPlaceholderSize)
            return TYPE_PLACEHOLDER;

        position = position - mPlaceholderSize;

        if (position == 0)
            return TYPE_HEADER;
        else
            return TYPE_CELL;
    }

    @Override
    public int getItemCount() {

        Log.d("0324", "getItemCount: " + mContentItems.size() + mPlaceholderSize);

        if (mContentItems != null) {
            return mContentItems.size() + mPlaceholderSize;
        }
        return 0;
    }

    private void onItemHolderClick(ContentViewHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    @Override
    public ContentViewHolder onCreateViewHolder(ViewGroup container, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root;
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        Boolean cardType = settings.getBoolean("miniCard", false);

        Log.d("0216", "viewType: " + viewType);
        switch (viewType) {
            case TYPE_PLACEHOLDER: {
                Log.d("0320", "PAGER PLACEHOLDER");
                if (cardType)
                    root = inflater.inflate(R.layout.material_view_pager_mini_placeholder, container, false);
                else
                    root = inflater.inflate(R.layout.material_view_pager_placeholder, container, false);

                root.setTag(viewType);
                mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
                return new ContentViewHolder(root, this);
            }
            case TYPE_HEADER: {
                Log.d("0320", "TOP HEADER");
                if (cardType)
                    root = inflater.inflate(R.layout.imdb_list_item_card_small, container, false);
                else
                    root = inflater.inflate(R.layout.imdb_list_item_card_big, container, false);
                mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
                root.setTag(viewType);
                return new ContentViewHolder(root, this);
            }
            case TYPE_CELL: {
                Log.d("0320", "TOP CELL");
                if (cardType)
                    root = inflater.inflate(R.layout.imdb_list_item_card_small, container, false);
                else
                    root = inflater.inflate(R.layout.imdb_list_item_card_big, container, false);
                mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
                root.setTag(viewType);
                return new ContentViewHolder(root, this);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ContentViewHolder holder, final int position) {
        Log.d("0327","position: " + position + " getItemViewType(position): " + getItemViewType(position));

        switch (getItemViewType(position)) {
            case TYPE_PLACEHOLDER:
                Log.d("0327", "MaterialViewPager->onBindViewHolder@ placeHolder");
                break;
            default:
                ImdbObject imdbObject = mContentItems.get(position - mPlaceholderSize);
                if (imdbObject!= null){
                    final String title = imdbObject.getTitle();
                    Log.d("0327", String.valueOf(position) + " title:" + title);
                    mProgressBar.setVisibility(View.VISIBLE);
                    holder.bind(imdbObject, mProgressBar, context);
                }
                break;
        }
    }
}
