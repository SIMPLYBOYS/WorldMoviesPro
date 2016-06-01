package com.github.florent37.materialviewpager.sample.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.ImdbCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.adapter.TestRecyclerViewAdapter;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class RecyclerViewFragment extends Fragment implements Response.Listener,
        Response.ErrorListener {

    protected abstract void arrangeModel();
    protected abstract void bindAdapter();
    protected abstract void clearModel();
    protected abstract RecyclerView getInitiatedRecyclerView();
    public abstract RecyclerView.Adapter getInitiatedAdapter();


    public interface Listener {
        public void onFragmentViewCreated(RecyclerViewFragment fragment);
        public void onFragmentAttached(RecyclerViewFragment fragment);
        public void onFragmentDetached(RecyclerViewFragment fragment);
    }

    public static final int upComingMovieCount = 74;

    public static final int top250MovieCount = 250;

    private RequestQueue mQueue;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final int PAGE_UNIT = 6; //default 6 cards in one page

    public String HOST_NAME = "http://ec2-52-192-246-11.ap-northeast-1.compute.amazonaws.com/";

    private List<TestRecyclerViewAdapter.MyObject> getRandomSublist(List<TestRecyclerViewAdapter.MyObject> array, int amount) {

        ArrayList<TestRecyclerViewAdapter.MyObject> list = new ArrayList<>(amount);
        Random random = new Random();
        while (list.size() < amount) {
            list.add(array.get(random.nextInt(amount)));
        }
        return list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    public void requestDataRefresh(boolean Refresh, String Query) {

        Log.d("0414", "requestDataRefresh");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            if (!this.getUserVisibleHint()) {
                return;
            }
        }

        int channel = this.getArguments().getInt("index", 0);
        Log.d("0414", String.valueOf(channel));
        CustomJSONObjectRequest jsonRequest = null;

        mQueue = CustomVolleyRequestQueue.getInstance(getContext())
                .getRequestQueue();

        CustomJSONObjectRequest jsonRequest_q = null; //json request from search bar

        switch (channel) {
            case 0:
                if (Query != null) {
                    // query from searchview
                    if (isNumeric(Query))
                        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                                "/imdb?from=" + Integer.parseInt(Query) + "&to=" + Integer.parseInt(Query) + "&ascending=1", new JSONObject(), this, this);
                    else {
                        try {
                            Query = URLEncoder.encode(Query, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new AssertionError("UTF-8 is unknown");
                        }

                        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/imdb?title=" + Query + "&ascending=1", new JSONObject(), this, this);
                    }
                    mQueue.add(jsonRequest_q);
                    return;
                }
                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) this.getInitiatedRecyclerView().getLayoutManager();
                SharedPreferences settings = getActivity().getSharedPreferences("settings", 0);
                boolean ascending = settings.getBoolean("ascending", false);
                int start = linearLayoutManager.getItemCount()-1;
                if (start % 6 != 0) {
                    removeAdapterModel();
                    start = 0;
                }
                int end = start + PAGE_UNIT; //6 default 6 cards per page
                Log.d("0416", "start: " + start);
                if (ascending) {
                    jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                            "/imdb?from=" + (start + 1) + "&to=" + end + "&ascending=1", new JSONObject(), this, this);
                } else {
                    jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                            "/imdb?from=" + (start + 1) + "&to=" + end + "&ascending=-1", new JSONObject(), this, this);
                }
                break;
            case 1:
                if (Query != null) {
                    // query from searchview
                    if (isNumeric(Query))
                        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                                "/imdb?release_from=" + Integer.parseInt(Query) + "&release_to=" + Integer.parseInt(Query) + "&ascending=1", new JSONObject(), this, this);
                    else {
                        try {
                            Query = URLEncoder.encode(Query, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            throw new AssertionError("UTF-8 is unknown");
                        }

                        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME + "/imdb?title=" + Query + "&ascending=1", new JSONObject(), this, this);
                    }
                    mQueue.add(jsonRequest_q);
                    return;
                }
                ImdbCardRecycleViewAdapter adapter =  (ImdbCardRecycleViewAdapter) getInitiatedAdapter();
                int count = adapter.getItemCount();
                if (count < 16) {
                    jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                            "/imdb?release_from=" + 20160501 + "&release_to=" + 20160531, new JSONObject(), this, this);
                } else if (count < 29) {
                    jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                            "/imdb?release_from=" + 20160601 + "&release_to=" + 20160630, new JSONObject(), this, this);
                } else if (count < 47) {
                    jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                            "/imdb?release_from=" + 20160701 + "&release_to=" + 20160731, new JSONObject(), this, this);
                } else if (count < 60) {
                    jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                            "/imdb?release_from=" + 20160801 + "&release_to=" + 20160831, new JSONObject(), this, this);
                } else if (count < 74) {
                    jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, HOST_NAME +
                            "/imdb?release_from=" + 20160901 + "&release_to=" + 20160930, new JSONObject(), this, this);
                }

                break;
            default:
                jsonRequest = new CustomJSONObjectRequest(Request.Method.GET
                        , HOST_NAME + "content/" + String.valueOf(channel), new JSONObject(), this, this);
                break;
        }

        if (Refresh)
            mSwipeRefreshLayout.setRefreshing(true);

        mQueue.add(jsonRequest); //trigger volley request
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
    }

    public void setupArrangeModel() {
        arrangeModel();
    }

    public void setAdapterBinding() {
        bindAdapter();
    }

    public RecyclerView setupRecyclerView() {
        return getInitiatedRecyclerView();
    }

    public RecyclerView.Adapter setupRecyclerAdapter() {
        return getInitiatedAdapter();
    }

    public void removeAdapterModel() {
        clearModel();
    }
}


