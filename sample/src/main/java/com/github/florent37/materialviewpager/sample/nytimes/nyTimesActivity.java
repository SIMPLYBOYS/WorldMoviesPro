package com.github.florent37.materialviewpager.sample.nytimes;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.NyTimesSwipeRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.fragment.MovieRecycleFragment;
import com.github.florent37.materialviewpager.sample.framework.WebViewActivity;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.ui.BaseActivity;
import com.github.florent37.materialviewpager.sample.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by aaron on 2016/6/10.
 */
public class nyTimesActivity extends BaseActivity implements Response.ErrorListener {
    private Toolbar toolbar;

    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;

    private List<Movie> movieList;

    private RecyclerView rvMovies;

    public static final String TAG = "nyTimesActivity";

    public static final String FILM_NAME = "filmName";

    private RequestQueue mQueue;

    private NyTimesSwipeRecycleViewAdapter rAdapter;

    private SimpleCursorAdapter mAdapter;

    private LinearLayoutManager linearLayoutManager;

    ShareActionProvider shareActionProvider;

    private MenuItem searchItem;

    private SearchView searchView = null;

    private SharedPreferences sp;

    int curSize = 0;

    public static final String REQUEST_TAG = "reviewRequest";

    private boolean mActionBarShown = true;

    private static String[] MOVIES = {};

    private int mProgressBarTopWhenActionBarShown;

    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;

    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Set<MovieRecycleFragment> mMovieRecycleFragments = new HashSet<MovieRecycleFragment>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nytimes);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle("NY Movies Review");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
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
        textView.setBackgroundColor(Color.parseColor("#ff212121"));
        textView.setLayoutParams(lParams);
        // 获得根视图并把TextView加进去。
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);
        loadHints();
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void trySetupSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_movie_layout);
        movieList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        rAdapter = new NyTimesSwipeRecycleViewAdapter(this, movieList);
        rvMovies = (RecyclerView) findViewById(R.id.recyclerView);
        rvMovies.setLayoutManager(linearLayoutManager);
        rvMovies.setAdapter(rAdapter);

        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();

        rvMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);

                rAdapter.totalItemCount = linearLayoutManager.getItemCount();
                rAdapter.lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!rAdapter.loading && rAdapter.totalItemCount <= (rAdapter.lastVisibleItem + rAdapter.visibleThreshold)) {
                    // End has been reached
                    // Do something
                    fetchMovies(false);
                    rAdapter.loading = true;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                fetchMovies(true);
            }
        });

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }

    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            Log.d("0612", "onDestroy");
        }
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchMovies(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        toolbar.setDrawingCacheBackgroundColor(Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.nytimes_menu, menu);

        for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            }
        }

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);

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

    private void onShareAction() {
        shareActionProvider.setShareIntent(createShareIntent());
        return;
    }

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME});
        for (int i = 0; i < MOVIES.length; i++) {
            if (MOVIES[i].toLowerCase().contains(query.toLowerCase()))
                cursor.addRow(new Object[]{i, MOVIES[i]});
        }
        mAdapter.changeCursor(cursor);
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://www.imdb.com/");
        return shareIntent;
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
            rAdapter.notifyItemInserted(movieList.size() - 1);
        }

        Log.d("0612","offSet: " + offSet);
        // appending offset to url
        String url = Config.URL_NY_TIMES + "offset=" + offSet + "&api-key=" + Config.NYTimesKey;

        CustomJSONObjectRequest jsonRequest_q = null;
        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!swipe) {
                        movieList.remove(movieList.size() - 1);
                        rAdapter.notifyItemRemoved(movieList.size());
                    }
                    JSONArray contents = response.getJSONArray("results");
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
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
                        Movie m = new Movie(head, date, summery, linkUrl, picUrl);
                        curSize = rAdapter.getItemCount();
                        movieList.add(movieList.size(), m);
                        if (rAdapter != null) {
                            rAdapter.notifyItemInserted(movieList.size());
                        }
                    }
                    rAdapter.setLoaded();
                    /*if(rAdapter != null) {
                        rAdapter.notifyItemRangeInserted(curSize, movieList.size()-1);
                    }*/

                    offSet += 20;
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
                Toast.makeText(nyTimesActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest_q);
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;

        mQueue = CustomVolleyRequestQueue.getInstance(nyTimesActivity.this)
                .getRequestQueue();
        CustomJSONObjectRequest jsonRequest_q = null;
        String url = null;

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");
                url= Config.URL_NY_TIMES + "query=" + Query + "&api-key=" + Config.NYTimesKey;
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }
            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray contents = response.getJSONArray("results");
                        JSONObject movieObj = contents.getJSONObject(0);
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
                        Movie movie = new Movie(head, date, summery, linkUrl, picUrl);
                        Intent intent = new Intent(nyTimesActivity.this, WebViewActivity.class);
                        intent.putExtra("movie", movie);
                        ActivityCompat.startActivity(nyTimesActivity.this, intent, null);
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
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_NYTIMES;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {

        // Prevent the swipe refresh by returning true here
        if (mViewPagerScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
            return true;
        }

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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                onShareAction();
                return true;
            case R.id.menu_smooth_zero:
                rvMovies.smoothScrollToPosition(0);
                return true;
            case R.id.menu_smooth_end:
                rvMovies.smoothScrollToPosition(linearLayoutManager.getItemCount());
                return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void onSaveInstanceState(Bundle saveInstanceState){
        super.onSaveInstanceState(saveInstanceState);
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
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
                Toast.makeText(nyTimesActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    public static String[] getStringArray(JSONArray jsonArray) {
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

    // 获取手机状态栏高度
    public int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    // 获取ActionBar的高度
    public int getActionBarHeight() {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))// 如果资源是存在的、有效的
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
}
