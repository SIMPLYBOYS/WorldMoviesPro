package com.github.florent37.materialviewpager.worldmovies.detail;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by aaron on 2016/3/28.
 */
public class DetailActivity extends AppCompatActivity implements Response.Listener,
        Response.ErrorListener {

    private Toolbar toolbar;
    private ImageView imageView;
    private RequestQueue mQueue;
    public static final String REQUEST_TAG = "DetailActivity";
    private String DETAIL_URL = "http://ec2-52-192-246-11.ap-northeast-1.compute.amazonaws.com/detail";
    private ArrayList<HashMap<String, String>> contentList;
    private long INTERVAL = 1000;
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, INTERVAL);
        }
    };

    // JSON Node names
    private static final String TAG_LOCATIOIN = "location";
    private static final String TAG_TIME = "time";
    private static final String TAG_IMAGE_URL = "imgUrl";


    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Bundle extras = getIntent().getExtras();

        if (extras != null)
            Log.d("0327", "index: " + extras.getInt("index") + " location: " + extras.getString("location"));

        imageView = (ImageView) findViewById(R.id.imageView);
        mQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext())
                .getRequestQueue();

        final CustomJSONObjectRequest jsonRequest = new CustomJSONObjectRequest(Request.Method
                .GET, DETAIL_URL + '/' + String.valueOf(extras.getString("location")),
                new JSONObject(), this, this);
        jsonRequest.setTag(REQUEST_TAG);

        mQueue.add(jsonRequest); //trigger volley request

        this.toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
//        toolbar.setTitle(getPageTitle(extras.getInt("pageIndex")));

        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        registerHideableHeaderView(findViewById(R.id.headerbar));
        overridePendingTransition(0, 0);
    }

    public void sessionDetailItemClicked(View viewClicked) {
        Object tag = null;
        if (viewClicked != null) {
            tag = viewClicked.getTag();
        }
        Log.d("0328", "detailclick");
    }

    protected void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView);
        }
    }

    public CharSequence getPageTitle(int position) {
        switch (position % 4) {
            case 0:
                return getResources().getString(R.string.IMDB);
            case 1:
                return getResources().getString(R.string.sports);
            case 2:
                return getResources().getString(R.string.technologie);
            case 3:
                return getResources().getString(R.string.international);
            default:
                return "Page " + position;
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(Object response) {
        contentList = new ArrayList<HashMap<String, String>>();
        try {
            Log.d("0328", "Response is: " + ((JSONObject) response).getJSONArray("contents"));
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            for (int i = 0; i < contents.length(); i++) {
                JSONObject c = contents.getJSONObject(i);
                String location = c.getString(TAG_LOCATIOIN);
                String time = c.getString(TAG_TIME);
                String imgUrl = c.getString(TAG_IMAGE_URL);
                HashMap<String, String> content = new HashMap<String, String>();
                content.put(TAG_LOCATIOIN, location);
                content.put(TAG_TIME, time);
                content.put(TAG_IMAGE_URL, imgUrl);
                contentList.add(content);
            }

            Picasso.with(imageView.getContext()).load(contentList.get(0).get(TAG_IMAGE_URL)).centerCrop().fit().into(imageView);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
