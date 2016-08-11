package com.github.florent37.materialviewpager.sample.genre;

import android.app.SearchManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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
import com.github.florent37.materialviewpager.sample.adapter.ImageCursorAdapter;
import com.github.florent37.materialviewpager.sample.adapter.upComingSwipeRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.fragment.RecyclerViewFragment;
import com.github.florent37.materialviewpager.sample.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;
import com.github.florent37.materialviewpager.sample.ui.BaseActivity;
import com.github.florent37.materialviewpager.sample.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.github.florent37.materialviewpager.sample.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/8/10.
 */
public class GenreDetailActivity extends BaseActivity implements Response.ErrorListener  {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<ImdbObject> movieList;
    private RecyclerView rvMovies;
    private Toolbar toolbar;
    private String HOST_NAME = Config.HOST_NAME;
    private upComingSwipeRecycleViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private boolean mActionBarShown = true;
    private int mProgressBarTopWhenActionBarShown;
    private int pageNum = 0;
    private int curSize = 0;
    private int offSet = 0;
    private JSONObject[] MOVIES = {};
    CustomJSONArrayRequest jsonRequest;
    private ImageCursorAdapter mAdapter;
    private RequestQueue mQueue;
    public static int [] monthList;
    // JSON Node names
    private final String TAG_TITLE = "title";
    private final String TAG_YEAR = "year";
    private final String TAG_DATA = "data";
    private final String TAG_RELEASE = "releaseDate";
    private final String TAG_TOP = "top";
    private final String TAG_POSTER_URL = "posterUrl";
    private final String TAG_RATING = "rating";
    private final String TAG_CAST = "cast";
    private final String TAG_DESCRIPTION = "description";
    private final String TAG_DETAIL_URL = "detailUrl";
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
    private SearchView searchView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_detail);
        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(getIntent().getStringExtra("genreType"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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
        textView.setBackgroundColor(Color.parseColor("#FF26C6DA"));
        textView.setLayoutParams(lParams);
        // 获得根视图并把TextView加进去。
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);
        loadHints();
        overridePendingTransition(0, 0);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_movie_layout);
        movieList = new ArrayList<>();
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        adapter = new upComingSwipeRecycleViewAdapter(this, movieList);
        rvMovies = (RecyclerView) findViewById(R.id.recyclerView);
        rvMovies.setLayoutManager(linearLayoutManager);
        rvMovies.setAdapter(adapter);
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();

        rvMovies.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                SharedPreferences settings = getSharedPreferences("settings", 0);
                Boolean Small = settings.getBoolean("miniCard", true);

                if (Small)
                    adapter.visibleThreshold = 2;
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

        fetchMovies(false);
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

        return true;
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

    private void loadHints() {
        final String[] from = new String [] {FILM_NAME};
        final int[] to = new int[] { R.id.text1};
        final CustomJSONObjectRequest jsonRequest;

        mAdapter = new ImageCursorAdapter(this,
                R.layout.search_row,
                null,
                from,
                to,
                "upcoming");

        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();

        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "imdb_title", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = ((JSONObject) response).getJSONArray("contents");
                    MOVIES = getJsonObjectArray(contents);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(GenreDetailActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
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
    public boolean onPrepareOptionsMenu (Menu menu) {
        return true;
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

    public void bindAdapter() {
        rvMovies.setAdapter(adapter);
        rvMovies.scheduleLayoutAnimation();
        adapter.notifyDataSetChanged();
        return;
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

    private Comparator AscendingComparator = new Comparator<ImdbObject>() {
        @Override
        public int compare(ImdbObject o1, ImdbObject o2) {
            if (Integer.parseInt(o1.getYear()) > Integer.parseInt(o2.getYear())) {
                return 1;
            }
            else if (Integer.parseInt(o1.getYear()) < Integer.parseInt(o2.getYear())) {
                return -1;
            }
            return 0;
        }
    };

    private Comparator DescendingComparator = new Comparator<ImdbObject>() {
        @Override
        public int compare(ImdbObject o1, ImdbObject o2) {
            if (Integer.parseInt(o1.getYear()) < Integer.parseInt(o2.getYear())) {
                return 1;
            }
            else if (Integer.parseInt(o1.getYear()) > Integer.parseInt(o2.getYear())) {
                return -1;
            }
            return 0;
        }
    };

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

    @Override
    public void trySetupSwipeRefresh() {

        mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }

    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchMovies(true);
    }

    public void fetchMovies(final boolean swipe) {

        int count = adapter.getItemCount();
        LOGD("0810", "total count: " + String.valueOf(count));

        if (swipe) {
            mSwipeRefreshLayout.setRefreshing(true);
        } else {
            movieList.add(null);
            adapter.notifyItemInserted(movieList.size() - 1);
        }

        if (count < 500) {
            jsonRequest = new CustomJSONArrayRequest(Config.HOST_NAME + "genre?type="+getIntent().getStringExtra("genreType")+"&page="+pageNum, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        pageNum++;
                        mSwipeRefreshLayout.setRefreshing(false);
                        buildImdbModel(response);
//                    genreList = blockUtils.moarItems(20); //TODO genre info insert in items ready
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, this);
            mQueue.add(jsonRequest);
        } else {
            movieList.remove(movieList.size() - 1);
            adapter.notifyItemRemoved(movieList.size());
        }
    }

    private int getReleaseDate(int roll, int day) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        c.roll(Calendar.MONTH, roll);
        c.set(Calendar.DAY_OF_MONTH, day);
        String str = df.format(c.getTime());
        String [] parts = TextUtils.split(str, "/");
        Log.d("0606", TextUtils.join("", parts));
        return Integer.parseInt(TextUtils.join("", parts));
    }

    private void buildImdbModel(JSONArray contents) throws JSONException {

        if (!adapter.swipe) {
            movieList.remove(movieList.size() - 1);
            adapter.notifyItemRemoved(movieList.size());
        }

        for (int i = 0; i < contents.length(); i++) {
            JSONObject c = contents.getJSONObject(i);
            JSONArray data = new JSONArray();
            String title = c.getString(TAG_TITLE);
            JSONObject d = null;

            if (c.has("detailContent")) {
                d = c.getJSONObject("detailContent");
            }

            int top = 0;
            String detailPosterUrl = "";
            String detailUrl = "";
            String year = "";
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

            if (c.has(TAG_DATA))
                data = c.getJSONArray(TAG_DATA);

            year = c.has(TAG_YEAR) ? c.getString(TAG_YEAR) : c.getString("releaseDate");

            /*if (c.has(TAG_RELEASE) && !c.has(TAG_TOP)) {
                year = String.valueOf(c.getInt(TAG_RELEASE));
                year = year.substring(4, 8);
            }*/

            /*if (c.has(TAG_DELTA)) {
                delta = c.getString(TAG_DELTA);
            }*/

            String description= c.getString(TAG_DESCRIPTION);
            String rating = c.getString(TAG_RATING);

            if (c.has(TAG_POSTER_URL)) {
                posterUrl = c.getString(TAG_POSTER_URL);
            }

            if (c.has(TAG_CAST)) {
                cast = c.getJSONArray(TAG_CAST);
            }

            if (c.has(TAG_DETAIL_URL))
                detailUrl = c.getString(TAG_DETAIL_URL);

            String plot = c.has(TAG_PLOT) ? c.getString(TAG_PLOT) : c.getString("story");
            String genre = c.has(TAG_GENRE) ? c.getString(TAG_GENRE) : "";
            String votes = c.has(TAG_VOTES) ? c.getString(TAG_VOTES) : "";
            String runTime = c.has(TAG_RUNTIME) ? c.getString(TAG_RUNTIME) : "";
            String metaScore = c.has(TAG_METASCORE) ? c.getString(TAG_METASCORE) : "";

            String summery = d != null ? d.getString(TAG_SUMMERY) : c.getString("mainInfo");
            String country = d != null ? d.getString(TAG_COUNTRY) : c.getString(TAG_COUNTRY);

            if (c.has(TAG_GALLERY_FULL)) {
                galleryFullUrl = c.getJSONArray(TAG_GALLERY_FULL);
            }

            if (runTime.compareTo("") == 0) {
                JSONObject jsonObj = new JSONObject();
                jsonObj = (JSONObject) data.get(4);
                runTime = jsonObj.getString("data");
            }

            if (genre.compareTo("") == 0)
                genre = c.getString("genre");

            String trailerUrl;
            String slate;

            if (c.has(TAG_TRAILER))
                trailerUrl = c.getString(TAG_TRAILER);
            else
                trailerUrl = "N/A";

            if (d != null)
                slate = d.has(TAG_SLATE) ? d.getString(TAG_SLATE) : "N/A";
            else
                slate = "N/A";

            ImdbObject item = new ImdbObject(title, String.valueOf(top), year, description,
                    rating, posterUrl, slate, summery, plot,
                    genre, votes, runTime, metaScore, delta, country,
                    trailerUrl, cast.toString(), galleryFullUrl.toString(), detailUrl);

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

        adapter.setLoaded();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_GENRE;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }
}
