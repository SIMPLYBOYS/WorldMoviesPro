package com.github.florent37.materialviewpager.worldmovies;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.RequestQueue;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.crashlytics.android.Crashlytics;
import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.worldmovies.adapter.TagAdapter;
import com.github.florent37.materialviewpager.worldmovies.adapter.TrendsCardRecycleViewAdapter;
import com.github.florent37.materialviewpager.worldmovies.fragment.DefaultFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.HomeFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.RecyclerViewFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.TrendsFragment;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.TagFilterHolder;
import com.github.florent37.materialviewpager.worldmovies.model.TagMetadata;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.provider.ScheduleContract;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.SearchActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionView;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.MultiSwipeRefreshLayout;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;

public class MainActivity extends BaseActivity implements RecyclerViewFragment.Listener,
        BottomNavigationBar.OnTabSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private MaterialViewPager mViewPager;
    private final String REQUEST_TAG = "MainVolleyActivity";
    private boolean mActionBarShown = true;
    private boolean Initail = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;// SwipeRefreshLayout allows the user to swipe the screen down to trigger a manual refresh
    private Toolbar toolbar;
    private RequestQueue mQueue;
    private View headerLogo;
    private ImageView headerLogoContent;
    private BottomNavigationBar bottomNavigationBar;
    private BadgeItem numberBadgeItem;
    private TagMetadata mTagMetadata;
    private TagFilterHolder mTagFilterHolder;
    private Set<RecyclerViewFragment> mMyRecyclerViewFragments = new HashSet<RecyclerViewFragment>();
    private Handler mHandler;
    private TimerTask tTask;
    private Timer tTimer;
    private int mViewPagerScrollState = ViewPager.SCROLL_STATE_IDLE;
    private int lastSelectedPosition = 0;
    private int mProgressBarTopWhenActionBarShown;
    private int SlideIndex;
    private long mExitTime = 0;
    private final int TAG_METADATA_TOKEN = 0x8;
    private final int TAG_METADATA_GENRE = 0x7;
    private final int tabCount = 8;
    private final String TAG = makeLogTag(MainActivity.class);
    private static final long GET_DATA_INTERVAL = 10000;
    private static final int GROUP_TOPIC_TYPE_OR_THEME = 0;
    private static final int GROUP_LIVE_STREAM = 1;
    private static final int GROUP_COUNTRY = 2;
    private static final int GROUP_LIVE_STREAM_2 = 3;
    private String searchChannel = "12";
    private String searchYear = "All";
    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;
    private FragmentStatePagerAdapter pagerAdapter;
    private SharedPreferences sp;
    private DrawerLayout mDrawerLayout;
    private CollectionView mDrawerCollectionView;
    private TagAdapter tagAdapter;

    // The OnClickListener for the Switch widgets on the navigation filter.
    private final View.OnClickListener mDrawerItemCheckBoxClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox)v).isChecked();
                    TagMetadata.Tag theTag = (TagMetadata.Tag)v.getTag();
                    LOGD(TAG, "Checkbox with tag: " + theTag.getName() + " isChecked => " + isChecked);
                    LOGD("1105", theTag.getCategory()+"\n"+theTag.getId());
                    if (isChecked) {
                        if (theTag.getCategory().equals("COUNTRY")) {
                            mTagFilterHolder.clear(); //support one country for searching
                        }

                        // Here we only add all 'types' if the user has not explicitly selected
                        // one of the category_type tags.
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                        mTagFilterHolder.add("All", "THEME");
                        List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

                        for (TagMetadata.Tag tag : tags) {
                            if (mTagFilterHolder.contains(tag.getId())) {
                                LOGD("1105", String.valueOf(tag.getId()));
                                searchChannel = String.valueOf(tag.getOrderInCategory());
                                CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                            }
                        }

                    } else {
                        searchChannel = "14";
                        mTagFilterHolder.remove(theTag.getId(), theTag.getCategory());
                        CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                    }

                    if (theTag.getCategory().equals("COUNTRY"))
                        mDrawerLayout.closeDrawer(GravityCompat.END);

                    //------------------//
                    tagAdapter = new TagAdapter(mTagMetadata, mDrawerItemCheckBoxClickListener, mTagFilterHolder);
                    mDrawerCollectionView.setCollectionAdapter(tagAdapter);
                    mDrawerCollectionView.updateInventory(tagAdapter.getInventory());
                    //------------------//
                }
            };

    public class MainPagerAdapter extends FragmentStatePagerAdapter {
        public MainPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return HomeFragment.newInstance(position);
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
                case 7:
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
                    return getResources().getString(R.string.Home);
                case 1:
                    return getResources().getString(R.string.Japan);
                case 2:
                    return getResources().getString(R.string.USA);
                case 3:
                    return getResources().getString(R.string.Taiwan);
                case 4:
                    return getResources().getString(R.string.Korea);
                case 5:
                    return getResources().getString(R.string.France);
                case 6:
                    return getResources().getString(R.string.China);
                case 7:
                    return getResources().getString(R.string.Germany);
                default:
                    return "Page " + position;
            }
        }

        int oldItemPosition = -1;

        @Override
        public void setPrimaryItem(ViewGroup container, final int position, Object object) {
            super.setPrimaryItem(container, position, object);
            LOGD("0303", "setPrimaryItem");

            if (oldItemPosition != position) {
                oldItemPosition = position;
                String imageUrl = null;
                int color = Color.BLACK;
                Drawable newDrawable = null;

                switch (position) {
                    case 0: //TODO
                        imageUrl = "http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg";
                        color = getResources().getColor(R.color.purple);
                        newDrawable = getResources().getDrawable(R.drawable.japan_circle);
                        break;
                    case 1:
                        imageUrl = "http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg";
                        color = getResources().getColor(R.color.purple);
                        newDrawable = getResources().getDrawable(R.drawable.japan_circle);
                        break;
                    case 2:
                        imageUrl = "http://ia.media-imdb.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX640_SY720_.jpg";
                        color = getResources().getColor(R.color.material_orange_900);
                        newDrawable = getResources().getDrawable(R.drawable.united_states);
                        break;
                    case 3:
                        imageUrl = "http://soocurious.com/fr/wp-content/uploads/2014/03/8-facettes-de-notre-cerveau-qui-ont-evolue-avec-la-technologie8.jpg";
                        color = getResources().getColor(R.color.com_facebook_button_background_color);
                        newDrawable = getResources().getDrawable(R.drawable.taiwan_circle);
                        break;
                    case 4:
                        imageUrl = "http://graduate.carleton.ca/wp-content/uploads/prog-banner-masters-international-affairs-juris-doctor.jpg";
                        color = getResources().getColor(R.color.material_grey_500);
                        newDrawable = getResources().getDrawable(R.drawable.korea_circle);
                        break;
                    case 5:
                        imageUrl = "http://i2.imgtong.com/1511/2df99d7cc478744f94ee7f0711e6afc4_ZXnCs61DyfBxnUmjxud.jpg";
                        color = getResources().getColor(R.color.material_lime_500);
                        newDrawable = getResources().getDrawable(R.drawable.france_circle);
                        break;
                    case 6:
                        imageUrl = "http://graduate.carleton.ca/wp-content/uploads/prog-banner-masters-international-affairs-juris-doctor.jpg";
                        color = getResources().getColor(R.color.material_red_A400);
                        newDrawable = getResources().getDrawable(R.drawable.china_circle);
                        break;
                    case 7:
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
    protected void
    onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isFinishing())
            return;

        getOverflowMenu();

        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());

        mViewPager = (MaterialViewPager) findViewById(R.id.materialViewPager);
        headerLogo = findViewById(R.id.headerLogo);
        headerLogoContent = (ImageView) findViewById(R.id.headerLogoContent);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        toolbar = mViewPager.getToolbar();

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(null);
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
        mViewPager.getViewPager().setCurrentItem(readCurrentPagePref(), true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_flipped, GravityCompat.END);
        mDrawerCollectionView = (CollectionView) findViewById(R.id.drawer_collection_view);

        if (headerLogo != null)
            headerLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.notifyHeaderChanged();
                    Toast.makeText(getApplicationContext(), "Yes, the title is clickable", Toast.LENGTH_SHORT).show();
                    showVisibleFragment().requestDataRefresh(true, null, null);
                }
            });

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
                SlideIndex = 0;

                switch (channel) {
                    case 0:
                        if (fragment.getInitiatedAdapter().getItemCount() < 1) {
                            LOGD("1216", String.valueOf(fragment.getInitiatedAdapter().getItemCount()));
                            fragment.requestDataRefresh(true, null, null);
                        } else
                            mSwipeRefreshLayout.setRefreshing(false);
                        break;
                    default:
                        TrendsCardRecycleViewAdapter trendsAdapter = (TrendsCardRecycleViewAdapter)fragment.setupRecyclerAdapter();

                        if (trendsAdapter == null)
                            return;

                        List<TrendsObject> mContentItems = trendsAdapter.getItem();

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
        // Start loading the tag metadata. This will in turn call the fragment with the
        // correct arguments.
        getLoaderManager().initLoader(TAG_METADATA_TOKEN, null, this);
//        getLoaderManager().initLoader(TAG_METADATA_GENRE, null, this);
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();
        refresh(bottomNavigationBar, lastSelectedPosition, numberBadgeItem);
        bottomNavigationBar.setTabSelectedListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle("");
        overridePendingTransition(0, 0);
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;
        if (numberBadgeItem != null) {
            numberBadgeItem.setText(Integer.toString(position)+1);
        }
        goToNavItem(position);
    }

    @Override
    public void onTabReselected(int position) {
        goToNavItem(position);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TAG_METADATA_TOKEN) {
            LOGD("1018", "createLoader");
            return TagMetadata.createCursorLoader(this);
        } else if (id == TAG_METADATA_GENRE) {
            LOGD("1103", "createGenreLoader");
            /*return new CursorLoader(this,
                    ScheduleContract.SearchTopicsSessions.CONTENT_URI,
                    SearchTopicsSessionsQuery.PROJECTION,
                    null, new String[] {"query"}, null);*/
            return TagMetadata.createCursorLoader(this);
        }
        return null;
    }

    @Override
    public void
    onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case TAG_METADATA_TOKEN:
                searchChannel = CredentialsHandler.getCountry(this);
                LOGD("1025", "load finish\n"+searchChannel+" # channel");
                mTagMetadata = new TagMetadata(cursor);
                onTagMetadataLoaded();
                break;
            case TAG_METADATA_GENRE:
                break;
            default:
                cursor.close();
                break;
        }
    }

    private interface SearchTopicsSessionsQuery {
        int TOKEN = 0x4;
        String[] PROJECTION = ScheduleContract.SearchTopicsSessions.DEFAULT_PROJECTION;

        int _ID = 0;
        int TAG_OR_SESSION_ID = 1;
        int SEARCH_SNIPPET = 2;
        int IS_TOPIC_TAG = 3;
    }

    public void onTagMetadataLoaded() {

        if (mTagFilterHolder == null) {
            // Use the Intent Extras to set up the TagFilterHolder
            mTagFilterHolder = new TagFilterHolder();
            String tag = getIntent().getStringExtra(EXTRA_FILTER_TAG); //TODO get tag from preference
            TagMetadata.Tag userTag = mTagMetadata.getTag(tag);
            String userTagCategory = userTag == null ? null : userTag.getCategory();

            if (tag != null && userTagCategory != null) {
                mTagFilterHolder.add(tag, userTagCategory);
            }

            List<TagMetadata.Tag> countryTags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

            if (countryTags != null && !TextUtils.equals(userTagCategory, Config.Tags.CATEGORY_COUNTRY)) {
                for (TagMetadata.Tag theTag : countryTags) {
                    if (String.valueOf(theTag.getOrderInCategory()).equals(searchChannel))
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                }
            }

            List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_THEME);
            // Here we only add all 'types' if the user has not explicitly selected
            // one of the category_type tags.
            if (tags != null && !TextUtils.equals(userTagCategory, Config.Tags.CATEGORY_THEME)) {
                for (TagMetadata.Tag theTag : tags) {
                    if (theTag.getId().equals("All")) {
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                    }
                }
            }
        }

        tagAdapter = new TagAdapter(mTagMetadata, mDrawerItemCheckBoxClickListener, mTagFilterHolder);
        mDrawerCollectionView.setCollectionAdapter(tagAdapter);
        mDrawerCollectionView.updateInventory(tagAdapter.getInventory());
    }

    @Override
    public void onTabUnselected(int position) {
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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
            if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawer(GravityCompat.END);
            } else if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出WorldMoviesPro", Toast.LENGTH_SHORT).show();
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

    private int readCurrentPagePref() {
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
                int channel = fragment.getArguments().getInt("index", 0);
                switch (channel) {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        SharedPreferences settings = getSharedPreferences("settings", 0);
        MenuItem miniCard = menu.findItem(R.id.menu_miniCard);
        MenuItem ascending = menu.findItem(R.id.menu_ascending);
        MenuItem filter = menu.findItem(R.id.action_filter);
        Drawable drawable = filter.getIcon();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        miniCard.setChecked(settings.getBoolean("miniCard", true));
        ascending.setChecked(settings.getBoolean("ascending", false));
        return true;
    }

    private EditText getEditText(final Context context, final View v) {
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    View editText = getEditText(context, child);

                    if (editText instanceof EditText) {
                        return (EditText) editText;
                    }
                }
            } else if (v instanceof EditText) {
                Log.d(TAG, "found edit text");
                return (EditText) v;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private void searchFor(String query) {
        // ANALYTICS EVENT: Start a search on the Search activity
        // Contains: Nothing (Event params are constant:  Search query not included)
        Bundle args = new Bundle(1);
        if (query == null) {
            query = "";
        }
        args.putString("query", query);
        if (!Initail) {
            LOGD("1103", "initLoader");
            getLoaderManager().initLoader(TAG_METADATA_GENRE, args, this);
            Initail = true;
        } else {
            LOGD("1103", "restartLoader");
            getLoaderManager().restartLoader(TAG_METADATA_GENRE, args, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.entrance_menu, menu);
        return true;
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
            case R.id.action_search:
                View searchMenuView = toolbar.findViewById(R.id.action_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle();
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("lastSelectedPosition", lastSelectedPosition);
                intent.putExtra("lauchBy", "main");
                ActivityCompat.startActivity(MainActivity.this, intent, null);
                return true;
            case R.id.action_filter:
                mDrawerLayout.openDrawer(GravityCompat.END);
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
    protected void onPause() {
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
        LOGD(TAG, "canSwipeRefreshChildScrollUp: " + mViewPagerScrollState);

        // Prevent the swipe refresh by returning true here
        if (mViewPagerScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
            return true;
        }

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
