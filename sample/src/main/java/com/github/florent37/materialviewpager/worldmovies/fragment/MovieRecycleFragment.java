package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.adapter.RecyclerViewMaterialAdapter;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.TestRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/3/22.
 */
public class MovieRecycleFragment extends Fragment {

    private RecyclerView mRecyclerView;

    private RecyclerView.Adapter mAdapter;

    private List<TestRecyclerViewAdapter.MyObject> mContentItems = new ArrayList<>();

    public static MovieRecycleFragment newInstance() {
        return new MovieRecycleFragment();
    }

    public interface Listener {
        public void onFragmentViewCreated(Fragment fragment);
        public void onFragmentAttached(MovieRecycleFragment fragment);
        public void onFragmentDetached(MovieRecycleFragment fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("0321", "onViewCreated");
        if (getActivity() instanceof Listener) {
            ((Listener) getActivity()).onFragmentViewCreated(this);
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        fetchContents(0);
        mAdapter = new RecyclerViewMaterialAdapter(new TestRecyclerViewAdapter(mContentItems));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        MaterialViewPagerHelper.registerRecyclerView(getActivity(), mRecyclerView);
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

    private void fetchContents(int titleIndex) {
        Log.d("0304", String.valueOf(titleIndex));
        //TODO get JSON data from REST API server by titleIndex
        // mContentItems.add(new MyObject(item0, item1, item2));
        mContentItems.add(new TestRecyclerViewAdapter.MyObject("France", "5/28 am 9:30", "http://www.telegraph.co.uk/travel/destination/article130148.ece/ALTERNATES/w620/parisguidetower.jpg"));
        mContentItems.add(new TestRecyclerViewAdapter.MyObject("Angleterre", "5/28 am 10:30", "http://www.traditours.com/images/Photos%20Angleterre/ForumLondonBridge.jpg"));
        mContentItems.add(new TestRecyclerViewAdapter.MyObject("Allemagne", "5/28 am 11:30", "http://tanned-allemagne.com/wp-content/uploads/2012/10/pano_rathaus_1280.jpg"));
        mContentItems.add(new TestRecyclerViewAdapter.MyObject("Espagne", "5/28 pm 00:30", "http://www.sejour-linguistique-lec.fr/wp-content/uploads/espagne-02.jpg"));
        mContentItems.add(new TestRecyclerViewAdapter.MyObject("Italie", "5/28 pm 1:30", "http://retouralinnocence.com/wp-content/uploads/2013/05/Hotel-en-Italie-pour-les-Vacances2.jpg"));
        mContentItems.add(new TestRecyclerViewAdapter.MyObject("Russie", "5/28 pm 2:30", "http://www.choisir-ma-destination.com/uploads/_large_russie-moscou2.jpg"));
    }

    public RecyclerView getRecyclerView(){
        return mRecyclerView;
    }
}
