package com.github.florent37.materialviewpager.worldmovies.imdb;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.ImdbSwipeRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.TagAdapter;
import com.github.florent37.materialviewpager.worldmovies.favorite.MoviesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.fragment.MovieRecycleFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.RecyclerViewFragment;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.model.TagFilterHolder;
import com.github.florent37.materialviewpager.worldmovies.model.TagMetadata;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.SearchActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionView;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;
import static com.github.florent37.materialviewpager.worldmovies.util.UIUtils.checkMoviesBookmark;

/**
 * Created by aaron on 2016/3/21.
 */
public class ImdbActivity extends BaseActivity implements Response.ErrorListener,
        BottomNavigationBar.OnTabSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static String IMDB_OBJECT = "IMDB_OBJECT";
    private Toolbar toolbar;
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private List<ImdbObject> movieList;
    private RecyclerView rvMovies;
    private RequestQueue mQueue;
    private String HOST_NAME = Config.HOST_NAME;
    private ImdbSwipeRecycleViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private DrawerLayout mDrawerLayout;
    private CollectionView mDrawerCollectionView;
    private MoviesFavoritePreference moviesFavor;
//    private SwipeListAdapter adapter;
    private boolean mActionBarShown = true;
    private int mProgressBarTopWhenActionBarShown;
    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;
    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Set<MovieRecycleFragment> mMovieRecycleFragments = new HashSet<MovieRecycleFragment>();
    private final int TAG_METADATA_TOKEN = 0x8;
    private final int PAGE_UNIT = 6; //default 6 cards in one page
    private final int GROUP_TOPIC_TYPE_OR_THEME = 0;
    private final int GROUP_LIVE_STREAM = 1;
    private final int GROUP_COUNTRY = 2;
    // JSON Node names
    private final String TAG_TITLE = "title";
    private final String TAG_YEAR = "year";
    private final String TAG_RELEASE = "releaseDate";
    private final String TAG_DATA = "data";
    private final String TAG_TOP = "top";
    private final String TAG_POSTER_URL = "posterUrl";
    private final String TAG_RATING = "rating";
    private final String TAG_DESCRIPTION = "description";
    private final String TAG_CAST = "cast";
    private final String TAG_DETAIL_URL = "detailUrl";
    private final String TAG_DETAIL_POSTER_URL = "poster";
    private final String TAG_SUMMERY = "summery";
    private final String TAG_PLOT = "plot";
    private final String TAG_GENRE = "genres";
    private final String TAG_VOTES = "votes";
    private final String TAG_RUNTIME = "runtime";
    private final String TAG_METASCORE = "metascore";
    private final String TAG_SLATE = "slate";
    private final String TAG_COUNTRY = "country";
    private final String TAG_TRAILER = "trailerUrl";
    private final String TAG_GALLERY_FULL = "gallery_full";
    private final String TAG_DELTA = "delta";
    public final String REQUEST_TAG = "imdb250Request";
    public static final String FILM_NAME = "filmName";
    private TagMetadata mTagMetadata;
    private TagFilterHolder mTagFilterHolder;
    private int lastSelectedPosition = 2;
    private BottomNavigationBar bottomNavigationBar;
    private BadgeItem numberBadgeItem;
    private static final String TAG = makeLogTag(ImdbActivity.class);
    private int curSize = 0;
    private String searchChannel = "14";
    private Menu activityMenu;
    private TagAdapter tagAdapter;
    // The OnClickListener for the Switch widgets on the navigation filter.
    private final View.OnClickListener mDrawerItemCheckBoxClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox)v).isChecked();
                    TagMetadata.Tag theTag = (TagMetadata.Tag)v.getTag();
                    LOGD(TAG, "Checkbox with tag: " + theTag.getName() + " isChecked => " + isChecked);
                    if (isChecked) {
                        if (theTag.getCategory().equals("COUNTRY")) {
                            mTagFilterHolder.clear(); //support one country for searching
                        }
                        // Here we only add all 'types' if the user has not explicitly selected
                        // one of the category_type tags.
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());

                        List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

                        for (TagMetadata.Tag tag : tags) {
                            if (mTagFilterHolder.contains(tag.getId())) {
                                searchChannel = String.valueOf(tag.getOrderInCategory());
                                CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                            }
                        }
                    } else {
                        searchChannel = "14";
                        mTagFilterHolder.remove(theTag.getId(), theTag.getCategory());
                        CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                    }

                    mDrawerLayout.closeDrawer(GravityCompat.END);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top250);
        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setNavigationIcon(R.drawable.ic_up);
        Drawable drawable = toolbar.getNavigationIcon();
        drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        registerHideableHeaderView(findViewById(R.id.headerbar));
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_flipped, GravityCompat.END);
        mDrawerCollectionView = (CollectionView) findViewById(R.id.drawer_collection_view);
        moviesFavor = new MoviesFavoritePreference();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        textView.setBackgroundColor(Color.parseColor("#f5de50"));
        textView.setLayoutParams(lParams);
        // 获得根视图并把TextView加进去。
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_movie_layout);
        movieList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        adapter = new ImdbSwipeRecycleViewAdapter(this, movieList);
        rvMovies = (RecyclerView) findViewById(R.id.recyclerView);
        rvMovies.setLayoutManager(linearLayoutManager);
        rvMovies.setAdapter(adapter);
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();
        CredentialsHandler.setCountry(getApplicationContext(), searchChannel);

        rvMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                SharedPreferences settings = getSharedPreferences("settings", 0);
                Boolean Small = settings.getBoolean("miniCard", true);

                if (Small)
                    adapter.visibleThreshold = 5;
                else
                    adapter.visibleThreshold = 1;

                int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
                adapter.totalItemCount = linearLayoutManager.getItemCount();
                adapter.lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!adapter.loading && adapter.totalItemCount <= (adapter.lastVisibleItem + adapter.visibleThreshold)) {
                    boolean ascending = settings.getBoolean("ascending", false);
                    // End has been reached
                    if (ascending) {
                        fetchMovies(false);
                        adapter.loading = true;
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        // Start loading the tag metadata. This will in turn call the fragment with the correct arguments.
        getLoaderManager().initLoader(TAG_METADATA_TOKEN, null, this);
        refresh();
        bottomNavigationBar.setTabSelectedListener(this);
        overridePendingTransition(0, 0);
    }

    private void refresh() {
        bottomNavigationBar.clearAll();
        numberBadgeItem = new BadgeItem()
                .setBorderWidth(4)
                .setBackgroundColorResource(R.color.blue)
                .setText("" + lastSelectedPosition);

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_trending_up, R.string.navdrawer_item_explore).setActiveColorResource(R.color.material_orange_900).setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_movie, R.string.navdrawer_item_up_coming).setActiveColorResource(R.color.material_teal_A200))
                .addItem(new BottomNavigationItem(R.drawable.imdb, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "nytimes").setActiveColorResource(R.color.material_brown_400))
                .addItem(new BottomNavigationItem(R.drawable.ic_person, "Profile").setActiveColorResource(R.color.material_red_900))
//                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_red_900))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setInActiveColor(R.color.material_grey_800)
                .setBarBackgroundColor(R.color.imdb_yellow)
                .initialise();
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
    protected void onResume() {
        super.onResume();
        if (activityMenu != null) {
            MenuItem filter = activityMenu.findItem(R.id.action_filter);
            Drawable image = filter.getIcon();
            image.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onTabReselected(int position) {
        goToNavItem(position);
    }

    @Override
    public void onTabUnselected(int position) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TAG_METADATA_TOKEN) {
            LOGD("1018", "createLoader");
            return TagMetadata.createCursorLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case TAG_METADATA_TOKEN:
                LOGD("1021", "load finish\n"+cursor.getCount());
                searchChannel = CredentialsHandler.getCountry(this);
                mTagMetadata = new TagMetadata(cursor);
                onTagMetadataLoaded();
                break;
            default:
                cursor.close();
        }
    }

    public void onTagMetadataLoaded() {

        if (mTagFilterHolder == null) {
            // Use the Intent Extras to set up the TagFilterHolder
            mTagFilterHolder = new TagFilterHolder();
            String tag = getIntent().getStringExtra(EXTRA_FILTER_TAG); //TODO get tag from preference
            TagMetadata.Tag userTag = mTagMetadata.getTag(tag);
            String userTagCategory = userTag == null ? null : userTag.getCategory();

            if (tag != null && userTagCategory != null) {
                mTagFilterHolder.add(tag, userTagCategory);
            }

            List<TagMetadata.Tag> countryTags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

            if (countryTags != null && !TextUtils.equals(userTagCategory, Config.Tags.CATEGORY_COUNTRY)) {
                for (TagMetadata.Tag theTag : countryTags) {
                    if (String.valueOf(theTag.getOrderInCategory()).equals(searchChannel))
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                }
            }

            List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_THEME);
            // Here we only add all 'types' if the user has not explicitly selected
            // one of the category_type tags.
            if (tags != null && !TextUtils.equals(userTagCategory, Config.Tags.CATEGORY_THEME)) {
                for (TagMetadata.Tag theTag : tags) {
                    if (theTag.getId().equals("All")) {
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                    }
                }
            }
        }

        tagAdapter = new TagAdapter(mTagMetadata, mDrawerItemCheckBoxClickListener, mTagFilterHolder);
        mDrawerCollectionView.setCollectionAdapter(tagAdapter);
        mDrawerCollectionView.updateInventory(tagAdapter.getInventory());
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
    public void trySetupSwipeRefresh() {

        mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */

        if (adapter.getItemCount() == 0) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    fetchMovies(true);
                }
            });
        }

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RecyclerViewFragment fragment;
        RecyclerView view;
        switch (item.getItemId()) {
            case R.id.menu_miniCard:
                item.setChecked(!item.isChecked());
                SharedPreferences miniCard = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor_miniCard = miniCard.edit();
                editor_miniCard.putBoolean("miniCard", item.isChecked());
                editor_miniCard.commit();
                bindAdapter();
                return true;
            case R.id.menu_refresh:
                fetchMovies(true);
                return true;
            case R.id.menu_smooth_zero:
                rvMovies.smoothScrollToPosition(0);
                return true;
            case R.id.menu_smooth_end:
                rvMovies.smoothScrollToPosition(linearLayoutManager.getItemCount());
                return true;
            case R.id.menu_ascending:
                item.setChecked(!item.isChecked());
                SharedPreferences ascending = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor_ascending = ascending.edit();
                editor_ascending.putBoolean("ascending", item.isChecked());
                editor_ascending.commit();
                arrangeModel();
                bindAdapter();
                return true;
            case R.id.action_settings:
                return true;
            case R.id.action_filter:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
            case R.id.action_search:
                View searchMenuView = toolbar.findViewById(R.id.action_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle();
                Intent intent = new Intent(ImdbActivity.this, SearchActivity.class);
                intent.putExtra("lastSelectedPosition", lastSelectedPosition);
                intent.putExtra("lauchBy", "imdb");
                ActivityCompat.startActivity(ImdbActivity.this, intent, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void arrangeModel() {
        SharedPreferences settings = getSharedPreferences("settings", 0);
        boolean ascending = settings.getBoolean("ascending", false);

        if (!ascending)
            Collections.sort(movieList, DescendingComparator);
        else
            Collections.sort(movieList, AscendingComparator);

        return;
    }

    public void bindAdapter() {
        rvMovies.setAdapter(adapter);
        rvMovies.scheduleLayoutAnimation();
        adapter.notifyDataSetChanged();
        return;
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchMovies(true);
    }

    /**
     * Fetching movies json by making http call
     */
    public void fetchMovies(final boolean swipe) {

        // showing refresh animation before making http call
        if (swipe)
            mSwipeRefreshLayout.setRefreshing(true);
        else {
            movieList.add(null);
            adapter.notifyItemInserted(movieList.size() - 1);
        }

        // appending offset to url
        int start = linearLayoutManager.getItemCount()-1;
        int end = start + PAGE_UNIT; //6 default 6 cards per page
        String url = HOST_NAME + "/imdb?from=" + (start + 1) + "&to=" + end + "&ascending=1";

        CustomJSONObjectRequest jsonRequest_q = null;
        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!swipe) {
                        movieList.remove(movieList.size() - 1);
                        adapter.notifyItemRemoved(movieList.size());
                    }
                    JSONArray contents = response.getJSONArray("contents");
                    boolean byTitle = ((JSONObject) response).getBoolean("byTitle");

                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject jsonObj = new JSONObject();
                        JSONArray data = new JSONArray();
                        JSONObject c = contents.getJSONObject(i);
                        String title = c.getString(TAG_TITLE);
                        JSONObject d = c.getJSONObject("detailContent");
                        int top = 0;
                        String detailPosterUrl = "";
                        String detailUrl = "";
                        String year = "";
                        String votes = "";
                        String plot = "";
                        String posterUrl = "http://www.imdb.com/title/tt1355631/mediaviewer/rm3798736128?ref_=tt_ov_i";
                        String delta = "0";
                        //----- start dummy GalleryUrl ----
                        JSONObject jo = new JSONObject();
                        jo.put("type", "full");
                        jo.put("url", "");
                        JSONArray galleryFullUrl = new JSONArray();
                        JSONArray cast = new JSONArray();
                        galleryFullUrl.put(jo);
                        //----- end dummy GalleryUrl ----

                        if (c.has(TAG_TOP)) {
                            top = c.getInt(TAG_TOP);
                            if (top > offSet)
                                offSet = top;
                        }

                        if (c.has(TAG_DATA))
                            data = c.getJSONArray(TAG_DATA);

                        if (c.has(TAG_DELTA))
                            delta = c.getString(TAG_DELTA);

                        String description= c.getString(TAG_DESCRIPTION);
                        String rating = c.getString(TAG_RATING);
                        year = c.has(TAG_YEAR) ? c.getString(TAG_YEAR) : c.getString("releaseDate");

                        if (c.has(TAG_POSTER_URL))
                            posterUrl = c.getString(TAG_POSTER_URL);

                        if (c.has(TAG_DETAIL_URL))
                            detailUrl = c.getString(TAG_DETAIL_URL);

                        if (c.has(TAG_CAST))
                            cast = c.getJSONArray(TAG_CAST);

                        if (c.has(TAG_PLOT))
                            plot = c.getString(TAG_PLOT);

                        if (c.has(TAG_VOTES))
                            votes = c.getString(TAG_VOTES);

                        String genre = c.has(TAG_GENRE) ? c.getString(TAG_GENRE) : "";
                        String runTime = c.has(TAG_RUNTIME) ? c.getString(TAG_RUNTIME) : "";
                        String metaScore = c.getString(TAG_METASCORE);
                        String summery = d.getString(TAG_SUMMERY);
                        String country = d.getString(TAG_COUNTRY);

                        if (runTime.compareTo("") == 0 && data.length()>0) {
                            jsonObj = (JSONObject) data.get(4);
                            runTime = jsonObj.getString("data");
                        }

                        if (genre.compareTo("") == 0)
                            genre = c.getString("genre");

                        if (c.has(TAG_GALLERY_FULL))
                            galleryFullUrl = c.getJSONArray(TAG_GALLERY_FULL);

                        String trailerUrl;
                        String slate;
                        trailerUrl = c.has(TAG_TRAILER) ? c.getString(TAG_TRAILER) : "N/A";
                        slate = d.has(TAG_SLATE) ? d.getString(TAG_SLATE) : "N/A";
                        ImdbObject item = new ImdbObject(title, String.valueOf(top), year, description,
                                rating, posterUrl, slate, summery, plot,
                                genre, votes, runTime, metaScore, delta, country,
                                trailerUrl, cast.toString(), galleryFullUrl.toString(), detailUrl);
                        SharedPreferences settings = getSharedPreferences("settings", 0);

                        if (checkMoviesBookmark(title, moviesFavor, getApplicationContext()))
                            item.setBookmark(true);

                        boolean ascending = settings.getBoolean("ascending", false);
                        curSize = adapter.getItemCount();

                        if (ascending)
                            movieList.add(movieList.size(), item);
                        else
                            movieList.add(0, item);
                        if (adapter != null && ascending)
                            adapter.notifyItemInserted(movieList.size());
                        else if (adapter != null && !ascending)
                            adapter.notifyItemInserted(0);
                    }

                    if (!byTitle)
                        adapter.setLoaded();

                    if (swipe)
                        mSwipeRefreshLayout.setRefreshing(false);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (swipe)
                    mSwipeRefreshLayout.setRefreshing(false);
//                Toast.makeText(ImdbActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });

        mQueue.add(jsonRequest_q);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
//        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        activityMenu = menu;
        getMenuInflater().inflate(R.menu.imdb_menu, menu);

        Drawable drawable = toolbar.getOverflowIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), Color.BLACK);
            toolbar.setOverflowIcon(drawable);
        }

        MenuItem filter = menu.findItem(R.id.action_filter);
        Drawable image = filter.getIcon();
        image.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences settings = getSharedPreferences("settings", 0);
        MenuItem miniCard = menu.findItem(R.id.menu_miniCard);
        MenuItem ascending = menu.findItem(R.id.menu_ascending);
        MenuItem search = menu.findItem(R.id.action_search);
        Drawable drawable = search.getIcon();
        drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        miniCard.setChecked(settings.getBoolean("miniCard", true));
        ascending.setChecked(settings.getBoolean("ascending", false));
        return true;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_IMDB;
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {

        // Prevent the swipe refresh by returning true here
        if (mViewPagerScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
            return true;
        }

        /*for( Map.Entry<Integer,Fragment> entry : mFragmentCache.entrySet()){
            Integer key = entry.getKey();
            Fragment value = entry.getValue();
            Log.d("0310", "key: " + key);
        }*/

        for (MovieRecycleFragment fragment : mMovieRecycleFragments) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (!fragment.getUserVisibleHint()) {
                    continue;
                }
            }
            return ViewCompat.canScrollVertically(fragment.getRecyclerView(), -1);
        }

        return false;
    }

    @Override
    public void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        int top = mActionBarShown ? mProgressBarTopWhenActionBarShown : 0;
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);
    }

    @Override
    protected void onRefreshingStateChanged(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    protected void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    private Comparator AscendingComparator = new Comparator<ImdbObject>() {
        @Override
        public int compare(ImdbObject o1, ImdbObject o2) {
            if (Integer.parseInt(o1.getTop()) > Integer.parseInt(o2.getTop())) {
                return 1;
            }
            else if (Integer.parseInt(o1.getTop()) < Integer.parseInt(o2.getTop())) {
                return -1;
            }
            return 0;
        }
    };

    private Comparator DescendingComparator = new Comparator<ImdbObject>() {
        @Override
        public int compare(ImdbObject o1, ImdbObject o2) {
            if (Integer.parseInt(o1.getTop()) < Integer.parseInt(o2.getTop())) {
                return 1;
            }
            else if (Integer.parseInt(o1.getTop()) > Integer.parseInt(o2.getTop())) {
                return -1;
            }
            return 0;
        }
    };
}
