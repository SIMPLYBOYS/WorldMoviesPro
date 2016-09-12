package com.github.florent37.materialviewpager.sample.fragment;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.TrendsCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.model.TrendsObject;
import com.github.florent37.materialviewpager.sample.trends.TrendsDetail;
import com.github.florent37.materialviewpager.sample.trends.TrendsFavoritePreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aaron on 2016/6/16.
 */
public class TrendsFragment extends RecyclerViewFragment implements AdapterView.OnItemClickListener {
    // JSON Node keys
    private static final String TAG_TITLE = "title";
    private static final String TAG_DATA = "data";
    private static final String TAG_ORIGIN_TITLE = "trailerTitle";
    private static final String TAG_RELEASE = "releaseDate";
    private static final String TAG_TOP = "top";
    private static final String TAG_POSTER_URL = "posterUrl";
    private static final String TAG_INFO = "mainInfo";
    private static final String TAG_DETAIL_URL = "detailUrl";
    private static final String TAG_GALLERY_FULL = "gallery_full";
    private static final String TAG_STORY = "story";
    private static final String TAG_CAST = "cast";
    private static final String TAG_REVIEW = "review";
    private static final String TAG_RATING = "rating";
    private static final String TAG_RUNTIME = "runtime";
    private static final String TAG_METASCORE = "metascore";
    private static final String TAG_SLATE = "slate";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_TRAILER = "trailerUrl";
    private static final String TAG_STAFF = "staff";
    private int visibleThreshold = 1;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TrendsFavoritePreference favor;
    private RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private LinearLayoutManager layoutManager;
    private TrendsCardRecycleViewAdapter trendsCardAdapter;
    private boolean loading;
    private int lastVisibleItem, totalItemCount, channel;

    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;

    public RecyclerView getInitiatedRecyclerView() {
        return mRecyclerView;
    }

    public RecyclerView.Adapter getInitiatedAdapter() {
        return trendsCardAdapter;
    }

    public static TrendsFragment newInstance(int position) {
        TrendsFragment fragment = new TrendsFragment();
        Bundle args = new Bundle();
        Log.d("0830", String.valueOf(position));
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
    public void onDetach() {
        super.onDetach();
        if (getActivity() instanceof Listener) {
            ((Listener) getActivity()).onFragmentDetached(this);
        }
    }

    public TrendsCardRecycleViewAdapter
    getAdapter() {
        return new TrendsCardRecycleViewAdapter(getContext());
    }

    @Override
    public void
    onResponse(Object response) {
        try {
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            Log.d("0616", String.valueOf(contents));
            boolean byTitle = ((JSONObject) response).getBoolean("byTitle");
            TrendsObject item = buildTrendsModel(contents, byTitle);
            loading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            Log.d("0515", String.valueOf(byTitle));
            progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            if (byTitle) {
                Intent intent = new Intent(getActivity(), TrendsDetail.class);
                intent.putExtra(TrendsDetail.TRENDS_OBJECT, item);
                ActivityCompat.startActivity(getActivity(), intent, null);
            }

        } catch (JSONException e) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mSwipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(),
                "Clicked: " + position + ", index " + mRecyclerView.indexOfChild(view),
                Toast.LENGTH_SHORT).show();
        TrendsObject tObject = trendsCardAdapter.getItem().get(position - 1);
        Log.d("0713", String.valueOf(tObject.getTitle()));
        Intent intent = new Intent(getActivity(), TrendsDetail.class);
        intent.putExtra(TrendsDetail.TRENDS_OBJECT, tObject);
        ActivityCompat.startActivity(getActivity(), intent, null);
    }

    private TrendsObject buildTrendsModel(JSONArray contents, boolean byTitle) throws JSONException {
        for (int i = 0; i < contents.length(); i++) {
            JSONObject c = contents.getJSONObject(i);
            String title;
            if (channel != 5)
                title= c.getString(TAG_TITLE);
            else
                title = c.getString(TAG_ORIGIN_TITLE);
            int top = c.getInt(TAG_TOP);;
            String releaseDate = "";
            String mainInfo = "";
            String story = "";
            String trailerUrl ="";
            String posterUrl = "";
            String detailUrl = "";
            String country = "";

            //----- start dummy GalleryUrl ----
            JSONObject jo = new JSONObject();
            jo.put("type", "full");
            jo.put("url", "");
            JSONArray galleryFullUrl = new JSONArray();
            galleryFullUrl.put(jo);
            //----- end dummy GalleryUrl ----

            JSONArray data = new JSONArray();
            JSONArray staff = new JSONArray();
            JSONArray cast = new JSONArray();
            JSONArray review = new JSONArray();
            JSONArray gallery = new JSONArray();
            JSONObject rating = new JSONObject();

            if (c.has(TAG_DATA))
                data = c.getJSONArray(TAG_DATA);
            if (c.has(TAG_INFO))
                mainInfo = c.getString(TAG_INFO);
            if (c.has(TAG_STAFF))
                staff = c.getJSONArray(TAG_STAFF);
            if (c.has(TAG_CAST))
                cast = c.getJSONArray(TAG_CAST);
            if (c.has(TAG_REVIEW))
                review = c.getJSONArray(TAG_REVIEW);
            if (c.has(TAG_GALLERY_FULL))
                gallery = c.getJSONArray(TAG_GALLERY_FULL);
            if (c.has(TAG_DETAIL_URL))
                detailUrl = c.getString(TAG_DETAIL_URL);
            if (c.has(TAG_POSTER_URL))
                posterUrl = c.getString(TAG_POSTER_URL);
            if (c.has(TAG_COUNTRY)) {
                country = c.getString(TAG_COUNTRY);
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("data", country);
                data.put(2, jsonObj);
            }

            story = c.getString(TAG_STORY);
            trailerUrl = c.getString(TAG_TRAILER);
            rating = c.getJSONObject(TAG_RATING);
            releaseDate = c.getString(TAG_RELEASE);
            TrendsObject item = null;
            item = new TrendsObject(title, String.valueOf(top), detailUrl, posterUrl, trailerUrl, cast.toString(), review.toString(),
                    staff.toString(), data.toString(), story, mainInfo, gallery.toString(), rating.toString(), releaseDate);
            item.setChannel(channel);
            if (checkBookmark(title))
                item.setBookmark(true);

            if (byTitle)
                return item; // only one item in case of query by title
            trendsCardAdapter.addItem(i, item);
        }
        arrangeModel();
        trendsCardAdapter.notifyDataSetChanged();
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        channel = this.getArguments().getInt("index", 0);
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
        trendsCardAdapter = getAdapter();
        trendsCardAdapter.setOnItemClickListener(this); //onItemClick
        mRecyclerView.setAdapter(trendsCardAdapter);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView);
        favor = new TrendsFavoritePreference();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
    }

    private boolean checkBookmark(String title) {
        ArrayList list = favor.loadFavorites(getActivity());

        for (int i=0; i<list.size(); i++) {
            if (title.compareTo((String) list.get(i)) == 0) return true;
        }

        return false;
    };

    public void bindAdapter() {
        if (trendsCardAdapter == null) {
            trendsCardAdapter = getAdapter();
            trendsCardAdapter.setOnItemClickListener(this);
        }
        mRecyclerView.setAdapter(trendsCardAdapter);
        mRecyclerView.scheduleLayoutAnimation();
        trendsCardAdapter.notifyDataSetChanged();
        return;
    }

    public void addRecycleViewScollListener(final RecyclerView mRecyclerView) {
        SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
        Boolean Small = settings.getBoolean("miniCard", true);
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
                Log.d("0409", "count: " + linearLayoutManager.getItemCount() + " last: " + linearLayoutManager.findLastVisibleItemPosition()
                        + "loading: " + loading);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();

                if (!loading
                        && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    // Screen End has been reached
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

    public void arrangeModel() {
        Log.d("0416", "arrangetrendsModel");
        if (getContext() == null)
            return;
        SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
        boolean ascending = settings.getBoolean("ascending", false);
        List<TrendsObject> trendsContentItems = trendsCardAdapter.getItem();

        if (!ascending)
            Collections.sort(trendsContentItems, DescendingComparator);
        else
            Collections.sort(trendsContentItems, AscendingComparator);

        return;
    }

    public void clearModel() {
        List<TrendsObject> trendsContentItems = trendsCardAdapter.getItem();

        for (int i=trendsContentItems.size(); i >= 0; i--) {
            trendsCardAdapter.removeItem(i);
        }
    }

    private Comparator AscendingComparator = new Comparator<TrendsObject>() {
        @Override
        public int compare(TrendsObject o1, TrendsObject o2) {
            if (Integer.parseInt(o1.getTop()) > Integer.parseInt(o2.getTop())) {
                return 1;
            }
            else if (Integer.parseInt(o1.getTop()) < Integer.parseInt(o2.getTop())) {
                return -1;
            }
            return 0;
        }
    };

    private Comparator DescendingComparator = new Comparator<TrendsObject>() {
        @Override
        public int compare(TrendsObject o1, TrendsObject o2) {
            if (Integer.parseInt(o1.getTop()) < Integer.parseInt(o2.getTop())) {
                return 1;
            }
            else if (Integer.parseInt(o1.getTop()) > Integer.parseInt(o2.getTop())) {
                return -1;
            }
            return 0;
        }
    };

    private TrendsCardRecycleViewAdapter.OnLoadMoreListener onLoadMoreListener = new TrendsCardRecycleViewAdapter.OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            Log.d("0409", "loading more!");
            SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
            boolean ascending = settings.getBoolean("ascending", false);
            if (ascending && trendsCardAdapter.getItemCount() < trendMovieCount && channel == 0) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        requestDataRefresh(true, null, null);
                    }
                });
            } else if (ascending && trendsCardAdapter.getItemCount() < trendMovieCount && channel == 1) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        requestDataRefresh(true, null, null);
                    }
                });
            }
        }
    };
}
