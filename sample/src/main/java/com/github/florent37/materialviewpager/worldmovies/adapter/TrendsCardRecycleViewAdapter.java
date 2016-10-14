package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.CustomLightBoxActivity;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/6/17.
 */
public class TrendsCardRecycleViewAdapter extends RecyclerView.Adapter<TrendsCardRecycleViewAdapter.ContentViewHolder> {
    private final List<TrendsObject> mContentItems;
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

    public TrendsCardRecycleViewAdapter(Context context) {
        mContentItems = new ArrayList<>();
        this.context = context;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void clearItem() {
        mContentItems.clear();
    }

    public List <TrendsObject> getItem() {
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
    public void addItem(int position, TrendsObject object) {
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
        private TrendsCardRecycleViewAdapter mAdapter;
        private TrendsObject trendsObject;

        public ContentViewHolder(View itemView, TrendsCardRecycleViewAdapter adapter) {
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
                Picasso.with(posterView.getContext()).load(R.drawable.parisguidetower).into(posterView);
            }
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);;
        }

        public void bind(final TrendsObject trendsObject, final ProgressBar mProgressBar, final Context context) {
                    String title = trendsObject.getTitle();
                    this.trendsObject = trendsObject;
                    if (titleView == null)
                        return;
                    titleView.setText(title);
                    topView.setText(trendsObject.getTop());
                    Gson gson = new Gson();
                    JsonArray staffInfo = new JsonParser().parse(trendsObject.getStaff()).getAsJsonArray();
                    JsonArray castInfo = new JsonParser().parse(trendsObject.getCast()).getAsJsonArray();
                    JsonObject ratingInfo = new JsonParser().parse(trendsObject.getRating()).getAsJsonObject();
                    TrendsObject.StaffItem staffItem = null;
                    TrendsObject.CastItem castItem = null;
                    TrendsObject.RatingItem ratingItem = null;
                    JsonElement jsonElement = null;
                    if (trendsObject.getReleaseDate().indexOf("公開日")!= -1)
                        yearView.setText(trendsObject.getReleaseDate().split("公開日")[1].trim());
                    else
                        yearView.setText(trendsObject.getReleaseDate());

                    /*------- rating -------*/
                    jsonElement = ratingInfo.getAsJsonObject();
                    ratingItem = gson.fromJson(jsonElement, TrendsObject.RatingItem.class);
                    rattingView.setText(ratingItem.getScore());
                    if (ratingItem.getVotes() != null)
                        votesView.setText(ratingItem.getVotes());
                    else
                        votesView.setVisibility(View.GONE);

                    /*yearView.setText(tObject.getYear());
                    rattingView.setText(tObject.getRatting());
                    votesView.setText(tObject.getVotes());
                    int delta = Math.abs(tObject.getDelta());
                    Log.d("0601: ", String.valueOf(delta));
                    if (delta != 0) {
                        deltaView.setText(String.valueOf(delta));
                        arrowView.setVisibility(View.VISIBLE);
                        if (tObject.getDelta() > 0)
                            arrowView.setImageResource(R.drawable.ic_trending_up);
                        else
                            arrowView.setImageResource(R.drawable.ic_trending_down);
                    } else if (delta == 0) {
                        deltaView.setText("");
                        arrowView.setVisibility(View.GONE);
                    }*/

                    if (staffInfo.size() > 0) {
                        jsonElement = staffInfo.get(0);
                        staffItem = gson.fromJson(jsonElement, TrendsObject.StaffItem.class);
                        String desciption;
                        if (staffItem.getStaff().indexOf(':') != -1)
                            desciption = staffItem.getStaff().split(":")[1] + " (dir)";
                        else
                            desciption = staffItem.getStaff() + " (dir)";

                        for(int i=0; i<castInfo.size(); i++ ) {
                            jsonElement = castInfo.get(i);
                            castItem = gson.fromJson(jsonElement, TrendsObject.CastItem.class);
                            if (castItem.getCast().indexOf(':') != -1)
                                desciption += ", " + castItem.getCast().split(":")[0];
                            else
                                desciption += ", " + castItem.getCast();
                        }
                        desciptionView.setText(desciption);
                    } else {
                        desciptionView.setText("");
                    }

                    String path = trendsObject.getPosterUrl();
                    Log.d("0921", path);

                    Picasso.with(posterView.getContext()).load(path != null ? path : "").placeholder(R.drawable.placeholder).centerCrop().fit()
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
                           Intent lightBoxIntent = new Intent(v.getContext(), CustomLightBoxActivity.class);
                           VIDEO_KEY = trendsObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
                           lightBoxIntent.putExtra(CustomLightBoxActivity.KEY_VIDEO_ID, VIDEO_KEY);
                           context.startActivity(lightBoxIntent);
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

        switch (viewType) {
            case TYPE_PLACEHOLDER: {
                if (cardType)
                    root = inflater.inflate(R.layout.material_view_pager_mini_placeholder, container, false);
                else
                    root = inflater.inflate(R.layout.material_view_pager_placeholder, container, false);

                root.setTag(viewType);
                mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
                return new ContentViewHolder(root, this);
            }
            case TYPE_HEADER: {
                if (cardType)
                    root = inflater.inflate(R.layout.trends_small_card, container, false);
                else
                    root = inflater.inflate(R.layout.trends_big_card, container, false);
                mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
                root.setTag(viewType);
                return new ContentViewHolder(root, this);
            }
            case TYPE_CELL: {
                if (cardType)
                    root = inflater.inflate(R.layout.trends_small_card, container, false);
                else
                    root = inflater.inflate(R.layout.trends_big_card, container, false);
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
                TrendsObject trendsObject = mContentItems.get(position - mPlaceholderSize);
                if (trendsObject!= null) {
                    final String title = trendsObject.getTitle();
                    Log.d("0327", String.valueOf(position) + " title:" + title);
                    mProgressBar.setVisibility(View.VISIBLE);
                    holder.bind(trendsObject, mProgressBar, context);
                }
                break;
        }
    }
}
