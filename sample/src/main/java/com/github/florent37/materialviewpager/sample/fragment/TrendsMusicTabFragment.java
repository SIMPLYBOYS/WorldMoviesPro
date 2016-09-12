package com.github.florent37.materialviewpager.sample.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.sample.Config;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.SearchResultsAdapter;
import com.github.florent37.materialviewpager.sample.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.sample.framework.ResultListScrollListener;
import com.github.florent37.materialviewpager.sample.framework.Search;
import com.github.florent37.materialviewpager.sample.framework.SearchPresenter;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.TrendsObject;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by aaron on 2016/8/26.
 */
public class TrendsMusicTabFragment extends Fragment implements Search.View {
    private TrendsObject trendsObject;
    private RecyclerView resultsList;
    private SearchView searchView;
    private RequestQueue mQueue;
    private TrendsMusicTabFragment musicTabFragment;
    private LinearLayoutManager searchLayoutManager;
    private Search.ActionListener mActionListener;
    private static final String KEY_CURRENT_QUERY = "CURRENT_QUERY";
    private com.spotify.sdk.android.player.Player mPlayer;
    private SearchResultsAdapter searchAdapter;
    private ScrollListener mScrollListener;

    public static TrendsMusicTabFragment newInstance(TrendsObject trendsObject) {
        TrendsMusicTabFragment fragment = new TrendsMusicTabFragment();
        Bundle args = new Bundle();
        args.putSerializable("trends", trendsObject);
        fragment.setArguments(args);
        return fragment;
    }

    private class ScrollListener extends ResultListScrollListener {

        public ScrollListener(LinearLayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        public void onLoadMore() {
            mActionListener.loadMoreResults();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        musicTabFragment = this;
        trendsObject = (TrendsObject) getArguments().getSerializable("trends");
        final View view = inflater.inflate(R.layout.music_fragment, container, false);
        String token = CredentialsHandler.getToken(getActivity());

        if (token != null) {

            setupPlayer(token, view);

            if (savedInstanceState != null) {
                String currentQuery = savedInstanceState.getString(KEY_CURRENT_QUERY);
                mActionListener.search(currentQuery);
            }

        } else {

            mQueue = CustomVolleyRequestQueue.getInstance(getActivity()).getRequestQueue();
            CustomJSONObjectRequest jsonRequest_q = null;
            String refreshToken = CredentialsHandler.getRefreshToken(getActivity());
            String url = Config.HOST_NAME+"refresh_token?refresh_token="+refreshToken;

            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d("0828", String.valueOf(response));
                        long expires_in = 3600;
                        String token = response.getString("access_token");
                        CredentialsHandler.setToken(getActivity(), token, expires_in, TimeUnit.SECONDS);
                        setupPlayer(token, view);

                        if (savedInstanceState != null) {
                            String currentQuery = savedInstanceState.getString(KEY_CURRENT_QUERY);
                            mActionListener.search(currentQuery);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getActivity(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                }
            });

            mQueue.add(jsonRequest_q);
        }

        return view;
    }

    public void setupPlayer(String token, View view) {
        com.spotify.sdk.android.player.Config playerConfig = new com.spotify.sdk.android.player.Config(getActivity(), token, Config.CLIENT_ID);
        mPlayer = Spotify.getPlayer(playerConfig, this, new com.spotify.sdk.android.player.Player.InitializationObserver() {
            @Override
            public void onInitialized(com.spotify.sdk.android.player.Player player) {
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });

        searchLayoutManager = new LinearLayoutManager(getActivity());
        mScrollListener = new ScrollListener(searchLayoutManager);
        mActionListener = new SearchPresenter(getActivity(), this, mPlayer);
        mActionListener.init(token);
        // Setup search results list
        searchAdapter = new SearchResultsAdapter(getActivity(), new SearchResultsAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(View itemView, Track item) {
                mActionListener.selectTrack(item);
            }
        });
        resultsList = (RecyclerView) view.findViewById(R.id.search_results);
        resultsList.setHasFixedSize(true);
        resultsList.setLayoutManager(searchLayoutManager);
        resultsList.setAdapter(searchAdapter);
        resultsList.addOnScrollListener(mScrollListener);
//        mPlayer.play("spotify:track:5RkoL37LfZVEyYaHEvKgTc");
        // Setup search field
        searchView = (SearchView) view.findViewById(R.id.search_view);
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) view.findViewById(id);
        textView.setTextColor(Color.BLACK);

        searchView.post(new Runnable() {
            @Override
            public void run() {
                searchView.setQuery(trendsObject.getTitle(), true);
            }
        });

        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mActionListener.search(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mActionListener!= null)
            mActionListener.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mActionListener!= null)
            mActionListener.resume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActionListener.getCurrentQuery() != null) outState.putString(KEY_CURRENT_QUERY, mActionListener.getCurrentQuery());

    }

    @Override
    public void reset() {
        mScrollListener.reset();
        searchAdapter.clearData();
    }

    @Override
    public void addData(List<Track> items) {
        searchAdapter.addData(items);
    }

    @Override
    public void onDestroy() {
        mActionListener.destroy();
        super.onDestroy();
    }
}
