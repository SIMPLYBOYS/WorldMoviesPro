package com.github.florent37.materialviewpager.worldmovies.nytimes;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.MainActivity;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.genre.GenreActivity;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
import com.github.florent37.materialviewpager.worldmovies.util.ParserUtils;
import com.github.florent37.materialviewpager.worldmovies.util.PrefUtils;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import im.delight.android.webview.AdvancedWebView;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/6/12.
 */
public class nyTimesDetailActivity extends AppCompatActivity implements Response.ErrorListener, BottomNavigationBar.OnTabSelectedListener  {
    protected static final int NAV_ITEM_TREND = 0;
    protected static final int NAV_ITEM_UPCOMING = 1;
    protected static final int NAV_ITEM_IMDB = 2;
    protected static final int NAV_ITEM_NYTIMES = 3;
    protected static final int NAV_ITEM_GENRE = 4;
    private ProgressBar progressBar;
    private AdvancedWebView mWebView;
    private MenuItem searchItem, shareItem, bookmarkItem;
    private SimpleCursorAdapter mAdapter;
    private SearchView searchView = null;
    private Movie movie;
    private TextView description, headLine, story, editor, publish;
    private ImageView pictureView;
    private String HOST_NAME = Config.HOST_NAME;
    ShareActionProvider shareActionProvider;
    private RequestQueue mQueue;
    public static String FILM_NAME = "filmName";
    public static String REQUEST_TAG = "reviewRequest";
    private static String[] MOVIES = {};
    int lastSelectedPosition = 3;
    BottomNavigationBar bottomNavigationBar;
    BadgeItem numberBadgeItem;
    private FloatingActionButton fab;
    private nyTimesFavoritePreference favor;
    LinearLayout bookmarkActionView;
    private ShineButton bookmarkView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nytims_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        movie = (Movie) getIntent().getSerializableExtra("movie");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        editor = (TextView) findViewById(R.id.editor);
        editor.setText(movie.getEditor());
        publish = (TextView) findViewById(R.id.publish);
        publish.setText(movie.getDate());
        story = (TextView) findViewById(R.id.story);
        story.setText(movie.getSummary_short());
        story.setTextIsSelectable(true);
        description = (TextView) findViewById(R.id.description);
        description.setText(movie.getDescription());
        description.setTextIsSelectable(true);
        headLine = (TextView) findViewById(R.id.headline);
        headLine.setText(movie.getHeadline());
        headLine.setTextIsSelectable(true);
        pictureView = (ImageView) findViewById(R.id.picture);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();
        favor = new nyTimesFavoritePreference();

        if (movie.getPicUrl() != null) {
            Picasso.with(pictureView.getContext()).load(movie.getPicUrl()).placeholder(R.drawable.placeholder)
                    .into(pictureView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });
        } else {
            ViewGroup parent = (ViewGroup) pictureView.getParent();
            parent.removeView(pictureView);
            parent.removeView(progressBar);
        }

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setTitle(movie.getHeadline());
        }

        fab = (FloatingActionButton) findViewById(R.id.floating_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createShareAction();
            }
        });

        int primaryDark = getResources().getColor(R.color.primary_dark_material_dark);
        int primary = getResources().getColor(R.color.primary_material_light);
        fab.setBackgroundTintList(ColorStateList.valueOf(primaryDark));
        fab.setRippleColor(primary);
        refresh();
        bottomNavigationBar.setTabSelectedListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createShareAction() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, movie.getLink());
        // Launch sharing dialog for image
        startActivity(Intent.createChooser(shareIntent, "Share Review"));
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
                .addItem(new BottomNavigationItem(R.drawable.ic_theaters, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "NyTimes").setActiveColorResource(R.color.material_brown_400))
                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_red_900))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setBarBackgroundColor(R.color.foreground_material_light)
                .initialise();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.nytimes_detail_menu, menu);

        for (int i = 0; i < menu.size(); i++) {

            Drawable drawable = menu.getItem(i).getIcon();

            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            }
        }

        bookmarkActionView = (LinearLayout) getLayoutInflater().inflate(R.layout.bookmark_image, null);
        LOGD("0816", String.valueOf(movie.getBookmark()));
        bookmarkView = (ShineButton) bookmarkActionView.findViewById(R.id.bookmarkView);
        bookmarkView.init(this);
        bookmarkView.getLayoutParams().height=96;
        bookmarkView.getLayoutParams().width=96;
//        bookmarkView.setImageResource(R.drawable.ic_turned_in);
        bookmarkView.setColorFilter(getResources().getColor(R.color.app_white));
        bookmarkView.setScaleType(ImageView.ScaleType.FIT_XY);

        if (movie.getBookmark()) {
            bookmarkView.setChecked(true);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
        } else {
            bookmarkView.setChecked(false);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in);
        }

        shareItem = menu.findItem(R.id.action_share);
        searchItem = menu.findItem(R.id.action_search);
        bookmarkItem = menu.findItem(R.id.action_bookmark);
        bookmarkItem.setActionView(bookmarkView);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);
        mQueryTextView.setHint("movie title or cast name");

        bookmarkView.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                Toast.makeText(nyTimesDetailActivity.this, "Bookmark "+checked+" !!!", Toast.LENGTH_SHORT).show();

                if (checked && !movie.getBookmark()) {
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                    User user = PrefUtils.getCurrentUser(getApplicationContext());
                    movie.setBookmark(true);
                    String headline = movie.getHeadline().indexOf(":") != -1 ? movie.getHeadline().split(":")[1].trim() : movie.getHeadline();
                    CustomJSONObjectRequest jsonRequest_q = null;
                    String url = HOST_NAME + "nyTimes/"+user.facebookID;
                    JSONObject jsonBody = new JSONObject();

                    try {
                        jsonBody.put("headline", headline);
                        jsonBody.put("link", movie.getLink());
                        jsonBody.put("picUrl", movie.getPicUrl());
                        favor.addFavorite(getApplicationContext(), headline);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    jsonRequest_q = new CustomJSONObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String result = response.getString("content");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(nyTimesDetailActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mQueue.add(jsonRequest_q);
                } else if (!checked && movie.getBookmark()) {
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in);
                    movie.setBookmark(false);
                    User user = PrefUtils.getCurrentUser(getApplicationContext());
                    String headline = movie.getHeadline().indexOf(":") != -1 ? movie.getHeadline().split(":")[1].trim() : movie.getHeadline();
//                        String headline = movie.getHeadline();
                    favor.removeFavorite(getApplicationContext(), headline);
                    CustomJSONObjectRequest jsonRequest_q = null;
                    headline = ParserUtils.encode(headline);
                    String url = HOST_NAME + "nyTimes/"+user.facebookID+"/"+headline;
                    JSONObject jsonBody = new JSONObject();

                    /*try {
                        jsonBody.put("headline", headline);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/

                    jsonRequest_q = new CustomJSONObjectRequest(Request.Method.DELETE, url, jsonBody, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String result = response.getString("content");
                                LOGD("0813", result);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(nyTimesDetailActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mQueue.add(jsonRequest_q);
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

        shareActionProvider = new ShareActionProvider(this);
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
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, movie.getLink());
        return shareIntent;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    private void onShareAction() {
        shareActionProvider.setShareIntent(createShareIntent());
        return;
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;
        if (numberBadgeItem != null) {
            numberBadgeItem.setText(Integer.toString(position));
        }
        goToNavItem(position);
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
                            nyTimesDetailActivity.this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    @Override
    public void onTabUnselected(int position) {
    }

    @Override
    public void onTabReselected(int position) {
        goToNavItem(position);
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;

        mQueue = CustomVolleyRequestQueue.getInstance(nyTimesDetailActivity.this)
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

                        if (contents.length() == 0 ) {
                            Toast.makeText(getApplicationContext(), "Search title not found any review!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject movieObj = contents.getJSONObject(0);
                        String head = movieObj.getString("headline");
                        String linkUrl = movieObj.getJSONObject("link").getString("url");

                        CustomJSONObjectRequest jsonRequest_inner = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "nyTimes?url=" + linkUrl, new JSONObject(), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray contents = response.getJSONArray("contents");
                                    String story,imageUrl, head, description, editor, publish;
                                    JSONObject reviewObj = contents.getJSONObject(0);
                                    story = reviewObj.getString("story");
                                    editor = reviewObj.getString("editor");
                                    publish = reviewObj.getString("date");
                                    JSONObject imgObj = reviewObj.getJSONObject("image");

                                    if (imgObj.has("src")) {
                                        imageUrl = imgObj.getString("src");
                                        description = imgObj.getString("description");
                                    } else {
                                        imageUrl = null;
                                        description = null;
                                    }

                                    head = movie.getHeadline();
                                    Movie foo = new Movie(head, description, story, "", imageUrl, editor, publish);
                                    if (checkBookmark(head))
                                        foo.setBookmark(true);
                                    Intent intent = new Intent(getApplicationContext(), nyTimesDetailActivity.class);
                                    intent.putExtra("movie", foo);
                                    ActivityCompat.startActivity(nyTimesDetailActivity.this, intent, null);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener () {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Remote Server not working!", Toast.LENGTH_LONG).show();
                            }
                        });
                        mQueue.add(jsonRequest_inner);

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

    private boolean checkBookmark(String headline) {
        headline = headline.indexOf(":") != -1 ? headline.split(":")[1].trim() : headline;
        ArrayList list = favor.loadFavorites(getApplicationContext());

        if (list == null)
            return false;

        for (int i=0; i<list.size(); i++) {
            if (headline.compareTo((String) list.get(i)) == 0) return true;
        }

        return false;
    };

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
