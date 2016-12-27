package com.github.florent37.materialviewpager.worldmovies.favorite;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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
import com.github.florent37.materialviewpager.worldmovies.adapter.nyTimesFavoriteDetailRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.genre.GenreActivity;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesActivity;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
import com.sackcentury.shinebuttonlib.ShineButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/9/16.
 */
public class nyTimesFavoriteDetail extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {
    private final String NYTIMES_FAVORITE = "NYTIMES_FAVORITE";
    private RecyclerView nyTimesRecyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private nyTimesFavoriteDetailRecycleViewAdapter nyTimesFavoriteAdapter;
    private BottomNavigationBar bottomNavigationBar;
    private BadgeItem numberBadgeItem;
    private List<nyTimesMovie> nyTimesList;
    private nyTimesFavoritePreference favor;
    private int lastSelectedPosition = 0;
    private User user;
    private MenuItem bookmarkItem = null;
    LinearLayout bookmarkActionView;
    private ShineButton bookmarkView = null;
    public static final String FILM_NAME = "filmName";
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
        setContentView(R.layout.nytimes_favorite_detail);
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

        numberBadgeItem = new BadgeItem()
                .setBorderWidth(4)
                .setBackgroundColorResource(R.color.blue)
                .setText("" + lastSelectedPosition);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        favor = new nyTimesFavoritePreference();
        mQueue = CustomVolleyRequestQueue.getInstance(getApplicationContext()).getRequestQueue();
//        user = UsersUtils.getCurrentUser(getApplicationContext());
        user = (User) getIntent().getSerializableExtra("user");
        nyTimesList = new ArrayList<>();
        nyTimesRecyclerView = (RecyclerView)findViewById(R.id.nytimes_recyclerview);
        nyTimesRecyclerView.getItemAnimator().setAddDuration(1000);
        nyTimesRecyclerView.getItemAnimator().setChangeDuration(1000);
        nyTimesRecyclerView.getItemAnimator().setMoveDuration(1000);
        nyTimesRecyclerView.getItemAnimator().setRemoveDuration(1000);
        nyTimesFavoriteAdapter = new nyTimesFavoriteDetailRecycleViewAdapter(nyTimesList);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_trending_up, R.string.navdrawer_item_explore).setActiveColorResource(R.color.material_orange_900).setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_movie, R.string.navdrawer_item_up_coming).setActiveColorResource(R.color.material_teal_A200))
                .addItem(new BottomNavigationItem(R.drawable.ic_theaters, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "NyTimes").setActiveColorResource(R.color.material_brown_300))
                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_light_blue_A100))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setBarBackgroundColor(R.color.foreground_material_light)
                .initialise();
        fetch_nytimes();
        bottomNavigationBar.setTabSelectedListener(this);
    }

    private void fetch_nytimes() {
        CustomJSONArrayRequest jsonRequest = new CustomJSONArrayRequest(HOST_NAME + "my_nyTimes/"+user.id, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray contents = ((JSONArray) response);
                try {
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject movieObj = contents.getJSONObject(i);
                        String head = movieObj.getString("headline");
                        String link = movieObj.getString("link");
                        String picUrl = movieObj.getString("picUrl");
                        nyTimesMovie movie = new nyTimesMovie(head, null, null, link, picUrl, null, null);
                        nyTimesList.add(nyTimesList.size(), movie);
                    }

                    staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                    //------- deserialize Gallery JSON object -------//
                    nyTimesRecyclerView.setLayoutManager(staggeredGridLayoutManager);
                    nyTimesRecyclerView.setAdapter(nyTimesFavoriteAdapter);
                    nyTimesFavoriteAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final nyTimesMovie movie = nyTimesList.get(position);
                            CustomJSONObjectRequest jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "nyTimes?url=" + movie.getLink(), new JSONObject(), new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String story,imageUrl, head, description, editor, date, url;
                                        JSONArray contents = response.getJSONArray("contents");
                                        JSONObject reviewObj = contents.getJSONObject(0);
                                        story = reviewObj.getString("story");
                                        editor = reviewObj.getString("editor");
                                        date = reviewObj.getString("date");
                                        url = reviewObj.getString("url");
                                        JSONObject imgObj = reviewObj.getJSONObject("image");

                                        if (imgObj.has("src")) {
                                            imageUrl = imgObj.getString("src");
                                            description = imgObj.getString("description");
                                        } else {
                                            imageUrl = null;
                                            description = null;
                                        }

                                        head = movie.getHeadline();
                                        nyTimesMovie movie = new nyTimesMovie(head, description, story, url, imageUrl, editor ,date);
                                        if (checkBookmark(head))
                                            movie.setBookmark(true);
                                        Intent intent = new Intent(nyTimesFavoriteDetail.this, nyTimesDetailActivity.class);
                                        intent.putExtra("movie", movie);
                                        ActivityCompat.startActivity(nyTimesFavoriteDetail.this, intent, null);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener () {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplication(), "Remote Server not working!", Toast.LENGTH_LONG).show();
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
                            nyTimesFavoriteDetail.this).toBundle());
        } else {
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
