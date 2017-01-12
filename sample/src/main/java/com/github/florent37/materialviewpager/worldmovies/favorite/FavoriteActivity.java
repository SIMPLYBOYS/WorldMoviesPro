package com.github.florent37.materialviewpager.worldmovies.favorite;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.github.florent37.materialviewpager.worldmovies.MainActivity;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.fragment.FansTabFragment;
import com.github.florent37.materialviewpager.worldmovies.fragment.FavoriteInfoTabFragment;
import com.github.florent37.materialviewpager.worldmovies.framework.ContentWebViewActivity;
import com.github.florent37.materialviewpager.worldmovies.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.nytimes.nyTimesActivity;
import com.github.florent37.materialviewpager.worldmovies.settings.SettingsActivity;
import com.github.florent37.materialviewpager.worldmovies.upcoming.upComingActivity;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.github.florent37.materialviewpager.worldmovies.util.UIUtils.setMenuItemScale;
import static com.github.florent37.materialviewpager.worldmovies.util.UIUtils.setViewScale;

/**
 * Created by aaron on 2016/6/21.
 */
public class FavoriteActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {
    private TextView userName;
    private ImageView userImage;
    private User user;
    private int lastSelectedPosition = 4;
    private BottomNavigationBar bottomNavigationBar;
    private BadgeItem numberBadgeItem;
    protected static final int NAV_ITEM_TREND = 0;
    protected static final int NAV_ITEM_UPCOMING = 1;
    protected static final int NAV_ITEM_IMDB = 2;
    protected static final int NAV_ITEM_NYTIMES = 3;
//    protected static final int NAV_ITEM_GENRE = 4;
    protected final int NAV_ITEM_FAVORITE = 4;
    LinearLayout socialAction;
    ShareActionProvider shareActionProvider;
    private ShineButton socialView = null;
    private MenuItem socialItem, shareItem, settingItem;
    private RelativeLayout user_profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        user = (User) getIntent().getSerializableExtra("user");

        if (user == null)
            user = UsersUtils.getCurrentUser(getApplicationContext());
        
        setupToolbar();
        setupViewPager();
        setupCollapsingToolbar();
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        userName = (TextView) findViewById(R.id.name);
        refresh();
        bottomNavigationBar.setTabSelectedListener(this);
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
                .addItem(new BottomNavigationItem(R.drawable.imdb, R.string.navdrawer_item_imdb).setActiveColorResource(R.color.material_blue_300))
                .addItem(new BottomNavigationItem(R.drawable.nytimes, "NyTimes").setActiveColorResource(R.color.material_brown_300))
                .addItem(new BottomNavigationItem(R.drawable.ic_person, "Profile").setActiveColorResource(R.color.material_red_900))
//                .addItem(new BottomNavigationItem(R.drawable.ic_genre, R.string.navdrawer_item_genre).setActiveColorResource(R.color.material_light_blue_A100))
                .setFirstSelectedPosition(lastSelectedPosition)
                .setBarBackgroundColor(R.color.foreground_material_light)
                .initialise();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite_menu, menu);
        socialAction = (LinearLayout) getLayoutInflater().inflate(R.layout.social_image, null);

        //-----------------//
        socialView = (ShineButton) socialAction.findViewById(R.id.socialView);
        socialView.init(this);
        setViewScale(this, socialView);
        socialView.setBackgroundResource(R.drawable.ic_person_add);
        socialView.setColorFilter(getResources().getColor(R.color.app_white));
        setMenuItemScale(this, menu);
        //-----------------//

        shareItem = menu.findItem(R.id.action_share);
        socialItem = menu.findItem(R.id.socialItem);
        settingItem = menu.findItem(R.id.action_settings);
        settingItem.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                createBackStack(new Intent(getApplicationContext(), SettingsActivity.class));
                return false;
            }
        });

        socialView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(FavoriteActivity.this, "Function Coming soon!", Toast.LENGTH_LONG).show();
                createBackStack(new Intent(getApplicationContext(), ExplorePeopleActivity.class));
            }
        });

        userImage = (ImageView) findViewById(R.id.avatar);
        user_profile = (RelativeLayout) findViewById(R.id.user_profile);
        user_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ContentWebViewActivity.class);
                intent.putExtra("url", user.link);
                context.startActivity(intent);
            }
        });

        if (user != null) {
            userName.setText(user.name);
            Picasso.with(userImage.getContext())
                    .load(user.pictureUrl)
                    .placeholder(R.drawable.person_image_empty)
                    .fit()
                    .centerCrop()
                    .into(userImage);
        }

        socialItem.setActionView(socialView);
//        settingItem.setActionView(settingView);
        shareActionProvider = new ShareActionProvider(this);
        MenuItemCompat.setActionProvider(shareItem, shareActionProvider);
        shareActionProvider.setShareIntent(createShareIntent());
        return true;
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (user != null)
            shareIntent.putExtra(Intent.EXTRA_TEXT, user.link); //TODO user's movie list page
        return shareIntent;
    }

    private void setupCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(
                R.id.collapse_toolbar);
        collapsingToolbar.setTitleEnabled(true);
    }

    private void setupViewPager() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(FavoriteInfoTabFragment.newInstance(user), "Collection");
        adapter.addFrag(FansTabFragment.newInstance("fans"), "Fans");
        adapter.addFrag(FansTabFragment.newInstance("follow"), "Following");
        viewPager.setAdapter(adapter);
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

    @Override
    public void onTabSelected(int position) {
        lastSelectedPosition = position;

        if (numberBadgeItem != null)
            numberBadgeItem.setText(Integer.toString(position));

        goToNavItem(position);
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
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(FavoriteActivity.this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    @Override
    public void onTabUnselected(int position) {
    }

    @Override
    public void onTabReselected(int position) {
        goToNavItem(position);
    }
}
