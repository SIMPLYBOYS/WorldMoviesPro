package com.github.florent37.materialviewpager.sample.imdb;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.Transition;
import com.github.aakira.expandablelayout.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.github.aakira.expandablelayout.Utils;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.ImdbGalleryRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.ViewportChangeListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

/**
 * Created by aaron on 2016/4/3.
 */

public class MovieDetail extends YouTubeBaseActivity implements AppCompatCallback,
        KenBurnsView.TransitionListener, AdapterView.OnItemClickListener,
        Response.Listener, Response.ErrorListener {

    public static final String IMDB_OBJECT = "IMDB_OBJECT";
    private static final String YOUTUBE_API_KEY = "AIzaSyC1rMU-mkhoyTvBIdTnYU0dss0tU9vtK48";
    private static final String TAG_RECORDS = "records";
    private static String VIDEO_KEY = "mzhX2PD6Srw";
    private YouTubePlayer youtubePlayer;
    private FloatingActionButton fab;
    private YouTubePlayerView youtube_view;
    private int mTransitionsCount = 0;
    private static final int TRANSITIONS_TO_SWITCH = 1;
    private List<ImdbObject.GalleryItem> list = null;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private KenBurnsView backgroundImageView, backgroundImageView2, backgroundImageView3, backgroundImageView4, backgroundImageView5;
    private TextView description, plot, genre, runtime, metascrore, country, moreButton, allButton, picNum;
    private RecyclerView myRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ImageView thumbnailView;
    private AppCompatDelegate delegate;
    private ExpandableRelativeLayout expandableLayout;
    private ViewFlipper mViewSwitcher;
    private View buttonLayout;
    private ImdbObject imdbObject = null;
    private ArrayList<ImdbObject> imdbColection = null;
    private ImdbGalleryRecycleViewAdapter imdbGalleryAdapter;
    MaterialDialog.Builder builder;
    MaterialDialog dialog;
    ShareActionProvider shareActionProvider;
    private LineChartView ratingChart, positionChart;
    private PreviewLineChartView ratingPreview, positionPreView;
    private LineChartData ratingData, positionData;
    private LineChartData ratingPreviewData, positionPreviewData;
    private RequestQueue mQueue;
    public static final String REQUEST_TAG = "recordsRequest";
    private String HOST_NAME = "http://ec2-52-192-246-11.ap-northeast-1.compute.amazonaws.com/";

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
    }

    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "Clicked: " + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(view.getContext(), SlideActivity.class);
        intent.putExtra(MovieDetail.IMDB_OBJECT, imdbObject);
        intent.putExtra(SlideActivity.PIC_POSITION, position);
        startActivityForVersion(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate = AppCompatDelegate.create(this, this);
        //the installViewFactory method replaces the default widgets
        //with the AppCompat-tinted versions
        delegate.installViewFactory();
        //we need to call the onCreate() of the AppCompatDelegate
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.movie_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);

        if (toolbar != null) {
            delegate.setSupportActionBar(toolbar);

            final ActionBar actionBar = delegate.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(upArrow);
            }
        }

        fab = (FloatingActionButton) findViewById(R.id.floating_button);

        //---------- Ken Burn Animation-----------//
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        backgroundImageView = (KenBurnsView) findViewById(R.id.backgroundImageView);
        backgroundImageView2 = (KenBurnsView) findViewById(R.id.backgroundImageView2);
        backgroundImageView3 = (KenBurnsView) findViewById(R.id.backgroundImageView3);
        backgroundImageView4 = (KenBurnsView) findViewById(R.id.backgroundImageView4);
        backgroundImageView5 = (KenBurnsView) findViewById(R.id.backgroundImageView5);

        backgroundImageView.setTransitionListener(this);
        backgroundImageView2.setTransitionListener(this);
        backgroundImageView3.setTransitionListener(this);
        backgroundImageView4.setTransitionListener(this);
        backgroundImageView5.setTransitionListener(this);
        //---------- Ken Burn Animation-----------//

        description = (TextView) findViewById(R.id.description);
        plot = (TextView) findViewById(R.id.plot);
        genre = (TextView) findViewById(R.id.genre);
        country = (TextView) findViewById(R.id.country);
        runtime = (TextView) findViewById(R.id.runtime);
        metascrore = (TextView) findViewById(R.id.metascrore);
        thumbnailView = (ImageView) findViewById(R.id.thumbnail);
        moreButton = (TextView) findViewById(R.id.button_more);
        allButton = (TextView) findViewById(R.id.button_all);
        mViewSwitcher = (ViewFlipper) findViewById(R.id.viewSwitcher);
        picNum = (TextView) findViewById(R.id.picNum);

        imdbObject = (ImdbObject) getIntent().getSerializableExtra(IMDB_OBJECT);
//        imdbColection = (ArrayList<ImdbObject>) getIntent().getBundleExtra(IMDB_COLLECTION).getSerializable(IMDB_COLLECTION);
        collapsingToolbar.setTitle(imdbObject.getTitle());
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.expandedappbar);
//        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.collapseappbar);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setContentScrimColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setStatusBarScrimColor(getResources().getColor(android.R.color.transparent));

        description.setText(imdbObject.getSummery());
        plot.setText(imdbObject.getPlot());
        genre.setText("Genre : " + imdbObject.getGenre());
        runtime.setText("RunTime : " + imdbObject.getRunTime());
        country.setText("Country : " + imdbObject.getCountry());
        countryFlag(imdbObject.getCountry());

        //---------- Expandable Layout ---------//
        expandableLayout = (ExpandableRelativeLayout) findViewById(R.id.expandableLayout);
        buttonLayout = findViewById(R.id.expandableButton);

        expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(buttonLayout, 0f, 180f).start();
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(buttonLayout, 180f, 0f).start();
            }
        });

        expandableLayout.setInterpolator(new FastOutLinearInInterpolator());

        buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClickButton(expandableLayout);
            }
        });

        //---------- Expandable Layout ---------//

        Log.d("0427", String.valueOf(imdbObject.getMetaScore().isEmpty()));

        if (!imdbObject.getMetaScore().equals("null"))
            metascrore.setText("MetaScore : " + imdbObject.getMetaScore());

        if (imdbObject.getTrailerUrl().equals("N/A")) {
            youtube_view = (YouTubePlayerView) findViewById(R.id.youtube_trailer);
            youtube_view.setVisibility(View.GONE);
        } else {
            VIDEO_KEY = imdbObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
            Log.d("0407", VIDEO_KEY);
            youtube_view = (YouTubePlayerView) findViewById(R.id.youtube_trailer);
            youtube_view.initialize(YOUTUBE_API_KEY, new YoutubeOnInitializedListener());
        }

        //------- Gallery RecyclerView -------//
        myRecyclerView = (RecyclerView)findViewById(R.id.myrecyclerview);
        myRecyclerView.getItemAnimator().setAddDuration(1000);
        myRecyclerView.getItemAnimator().setChangeDuration(1000);
        myRecyclerView.getItemAnimator().setMoveDuration(1000);
        myRecyclerView.getItemAnimator().setRemoveDuration(1000);
        imdbGalleryAdapter = new ImdbGalleryRecycleViewAdapter(imdbObject, false);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myRecyclerView.setLayoutManager(linearLayoutManager);
        //------- Gallery RecyclerView -------//

        //------- deserialize Gallery JSON object -------//
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonParser().parse(imdbObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<ImdbObject.GalleryItem>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement str = jsonArray.get(i);
            ImdbObject.GalleryItem obj = gson.fromJson(str, ImdbObject.GalleryItem.class);
            list.add(obj);
            imdbGalleryAdapter.addItem(i,obj);
        }
        //------- deserialize Gallery JSON object -------//

        picNum.setText(String.valueOf(list.size()));
        myRecyclerView.setAdapter(imdbGalleryAdapter);
        imdbGalleryAdapter.setOnItemClickListener(this);

        //----------- record chart -----------//
        requestDataRefresh(imdbObject.getTitle());
        //----------- record chart -----------//
    }

    private void onClickButton(final ExpandableLayout expandableLayout) {
        expandableLayout.toggle();
    }

    private class YoutubeOnInitializedListener implements YouTubePlayer.OnInitializedListener {
        @Override
        public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {
            Toast.makeText(getApplicationContext(), "Youtube onInitializationFailure !", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
            if (!wasRestored) {
                youtubePlayer = player;
                youtubePlayer.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI);
//                youtubePlayer.loadVideo(VIDEO_KEY);
                youtubePlayer.cueVideo(VIDEO_KEY);
            }
        }
    }

    private void setPalette() {
        Bitmap bitmap = ((BitmapDrawable) backgroundImageView.getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int primaryDark = getResources().getColor(R.color.primary_dark_material_dark);
                int primary = getResources().getColor(R.color.primary_material_light);
                collapsingToolbar.setContentScrimColor(palette.getMutedColor(primary));
                collapsingToolbar.setStatusBarScrimColor(palette.getDarkVibrantColor(primaryDark));
                fab.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkVibrantColor(primaryDark)));
                fab.setRippleColor(palette.getMutedColor(primary));
            }
        });
    }

    @Override
    public void onTransitionStart(Transition transition) {
    }

    @Override
    public void onTransitionEnd(Transition transition) {
        mTransitionsCount++;
        if (mTransitionsCount == TRANSITIONS_TO_SWITCH) {
            /*Random random = new Random();
            transitionImageUrl(backgroundImageView, list.get(random.nextInt(list.size())).getUrl(), 250);*/
            mViewSwitcher.showNext();
            setPalette();
            mTransitionsCount = 0;
        }
    }

    public android.animation.ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        android.animation.ObjectAnimator animator = android.animation.ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return animator;
    }

    @Override
    public void onResume () {
        super.onResume();

        if (imdbObject.getSlate().equals("N/A"))
            Picasso.with(this).load(imdbObject.getPosterUrl()).centerCrop().fit().into(backgroundImageView);
        else
            Picasso.with(this).load(imdbObject.getSlate()).centerCrop().fit().into(backgroundImageView);

        Random random = new Random();
        Log.d("0501", list.get(random.nextInt(list.size())).getUrl());
        Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView2);
        Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView3);
        Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView4);
        Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView5);

        Log.d("0406", "slate: " + imdbObject.getSlate() + (imdbObject.getSlate().equals("N/A")));
        builder = new MaterialDialog.Builder(MovieDetail.this)
                .iconRes(R.drawable.ic_launcher)
                .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                .title("Rottentomatoes")
                .titleColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .contentColor(Color.BLACK)
                .content("Redirect to Rottenntomatoes.com ?")
                .positiveText("Agree")
                .negativeText("Disagree").onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        startActivityForVersion(new Intent("android.intent.action.VIEW",
                                Uri.parse("http://www.rottentomatoes.com/search/?search=" + imdbObject.getTitle())));
                    }
                });

        dialog = builder.build();

        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

            }
        });

        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), AlbumActivity.class);
                intent.putExtra(MovieDetail.IMDB_OBJECT, imdbObject);
                startActivityForVersion(intent);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected  void onRestart() {
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);

        for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            }
        }

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = new ShareActionProvider(this);
        /*shareActionProvider =  (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);*/
        MenuItemCompat.setActionProvider(shareItem, shareActionProvider);
        shareActionProvider.setShareIntent(createShareIntent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                onShareAction();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;

        mQueue = CustomVolleyRequestQueue.getInstance(MovieDetail.this)
                .getRequestQueue();

        CustomJSONObjectRequest jsonRequest_q = null;

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }

            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/imdb_records?title=" + Query + "&ascending=1", new JSONObject(), this, this);
            mQueue.add(jsonRequest_q);
            return;
        }

        jsonRequest.setTag(REQUEST_TAG);

        mQueue.add(jsonRequest); //trigger volley request
    }

    @Override
    public void onResponse(Object response) {
        try {
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");

            JSONObject c = contents.getJSONObject(0);
            Log.d("0514", String.valueOf(c));
            JSONArray records = c.getJSONArray(TAG_RECORDS);

            ratingChart = (LineChartView) findViewById(R.id.ratingChart);
            positionChart = (LineChartView) findViewById(R.id.postitionChart);
            ratingPreview = (PreviewLineChartView) findViewById(R.id.rating_preview);
            positionPreView = (PreviewLineChartView) findViewById(R.id.position_preview);
            generateData(records);
            ratingChart.setLineChartData(ratingData);
            positionChart.setLineChartData(positionData);

            // Disable zoom/scroll for previewed chart, visible chart ranges depends on preview chart viewport so
            // zoom/scroll is unnecessary.
            ratingChart.setZoomEnabled(false);
            ratingChart.setScrollEnabled(false);
            ratingPreview.setLineChartData(ratingPreviewData);
            ratingPreview.setViewportChangeListener(new ViewportListener());
            ratingPreview.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
            previewX(ratingChart, ratingPreview, false);

            positionChart.setZoomEnabled(false);
            positionChart.setScrollEnabled(false);
            positionPreView.setLineChartData(positionPreviewData);
            positionPreView.setViewportChangeListener(new ViewportListener());
            positionPreView.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
            previewX(positionChart, positionPreView, false);

        } catch (JSONException e) {
            Toast.makeText(this, "Remote Server error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        View chart = (LinearLayout) findViewById(R.id.chart_layout);
        ViewGroup parent = (ViewGroup) chart.getParent();
        parent.removeView(chart);
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    public static void transitionImageUrl(final ImageView imageView, final String urlImage, final int fadeDuration) {
        final float alpha = ViewHelper.getAlpha(imageView);

        //fade to alpha=0
        final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 0).setDuration(fadeDuration);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                //change the image when alpha=0
                Picasso.with(imageView.getContext()).load(urlImage)
                        .centerCrop().fit().into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                        //then fade to alpha=1

                        final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 1.0f).setDuration(fadeDuration);
                        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                        fadeIn.start();
                    }
                    @Override
                    public void onError() {
                    }
                });
            }
        });
        fadeOut.start();
    }

    private void onShareAction() {
        shareActionProvider.setShareIntent(createShareIntent());

        /*// Create the share Intent
        String playStoreLink = "https://play.google.com/store/apps/details?id=" + getPackageName();
        String yourShareText = getResources().getString(R.string.library_ion_author) + playStoreLink;
        Intent shareIntent = ShareCompat.IntentBuilder.from(this).setType("text/plain").setText(yourShareText).getIntent();
        // Set the share Intent
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }*/

        return;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.imdb.com/" + imdbObject.getDetailPosterUrl());
        return shareIntent;
    }

    private void previewX(LineChartView chart, PreviewLineChartView previewChart, boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    // Get the uri to a random image in the photo gallery
    private Uri getRandomImageUri() {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media._ID };
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(mediaUri, projection, null, null, null);
            cursor.moveToPosition((int) (Math.random() * cursor.getCount()));
            String id = cursor.getString(0);
            Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            return uri;
        }
        catch (Exception e) {
            return null;
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    /**
     * Viewport listener for preview chart(lower one). in {@link #onViewportChanged(Viewport)} method change
     * viewport of upper chart.
     */
    private class ViewportListener implements ViewportChangeListener {

        @Override
        public void onViewportChanged(Viewport newViewport) {
            // don't use animation, it is unnecessary when using preview chart.
            ratingChart.setCurrentViewport(newViewport);
            positionChart.setCurrentViewport(newViewport);
        }

    }

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            MovieDetail.this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    private void generateData(JSONArray records) {
        try {

            int numValues = 50; //TODO => records.length()

            Line line;
            List<PointValue> values;
            List<Line> lines = new ArrayList<Line>();

            //---------- Position line ----------//
            values = new ArrayList<PointValue>();
            for (int i = 0; i < records.length(); ++i) {
                // Some random height values, add +200 to make line a little more natural
                JSONObject jsonItem = (JSONObject) records.getJSONObject(i);

                int position = Integer.parseInt(jsonItem.getString("position"));
                Log.d("0511", new PointValue(i, position).toString());
                values.add(new PointValue(i, position));
            }

            line = new Line(values);
            line.setColor(Color.BLACK);
            line.setShape(ValueShape.CIRCLE);
            line.setHasLabelsOnlyForSelected(true);
            line.setHasPoints(true);
            line.setFilled(true);
            line.setStrokeWidth(1);
            lines.add(line);

            positionData = new LineChartData(lines);
            //---------- Position line ----------//

            //---------- Rating line -----------//
            values = new ArrayList<PointValue>();
            for (int i = 0; i < records.length(); ++i) {
                JSONObject jsonItem = records.getJSONObject(i);
                float rating = Float.parseFloat(jsonItem.getString("rating"));
                Log.d("0510", new PointValue(i, rating).toString());
                values.add(new PointValue(i, rating));
            }

            line = new Line(values);
            lines = new ArrayList<Line>();
            line.setShape(ValueShape.SQUARE);
            line.setColor(ChartUtils.COLOR_RED);
            line.setHasLabelsOnlyForSelected(true);
            line.setFormatter(new SimpleLineChartValueFormatter(1));
            line.setCubic(false);
            line.setFilled(true);
            line.setHasPoints(true);
            line.setStrokeWidth(1);
            lines.add(line);

            // Data and axes
            ratingData = new LineChartData(lines);
            //---------- Rating line -----------//

            List<AxisValue> axisXValues = new ArrayList<AxisValue>();

            // Distance axis(bottom X) with formatter that will ad [km] to values, remember to modify max label charts
            // value.
            for (int i = 0; i <records.length(); ++i) {
                JSONObject jsonItem = records.getJSONObject(i);
                String year = jsonItem.getString("year");
                String month = jsonItem.getString("month");
                String date = jsonItem.getString("date");
                axisXValues.add(new AxisValue(i).setLabel(year+"-"+month+"-"+date));
            }

            Axis distanceAxis = new Axis(axisXValues);
            distanceAxis.setName("Year");
            distanceAxis.setTextColor(ChartUtils.COLOR_ORANGE);
            distanceAxis.setMaxLabelChars(8);
            distanceAxis.setHasLines(true);
            distanceAxis.setHasTiltedLabels(true);

            ratingData.setAxisXBottom(distanceAxis);
            positionData.setAxisXBottom(distanceAxis);

            List<AxisValue> axisValues = new ArrayList<AxisValue>();
            for (int i = 1; i <= 10; i += 1) {
                axisValues.add(new AxisValue(i).setLabel(String.valueOf(i)));
            }

            ratingData.setAxisYLeft(new Axis(axisValues).setName("Rating").setMaxLabelChars(3).setTextColor(ChartUtils.COLOR_RED)
                    .setHasLines(true).setInside(false));

            axisValues = new ArrayList<AxisValue>();
            for (int j = 1; j <= 250; j += 1) {
                axisValues.add(new AxisValue(j).setLabel(String.valueOf(j)));
            }
            positionData.setAxisYLeft(new Axis(axisValues).setName("Position").setMaxLabelChars(3).setTextColor(Color.BLACK)
                    .setHasLines(true).setInside(false));

            ratingPreviewData = new LineChartData(ratingData);
            ratingPreviewData.setAxisXBottom(distanceAxis);
            positionPreviewData = new LineChartData(positionData);
            positionPreviewData.setAxisXBottom(distanceAxis);

        } catch (JSONException e) {
            Toast.makeText(this, "Remote Server data format error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void countryFlag ( String location) {

        switch (location) {
            case "France":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.fr).into(thumbnailView);
                break;
            case "Germany":
            case "West Germany":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.gm).into(thumbnailView);
                break;
            case "Japan":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.japan).into(thumbnailView);
                break;
            case "Brazil":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.brazil).into(thumbnailView);
                break;
            case "Espagne":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                break;
            case "Italy":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.it).into(thumbnailView);
                break;
            case "New Zealand":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.newzealand).into(thumbnailView);
                break;
            case "South Korea":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.korea).into(thumbnailView);
                break;
            case "UK":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.uk).into(thumbnailView);
                break;
            case "Iran":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.iran).into(thumbnailView);
                break;
            case "India":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.india).into(thumbnailView);
                break;
            case "Lebanon":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.lebanon).into(thumbnailView);
                break;
            case "Spain":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.es).into(thumbnailView);
                break;
            case "Sweden":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.sweden).into(thumbnailView);
                break;
            case "Argentina":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.argentina).into(thumbnailView);
                break;
            case "Canada":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.canada).into(thumbnailView);
                break;
            case "Australia":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.australia).into(thumbnailView);
                break;
            case "Ireland":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.ireland).into(thumbnailView);
                break;
            case "Mexico":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.mexico).into(thumbnailView);
                break;
            case "Soviet Union":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.ru).into(thumbnailView);
                break;
            case "Hong Kong":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.hong_kong).into(thumbnailView);
                break;
            case "Denmark":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.denmark).into(thumbnailView);
                break;
            case "USA":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.usa).into(thumbnailView);
                break;
        }
    }
}
