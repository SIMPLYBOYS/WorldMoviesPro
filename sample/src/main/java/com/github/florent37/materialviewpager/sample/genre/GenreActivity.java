package com.github.florent37.materialviewpager.sample.genre;

import android.content.Intent;
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
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.ImageCursorAdapter;
import com.github.florent37.materialviewpager.sample.fragment.MovieRecycleFragment;
import com.github.florent37.materialviewpager.sample.framework.AGVRecyclerViewAdapter;
import com.github.florent37.materialviewpager.sample.framework.AsymmetricItem;
import com.github.florent37.materialviewpager.sample.framework.AsymmetricRecyclerView;
import com.github.florent37.materialviewpager.sample.framework.AsymmetricRecyclerViewAdapter;
import com.github.florent37.materialviewpager.sample.framework.SpacesItemDecoration;
import com.github.florent37.materialviewpager.sample.framework.Utils;
import com.github.florent37.materialviewpager.sample.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.BlockItem;
import com.github.florent37.materialviewpager.sample.ui.BaseActivity;
import com.github.florent37.materialviewpager.sample.ui.widget.MultiSwipeRefreshLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.florent37.materialviewpager.sample.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/7/13.
 */
public class GenreActivity extends BaseActivity implements Response.ErrorListener {
    private final String TAG_TITLE = "title";
    private final String TAG_YEAR = "year";
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
    public final String REQUEST_TAG = "genreRequest";
    private Toolbar toolbar;
    public final String TAG = "genresActivity";
    private MenuItem searchItem;
    private SearchView searchView = null;
    private ImageCursorAdapter mAdapter;
    private RequestQueue mQueue;
    private List<BlockItem> genreList;
    private int pageNum = 0;
    CustomJSONArrayRequest jsonRequest;
    GenreSwipeRecyclerViewAdapter genreAdapter;
    private GenreActivity activity;
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private JSONObject[] MOVIES = {};
    private int offSet = 0;
    private ProgressBar mProgressBar;
    AsymmetricRecyclerView recyclerView;

    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Set<MovieRecycleFragment> mMovieRecycleFragments = new HashSet<MovieRecycleFragment>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGD("0810", "GenreActivity_onCreate");
        activity = this;
        setContentView(R.layout.activity_genre);
        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mProgressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.material_blue_300), PorterDuff.Mode.SRC_ATOP);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle(R.string.description_genre);
        toolbar.setTitleTextColor(Color.BLACK);
        genreList = new ArrayList<>();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);
        registerHideableHeaderView(findViewById(R.id.headerbar));
        recyclerView = (AsymmetricRecyclerView) findViewById(R.id.recyclerView);
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        textView.setBackgroundColor(Color.parseColor("#ff29b6f6"));//TODO string
        textView.setLayoutParams(lParams);

        // 获得根视图并把TextView加进去。
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_movie_layout);

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

        loadHints(); //TODO call searchAPI
        fetchGenresTopic(false);
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

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_GENRE;
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        fetchGenresTopic(true);
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
        searchTextView.setHintTextColor(getResources().getColor(R.color.material_blue_100));
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

    /**
     * Fetching genre of movies
     * */
    public void fetchGenresTopic(final boolean swipe) {

        if (genreList.size() == 0) {
            jsonRequest = new CustomJSONArrayRequest(Config.HOST_NAME + "genre_topic", new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    JSONArray contents = ((JSONArray) response);
                    mProgressBar.setVisibility(View.GONE);
                    buildGenreModel(contents);
//                    genreList = blockUtils.moarItems(20); //TODO genre info insert in items ready
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(GenreActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
                }
            });
            mQueue.add(jsonRequest);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void buildGenreModel(JSONArray genreItems) {
        try {

            for (int i = 0; i < genreItems.length(); i++) {
                int colSpan = Math.random() < 0.3f ? 2 : 1;
                // Swap the next 2 lines to have items with variable
                // column/row span.
                int rowSpan;

                /*if (colSpan == 1)
                    rowSpan = colSpan;
                else*/
                    rowSpan = Math.random() < 0.5f ? 2 : 1;
//            int rowSpan = colSpan;
                BlockItem item = new BlockItem(colSpan, rowSpan, i);
                item.setTopic(genreItems.getJSONObject(i).getString("type"));

                if (genreItems.getJSONObject(i).has("imageUrl")) {
                    item.setImageUrl(genreItems.getJSONObject(i).getString("imageUrl"));
                }
                genreList.add(item);
            }

            genreAdapter = new GenreSwipeRecyclerViewAdapter(genreList);
            //        recyclerView.setBackgroundColor(Color.BLACK);
            recyclerView.setRequestedColumnCount(3);
            recyclerView.setDebugging(false);
            recyclerView.setRequestedHorizontalSpacing(Utils.dpToPx(GenreActivity.this, 1));
            recyclerView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_padding)));
            recyclerView.setAdapter(new AsymmetricRecyclerViewAdapter<>(GenreActivity.this, recyclerView, genreAdapter));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.genre_menu, menu);
        Drawable drawable = toolbar.getOverflowIcon();

        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), Color.BLACK);
            toolbar.setOverflowIcon(drawable);
        }

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);
        mQueryTextView.setHint("movie title or cast name");

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

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_share) {
            showShareDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showShareDialog() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, ""); //TODO app url
        startActivity(Intent.createChooser(intent, "Share"));
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
                "genre");

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
                Toast.makeText(GenreActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    class GenreSwipeRecyclerViewAdapter extends AGVRecyclerViewAdapter<ViewHolder> {
        private final List<BlockItem> items;

        public GenreSwipeRecyclerViewAdapter(List<BlockItem> items) {
            this.items = items;
        }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(parent, viewType);
        }

        @Override public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override public int getItemCount() {
            return items.size();
        }

        @Override public AsymmetricItem getItem(int position) {
            return items.get(position);
        }

        @Override public int getItemViewType(int position) {
//            return position % 2 == 0 ? 1 : 0;
            return items.get(position) == null ? 1: 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
                ImageView coverView;
        private int Type;
        public ViewHolder(ViewGroup parent, int viewType) {
            super(LayoutInflater.from(parent.getContext()).inflate(
                    viewType == 0 ? R.layout.genre_item_odd : R.layout.genre_item_even, parent, false));
            Type = viewType;
            if (viewType == 0) {
                textView = (TextView) itemView.findViewById(R.id.genre_topic);
                coverView =(ImageView) itemView.findViewById(R.id.cover_iv);
            } else {
                textView = (TextView) itemView.findViewById(R.id.textview_odd);
            }
        }
        public void bind(final BlockItem item) {
            final int position = item.getPosition();
            textView.setText(item.getTopic());
            if (Type == 0) {
                Picasso.with(coverView.getContext()).load(item.getImageUrl().compareTo("") == 0 ? "http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg" :
                item.getImageUrl()).placeholder(R.drawable.placeholder).centerCrop().fit()
                        .into(coverView);
                coverView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(activity, String.valueOf(position), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GenreActivity.this, GenreDetailActivity.class);
                        intent.putExtra("genreType", item.getTopic());
                        ActivityCompat.startActivity(GenreActivity.this, intent, null);
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }
}
