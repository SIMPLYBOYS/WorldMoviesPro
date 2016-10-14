package com.github.florent37.materialviewpager.worldmovies.favorite;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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
import com.github.florent37.materialviewpager.worldmovies.adapter.TrendsFavoriteRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.genre.GenreActivity;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.Movie;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesActivity;
import com.github.florent37.materialviewpager.worldmovies.trends.TrendsDetail;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
import com.github.florent37.materialviewpager.worldmovies.util.BuildModelUtils;
import com.github.florent37.materialviewpager.worldmovies.util.UIUtils;
import com.sackcentury.shinebuttonlib.ShineButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/9/16.
 */
public class MoviesFavoriteDetail extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {
    private final String TRENDS_FAVORITE = "TRENDS_FAVORITE";
    private RecyclerView trendsRecyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private TrendsFavoriteRecycleViewAdapter trendsFavoriteAdapter;
    private List<Movie> trendsList;
    private BottomNavigationBar bottomNavigationBar;
    private int lastSelectedPosition = 0;
    private BadgeItem numberBadgeItem;
    private User user;
    private MenuItem searchItem;
    private MenuItem bookmarkItem = null;
    LinearLayout bookmarkActionView;
    private ShineButton bookmarkView = null;
    private SearchView searchView = null;
    public static final String FILM_NAME = "filmName";
    private SimpleCursorAdapter mAdapter;
    private static String[] MOVIES = {};
    private RequestQueue mQueue;
    private String HOST_NAME = Config.HOST_NAME;
    protected final int NAV_ITEM_TREND = 0;
    protected final int NAV_ITEM_UPCOMING = 1;
    protected final int NAV_ITEM_IMDB = 2;
    protected final int NAV_ITEM_NYTIMES = 3;
    protected final int NAV_ITEM_GENRE = 4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trends_favorite_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            toolbar.setTitleTextColor(Color.WHITE);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        mQueue = CustomVolleyRequestQueue.getInstance(getApplicationContext()).getRequestQueue();
//        user = PrefUtils.getCurrentUser(getApplicationContext());
        user = (User) getIntent().getSerializableExtra("user");
        trendsList = new ArrayList<>();
        trendsRecyclerView = (RecyclerView)findViewById(R.id.trends_recyclerview);
        trendsRecyclerView.getItemAnimator().setAddDuration(1000);
        trendsRecyclerView.getItemAnimator().setChangeDuration(1000);
        trendsRecyclerView.getItemAnimator().setMoveDuration(1000);
        trendsRecyclerView.getItemAnimator().setRemoveDuration(1000);
        trendsFavoriteAdapter = new TrendsFavoriteRecycleViewAdapter(trendsList);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_trending_up, R.string.navdrawer_item_explore).setActiveColorResource(R.color.material_orange_900).setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_movie, R.string.navdrawer_item_up_coming).setActiveColorResource(R.color.material_teal_A200))
                .addItem(new BottomNavigationItem(R.drawable.ic_theaters, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "NyTimes").setActiveColorResource(R.color.material_brown_300))
                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_light_blue_A100))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setBarBackgroundColor(R.color.material_grey_900)
                .initialise();
        fetch_trends();
        bottomNavigationBar.setTabSelectedListener(this);
    }

    private void fetch_trends() {
        CustomJSONArrayRequest jsonRequest = new CustomJSONArrayRequest(HOST_NAME + "my_trends/"+user.id, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray contents = ((JSONArray) response);
                try {
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String title = movieObj.getString("title");
                        String link = movieObj.getString("link");
                        int channel = movieObj.getInt("channel");
                        String picUrl = movieObj.getString("picUrl");
                        Movie movie = new Movie(title, null, null, link, picUrl, null, null);
                        movie.setChannel(channel);
                        trendsList.add(trendsList.size(), movie);
                    }

                    staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                    //------- deserialize Gallery JSON object -------//
                    trendsRecyclerView.setLayoutManager(staggeredGridLayoutManager);
                    trendsRecyclerView.setAdapter(trendsFavoriteAdapter);
                    trendsFavoriteAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final Movie movie = trendsList.get(position);
                            String url = UIUtils.getTrendsUrl(movie);

                            CustomJSONObjectRequest jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONArray contents = response.getJSONArray("contents");
                                        TrendsObject item = BuildModelUtils.buildTrendsModel(contents, true, movie.getChannel());
                                        Intent intent = new Intent(MoviesFavoriteDetail.this, TrendsDetail.class);
                                        intent.putExtra(TrendsDetail.TRENDS_OBJECT, item);
                                        ActivityCompat.startActivity(MoviesFavoriteDetail.this, intent, null);

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

                            mQueue.add(jsonRequest_q);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;

        if (numberBadgeItem != null)
            numberBadgeItem.setText(Integer.toString(position));

        goToNavItem(position);
    }


    @Override
    public void onTabUnselected(int position) {
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
                            MoviesFavoriteDetail.this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    @Override
    public void onTabReselected(int position) {
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
}
