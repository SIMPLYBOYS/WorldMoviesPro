package com.github.florent37.materialviewpager.worldmovies.favorite;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.fragment.FragmentAdapter;
import com.github.florent37.materialviewpager.worldmovies.fragment.FriendsFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.LeaderBoardTabFragment;

/**
 * Created by aaron on 2016/11/19.
 */

public class ExplorePeopleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_people);
        setTitle(null); // ""
        setupToolbar();
        setViewPager();
    }

    protected void setViewPager() {
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new LeaderBoardTabFragment(), "LearderBoard");
        adapter.addFragment(new FriendsFragment(), "Friends");
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
