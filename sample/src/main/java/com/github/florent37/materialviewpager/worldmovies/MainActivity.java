package com.github.florent37.materialviewpager.worldmovies;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
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
import com.github.florent37.materialviewpager.worldmovies.adapter.ImageCursorAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.TrendsCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.fragment.DefaultFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.RecyclerViewFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.TrendsFragment;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

public class MainActivity extends BaseActivity implements RecyclerViewFragment.Listener, Response.ErrorListener {

    private MaterialViewPager mViewPager;
    public static final String REQUEST_TAG = "MainVolleyActivity";
    private SwipeRefreshLayout mSwipeRefreshLayout;// SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private boolean mActionBarShown = true;
    CustomJSONArrayRequest jsonRequest;
    private String HOST_NAME = Config.HOST_NAME;
    private Toolbar toolbar;
    private RequestQueue mQueue;
    View headerLogo;
    ImageView headerLogoContent;
    private long mExitTime = 0;
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
    final int tabCount = 7;
    public static final String FILM_NAME = "filmName";
    String[] from = new String [] {FILM_NAME};
    int[] to = new int[] { R.id.text1};
    private ImageCursorAdapter mAdapter;
    private static JSONObject[] MOVIES = {};
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
            switch (position) {
                case 0:
                    return TrendsFragment.newInstance(position);
                case 1:
                    return TrendsFragment.newInstance(position);
                case 2:
                    return TrendsFragment.newInstance(position);
                case 3:
                    return TrendsFragment.newInstance(position);
                case 4:
                    return TrendsFragment.newInstance(position);
                case 5:
                    return TrendsFragment.newInstance(position);
                case 6:
                    return TrendsFragment.newInstance(position);
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
            switch (position % tabCount) {
                case 0:
                    return getResources().getString(R.string.Japan);
                case 1:
                    return getResources().getString(R.string.USA);
                case 2:
                    return getResources().getString(R.string.Taiwan);
                case 3:
                    return getResources().getString(R.string.Korea);
                case 4:
                    return getResources().getString(R.string.France);
                case 5:
                    return getResources().getString(R.string.China);
                case 6:
                    return getResources().getString(R.string.Germany);
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
                        newDrawable = getResources().getDrawable(R.drawable.japan_circle);
                        break;
                    case 1:
                        imageUrl = "http://ia.media-imdb.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX640_SY720_.jpg";
                        color = getResources().getColor(R.color.material_orange_900);
                        newDrawable = getResources().getDrawable(R.drawable.united_states);
                        break;
                    case 2:
                        imageUrl = "http://soocurious.com/fr/wp-content/uploads/2014/03/8-facettes-de-notre-cerveau-qui-ont-evolue-avec-la-technologie8.jpg";
                        color = getResources().getColor(R.color.com_facebook_button_background_color);
                        newDrawable = getResources().getDrawable(R.drawable.taiwan_circle);
                        break;
                    case 3:
                        imageUrl = "http://graduate.carleton.ca/wp-content/uploads/prog-banner-masters-international-affairs-juris-doctor.jpg";
                        color = getResources().getColor(R.color.material_grey_500);
                        newDrawable = getResources().getDrawable(R.drawable.korea_circle);
                        break;
                    case 4:
                        imageUrl = "http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg";
                        color = getResources().getColor(R.color.material_lime_500);
                        newDrawable = getResources().getDrawable(R.drawable.france_circle);
                        break;
                    case 5:
                        imageUrl = "http://graduate.carleton.ca/wp-content/uploads/prog-banner-masters-international-affairs-juris-doctor.jpg";
                        color = getResources().getColor(R.color.material_red_A400);
                        newDrawable = getResources().getDrawable(R.drawable.china_circle);
                        break;
                    case 6:
                        imageUrl = "http://graduate.carleton.ca/wp-content/uploads/prog-banner-masters-international-affairs-juris-doctor.jpg";
                        color = getResources().getColor(R.color.material_brown_700);
                        newDrawable = getResources().getDrawable(R.drawable.germany_circle);
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
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
                if (fragment == null)
                    return;
                int channel = fragment.getArguments().getInt("index", 0);
                int SlideIndex = 0;

                switch (channel) {
                    default:
                        TrendsCardRecycleViewAdapter trendsAdapter = (TrendsCardRecycleViewAdapter)fragment.setupRecyclerAdapter();

                        if (trendsAdapter == null)
                            return;

                        List<TrendsObject> mContentItems = trendsAdapter.getItem();
                        LOGD("0419", String.valueOf(mContentItems.size()));

                        if (mContentItems.size() == 0)
                            return;

                        SlideIndex = random.nextInt(mContentItems.size());
                        mViewPager.setImageUrl(mContentItems.get(SlideIndex).getPosterUrl(), 750);
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

//        loadHints(); //chaching for search hint
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();
        mAdapter = new ImageCursorAdapter(this, R.layout.search_row, null, from, to, "main");
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
        final String[] from = new String [] {FILM_NAME};
        final int[] to = new int[] { R.id.text1};
        final CustomJSONObjectRequest jsonRequest;

        mAdapter = new ImageCursorAdapter(this,
                R.layout.search_row,
                null,
                from,
                to,
                "main");

        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();

        jsonRequest = new CustomJSONObjectRequest(Request.Method.GET, Config.HOST_NAME + "imdb_title", new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray contents = ((JSONObject) response).getJSONArray("contents");
                    MOVIES = getJsonObjectArray(contents);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Remote Server connect fail from MainActivity!", Toast.LENGTH_SHORT).show();
            }
        });

        jsonRequest.setTag(REQUEST_TAG);
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
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出WorldMoviePro", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
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

//                ((GradientDrawable) headerLogo.getBackground()).setColor(newColor);

                headerLogoContent.setImageDrawable(newLogo);

                animatorSetAppear.start();
            }
        });

        animatorSetDisappear.start();
    }

    private void getOverflowMenu() {
        try {
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
                    default:
                        if (fragment.getInitiatedAdapter().getItemCount() < fragment.trendMovieCount)
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
        mQueryTextView.setThreshold(1);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);
        mQueryTextView.setHint("movie title or cast name");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //if you want to collapse the searchview
                requestDataRefresh(query);
                invalidateOptionsMenu();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
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
                Cursor cursor = (Cursor) searchView.getSuggestionsAdapter().getItem(position);
                final String feedName = cursor.getString(1);
                searchView.post(new Runnable(){
                    @Override
                    public void run() {
                        searchView.setQuery(feedName, true);
                    }
                });
                return true;
            }
        });

        searchView.setSuggestionsAdapter(mAdapter);

        return true;
    }

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, FILM_NAME, FILM_DESCRIPTION, FILM_POSTER});
        String url;
        try {
            url = Config.HOST_NAME + "search/2/" + URLEncoder.encode(query, "UTF-8"); //TODO muti-channel support
        }  catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }
        jsonRequest = new CustomJSONArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONArray contents = ((JSONArray) response);
                MOVIES = getJsonObjectArray(contents);
                String posterUrl;
                try {
                    for (int i = 0; i < MOVIES.length; i++) {
                        JSONObject obj = MOVIES[i].getJSONObject("_source");
                        posterUrl = obj.has("posterUrl") ? obj.getString("posterUrl") : "http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg";
                        cursor.addRow(new Object[]{i, obj.getString("title"), obj.getString("description"), posterUrl});
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mAdapter.changeCursor(cursor);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            if (isNavDrawerOpen()) {
                closeNavDrawer();
            }
//            dialog.show();
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
        LOGD("0311", "onFragmentAttached: ");
        mMyRecyclerViewFragments.add(fragment);
    }

    @Override
    public void onFragmentDetached(RecyclerViewFragment fragment) {
        LOGD("0311", "onFragmentDetached: ");
        mMyRecyclerViewFragments.remove(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                        MainActivity.this.finishAffinity();
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
