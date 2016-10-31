package com.github.florent37.materialviewpager.worldmovies.trends;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.Transition;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.MainActivity;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.ImageCursorAdapter;
import com.github.florent37.materialviewpager.worldmovies.favorite.FavoriteActivity;
import com.github.florent37.materialviewpager.worldmovies.fragment.TrendsCastTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.TrendsInfoTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.TrendsMusicTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.TrendsReviewTabFragment;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONArrayRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.imdb.MovieDetailActivity;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.model.TagFilterHolder;
import com.github.florent37.materialviewpager.worldmovies.model.TagMetadata;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionView;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionViewCallbacks;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
import com.github.florent37.materialviewpager.worldmovies.util.BuildModelUtils;
import com.github.florent37.materialviewpager.worldmovies.util.UIUtils;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

import static com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity.EXTRA_FILTER_TAG;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;
import static com.github.florent37.materialviewpager.worldmovies.util.UIUtils.drawCountryFlag;

/**
 * Created by aaron on 2016/6/18.
 */
public class TrendsDetail extends AppCompatActivity implements KenBurnsView.TransitionListener, BottomNavigationBar.OnTabSelectedListener,
        Response.Listener, Response.ErrorListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static String TRENDS_OBJECT = "TRENDS_OBJECT";
    public final String FILM_NAME = "filmName";
    public final String FILM_DESCRIPTION = "filmDescription";
    public final String FILM_POSTER = "filmPoster";
    public final String REQUEST_TAG = "titleRequest";
    //------------------------------------//
    protected final int NAV_ITEM_TREND = 0;
    protected final int NAV_ITEM_UPCOMING = 1;
    protected final int NAV_ITEM_IMDB = 2;
    protected final int NAV_ITEM_NYTIMES = 3;
    protected final int NAV_ITEM_FAVORITE = 4;
    //------------------------------------//
    private static final int TAG_METADATA_TOKEN = 0x8;
    private static final int GROUP_TOPIC_TYPE_OR_THEME = 0;
    private static final int GROUP_LIVE_STREAM = 1;
    private static final int GROUP_COUNTRY = 2;
    private static final String TAG = makeLogTag(ImdbActivity.class);
    private DrawerLayout mDrawerLayout;
    private CollectionView mDrawerCollectionView;
    private TrendsFavoritePreference favor;
    private int mTransitionsCount = 0;
    private String HOST_NAME = Config.HOST_NAME;
    private static int TRANSITIONS_TO_SWITCH = 1;
    private List<TrendsObject.GalleryItem> list = null;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private KenBurnsView backgroundImageView, backgroundImageView2, backgroundImageView3, backgroundImageView4, backgroundImageView5;
    private List<KenBurnsView> kenBurnsViews = null;
    private FloatingActionButton fab;
    private TextView title;
    private ViewFlipper mViewSwitcher;
    private TrendsObject trendsObject = null;
    private SearchView searchView = null;
    private ShineButton bookmarkView = null;
    private MenuItem searchItem = null;
    private MenuItem bookmarkItem = null;
    private RequestQueue mQueue;
    private ImageCursorAdapter mAdapter;
    private static JSONObject[] MOVIES = {};
    private TagMetadata mTagMetadata;
    private TagFilterHolder mTagFilterHolder;
    private String searchChannel = "14";
    private int lastSelectedPosition = 0;
    private ShareActionProvider shareActionProvider;
    private BottomNavigationBar bottomNavigationBar;
    private BadgeItem numberBadgeItem;
    private LinearLayout bookmarkActionView;
    private CustomJSONArrayRequest jsonRequest;
    private Handler completeHandler;
    public final int MESSAGE_TEXT_CHANGE = 100;
    public final int AUTOCOMPLETE_DELAY = 750;
    public int mAutoCompleteDelay = AUTOCOMPLETE_DELAY;
    private String[] from = new String [] {FILM_NAME};
    private int[] to = new int[] { R.id.text1};

    // The OnClickListener for the Switch widgets on the navigation filter.
    private final View.OnClickListener mDrawerItemCheckBoxClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox)v).isChecked();
                    TagMetadata.Tag theTag = (TagMetadata.Tag)v.getTag();
                    LOGD(TAG, "Checkbox with tag: " + theTag.getName() + " isChecked => " + isChecked);
                    if (isChecked) {
                        if (theTag.getCategory().equals("COUNTRY")) {
                            mTagFilterHolder.clear(); //support one country for searching
                        }
                        // Here we only add all 'types' if the user has not explicitly selected
                        // one of the category_type tags.
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());

                        List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

                        for (TagMetadata.Tag tag : tags) {
                            if (mTagFilterHolder.contains(tag.getId())) {
                                LOGD("1019", String.valueOf(tag.getOrderInCategory()));
                                searchChannel = String.valueOf(tag.getOrderInCategory());
                                CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                            }
                        }
                    } else {
                        searchChannel = "14";
                        mTagFilterHolder.remove(theTag.getId(), theTag.getCategory());
                        CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Gson gson = new Gson();
        setContentView(R.layout.movie_detail);
        setupToolbar();
        setupViewPager();
        setupCollapsingToolbar();
        title = (TextView) findViewById(R.id.title);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        fab = (FloatingActionButton) findViewById(R.id.floating_button);
        mAdapter = new ImageCursorAdapter(this, R.layout.search_row, null, from, to, "detail");

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                createShareAction();
            }
        });

        //---------- Ken Burn Animation-----------//
        backgroundImageView = (KenBurnsView) findViewById(R.id.backgroundImageView);
        backgroundImageView2 = (KenBurnsView) findViewById(R.id.backgroundImageView2);
        backgroundImageView3 = (KenBurnsView) findViewById(R.id.backgroundImageView3);
        backgroundImageView4 = (KenBurnsView) findViewById(R.id.backgroundImageView4);
        backgroundImageView5 = (KenBurnsView) findViewById(R.id.backgroundImageView5);

        backgroundImageView.setTransitionListener(this);
        backgroundImageView2.setTransitionListener(this);
        backgroundImageView3.setTransitionListener(this);
        backgroundImageView4.setTransitionListener(this);
        backgroundImageView5.setTransitionListener(this);
        //---------- Ken Burn Animation-----------//

        trendsObject = (TrendsObject) getIntent().getSerializableExtra(TRENDS_OBJECT);
        mViewSwitcher = (ViewFlipper) findViewById(R.id.viewSwitcher);
//        collapsingToolbar.setTitle(trendsObject.getTitle());
        title.setText(trendsObject.getTitle());

        //------- deserialize Gallery JSON object -------//
        JsonArray galleryInfo = new JsonParser().parse(trendsObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<TrendsObject.GalleryItem>();
        for (int i = 0; i < galleryInfo.size(); i++) {
            JsonElement str = galleryInfo.get(i);
            TrendsObject.GalleryItem obj = gson.fromJson(str, TrendsObject.GalleryItem.class);
            list.add(obj);
        }
        //------- deserialize Gallery JSON object -------//
        // Start loading the tag metadata. This will in turn call the fragment with the correct arguments.
        getLoaderManager().initLoader(TAG_METADATA_TOKEN, null, this);
        refresh(); //refresh bottomView
        bottomNavigationBar.setTabSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_flipped, GravityCompat.END);
        mDrawerCollectionView = (CollectionView) findViewById(R.id.drawer_collection_view);
        favor = new TrendsFavoritePreference();
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();

        completeHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                giveSuggestions((String) msg.obj);
            }
        };

        overridePendingTransition(0, 0);
    }

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;
        if (numberBadgeItem != null) {
            numberBadgeItem.setText(Integer.toString(position));
        }
        goToNavItem(position);
    }

    @Override
    public void onTabUnselected(int position) {
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
            return TagMetadata.createCursorLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case TAG_METADATA_TOKEN:
                mTagMetadata = new TagMetadata(cursor);
                onTagMetadataLoaded();
                break;
            default:
                cursor.close();
        }
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

            List<TagMetadata.Tag> tags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_TYPE);
            // Here we only add all 'types' if the user has not explicitly selected
            // one of the category_type tags.
            if (tags != null && !TextUtils.equals(userTagCategory, Config.Tags.CATEGORY_TYPE)) {
                for (TagMetadata.Tag theTag : tags) {
                    mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                }
            }

            List<TagMetadata.Tag> countryTags = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

            if (countryTags != null && !TextUtils.equals(userTagCategory, Config.Tags.CATEGORY_COUNTRY)) {
                for (TagMetadata.Tag theTag : countryTags) {
                    if (String.valueOf(theTag.getOrderInCategory()).equals(searchChannel))
                        mTagFilterHolder.add(theTag.getId(), theTag.getCategory());
                }
            }
        }

        TagAdapter tagAdapter = new TagAdapter();
        mDrawerCollectionView.setCollectionAdapter(tagAdapter);
        mDrawerCollectionView.updateInventory(tagAdapter.getInventory());
    }

    private class TagAdapter implements CollectionViewCallbacks {

        public CollectionView.Inventory getInventory() {
            Log.d("1018", "getInventory");
            List<TagMetadata.Tag> countries = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_TOPIC);
            CollectionView.Inventory inventory = new CollectionView.Inventory();
            CollectionView.InventoryGroup themeGroup = new CollectionView.InventoryGroup(GROUP_TOPIC_TYPE_OR_THEME)
                    .setDisplayCols(1)
                    .setDataIndexStart(0)
                    .setShowHeader(false);

            if (countries != null && countries.size() > 0) {
                for (TagMetadata.Tag country : countries) {
                    themeGroup.addItemWithTag(country);
                }
                inventory.addGroup(themeGroup);
            }

            // We need to add the Live streamed section after the Type category
            CollectionView.InventoryGroup liveStreamGroup = new CollectionView.InventoryGroup(GROUP_LIVE_STREAM)
                    .setDataIndexStart(0)
                    .setShowHeader(true)
                    .addItemWithTag("Livestreamed");

            inventory.addGroup(liveStreamGroup);

            CollectionView.InventoryGroup topicsGroup = new CollectionView.InventoryGroup(GROUP_COUNTRY)
                    .setDataIndexStart(0)
                    .setShowHeader(true);

            List<TagMetadata.Tag> topics = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

            if (topics != null && topics.size() > 0) {
                for (TagMetadata.Tag topic : topics) {
                    Log.d("1018", String.valueOf(topic));
                    topicsGroup.addItemWithTag(topic);
                }
                inventory.addGroup(topicsGroup);
            }

            return inventory;
        }

        @Override
        public View newCollectionHeaderView(Context context, int groupId, ViewGroup parent) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.explore_sessions_list_item_alt_header, parent, false);
            // We do not want the divider/header to be read out by TalkBack, so
            // inform the view that this is not important for accessibility.
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        @Override
        public void bindCollectionHeaderView(Context context, View view, int groupId,
                                             String headerLabel, Object headerTag) {
        }

        @Override
        public View newCollectionItemView(Context context, int groupId, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(groupId == GROUP_LIVE_STREAM ?
                    R.layout.explore_sessions_list_item_livestream_alt_drawer :
                    R.layout.explore_sessions_list_item_alt_drawer, parent, false);
        }

        @Override
        public void bindCollectionItemView(Context context, View view, int groupId,
                                           int indexInGroup, int dataIndex, Object tag) {
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.filter_checkbox);
            if (groupId == GROUP_LIVE_STREAM) {
                //Do nothing
            } else {
                TagMetadata.Tag theTag = (TagMetadata.Tag) tag;
                if (theTag != null && groupId == GROUP_TOPIC_TYPE_OR_THEME) {
                    ((TextView) view.findViewById(R.id.text_view)).setText(theTag.getName());
                    // set the original checked state by looking up our tags.
                    checkBox.setChecked(mTagFilterHolder.contains(theTag.getId()));
                    checkBox.setTag(theTag);
                    checkBox.setOnClickListener(mDrawerItemCheckBoxClickListener);
                    //TODO poster by Genre api
                } else if (theTag != null && groupId == GROUP_COUNTRY) {
                    ((TextView) view.findViewById(R.id.text_view)).setText(theTag.getName());
                    drawCountryFlag(view, theTag.getOrderInCategory());
                    // set the original checked state by looking up our tags.
                    checkBox.setChecked(mTagFilterHolder.contains(theTag.getId()));
                    checkBox.setTag(theTag);
                    checkBox.setOnClickListener(mDrawerItemCheckBoxClickListener);
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.performClick();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
                upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
                actionBar.setHomeAsUpIndicator(upArrow);
            }
        }
    }

    private void refresh() {
        bottomNavigationBar.clearAll();
        numberBadgeItem = new BadgeItem()
                .setBorderWidth(4)
                .setBackgroundColorResource(R.color.blue)
                .setText("" + lastSelectedPosition);

//        bottomNavigationBar.setFab(fab);

        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_trending_up, R.string.navdrawer_item_explore).setActiveColorResource(R.color.material_orange_900).setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_movie, R.string.navdrawer_item_up_coming).setActiveColorResource(R.color.material_teal_A200))
                .addItem(new BottomNavigationItem(R.drawable.ic_theaters, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "NyTimes").setActiveColorResource(R.color.material_brown_300))
                .addItem(new BottomNavigationItem(R.drawable.ic_person, "Profile").setActiveColorResource(R.color.material_red_900))
//                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_light_blue_A100))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setBarBackgroundColor(R.color.material_grey_900)
                .initialise();
    }

    private void setupViewPager() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.detail_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (trendsObject == null) {
            trendsObject = (TrendsObject) getIntent().getSerializableExtra(TRENDS_OBJECT);
        }
        adapter.addFrag(TrendsInfoTabFragment.newInstance(trendsObject), "Info");
        adapter.addFrag(TrendsCastTabFragment.newInstance(trendsObject), "Cast");
        adapter.addFrag(TrendsReviewTabFragment.newInstance(trendsObject), "Review");
        adapter.addFrag(TrendsMusicTabFragment.newInstance(trendsObject), "Music");
        viewPager.setAdapter(adapter);

        /*viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("0621", "positionOffset ---> " + String.valueOf(positionOffset) + "positionOffsetPixels --> " + String.valueOf(positionOffsetPixels));
            }

            @Override
            public void onPageSelected(int position) {
                *//*if (position == 0) {
                    Log.d("0621", String.valueOf(position));
                    NestedScrollView nested_scrollview = (NestedScrollView) adapter.getItem(0).getView().findViewById(R.id.nested_scrollview);
                    nested_scrollview.smoothScrollTo(0, 0);
                }*//*
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("0622", "pagescrollstatechanged");
                supportInvalidateOptionsMenu();
            }
        });*/
    }

    @Override
    public void onResponse(Object response) {
        try {
            Log.d("0419", "title onResponse");
            JSONArray contents = ((JSONObject) response).getJSONArray("contents");
            MOVIES = BaseActivity.getJsonObjectArray(contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(this, "Remote Server not working!", Toast.LENGTH_LONG).show();
    }

    private void setupCollapsingToolbar() {
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(false);
        collapsingToolbar.setContentScrimColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setStatusBarScrimColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

    }

    private void setPalette(int num) {
        Bitmap bitmap = ((BitmapDrawable) kenBurnsViews.get(0).getDrawable()).getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int primaryDark = getResources().getColor(R.color.primary_dark_material_dark);
                int primary = getResources().getColor(R.color.primary_material_light);
                /*collapsingToolbar.setContentScrimColor(palette.getMutedColor(primary));
                collapsingToolbar.setStatusBarScrimColor(palette.getDarkVibrantColor(primaryDark));*/
                fab.setBackgroundTintList(ColorStateList.valueOf(palette.getDarkVibrantColor(primaryDark)));
                fab.setRippleColor(palette.getMutedColor(primary));
            }
        });
    }

    @Override
    public void onTransitionStart(Transition transition) {
    }

    @Override
    public void onTransitionEnd(Transition transition) {
        mTransitionsCount++;
        if (list.size() < 2)
            return;
        if (mTransitionsCount == TRANSITIONS_TO_SWITCH) {
            Random random = new Random();
            transitionImageUrl(backgroundImageView, list.get(random.nextInt(list.size())).getUrl(), 250);
            mViewSwitcher.showNext();
            setPalette(random.nextInt(kenBurnsViews.size()));
            mTransitionsCount = 0;
        }
    }

    @Override
    public void onResume () {
        super.onResume();
        Picasso.with(this).load(trendsObject.getPosterUrl()).centerCrop().fit().into(backgroundImageView);
        Random random = new Random();
        kenBurnsViews = new ArrayList<KenBurnsView>();
        kenBurnsViews.add(backgroundImageView);
        if (list.size() > 1) {
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView2);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView3);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView4);
            Picasso.with(this).load(list.get(random.nextInt(list.size())).getUrl()).centerCrop().fit().into(backgroundImageView5);
            kenBurnsViews.add(backgroundImageView2);
            kenBurnsViews.add(backgroundImageView3);
            kenBurnsViews.add(backgroundImageView4);
            kenBurnsViews.add(backgroundImageView5);
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
    protected  void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected  void onRestart() {
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.trends_menu, menu);

        for(int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
            }
        }

        MenuItem filter = menu.findItem(R.id.action_filter);
        bookmarkActionView = (LinearLayout) getLayoutInflater().inflate(R.layout.bookmark_image, null);
        bookmarkView = (ShineButton) bookmarkActionView.findViewById(R.id.bookmarkView);
        bookmarkView.init(this);
        bookmarkView.getLayoutParams().height=96;
        bookmarkView.getLayoutParams().width=96;
//        bookmarkView.setImageResource(R.drawable.ic_turned_in);
        bookmarkView.setColorFilter(getResources().getColor(R.color.app_white));
        bookmarkView.setScaleType(ImageView.ScaleType.FIT_XY);
        bookmarkItem = menu.findItem(R.id.action_bookmark);
        bookmarkItem.setActionView(bookmarkView);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        AutoCompleteTextView mQueryTextView = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        mQueryTextView.setThreshold(1);
        mQueryTextView.setTextColor(Color.WHITE);
        mQueryTextView.setHintTextColor(Color.WHITE);
        mQueryTextView.setHint("movie title or cast name");

        if (trendsObject.getBookmark()) {
            bookmarkView.setChecked(true);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
        } else {
            bookmarkView.setChecked(false);
            bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_not_white);
        }

        bookmarkView.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
//                Snackbar.make(view, "Bookmark "+checked+" !!!", Snackbar.LENGTH_LONG).show();
                Toast.makeText(TrendsDetail.this, "Bookmark "+checked+" !!!", Toast.LENGTH_SHORT).show();
                bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);

                if (checked && !trendsObject.getBookmark()) {
                    User user = UsersUtils.getCurrentUser(getApplicationContext());
                    trendsObject.setBookmark(true);
                    CustomJSONObjectRequest jsonRequest_q = null;
                    String url = HOST_NAME + "trends/"+user.id;
                    JSONObject jsonBody = new JSONObject();

                    try {
                        jsonBody.put("title", trendsObject.getTitle());
                        jsonBody.put("link", trendsObject.getDetailUrl());
                        jsonBody.put("picUrl", trendsObject.getPosterUrl());
                        jsonBody.put("channel", trendsObject.getChannel());
                        favor.addFavorite(getApplicationContext(), trendsObject.getTitle());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    jsonRequest_q = new CustomJSONObjectRequest(Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String result = response.getString("content");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(TrendsDetail.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mQueue.add(jsonRequest_q);
                } else if (!checked && trendsObject.getBookmark()) {
                    User user = UsersUtils.getCurrentUser(getApplicationContext());
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in);
                    trendsObject.setBookmark(false);
                    favor.removeFavorite(getApplicationContext(), trendsObject.getTitle());
                    CustomJSONObjectRequest jsonRequest_q = null;
                    String Query;

                    try {
                        Query = URLEncoder.encode(trendsObject.getTitle(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionError("UTF-8 is unknown");
                    }

                    String url = HOST_NAME + "trends/"+user.id+"/"+Query;

                    jsonRequest_q = new CustomJSONObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String result = response.getString("content");
                                LOGD("0813", result);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(TrendsDetail.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mQueue.add(jsonRequest_q);
                }
            }
        });

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
                if (!query.trim().isEmpty()) {
                    completeHandler.removeMessages(MESSAGE_TEXT_CHANGE);
                    completeHandler.sendMessageDelayed(completeHandler.obtainMessage(MESSAGE_TEXT_CHANGE, query), mAutoCompleteDelay);
                }
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

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = new ShareActionProvider(this);
        /*shareActionProvider =  (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);*/
        MenuItemCompat.setActionProvider(shareItem, shareActionProvider);
        shareActionProvider.setShareIntent(createShareIntent());
        return true;
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
                "detail");

        mQueue = CustomVolleyRequestQueue.getInstance(this)
                .getRequestQueue();


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
                Toast.makeText(TrendsDetail.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }

    public static JSONObject [] getJsonObjectArray(JSONArray jsonArray) {
        JSONObject[] jsonObjectsArray = null;
        int length = jsonArray.length();
        if (jsonArray!=null) {
            try {
                jsonObjectsArray = new JSONObject[length];
                for (int i = 0; i < length; i++) {
                    jsonObjectsArray[i] = jsonArray.getJSONObject(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObjectsArray;
    }

    private void giveSuggestions(String query) {
        final MatrixCursor cursor = new MatrixCursor(new String[]{ BaseColumns._ID, FILM_NAME, FILM_DESCRIPTION, FILM_POSTER});
        String url;

        try {
            url = Config.HOST_NAME + "search/"+ searchChannel+"/" + URLEncoder.encode(query, "UTF-8"); //TODO muti-channel support
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
//                Toast.makeText(TrendsDetail.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
            }
        });
        mQueue.add(jsonRequest);
    }



    public void requestDataRefresh(String Query) {
        final CustomJSONObjectRequest jsonRequest = null;
        mQueue = CustomVolleyRequestQueue.getInstance(TrendsDetail.this).getRequestQueue();
        CustomJSONObjectRequest jsonRequest_q = null;
        String url = null;
        String searchChannel = CredentialsHandler.getCountry(this);
        // String searchGenre = CredentialsHandler.getGenre(this); TODO

        if (Query != null) {
            // launch query from searchview
            try {
                Query = URLEncoder.encode(Query, "UTF-8");
                url= Config.HOST_NAME + "/world/"+searchChannel+"/all?title=" + Query + "&ascending=1"; //TODO search by country with genre
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }

            jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray contents = response.getJSONArray("contents");
                        ImdbObject item = BuildModelUtils.buildImdbModel(contents);
                        Intent intent = new Intent(TrendsDetail.this, MovieDetailActivity.class);
                        intent.putExtra(ImdbActivity.IMDB_OBJECT, item);
                        ActivityCompat.startActivity(TrendsDetail.this, intent, null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(TrendsDetail.this, "Remote Server connect fail from GenreActivity!", Toast.LENGTH_SHORT).show();
                }
            });
            mQueue.add(jsonRequest_q);
            return;
        }

        mQueue.add(jsonRequest); //trigger volley request
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                onShareAction();
                return true;
            case R.id.action_bookmark:
                Snackbar.make(item.getActionView(),"Bookmark !!!", Snackbar.LENGTH_LONG).show();
                return true;
            case R.id.action_filter:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void transitionImageUrl(final ImageView imageView, final String urlImage, final int fadeDuration) {
        final float alpha = ViewHelper.getAlpha(imageView);

        //fade to alpha=0
        final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 0).setDuration(fadeDuration);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                //change the image when alpha=0
                Picasso.with(imageView.getContext()).load(urlImage)
                        .centerCrop().fit().into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {

                        //then fade to alpha=1

                        final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 1.0f).setDuration(fadeDuration);
                        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                        fadeIn.start();
                    }
                    @Override
                    public void onError() {
                    }
                });
            }
        });
        fadeOut.start();
    }

    private void goToNavItem(int item) {
        switch (item) {
            case NAV_ITEM_TREND:
                createBackStack(new Intent(this, MainActivity.class));
                break;
            case NAV_ITEM_UPCOMING:
                createBackStack(new Intent(this, upComingActivity.class));
                break;
            case NAV_ITEM_IMDB:
                createBackStack(new Intent(this, ImdbActivity.class));
                break;
            case NAV_ITEM_NYTIMES:
                createBackStack(new Intent(this, nyTimesActivity.class));
                break;
            case NAV_ITEM_FAVORITE:
//                createBackStack(new Intent(this, GenreActivity.class));
                Intent intent = new Intent(this, FavoriteActivity.class);
                User user = UsersUtils.getCurrentUser(getApplicationContext());
                intent.putExtra("user", user);
                createBackStack(intent);
                break;
        }
    }

    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivityForVersion(intent);
            finish();
        }
    }

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            TrendsDetail.this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    private void onShareAction() {
        shareActionProvider.setShareIntent(createShareIntent());
        return;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trendsObject.getDetailUrl());
        return shareIntent;
    }

    private void createShareAction() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, trendsObject.getDetailUrl());
        // Launch sharing dialog for image
        startActivity(Intent.createChooser(shareIntent, "Share Review"));
    }

    private void previewX(LineChartView chart, PreviewLineChartView previewChart, boolean animate) {
        Viewport tempViewport = new Viewport(chart.getMaximumViewport());
        float dx = tempViewport.width() / 4;
        tempViewport.inset(dx, 0);
        if (animate) {
            previewChart.setCurrentViewportWithAnimation(tempViewport);
        } else {
            previewChart.setCurrentViewport(tempViewport);
        }
        previewChart.setZoomType(ZoomType.HORIZONTAL);
    }

    // Get the uri to a random image in the photo gallery
    private Uri getRandomImageUri() {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Images.Media._ID };
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(mediaUri, projection, null, null, null);
            cursor.moveToPosition((int) (Math.random() * cursor.getCount()));
            String id = cursor.getString(0);
            Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            return uri;
        }
        catch (Exception e) {
            return null;
        }
        finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
