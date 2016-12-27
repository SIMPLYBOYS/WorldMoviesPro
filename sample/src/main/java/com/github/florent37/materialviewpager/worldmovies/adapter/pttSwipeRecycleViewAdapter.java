package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.ContentWebViewActivity;
import com.github.florent37.materialviewpager.worldmovies.framework.FlatButton;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.github.florent37.materialviewpager.worldmovies.ptt.pttMovie;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;
import com.sackcentury.shinebuttonlib.ShineButton;

import org.json.JSONArray;

import java.util.List;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/12/23.
 */

public class pttSwipeRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity activity;
    private List<pttMovie> movieList;
    private String[] bgColors;
    private String HOST_NAME = Config.HOST_NAME;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private RequestQueue mQueue;
    public int visibleThreshold = 2;
    public static boolean swipe;
    public int lastVisibleItem, totalItemCount;
    public boolean loading;
    CustomJSONObjectRequest jsonRequest_q = null;

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public pttSwipeRecycleViewAdapter.UserViewHolder.MyViewHolderClick mListener;
        private TextView headline;
        private TextView date;
        private TextView author;
        private FlatButton moreButton;
        private ImageView photoView;
        private ImageView shareView;
        private ShineButton bookmarkView;

        public UserViewHolder(View itemView, pttSwipeRecycleViewAdapter.UserViewHolder.MyViewHolderClick listener) {
            super(itemView);
            mListener = listener;
            headline = (TextView) itemView.findViewById(R.id.headline);
            date = (TextView) itemView.findViewById(R.id.date);
            author = (TextView) itemView.findViewById(R.id.author);
            shareView = (ImageView) itemView.findViewById(R.id.share);
            moreButton = (FlatButton) itemView.findViewById(R.id.button_more);
            bookmarkView = (ShineButton) itemView.findViewById(R.id.bookmark);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.clickOnView(v, getLayoutPosition());
        }

        public interface MyViewHolderClick {
            void clickOnView(View v, int position);
        }

        public void bind(pttMovie movie, String color) {
            shareView.setColorFilter(shareView.getContext().getResources().getColor(R.color.navdrawer_icon_tint));
            headline.setText(String.valueOf(movie.getHeadline()));
            author.setText(movie.getEditor());
            date.setText(movie.getPublication_date());

            if (movie.getBookmark()) {
                bookmarkView.setChecked(true);
                bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
            } else {
                bookmarkView.setChecked(false);
                bookmarkView.setBackgroundResource(R.drawable.ic_turned_in);
                bookmarkView.setColorFilter(shareView.getContext().getResources().getColor(R.color.navdrawer_icon_tint));
            }
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    public pttSwipeRecycleViewAdapter(final Activity activity, List<pttMovie> movieList) {
        this.activity = activity;
        this.movieList = movieList;
        this.bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
        mQueue = CustomVolleyRequestQueue.getInstance(activity).getRequestQueue();
        User user = UsersUtils.getCurrentUser(activity.getApplicationContext());
    }

    @Override
    public int getItemViewType(int position) {
        return movieList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private void createShareIntent(pttMovie movie) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, movie.getLink());
        // Launch sharing dialog for image
        activity.startActivity(Intent.createChooser(shareIntent, "Share Review"));
    }

    public void setupFacebookShareIntent(nyTimesMovie movie) {
        ShareDialog shareDialog;
        FacebookSdk.sdkInitialize(activity);
        shareDialog = new ShareDialog(activity);

        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle("Title")
                .setContentDescription(
                        "\"Body Of Test Post\"")
                .setContentUrl(Uri.parse(movie.getLink()))
                .build();

        shareDialog.show(linkContent);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final Context context = viewGroup.getContext();
        if (viewType == VIEW_TYPE_ITEM) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.list_item_ptt_review, viewGroup, false);

            TextView author = (TextView) convertView.findViewById(R.id.author);
            TextView headline = (TextView) convertView.findViewById(R.id.headline);
            author.setTextIsSelectable(true);
            headline.setTextIsSelectable(true);
            pttSwipeRecycleViewAdapter.UserViewHolder viewHolder = new pttSwipeRecycleViewAdapter.UserViewHolder(convertView, new pttSwipeRecycleViewAdapter.UserViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                }
            });
            return viewHolder;
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.loading_item, viewGroup, false);
            return new pttSwipeRecycleViewAdapter.ProgressViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof pttSwipeRecycleViewAdapter.UserViewHolder) {
            LOGD("0322", "onBindViewHolder: " + position);
            pttSwipeRecycleViewAdapter.UserViewHolder myHolder = (pttSwipeRecycleViewAdapter.UserViewHolder) viewHolder;
            String headline;
            final pttMovie movie = movieList.get(position);
            final ShineButton bookmarkView = (ShineButton) ((pttSwipeRecycleViewAdapter.UserViewHolder) viewHolder).bookmarkView.findViewById(R.id.bookmark);
            String color = bgColors[position % bgColors.length];
            ShineButton shareView = (ShineButton) ((pttSwipeRecycleViewAdapter.UserViewHolder) viewHolder).shareView.findViewById(R.id.share);
            FlatButton moreView = (FlatButton) ((pttSwipeRecycleViewAdapter.UserViewHolder) viewHolder).moreButton.findViewById(R.id.button_more);
            bookmarkView.init(activity);
            shareView.init(activity);

            /*bookmarkView.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, boolean checked) {
//                    Snackbar.make(view, "Bookmark "+checked+" !!!", Snackbar.LENGTH_LONG).show();
                    if (checked && !movie.getBookmark()) {
                        bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                        User user = UsersUtils.getCurrentUser(activity.getApplicationContext());
                        movie.setBookmark(true);
                        String headline = movie.getHeadline().indexOf(":") != -1 ? movie.getHeadline().split(":")[1].trim() : movie.getHeadline();
                        CustomJSONObjectRequest jsonRequest_q = null;
                        String url = HOST_NAME + "nyTimes/"+user.id;
                        JSONObject jsonBody = new JSONObject();

                        try {
                            jsonBody.put("headline", headline);
                            jsonBody.put("link", movie.getLink());
                            jsonBody.put("picUrl", movie.getPicUrl());
                            favor.addFavorite(activity.getApplicationContext(), headline);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String result = response.getString("content");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(activity, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        mQueue.add(jsonRequest_q);
                    } else if (!checked && movie.getBookmark()) {
                        bookmarkView.setBackgroundResource(R.drawable.ic_turned_in);
                        User user = UsersUtils.getCurrentUser(activity.getApplicationContext());
                        String headline = movie.getHeadline().indexOf(":") != -1 ? movie.getHeadline().split(":")[1].trim() : movie.getHeadline();
//                        String headline = movie.getHeadline();
                        favor.removeFavorite(activity.getApplicationContext(), headline);
                        CustomJSONObjectRequest jsonRequest_q = null;
                        headline = ParserUtils.encode(headline);
                        String url = HOST_NAME + "nyTimes/"+user.id+headline;
                        JSONObject jsonBody = new JSONObject();

                        try {
                            jsonBody.put("headline", headline);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.DELETE, url, jsonBody, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String result = response.getString("content");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(activity, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                            }
                        });

                        mQueue.add(jsonRequest_q);
                    }
                }
            });*/

            shareView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createShareIntent(movie);
                }
            });

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ContentWebViewActivity.class);
                    intent.putExtra("url", movie.getLink());
                    context.startActivity(intent);
                }
            };

            myHolder.bind(movie, color);
            moreView.setOnClickListener(listener);
//            photoView.setOnClickListener(listener);
        } else {
            pttSwipeRecycleViewAdapter.ProgressViewHolder progressViewHolder = (pttSwipeRecycleViewAdapter.ProgressViewHolder) viewHolder;
            progressViewHolder.progressBar.setIndeterminate(true);
        }
    }

    public String[] getStringArray(JSONArray jsonArray) {
        String[] stringArray = null;
        int length = jsonArray.length();
        if (jsonArray!=null) {
            stringArray = new String[length];
            for(int i=0;i<length;i++){
                stringArray[i]= jsonArray.optString(i);
            }
        }
        return stringArray;
    }

    public void setLoaded() {
        loading = false;
        swipe = false;
    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }
}
