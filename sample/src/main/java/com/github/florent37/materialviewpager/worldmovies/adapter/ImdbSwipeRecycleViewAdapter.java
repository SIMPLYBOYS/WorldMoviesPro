package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.CustomLightBoxActivity;
import com.github.florent37.materialviewpager.worldmovies.imdb.MovieDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import im.delight.android.webview.AdvancedWebView;

/**
 * Created by aaron on 2016/6/13.
 */
public class ImdbSwipeRecycleViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static Activity activity;
    List<ImdbObject> movieList;
    private String[] bgColors;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public static int visibleThreshold = 2;
    private static String VIDEO_KEY;
    public static int lastVisibleItem, totalItemCount;
    public static boolean loading;
    public static boolean swipe;
    private AdvancedWebView webview;
    ShareActionProvider shareActionProvider;
    private ProgressBar mProgressBar;

    public static class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyViewHolderClick mListener;
        public View mView;
        private TextView titleView;
        private TextView topView;
        private TextView yearView;
        private TextView desciptionView;
        private TextView rattingView;
        private TextView votesView;
        private TextView deltaView;
        private ImageView posterView, arrowView, starView;

        public ContentViewHolder(View itemView, MyViewHolderClick listener) {
            super(itemView);
            mListener = listener;
            mView = itemView;
            mView.setOnClickListener(this);
            titleView = (TextView) itemView.findViewById(R.id.title);
            topView = (TextView) itemView.findViewById(R.id.top);
            rattingView = (TextView) itemView.findViewById(R.id.rating);
            arrowView = (ImageView) itemView.findViewById(R.id.arrow);
            yearView = (TextView)itemView.findViewById(R.id.year);
            votesView = (TextView) itemView.findViewById(R.id.votes);
            deltaView = (TextView) itemView.findViewById(R.id.delta);
            desciptionView = (TextView) itemView.findViewById(R.id.description);
            posterView = (ImageView) itemView.findViewById(R.id.poster);
            starView = (ImageView)itemView.findViewById(R.id.thumbnail);
            int color = Color.parseColor("#F3CE13"); //The color u want
            starView.setColorFilter(color);
            Picasso.with(posterView.getContext()).load(R.drawable.parisguidetower).into(posterView);
        }

        @Override
        public void onClick(View v) {
            mListener.clickOnView(v, getLayoutPosition());
        }

        public interface MyViewHolderClick {
            void clickOnView(View v, int position);
        }

        public void bind(final ImdbObject imdbObject, String color, final ProgressBar mProgressBar) {
            String title = imdbObject.getTitle();
            Gson gson = new Gson();
            ImdbObject.RatingItem ratingItem = null;
            JsonElement jsonElement = null;

            if (imdbObject.getRating().compareTo("")!=0) {
                JsonObject ratingInfo = new JsonParser().parse(imdbObject.getRating()).getAsJsonObject();
                jsonElement = ratingInfo.getAsJsonObject();
                ratingItem = gson.fromJson(jsonElement, ImdbObject.RatingItem.class);
                rattingView.setText(ratingItem.getScore());
                votesView.setText(imdbObject.getVotes());

                if (ratingItem.getScore() != null) {
                    rattingView.setText(ratingItem.getScore());
                    starView.setVisibility(View.VISIBLE);
                }
            }

            if (titleView == null)
                return;
            titleView.setText(title);

            if (imdbObject.getTop() != "0")
                topView.setText(imdbObject.getTop());

            yearView.setText(imdbObject.getYear());
            int delta = Math.abs(imdbObject.getDelta());
            Log.d("0601: ", String.valueOf(delta));

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

            desciptionView.setText(imdbObject.getDescription());
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
                    Intent lightBoxIntent = new Intent(v.getContext(), CustomLightBoxActivity.class);
                    VIDEO_KEY = imdbObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
                    lightBoxIntent.putExtra(CustomLightBoxActivity.KEY_VIDEO_ID, VIDEO_KEY);
                    activity.startActivity(lightBoxIntent);
                }
            });
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    public ImdbSwipeRecycleViewAdapter(Activity activity, List<ImdbObject> movieList) {
        this.activity = activity;
        this.movieList = movieList;
        this.bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
        shareActionProvider = new ShareActionProvider(activity);
    }

    @Override
    public int getItemViewType(int position) {
        return movieList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private void createShareIntent(ImdbObject movie) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, movie.getDetailUrl());
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

        Log.d("0322", "onCreateViewHolder");
        final Context context = viewGroup.getContext();
        if (viewType == VIEW_TYPE_ITEM) {
            SharedPreferences settings = context.getSharedPreferences("settings", 0);
            Boolean cardType = settings.getBoolean("miniCard", false);
            View convertView = null;

            if (cardType)
                convertView = LayoutInflater.from(context).inflate(R.layout.imdb_list_item_card_small, viewGroup, false);
            else
                convertView = LayoutInflater.from(context).inflate(R.layout.imdb_list_item_card_big, viewGroup, false);

            mProgressBar = (ProgressBar)convertView.findViewById(android.R.id.progress);

            ContentViewHolder viewHolder = new ContentViewHolder(convertView, new ContentViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                    final ImdbObject movie = movieList.get(position);
                    Intent intent = new Intent(v.getContext(), MovieDetailActivity.class);
                    movie.setType("imdb");
                    intent.putExtra(MovieDetailActivity.IMDB_OBJECT, movie);
                    ActivityCompat.startActivity(activity, intent, null);
//                    Snackbar.make(v, movie.getHeadline(), Snackbar.LENGTH_LONG).show();
                    /*View shareView = v.findViewById(R.id.share);
                    final View bookmarkView = v.findViewById(R.id.bookmark);
                    shareView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            createShareIntent(movie);
//                            setupFacebookShareIntent(movie);
                        }
                    });
                    bookmarkView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                            Snackbar.make(v, "Bookmark !!", Snackbar.LENGTH_LONG).show();
                        }
                    });*/
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
        if (viewHolder instanceof ContentViewHolder) {
            Log.d("0322", "onBindViewHolder: " + position);
            ContentViewHolder myHolder = (ContentViewHolder) viewHolder;
            ImdbObject movie = movieList.get(position);
            String color = bgColors[position % bgColors.length];
            mProgressBar.setVisibility(View.VISIBLE);
            myHolder.bind(movie, color, mProgressBar);
        } else {
            ProgressViewHolder progressViewHolder = (ProgressViewHolder) viewHolder;
            progressViewHolder.progressBar.setIndeterminate(true);
        }
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
