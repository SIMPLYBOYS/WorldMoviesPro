package com.github.florent37.materialviewpager.sample.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
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
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.nytimes.Movie;
import com.github.florent37.materialviewpager.sample.nytimes.nyTimesDetailActivity;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by aaron on 2016/6/11.
 */
public class NyTimesSwipeRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity activity;
    List<Movie> movieList;
    private String[] bgColors;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public static int visibleThreshold = 2;
    public static int lastVisibleItem, totalItemCount;
    public static boolean loading;
    private RequestQueue mQueue;
    CustomJSONObjectRequest jsonRequest_q = null;

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyViewHolderClick mListener;
        private TextView headline;
        private TextView date;
        private TextView summary;
        private ImageView moreButton;
        private ImageView photoView;
        private ImageView shareView;
        private ImageView bookmarkView;

        public UserViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);
            mListener = listener;
            headline = (TextView) itemView.findViewById(R.id.headline);
            date = (TextView) itemView.findViewById(R.id.date);
            summary = (TextView) itemView.findViewById(R.id.summary);
            photoView = (ImageView) itemView.findViewById(R.id.media);
            shareView = (ImageView) itemView.findViewById(R.id.share);
            moreButton = (ImageView) itemView.findViewById(R.id.button_more);
            bookmarkView = (ImageView) itemView.findViewById(R.id.bookmark);
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
            headline.setText(String.valueOf(movie.getHeadline()));
            summary.setText(movie.getSummary_short());
            date.setText(movie.getPublication_date());
            if (movie.getPicUrl()!= "") {
                Picasso.with(photoView.getContext()).load(movie.getPicUrl()).placeholder(R.drawable.placeholder).centerCrop().fit()
                        .into(photoView);
            }
            shareView.setColorFilter(shareView.getContext().getResources().getColor(R.color.navdrawer_icon_tint));
            bookmarkView.setColorFilter(shareView.getContext().getResources().getColor(R.color.navdrawer_icon_tint));

//            headline.setBackgroundColor(Color.parseColor(color));
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    public NyTimesSwipeRecycleViewAdapter(Activity activity, List<Movie> movieList) {
        this.activity = activity;
        this.movieList = movieList;
        this.bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
        mQueue = CustomVolleyRequestQueue.getInstance(activity)
                .getRequestQueue();
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

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            activity).toBundle());
        }
        else {
            activity.startActivity(intent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof UserViewHolder) {
            Log.d("0322", "onBindViewHolder: " + position);
            UserViewHolder myHolder = (UserViewHolder) viewHolder;
            final Movie movie = movieList.get(position);
            final ShineButton bookmarkView = (ShineButton) ((UserViewHolder) viewHolder).bookmarkView.findViewById(R.id.bookmark);
            String color = bgColors[position % bgColors.length];
            ShineButton shareView = (ShineButton) ((UserViewHolder) viewHolder).shareView.findViewById(R.id.share);
            ShineButton moreView = (ShineButton) ((UserViewHolder) viewHolder).moreButton.findViewById(R.id.button_more);
            ImageView photoView = (ImageView) ((UserViewHolder) viewHolder).photoView.findViewById(R.id.media);
            moreView.init(activity);
            bookmarkView.init(activity);
            shareView.init(activity);
            bookmarkView.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(View view, boolean checked) {
                    Snackbar.make(view, "Bookmark "+checked+" !!!", Snackbar.LENGTH_LONG).show();
                    if (checked)
                        bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                    else
                        bookmarkView.setBackgroundResource(R.drawable.ic_turned_in);
                    //TODO bookmark info for the user's acccount
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
                                JSONArray contents = response.getJSONArray("contents");
                                String story,imageUrl, head, description, editor, date;

                                JSONObject reviewObj = contents.getJSONObject(0);
                                story = reviewObj.getString("story");
                                editor = reviewObj.getString("editor");
                                date = reviewObj.getString("date");
                                JSONObject imgObj = reviewObj.getJSONObject("image");

                                if (imgObj.has("src")) {
                                    imageUrl = imgObj.getString("src");
                                    description = imgObj.getString("description");
                                } else {
                                    imageUrl = null;
                                    description = null;
                                }

                                head = movie.getHeadline();
                                Movie foo = new Movie(head, description, story, "", imageUrl, editor ,date);
                                Intent intent = new Intent(activity, nyTimesDetailActivity.class);
                                intent.putExtra("movie", foo);
                                ActivityCompat.startActivity(activity, intent, null);

                                        /*JSONObject movieObj = contents.getJSONObject(0);
                                        String head = movieObj.getString("headline");
                                        String date = movieObj.getString("publication_date");
                                        String summery = movieObj.getString("summary_short");
                                        JSONObject link = null;
                                        JSONObject media = null;
                                        String picUrl = "";
                                        if (!movieObj.isNull("multimedia")) {
                                            media = movieObj.getJSONObject("multimedia");
                                            picUrl = media.getString("src");
                                        }
                                        link = movieObj.getJSONObject("link");
                                        String linkUrl = link.getString("url");
                                        Movie movie = new Movie(head, date, summery, linkUrl, picUrl);*/
                                        /*Intent intent = new Intent(nyTimesActivity.this, WebViewActivity.class);
                                        intent.putExtra("movie", movie);
                                        ActivityCompat.startActivity(nyTimesActivity.this, intent, null);*/

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
                            /*Intent intent = new Intent(v.getContext(), WebViewActivity.class); //TODO nyTimesDetail
                            intent.putExtra("movie", movie);
                            startActivityForVersion(intent);*/
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

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }
}
