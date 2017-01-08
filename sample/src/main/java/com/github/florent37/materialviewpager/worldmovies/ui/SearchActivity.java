/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.florent37.materialviewpager.worldmovies.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.ImageCursorAdapter;
import com.github.florent37.materialviewpager.worldmovies.framework.ContentWebViewActivity;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.genre.GenreDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.imdb.MovieDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.provider.ScheduleContract;
import com.github.florent37.materialviewpager.worldmovies.util.BuildModelUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;

//import com.github.florent37.materialviewpager.worldmovies.util.AnalyticsHelper;

public class SearchActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final String TAG = makeLogTag("SearchActivity");
    private static final String SCREEN_LABEL = "Search";
    private static final String ARG_QUERY = "query";
    private SearchView mSearchView;
    private ImageView closeButton;
    private String mQuery = "";
    private ListView mSearchResults;
    private boolean Initail = false;
    private boolean isSearching = false;
    private boolean reachBootom = false;
    private boolean isGenreCursor = true;
    private SimpleCursorAdapter mResultsAdapter;
    private Handler completeHandler;
    private ImageCursorAdapter cursorAdapter;
    private JSONObject[] MOVIES = {};
    private String searchChannel = "12";
    private String scrollId = "";
    private CustomJSONObjectRequest jsonRequest;
    private RequestQueue mQueue;
    private String[] from = new String [] {FILM_NAME};
    private int[] to = new int[] {-1000303};
    private int lastSelectedPosition = 0;
    private ProgressBar mProgressBar;
    private ProgressBar mSearchfooter;
    private String lauchBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mSearchView = (SearchView) findViewById(R.id.search_view);
        setupSearchView();
        mSearchResults = (ListView) findViewById(R.id.search_results);
        mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
        cursorAdapter = new ImageCursorAdapter(this, R.layout.search_row, null, from, to, "main");
        mResultsAdapter = new SimpleCursorAdapter(this, R.layout.list_item_search_result, null,
                new String[]{ScheduleContract.SearchTopicSessionsColumns.TAG_OR_SESSION_ID},
                new int[]{R.id.search_result}, 0);
        mSearchResults.setAdapter(mResultsAdapter);
        mSearchResults.setOnItemClickListener(this);
        mSearchfooter = new ProgressBar(this);
        mSearchResults.setOnScrollListener(scrollListener);
        Toolbar toolbar = getActionBarToolbar();
        Drawable up = DrawableCompat.wrap(ContextCompat.getDrawable(this, R.drawable.ic_up));
        DrawableCompat.setTint(up, getResources().getColor(R.color.app_body_text_2));
        closeButton = (ImageView) mSearchView.findViewById(R.id.search_close_btn);

        completeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mQuery = (String) msg.obj;
                giveSuggestions(mQuery);
            }
        };

        toolbar.setNavigationIcon(up);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateUpOrBack(SearchActivity.this, null);
            }
        });

        String query = getIntent().getStringExtra(SearchManager.QUERY);
        lastSelectedPosition = getIntent().getIntExtra("lastSelectedPosition",3);
        lauchBy = getIntent().getStringExtra("lauchBy");
        mQuery = query == null ? "" : query;

        switch (lauchBy) {
            case "main":
                cursorAdapter = new ImageCursorAdapter(getApplicationContext(), R.layout.search_row, null, from, to, "main");
                mSearchfooter.setBackgroundColor(getResources().getColor(R.color.primary_dark_material_dark));
                break;
            case "imdb":
                cursorAdapter = new ImageCursorAdapter(getApplicationContext(), R.layout.search_row, null, from, to, "imdb");
                mSearchfooter.setBackgroundColor(getResources().getColor(R.color.imdb_yellow));
                break;
            case "genre":
                cursorAdapter = new ImageCursorAdapter(getApplicationContext(), R.layout.search_row, null, from, to, "genre");
                mSearchfooter.setBackgroundColor(getResources().getColor(R.color.material_blue_300));
                break;
            case "upcoming":
                cursorAdapter = new ImageCursorAdapter(getApplicationContext(), R.layout.search_row, null, from, to, "upcoming");
                mSearchfooter.setBackgroundColor(getResources().getColor(R.color.tab_background));
                break;
            default:
                cursorAdapter = new ImageCursorAdapter(getApplicationContext(), R.layout.search_row, null, from, to, "detail");
                mSearchfooter.setBackgroundColor(getResources().getColor(R.color.primary_dark_material_dark));
                break;
        }

        if (mSearchView != null)
            mSearchView.setQuery(query, false);

        if (!mQuery.equals(""))
            searchFor(query);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            doEnterAnim();

        overridePendingTransition(0, 0);
    }

    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if(lastInScreen == totalItemCount && !isSearching && Initail && !reachBootom && !isGenreCursor && searchChannel.equals("16")) {
                mSearchResults.addFooterView(mSearchfooter);
                loadMoreItems();
                isSearching = true;
            }
        }
    };

    private void loadMoreItems() {
        LOGD("1231", "loadMoreItems");
        String url;
        final MatrixCursor cursor;
        mQueue = CustomVolleyRequestQueue.getInstance(SearchActivity.this).getRequestQueue();
        try {
            cursor = (MatrixCursor) cursorAdapter.getCursor();
            url = Config.HOST_NAME + "search/" + searchChannel + "/" + URLEncoder.encode(mQuery, "UTF-8") + "/" + scrollId;
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url , new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject[] movies = {};
                    String posterUrl, description;
                    int length = MOVIES.length;
                    LOGD("0107", String.valueOf(length));
                    JSONArray contents = response.getJSONArray("search");
                    scrollId = response.getString("scrollId");
                    movies = getJsonObjectArray(contents);
                    JSONObject[] mMOVIES = new JSONObject[movies.length +MOVIES.length];

                    for (int i=0; i<MOVIES.length; i++) {
                        mMOVIES[i] = MOVIES[i];
                    }

                    if (movies.length == 0) {
                        reachBootom = true;
                        isSearching = false;
                        mSearchResults.removeFooterView(mSearchfooter);
                        return;
                    }

                    for (int i = 0; i < movies.length; i++) {
                        mMOVIES[length+i] = movies[i];
                        JSONObject obj = movies[i].getJSONObject("_source");
                        posterUrl = obj.has("posterUrl") ? obj.getString("posterUrl") : "http://img.eiga.k-img.com/images/person/noimg/400.png?1423551130";
                        description = obj.has("description") ? obj.getString("description") : obj.getString("date");
                        cursor.addRow(new Object[]{i, obj.getString("title"), description, posterUrl});
                    }

                    MOVIES = new JSONObject[mMOVIES.length];
                    MOVIES = mMOVIES;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                isSearching = false;
                mSearchResults.removeFooterView(mSearchfooter);
                cursorAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MainActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    private void giveSuggestions(String query) {
        LOGD("1231", "giveSuggestions");
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME, FILM_DESCRIPTION, FILM_POSTER});
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();
        cursorAdapter.changeCursor(cursor);
        String url;
        searchChannel = CredentialsHandler.getCountry(this);
        try {
            closeButton.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            url = Config.HOST_NAME + "search/"+ searchChannel+"/" + URLEncoder.encode(query, "UTF-8") + "/" + scrollId;
            LOGD("1231", url);
        }  catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url , new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject[] movies = {};
                    JSONArray contents = response.getJSONArray("search");
                    scrollId = response.getString("scrollId");
                    movies = getJsonObjectArray(contents);
                    String posterUrl, description;
                    MOVIES = new JSONObject[movies.length];
                    for (int i = 0; i < movies.length; i++) {
                        MOVIES[i] = movies[i];
                        JSONObject obj = movies[i].getJSONObject("_source");
                        posterUrl = obj.has("posterUrl") ? obj.getString("posterUrl") : "http://img.eiga.k-img.com/images/person/noimg/400.png?1423551130";
                        description = obj.has("description") ? obj.getString("description") : obj.getString("date");
                        cursor.addRow(new Object[]{i, obj.getString("title"), description, posterUrl});
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSearchResults.setAdapter(cursorAdapter);
                mSearchResults.setVisibility(cursor.getCount() > 0 ? View.VISIBLE : View.GONE);
                mProgressBar.setVisibility(View.GONE);
                closeButton.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(MainActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    /**
     * As we only ever want one instance of this screen, we set a launchMode of singleTop. This
     * means that instead of re-creating this Activity, a new intent is delivered via this callback.
     * This prevents multiple instances of the search dialog 'stacking up' e.g. if you perform a
     * voice search.
     *
     * See: http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                searchFor(query);
                completeHandler.removeMessages(MESSAGE_TEXT_CHANGE);
                completeHandler.sendMessageDelayed(completeHandler.obtainMessage(MESSAGE_TEXT_CHANGE, query), mAutoCompleteDelay);
                mSearchView.setQuery(query, false);
            }
        }
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setFocusable(true);
        mSearchView.setIconified(false);
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.requestFocusFromTouch();
        // Set the query hint.
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mSearchView.setImeOptions(mSearchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                mSearchView.setVisibility(View.INVISIBLE);
                mSearchView.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                scrollId = "";
                reachBootom = false;
                if (TextUtils.isEmpty(query)) {
                    searchFor(query);
                    isGenreCursor = true;
                } else {
                    isGenreCursor = false;
                    completeHandler.removeMessages(MESSAGE_TEXT_CHANGE);
                    completeHandler.sendMessageDelayed(completeHandler.obtainMessage(MESSAGE_TEXT_CHANGE, query), mAutoCompleteDelay);
                }
                return true;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                dismiss(null);
                return false;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                final String feedName = cursor.getString(1);
                mSearchView.post(new Runnable() {
                    @Override
                    public void run() {
                        mSearchView.setQuery(feedName, true);
                    }
                });
                return true;
            }
        });

        if (!TextUtils.isEmpty(mQuery))
            mSearchView.setQuery(mQuery, false);
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    public void dismiss(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            doExitAnim();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    /**
     * On Lollipop+ perform a circular reveal animation (an expanding circular mask) when showing
     * the search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doEnterAnim() {
        // Fade in a background scrim as this is a floating window. We could have used a
        // translucent window background but this approach allows us to turn off window animation &
        // overlap the fade with the reveal animation – making it feel snappier.
        View scrim = findViewById(R.id.scrim);
        scrim.animate()
                .alpha(1f)
                .setDuration(500L)
                .setInterpolator(
                        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in))
                .start();

        // Next perform the circular reveal on the search panel
        final View searchPanel = findViewById(R.id.search_panel);
        if (searchPanel != null) {
            // We use a view tree observer to set this up once the view is measured & laid out
            searchPanel.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    searchPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                    // As the height will change once the initial suggestions are delivered by the
                    // loader, we can't use the search panels height to calculate the final radius
                    // so we fall back to it's parent to be safe
                    int revealRadius = ((ViewGroup) searchPanel.getParent()).getHeight();
                    // Center the animation on the top right of the panel i.e. near to the
                    // search button which launched this screen.
                    Animator show = ViewAnimationUtils.createCircularReveal(searchPanel,
                        searchPanel.getRight(), searchPanel.getTop(), 0f, revealRadius);
                    show.setDuration(250L);
                    show.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                            android.R.interpolator.fast_out_slow_in));
                    show.start();
                    return false;
                }
            });
        }
    }

    /**
     * On Lollipop+ perform a circular animation (a contracting circular mask) when hiding the
     * search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doExitAnim() {
        final View searchPanel = findViewById(R.id.search_panel);
        // Center the animation on the top right of the panel i.e. near to the search button which
        // launched this screen. The starting radius therefore is the diagonal distance from the top
        // right to the bottom left
        int revealRadius = (int) Math.sqrt(Math.pow(searchPanel.getWidth(), 2)
                + Math.pow(searchPanel.getHeight(), 2));
        // Animating the radius to 0 produces the contracting effect
        Animator shrink = ViewAnimationUtils.createCircularReveal(searchPanel,
                searchPanel.getRight(), searchPanel.getTop(), revealRadius, 0f);
        shrink.setDuration(200L);
        shrink.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                android.R.interpolator.fast_out_slow_in));
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchPanel.setVisibility(View.INVISIBLE);
                ActivityCompat.finishAfterTransition(SearchActivity.this);
            }
        });
        shrink.start();

        // We also animate out the translucent background at the same time.
        findViewById(R.id.scrim).animate()
                .alpha(0f)
                .setDuration(200L)
                .setInterpolator(
                        AnimationUtils.loadInterpolator(SearchActivity.this,
                                android.R.interpolator.fast_out_slow_in))
                .start();
    }

    private void searchFor(String query) {
        // ANALYTICS EVENT: Start a search on the Search activity
        // Contains: Nothing (Event params are constant:  Search query not included)
        Bundle args = new Bundle(1);
        if (query == null) {
            query = "";
        }
        args.putString("query", query);
        if (!Initail) {
            LOGD("1103", "initLoader");
            getLoaderManager().initLoader(SearchTopicsSessionsQuery.TOKEN, args, this);
            Initail = true;
        } else {
            LOGD("1103", "restartLoader");
            getLoaderManager().restartLoader(SearchTopicsSessionsQuery.TOKEN, args, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                ScheduleContract.SearchTopicsSessions.CONTENT_URI,
                SearchTopicsSessionsQuery.PROJECTION,
                null, new String[] {args.getString(ARG_QUERY)}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mResultsAdapter.swapCursor(cursor);
        mSearchResults.setAdapter(mResultsAdapter);
        mSearchResults.setVisibility(cursor.getCount() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mResultsAdapter.getCursor();
        cursor.moveToPosition(position);

        if (cursorAdapter.getCursor() == null) {
            String tagOrSessionId = cursor.getString(SearchTopicsSessionsQuery.TAG_OR_SESSION_ID);
            String searchYear = CredentialsHandler.getSearchYear(this);
            LOGD("1207", "onItemClik " + searchYear);
            Intent intent = new Intent(SearchActivity.this, GenreDetailActivity.class);
            intent.putExtra("genreType", tagOrSessionId);
            intent.putExtra("lastSelectedPosition", lastSelectedPosition);
            intent.putExtra("searchYear", searchYear);
            ActivityCompat.startActivity(SearchActivity.this, intent, null);
        } else {
            if (!searchChannel.equals("16")) {
                Cursor imageCusor = cursorAdapter.getCursor();
                int titleCol = imageCusor.getColumnIndex("filmName");
                LOGD("1103", "title " + imageCusor.getString(titleCol) + "\n" + lauchBy);
                requestDataRefresh(imageCusor.getString(titleCol));
            } else {
                Intent intent = new Intent(this, ContentWebViewActivity.class);
                try {
//                    JSONObject obj = MOVIES.get(position).getJSONObject("_source");
                    JSONObject obj = (JSONObject) MOVIES[position];
                    LOGD("1211", obj.getJSONObject("_source").getString("link"));
                    intent.putExtra("url", obj.getJSONObject("_source").getString("link"));
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private interface SearchTopicsSessionsQuery {
        int TOKEN = 0x4;
        String[] PROJECTION = ScheduleContract.SearchTopicsSessions.DEFAULT_PROJECTION;
        int _ID = 0;
        int TAG_OR_SESSION_ID = 1;
        int SEARCH_SNIPPET = 2;
        int IS_TOPIC_TAG = 3;
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;
        mQueue = CustomVolleyRequestQueue.getInstance(SearchActivity.this).getRequestQueue();
        CustomJSONObjectRequest jsonRequest_q = null;
        String url = null;
        String searchChannel = CredentialsHandler.getCountry(this);

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");
                url= Config.HOST_NAME + "world/"+searchChannel+"?title=" + Query + "&ascending=1"; //TODO search by country with year
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }

            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray contents = response.getJSONArray("contents");
                        ImdbObject item = BuildModelUtils.buildImdbModel(contents);
                        Intent intent = new Intent(SearchActivity.this, MovieDetailActivity.class);
                        intent.putExtra(ImdbActivity.IMDB_OBJECT, item);
                        ActivityCompat.startActivity(SearchActivity.this, intent, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SearchActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
                }
            });
            mQueue.add(jsonRequest_q);
            return;
        }

        mQueue.add(jsonRequest); //trigger volley request
    }
}
