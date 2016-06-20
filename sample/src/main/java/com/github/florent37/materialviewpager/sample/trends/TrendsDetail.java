package com.github.florent37.materialviewpager.sample.trends;

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
import android.support.design.widget.Snackbar;
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
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.Transition;
import com.github.aakira.expandablelayout.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.github.aakira.expandablelayout.Utils;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.TrendsGalleryRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.imdb.SlideActivity;
import com.github.florent37.materialviewpager.sample.model.TrendsObject;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

/**
 * Created by aaron on 2016/6/18.
 */
public class TrendsDetail extends YouTubeBaseActivity implements AppCompatCallback,
        KenBurnsView.TransitionListener, AdapterView.OnItemClickListener {

    public static String TRENDS_OBJECT = "TRENDS_OBJECT";
    private String YOUTUBE_API_KEY = Config.YOUTUBE_API_KEY;
    private String TAG_RECORDS = "records";
    private String VIDEO_KEY = Config.VIDEO_KEY;
    private YouTubePlayer youtubePlayer;
    private FloatingActionButton fab;
    private YouTubePlayerView youtube_view;
    private int mTransitionsCount = 0;
    private static int TRANSITIONS_TO_SWITCH = 1;
    private List<TrendsObject.GalleryItem> list = null;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private KenBurnsView backgroundImageView, backgroundImageView2, backgroundImageView3, backgroundImageView4, backgroundImageView5;
    private TextView description, plot, genre, runtime, country, moreButton, allButton, picNum, year, studio, title;
    private RecyclerView myRecyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ImageView thumbnailView;
    private AppCompatDelegate delegate;
    private ExpandableRelativeLayout expandableLayout;
    private ViewFlipper mViewSwitcher;
    private View buttonLayout;
    private TrendsObject trendsObject = null;
    private ArrayList<TrendsObject> trendsColection = null;
    private TrendsGalleryRecycleViewAdapter trendsGalleryAdapter;
    MaterialDialog.Builder builder;
    MaterialDialog dialog;
    ShareActionProvider shareActionProvider;
    private PreviewLineChartView positionPreView;
    private LineChartData ratingData, positionData;
    private LineChartData ratingPreviewData, positionPreviewData;

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
        Intent intent = new Intent(view.getContext(), TrendsSlideActivity.class);
        intent.putExtra(TrendsDetail.TRENDS_OBJECT, trendsObject);
        intent.putExtra(SlideActivity.PIC_POSITION, position);
        startActivityForVersion(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        delegate = AppCompatDelegate.create(this, this);
        delegate.installViewFactory();
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.trends_detail);
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
                actionBar.setDisplayUseLogoEnabled(true);
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
        year = (TextView) findViewById(R.id.year);
        genre = (TextView) findViewById(R.id.genre);
        country = (TextView) findViewById(R.id.country);
        title = (TextView) findViewById(R.id.title);
        studio = (TextView) findViewById(R.id.studio);
        runtime = (TextView) findViewById(R.id.runtime);
        thumbnailView = (ImageView) findViewById(R.id.thumbnail);
        moreButton = (TextView) findViewById(R.id.button_more);
        allButton = (TextView) findViewById(R.id.button_all);
        mViewSwitcher = (ViewFlipper) findViewById(R.id.viewSwitcher);
        picNum = (TextView) findViewById(R.id.picNum);

        trendsObject = (TrendsObject) getIntent().getSerializableExtra(TRENDS_OBJECT);

//        collapsingToolbar.setTitle(trendsObject.getTitle());
        title.setText(trendsObject.getTitle());
        collapsingToolbar.setContentScrimColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setStatusBarScrimColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        description.setText(trendsObject.getMainInfo());
        plot.setText(trendsObject.getStory());

        JsonArray dataInfo = new JsonParser().parse(trendsObject.getData()).getAsJsonArray();
        JsonElement jsonElement = null;
        TrendsObject.DataItem dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(4);
        } else {
            jsonElement = dataInfo.get(3);
        }

        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        runtime.setText(dataItem.getData());

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(2);
        } else {
            jsonElement = dataInfo.get(1);
        }
        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        country.setText(dataItem.getData());
        countryFlag(dataItem.getData().split(":")[1]);

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(1);
        } else {
            jsonElement = dataInfo.get(0);
        }
        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        year.setText(dataItem.getData());

        if (dataInfo.size() == 5) {
            jsonElement = dataInfo.get(3);
        } else {
            jsonElement = dataInfo.get(2);
        }
        dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
        studio.setText(dataItem.getData());

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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //---------- Expandable Layout ---------//

        if (trendsObject.getTrailerUrl().equals("N/A")) {
            youtube_view = (YouTubePlayerView) findViewById(R.id.youtube_trailer);
            youtube_view.setVisibility(View.GONE);
        } else {
            VIDEO_KEY = trendsObject.getTrailerUrl().split("[?]")[1].split("[=]")[1];
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
        trendsGalleryAdapter = new TrendsGalleryRecycleViewAdapter(trendsObject, false);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myRecyclerView.setLayoutManager(linearLayoutManager);
        //------- Gallery RecyclerView -------//

        //------- deserialize Gallery JSON object -------//
        JsonArray galleryInfo = new JsonParser().parse(trendsObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<TrendsObject.GalleryItem>();
        for (int i = 0; i < galleryInfo.size(); i++) {
            JsonElement str = galleryInfo.get(i);
            TrendsObject.GalleryItem obj = gson.fromJson(str, TrendsObject.GalleryItem.class);
            list.add(obj);
            trendsGalleryAdapter.addItem(i,obj);
        }
        //------- deserialize Gallery JSON object -------//

        picNum.setText(String.valueOf(list.size()));
        if (galleryInfo.size() > 1) {
            myRecyclerView.setAdapter(trendsGalleryAdapter);
            trendsGalleryAdapter.setOnItemClickListener(this);
        } else {
            View gallery = (LinearLayout) findViewById(R.id.gallery_container);
            ViewGroup parent = (ViewGroup) gallery.getParent();
            parent.removeView(gallery);
        }
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

        Picasso.with(this).load(trendsObject.getPosterUrl()).centerCrop().fit().into(backgroundImageView);

        Random random = new Random();

        if (list.size() > 1) {
            Log.d("0501", list.get(random.nextInt(list.size())).getUrl());
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView2);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView3);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView4);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView5);
        }

        builder = new MaterialDialog.Builder(TrendsDetail.this)
                .iconRes(R.drawable.ic_launcher)
                .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                .title("Rottentomatoes")
                .titleColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .contentColor(Color.BLACK)
                .content("Redirect to Rottenntomatoes.com ?")
                .positiveText("Agree")
                .negativeText("Disagree")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        startActivityForVersion(new Intent("android.intent.action.VIEW",
                                Uri.parse("http://www.rottentomatoes.com/search/?search=" + trendsObject.getTitle())));
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
                Intent intent = new Intent(v.getContext(), TrendsAlbumActivity.class);
                intent.putExtra(TrendsDetail.TRENDS_OBJECT, trendsObject);
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
        return;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trendsObject.getDetailUrl());
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

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            TrendsDetail.this).toBundle());
        }
        else {
            startActivity(intent);
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
            case "日本":
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
            case "アメリカ":
            case "USA":
                Picasso.with(thumbnailView.getContext()).load(R.drawable.usa).into(thumbnailView);
                break;
        }
    }
}
