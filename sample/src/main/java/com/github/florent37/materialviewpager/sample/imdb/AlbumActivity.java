package com.github.florent37.materialviewpager.sample.imdb;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.ImdbGalleryRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aaron on 2016/5/3.
 */
public class AlbumActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        Response.Listener, Response.ErrorListener {

    private ShareActionProvider shareActionProvider;
    private final String IMDB_OBJECT = "IMDB_OBJECT";
    private ImdbObject imdbObject;
    private RecyclerView myRecyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ImdbGalleryRecycleViewAdapter imdbGalleryAdapter;
    private List<ImdbObject.GalleryItem> list = null;
    private MenuItem searchItem, shareItem;
    private SearchView searchView = null;
    public static final String FILM_NAME = "filmName";
    private SimpleCursorAdapter mAdapter;
    private static String[] MOVIES = {};
    private RequestQueue mQueue;
    public static ArrayList<HashMap<String, String>> contentList;
    public static ArrayList<HashMap<String, String>> galleryList;
    private String HOST_NAME = Config.HOST_NAME;
    public static final String REQUEST_TAG = "titleRequest";
    private static final String TAG_TITLE = "title";
    private static final String TAG_YEAR = "year";
    private static final String TAG_DETAIL_URL = "detailUrl";
    private static final String TAG_TOP = "top";
    private static final String TAG_POSTER_URL = "posterUrl";
    private static final String TAG_RATING = "rating";
    private static final String TAG_DESCRIPTION = "description";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        imdbObject = (ImdbObject) getIntent().getSerializableExtra(IMDB_OBJECT);
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
        imdbGalleryAdapter = new ImdbGalleryRecycleViewAdapter(imdbObject, true);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        myRecyclerView.setLayoutManager(staggeredGridLayoutManager);

        //------- deserialize Gallery JSON object -------//
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonParser().parse(imdbObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<ImdbObject.GalleryItem>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement str = jsonArray.get(i);
            ImdbObject.GalleryItem obj = gson.fromJson(str, ImdbObject.GalleryItem.class);
            list.add(obj);
            imdbGalleryAdapter.addItem(i,obj);
        }
        //------- deserialize Gallery JSON object -------//
        myRecyclerView.setAdapter(imdbGalleryAdapter);
        imdbGalleryAdapter.setOnItemClickListener(this);
        loadHints(); //chaching for search hint
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

        shareItem = menu.findItem(R.id.action_share); // Retrieve the share menu item
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

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME});
        for (int i = 0; i < MOVIES.length; i++) {
            if (MOVIES[i].toLowerCase().contains(query.toLowerCase()))
                cursor.addRow(new Object[]{i, MOVIES[i]});
        }
        mAdapter.changeCursor(cursor);
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

        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/imdb_title", new JSONObject(), this, this);
        mQueue.add(jsonRequest);
    }

    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;
        contentList = new ArrayList<HashMap<String, String>>();
        galleryList = new ArrayList<HashMap<String, String>>();

        mQueue = CustomVolleyRequestQueue.getInstance(AlbumActivity.this)
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
                        JSONArray contents = response.getJSONArray("contents");
                        Log.d("0504", "title onResponse" + contents);
                        ImdbObject item = buildImdbModel(contents);
                        Intent intent = new Intent(AlbumActivity.this, MovieDetail.class);
                        intent.putExtra(MovieDetail.IMDB_OBJECT, item);
                        ActivityCompat.startActivity(AlbumActivity.this, intent, null);
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
    public void onBackPressed() {
        if (searchView != null && !searchView.isIconified()) {
            MenuItemCompat.collapseActionView(searchItem);
            searchView.setIconified(true);
            return;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(this, "Clicked: " + position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(view.getContext(), SlideActivity.class);
        intent.putExtra(SlideActivity.PIC_POSITION, position);
        intent.putExtra(MovieDetail.IMDB_OBJECT, imdbObject);
        startActivity(intent);
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
            Log.d("0419", "title onResponse");
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            MOVIES = getStringArray(contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getDetailUrl());
        return shareIntent;
    }

    public static ImdbObject buildImdbModel(JSONArray contents) throws JSONException {
            JSONObject c = contents.getJSONObject(0);
            String title = c.getString(TAG_TITLE);
            JSONObject d = c.getJSONObject("detailContent");
            Log.d("0406", String.valueOf(d));
            int top = c.getInt(TAG_TOP);
            String year = c.getString(TAG_YEAR);
            String detailUrl = c.getString(TAG_DETAIL_URL);
            String description = c.getString(TAG_DESCRIPTION);
            String rating = c.getString(TAG_RATING);
            String posterUrl = c.getString(TAG_POSTER_URL);
            String plot = c.getString(TAG_PLOT);
            String genre = c.getString(TAG_GENRE);
            String votes = c.getString(TAG_VOTES);
            String runTime = c.getString(TAG_RUNTIME);
            String metaScore = c.getString(TAG_METASCORE);
            String summery = d.getString(TAG_SUMMERY);
            String country = d.getString(TAG_COUNTRY);
            String detailPosterUrl = d.getString(TAG_DETAIL_POSTER_URL);
            JSONArray galleryFullUrl = c.getJSONArray(TAG_GALLERY_FULL);
            String trailerUrl;
            String slate;
            String delta;

            if (c.has(TAG_TRAILER))
                trailerUrl = c.getString(TAG_TRAILER);
            else
                trailerUrl = "N/A";

            if (d.has(TAG_SLATE))
                slate = d.getString(TAG_SLATE);
            else
                slate = "N/A";

            if (c.has(TAG_DELTA))
                delta = c.getString(TAG_DELTA);
            else
                delta = "";

            Log.d("0407", trailerUrl);
            HashMap<String, String> content = new HashMap<String, String>();
            HashMap<String, String> gallery = new HashMap<String, String>();
            content.put(TAG_TITLE, title);
            content.put(TAG_TOP, String.valueOf(top));
            content.put(TAG_YEAR, year);
            content.put(TAG_DETAIL_URL, detailUrl);
            content.put(TAG_DESCRIPTION, description);
            content.put(TAG_RATING, rating);
            content.put(TAG_POSTER_URL, posterUrl);
            content.put(TAG_SLATE, slate);
            content.put(TAG_SUMMERY, summery);
            content.put(TAG_PLOT, plot);
            content.put(TAG_GENRE, genre);
            content.put(TAG_VOTES, votes);
            content.put(TAG_RUNTIME,runTime);
            content.put(TAG_METASCORE, metaScore);
            content.put(TAG_DELTA, delta);
            content.put(TAG_COUNTRY, country);
            content.put(TAG_DETAIL_POSTER_URL, detailPosterUrl);
            content.put(TAG_TRAILER, trailerUrl);
            gallery.put(TAG_GALLERY_FULL, galleryFullUrl.toString());
            contentList.add(content);
            galleryList.add(gallery);

//        imdbCollection = new ArrayList<ImdbObject>();

            content = contentList.get(0);
            gallery = galleryList.get(0);
            ImdbObject item = new ImdbObject(content.get(TAG_TITLE), content.get(TAG_TOP), content.get(TAG_YEAR), content.get(TAG_DESCRIPTION),
                    content.get(TAG_RATING), content.get(TAG_POSTER_URL), content.get(TAG_SLATE), content.get(TAG_SUMMERY), content.get(TAG_PLOT),
                    content.get(TAG_GENRE), content.get(TAG_VOTES), content.get(TAG_RUNTIME), content.get(TAG_METASCORE), content.get(TAG_DELTA),content.get(TAG_COUNTRY),
                    content.get(TAG_TRAILER), gallery.get(TAG_GALLERY_FULL));

            return item;
    }

}
