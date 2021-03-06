package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.ImdbReviewRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.model.ReviewItem;
import com.google.gson.Gson;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/7/28.
 */
public class ImdbReviewTabFragment extends Fragment implements Response.ErrorListener {
    private ImdbObject imdbObject;
    private Gson gson = new Gson();
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public static RecyclerView movieReview;
    public static LinearLayoutManager linearLayoutManager;
    private ImdbReviewRecycleViewAdapter rAdapter;
    private String HOST_NAME = Config.HOST_NAME;
    private ProgressBar progressBar;
    private ArrayList<ReviewItem> reviewItems;
    private RequestQueue mQueue;
    private int offSet = 0;
    private int maxSize = 0;
    private String searchChannel = "12";
    private int curSize = 0;

    public static ImdbReviewTabFragment newInstance(ImdbObject imdbObject) {
        ImdbReviewTabFragment fragment = new ImdbReviewTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("imdb", imdbObject);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        imdbObject = (ImdbObject) getArguments().getSerializable("imdb");
        reviewItems = new ArrayList<> ();
        rAdapter = new ImdbReviewRecycleViewAdapter(getActivity(), reviewItems);
        movieReview = (RecyclerView) inflater.inflate(
                R.layout.fragment_recyclerview, container, false);
        linearLayoutManager = new LinearLayoutManager(movieReview.getContext());
        movieReview.setLayoutManager(linearLayoutManager);
        movieReview.setAdapter(rAdapter);
        movieReview.addItemDecoration(new HorizontalDividerItemDecoration.Builder(movieReview.getContext()).build());

        mQueue = CustomVolleyRequestQueue.getInstance(getActivity()).getRequestQueue();

        movieReview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();

                rAdapter.totalItemCount = linearLayoutManager.getItemCount();
                rAdapter.lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!rAdapter.loading && rAdapter.totalItemCount <= (rAdapter.lastVisibleItem + rAdapter.visibleThreshold)) {
                    // End has been reached
                    if (maxSize == 0 || rAdapter.totalItemCount < maxSize) {
                        fetchReviews(false);
                        rAdapter.loading = true;
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        fetchReviews(true);
        return movieReview;
    }

    public String encode(@NonNull String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            Assert.fail("Uri string cannot be empty!");
            return uriString;
        }
        // getQueryParameterNames is not exist then cannot iterate on queries
        if (Build.VERSION.SDK_INT < 11) {
            return uriString;
        }

        // Check if uri has valid characters
        // See https://tools.ietf.org/html/rfc3986
        Pattern allowedUrlCharacters = Pattern.compile("([A-Za-z0-9_.~:/?\\#\\[\\]@!$&'()*+,;" +
                "=-]|%[0-9a-fA-F]{2})+");
        Matcher matcher = allowedUrlCharacters.matcher(uriString);
        String validUri = null;
        if (matcher.find()) {
            validUri = matcher.group();
        }
        if (TextUtils.isEmpty(validUri) || uriString.length() == validUri.length()) {
            return uriString;
        }

        // The uriString is not encoded. Then recreate the uri and encode it this time
        Uri uri = Uri.parse(uriString);
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority());
        for (String path : uri.getPathSegments()) {
            uriBuilder.appendPath(path);
        }
        for (String key : uri.getQueryParameterNames()) {
            uriBuilder.appendQueryParameter(key, uri.getQueryParameter(key));
        }
        String correctUrl = uriBuilder.build().toString();

        return correctUrl;
    }

    public void fetchReviews(final boolean first) {
        if (!first) {
            reviewItems.add(null);
            rAdapter.notifyItemInserted(reviewItems.size() - 1);
        }

        String title = imdbObject.getTitle();
        searchChannel = CredentialsHandler.getCountry(getActivity());
        String url;
        try {
            url = Config.HOST_NAME + "worldReview/" + searchChannel + "?title=" + URLEncoder.encode(title, "UTF-8") + "&ascending=1&start=" + offSet;
        }  catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        CustomJSONObjectRequest jsonRequest_q = null;
        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (reviewItems.size() > 0 && !first) {
                        reviewItems.remove(reviewItems.size() - 1);
                        rAdapter.notifyItemRemoved(reviewItems.size());
                    }
                    JSONArray contents = response.getJSONArray("review");
                    maxSize = response.getInt("size");
                    LOGD("0725", String.valueOf(maxSize));
                    for (int i = 0; i < contents.length(); i++) {
                        JSONObject reviewObj = contents.getJSONObject(i);
                        String avatar = reviewObj.getString("avatar");
                        String name = reviewObj.getString("name");
                        String date = reviewObj.getString("date");
                        String topic = reviewObj.getString("topic");
                        String text = reviewObj.getString("text");
                        String point = reviewObj.getString("point");

                        ReviewItem Item = point.compareTo("null") != 0 ? new ReviewItem(avatar, name, date, topic, text, Float.parseFloat(point)) :
                                new ReviewItem(avatar, name, date, topic, text, 0.0f)  ;
                        curSize = rAdapter.getItemCount();
                        reviewItems.add(reviewItems.size(), Item);
                        if (rAdapter != null) {
                            rAdapter.notifyItemInserted(reviewItems.size());
                        }
                    }
                    rAdapter.setLoaded();
                    /*if(rAdapter != null) {
                        rAdapter.notifyItemRangeInserted(curSize, movieList.size()-1);
                    }*/
                    offSet += 10;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LOGD("0606", String.valueOf(error.getMessage()));
//                Toast.makeText(getActivity(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        RetryPolicy policy = new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonRequest_q.setRetryPolicy(policy);
        mQueue.add(jsonRequest_q);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
    }
}
