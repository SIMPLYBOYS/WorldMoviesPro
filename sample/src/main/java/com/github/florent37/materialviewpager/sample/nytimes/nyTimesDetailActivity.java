package com.github.florent37.materialviewpager.sample.nytimes;

import android.app.SearchManager;
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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import im.delight.android.webview.AdvancedWebView;

/**
 * Created by aaron on 2016/6/12.
 */
public class nyTimesDetailActivity extends AppCompatActivity implements Response.ErrorListener  {
    private ProgressBar progressBar;
    private AdvancedWebView mWebView;
    private MenuItem searchItem, shareItem;
    private SimpleCursorAdapter mAdapter;
    private SearchView searchView = null;
    private Movie movie;
    private TextView description, headLine, story, editor, publish;
    private ImageView pictureView;
    ShareActionProvider shareActionProvider;
    private RequestQueue mQueue;
    public static String FILM_NAME = "filmName";
    public static String REQUEST_TAG = "reviewRequest";
    private static String[] MOVIES = {};

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
        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();
        loadHints();
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

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
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

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME});
        for (int i = 0; i < MOVIES.length; i++) {
            if (MOVIES[i].toLowerCase().contains(query.toLowerCase()))
                cursor.addRow(new Object[]{i, MOVIES[i]});
        }
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    private void onShareAction() {
        shareActionProvider.setShareIntent(createShareIntent());
        return;
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
                        final Movie movie = new Movie(head, date, summery, linkUrl, picUrl, null, null);

                        /*Intent intent = new Intent(nyTimesActivity.this, WebViewActivity.class);
                        intent.putExtra("movie", movie);
                        ActivityCompat.startActivity(nyTimesActivity.this, intent, null);*/

                        CustomJSONObjectRequest jsonRequest_inner = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "nyTimes?url=" + movie.getLink(), new JSONObject(), new Response.Listener<JSONObject>() {
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
                                    Intent intent = new Intent(getApplicationContext(), nyTimesDetailActivity.class);
                                    intent.putExtra("movie", foo);
                                    ActivityCompat.startActivity(nyTimesDetailActivity.this, intent, null);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },new Response.ErrorListener () {
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
                Toast.makeText(nyTimesDetailActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
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
