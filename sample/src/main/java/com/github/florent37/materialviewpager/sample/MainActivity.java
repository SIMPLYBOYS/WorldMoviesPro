package com.github.florent37.materialviewpager.sample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.sample.adapter.TrendsCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.sample.fragment.DefaultFragment;
import com.github.florent37.materialviewpager.sample.fragment.RecyclerViewFragment;
import com.github.florent37.materialviewpager.sample.fragment.trendsFragment;
import com.github.florent37.materialviewpager.sample.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.sample.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.sample.model.TrendsObject;
import com.github.florent37.materialviewpager.sample.ui.BaseActivity;
import com.github.florent37.materialviewpager.sample.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity implements RecyclerViewFragment.Listener, Response.Listener, Response.ErrorListener {

    private MaterialViewPager mViewPager;
    public static final String REQUEST_TAG = "MainVolleyActivity";
    private Context mContext;
    private SwipeRefreshLayout mSwipeRefreshLayout;// SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private boolean mActionBarShown = true;
    private String HOST_NAME = Config.HOST_NAME;
    private Toolbar toolbar;
    private RequestQueue mQueue;
    private boolean mSearchCheck;
    View headerLogo;
    ImageView headerLogoContent;
    private int mProgressBarTopWhenActionBarShown;
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private Set<RecyclerViewFragment> mMyRecyclerViewFragments = new HashSet<RecyclerViewFragment>();
    private static final long GET_DATA_INTERVAL = 10000;
    private Handler mHandler;
    private TimerTask tTask;
    private Timer tTimer;
    private int SlideIndex;
    private FragmentStatePagerAdapter pagerAdapter;
    private SharedPreferences sp;
    final int tabCount = 4; //TODO title items design
    public static final String FILM_NAME = "filmName";
    private SimpleCursorAdapter mAdapter;
    private static String[] MOVIES = {};
    private SearchView searchView = null;
    private MenuItem searchItem = null;
    MaterialDialog.Builder builder;
    MaterialDialog dialog;

    public class MainPagerAdapter extends FragmentStatePagerAdapter {
        public MainPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int position) {
            Log.d("0330", "getItem: " + position);
            switch (position){
                case 0:
                    return trendsFragment.newInstance(position);
                case 1:
                    return trendsFragment.newInstance(position);
                default:
                    return DefaultFragment.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            return tabCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position % 4) {
                case 0:
                    return getResources().getString(R.string.Japan);
                case 1:
                    return getResources().getString(R.string.USA);
                case 2:
                    return getResources().getString(R.string.Taiwan);
                case 3:
                    return getResources().getString(R.string.Korea);
                default:
                    return "Page " + position;
            }
        }

        int oldItemPosition = -1;

        @Override
        public void setPrimaryItem(ViewGroup container, final int position, Object object) {
            super.setPrimaryItem(container, position, object);
            Log.d("0303", "setPrimaryItem");

            if (oldItemPosition != position) {
                oldItemPosition = position;

                String imageUrl = null;
                int color = Color.BLACK;
                Drawable newDrawable = null;

                switch (position) {
                    case 0:
                        imageUrl = "http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg";
                        color = getResources().getColor(R.color.purple);
                        newDrawable = getResources().getDrawable(R.drawable.japan);
                        break;
                    case 1:
                        imageUrl = "http://ia.media-imdb.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX640_SY720_.jpg";
                        color = getResources().getColor(R.color.material_orange_900);
                        newDrawable = getResources().getDrawable(R.drawable.usa);
                        break;
                    case 2:
                        imageUrl = "http://soocurious.com/fr/wp-content/uploads/2014/03/8-facettes-de-notre-cerveau-qui-ont-evolue-avec-la-technologie8.jpg";
                        color = getResources().getColor(R.color.com_facebook_button_background_color);
                        newDrawable = getResources().getDrawable(R.drawable.taiwan);
                        break;
                    case 3:
                        imageUrl = "http://graduate.carleton.ca/wp-content/uploads/prog-banner-masters-international-affairs-juris-doctor.jpg";
                        color = getResources().getColor(R.color.green_teal);
                        newDrawable = getResources().getDrawable(R.drawable.korea);
                        break;
                }

                int fadeDuration = 250;
                mViewPager.setColor(color, fadeDuration);
                mViewPager.setImageUrl(imageUrl, fadeDuration);
                toggleLogo(newDrawable, color, fadeDuration);

                for (final RecyclerViewFragment fragment : mMyRecyclerViewFragments) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                        if (!fragment.getUserVisibleHint()) {
                            continue;
                        }
                    }
                    fragment.requestDataRefresh(false, null, null);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_main);
        getOverflowMenu();
        Log.d("0224", "onCreate");
        mContext = this;

        /*ContentResolver mContentResolver = mContext.getContentResolver();

        ContentProviderClient provider = mContentResolver.acquireContentProviderClient("com.github.florent37.materialviewpager.sample");
        Log.d("0224", "provider: " + provider);*/

        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());

        setTitle("");
        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
        headerLogo = findViewById(R.id.headerLogo);
        headerLogoContent = (ImageView) findViewById(R.id.headerLogoContent);
        toolbar = mViewPager.getToolbar();

        if (toolbar != null) {
            setSupportActionBar(toolbar);

            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
            }
        }

        pagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.getViewPager().setAdapter(pagerAdapter);
        mViewPager.getViewPager().setOffscreenPageLimit(mViewPager.getViewPager().getAdapter().getCount());
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());
        sp = getSharedPreferences("CURRENT_PAGE", Context.MODE_PRIVATE);
        Log.d("0328", "current_page: " + readCurrentPagePref());
        mViewPager.getViewPager().setCurrentItem(readCurrentPagePref(), true);
        View logo = findViewById(R.id.headerLogo);
        mViewPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.notifyHeaderChanged();
                Toast.makeText(getApplicationContext(), "Yes, the title is clickable", Toast.LENGTH_SHORT).show();
            }
        });

        if (logo != null)
            logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.notifyHeaderChanged();
                    Toast.makeText(getApplicationContext(), "Yes, the title is clickable", Toast.LENGTH_SHORT).show();
                    /*RecyclerViewFragment fragment = showVisibleFragment();
                    fragment.requestDataRefresh(true, null);*/
                    showVisibleFragment().requestDataRefresh(true, null, null);
                }
            });

        SharedPreferences settings = getSharedPreferences("settings", 0);
        final boolean miniCard = settings.getBoolean("miniCard", true);

        //------------ Start of Timer -------------//

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Random random = new Random();
                RecyclerViewFragment fragment = showVisibleFragment();
                int channel = fragment.getArguments().getInt("index", 0);
                int SlideIndex = 0;

                switch (channel) {
                    case 0:
                        TrendsCardRecycleViewAdapter trendsAdapter = (TrendsCardRecycleViewAdapter)fragment.setupRecyclerAdapter();

                        if (trendsAdapter == null)
                            return;
                        List<TrendsObject> mContentItems = trendsAdapter.getItem();
                        Log.d("0419", String.valueOf(mContentItems.size()));

                        if (mContentItems.size() == 0)
                            return;

                        SlideIndex = random.nextInt(mContentItems.size());
                        /*if (mContentItems.get(SlideIndex).getSlate().equals("N/A"))
                            mViewPager.setImageUrl(mContentItems.get(SlideIndex).getPosterUrl(), 250);
                        else
                            mViewPager.setImageUrl(mContentItems.get(SlideIndex).getSlate(), 250);*/
                        break;
                    default:
//                        DefaultCardRecycleViewAdapter adapter_2 = (DefaultCardRecycleViewAdapter)fragment.setupRecyclerAdapter();
                        //Do nothing
                        break;
                }
            }
        };

        tTask = new TimerTask() {
            @Override
            public void run() {
                SlideIndex++;
                Message message = new Message();
                message.what = 1; //unused
                mHandler.sendMessage(message);
            }
        };

        //------------ End of Timer -------------//

        loadHints(); //chaching for search hint
        overridePendingTransition(0, 0);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private void loadHints() {
        final String[] from = new String[]{FILM_NAME};
        final int[] to = new int[]{android.R.id.text1};
        final CustomJSONObjectRequest jsonRequest;
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.hint_row,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();

        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "/imdb_title", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("0419", "title onResponse");
                    JSONArray contents = ((JSONObject) response).getJSONArray("contents");
                    MOVIES = getStringArray(contents);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void toggleLogo(final Drawable newLogo, final int newColor, int duration) {

        final AnimatorSet animatorSetDisappear = new AnimatorSet();
        animatorSetDisappear.setDuration(duration);
        animatorSetDisappear.playTogether(
                ObjectAnimator.ofFloat(headerLogo, "scaleX", 0),
                ObjectAnimator.ofFloat(headerLogo, "scaleY", 0)
        );

        final AnimatorSet animatorSetAppear = new AnimatorSet();
        animatorSetAppear.setDuration(duration);
        animatorSetAppear.playTogether(
                ObjectAnimator.ofFloat(headerLogo, "scaleX", 1),
                ObjectAnimator.ofFloat(headerLogo, "scaleY", 1)
        );

        animatorSetDisappear.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                ((GradientDrawable) headerLogo.getBackground()).setColor(newColor);

                headerLogoContent.setImageDrawable(newLogo);

                animatorSetAppear.start();
            }
        });

        animatorSetDisappear.start();
    }

    private void getOverflowMenu() {
        try{
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null){
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void restoreCurrentPagePref() {
        SharedPreferences.Editor edit = sp.edit();
        edit.putInt("current_page", mViewPager.getViewPager().getCurrentItem());
        edit.apply();
    }

    private int readCurrentPagePref(){
        return sp.getInt("current_page",0);
    }

    @Override
    public void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        int top = mActionBarShown ? mProgressBarTopWhenActionBarShown : 0;
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);
    }

    @Override
    public void trySetupSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RecyclerViewFragment fragment = showVisibleFragment();
                Log.d("0513", "count: " + fragment.getInitiatedAdapter().getItemCount());
                int channel = fragment.getArguments().getInt("index", 0);
                switch (channel) {
                    case 0:
                        if (fragment.getInitiatedAdapter().getItemCount() < fragment.trendMovieCount)
                            fragment.requestDataRefresh(true, null, null);
                        else
                            mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    case 1:
                        if (fragment.getInitiatedAdapter().getItemCount() < fragment.upComingMovieCount)
                            fragment.requestDataRefresh(true, null, null);
                        else
                            mSwipeRefreshLayout.setRefreshing(false);
                        break;
                }

            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.green_teal,
                R.color.material_orange_800, R.color.red);

        if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
            MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
            mswrl.setCanChildScrollUpCallback(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pager_menu, menu);
        SharedPreferences settings = getSharedPreferences("settings", 0);
//        boolean isChecked = settings.getBoolean("miniCard", false);    boolean isChecked = settings.getBoolean("miniCard", false);

        for (int i=0; i< menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            SpannableString spanString = new SpannableString(menu.getItem(i).getTitle().toString());
            spanString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spanString.length(), 0);
            item.setTitle(spanString);
        }

        MenuItem miniCard = menu.findItem(R.id.menu_miniCard);
        MenuItem ascending = menu.findItem(R.id.menu_ascending);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        miniCard.setChecked(settings.getBoolean("miniCard", true));
        ascending.setChecked(settings.getBoolean("ascending", false));

        AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("0419", "submit query text: " + query);
                RecyclerViewFragment fragment = showVisibleFragment();

//                fragment.removeAdapterModel();
                fragment.requestDataRefresh(true, query, null);

                //if you want to collapse the searchview
                invalidateOptionsMenu();
                mSearchCheck = false;
                mSearchCheck = false;
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                mSearchCheck = true;
                Log.d("0418", "query text change!");
                giveSuggestions(query);
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor)searchView.getSuggestionsAdapter().getItem(position);
                String feedName = cursor.getString(1);
                searchView.setQuery(feedName, false);
//                searchView.clearFocus();
                return true;
            }
        });

        searchView.setSuggestionsAdapter(mAdapter);

        return true;
    }

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME});
        for (int i = 0; i < MOVIES.length; i++) {
            if (MOVIES[i].toLowerCase().contains(query.toLowerCase()))
                cursor.addRow(new Object[]{i, MOVIES[i]});
        }
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            if (isNavDrawerOpen()) {
                closeNavDrawer();
            }
            dialog.show();
        }
    }

    @Override
    public void onResponse(Object response) {
        try {
            Log.d("0419", "title onResponse");
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            MOVIES = getStringArray(contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String[] getStringArray(JSONArray jsonArray) {
        String[] stringArray = null;
        int length = jsonArray.length();
        if (jsonArray!=null) {
            stringArray = new String[length];
            for(int i=0;i<length;i++){
                stringArray[i]= jsonArray.optString(i);
            }
        }
        return stringArray;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    private void changeSearchViewTextColor(View view) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.WHITE);
                return;
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RecyclerViewFragment fragment;
        RecyclerView view;
        switch (item.getItemId()) {
            case R.id.menu_miniCard:
                item.setChecked(!item.isChecked());
                SharedPreferences miniCard = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor_miniCard = miniCard.edit();
                editor_miniCard.putBoolean("miniCard", item.isChecked());
                editor_miniCard.commit();
                arrangeFragments();
                return true;
            case R.id.menu_refresh:
                fragment = showVisibleFragment();
                fragment.requestDataRefresh(true, null, null);
                return true;
            case R.id.menu_smooth_zero:
                fragment = showVisibleFragment();
                view = fragment.setupRecyclerView();
                view.smoothScrollToPosition(0);
                return true;
            case R.id.menu_smooth_end:
                fragment = showVisibleFragment();
                view = fragment.setupRecyclerView();
                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) fragment.setupRecyclerView().getLayoutManager();
                view.smoothScrollToPosition(linearLayoutManager.getItemCount());
                return true;
            case R.id.menu_ascending:
                item.setChecked(!item.isChecked());
                SharedPreferences ascending = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor_ascending = ascending.edit();
                editor_ascending.putBoolean("ascending", item.isChecked());
                editor_ascending.commit();
                arrangeFragments();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private RecyclerViewFragment showVisibleFragment() {
        for (RecyclerViewFragment fragment : mMyRecyclerViewFragments) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (!fragment.getUserVisibleHint()) {
                    continue;
                }
                return fragment;
            }
        }
        return null;
    }

    private void arrangeFragments() {
        if (mMyRecyclerViewFragments == null)
            return;
        for (RecyclerViewFragment mfragment : mMyRecyclerViewFragments) {
            mfragment.setupArrangeModel();
            mfragment.setAdapterBinding();
        }
    }

    @Override
    public void onFragmentViewCreated(RecyclerViewFragment fragment) {
        int titleIndex = fragment.getArguments().getInt("index", 0);
        Log.d("0311", "onFragmentViewCreated: " + titleIndex);

        /*if (titleIndex < 0) {
            fragment.setListAdapter(mDayZeroAdapter);
            fragment.getListView().setRecyclerListener(mDayZeroAdapter);
        } else {
            fragment.setListAdapter(mScheduleAdapters[dayIndex]);
            fragment.getListView().setRecyclerListener(mScheduleAdapters[dayIndex]);
        }*/
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState){
        super.onSaveInstanceState(saveInstanceState);
        restoreCurrentPagePref();
    }

    @Override
    protected void onRefreshingStateChanged(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        restoreCurrentPagePref();
    }

    @Override
    protected void enableDisableSwipeRefresh(boolean enable) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(enable);
        }
    }

    @Override
    public void onFragmentAttached(RecyclerViewFragment fragment) {
        Log.d("0311", "onFragmentAttached: ");
        mMyRecyclerViewFragments.add(fragment);
    }

    @Override
    public void onFragmentDetached(RecyclerViewFragment fragment) {
        Log.d("0311", "onFragmentDetached: ");
        mMyRecyclerViewFragments.remove(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("0224", "onResume");
        if (tTimer == null) {
            tTimer = new Timer();
            tTimer.schedule(tTask, GET_DATA_INTERVAL, GET_DATA_INTERVAL);
        }

        builder = new MaterialDialog.Builder(MainActivity.this)
                .iconRes(R.drawable.ic_launcher)
                .limitIconToDefaultSize() // limits the displayed icon size to 48dp
                .title("Hint")
                .titleColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .contentColor(Color.BLACK)
                .content("Do you really want to leave ?")
                .positiveText("Agree")
                .negativeText("Disagree")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        MainActivity.this.finish();
                    }
                });

        dialog = builder.build();

        // Check to ensure a Google Account is active for the app. Placing the check here ensures
        // it is run again in the case where a Google Account wasn't present on the device and a
        // picker had to be started.
        /*if (!AccountUtils.enforceActiveGoogleAccount(this, SELECT_GOOGLE_ACCOUNT_RESULT)) {
            LOGD(TAG, "EnforceActiveGoogleAccount returned false");
            return;
        }

        // Watch for sync state changes
        mSyncStatusObserver.onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

        startLoginProcess();*/
    }

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_EXPLORE;
    }

    private boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_SETTINGS;
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * {@code AndroidManifest.xml} to find out the parent activity names for each activity.
     * @param intent
     */
    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        Log.d("0403", "canSwipeRefreshChildScrollUp: " + mViewPagerScrollState);

        // Prevent the swipe refresh by returning true here
        if (mViewPagerScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
            return true;
        }

        /*for( Map.Entry<Integer,Fragment> entry : mFragmentCache.entrySet()){
            Integer key = entry.getKey();
            Fragment value = entry.getValue();
            Log.d("0310", "key: " + key);
        }*/

        for (RecyclerViewFragment fragment : mMyRecyclerViewFragments) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (!fragment.getUserVisibleHint()) {
                    continue;
                }
            }

            return ViewCompat.canScrollVertically(fragment.setupRecyclerView(), -1);
        }

        return false;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mQueue != null)
            mQueue.cancelAll(REQUEST_TAG);
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        if (tTimer!=null)
            tTimer.cancel();
    }
}
