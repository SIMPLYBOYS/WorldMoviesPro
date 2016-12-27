package com.github.florent37.materialviewpager.worldmovies.ptt;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.TagAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.pttSwipeRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.fragment.MovieRecycleFragment;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.TagFilterHolder;
import com.github.florent37.materialviewpager.worldmovies.model.TagMetadata;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.SearchActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionView;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;

/**
 * Created by aaron on 2016/12/23.
 */

public class pttActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private Toolbar toolbar;
    private String HOST_NAME = Config.HOST_NAME;
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private List<pttMovie> movieList;
    private RecyclerView pttMovies;
    public static final String FILM_NAME = "filmName";
    private RequestQueue mQueue;
    private pttSwipeRecycleViewAdapter pttAdapter;
    private SimpleCursorAdapter mAdapter;
    private LinearLayoutManager linearLayoutManager;
    private MenuItem searchItem;
    private SearchView searchView = null;
    public final String REQUEST_TAG = "reviewRequest";
    private boolean mActionBarShown = true;
    private int lastSelectedPosition = 3;
    private BottomNavigationBar bottomNavigationBar;
    private CustomJSONObjectRequest jsonRequest;
    private TagFilterHolder mTagFilterHolder;
    private DrawerLayout mDrawerLayout;
    private final int TAG_METADATA_TOKEN = 0x8;
    private CollectionView mDrawerCollectionView;
    private String searchChannel = "14";
    private BadgeItem numberBadgeItem;
    private TagMetadata mTagMetadata;
    private static final String TAG = makeLogTag(pttActivity.class);
    private int mProgressBarTopWhenActionBarShown;
    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Set<MovieRecycleFragment> mMovieRecycleFragments = new HashSet<MovieRecycleFragment>();
    private TagAdapter tagAdapter;
    private int skipSize = 0;

    private final View.OnClickListener mDrawerItemCheckBoxClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox)v).isChecked();
                    TagMetadata.Tag theTag = (TagMetadata.Tag)v.getTag();
                    LOGD(TAG, "Checkbox with tag: " + theTag.getName() + " isChecked => " + isChecked);
                    LOGD("1105", theTag.getCategory()+"\n"+theTag.getId());
                    if (isChecked) {
                        if (theTag.getCategory().equals("COUNTRY")) {
                            mTagFilterHolder.clear(); //support one country for searching
                        }

                        // Here we only add all 'types' if the user has not explicitly selected
                        // one of the category_type tags.
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                        mTagFilterHolder.add("All", "THEME");
                        List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

                        for (TagMetadata.Tag tag : tags) {
                            if (mTagFilterHolder.contains(tag.getId())) {
                                LOGD("1105", String.valueOf(tag.getId()));
                                searchChannel = String.valueOf(tag.getOrderInCategory());
                                CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                            }
                        }

                    } else {
                        searchChannel = "14";
                        mTagFilterHolder.remove(theTag.getId(), theTag.getCategory());
                        CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                    }

                    if (theTag.getCategory().equals("COUNTRY"))
                        mDrawerLayout.closeDrawer(GravityCompat.END);

                    //------------------//
                    tagAdapter = new TagAdapter(mTagMetadata, mDrawerItemCheckBoxClickListener, mTagFilterHolder);
                    mDrawerCollectionView.setCollectionAdapter(tagAdapter);
                    mDrawerCollectionView.updateInventory(tagAdapter.getInventory());
                    //------------------//
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ptt);
        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitleTextColor(Color.WHITE);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_flipped, GravityCompat.END);
        mDrawerCollectionView = (CollectionView) findViewById(R.id.drawer_collection_view);
        setSupportActionBar(toolbar);
        registerHideableHeaderView(findViewById(R.id.headerbar));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        //-------- change statusbar color -----//
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        textView.setBackgroundColor(Color.parseColor("#ff212121"));
        textView.setLayoutParams(lParams);
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);
        //-------- fixed statusbar color -----//

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_movie_layout);
        movieList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        pttAdapter = new pttSwipeRecycleViewAdapter(this, movieList);
        pttMovies = (RecyclerView) findViewById(R.id.ptt_recyclerView);
        pttMovies.getItemAnimator().setAddDuration(500);
        pttMovies.getItemAnimator().setChangeDuration(500);
        pttMovies.setLayoutManager(linearLayoutManager);
        pttMovies.setAdapter(pttAdapter);
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();

        pttMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);

                pttAdapter.totalItemCount = linearLayoutManager.getItemCount();
                pttAdapter.lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!pttAdapter.loading && pttAdapter.totalItemCount <= (pttAdapter.lastVisibleItem + pttAdapter.visibleThreshold)) {
                    // End has been reached
                    // Do something
                    fetchPtt(false);
                    pttAdapter.loading = true;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        getLoaderManager().initLoader(TAG_METADATA_TOKEN, null, this);
        refresh(bottomNavigationBar, lastSelectedPosition, numberBadgeItem);
        bottomNavigationBar.setTabSelectedListener(this);
        overridePendingTransition(0, 0);
    }

    public void fetchPtt(final boolean swipe) {
        // showing refresh animation before making http call
        if (swipe)
            mSwipeRefreshLayout.setRefreshing(true);
        else {
            movieList.add(null);
            pttAdapter.notifyItemInserted(movieList.size() - 1);
        }

        Calendar c = Calendar.getInstance();
        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/ptt_movies?post_from=" + getPostDate(c.get(Calendar.MONTH), 0, 1)+"&skip="+skipSize, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    if (!swipe) {
                        movieList.remove(movieList.size() - 1);
                        pttAdapter.notifyItemRemoved(movieList.size());
                    }

                    JSONArray contents = response.getJSONArray("contents");
                    skipSize += contents.length();

                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String title = movieObj.getString("title");
                        LOGD("1224", title);
                        String date = movieObj.getString("date");
                        String linkUrl = movieObj.getString("link");
                        String editor = movieObj.getString("autor");
                        pttMovie movie = new pttMovie(title, date, null, linkUrl, null, editor, null);
                        movieList.add(movieList.size(), movie);
                        if (pttAdapter != null)
                            pttAdapter.notifyItemInserted(movieList.size());

                    }

                    pttAdapter.setLoaded();

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
                Toast.makeText(pttActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    @Override
    public void trySetupSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        if (pttAdapter.getItemCount() == 0) {
            fetchPtt(true);
            pttAdapter.swipe = true;
        }

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        SharedPreferences settings = getSharedPreferences("settings", 0);
        getMenuInflater().inflate(R.menu.imdb_menu, menu);
        Drawable drawable = toolbar.getOverflowIcon();

        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), Color.parseColor("#FFFFFF"));
            toolbar.setOverflowIcon(drawable);
        }

        MenuItem filter = menu.findItem(R.id.action_filter);
        Drawable image = filter.getIcon();
        image.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                fetchPtt(true);
                return true;
            case R.id.menu_smooth_zero:
                pttMovies.smoothScrollToPosition(0);
                return true;
            case R.id.menu_smooth_end:
                pttMovies.smoothScrollToPosition(linearLayoutManager.getItemCount());
                return true;
            case R.id.action_filter:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
            case R.id.action_search:
                View searchMenuView = toolbar.findViewById(R.id.action_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle();
                Intent intent = new Intent(pttActivity.this, SearchActivity.class);
                intent.putExtra("lastSelectedPosition", lastSelectedPosition);
                intent.putExtra("lauchBy", "upcoming");
                ActivityCompat.startActivity(pttActivity.this, intent, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return true;
    }

    @Override
    public void onTabReselected(int position) {
        goToNavItem(position);
    }

    @Override
    public void onTabUnselected(int position) {
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAV_ITEM_TREND;
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchPtt(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TAG_METADATA_TOKEN) {
            return TagMetadata.createCursorLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case TAG_METADATA_TOKEN:
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

    public static String getPostDate(int start, int month, int day) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String str = df.format(c.getTime());
        return str;
    }

}
