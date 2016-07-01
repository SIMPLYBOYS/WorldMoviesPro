package com.github.florent37.materialviewpager.sample.upcoming;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
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
import com.github.florent37.materialviewpager.sample.fragment.RecyclerViewFragment;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;
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

/**
 * Created by aaron on 2016/6/16.
 */
public class upComingActivity extends ImdbActivity implements Response.Listener, Response.ErrorListener  {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<ImdbObject> movieList;
    private RecyclerView rvMovies;
    private Toolbar toolbar;
    private ImdbSwipeRecycleViewAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private static String[] MOVIES = {};
    int curSize = 0;
    CustomJSONObjectRequest jsonRequest;
    private SimpleCursorAdapter mAdapter;
    private RequestQueue mQueue;
    public static int [] monthList;
    // JSON Node names
    private static final String TAG_TITLE = "title";
    private static final String TAG_YEAR = "year";
    private static final String TAG_RELEASE = "releaseDate";
    private static final String TAG_TOP = "top";
    private static final String TAG_POSTER_URL = "posterUrl";
    private static final String TAG_RATING = "rating";
    private static final String TAG_DESCRIPTION = "description";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerHideableHeaderView(findViewById(R.id.headerbar));
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
                Toast.makeText(upComingActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
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
    public void trySetupSwipeRefresh() {

        mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        Log.d("0625", String.valueOf(adapter.getItemCount()));

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        if (adapter.getItemCount() == 0) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mQueue = CustomVolleyRequestQueue.getInstance(upComingActivity.this).getRequestQueue();
                    CustomJSONObjectRequest jsonRequest_q = null;
                    jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/monthList", new JSONObject(), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray contents = response.getJSONArray("contents");
                                if (contents != null && monthList == null) {
                                    try {
                                        monthList = new int[12];
                                        for (int i = 0; i < contents.length(); i++) {
                                            monthList[i] = contents.getInt(i);
                                        }
                                    } catch (JSONException e) {
                                        Log.e("App", "unexpect JSON exception", e);
                                    }
                                }
                                mSwipeRefreshLayout.setRefreshing(true);
                                fetchMovies(true);
                                adapter.swipe = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(upComingActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mQueue.add(jsonRequest_q);

                }
            });
        }

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }

    }

    @Override
    public void fetchMovies(final boolean swipe) {
        // showing refresh animation before making http call
        if (swipe)
            mSwipeRefreshLayout.setRefreshing(true);
        else {
            movieList.add(null);
            adapter.notifyItemInserted(movieList.size() - 1);
        }

        int count = adapter.getItemCount();
        Calendar c = Calendar.getInstance();
        Log.d("0616", "total count: " + String.valueOf(count));

        if (monthList == null)
            return; //skip fetching upon network not stable.

        if (count < monthList[c.get(Calendar.MONTH)]) {
            Log.d("0607", "count: " + count + " " + String.valueOf(getReleaseDate(0, 1)) +' ' +String.valueOf(getReleaseDate(0, 30)));
            jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                    "/imdb?release_from=" + getReleaseDate(0, 1) + "&release_to=" + getReleaseDate(0, 30), new JSONObject(), this, this);
        } else if (count < monthList[c.get(Calendar.MONTH)] + monthList[c.get(Calendar.MONTH)+1]) {
            Log.d("0607", "count: " + count + " " + String.valueOf(getReleaseDate(1, 1)) +' ' +String.valueOf(getReleaseDate(1, 31)));
            jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                    "/imdb?release_from=" + getReleaseDate(1, 1) + "&release_to=" + getReleaseDate(1, 31), new JSONObject(), this, this);
        } else if (count < monthList[c.get(Calendar.MONTH)] + monthList[c.get(Calendar.MONTH)+1] + monthList[c.get(Calendar.MONTH)+2]) {
            Log.d("0607", "count: " + count + " " + String.valueOf(getReleaseDate(2, 1)) +' ' +String.valueOf(getReleaseDate(2, 31)));
            jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                    "/imdb?release_from=" + getReleaseDate(2, 1) + "&release_to=" + getReleaseDate(2, 31), new JSONObject(), this, this);
        } else if (count < monthList[c.get(Calendar.MONTH)] + monthList[c.get(Calendar.MONTH)+1] + monthList[c.get(Calendar.MONTH)+2] + monthList[c.get(Calendar.MONTH)+3]) {
            Log.d("0607", "count: " + count + " " + String.valueOf(getReleaseDate(3, 1)) +' ' +String.valueOf(getReleaseDate(3, 30)));
            jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                    "/imdb?release_from=" + getReleaseDate(3, 1) + "&release_to=" + getReleaseDate(3, 30), new JSONObject(), this, this);
        } else if (count < monthList[c.get(Calendar.MONTH)] + monthList[c.get(Calendar.MONTH)+1] + monthList[c.get(Calendar.MONTH)+2] + monthList[c.get(Calendar.MONTH)+3] + monthList[c.get(Calendar.MONTH)+4]) {
            Log.d("0607", "count: " + count + " " + String.valueOf(getReleaseDate(4, 1)) +' ' +String.valueOf(getReleaseDate(4, 31)));
            jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                    "/imdb?release_from=" + getReleaseDate(4, 1) + "&release_to=" + getReleaseDate(4, 31), new JSONObject(), this, this);
        }

        if (count > monthList[c.get(Calendar.MONTH)] +
                monthList[c.get(Calendar.MONTH)+1] +
                monthList[c.get(Calendar.MONTH)+2] +
                monthList[c.get(Calendar.MONTH)+3] +
                monthList[c.get(Calendar.MONTH)+4]) {
            movieList.remove(movieList.size() - 1);
            adapter.notifyItemRemoved(movieList.size());
        } else {
            mQueue.add(jsonRequest);
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

    @Override
    public void
    onResponse(Object response) {
        try {
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            boolean byTitle = ((JSONObject) response).getBoolean("byTitle");
            buildImdbModel(contents, byTitle);
            mSwipeRefreshLayout.setRefreshing(false);

        } catch (JSONException e) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(upComingActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void buildImdbModel(JSONArray contents, boolean byTitle) throws JSONException {
        if (!adapter.swipe) {
            movieList.remove(movieList.size() - 1);
            adapter.notifyItemRemoved(movieList.size());
        }
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
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_UP_COMING;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }
}
