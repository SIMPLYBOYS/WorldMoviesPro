package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.framework.ImageTrasformation;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.imdb.MovieDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.util.BuildModelUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by aaron on 2016/9/3.
 */
public class FavoriteMoviesRecycleViewAdapter extends RecyclerView.Adapter<FavoriteMoviesRecycleViewAdapter.TrendsItemHolder> {
    private Activity activity;
    private List<ImdbObject> mItems;
    private AdapterView.OnItemClickListener mOnItemClickListener;
    private int mPlaceholderSize = 0; //default value
    private ProgressBar mProgressBar;
    private RequestQueue mQueue;

    public FavoriteMoviesRecycleViewAdapter(final Activity activity, List<ImdbObject> movieList) {
        mItems = movieList;
        this.activity = activity;
        mQueue = CustomVolleyRequestQueue.getInstance(activity).getRequestQueue();
    }

    @Override
    public TrendsItemHolder onCreateViewHolder(ViewGroup container, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View root = null;
        root = inflater.inflate(R.layout.movies_favorite_item, container, false);
        mProgressBar = (ProgressBar)root.findViewById(android.R.id.progress);
        return new TrendsItemHolder(root, this);
    }

    public void setItemCount(int count) {
        mItems.clear();
//        mItems.addAll(generateDummyData(count));
        notifyDataSetChanged();
    }

    public void addItem(int position, ImdbObject Item) {
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

    private void onItemHolderClick(TrendsItemHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.pictureView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        }
    }

    @Override
    public void onBindViewHolder(TrendsItemHolder itemHolder, int position) {
        final ImdbObject item = mItems.get(position - mPlaceholderSize);
        if (item!= null) {
            final String Url = item.getPosterUrl();
            if (!Url.isEmpty()) {
                mProgressBar.setVisibility(View.VISIBLE);
                itemHolder.bind(itemHolder, item, mProgressBar);
                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestDataRefresh(item.getTitle());
                    }
                };

                if (item.getChannel() == -1) {
                    itemHolder.pictureView.setOnClickListener(listener);
                    itemHolder.titleView.setOnClickListener(listener);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected class TrendsItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private FavoriteMoviesRecycleViewAdapter mAdapter;
        private ImageView pictureView;
        private TextView titleView;

        public TrendsItemHolder(View itemView, FavoriteMoviesRecycleViewAdapter adapter) {
            super(itemView);
            itemView.setOnClickListener(this);
            mAdapter = adapter;
            pictureView = (ImageView) itemView.findViewById(R.id.picture);
            pictureView.setScaleType(ImageView.ScaleType.CENTER);
            titleView = (TextView) itemView.findViewById(R.id.movie_title);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                titleView.setBackgroundResource(R.drawable.item_click_background);
        }

        public void bind(TrendsItemHolder itemHolder, ImdbObject item, final ProgressBar mProgressBar) {

            Picasso.with(pictureView.getContext()).load(item.getPosterUrl()).placeholder(R.drawable.placeholder)
                    .transform(ImageTrasformation.getTransformation(itemHolder.pictureView))
                    .into(pictureView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {}
                    });
            titleView.setText(item.getTitle());
        }

        @Override
        public void onClick(View v) {
            mAdapter.onItemHolderClick(this);
        }
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;
        mQueue = CustomVolleyRequestQueue.getInstance(activity).getRequestQueue();
        CustomJSONObjectRequest jsonRequest_q = null;
        String url = null;
        String searchChannel = CredentialsHandler.getCountry(activity);
        // String searchGenre = CredentialsHandler.getGenre(this); TODO search by genre

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");
                url= Config.HOST_NAME + "imdb?title=" + Query;
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }

            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray contents = response.getJSONArray("contents");
                        ImdbObject item = BuildModelUtils.buildImdbModel(contents);
                        Intent intent = new Intent(activity, MovieDetailActivity.class);
                        intent.putExtra(ImdbActivity.IMDB_OBJECT, item);
                        ActivityCompat.startActivity(activity, intent, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(activity, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
                }
            });
            mQueue.add(jsonRequest_q);
            return;
        }

        mQueue.add(jsonRequest); //trigger volley request
    }

}
