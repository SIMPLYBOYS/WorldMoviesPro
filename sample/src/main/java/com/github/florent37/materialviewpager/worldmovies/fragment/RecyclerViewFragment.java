package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.TestRecyclerViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.TrendsCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.HomeRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public abstract class RecyclerViewFragment extends Fragment implements Response.Listener,
        Response.ErrorListener {

    protected abstract void arrangeModel();
    protected abstract void bindAdapter();
    protected abstract RecyclerView getInitiatedRecyclerView();
    public abstract RecyclerView.Adapter getInitiatedAdapter();


    public interface Listener {
        public void onFragmentViewCreated(RecyclerViewFragment fragment);
        public void onFragmentAttached(RecyclerViewFragment fragment);
        public void onFragmentDetached(RecyclerViewFragment fragment);
    }

    public static final int upComingMovieCount = 74;

    public static final int top250MovieCount = 250;

    public static final int trendMovieCount = 10;

    public static final int topicBoard = 2;

    public static int [] monthList;

    private RequestQueue mQueue;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final int PAGE_UNIT = 6; //default 6 cards in one page

    public String HOST_NAME = Config.HOST_NAME;

    private ProgressBar progressBar;

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

    public void requestDataRefresh(boolean Refresh, String Query, JSONArray List) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            if (!this.getUserVisibleHint()) {
                return;
            }
        }

        if (List != null && monthList == null) {
            try {
                monthList = new int[12];
                for (int i = 0; i < List.length(); i++) {
                    monthList[i] = List.getInt(i);
                }
            } catch (JSONException e) {
                Log.e("App", "unexpect JSON exception", e);
            }
        }

        CustomJSONObjectRequest jsonRequest = null;
        int channel = this.getArguments().getInt("index", 0);
        mQueue = CustomVolleyRequestQueue.getInstance(getContext()).getRequestQueue();
        SharedPreferences settings = getActivity().getSharedPreferences("settings", 0);
        boolean ascending = settings.getBoolean("ascending", false);
        String url = "";
        RecyclerView.Adapter adapter;

        switch (channel) {
            case 0: //TODO fetching for homepage meta data
                url = HOST_NAME + "jpTrends";
                adapter =  (HomeRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            case 1:
                url = HOST_NAME + "jpTrends";
                adapter =  (TrendsCardRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            case 2:
                url = HOST_NAME + "usTrends";
                adapter =  (TrendsCardRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            case 3:
                url = HOST_NAME + "twTrends";
                adapter =  (TrendsCardRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            case 4:
                url = HOST_NAME + "krTrends";
                adapter =  (TrendsCardRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            case 5:
                url = HOST_NAME + "frTrends";
                adapter =  (TrendsCardRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            case 6:
                url = HOST_NAME + "cnTrends";
                adapter =  (TrendsCardRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            case 7:
                url = HOST_NAME + "gmTrends";
                adapter =  (TrendsCardRecycleViewAdapter) getInitiatedAdapter();

                if (adapter != null && adapter.getItemCount() > 1)
                    return;

                progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.transparent_black), PorterDuff.Mode.SRC_ATOP);
                break;

            default:
                jsonRequest = new CustomJSONObjectRequest(Request.Method.GET
                        , HOST_NAME + "content/" + String.valueOf(channel), new JSONObject(), this, this);
                break;
        }

        if (ascending) {
            jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url + "?ascending=1", new JSONObject(), this, this);
        } else {
            jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, url + "?ascending=-1", new JSONObject(), this, this);
        }

        if (Refresh)
            mSwipeRefreshLayout.setRefreshing(true);

        RetryPolicy policy = new DefaultRetryPolicy(8000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonRequest.setRetryPolicy(policy);
        mQueue.add(jsonRequest); //trigger volley request
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
    }

    private int getReleaseDate(int roll, int day) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        c.roll(Calendar.MONTH, roll);
        c.set(Calendar.DAY_OF_MONTH, day);
        String str = df.format(c.getTime());
        String [] parts = TextUtils.split(str, "/");
        Log.d("0606", TextUtils.join("", parts));
        return Integer.parseInt(TextUtils.join("", parts));
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
}


