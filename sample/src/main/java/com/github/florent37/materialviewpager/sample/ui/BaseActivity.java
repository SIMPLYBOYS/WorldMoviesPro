

/**
 * Created by aaron on 2016/2/24.
 */

/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.florent37.materialviewpager.sample.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SyncStatusObserver;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.florent37.materialviewpager.sample.MainActivity;
import com.github.florent37.materialviewpager.sample.R;
import com.github.florent37.materialviewpager.sample.about.AboutActivity;
import com.github.florent37.materialviewpager.sample.framework.Model;
import com.github.florent37.materialviewpager.sample.framework.PresenterFragmentImpl;
import com.github.florent37.materialviewpager.sample.framework.QueryEnum;
import com.github.florent37.materialviewpager.sample.framework.UpdatableView;
import com.github.florent37.materialviewpager.sample.framework.UserActionEnum;
import com.github.florent37.materialviewpager.sample.imdb.ImdbActivity;
import com.github.florent37.materialviewpager.sample.model.User;
import com.github.florent37.materialviewpager.sample.provider.ScheduleContract;
import com.github.florent37.materialviewpager.sample.service.QuickstartPreferences;
import com.github.florent37.materialviewpager.sample.service.RegistrationIntentService;
import com.github.florent37.materialviewpager.sample.settings.SettingsUtils;
import com.github.florent37.materialviewpager.sample.sync.SyncHelper;
import com.github.florent37.materialviewpager.sample.ui.widget.MultiSwipeRefreshLayout;
import com.github.florent37.materialviewpager.sample.ui.widget.ScrimInsetsScrollView;
import com.github.florent37.materialviewpager.sample.util.AccountUtils;
import com.github.florent37.materialviewpager.sample.util.ImageLoader;
import com.github.florent37.materialviewpager.sample.util.LUtils;
import com.github.florent37.materialviewpager.sample.util.LoginAndAuthHelper;
import com.github.florent37.materialviewpager.sample.util.PrefUtils;
import com.github.florent37.materialviewpager.sample.welcome.WelcomeActivity;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.github.florent37.materialviewpager.sample.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.sample.util.LogUtils.LOGE;
import static com.github.florent37.materialviewpager.sample.util.LogUtils.LOGW;
import static com.github.florent37.materialviewpager.sample.util.LogUtils.makeLogTag;


/**
 * A base activity that handles common functionality in the app. This includes the
 * navigation drawer, login and authentication, Action Bar tweaks, amongst others.
 */
public abstract class BaseActivity extends AppCompatActivity implements
        LoginAndAuthHelper.Callbacks,
        SharedPreferences.OnSharedPreferenceChangeListener,
        MultiSwipeRefreshLayout.CanChildScrollUpCallback,
        SwipeRefreshLayout.OnRefreshListener {

    public final static String PRESENTER_TAG = "Presenter";

    private static final String TAG = makeLogTag(BaseActivity.class);

    private static final int SELECT_GOOGLE_ACCOUNT_RESULT = 9999;

    private static final int SELECT_FACEBOOK_ACCOUNT_RESULT = 64206;

    private AccessToken accessToken;

    private Context context;

    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth
    private LoginAndAuthHelper mLoginAndAuthHelper;

    // Navigation drawer:
    private DrawerLayout mDrawer;

    // Helper methods for L APIs
    private LUtils mLUtils;

    private ObjectAnimator mStatusBarColorAnimator;

    private LinearLayout mAccountListContainer;

    private ViewGroup mDrawerItemsListContainer;

    private ActionBarDrawerToggle mDrawerToggle;

    private Handler mHandler;

    private ImageView mExpandAccountBoxIndicator;

    private boolean mAccountBoxExpanded = false;

    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();

    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_MY_FAVORITE = 0;

    protected static final int NAVDRAWER_ITEM_IO_LIVE = 1;

    protected static final int NAVDRAWER_ITEM_EXPLORE = 2;

    protected static final int NAVDRAWER_ITEM_MAP = 3;

    protected static final int NAVDRAWER_ITEM_SOCIAL = 4;

    protected static final int NAVDRAWER_ITEM_VIDEO_LIBRARY = 5;

    protected static final int NAVDRAWER_ITEM_SIGN_IN = 6;

    protected static final int NAVDRAWER_ITEM_SETTINGS = 7;

    protected static final int NAVDRAWER_ITEM_ABOUT = 8;

    protected static final int NAVDRAWER_ITEM_DEBUG = 11;

    protected static final int NAVDRAWER_ITEM_SIGN_OUT = 10;

    protected static final int NAVDRAWER_ITEM_IMDB = 9;

    protected static final int NAVDRAWER_ITEM_INVALID = -1;

    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;

    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[] {
            R.string.navdrawer_item_my_favorite,
            R.string.navdrawer_item_io_live,
            R.string.navdrawer_item_explore,
            R.string.navdrawer_item_map,
            R.string.navdrawer_item_social,
            R.string.navdrawer_item_video_library,
            R.string.navdrawer_item_sign_in,
            R.string.navdrawer_item_settings,
            R.string.description_about,
            R.string.navdrawer_item_imdb,
            R.string.navdrawer_item_logout,
            R.string.navdrawer_item_debug
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_favorite,
            R.drawable.ic_navview_play_circle_fill, // I/O Live
            R.drawable.ic_trending_up,  // Explore
            R.drawable.ic_navview_map, // Map
            R.drawable.ic_speaker, // Social
            R.drawable.ic_navview_video_library, // Video Library
            R.drawable.facebook, // Sign in
            R.drawable.ic_navview_settings, // Settings.
            R.drawable.ic_info_outline, // About
            R.drawable.ic_theaters, //IMDB
            R.drawable.ic_navview_logout, //Sign out
            R.drawable.ic_navview_settings, // Debug
            R.drawable.ic_navview_my_schedule // My Schedule
    };

    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;

    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;

    // Primary toolbar and drawer toggle
    private Toolbar mActionBarToolbar;

    // handle to our sync observer (that notifies us about changes in our sync state)
    private Object mSyncObserverHandle;

    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private boolean mActionBarAutoHideEnabled = false;

    private int mActionBarAutoHideSensivity = 0;

    private int mActionBarAutoHideMinY = 0;

    private int mActionBarAutoHideSignal = 0;

    private boolean mActionBarShown = true;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;

    private boolean mManualSyncRequest;

    private int mThemedStatusBarColor;

    private int mNormalStatusBarColor;

    private int mProgressBarTopWhenActionBarShown;

    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();

    private ImageLoader mImageLoader;

    private CallbackManager callbackManager;

    private AccessTokenTracker accessTokenTracker;

    private ProfileTracker profileTracker;

    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private boolean isReceiverRegistered;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("0531", "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
//        RecentTasksStyler.styleRecentTasksEntry(this);
        // Check if the EULA has been accepted; if not, show it.
        //TODO change Welcome to Login Page which support facebook, google
        if (WelcomeActivity.shouldDisplay(this)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivityForVersion(intent);
            finish();
            return;
        }

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, mCallBack);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                //TODO currentAccessToken when it's loaded or set.
                accessToken = AccessToken.getCurrentAccessToken();
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();


        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                //TODO App code
            }
        };
        context = getApplicationContext();
        mImageLoader = new ImageLoader(this);
        mHandler = new Handler();

        setupWindowAnimations();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                Log.d("0523", String.valueOf(sentToken));
                /*if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.token_error_message));
                }*/
            }
        };

        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    private void selectItem(int position) {
        mDrawer.closeDrawer(GravityCompat.START);
        //TODO fragment transaction
    }

    private void setupWindowAnimations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Explode explode = new Explode();
            getWindow().setExitTransition(explode);

            Fade fade = new Fade();
            getWindow().setReenterTransition(fade);
        }
    }

    private void registerReceiver() {
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {

        //登入成功
        @Override
        public void onSuccess(LoginResult loginResult) {

            //accessToken之後或許還會用到 先存起來
            accessToken = loginResult.getAccessToken();

            Log.d("FB", "access token got.");
            Log.d("FB", "accessToken" + ": " + accessToken.getToken());

            final GraphRequest friendsRequest = new GraphRequest( /* handle the result */
                    AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            if(response.getError() == null) {
                                List<User> FriendList = new ArrayList<>();;
                                JSONObject jsonObj = response.getJSONObject();
                                JSONArray data = jsonObj.optJSONArray("data");

                                for(int i=0; i < data.length(); i++) {
                                    User user = new User();
                                    Log.d("FB", "friends complete " + i + ": " + data.optJSONObject(i));
                                    JSONObject dataobj= data.optJSONObject(i);
                                    user.name = dataobj.optString("name");
                                    user.facebookID = dataobj.optString("id");
                                    user.pictureUrl = "https://graph.facebook.com/" + user.facebookID + "/picture?type=large";
                                    FriendList.add(user);
                                }

                                PrefUtils.setCurrentFriends(FriendList, getApplicationContext());
                                User user = PrefUtils.getCurrentUser(getApplicationContext());
                                onAuthSuccess(user.name, true);
                                /*AccountUtils.setActiveAccount(getApplicationContext(), user.name);
                                    onAuthSuccess(user.name, true);
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();*/
                                startActivityForVersion(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(BaseActivity.this,
                                        response.getError().getErrorMessage(),
                                        Toast.LENGTH_SHORT).show();
                                startActivityForVersion(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        }
                    }
            );

            // If the access token is available already assign it.
            //send request and call graph api
            GraphRequest meRequest = GraphRequest.newMeRequest(
                    accessToken,
                    new GraphRequest.GraphJSONObjectCallback() {

                        //當RESPONSE回來的時候
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            if(response.getError() == null) {
                                User user = new User();

                                //讀出姓名 ID FB個人頁面連結
                                Log.d("FB", "complete " + object);
                                Log.d("FB", object.optString("name"));
                                Log.d("FB", object.optString("link"));
                                Log.d("FB", object.optString("id"));
                                Log.d("FB", object.optString("email"));
                                Log.d("FB", object.optString("gender"));

//                                Log.d("FB", object.optString("birthday"));
                                /*JSONObject object1 = object.optJSONObject("picture");
                                Log.d("FB", String.valueOf(object1.optJSONObject("data").optString("url")));*/

                                try {
                                    user.facebookID = object.optString("id");
                                    user.email = object.optString("email");
                                    user.name = object.optString("name");
                                    user.gender = object.optString("gender");
                                    user.accessToken = accessToken.getToken();
                                    user.pictureUrl = "https://graph.facebook.com/" + user.facebookID + "/picture?type=large";

                                    PrefUtils.setCurrentUser(user, getApplicationContext());
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields", "id,name,email,picture");
                                    friendsRequest.setParameters(parameters);
                                    friendsRequest.executeAsync();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(BaseActivity.this,
                                        response.getError().getErrorMessage(),
                                        Toast.LENGTH_SHORT).show();
                                startActivityForVersion(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        }
                    }
            );

            //包入你想要得到的資料 送出request
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,link,email,gender,birthday,picture");
            meRequest.setParameters(parameters);
            meRequest.executeAsync();
        }

        //登入取消
        @Override
        public void onCancel() {
            // App code
            Log.d("FB", "CANCEL");
        }

        //登入失敗
        @Override
        public void onError(FacebookException exception) {
            // App code
            Log.d("FB", exception.toString());
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            BaseActivity.this).toBundle());
        }
        else {
            startActivity(intent);
        }
    }

    private class DrawerItemClickListener
            implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
            selectItem(position);
        }
    }

    public void trySetupSwipeRefresh(){
        //do nothing here
    }

    protected void setProgressBarTopWhenActionBarShown(int progressBarTopWhenActionBarShown) {
        mProgressBarTopWhenActionBarShown = progressBarTopWhenActionBarShown;
        updateSwipeRefreshProgressBarTop();
    }

    public void updateSwipeRefreshProgressBarTop() {
        //do nothing here
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        //donothing here
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Sets up the navigation drawer as appropriate. Note that the nav drawer will be
     * different depending on whether the attendee indicated that they are attending the
     * event on-site vs. attending remotely.
     */
    private void setupNavDrawer() {

        // What nav drawer item should be selected?
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mDrawer == null) {
            return;
        }

        mDrawer.setStatusBarBackgroundColor(
                getResources().getColor(R.color.theme_primary_dark));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, 0, 0);
        mDrawerToggle.syncState();

//        mDrawer.setDrawerListener(mDrawerToggle);

        int selfItem = getSelfNavDrawerItem();

        if (selfItem == NAVDRAWER_ITEM_INVALID) {
            // do not show a nav drawer
            if (mDrawer != null) {
                ((ViewGroup) mDrawer.getParent()).removeView(mDrawer);
            }
            mDrawer = null;
            return;
        }

        ScrimInsetsScrollView navDrawer = (ScrimInsetsScrollView) mDrawer.findViewById(R.id.navdrawer);

        Log.d("0227", "selfItem: " +selfItem);

        if (navDrawer != null) {
            final View chosenAccountContentView = findViewById(R.id.chosen_account_content_view);
            final View chosenAccountView = findViewById(R.id.chosen_account_view);
            final int navDrawerChosenAccountHeight = getResources().getDimensionPixelSize(
                    R.dimen.navdrawer_chosen_account_height);
            navDrawer.setOnInsetsCallback(new ScrimInsetsScrollView.OnInsetsCallback() {
                @Override
                public void onInsetsChanged(Rect insets) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) chosenAccountContentView.getLayoutParams();
                    lp.topMargin = insets.top;
                    chosenAccountContentView.setLayoutParams(lp);

                    ViewGroup.LayoutParams lp2 = chosenAccountView.getLayoutParams();
                    lp2.height = navDrawerChosenAccountHeight + insets.top;
                    chosenAccountView.setLayoutParams(lp2);
                }
            });
        }

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            });
        }

        mDrawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                //updateStatusBarForNavDrawerSlide(0f);
                onNavDrawerStateChanged(false, false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                //updateStatusBarForNavDrawerSlide(1f);
                onNavDrawerStateChanged(true, false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                //invalidateOptionsMenu();
                onNavDrawerStateChanged(isNavDrawerOpen(), newState != DrawerLayout.STATE_IDLE);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //updateStatusBarForNavDrawerSlide(slideOffset);
                onNavDrawerSlide(slideOffset);
            }
        });

        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // populate the nav drawer with the correct items
        populateNavDrawer();

        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!SettingsUtils.isFirstRunProcessComplete(this)) {
            // first run of the app starts with the nav drawer open
            SettingsUtils.markFirstRunProcessesDone(this, true);
            mDrawer.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    // Subclasses can override this for custom behavior
    protected void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating) {
        if (mActionBarAutoHideEnabled && isOpen) {
            autoShowOrHideActionBar(true);
        }
    }

    protected void onNavDrawerSlide(float offset) {
    }

    protected boolean isNavDrawerOpen() {
        return mDrawer != null && mDrawer.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawer != null) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
    }

    /**
     * Defines the Navigation Drawer items to display by updating {@code mNavDrawerItems} then
     * forces the Navigation Drawer to redraw itself.
     */
    private void populateNavDrawer() {

        Log.d("0227", "populateNavDrawer user: " + PrefUtils.getCurrentUser(this) + " account: " + AccountUtils.hasActiveAccount(this));
        mNavDrawerItems.clear();

        // decide which items will appear in the nav drawer
        if (AccountUtils.hasActiveAccount(this) || PrefUtils.getCurrentUser(this) != null) {
            // Only logged-in users can save sessions, so if there is no active account,
            // there is no My Schedule
            mNavDrawerItems.add(NAVDRAWER_ITEM_MY_FAVORITE);
            // Explore is always shown.
            mNavDrawerItems.add(NAVDRAWER_ITEM_EXPLORE);
            // Other items that are always in the nav drawer.
            mNavDrawerItems.add(NAVDRAWER_ITEM_IMDB);
            mNavDrawerItems.add(NAVDRAWER_ITEM_SOCIAL);
            mNavDrawerItems.add(NAVDRAWER_ITEM_VIDEO_LIBRARY);
            mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR_SPECIAL);
            mNavDrawerItems.add(NAVDRAWER_ITEM_SIGN_OUT);
            mNavDrawerItems.add(NAVDRAWER_ITEM_ABOUT);
        } else {
            // If no active account, show Sign In
            mNavDrawerItems.add(NAVDRAWER_ITEM_SIGN_IN);
            mNavDrawerItems.add(NAVDRAWER_ITEM_EXPLORE);
            mNavDrawerItems.add(NAVDRAWER_ITEM_IMDB);
            // Other items that are always in the nav drawer.
            mNavDrawerItems.add(NAVDRAWER_ITEM_SOCIAL);
            mNavDrawerItems.add(NAVDRAWER_ITEM_VIDEO_LIBRARY);
            mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR_SPECIAL);
//        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);

            mNavDrawerItems.add(NAVDRAWER_ITEM_ABOUT);
        }
        createNavDrawerItems();
    }

    @Override
    public void onBackPressed() {
        Log.d("0531", "onBackPressed");
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        Log.d("0531: ", String.valueOf(mNavDrawerItems.size()));

        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            Log.d("0531: ", String.valueOf(i));
            ++i;
        }
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    mNavDrawerItemViews[i].setActivated(itemId == thisItemId);
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key != null && key.equals(SettingsUtils.PREF_ATTENDEE_AT_VENUE)) {
            LOGD(TAG, "Attendee at venue preference changed, repopulating nav drawer and menu.");
//            populateNavDrawer();
            invalidateOptionsMenu();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("0531", "onStart");
        setupNavDrawer();
        setupAccountBox(true);
        trySetupSwipeRefresh();
        updateSwipeRefreshProgressBarTop();

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d("0531", "onPostCreate");
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            LOGW(TAG, "No view with ID main_content to fade in.");
        }
    }

    /**
     * Sets up the account box. The account box is the area at the top of the nav drawer that
     * shows which account the user is logged in as, and lets them switch accounts. It also
     * shows the user's Google+ cover photo as background.
     */
    private void setupAccountBox(boolean fromFB) {

        mAccountListContainer = (LinearLayout) findViewById(R.id.account_list);

        if (mAccountListContainer == null) {
            //This activity does not have an account box
            return;
        }

        String friends = PrefUtils.getCurrentFriends(this);
        Gson gson = new Gson();
        List<User> friendslist = gson.fromJson(friends, new TypeToken<List<User>>(){}.getType());
        Log.d("0526", String.valueOf(friendslist));

        final View chosenAccountView = findViewById(R.id.chosen_account_view);
        Account chosenAccount = AccountUtils.getActiveAccount(this);
        Log.d("0314", "chosenAccount: " + chosenAccount);

        chosenAccountView.setVisibility(View.VISIBLE);
        mAccountListContainer.setVisibility(View.INVISIBLE);

        ImageView coverImageView = (ImageView) chosenAccountView
                .findViewById(R.id.profile_cover_image);
        ImageView profileImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_image);
        TextView nameTextView = (TextView) chosenAccountView.findViewById(R.id.profile_name_text);
        TextView email = (TextView) chosenAccountView.findViewById(R.id.profile_email_text);
        mExpandAccountBoxIndicator = (ImageView) findViewById(R.id.expand_account_box_indicator);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Yes, the title is clickable", Toast.LENGTH_SHORT).show();
                //TODO show user profile
            }
        });

        String name = AccountUtils.getPlusName(this);
        if (name == null) {
            nameTextView.setVisibility(View.GONE);
        } else {
            nameTextView.setVisibility(View.VISIBLE);
            nameTextView.setText(name);
        }

        String imageUrl = null;

        if (fromFB) {
            User user= PrefUtils.getCurrentUser(this);
            if (user != null) {
                imageUrl = user.pictureUrl;
                nameTextView.setVisibility(View.VISIBLE);
                nameTextView.setText(user.name);
                email.setText(user.email);
            }

        } else {
            imageUrl = AccountUtils.getPlusImageUrl(this);
            email.setText(chosenAccount != null ? chosenAccount.name: "");
        }

        if (imageUrl != null) {
            mImageLoader.loadImage(imageUrl, profileImageView);
        }

        String coverImageUrl = AccountUtils.getPlusCoverUrl(this);

        if (coverImageUrl != null) {
            findViewById(R.id.profile_cover_image_placeholder).setVisibility(View.GONE);
            coverImageView.setVisibility(View.VISIBLE);
            coverImageView.setContentDescription(getResources().getString(
                    R.string.navview_header_user_image_content_description));
            mImageLoader.loadImage(coverImageUrl, coverImageView);
            coverImageView.setColorFilter(getResources().getColor(R.color.light_content_scrim));
        }

        if (friendslist == null) {
            // There's only one account on the device, so no need for a switcher.
            mExpandAccountBoxIndicator.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            chosenAccountView.setEnabled(false);
            return;
        }

        if (friendslist.size() == 0) {
            // There's only one account on the device, so no need for a switcher.
            mExpandAccountBoxIndicator.setVisibility(View.GONE);
            mAccountListContainer.setVisibility(View.GONE);
            chosenAccountView.setEnabled(false);
            return;
        } else {
            mExpandAccountBoxIndicator.setVisibility(View.VISIBLE);
            chosenAccountView.setEnabled(true);
            mExpandAccountBoxIndicator.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mAccountBoxExpanded = !mAccountBoxExpanded;
                    setupAccountBoxToggle();
                }
            });
        }

        setupAccountBoxToggle();

        populateAccountList(friendslist);
    }

    private void populateAccountList(List<User> friendList) {

        mAccountListContainer.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        for (Iterator it = friendList.iterator(); it.hasNext();) {
            View itemView = layoutInflater.inflate(R.layout.list_item_account, mAccountListContainer, false);
            User user = (User) it.next();


            ((TextView) itemView.findViewById(R.id.profile_email_text))
                    .setText(user.name);
            final String accountName = user.name;
            String imageUrl = user.pictureUrl;

            Log.d("0426", String.valueOf(user.name));

            if (!TextUtils.isEmpty(imageUrl)) {
                mImageLoader.loadImage(imageUrl,
                        (ImageView) itemView.findViewById(R.id.profile_image));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ConnectivityManager cm = (ConnectivityManager)
                            getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork == null || !activeNetwork.isConnected()) {
                        // if there's no network, don't try to change the selected account
                        Toast.makeText(BaseActivity.this, R.string.no_connection_cant_login,
                                Toast.LENGTH_SHORT).show();
                        mDrawer.closeDrawer(GravityCompat.START);
                    } else {
                        Log.d("0426", "show account: " + accountName);
                        //TODO friend profile display
                        /*AccountUtils.setActiveAccount(BaseActivity.this, accountName);
                        onAccountChangeRequested();
                        startLoginProcess();
                        mAccountBoxExpanded = false;
                        setupAccountBoxToggle();
                        mDrawer.closeDrawer(GravityCompat.START);
                        setupAccountBox(true);*/
                    }
                }
            });
            mAccountListContainer.addView(itemView);
        }
    }

    protected void onAccountChangeRequested() {
        // override if you want to be notified when another account has been selected account has changed
    }

    private void setupAccountBoxToggle() {
        int selfItem = getSelfNavDrawerItem();
        if (mDrawer == null || selfItem == NAVDRAWER_ITEM_INVALID) {
            // this Activity does not have a nav drawer
            return;
        }
        if (mExpandAccountBoxIndicator != null) {
            mExpandAccountBoxIndicator.setImageResource(mAccountBoxExpanded
                    ? R.drawable.ic_navview_accounts_collapse
                    : R.drawable.ic_navview_accounts_expand);
        }
        int hideTranslateY = -mAccountListContainer.getHeight() / 4; // last 25% of animation
        if (mAccountBoxExpanded && mAccountListContainer.getTranslationY() == 0) {
            // initial setup
            mAccountListContainer.setAlpha(0);
            mAccountListContainer.setTranslationY(hideTranslateY);
        }

        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mDrawerItemsListContainer == null) {
                    return;
                }
                mDrawerItemsListContainer.setVisibility(mAccountBoxExpanded
                        ? View.INVISIBLE : View.VISIBLE);
                mAccountListContainer.setVisibility(mAccountBoxExpanded
                        ? View.VISIBLE : View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });

        if (mAccountBoxExpanded) {
            Log.d("0315", "Expanded");
            mAccountListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    ObjectAnimator.ofFloat(mDrawerItemsListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    subSet);
            set.start();
        } else {
            Log.d("0315", "deExpanded");
            mDrawerItemsListContainer.setVisibility(View.VISIBLE);
            AnimatorSet subSet = new AnimatorSet();
            subSet.playTogether(
                    ObjectAnimator.ofFloat(mAccountListContainer, View.ALPHA, 0)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION),
                    ObjectAnimator.ofFloat(mAccountListContainer, View.TRANSLATION_Y,
                            hideTranslateY)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.playSequentially(
                    subSet,
                    ObjectAnimator.ofFloat(mDrawerItemsListContainer, View.ALPHA, 1)
                            .setDuration(ACCOUNT_BOX_EXPAND_ANIM_DURATION));
            set.start();
        }
        set.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) ||
                super.onOptionsItemSelected(item);
    }

    private void goToNavDrawerItem(int item) {
        Log.d("0213", "goToNavDrawerItem: " + item);
        switch (item) {
            case NAVDRAWER_ITEM_MY_FAVORITE:
//                createBackStack(new Intent(this, MyScheduleActivity.class));
//                createBackStack(new Intent(this, VideoLibraryActivity.class));
                break;
            case NAVDRAWER_ITEM_EXPLORE:
//                context.startService(new Intent(context, DataBootstrapService.class)); //get latest JSON file from server
                startActivityForVersion(new Intent(this, MainActivity.class));
                finish();
                break;
            case NAVDRAWER_ITEM_MAP:
//                createBackStack(new Intent(this, MapActivity.class));
                break;
            case NAVDRAWER_ITEM_SOCIAL:
//                createBackStack(new Intent(this, SocialActivity.class));
                break;
            case NAVDRAWER_ITEM_VIDEO_LIBRARY:
//                createBackStack(new Intent(this, VideoLibraryActivity.class));
//                createBackStack(new Intent(this, LiveActivity.class));
                break;
            case NAVDRAWER_ITEM_SIGN_IN:
                signInOrCreateAnAccount();
                break;
            case NAVDRAWER_ITEM_SIGN_OUT:
                signOut();
                break;
            case NAVDRAWER_ITEM_SETTINGS:
//                createBackStack(new Intent(this, SettingsActivity.class));
                break;
            case NAVDRAWER_ITEM_ABOUT:
                createBackStack(new Intent(this, AboutActivity.class));
                break;
            case NAVDRAWER_ITEM_IMDB:
                createBackStack(new Intent(this, ImdbActivity.class));
                break;
        }
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
            startActivityForVersion(intent);
            finish();
        }
    }

    /**
     * This utility method handles Up navigation intents by searching for a parent activity and
     * navigating there if defined. When using this for an activity make sure to define both the
     * native parentActivity as well as the AppCompat one when supporting API levels less than 16.
     * when the activity has a single parent activity. If the activity doesn't have a single parent
     * activity then don't define one and this method will use back button functionality. If "Up"
     * functionality is still desired for activities without parents then use
     * {@code syntheticParentActivity} to define one dynamically.
     *
     * Note: Up navigation intents are represented by a back arrow in the top left of the Toolbar
     *       in Material Design guidelines.
     *
     * @param currentActivity Activity in use when navigate Up action occurred.
     * @param syntheticParentActivity Parent activity to use when one is not already configured.
     */
    public static void navigateUpOrBack(Activity currentActivity,
                                        Class<? extends Activity> syntheticParentActivity) {
        // Retrieve parent activity from AndroidManifest.
        Intent intent = NavUtils.getParentActivityIntent(currentActivity);

        // Synthesize the parent activity when a natural one doesn't exist.
        if (intent == null && syntheticParentActivity != null) {
            try {
                intent = NavUtils.getParentActivityIntent(currentActivity, syntheticParentActivity);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (intent == null) {
            // No parent defined in manifest. This indicates the activity may be used by
            // in multiple flows throughout the app and doesn't have a strict parent. In
            // this case the navigation up button should act in the same manner as the
            // back button. This will result in users being forwarded back to other
            // applications if currentActivity was invoked from another application.
            currentActivity.onBackPressed();
        } else {
            if (NavUtils.shouldUpRecreateTask(currentActivity, intent)) {
                // Need to synthesize a backstack since currentActivity was probably invoked by a
                // different app. The preserves the "Up" functionality within the app according to
                // the activity hierarchy defined in AndroidManifest.xml via parentActivity
                // attributes.
                TaskStackBuilder builder = TaskStackBuilder.create(currentActivity);
                builder.addNextIntentWithParentStack(intent);
                builder.startActivities();
            } else {
                // Navigate normally to the manifest defined "Up" activity.
                NavUtils.navigateUpTo(currentActivity, intent);
            }
        }
    }

    private void signInOrCreateAnAccount() {
        //Get list of accounts on device.
        /*AccountManager am = AccountManager.get(BaseActivity.this);
        Account[] accountArray = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accountArray.length == 0) {
            //Send the user to the "Add Account" page.
            Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
            intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE});
            startActivityForVersion(intent);
        } else {
            //Try to log the user in with the first account on the device.
            startLoginProcess();
            mDrawer.closeDrawer(GravityCompat.START);
        }*/

        if (!AccountUtils.enforceActiveFaceBookAccount(this, SELECT_FACEBOOK_ACCOUNT_RESULT)) {
            Log.d("0314", "EnforceActiveFaceBookAccount returned false");
            return;
        }

        Log.d("0314", "Login do nothing now.");
//        mDrawer.closeDrawer(GravityCompat.START);
    }

    private void signOut(){
        Log.d("0315", "signOut");

        PrefUtils.clearCurrentUser(this);
        AccountUtils.clearActiveAccount(this);
        LoginManager.getInstance().logOut();

        Intent i=  new Intent(this, MainActivity.class);
        startActivityForVersion(i);
        finish();
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (isSpecialItem(itemId)) {
            goToNavDrawerItem(itemId);
        } else {
            // launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(itemId);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            // change the active item on the list so the user can see the item changed
            setSelectedNavDrawerItem(itemId);
            // fade out the main content
            View mainContent = findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }
        mDrawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("0219", "BaseActivity -> onResume");
        // Perform one-time bootstrap setup, if needed
//        DataBootstrapService.startDataBootstrapIfNecessary(this);

        // Check to ensure a Google Account is active for the app. Placing the check here ensures
        // it is run again in the case where a Google Account wasn't present on the device and a
        // picker had to be started.

        /*if (!AccountUtils.enforceActiveGoogleAccount(this, SELECT_GOOGLE_ACCOUNT_RESULT)) {
            LOGD(TAG, "EnforceActiveGoogleAccount returned false");
            return;
        }*/

        // Watch for sync state changes
        mSyncStatusObserver.onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

        startLoginProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("0531", "onPause");
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    /**
     * Converts an intent into a {@link Bundle} suitable for use as fragment arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts an intent and a {@link Bundle} into a {@link Bundle} suitable for use as fragment arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent, Bundle extras) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable("_uri");
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }

    private void promptAddAccount() {
        Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
        startActivityForVersion(intent);
        finish();
    }

    private void startLoginProcess() {
        LOGD(TAG, "Starting login process.");
        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "No active account, attempting to pick a default.");
            String defaultAccount = AccountUtils.getActiveAccountName(this);
            if (defaultAccount == null) {
                LOGE(TAG, "Failed to pick default account (no accounts). Failing.");
                //complainMustHaveGoogleAccount();
                return;
            }
            LOGD(TAG, "Default to: " + defaultAccount);
            AccountUtils.setActiveAccount(this, defaultAccount);
        }

        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "Can't proceed with login -- no account chosen.");
            return;
        } else {
            LOGD(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(this));
        }

        String accountName = AccountUtils.getActiveAccountName(this);
        LOGD(TAG, "Chosen account: " + AccountUtils.getActiveAccountName(this));

        if (mLoginAndAuthHelper != null && mLoginAndAuthHelper.getAccountName()
                .equals(accountName)) {
            LOGD(TAG, "Helper already set up; simply starting it.");
            mLoginAndAuthHelper.start();
            return;
        }

        LOGD(TAG, "Starting login process with account " + accountName);

        if (mLoginAndAuthHelper != null) {
            LOGD(TAG, "Tearing down old Helper, was " + mLoginAndAuthHelper.getAccountName());
            if (mLoginAndAuthHelper.isStarted()) {
                LOGD(TAG, "Stopping old Helper");
                mLoginAndAuthHelper.stop();
            }
            mLoginAndAuthHelper = null;
        }

        LOGD(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthHelper = new LoginAndAuthHelper(this, this, accountName);
        mLoginAndAuthHelper.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle the select {@code startActivityForResult} from
        // {@code enforceActiveGoogleAccount()} when a Google Account wasn't present on the device.
        if (requestCode == SELECT_GOOGLE_ACCOUNT_RESULT) {
            if (resultCode == RESULT_OK) {
                // Set selected GoogleAccount as active.
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                AccountUtils.setActiveAccount(this, accountName);
                onAuthSuccess(accountName, true);
            } else {
                LOGW(TAG, "A Google Account is required to use this application.");
                // This application requires a Google Account to be selected.
                finish();
            }
            return;
        } else if (requestCode == SELECT_FACEBOOK_ACCOUNT_RESULT){
            if (resultCode == RESULT_OK) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            } else {
                finish();
            }
        }

        if (mLoginAndAuthHelper == null || !mLoginAndAuthHelper.onActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onStop() {
        LOGD("0531", "onStop");
        super.onStop();
        if (mLoginAndAuthHelper != null) {
            mLoginAndAuthHelper.stop();
        }
    }

    protected void refreshAccountDependantData() {
        // Force local data refresh for data that depends on the logged user:
        LOGD(TAG, "Refreshing User Data");
        getContentResolver().notifyChange(ScheduleContract.MySchedule.CONTENT_URI, null, false);
        getContentResolver().notifyChange(ScheduleContract.MyViewedVideos.CONTENT_URI, null, false);
        getContentResolver().notifyChange(
                ScheduleContract.MyFeedbackSubmitted.CONTENT_URI, null, false);
    }

    protected void retryAuth() {
        mLoginAndAuthHelper.retryAuthByUserRequest();
    }

    /**
     * Called when authentication succeeds. This may either happen because the user just
     * authenticated for the first time (and went through the sign in flow), or because it's
     * a returning user.
     *
     * @param accountName        name of the account that just authenticated successfully.
     * @param newlyAuthenticated If true, this user just authenticated for the first time.
     *                           If false, it's a returning user.
     */
    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {
        Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        LOGD(TAG, "onAuthSuccess, account " + accountName + ", newlyAuthenticated="
                + newlyAuthenticated);
        Log.d("0314", "onAuthSuccess, account " + accountName + ", newlyAuthenticated="
                + newlyAuthenticated);

        refreshAccountDependantData();

        if (newlyAuthenticated) {
            LOGD(TAG, "Enabling auto sync on content provider for onreaccount " + accountName);
            SyncHelper.updateSyncInterval(this, account);
            SyncHelper.requestManualSync(account);
        }
        setupAccountBox(true);
        populateNavDrawer();
//        registerGCMClient();
    }

    @Override
    public void onAuthFailure(String accountName) {
        LOGD(TAG, "Auth failed for account " + accountName);
        refreshAccountDependantData();
    }

    @Override
    public void onPlusInfoLoaded(String accountName) {
        setupAccountBox(false);
        populateNavDrawer();
    }

    /**
     * Initializes the Action Bar auto-hide (aka Quick Recall) effect.
     */
    private void initActionBarAutoHide() {
        mActionBarAutoHideEnabled = true;
        mActionBarAutoHideMinY = getResources().getDimensionPixelSize(
                R.dimen.action_bar_auto_hide_min_y);
        mActionBarAutoHideSensivity = getResources().getDimensionPixelSize(
                R.dimen.action_bar_auto_hide_sensivity);
    }

    /**
     * Indicates that the main content has scrolled (for the purposes of showing/hiding
     * the action bar for the "action bar auto hide" effect). currentY and deltaY may be exact
     * (if the underlying view supports it) or may be approximate indications:
     * deltaY may be INT_MAX to mean "scrolled forward indeterminately" and INT_MIN to mean
     * "scrolled backward indeterminately".  currentY may be 0 to mean "somewhere close to the
     * start of the list" and INT_MAX to mean "we don't know, but not at the start of the list"
     */
    private void onMainContentScrolled(int currentY, int deltaY) {
        if (deltaY > mActionBarAutoHideSensivity) {
            deltaY = mActionBarAutoHideSensivity;
        } else if (deltaY < -mActionBarAutoHideSensivity) {
            deltaY = -mActionBarAutoHideSensivity;
        }

        if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0) {
            // deltaY is a motion opposite to the accumulated signal, so reset signal
            mActionBarAutoHideSignal = deltaY;
        } else {
            // add to accumulated signal
            mActionBarAutoHideSignal += deltaY;
        }

        boolean shouldShow = currentY < mActionBarAutoHideMinY ||
                (mActionBarAutoHideSignal <= -mActionBarAutoHideSensivity);
        autoShowOrHideActionBar(shouldShow);
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                // Depending on which version of Android you are on the Toolbar or the ActionBar may be
                // active so the a11y description is set here.
                mActionBarToolbar.setNavigationContentDescription(getResources().getString(R.string
                        .navdrawer_description_a11y));
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown) {
            return;
        }

        mActionBarShown = show;
        onActionBarAutoShowOrHide(show);
    }

    protected void enableActionBarAutoHide(final ListView listView) {
        initActionBarAutoHide();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            /** The heights of all items. */
            private Map<Integer, Integer> heights = new HashMap<>();
            private int lastCurrentScrollY = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {

                // Get the first visible item's view.
                View firstVisibleItemView = view.getChildAt(0);
                if (firstVisibleItemView == null) {
                    return;
                }

                // Save the height of the visible item.
                heights.put(firstVisibleItem, firstVisibleItemView.getHeight());

                // Calculate the height of all previous (hidden) items.
                int previousItemsHeight = 0;
                for (int i = 0; i < firstVisibleItem; i++) {
                    previousItemsHeight += heights.get(i) != null ? heights.get(i) : 0;
                }

                int currentScrollY = previousItemsHeight - firstVisibleItemView.getTop()
                        + view.getPaddingTop();

                onMainContentScrolled(currentScrollY, currentScrollY - lastCurrentScrollY);

                lastCurrentScrollY = currentScrollY;
            }
        });
    }

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == itemId;
        if (isSeparator(itemId)) {
            View separator =
                    getLayoutInflater().inflate(R.layout.navdrawer_separator, container, false);
            setAccessibilityIgnore(separator);
            return separator;
        }

        View item =  getLayoutInflater().inflate(
                R.layout.navdrawer_item, container, false);

        ImageView iconView = (ImageView) item.findViewById(R.id.icon);
        TextView titleView = (TextView) item.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ?
                NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ?
                NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

//        item.setContent(NAVDRAWER_ICON_RES_ID[itemId], NAVDRAWER_TITLE_RES_ID[itemId]);
        item.setActivated(getSelfNavDrawerItem() == itemId);
        if (item.isActivated()) {
            item.setContentDescription(getString(R.string.navdrawer_selected_menu_item_a11y_wrapper,
                    getString(NAVDRAWER_TITLE_RES_ID[itemId])));
        } else {
            item.setContentDescription(getString(R.string.navdrawer_menu_item_a11y_wrapper,
                    getString(NAVDRAWER_TITLE_RES_ID[itemId])));
        }

        formatNavDrawerItem(item, itemId, selected);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });
        return item;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    private boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_SETTINGS;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("0531", "onDestroy");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String accountName = AccountUtils.getActiveAccountName(BaseActivity.this);
                    if (TextUtils.isEmpty(accountName)) {
                        onRefreshingStateChanged(false);
                        mManualSyncRequest = false;
                        return;
                    }

                    Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, ScheduleContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, ScheduleContract.CONTENT_AUTHORITY);
                    if (!syncActive && !syncPending) {
                        mManualSyncRequest = false;
                    }
                    onRefreshingStateChanged(syncActive || mManualSyncRequest);
                }
            });
        }
    };

    protected void onRefreshingStateChanged(boolean refreshing) {
        //Do nothing
    }

    protected void enableDisableSwipeRefresh(boolean enable) {
        //Do nothing
    }

    protected void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView);
        }
    }

    protected void deregisterHideableHeaderView(View hideableHeaderView) {
        if (mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.remove(hideableHeaderView);
        }
    }

    public LUtils getLUtils() {
        return mLUtils;
    }

    public int getThemedStatusBarColor() {
        return mThemedStatusBarColor;
    }

    public void setNormalStatusBarColor(int color) {
        mNormalStatusBarColor = color;
        if (mDrawer != null) {
            mDrawer.setStatusBarBackgroundColor(mNormalStatusBarColor);
        }
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }
        mStatusBarColorAnimator = ObjectAnimator.ofInt(
                (mDrawer != null) ? mDrawer : mLUtils,
                (mDrawer != null) ? "statusBarBackgroundColor" : "statusBarColor",
                shown ? Color.BLACK : mNormalStatusBarColor,
                shown ? mNormalStatusBarColor : Color.BLACK)
                .setDuration(250);
        if (mDrawer != null) {
            mStatusBarColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ViewCompat.postInvalidateOnAnimation(mDrawer);
                }
            });
        }
        mStatusBarColorAnimator.setEvaluator(ARGB_EVALUATOR);
        mStatusBarColorAnimator.start();

        updateSwipeRefreshProgressBarTop();

        for (final View view : mHideableHeaderViews) {
            if (shown) {
                ViewCompat.animate(view)
                        .translationY(0)
                        .alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        // Setting Alpha animations should be done using the
                        // layer_type set to layer_type_hardware for the duration of the animation.
                        .withLayer();
            } else {
                ViewCompat.animate(view)
                        .translationY(-view.getBottom())
                        .alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        // Setting Alpha animations should be done using the
                        // layer_type set to layer_type_hardware for the duration of the animation.
                        .withLayer();
            }
        }
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return false;
    }

    /**
     * Adds a {@link com.github.florent37.materialviewpager.sample.framework.PresenterFragmentImpl} to the
     * Activity if required, and sets it up with the {@code model}, {@code queries},
     * {@code actions} and the {@link com.github.florent37.materialviewpager.sample.framework.UpdatableView}
     * corresponding to the {@code updatableViewResId}.
     *
     * @return the {@link com.github.florent37.materialviewpager.sample.framework.PresenterFragmentImpl},
     */
    public PresenterFragmentImpl addPresenterFragment(int updatableViewResId, Model model, QueryEnum[] queries,
                                                      UserActionEnum[] actions) {
        FragmentManager fragmentManager = getFragmentManager();

        //Check if the presenter fragment is already present (ie if the activity is recreated due
        // to orientation change).
        PresenterFragmentImpl presenter = (PresenterFragmentImpl) fragmentManager.findFragmentByTag(
                PRESENTER_TAG);
        if (presenter == null) {
            Log.d("0219-1","presenter is null");
            //Create, set up and add the presenter.
            presenter = new PresenterFragmentImpl();
            setUpPresenter(presenter, fragmentManager, updatableViewResId, model, queries, actions);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(presenter, PRESENTER_TAG);
            fragmentTransaction.commit();
        } else {
            Log.d("0219-2","presenter is not null");
            //Set up the presenter.
            setUpPresenter(presenter, fragmentManager, updatableViewResId, model, queries, actions);
        }
        return presenter;
    }

    /**
     * Registers the {@code presenter} as a
     * {@link com.github.florent37.materialviewpager.sample.util.ThrottledContentObserver} for the given
     * {@code uri}. When the content is changed, the specified {@code queries} are run.
     */
    public void registerPresenterFragmentAsContentObserverForUri
    (PresenterFragmentImpl presenter, Uri uri, QueryEnum[] queries) {
        if (presenter != null) {
            presenter.registerContentObserverOnUri(uri, queries);
        } else {
            LOGE(TAG, "You must add the presenter using addPresenterFragment method before " +
                    "calling registerPresenterFragmentAsContentObserverForUri! Pass in the returned"
                    + " object from addPresenterFragment as first argument.");
        }
    }

    private void setUpPresenter(PresenterFragmentImpl presenter, FragmentManager fragmentManager,
                                int updatableViewResId, Model model, QueryEnum[] queries,
                                UserActionEnum[] actions) {
        UpdatableView ui = (UpdatableView) fragmentManager.findFragmentById(
                updatableViewResId);
        presenter.setModel(model);
        presenter.setUpdatableView(ui);
        presenter.setInitialQueriesToLoad(queries);
        presenter.setValidUserActions(actions);
    }

    /**
     * Configure this Activity as a floating window, with the given {@code width}, {@code height}
     * and {@code alpha}, and dimming the background with the given {@code dim} value.
     */
    protected void setupFloatingWindow(int width, int height, int alpha, float dim) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(width);
        params.height = getResources().getDimensionPixelSize(height);
        params.alpha = alpha;
        params.dimAmount = dim;
        params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        getWindow().setAttributes(params);
    }

    /**
     * Returns true if the theme sets the {@code R.attr.isFloatingWindow} flag to true.
     */
    protected boolean shouldBeFloatingWindow() {
        Resources.Theme theme = getTheme();
        TypedValue floatingWindowFlag = new TypedValue();

        // Check isFloatingWindow flag is defined in theme.
        if (theme == null || !theme
                .resolveAttribute(R.attr.isFloatingWindow, floatingWindowFlag, true)) {
            return false;
        }

        return (floatingWindowFlag.data != 0);
    }
}
