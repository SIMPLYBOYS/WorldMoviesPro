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
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.adapter.ImdbCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.imdb.MovieDetail;
import com.github.florent37.materialviewpager.sample.model.ImdbObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aaron on 2016/4/15.
 */
public class ImdbFragment extends RecyclerViewFragment implements AdapterView.OnItemClickListener {

    // JSON Node names
    private static final String TAG_TITLE = "title";
    private static final String TAG_YEAR = "year";
    private static final String TAG_RELEASE = "releaseDate";
    private static final String TAG_TOP = "top";
    private static final String TAG_POSTER_URL = "posterUrl";
    private static final String TAG_RATING = "rating";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_DETAIL_URL = "detailUrl";
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

    private ArrayList<HashMap<String, String>> contentList;
    private ArrayList<HashMap<String, String>> galleryList;
    private int visibleThreshold = 1;
//    private ArrayList<ImdbObject> imdbCollection = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private ImdbCardRecycleViewAdapter imdbCardAdapter;
    private boolean loading;
    private int titleIndex = 0; //default
    private int lastVisibleItem, totalItemCount, channel;

    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;

    public RecyclerView getInitiatedRecyclerView() {
        return mRecyclerView;
    }

    public RecyclerView.Adapter getInitiatedAdapter() {
        return imdbCardAdapter;
    }

    public static ImdbFragment newInstance(int position) {
        ImdbFragment fragment = new ImdbFragment();
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
            Log.d("0607", "fragment attached");
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

    public ImdbCardRecycleViewAdapter
    getAdapter() {
        return new ImdbCardRecycleViewAdapter(getContext());
    }

    @Override
    public void
    onResponse(Object response) {
        try {
            contentList = new ArrayList<HashMap<String, String>>();
            galleryList = new ArrayList<HashMap<String, String>>();
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            boolean byTitle = ((JSONObject) response).getBoolean("byTitle");
            ImdbObject item = buildImdbModel(contents, byTitle);
            loading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            Log.d("0515", String.valueOf(byTitle));

            if (byTitle) {
                Intent intent = new Intent(getActivity(), MovieDetail.class);
                intent.putExtra(MovieDetail.IMDB_OBJECT, item);
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
        Log.d("0606", String.valueOf(error.getMessage()));
        Toast.makeText(getContext(), "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(),
                "Clicked: " + position + ", index " + mRecyclerView.indexOfChild(view),
                Toast.LENGTH_SHORT).show();
        ImdbObject imdbObject = imdbCardAdapter.getItem().get(position - 1);
        Intent intent = new Intent(getActivity(), MovieDetail.class);
        intent.putExtra(MovieDetail.IMDB_OBJECT, imdbObject);
        /*Bundle bundle = new Bundle();
        bundle.putSerializable(MovieDetail.IMDB_COLLECTION, imdbCollection);
        intent.putExtra(MovieDetail.IMDB_COLLECTION, bundle);*/
        ActivityCompat.startActivity(getActivity(), intent, null);
    }

    private ImdbObject buildImdbModel(JSONArray contents, boolean byTitle) throws JSONException {
        channel = this.getArguments().getInt("index", 0);
        for (int i = 0; i < contents.length(); i++) {
            JSONObject c = contents.getJSONObject(i);
            String title = c.getString(TAG_TITLE);
            JSONObject d = c.getJSONObject("detailContent");
            int top = 0;
            String detailPosterUrl = "";
            String year = "";
            String posterUrl = "http://www.imdb.com/title/tt1355631/mediaviewer/rm3798736128?ref_=tt_ov_i";
            String delta = "0";
            //----- start dummy GalleryUrl ----
            JSONObject jo = new JSONObject();
            jo.put("type", "full");
            jo.put("url", "");
            JSONArray galleryFullUrl = new JSONArray();
            galleryFullUrl.put(jo);
            //----- end dummy GalleryUrl ----

            if (c.has(TAG_TOP)) {
                Log.d("0518", title);
                top = c.getInt(TAG_TOP);
                if (top > offSet)
                    offSet = top;
            }
            year = c.getString(TAG_YEAR);

            if (c.has(TAG_RELEASE) && !c.has(TAG_TOP)) {
                year = String.valueOf(c.getInt(TAG_RELEASE));
                year = year.substring(4, 8);
            }

            if (c.has(TAG_DELTA)) {
                delta = c.getString(TAG_DELTA);
            }

            String description= c.getString(TAG_DESCRIPTION);
            String rating = c.getString(TAG_RATING);

            if (c.has(TAG_POSTER_URL)) {
                posterUrl = c.getString(TAG_POSTER_URL);
            }

            String plot = c.getString(TAG_PLOT);
            String genre = c.getString(TAG_GENRE);
            String votes = c.getString(TAG_VOTES);
            String runTime = c.getString(TAG_RUNTIME);
            String metaScore = c.getString(TAG_METASCORE);

            String summery = d.getString(TAG_SUMMERY);
            String country = d.getString(TAG_COUNTRY);

            if (c.has(TAG_GALLERY_FULL)) {
                galleryFullUrl = c.getJSONArray(TAG_GALLERY_FULL);
            }

            String trailerUrl;
            String slate;

            if (c.has(TAG_TRAILER))
                trailerUrl = c.getString(TAG_TRAILER);
            else
                trailerUrl = "N/A";

            if (d.has(TAG_SLATE))
                slate = d.getString(TAG_SLATE);
            else
                slate = "N/A";

            HashMap<String, String> content = new HashMap<String, String>();
            HashMap<String, String> gallery = new HashMap<String, String>();
            content.put(TAG_TITLE, title);
            if (channel == 0)
                content.put(TAG_TOP, String.valueOf(top));
            content.put(TAG_YEAR, year);
//            content.put(TAG_DETAIL_URL, detailUrl);
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
        }

//        imdbCollection = new ArrayList<ImdbObject>();
        ImdbObject item = null;

        for (int j = 0; j < contentList.size(); j++) {
            HashMap<String, String> content = contentList.get(j);
            HashMap<String, String> gallery = galleryList.get(j);
            item = new ImdbObject(content.get(TAG_TITLE), content.get(TAG_TOP), content.get(TAG_YEAR), content.get(TAG_DESCRIPTION),
                    content.get(TAG_RATING), content.get(TAG_POSTER_URL), content.get(TAG_SLATE), content.get(TAG_SUMMERY), content.get(TAG_PLOT),
                    content.get(TAG_GENRE), content.get(TAG_VOTES), content.get(TAG_RUNTIME), content.get(TAG_METASCORE), content.get(TAG_DELTA), content.get(TAG_COUNTRY),
                    content.get(TAG_TRAILER), gallery.get(TAG_GALLERY_FULL));
            if (byTitle)
                return item; // only one item in case of query by title
            imdbCardAdapter.addItem(j, item);
//            imdbCollection.add(item);
        }
        arrangeModel();
        imdbCardAdapter.notifyDataSetChanged();
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        imdbCardAdapter = getAdapter();
        imdbCardAdapter.setOnItemClickListener(this); //onItemClick
        mRecyclerView.setAdapter(imdbCardAdapter);

        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_refresh_layout);
    }

    public void bindAdapter() {
        if (imdbCardAdapter == null) {
            imdbCardAdapter = getAdapter();
            imdbCardAdapter.setOnItemClickListener(this);
        }
        mRecyclerView.setAdapter(imdbCardAdapter);
        mRecyclerView.scheduleLayoutAnimation();
        imdbCardAdapter.notifyDataSetChanged();
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
                    // End has been reached
                    Log.d("0409", "case1");
                    if (onLoadMoreListener != null) {
                        Log.d("0409", "case2");
                        onLoadMoreListener.onLoadMore();
                    }
//                    loading = true;
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
        Log.d("0416", "arrangeImdbModel");
        if (getContext() == null)
            return;
        SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
        boolean ascending = settings.getBoolean("ascending", false);
        List<ImdbObject> mImdbContentItems = imdbCardAdapter.getItem();

        if (!ascending)
            Collections.sort(mImdbContentItems, DescendingComparator);
        else
            Collections.sort(mImdbContentItems, AscendingComparator);

        return;
    }

    public void clearModel() {
        List<ImdbObject> mImdbContentItems = imdbCardAdapter.getItem();

        for (int i=mImdbContentItems.size(); i >= 0; i--) {
            imdbCardAdapter.removeItem(i);
        }
//        imdbCardAdapter.notifyDataSetChanged();
    }

    // for IMDB only
    private Comparator AscendingComparator = new Comparator<ImdbObject>() {
        @Override
        public int compare(ImdbObject o1, ImdbObject o2) {
            switch (channel) {
                case 0:
                    if (Integer.parseInt(o1.getTop()) > Integer.parseInt(o2.getTop())) {
                        return 1;
                    }
                    else if (Integer.parseInt(o1.getTop()) < Integer.parseInt(o2.getTop())) {
                        return -1;
                    }
                case 1:
                    if (Integer.parseInt(o1.getYear()) > Integer.parseInt(o2.getYear())) {
                        return 1;
                    }
                    else if (Integer.parseInt(o1.getYear()) < Integer.parseInt(o2.getYear())) {
                        return -1;
                    }
            }
            return 0;
        }
    };

    // for IMDB only
    private Comparator DescendingComparator = new Comparator<ImdbObject>() {
        @Override
        public int compare(ImdbObject o1, ImdbObject o2) {
            switch (channel) {
                case 0:
                    if (Integer.parseInt(o1.getTop()) < Integer.parseInt(o2.getTop())) {
                        return 1;
                    }
                    else if (Integer.parseInt(o1.getTop()) > Integer.parseInt(o2.getTop())) {
                        return -1;
                    }
                case 1:
                    if (Integer.parseInt(o1.getYear()) < Integer.parseInt(o2.getYear())) {
                        return 1;
                    }
                    else if (Integer.parseInt(o1.getYear()) > Integer.parseInt(o2.getYear())) {
                        return -1;
                    }
            }
            return 0;
        }
    };

    private ImdbCardRecycleViewAdapter.OnLoadMoreListener onLoadMoreListener = new ImdbCardRecycleViewAdapter.OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            Log.d("0409", "loading more!");
            SharedPreferences settings = getContext().getSharedPreferences("settings", 0);
            boolean ascending = settings.getBoolean("ascending", false);
            if (ascending && imdbCardAdapter.getItemCount() < top250MovieCount && channel == 0) {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        requestDataRefresh(true, null, null);
                    }
                });
            } else if (ascending && imdbCardAdapter.getItemCount() < upComingMovieCount && channel == 1) {
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
