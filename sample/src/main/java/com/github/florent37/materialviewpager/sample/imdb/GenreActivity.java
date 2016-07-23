package com.github.florent37.materialviewpager.sample.imdb;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.fragment.MovieRecycleFragment;
import com.github.florent37.materialviewpager.sample.framework.AGVRecyclerViewAdapter;
import com.github.florent37.materialviewpager.sample.framework.AsymmetricItem;
import com.github.florent37.materialviewpager.sample.framework.AsymmetricRecyclerView;
import com.github.florent37.materialviewpager.sample.framework.AsymmetricRecyclerViewAdapter;
import com.github.florent37.materialviewpager.sample.framework.SpacesItemDecoration;
import com.github.florent37.materialviewpager.sample.framework.Utils;
import com.github.florent37.materialviewpager.sample.model.DemoItem;
import com.github.florent37.materialviewpager.sample.ui.BaseActivity;
import com.github.florent37.materialviewpager.sample.ui.widget.MultiSwipeRefreshLayout;
import com.github.florent37.materialviewpager.sample.util.DemoUtils;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by aaron on 2016/7/13.
 */
public class GenreActivity extends BaseActivity {
    private Toolbar toolbar;
    public static final String TAG = "genresActivity";
    private final DemoUtils demoUtils = new DemoUtils();
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;

    // SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Set<MovieRecycleFragment> mMovieRecycleFragments = new HashSet<MovieRecycleFragment>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);
        this.toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle("Movie Genre");
        toolbar.setTitleTextColor(Color.BLACK);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSupportActionBar(toolbar);
        registerHideableHeaderView(findViewById(R.id.headerbar));
        AsymmetricRecyclerView recyclerView = (AsymmetricRecyclerView) findViewById(R.id.recyclerView);


        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        textView.setBackgroundColor(Color.parseColor("#ff29b6f6"));
        textView.setLayoutParams(lParams);
        // 获得根视图并把TextView加进去。
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(demoUtils.moarItems(50));
        recyclerView.setRequestedColumnCount(3);
        recyclerView.setDebugging(true);
        recyclerView.setRequestedHorizontalSpacing(Utils.dpToPx(this, 3));
        recyclerView.addItemDecoration(
                new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_padding)));
        recyclerView.setAdapter(new AsymmetricRecyclerViewAdapter<>(this, recyclerView, adapter));

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_movie_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void trySetupSwipeRefresh() {

        mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */

        /*if (adapter.getItemCount() == 0) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                    fetchMovies(true);
                }
            });
        }*/

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }

    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_GENRE;
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {

        // Prevent the swipe refresh by returning true here
        if (mViewPagerScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
            return true;
        }

        for (MovieRecycleFragment fragment : mMovieRecycleFragments) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (!fragment.getUserVisibleHint()) {
                    continue;
                }
            }
            return ViewCompat.canScrollVertically(fragment.getRecyclerView(), -1);
        }

        return false;
    }

    class RecyclerViewAdapter extends AGVRecyclerViewAdapter<ViewHolder> {
        private final List<DemoItem> items;

        public RecyclerViewAdapter(List<DemoItem> items) {
            this.items = items;
        }

        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d("RecyclerViewActivity", "onCreateView");
            return new ViewHolder(parent, viewType);
        }

        @Override public void onBindViewHolder(ViewHolder holder, int position) {
            Log.d("RecyclerViewActivity", "onBindView position=" + position);
            holder.bind(items.get(position));
        }

        @Override public int getItemCount() {
            return items.size();
        }

        @Override public AsymmetricItem getItem(int position) {
            return items.get(position);
        }

        @Override public int getItemViewType(int position) {
            return position % 2 == 0 ? 1 : 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
                ImageView coverView;
        public ViewHolder(ViewGroup parent, int viewType) {
            super(LayoutInflater.from(parent.getContext()).inflate(
                    viewType == 0 ? R.layout.adapter_item : R.layout.adapter_item_odd, parent, false));
            if (viewType == 0) {
                textView = (TextView) itemView.findViewById(R.id.textview);
                coverView =(ImageView) itemView.findViewById(R.id.cover_iv);
                Picasso.with(coverView.getContext()).load("http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg").placeholder(R.drawable.placeholder).centerCrop().fit()
                        .into(coverView);

            } else {
                textView = (TextView) itemView.findViewById(R.id.textview_odd);
            }
        }

        public void bind(DemoItem item) {
            textView.setText(String.valueOf(item.getPosition()));
        }
    }
}
