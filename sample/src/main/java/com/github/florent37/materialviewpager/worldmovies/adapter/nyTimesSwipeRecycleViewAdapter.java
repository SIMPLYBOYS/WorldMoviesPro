package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.Movie;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.util.ParserUtils;
import com.github.florent37.materialviewpager.worldmovies.util.PrefUtils;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/6/11.
 */
public class nyTimesSwipeRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity activity;
    private List<Movie> movieList;
    private String[] bgColors;
    private String HOST_NAME = Config.HOST_NAME;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private RequestQueue mQueue;
//    private final CustomJSONArrayRequest jsonRequest;
    public int visibleThreshold = 2;
    public int lastVisibleItem, totalItemCount;
    public boolean loading;
    private nyTimesFavoritePreference favor;

    CustomJSONObjectRequest jsonRequest_q = null;

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyViewHolderClick mListener;
        private TextView headline;
        private TextView date;
        private TextView summary;
        private ImageView moreButton;
        private ImageView photoView;
        private ImageView shareView;
        private ShineButton bookmarkView;

        public UserViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);
            mListener = listener;
            headline = (TextView) itemView.findViewById(R.id.headline);
            date = (TextView) itemView.findViewById(R.id.date);
            summary = (TextView) itemView.findViewById(R.id.summary);
            photoView = (ImageView) itemView.findViewById(R.id.media);
            shareView = (ImageView) itemView.findViewById(R.id.share);
            moreButton = (ImageView) itemView.findViewById(R.id.button_more);
            bookmarkView = (ShineButton) itemView.findViewById(R.id.bookmark);
            Picasso.with(photoView.getContext()).load(R.drawable.placeholder).into(photoView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.clickOnView(v, getLayoutPosition());
        }

        public interface MyViewHolderClick {
            void clickOnView(View v, int position);
        }

        public void bind(Movie movie, String color) {
            shareView.setColorFilter(shareView.getContext().getResources().getColor(R.color.navdrawer_icon_tint));
            headline.setText(String.valueOf(movie.getHeadline()));
            summary.setText(movie.getSummary_short());
            date.setText(movie.getPublication_date());

            if (movie.getPicUrl()!= "") {
                Picasso.with(photoView.getContext()).load(movie.getPicUrl()).placeholder(R.drawable.placeholder).centerCrop().fit()
                        .into(photoView);
            }

            if (movie.getBookmark()) {
//                Toast.makeText(activity.getApplicationContext(), "Yes, the profile is clickable", Toast.LENGTH_SHORT).show();
                bookmarkView.setChecked(true);
                bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
//                bookmarkView.setColorFilter(shareView.getContext().getResources().getColor(R.color.navdrawer_icon_tint));
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

    public nyTimesSwipeRecycleViewAdapter(final Activity activity, List<Movie> movieList) {
        this.activity = activity;
        this.movieList = movieList;
        this.bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
        mQueue = CustomVolleyRequestQueue.getInstance(activity).getRequestQueue();
        User user = PrefUtils.getCurrentUser(activity.getApplicationContext());
        favor = new nyTimesFavoritePreference();

        if (favor.loadFavorites(activity.getApplicationContext()) == null){
            CustomJSONArrayRequest jsonRequest = new CustomJSONArrayRequest(Config.HOST_NAME + "my_nyTimes/"+user.id, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    JSONArray contents = ((JSONArray) response);
                    try {
                        for (int i = 0; i < contents.length(); i++) {
                            JSONObject movieObj = contents.getJSONObject(i);
                            favor.addFavorite(activity.getApplicationContext(), movieObj.getString("headline"));
                        }
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
            mQueue.add(jsonRequest);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return movieList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private void createShareIntent(Movie movie) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, movie.getLink());
        // Launch sharing dialog for image
        activity.startActivity(Intent.createChooser(shareIntent, "Share Review"));
    }

    public void setupFacebookShareIntent(Movie movie) {
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
            View convertView = LayoutInflater.from(context).inflate(R.layout.list_item_review, viewGroup, false);

            TextView summary = (TextView) convertView.findViewById(R.id.summary);
            TextView headline = (TextView) convertView.findViewById(R.id.headline);
            summary.setTextIsSelectable(true);
            headline.setTextIsSelectable(true);
            UserViewHolder viewHolder = new UserViewHolder(convertView, new UserViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                }
            });
            return viewHolder;
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(context).inflate(R.layout.loading_item, viewGroup, false);
            return new ProgressViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof UserViewHolder) {
            Log.d("0322", "onBindViewHolder: " + position);
            UserViewHolder myHolder = (UserViewHolder) viewHolder;
            String headline;
            final Movie movie = movieList.get(position);
            final ShineButton bookmarkView = (ShineButton) ((UserViewHolder) viewHolder).bookmarkView.findViewById(R.id.bookmark);
            String color = bgColors[position % bgColors.length];
            ShineButton shareView = (ShineButton) ((UserViewHolder) viewHolder).shareView.findViewById(R.id.share);
            ShineButton moreView = (ShineButton) ((UserViewHolder) viewHolder).moreButton.findViewById(R.id.button_more);
            ImageView photoView = (ImageView) ((UserViewHolder) viewHolder).photoView.findViewById(R.id.media);
            moreView.init(activity);
            bookmarkView.init(activity);
            shareView.init(activity);
            ArrayList list = favor.loadFavorites(activity.getApplicationContext());

            /*for (int i=0; i<list.size(); i++) {
                favor.removeFavorite(activity.getApplicationContext(), (String) list.get(i));
            }*/

            bookmarkView.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, boolean checked) {
//                    Snackbar.make(view, "Bookmark "+checked+" !!!", Snackbar.LENGTH_LONG).show();
                    if (checked && !movie.getBookmark()) {
                        bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                        User user = PrefUtils.getCurrentUser(activity.getApplicationContext());
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
                        User user = PrefUtils.getCurrentUser(activity.getApplicationContext());
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
            });

            shareView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createShareIntent(movie);
//                            setupFacebookShareIntent(movie);
                }
            });

            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "nyTimes?url=" + movie.getLink(), new JSONObject(), new Response.Listener<JSONObject>() {
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

                                head = movie.getHeadline();
                                Movie movie = new Movie(head, description, story, url, imageUrl, editor ,date);
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

            myHolder.bind(movie, color);
            moreView.setOnClickListener(listener);
            photoView.setOnClickListener(listener);
        } else {
            ProgressViewHolder progressViewHolder = (ProgressViewHolder) viewHolder;
            progressViewHolder.progressBar.setIndeterminate(true);
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
    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }
}
