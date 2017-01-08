package com.github.florent37.materialviewpager.worldmovies.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.FriendsListAdapter;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by aaron on 2016/11/19.
 */

public class FriendsFragment extends Fragment {
    private FriendsListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_list_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        String friends = UsersUtils.getCurrentFriends(getActivity()); //TODO get from social api
        Gson gson = new Gson();
        List<User> friendslist = gson.fromJson(friends, new TypeToken<List<User>>(){}.getType());

        for (final User user : friendslist) {
            user.description = "";
            user.total = 0;
        }

        mAdapter = new FriendsListAdapter(friendslist, getActivity());
        recyclerView.setAdapter(mAdapter);

        return view;
    }
}
