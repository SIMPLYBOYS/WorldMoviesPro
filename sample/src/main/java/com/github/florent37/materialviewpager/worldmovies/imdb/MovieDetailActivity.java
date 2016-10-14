package com.github.florent37.materialviewpager.worldmovies.imdb;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.Transition;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.MainActivity;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.ImageCursorAdapter;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbCastTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbChartTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbInfoTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbMusicTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbReviewTabFragment;
import com.github.florent37.materialviewpager.worldmovies.framework.MovieDetail;
import com.github.florent37.materialviewpager.worldmovies.genre.GenreActivity;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
import com.github.florent37.materialviewpager.worldmovies.util.BuildModelUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/7/28.
 */
public class MovieDetailActivity extends AppCompatActivity implements KenBurnsView.TransitionListener, BottomNavigationBar.OnTabSelectedListener,
        Response.Listener, Response.ErrorListener {
    public static String IMDB_OBJECT = "IMDB_OBJECT";
    public final String FILM_NAME = "filmName";
    public final String FILM_DESCRIPTION = "filmDescription";
    public final String FILM_POSTER = "filmPoster";
    protected final int NAV_ITEM_TREND = 0;
    protected final int NAV_ITEM_UPCOMING = 1;
    protected final int NAV_ITEM_IMDB = 2;
    protected final int NAV_ITEM_NYTIMES = 3;
    protected final int NAV_ITEM_GENRE = 4;
    private int mTransitionsCount = 0;
    private String TAG_TOP = "top";
    private String HOST_NAME = Config.HOST_NAME;
    private String type;
    private int TRANSITIONS_TO_SWITCH = 1;
    private List<ImdbObject.GalleryItem> list = null;
    public  String REQUEST_TAG = "titleRequest";
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private KenBurnsView backgroundImageView, backgroundImageView2, backgroundImageView3, backgroundImageView4, backgroundImageView5;
    private List<KenBurnsView> kenBurnsViews = null;
    private FloatingActionButton fab;
    private TextView title;
    private ViewFlipper mViewSwitcher;
    private ImdbObject imdbObject = null;
    private SearchView searchView = null;
    private MenuItem searchItem = null;
    private MenuItem bookmarkItem = null;
    private RequestQueue mQueue;
    public ArrayList<HashMap<String, String>> contentList;
    public ArrayList<HashMap<String, String>> galleryList;
    private ImageCursorAdapter mAdapter;
    private JSONObject[] MOVIES = {};
    int lastSelectedPosition = 0;
    ShareActionProvider shareActionProvider;
    BottomNavigationBar bottomNavigationBar;
    BadgeItem numberBadgeItem;
    LinearLayout bookmarkActionView;
    private ShineButton bookmarkView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        setContentView(R.layout.movie_detail);
        setupToolbar();
        setupViewPager();
        setupCollapsingToolbar();
        title = (TextView) findViewById(R.id.title);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        fab = (FloatingActionButton) findViewById(R.id.floating_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                createShareAction();
            }
        });

        //---------- Ken Burn Animation-----------//
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

        imdbObject = (ImdbObject) getIntent().getSerializableExtra(IMDB_OBJECT);
        mViewSwitcher = (ViewFlipper) findViewById(R.id.viewSwitcher);
//        collapsingToolbar.setTitle(trendsObject.getTitle());
        title.setText(imdbObject.getTitle());
        type = imdbObject.getType();

        if (type.compareTo("upcoming") == 0)
            lastSelectedPosition = 1;
        else if (type.compareTo("imdb") == 0)
            lastSelectedPosition = 2;

        //------- deserialize Gallery JSON object -------//
        JsonArray galleryInfo = new JsonParser().parse(imdbObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<ImdbObject.GalleryItem>();

        for (int i = 0; i < galleryInfo.size(); i++) {
            JsonElement str = galleryInfo.get(i);
            ImdbObject.GalleryItem obj = gson.fromJson(str, ImdbObject.GalleryItem.class);
            list.add(obj);
        }

        //------- deserialize Gallery JSON object -------//
        refresh();
        loadHints();
        bottomNavigationBar.setTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;
        if (numberBadgeItem != null) {
            numberBadgeItem.setText(Integer.toString(position));
        }
        goToNavItem(position);
    }

    @Override
    public void onTabUnselected(int position) {
    }

    @Override
    public void onTabReselected(int position) {
        goToNavItem(position);
    }

    @Override
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            MenuItemCompat.collapseActionView(searchItem);
            searchView.setIconified(true);
            return;
        } else {
            super.onBackPressed();
        }
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
                upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
                actionBar.setHomeAsUpIndicator(upArrow);
            }
        }
    }

    private void refresh() {
        bottomNavigationBar.clearAll();
        numberBadgeItem = new BadgeItem()
                .setBorderWidth(4)
                .setBackgroundColorResource(R.color.blue)
                .setText("" + lastSelectedPosition);

//        bottomNavigationBar.setFab(fab);

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_trending_up, R.string.navdrawer_item_explore).setActiveColorResource(R.color.material_orange_900).setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_movie, R.string.navdrawer_item_up_coming).setActiveColorResource(R.color.material_teal_A200))
                .addItem(new BottomNavigationItem(R.drawable.ic_theaters, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "NyTimes").setActiveColorResource(R.color.material_brown_300))
                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_light_blue_A100))
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();
    }

    private void setupViewPager() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.detail_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        if (imdbObject == null) {
            imdbObject = (ImdbObject) getIntent().getSerializableExtra(IMDB_OBJECT);
        }

        adapter.addFrag(ImdbInfoTabFragment.newInstance(imdbObject), "Info");
        adapter.addFrag(ImdbCastTabFragment.newInstance(imdbObject), "Cast");
        adapter.addFrag(ImdbReviewTabFragment.newInstance(imdbObject), "Review");
        adapter.addFrag(ImdbMusicTabFragment.newInstance(imdbObject), "Music");
        if (imdbObject.getType().compareTo("imdb") == 0)
            adapter.addFrag(ImdbChartTabFragment.newInstance(imdbObject), "Chart");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onResponse(Object response) {
        try {
            Log.d("0419", "title onResponse");
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            MOVIES = BaseActivity.getJsonObjectArray(contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    private void setupCollapsingToolbar() {
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(false);
        collapsingToolbar.setContentScrimColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setStatusBarScrimColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

    }

    private void setPalette(int num) {
        Bitmap bitmap = ((BitmapDrawable) kenBurnsViews.get(0).getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int primaryDark = getResources().getColor(R.color.primary_dark_material_dark);
                int primary = getResources().getColor(R.color.primary_material_light);
                /*collapsingToolbar.setContentScrimColor(palette.getMutedColor(primary));
                collapsingToolbar.setStatusBarScrimColor(palette.getDarkVibrantColor(primaryDark));*/
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
        if (list.size() < 2)
            return;
        if (mTransitionsCount == TRANSITIONS_TO_SWITCH) {
            Random random = new Random();
            transitionImageUrl(backgroundImageView, list.get(random.nextInt(list.size())).getUrl(), 250);
            mViewSwitcher.showNext();
            setPalette(random.nextInt(kenBurnsViews.size()));
            mTransitionsCount = 0;
        }
    }

    @Override
    public void onResume () {
        super.onResume();
        Picasso.with(this).load(imdbObject.getPosterUrl()).centerCrop().fit().into(backgroundImageView);
        Random random = new Random();
        kenBurnsViews = new ArrayList<KenBurnsView>();
        kenBurnsViews.add(backgroundImageView);
        if (list.size() > 1) {
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView2);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView3);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView4);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView5);
            kenBurnsViews.add(backgroundImageView2);
            kenBurnsViews.add(backgroundImageView3);
            kenBurnsViews.add(backgroundImageView4);
            kenBurnsViews.add(backgroundImageView5);
        }
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

        getMenuInflater().inflate(R.menu.trends_menu, menu);

        for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            }
        }

        bookmarkActionView = (LinearLayout) getLayoutInflater().inflate(R.layout.bookmark_image, null);
        bookmarkView = (ShineButton) bookmarkActionView.findViewById(R.id.bookmarkView);
        bookmarkView.init(this);
        bookmarkView.getLayoutParams().height=96;
        bookmarkView.getLayoutParams().width=96;
//        bookmarkView.setImageResource(R.drawable.ic_turned_in);
        bookmarkView.setColorFilter(getResources().getColor(R.color.app_white));
        bookmarkView.setScaleType(ImageView.ScaleType.FIT_XY);
        searchItem = menu.findItem(R.id.action_search);
        bookmarkItem = menu.findItem(R.id.action_bookmark);
        bookmarkItem.setActionView(bookmarkView);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);

        if (imdbObject.getBookmark()) {
            bookmarkView.setChecked(true);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
        } else {
            bookmarkView.setChecked(false);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_not_white);
        }

        bookmarkView.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
//                Snackbar.make(view, "Bookmark "+checked+" !!!", Snackbar.LENGTH_LONG).show();
                Toast.makeText(MovieDetailActivity.this, "Bookmark "+checked+" !!!", Toast.LENGTH_SHORT).show();
                if (checked) {
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                    LOGD("0812", imdbObject.getTitle() + " " + imdbObject.getPosterUrl());
                    //TODO bookmark
                } else {
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in);
                    //TODO cancel bookmark
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //if you want to collapse the searchview
                requestDataRefresh(query);
                invalidateOptionsMenu();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                giveSuggestions(query);
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                final String feedName = cursor.getString(1);
                searchView.post(new Runnable(){
                    @Override
                    public void run() {
                        searchView.setQuery(feedName, true);
                    }
                });
                return true;
            }
        });

        searchView.setSuggestionsAdapter(mAdapter);

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = new ShareActionProvider(this);
        /*shareActionProvider =  (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);*/
        MenuItemCompat.setActionProvider(shareItem, shareActionProvider);
        shareActionProvider.setShareIntent(createShareIntent());
        return true;

    }

    private void loadHints() {
        final String[] from = new String [] {FILM_NAME};
        final int[] to = new int[] { R.id.text1};
        final CustomJSONObjectRequest jsonRequest;

        mAdapter = new ImageCursorAdapter(this,
                R.layout.search_row,
                null,
                from,
                to,
                "detail");

        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();

        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "imdb_title", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = ((JSONObject) response).getJSONArray("contents");
                    MOVIES = BaseActivity.getJsonObjectArray(contents);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MovieDetailActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME, FILM_DESCRIPTION, FILM_POSTER});
        try {
            for (int i = 0; i < MOVIES.length; i++) {
                if (MOVIES[i].getString("title").toLowerCase().contains(query.toLowerCase()))
                    cursor.addRow(new Object[]{i, MOVIES[i].getString("title"), MOVIES[i].getString("description"), MOVIES[i].getString("posterUrl")});
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapter.changeCursor(cursor);
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;
        AlbumActivity.contentList = new ArrayList<HashMap<String, String>>();
        AlbumActivity.galleryList = new ArrayList<HashMap<String, String>>();

        mQueue = CustomVolleyRequestQueue.getInstance(MovieDetailActivity.this)
                .getRequestQueue();

        CustomJSONObjectRequest jsonRequest_q = null;

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }
            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/imdb?title=" + Query + "&ascending=1", new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray contents = response.getJSONArray("contents");
                        JSONObject c = contents.getJSONObject(0);
                        ImdbObject movie = BuildModelUtils.buildImdbModel(contents);
                        if (c.has(TAG_TOP))
                            movie.setType("imdb");
                        else
                            movie.setType("genre");
                        Intent intent = new Intent(MovieDetailActivity.this, MovieDetailActivity.class);
                        intent.putExtra(MovieDetail.IMDB_OBJECT, movie);
                        ActivityCompat.startActivity(MovieDetailActivity.this, intent, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, this);
            mQueue.add(jsonRequest_q);
            return;
        }

        jsonRequest.setTag(REQUEST_TAG);

        mQueue.add(jsonRequest); //trigger volley request
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
            case R.id.menu_smooth_zero:
                ImdbReviewTabFragment.movieReview.smoothScrollToPosition(0);
                return true;
            case R.id.menu_smooth_end:
                ImdbReviewTabFragment.movieReview.smoothScrollToPosition(ImdbReviewTabFragment.linearLayoutManager.getItemCount());
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

    private void goToNavItem(int item) {
        switch (item) {
            case NAV_ITEM_TREND:
                createBackStack(new Intent(this, MainActivity.class));
                break;
            case NAV_ITEM_UPCOMING:
                createBackStack(new Intent(this, upComingActivity.class));
                break;
            case NAV_ITEM_IMDB:
                createBackStack(new Intent(this, ImdbActivity.class));
                break;
            case NAV_ITEM_NYTIMES:
                createBackStack(new Intent(this, nyTimesActivity.class));
                break;
            case NAV_ITEM_GENRE:
                createBackStack(new Intent(this, GenreActivity.class));
                break;
        }
    }

    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivityForVersion(intent);
            finish();
        }
    }

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            MovieDetailActivity.this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    private void onShareAction() {
        shareActionProvider.setShareIntent(createShareIntent());
        return;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (type.compareTo("imdb") == 0)
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getDetailUrl());
        else
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getTrailerUrl());
        return shareIntent;
    }

    private void createShareAction() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (type.compareTo("imdb") == 0)
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getDetailUrl());
        else
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getTrailerUrl());
        // Launch sharing dialog for image
        startActivity(Intent.createChooser(shareIntent, "Share Review"));
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

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
