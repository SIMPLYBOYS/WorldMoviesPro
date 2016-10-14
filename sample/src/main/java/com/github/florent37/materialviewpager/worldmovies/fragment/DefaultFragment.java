package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.worldmovies.adapter.DefaultCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.detail.DetailActivity;
import com.github.florent37.materialviewpager.worldmovies.model.MyObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by aaron on 2016/4/14.
 */
public class DefaultFragment extends RecyclerViewFragment implements AdapterView.OnItemClickListener {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private DefaultCardRecycleViewAdapter defaultCardAdapter;
    private String cardType;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RequestQueue mQueue;
    private boolean loading;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private ArrayList<HashMap<String, String>> contentList;

    // JSON Node names
    private static final String TAG_LOCATIOIN = "location";
    private static final String TAG_TIME = "time";
    private static final String TAG_IMAGE_URL = "imgUrl";

    public RecyclerView getInitiatedRecyclerView() {
        return mRecyclerView;
    }

    public RecyclerView.Adapter getInitiatedAdapter() {
        return defaultCardAdapter;
    }

    public static DefaultFragment newInstance(int position) {
        DefaultFragment fragment = new DefaultFragment();
        Bundle args = new Bundle();
        args.putInt("index", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof Listener) {
            ((Listener) getActivity()).onFragmentViewCreated(this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof Listener) {
            ((Listener) getActivity()).onFragmentAttached(this);
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if (getActivity() instanceof Listener){
            ((Listener) getActivity()).onFragmentDetached(this);
        }
    }

    protected DefaultCardRecycleViewAdapter getAdapter() {

        return new DefaultCardRecycleViewAdapter(getContext());
    }

    @Override
    public void onResponse(Object response) {
        try {
            contentList = new ArrayList<HashMap<String, String>>();
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            buildDefaultModel(contents);
            loading = false;
            mSwipeRefreshLayout.setRefreshing(false);
        }catch (JSONException e) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Remote Server error!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mSwipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(),
        "Clicked: " + position + ", index " + mRecyclerView.indexOfChild(view),
        Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        Bundle extra = new Bundle();
        MyObject myObject = defaultCardAdapter.getItem().get(position-1);
        extra.putInt("index", position);
        extra.putString("location", myObject.getLocation());
        intent.putExtras(extra);
        ActivityCompat.startActivity(getActivity(), intent, null);
    }

    private void buildDefaultModel(JSONArray contents) throws JSONException {
        for (int i = 0; i < contents.length(); i++) {
            JSONObject c = contents.getJSONObject(i);
            String location = c.getString(TAG_LOCATIOIN);
            String time = c.getString(TAG_TIME);
            String imgUrl = c.getString(TAG_IMAGE_URL);
            HashMap<String, String> content = new HashMap<String, String>();
            content.put(TAG_LOCATIOIN,location);
            content.put(TAG_TIME, time);
            content.put(TAG_IMAGE_URL, imgUrl);
            contentList.add(content);
        }

        for (int j = 0; j < contentList.size(); j++) {
            HashMap<String, String> content = contentList.get(j);
            Log.d("0324", "TAG_LOCATION: " + content.get(TAG_LOCATIOIN));
            defaultCardAdapter.addItem(j, new MyObject(content.get(TAG_LOCATIOIN), content.get(TAG_TIME), content.get(TAG_IMAGE_URL)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        mRecyclerView.getItemAnimator().setAddDuration(1000);
        mRecyclerView.getItemAnimator().setChangeDuration(1000);
        mRecyclerView.getItemAnimator().setMoveDuration(1000);
        mRecyclerView.getItemAnimator().setRemoveDuration(1000);

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//        layoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.scheduleLayoutAnimation();
        addRecycleViewScollListener(mRecyclerView);
        defaultCardAdapter = getAdapter();
        defaultCardAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(defaultCardAdapter);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView);
        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
    }

    public void bindAdapter() {
        if (defaultCardAdapter == null) {
            defaultCardAdapter = getAdapter();
            defaultCardAdapter.setOnItemClickListener(this);
        }
        mRecyclerView.setAdapter(defaultCardAdapter);
        mRecyclerView.scheduleLayoutAnimation();
        defaultCardAdapter.notifyDataSetChanged();
        return;
    }

    public void arrangeModel() {
        Log.d("0416", "arrangeImdbModel");
        SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
        Boolean Small = settings.getBoolean("miniCard", false);
        int channel = this.getArguments().getInt("index", 0);
        boolean ascending = settings.getBoolean("ascending", false);

        //Do nothing
        return;
    }

    public void clearModel() {
        //TODO
    }

    public void addRecycleViewScollListener(final RecyclerView mRecyclerView) {
        SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
        Boolean Small = settings.getBoolean("miniCard", false);
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (Small)
            visibleThreshold = 5;
        else
            visibleThreshold = 1;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
                //TODO load more scroll to bottom
                Log.d("0409", "count: " + linearLayoutManager.getItemCount() + " last: " + linearLayoutManager.findLastVisibleItemPosition() +
                        "loading: " + loading);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();

                if (!loading
                        && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    // End has been reached
                    Log.d("0409", "case1");
                    if (onLoadMoreListener != null) {
                        Log.d("0409", "case2");
                        onLoadMoreListener.onLoadMore();
                    }
                    loading = true;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        return;
    }

    private DefaultCardRecycleViewAdapter.OnLoadMoreListener onLoadMoreListener = new DefaultCardRecycleViewAdapter.OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            Log.d("0409", "loading more!");
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    requestDataRefresh(true, null, null);
                }
            });
        }
    };
}
