package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.HomeRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.TrendsCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesMovie;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/12/15.
 */

public class HomeFragment extends RecyclerViewFragment {
    private RecyclerView HomeRecyclerview;
    private HomeRecycleViewAdapter HomeAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager homelinearLayoutManager;
    private List<nyTimesMovie> HomeList;
    private ProgressBar progressBar;
    private RequestQueue mQueue;
    private boolean loading;
    private int visibleThreshold = 1;
    private int lastVisibleItem, totalItemCount, channel;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOGD("1118", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        channel = this.getArguments().getInt("index", 0);
        HomeList = new ArrayList<>();
        mQueue = CustomVolleyRequestQueue.getInstance(getActivity()).getRequestQueue();
        homelinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        user = UsersUtils.getCurrentUser(getApplicationContext());
        HomeRecyclerview = (RecyclerView) getView().findViewById(R.id.recyclerView);
        HomeRecyclerview.setLayoutManager(homelinearLayoutManager);
        HomeRecyclerview.scheduleLayoutAnimation();
        HomeAdapter = new HomeRecycleViewAdapter(HomeList, getActivity());
        HomeRecyclerview.setAdapter(HomeAdapter);
        addRecycleViewScollListener(HomeRecyclerview);
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), HomeRecyclerview);

        if (user != null) {
//            fetch_home(); TODO
            HomeList.add(HomeList.size(), new nyTimesMovie("Bleed for This’ Is a Boxing nyTimesMovie That Gets Boxing", null, null, "http://www.nytimes.com/2016/11/18/movies/bleed-for-this-review-miles-teller.html"
                    , "https://static01.nyt.com/images/2016/11/18/arts/18BLEED/18BLEED-superJumbo.jpg", null, null));
            HomeList.add(HomeList.size(), new nyTimesMovie("Nocturnal Animals,’ Brutality Between the Pages and Among the Fabulous", null, null, "http://www.nytimes.com/2016/11/18/movies/nocturnal-animals-review-amy-adams-jake-gyllenhaal.html"
                    , "https://static01.nyt.com/images/2016/11/18/arts/18NOCTURNAL1/18NOCTURNAL1-superJumbo-v2.jpg", null, null));
            HomeList.add(HomeList.size(), new nyTimesMovie("Nocturnal Animals,’ Brutality Between the Pages and Among the Fabulous", null, null, "http://www.nytimes.com/2016/11/18/movies/nocturnal-animals-review-amy-adams-jake-gyllenhaal.html"
                    , "https://static01.nyt.com/images/2016/11/18/arts/18NOCTURNAL1/18NOCTURNAL1-superJumbo-v2.jpg", null, null));
            HomeList.add(HomeList.size(), new nyTimesMovie("Nocturnal Animals,’ Brutality Between the Pages and Among the Fabulous", null, null, "http://www.nytimes.com/2016/11/18/movies/nocturnal-animals-review-amy-adams-jake-gyllenhaal.html"
                    , "https://static01.nyt.com/images/2016/11/18/arts/18NOCTURNAL1/18NOCTURNAL1-superJumbo-v2.jpg", null, null));
        }
    }

    public RecyclerView getInitiatedRecyclerView() {
        return HomeRecyclerview;
    }

    public RecyclerView.Adapter getInitiatedAdapter() {
        return HomeAdapter;
    }

    public static HomeFragment newInstance(int position) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt("index", position);
        fragment.setArguments(args);
        return fragment;
    }

    public void arrangeModel() {
        LOGD("0416", "arrangetrendsModel");
        //Do nothing
        return;
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

    private void fetch_home() {
        CustomJSONArrayRequest jsonRequest = new CustomJSONArrayRequest(Config.HOST_NAME + "homeTopic/"+user.id, new Response.Listener<JSONArray>() {
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
                        HomeList.add(HomeList.size(), movie);
                    }

                    if (getView() == null)
                        return;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getActivity(), "Remote Server connect fail from FavoriteInfoTabFragment!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
        if (isVisible() && channel > 0)
            requestDataRefresh(false, null, null);
    }

    @Override
    public void onResponse(Object response) {
        try {
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");

            if (getActivity() == null)
                return;

            boolean byTitle = ((JSONObject) response).getBoolean("byTitle");
//            TrendsObject item = buildTrendsModel(contents, byTitle);
            loading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            /*if (byTitle) {
                Intent intent = new Intent(getActivity(), TrendsDetail.class);
                intent.putExtra(TrendsDetail.TRENDS_OBJECT, item);
                ActivityCompat.startActivity(getActivity(), intent, null);
            }*/

        } catch (JSONException e) {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void bindAdapter() {

        if (HomeAdapter == null)
            HomeAdapter = getAdapter();

        HomeRecyclerview.setAdapter(HomeAdapter);
        HomeRecyclerview.scheduleLayoutAnimation();
        HomeAdapter.notifyDataSetChanged();
        return;
    }

    public void addRecycleViewScollListener(final RecyclerView mRecyclerView) {
        SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        visibleThreshold = 1;

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition = (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
                LOGD("0409", "count: " + linearLayoutManager.getItemCount() + " last: " + linearLayoutManager.findLastVisibleItemPosition()
                        + "loading: " + loading);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager
                        .findLastVisibleItemPosition();

                if (!loading
                        && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    // Screen End has been reached
                    if (onLoadMoreListener != null) {
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

    public HomeRecycleViewAdapter getAdapter() {
        return new HomeRecycleViewAdapter(HomeList, getActivity());
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mSwipeRefreshLayout.setRefreshing(false);
        Toast.makeText(getContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
    }

    private TrendsCardRecycleViewAdapter.OnLoadMoreListener onLoadMoreListener = new TrendsCardRecycleViewAdapter.OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            LOGD("0409", "loading more!");
            SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
            boolean ascending = settings.getBoolean("ascending", false);
            if (ascending && HomeAdapter.getItemCount() < topicBoard) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        requestDataRefresh(true, null, null);
                    }
                });
            } else if (ascending && HomeAdapter.getItemCount() < trendMovieCount) {
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

