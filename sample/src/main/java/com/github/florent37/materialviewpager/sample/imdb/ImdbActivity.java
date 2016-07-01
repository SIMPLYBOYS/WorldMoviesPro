package com.github.florent37.materialviewpager.sample.imdb;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.ImdbSwipeRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.fragment.MovieRecycleFragment;
import com.github.florent37.materialviewpager.sample.fragment.RecyclerViewFragment;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;
import com.github.florent37.materialviewpager.sample.ui.BaseActivity;
import com.github.florent37.materialviewpager.sample.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by aaron on 2016/3/21.
 */
public class ImdbActivity extends BaseActivity implements Response.ErrorListener {

    private Toolbar toolbar;
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private List<ImdbObject> movieList;
    private RecyclerView rvMovies;
    private RequestQueue mQueue;
    public String HOST_NAME = Config.HOST_NAME;
    private ImdbSwipeRecycleViewAdapter adapter;
    private StaggeredGridLayoutManager gaggeredGridLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private static final int PAGE_UNIT = 6; //default 6 cards in one page
//    private SwipeListAdapter adapter;
    private boolean mActionBarShown = true;
    private int mProgressBarTopWhenActionBarShown;
    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;
    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Set<MovieRecycleFragment> mMovieRecycleFragments = new HashSet<MovieRecycleFragment>();
    // JSON Node names
    private static final String TAG_TITLE = "title";
    private static final String TAG_YEAR = "year";
    private static final String TAG_RELEASE = "releaseDate";
    private static final String TAG_TOP = "top";
    private static final String TAG_POSTER_URL = "posterUrl";
    private static final String TAG_RATING = "rating";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_DETAIL_URL = "detailUrl";
    private static final String TAG_DETAIL_POSTER_URL = "poster";
    private static final String TAG_SUMMERY = "summery";
    private static final String TAG_PLOT = "plot";
    private static final String TAG_GENRE = "genres";
    private static final String TAG_VOTES = "votes";
    private static final String TAG_RUNTIME = "runtime";
    private static final String TAG_METASCORE = "metascore";
    private static final String TAG_SLATE = "slate";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_TRAILER = "trailerUrl";
    private static final String TAG_GALLERY_FULL = "gallery_full";
    private static final String TAG_DELTA = "delta";
    public static final String REQUEST_TAG = "imdb250Request";
    int curSize = 0;
    private MenuItem searchItem;
    private SearchView searchView = null;
    private SimpleCursorAdapter mAdapter;
    public static final String FILM_NAME = "filmName";
    private static String[] MOVIES = {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top250);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerHideableHeaderView(findViewById(R.id.headerbar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        textView.setBackgroundColor(Color.parseColor("#f5de50"));
        textView.setLayoutParams(lParams);
        // 获得根视图并把TextView加进去。
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);
        loadHints();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_movie_layout);
        movieList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        adapter = new ImdbSwipeRecycleViewAdapter(this, movieList);
        rvMovies = (RecyclerView) findViewById(R.id.recyclerView);
        rvMovies.setLayoutManager(linearLayoutManager);
        rvMovies.setAdapter(adapter);

        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();

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
        overridePendingTransition(0, 0);
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

    private void loadHints() {
        final String[] from = new String[]{FILM_NAME};
        final int[] to = new int[]{android.R.id.text1};
        final CustomJSONObjectRequest jsonRequest;
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.hint_row,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();

        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "/imdb_title", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("0419", "title onResponse");
                    JSONArray contents = ((JSONObject) response).getJSONArray("contents");
                    MOVIES = getStringArray(contents);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ImdbActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
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

        Log.d("0309","offSet: " + offSet);
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
                        JSONObject c = contents.getJSONObject(i);
                        String title = c.getString(TAG_TITLE);
                        JSONObject d = c.getJSONObject("detailContent");
                        int top = 0;
                        String detailPosterUrl = "";
                        String year = "";
                        String posterUrl = "http://www.imdb.com/title/tt1355631/mediaviewer/rm3798736128?ref_=tt_ov_i";
                        String delta = "0";
                        //----- start dummy GalleryUrl ----
                        JSONObject jo = new JSONObject();
                        jo.put("type", "full");
                        jo.put("url", "");
                        JSONArray galleryFullUrl = new JSONArray();
                        galleryFullUrl.put(jo);
                        //----- end dummy GalleryUrl ----

                        if (c.has(TAG_TOP)) {
                            Log.d("0518", title);
                            top = c.getInt(TAG_TOP);
                            if (top > offSet)
                                offSet = top;
                        }
                        year = c.getString(TAG_YEAR);

                        if (c.has(TAG_RELEASE) && !c.has(TAG_TOP)) {
                            year = String.valueOf(c.getInt(TAG_RELEASE));
                            year = year.substring(4, 8);
                        }

                        if (c.has(TAG_DELTA)) {
                            delta = c.getString(TAG_DELTA);
                        }

                        String description= c.getString(TAG_DESCRIPTION);
                        String rating = c.getString(TAG_RATING);

                        if (c.has(TAG_POSTER_URL)) {
                            posterUrl = c.getString(TAG_POSTER_URL);
                        }

                        String plot = c.getString(TAG_PLOT);
                        String genre = c.getString(TAG_GENRE);
                        String votes = c.getString(TAG_VOTES);
                        String runTime = c.getString(TAG_RUNTIME);
                        String metaScore = c.getString(TAG_METASCORE);

                        String summery = d.getString(TAG_SUMMERY);
                        String country = d.getString(TAG_COUNTRY);

                        if (c.has(TAG_GALLERY_FULL)) {
                            galleryFullUrl = c.getJSONArray(TAG_GALLERY_FULL);
                        }

                        String trailerUrl;
                        String slate;

                        if (c.has(TAG_TRAILER))
                            trailerUrl = c.getString(TAG_TRAILER);
                        else
                            trailerUrl = "N/A";

                        if (d.has(TAG_SLATE))
                            slate = d.getString(TAG_SLATE);
                        else
                            slate = "N/A";

                        ImdbObject item = new ImdbObject(title, String.valueOf(top), year, description,
                                rating, posterUrl, slate, summery, plot,
                                genre, votes, runTime, metaScore, delta, country,
                                trailerUrl, galleryFullUrl.toString());

                        SharedPreferences settings = getSharedPreferences("settings", 0);
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
                Log.d("0606", String.valueOf(error.getMessage()));
                Toast.makeText(ImdbActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });

        mQueue.add(jsonRequest_q);
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;

        mQueue = CustomVolleyRequestQueue.getInstance(ImdbActivity.this)
                .getRequestQueue();
        CustomJSONObjectRequest jsonRequest_q = null;
        String url = null;

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");

                url= Config.HOST_NAME + "/imdb?title=" + Query + "&ascending=1";
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }
            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray contents = response.getJSONArray("contents");
                        JSONObject c = contents.getJSONObject(0);
                        String title = c.getString(TAG_TITLE);
                        JSONObject d = c.getJSONObject("detailContent");
                        int top = 0;
                        String year = "";
                        String posterUrl = "http://www.imdb.com/title/tt1355631/mediaviewer/rm3798736128?ref_=tt_ov_i";
                        String delta = "0";
                        //----- start dummy GalleryUrl ----
                        JSONObject jo = new JSONObject();
                        jo.put("type", "full");
                        jo.put("url", "");
                        JSONArray galleryFullUrl = new JSONArray();
                        galleryFullUrl.put(jo);
                        //----- end dummy GalleryUrl ----

                        if (c.has(TAG_TOP)) {
                            Log.d("0518", title);
                            top = c.getInt(TAG_TOP);
                            if (top > offSet)
                                offSet = top;
                        }
                        year = c.getString(TAG_YEAR);

                        if (c.has(TAG_RELEASE) && !c.has(TAG_TOP)) {
                            year = String.valueOf(c.getInt(TAG_RELEASE));
                            year = year.substring(4, 8);
                        }

                        if (c.has(TAG_DELTA)) {
                            delta = c.getString(TAG_DELTA);
                        }

                        String description= c.getString(TAG_DESCRIPTION);
                        String rating = c.getString(TAG_RATING);

                        if (c.has(TAG_POSTER_URL)) {
                            posterUrl = c.getString(TAG_POSTER_URL);
                        }

                        String plot = c.getString(TAG_PLOT);
                        String genre = c.getString(TAG_GENRE);
                        String votes = c.getString(TAG_VOTES);
                        String runTime = c.getString(TAG_RUNTIME);
                        String metaScore = c.getString(TAG_METASCORE);

                        String summery = d.getString(TAG_SUMMERY);
                        String country = d.getString(TAG_COUNTRY);

                        if (c.has(TAG_GALLERY_FULL)) {
                            galleryFullUrl = c.getJSONArray(TAG_GALLERY_FULL);
                        }

                        String trailerUrl;
                        String slate;

                        if (c.has(TAG_TRAILER))
                            trailerUrl = c.getString(TAG_TRAILER);
                        else
                            trailerUrl = "N/A";

                        if (d.has(TAG_SLATE))
                            slate = d.getString(TAG_SLATE);
                        else
                            slate = "N/A";

                        ImdbObject movie = new ImdbObject(title, String.valueOf(top), year, description,
                                rating, posterUrl, slate, summery, plot,
                                genre, votes, runTime, metaScore, delta, country,
                                trailerUrl, galleryFullUrl.toString());
                        Intent intent = new Intent(ImdbActivity.this, MovieDetail.class);
                        intent.putExtra(MovieDetail.IMDB_OBJECT, movie);
                        ActivityCompat.startActivity(ImdbActivity.this, intent, null);
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
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        SharedPreferences settings = getSharedPreferences("settings", 0);
        getMenuInflater().inflate(R.menu.imdb_menu, menu);

        Drawable drawable = toolbar.getOverflowIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), Color.BLACK);
            toolbar.setOverflowIcon(drawable);
        }

        MenuItem miniCard = menu.findItem(R.id.menu_miniCard);
        MenuItem ascending = menu.findItem(R.id.menu_ascending);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);
        mQueryTextView.setHint("movie title or cast name");
        miniCard.setChecked(settings.getBoolean("miniCard", true));
        ascending.setChecked(settings.getBoolean("ascending", false));

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
                Cursor cursor = (Cursor)searchView.getSuggestionsAdapter().getItem(position);
                String feedName = cursor.getString(1);
                searchView.setQuery(feedName, false);
                return true;
            }
        });

        searchView.setSuggestionsAdapter(mAdapter);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        /*//------------------------------//
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_trending_down);
        v.setScaleX(0.8f);
        v.setScaleY(0.8f);
        //------------------------------//*/

        MenuItem mSearchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        int searchImgId = android.support.v7.appcompat.R.id.search_button;
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageResource(R.drawable.ic_action_search);
        v.setColorFilter(Color.BLACK);

        int searchTextViewId = android.support.v7.appcompat.R.id.search_src_text;
        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(searchTextViewId);
        searchTextView.setHintTextColor(getResources().getColor(R.color.material_lime_500));
        searchTextView.setTextColor(getResources().getColor(android.R.color.white));
        searchTextView.setTextSize(18.0f);

        SpannableStringBuilder ssb = new SpannableStringBuilder("   "); // for the icon
        ssb.append("movie title or cast name");
        Drawable searchIcon = getResources().getDrawable(R.drawable.ic_action_search);
        int textSize = (int) (searchTextView.getTextSize() * 1.25);
        searchIcon.setBounds(0, 0, textSize, textSize);
        ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchTextView.setHint(ssb);
        return super.onPrepareOptionsMenu(menu);
    }

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME});
        for (int i = 0; i < MOVIES.length; i++) {
            if (MOVIES[i].toLowerCase().contains(query.toLowerCase()))
                cursor.addRow(new Object[]{i, MOVIES[i]});
        }
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
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
