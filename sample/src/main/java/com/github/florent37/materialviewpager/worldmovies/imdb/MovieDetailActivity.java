package com.github.florent37.materialviewpager.worldmovies.imdb;

import android.app.ActivityOptions;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.github.florent37.materialviewpager.worldmovies.favorite.FavoriteActivity;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbCastTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbChartTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbInfoTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbMusicTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.ImdbReviewTabFragment;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.model.TagFilterHolder;
import com.github.florent37.materialviewpager.worldmovies.model.TagMetadata;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesActivity;
import com.github.florent37.materialviewpager.worldmovies.trends.MoviesFavoritePreference;
import com.github.florent37.materialviewpager.worldmovies.ui.SearchActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionView;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionViewCallbacks;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
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
 * Created by aaron on 2016/7/28.
 */

public class MovieDetailActivity extends AppCompatActivity implements KenBurnsView.TransitionListener,
        BottomNavigationBar.OnTabSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static String IMDB_OBJECT = "IMDB_OBJECT";
    public final String FILM_NAME = "filmName";
    public final String FILM_DESCRIPTION = "filmDescription";
    public final String FILM_POSTER = "filmPoster";
    protected final int NAV_ITEM_TREND = 0;
    protected final int NAV_ITEM_UPCOMING = 1;
    protected final int NAV_ITEM_IMDB = 2;
    protected final int NAV_ITEM_NYTIMES = 3;
    protected final int NAV_ITEM_FAVORITE = 4;
    private int mTransitionsCount = 0;
    private MoviesFavoritePreference moviesFavor;
    private String HOST_NAME = Config.HOST_NAME;
    private String type;
    private int TRANSITIONS_TO_SWITCH = 1;
    private List<ImdbObject.GalleryItem> list = null;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private KenBurnsView backgroundImageView, backgroundImageView2, backgroundImageView3, backgroundImageView4, backgroundImageView5;
    private List<KenBurnsView> kenBurnsViews = null;
    private FloatingActionButton fab;
    private TextView title;
    private RequestQueue mQueue;
    private ViewFlipper mViewSwitcher;
    private ImdbObject imdbObject = null;
    private MenuItem bookmarkItem = null;
    private int lastSelectedPosition = 0;
    private ShareActionProvider shareActionProvider;
    private BottomNavigationBar bottomNavigationBar;
    private BadgeItem numberBadgeItem;
    private LinearLayout bookmarkActionView;
    private ShineButton bookmarkView = null;
    private DrawerLayout mDrawerLayout;
    private CollectionView mDrawerCollectionView;
    private String searchChannel = "14";
    private final int TAG_METADATA_TOKEN = 0x8;
    private TagMetadata mTagMetadata;
    private TagFilterHolder mTagFilterHolder;
    private static final int GROUP_TOPIC_TYPE_OR_THEME = 0;
    private static final int GROUP_LIVE_STREAM = 1;
    private static final int GROUP_COUNTRY = 2;
    private final String TAG = makeLogTag(MovieDetailActivity.class);
    private TagAdapter tagAdapter;
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
                        searchChannel = "12";
                        mTagFilterHolder.remove(theTag.getId(), theTag.getCategory());
                        CredentialsHandler.setCountry(getApplicationContext(), searchChannel);
                    }

                    mDrawerLayout.closeDrawer(GravityCompat.END);

                    //------------------//
                    tagAdapter = new TagAdapter();
                    mDrawerCollectionView.setCollectionAdapter(tagAdapter);
                    mDrawerCollectionView.updateInventory(tagAdapter.getInventory());
                    //------------------//
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
        moviesFavor = new MoviesFavoritePreference();
        mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        fab = (FloatingActionButton) findViewById(R.id.floating_button);

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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_flipped, GravityCompat.END);
        mDrawerCollectionView = (CollectionView) findViewById(R.id.drawer_collection_view);
        imdbObject = (ImdbObject) getIntent().getSerializableExtra(IMDB_OBJECT);
        mViewSwitcher = (ViewFlipper) findViewById(R.id.viewSwitcher);
//        collapsingToolbar.setTitle(trendsObject.getTitle());
        title.setText(imdbObject.getTitle());
        type = imdbObject.getType();
        getLoaderManager().initLoader(TAG_METADATA_TOKEN, null, this);

        if (type.compareTo("upcoming") == 0)
            lastSelectedPosition = 1;
        else if (type.compareTo("imdb") == 0)
            lastSelectedPosition = 2;

        //------- deserialize Gallery JSON object -------//
        JsonArray galleryInfo = new JsonParser().parse(imdbObject.getGalleryUrl()).getAsJsonArray();
        list = new ArrayList<ImdbObject.GalleryItem>();

        for (int i = 0; i < galleryInfo.size(); i++) {
            JsonElement str = galleryInfo.get(i);
            ImdbObject.GalleryItem obj = gson.fromJson(str, ImdbObject.GalleryItem.class);
            list.add(obj);
        }

        //------- deserialize Gallery JSON object -------//
        getLoaderManager().initLoader(TAG_METADATA_TOKEN, null, this);
        refresh();
        bottomNavigationBar.setTabSelectedListener(this);
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
                .setBackgroundColorResource(R.color.material_blue_700)
                .setText("" + lastSelectedPosition);

//        bottomNavigationBar.setFab(fab);
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_trending_up, R.string.navdrawer_item_explore).setActiveColorResource(R.color.material_orange_900).setBadgeItem(numberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_movie, R.string.navdrawer_item_up_coming).setActiveColorResource(R.color.material_teal_A200))
                .addItem(new BottomNavigationItem(R.drawable.imdb, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "NyTimes").setActiveColorResource(R.color.material_brown_300))
                .addItem(new BottomNavigationItem(R.drawable.ic_person, "Profile").setActiveColorResource(R.color.material_red_900))
//                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_light_blue_A100))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setBarBackgroundColor(R.color.material_grey_900)
                .initialise();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == TAG_METADATA_TOKEN) {
            LOGD("1018", "createLoader");
            return TagMetadata.createCursorLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case TAG_METADATA_TOKEN:
                searchChannel = CredentialsHandler.getCountry(this);
                LOGD("1025", "load finish\n"+searchChannel+" # channel");
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
            List<TagMetadata.Tag> year = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_TOPIC);
            CollectionView.Inventory inventory = new CollectionView.Inventory();
            /*CollectionView.InventoryGroup themeGroup = new CollectionView.InventoryGroup(GROUP_TOPIC_TYPE_OR_THEME)
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

            inventory.addGroup(liveStreamGroup);*/

            CollectionView.InventoryGroup countryGroup = new CollectionView.InventoryGroup(GROUP_COUNTRY)
                    .setDataIndexStart(0)
                    .setShowHeader(false);

            List<TagMetadata.Tag> countries = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

            if (countries != null && countries.size() > 0) {
                for (TagMetadata.Tag country : countries) {
                    Log.d("1018", String.valueOf(country));
                    countryGroup.addItemWithTag(country);
                }
                inventory.addGroup(countryGroup);
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
        public void bindCollectionHeaderView(Context context, View view, int groupId, String headerLabel, Object headerTag) {
        }

        @Override
        public View newCollectionItemView(Context context, int groupId, ViewGroup parent) {
            return LayoutInflater.from(context).inflate(groupId == GROUP_LIVE_STREAM ?
                    R.layout.explore_sessions_list_item_livestream1_alt_drawer :
                    R.layout.explore_sessions_list_item_alt_drawer, parent, false);
        }

        @Override
        public void bindCollectionItemView(Context context, View view, int groupId, int indexInGroup, int dataIndex, Object tag) {
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

    private void setupViewPager() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.detail_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        if (imdbObject == null)
            imdbObject = (ImdbObject) getIntent().getSerializableExtra(IMDB_OBJECT);

        adapter.addFrag(ImdbInfoTabFragment.newInstance(imdbObject), "Info");
        adapter.addFrag(ImdbCastTabFragment.newInstance(imdbObject), "Cast");
        adapter.addFrag(ImdbReviewTabFragment.newInstance(imdbObject), "Review");
        adapter.addFrag(ImdbMusicTabFragment.newInstance(imdbObject), "Music");

        if (imdbObject.getType().compareTo("imdb") == 0)
            adapter.addFrag(ImdbChartTabFragment.newInstance(imdbObject), "Chart");

        viewPager.setAdapter(adapter);
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
        if (kenBurnsViews.get(0).getDrawable() == null)
            return;

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
        Picasso.with(this).load(imdbObject.getPosterUrl()).centerCrop().fit().into(backgroundImageView);
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

        getMenuInflater().inflate(R.menu.trends_menu, menu);
        bookmarkActionView = (LinearLayout) getLayoutInflater().inflate(R.layout.bookmark_image, null);
        bookmarkView = (ShineButton) bookmarkActionView.findViewById(R.id.bookmarkView);
        bookmarkView.init(this);
        bookmarkView.getLayoutParams().height=96;
        bookmarkView.getLayoutParams().width=96;
        bookmarkView.setColorFilter(getResources().getColor(R.color.app_white));
        bookmarkView.setScaleType(ImageView.ScaleType.FIT_XY);
        bookmarkItem = menu.findItem(R.id.action_bookmark);
        bookmarkItem.setActionView(bookmarkView);
        MenuItem filter = menu.findItem(R.id.action_filter);
        Drawable drawable = filter.getIcon();
        drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        if (imdbObject.getBookmark()) {
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
                Toast.makeText(MovieDetailActivity.this, "Bookmark "+checked+" !!!", Toast.LENGTH_SHORT).show();
                if (checked && !imdbObject.getBookmark()) {
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_black);
                    User user = UsersUtils.getCurrentUser(getApplicationContext());
                    imdbObject.setBookmark(true);
                    CustomJSONObjectRequest jsonRequest_q = null;
                    String url = HOST_NAME + "movies/"+user.id;
                    JSONObject jsonBody = new JSONObject();
                    LOGD("0812", imdbObject.getTitle() + " " + imdbObject.getCountry());

                    try {
                        jsonBody.put("title", imdbObject.getTitle());
                        jsonBody.put("link", imdbObject.getDetailUrl());
                        jsonBody.put("picUrl", imdbObject.getPosterUrl());
                        jsonBody.put("country", imdbObject.getCountry());
                        jsonBody.put("channel", -1);
                        moviesFavor.addFavorite(getApplicationContext(), imdbObject.getTitle());
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
                            Toast.makeText(MovieDetailActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mQueue.add(jsonRequest_q);
                } else {
                    bookmarkView.setBackgroundResource(R.drawable.ic_turned_in_not_white);
                    User user = UsersUtils.getCurrentUser(getApplicationContext());
                    imdbObject.setBookmark(false);
                    moviesFavor.removeFavorite(getApplicationContext(), imdbObject.getTitle());
                    CustomJSONObjectRequest jsonRequest_q = null;
                    JSONObject jsonBody = new JSONObject();
                    String Query;

                    try {
                        jsonBody.put("title", imdbObject.getTitle());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        Query = URLEncoder.encode(imdbObject.getTitle(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionError("UTF-8 is unknown");
                    }

                    String url = HOST_NAME + "movies/"+user.id+"/"+Query;

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
                            Toast.makeText(MovieDetailActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mQueue.add(jsonRequest_q);
                }
            }
        });

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = new ShareActionProvider(this);
        /*shareActionProvider =  (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);*/
        MenuItemCompat.setActionProvider(shareItem, shareActionProvider);
        shareActionProvider.setShareIntent(createShareIntent());
        return true;

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
            case R.id.menu_smooth_zero:
                ImdbReviewTabFragment.movieReview.smoothScrollToPosition(0);
                return true;
            case R.id.menu_smooth_end:
                ImdbReviewTabFragment.movieReview.smoothScrollToPosition(ImdbReviewTabFragment.linearLayoutManager.getItemCount());
                return true;
            case R.id.action_filter:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
            case R.id.action_search:
                View searchMenuView = toolbar.findViewById(R.id.action_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                        getString(R.string.transition_search_back)).toBundle();
                Intent intent = new Intent(MovieDetailActivity.this, SearchActivity.class);
                intent.putExtra("lastSelectedPosition", lastSelectedPosition);
                intent.putExtra("lauchBy", "detail");
                ActivityCompat.startActivity(MovieDetailActivity.this, intent, null);
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
                Intent intent = new Intent(this, FavoriteActivity.class);
                User user = UsersUtils.getCurrentUser(getApplicationContext());
                intent.putExtra("user", user);
                createBackStack(intent);
//                createBackStack(new Intent(this, GenreActivity.class));
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
                            MovieDetailActivity.this).toBundle());
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
        if (type.compareTo("imdb") == 0)
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getDetailUrl());
        else
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getTrailerUrl());
        return shareIntent;
    }

    private void createShareAction() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (type.compareTo("imdb") == 0)
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getDetailUrl());
        else
            shareIntent.putExtra(Intent.EXTRA_TEXT, imdbObject.getTrailerUrl());
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
