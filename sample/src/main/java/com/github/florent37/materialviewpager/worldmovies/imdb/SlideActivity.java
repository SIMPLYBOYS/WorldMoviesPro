package com.github.florent37.materialviewpager.worldmovies.imdb;

import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.ImageCursorAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.ImdbSlideRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.util.BuildModelUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sackcentury.shinebuttonlib.ShineButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/5/3.
 */
public class SlideActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        Response.Listener, Response.ErrorListener {

    public static final String PIC_POSITION = "PIC_POSITION";
    private final String IMDB_OBJECT = "IMDB_OBJECT";
    private final String TAG_TOP = "top";
    private ImdbObject imdbObject;
    private RecyclerView myRecyclerView;
    LinearLayout bookmarkActionView;
    private ShineButton bookmarkView = null;
    private LinearLayoutManager linearLayoutManager;
    private ImdbSlideRecycleViewAdapter imdbSlideAdapter;
    private List<ImdbObject.GalleryItem> list = null;
    private MenuItem shareItem;
    private MenuItem bookmarkItem = null;
    public static final String FILM_NAME = "filmName";
    public static final String FILM_DESCRIPTION = "filmDescription";
    public static final String FILM_POSTER = "filmPoster";
    private ImageCursorAdapter mAdapter;
    private static JSONObject[] MOVIES = {};
    private RequestQueue mQueue;
    private ArrayList<HashMap<String, String>> contentList;
    private ArrayList<HashMap<String, String>> galleryList;
    private String HOST_NAME = Config.HOST_NAME;
    public static final String REQUEST_TAG = "titleRequest";
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        imdbObject = (ImdbObject) getIntent().getSerializableExtra(IMDB_OBJECT);
        position = getIntent().getExtras().getInt(PIC_POSITION);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setTitle(imdbObject.getTitle());
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        myRecyclerView = (RecyclerView)findViewById(R.id.myrecyclerview);
        myRecyclerView.getItemAnimator().setAddDuration(1000);
        myRecyclerView.getItemAnimator().setChangeDuration(1000);
        myRecyclerView.getItemAnimator().setMoveDuration(1000);
        myRecyclerView.getItemAnimator().setRemoveDuration(1000);
        imdbSlideAdapter = new ImdbSlideRecycleViewAdapter(imdbObject);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myRecyclerView.setLayoutManager(linearLayoutManager);

        //------- deserialize Gallery JSON object -------//
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonParser().parse(imdbObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<ImdbObject.GalleryItem>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement str = jsonArray.get(i);
            ImdbObject.GalleryItem obj = gson.fromJson(str, ImdbObject.GalleryItem.class);
            list.add(obj);
            imdbSlideAdapter.addItem(i,obj);
        }
        //------- deserialize Gallery JSON object -------//

        myRecyclerView.setAdapter(imdbSlideAdapter);
        imdbSlideAdapter.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.album_menu, menu);

        for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            }
        }

        bookmarkActionView = (LinearLayout) getLayoutInflater().inflate(R.layout.bookmark_image, null);
        bookmarkView = (ShineButton) bookmarkActionView.findViewById(R.id.bookmarkView);
        bookmarkView.init(this);
        bookmarkView.getLayoutParams().height=96;
        bookmarkView.getLayoutParams().width=96;
        bookmarkView.setColorFilter(getResources().getColor(R.color.app_white));
        bookmarkView.setScaleType(ImageView.ScaleType.FIT_XY);
        // Retrieve the share menu item
        shareItem = menu.findItem(R.id.action_share);
        bookmarkItem = menu.findItem(R.id.action_bookmark);
        bookmarkItem.setActionView(bookmarkView);

        if (imdbObject.getBookmark()) {
            bookmarkView.setChecked(true);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
        } else {
            bookmarkView.setChecked(false);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_not_white);
        }

        bookmarkView.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
//                Snackbar.make(view, "Bookmark "+checked+" !!!", Snackbar.LENGTH_LONG).show();
                Toast.makeText(SlideActivity.this, "Bookmark "+checked+" !!!", Toast.LENGTH_SHORT).show();

                if (checked) {
                    /*JsonArray dataInfo = new JsonParser().parse(imdbObject.getData()).getAsJsonArray();
                    JsonElement jsonElement = null;
                    Gson gson = new Gson();
                    String country;
                    jsonElement = dataInfo.size() == 5 ? dataInfo.get(2) : dataInfo.get(1);
                    TrendsObject.DataItem dataItem = gson.fromJson(jsonElement, TrendsObject.DataItem.class);
                    country = dataItem.getData().indexOf(":") != -1 ? dataItem.getData().split(":")[1] : dataItem.getData();*/
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                    LOGD("0812", imdbObject.getTitle());
                } else {
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_not_white);
                    //TODO bookmark info for the user's acccount
                }
            }
        });

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

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;

        mQueue = CustomVolleyRequestQueue.getInstance(SlideActivity.this)
                .getRequestQueue();

        CustomJSONObjectRequest jsonRequest_q = null;

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }
            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/imdb?title=" + Query + "&ascending=1", new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        contentList = new ArrayList<HashMap<String, String>>();
                        galleryList = new ArrayList<HashMap<String, String>>();
                        JSONArray contents = response.getJSONArray("contents");
                        JSONObject c = contents.getJSONObject(0);
                        Log.d("0504", "title onResponse" + contents);
                        ImdbObject movie = BuildModelUtils.buildImdbModel(contents);
                        if (c.has(TAG_TOP))
                            movie.setType("imdb");
                        else
                            movie.setType("genre");
                        Intent intent = new Intent(SlideActivity.this, MovieDetailActivity.class);
                        intent.putExtra(ImdbActivity.IMDB_OBJECT, movie);
                        ActivityCompat.startActivity(SlideActivity.this, intent, null);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "Clicked: " + position, Toast.LENGTH_SHORT).show();
        /*Intent intent = new Intent(getActivity(), DetailActivity.class);
        Bundle extra = new Bundle();
        MyObject myObject = defaultCardAdapter.getItem().get(position-1);
        extra.putInt("index", position);
        extra.putString("location", myObject.getLocation());
        intent.putExtras(extra);
        ActivityCompat.startActivity(this, intent, null);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_share) {
            showShareDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResponse(Object response) {
        try {
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            MOVIES = BaseActivity.getJsonObjectArray(contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        linearLayoutManager.scrollToPosition(position);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
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

    private void showShareDialog() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, imdbObject.getDetailUrl());
        startActivity(Intent.createChooser(intent, "Share"));
    }
}
