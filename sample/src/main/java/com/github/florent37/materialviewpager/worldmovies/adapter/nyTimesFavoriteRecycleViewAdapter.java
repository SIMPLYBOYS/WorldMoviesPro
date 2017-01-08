package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.ImageTrasformation;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/8/16.
 */
public class nyTimesFavoriteRecycleViewAdapter extends RecyclerView.Adapter<nyTimesFavoriteRecycleViewAdapter.NyTimesItemHolder>  {
    private Activity activity;
    private List<nyTimesMovie> mItems;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private int mPlaceholderSize = 0; //default value
    private ProgressBar mProgressBar;
    private nyTimesFavoritePreference favor;
    private CustomJSONObjectRequest jsonRequest_q = null;
    private RequestQueue mQueue;

    public nyTimesFavoriteRecycleViewAdapter(final Activity activity, List<nyTimesMovie> movieList) {
        mItems = movieList;
        this.activity = activity;
        favor = new nyTimesFavoritePreference();
        mQueue = CustomVolleyRequestQueue.getInstance(activity).getRequestQueue();
    }

    @Override
    public NyTimesItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = null;
        root = inflater.inflate(R.layout.nytimes_favorite_item, container, false);
        mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
        return new NyTimesItemHolder(root, this);
    }

    public void setItemCount(int count) {
        mItems.clear();
//        mItems.addAll(generateDummyData(count));
        notifyDataSetChanged();
    }

    public List <nyTimesMovie> getItem() {
        return mItems;
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
        final nyTimesMovie item = mItems.get(position - mPlaceholderSize);
        if (item!= null) {
            final String Url = item.getPicUrl();
            if (!Url.isEmpty()) {
                mProgressBar.setVisibility(View.VISIBLE);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "nyTimes?url=" + item.getLink(), new JSONObject(), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String story,imageUrl, head, description, editor, date, url;
                                    JSONArray contents = response.getJSONArray("contents");
                                    JSONObject reviewObj = contents.getJSONObject(0);
                                    story = reviewObj.getString("story");
                                    editor = reviewObj.getString("editor");
                                    date = reviewObj.getString("date");
                                    url = reviewObj.getString("url");
                                    JSONObject imgObj = reviewObj.getJSONObject("image");

                                    if (imgObj.has("src")) {
                                        imageUrl = imgObj.getString("src");
                                        description = imgObj.getString("description");
                                    } else {
                                        imageUrl = null;
                                        description = null;
                                    }

                                    head = item.getHeadline();
                                    nyTimesMovie movie = new nyTimesMovie(head, description, story, url, imageUrl, editor ,date);
                                    if (checkBookmark(head))
                                        movie.setBookmark(true);
                                    Intent intent = new Intent(activity, nyTimesDetailActivity.class);
                                    intent.putExtra("movie", movie);
                                    ActivityCompat.startActivity(activity, intent, null);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener () {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(activity, "Remote Server not working!", Toast.LENGTH_LONG).show();
                            }
                        });
                        mQueue.add(jsonRequest_q);
                    }
                };
                itemHolder.bind(itemHolder, item, mProgressBar);
                itemHolder.pictureView.setOnClickListener(listener);
            }
        }
    }

    private boolean checkBookmark(String headline) {
        headline = headline.indexOf(":") != -1 ? headline.split(":")[1].trim() : headline;
        ArrayList list = favor.loadFavorites(activity.getApplicationContext());

        if (list == null)
            return false;

        for (int i=0; i<list.size(); i++) {
            if (headline.compareTo((String) list.get(i)) == 0) return true;
        }

        return false;
    };

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class NyTimesItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private nyTimesFavoriteRecycleViewAdapter mAdapter;
        private ImageView pictureView;
        private TextView review;

        public NyTimesItemHolder(View itemView, nyTimesFavoriteRecycleViewAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);
            mAdapter = adapter;
            pictureView = (ImageView) itemView.findViewById(R.id.picture);
            review = (TextView) itemView.findViewById(R.id.review_title);
        }

        public void bind(NyTimesItemHolder itemHolder, nyTimesMovie item, final ProgressBar mProgressBar) {
            Picasso.with(pictureView.getContext()).load(item.getPicUrl()).transform(ImageTrasformation.getTransformation(itemHolder.pictureView))
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
