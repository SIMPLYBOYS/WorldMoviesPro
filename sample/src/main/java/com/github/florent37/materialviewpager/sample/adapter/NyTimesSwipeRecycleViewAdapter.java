package com.github.florent37.materialviewpager.sample.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
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
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.framework.WebViewActivity;
import com.github.florent37.materialviewpager.sample.nytimes.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

import im.delight.android.webview.AdvancedWebView;

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
    private AdvancedWebView webview;
    ShareActionProvider shareActionProvider;

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyViewHolderClick mListener;
        private TextView headline;
        private TextView date;
        private TextView summary;
        private TextView moreButton;
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
            moreButton = (TextView) itemView.findViewById(R.id.button_more);
            bookmarkView = (ImageView) itemView.findViewById(R.id.bookmark);
            moreButton = (TextView) itemView.findViewById(R.id.button_more);
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
        shareActionProvider = new ShareActionProvider(activity);
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
        Log.d("0322", "onCreateViewHolder");
        final Context context = viewGroup.getContext();
        if (viewType == VIEW_TYPE_ITEM) {
            View convertView = LayoutInflater.from(context).inflate(R.layout.list_item_review, viewGroup, false);

            UserViewHolder viewHolder = new UserViewHolder(convertView, new UserViewHolder.MyViewHolderClick() {
                @Override
                public void clickOnView(View v, int position) {
                    final Movie movie = movieList.get(position);
//                    Snackbar.make(v, movie.getHeadline(), Snackbar.LENGTH_LONG).show();
                    View shareView = v.findViewById(R.id.share);
                    final View bookmarkView = v.findViewById(R.id.bookmark);
                    View moreView = v.findViewById(R.id.button_more);

                    moreView.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(v.getContext(), WebViewActivity.class);
                            intent.putExtra("movie", movie);
                            startActivityForVersion(intent);
                        }
                    });
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
                            //TODO bookmark info for the user's acccount
                        }
                    });
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
            Movie movie = movieList.get(position);
            String color = bgColors[position % bgColors.length];
            myHolder.bind(movie, color);
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
