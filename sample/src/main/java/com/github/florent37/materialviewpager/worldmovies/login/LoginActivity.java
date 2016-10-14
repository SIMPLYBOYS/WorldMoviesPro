package com.github.florent37.materialviewpager.worldmovies.login;

import android.accounts.Account;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.MainActivity;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.adapter.CustomPagerAdapter;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.model.PagerObject;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.sync.SyncHelper;
import com.github.florent37.materialviewpager.worldmovies.util.AccountUtils;
import com.github.florent37.materialviewpager.worldmovies.util.LoginAndAuthHelper;
import com.github.florent37.materialviewpager.worldmovies.util.PrefUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGW;
import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.makeLogTag;

/**
 * Created by aaron on 2016/9/26.
 */

public class LoginActivity extends AppCompatActivity implements LoginAndAuthHelper.Callbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int SELECT_GOOGLE_ACCOUNT_RESULT = 9001;
    private static final int SELECT_FACEBOOK_ACCOUNT_RESULT = 64206;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private RequestQueue mQueue;
    private AccessToken accessToken;
    private String HOST_NAME = Config.HOST_NAME;
    private static final String TAG = makeLogTag(LoginActivity.class);
    private AccessTokenTracker accessTokenTracker;
    private ProgressDialog mProgressDialog;
    private ProfileTracker profileTracker;
    // the LoginAndAuthHelper handles signing in to Google Play Services and OAuth
    private LoginAndAuthHelper mLoginAndAuthHelper;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
//        initToolbar();

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

        ViewPager viewpager = (ViewPager) findViewById(R.id.viewpager);
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        viewpager.setAdapter(new CustomPagerAdapter(LoginActivity.this, dataSource()));
        indicator.setViewPager(viewpager);
        viewpager.setCurrentItem(2);
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(ViewPager.LayoutParams.MATCH_PARENT, getStatusBarHeight());
        textView.setBackgroundColor(Color.TRANSPARENT);
        textView.setLayoutParams(lParams);
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.addView(textView);

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    @Override public void onBackStackChanged() {
                        int count = getSupportFragmentManager().getBackStackEntryCount();
                        ActionBar actionbar = getSupportActionBar();
                        if (actionbar != null) {
                            actionbar.setDisplayHomeAsUpEnabled(count > 0);
                            actionbar.setDisplayShowHomeEnabled(count > 0);
                        }
                    }
                });

        //----------- facebook login -----------//

        AppEventsLogger.activateApp(this);
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

        LoginButton fbButton = (LoginButton) view.findViewById(R.id.fb_login);

        fbButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!AccountUtils.enforceActiveFaceBookAccount(LoginActivity.this, SELECT_FACEBOOK_ACCOUNT_RESULT)) {
                            Log.d("0314", "EnforceActiveFaceBookAccount returned false");
                            return;
                        }
                    }
                }
        );

        //----------- facebook login -----------//

        //----------- google login -------------//

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                if (!AccountUtils.enforceActiveGoogleAccount(LoginActivity.this, mGoogleApiClient, SELECT_GOOGLE_ACCOUNT_RESULT)) {
                    Log.d("0314", "EnforceActiveGoogleAccount returned false");
                    hideProgressDialog();
                    return;
                }
            }
        });

        //----------- google login -------------//
    }

    private List<PagerObject> dataSource() {
        List<PagerObject> data = new ArrayList<PagerObject>();
        //TODO poster provider api for splash screen
        data.add(new PagerObject("http://ia.media-imdb.com/images/M/MV5BMTMxNTMwODM0NF5BMl5BanBnXkFtZTcwODAyMTk2Mw@@._V1_SX640_SY720_.jpg", "音樂x電影"));
        data.add(new PagerObject("https://s.yimg.com/vu/movies/fp/mpost/64/55/6455.jpg", "中西電影資料搜尋"));
        data.add(new PagerObject("https://s.yimg.com/vu/movies/fp/mpost/63/83/6383.jpg", "世界電影趨勢x評論"));
        data.add(new PagerObject("https://s.yimg.com/vu/movies/fp/mpost/63/90/6390.jpg", "IMDB 即時排行榜"));
        data.add(new PagerObject("https://images-na.ssl-images-amazon.com/images/M/MV5BMTUzNTc0NTAyM15BMl5BanBnXkFtZTgwMTk1ODA5OTE@._V1_SY1000_CR0,0,675,1000_AL_.jpg", "NewYork Times 電影分析"));
        return data;
    }

    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        startLoginProcess();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    // fetch height of status bar
    private int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    private FacebookCallback<LoginResult> mCallBack = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {
            showProgressDialog();
            accessToken = loginResult.getAccessToken();
            Log.d("FB", "access token got.");
            Log.d("FB", "accessToken" + ": " + accessToken.getToken());

            final GraphRequest friendsRequest = new GraphRequest( /* handle the result */
                    accessToken,
                    "/me/friends",
                    null,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            if(response.getError() == null) {
                                List<User> FriendList = new ArrayList<>();;
                                JSONObject jsonObj = response.getJSONObject();
                                JSONArray data = jsonObj.optJSONArray("data");

                                for (int i=0; i < data.length(); i++) {
                                    User user = new User();
                                    Log.d("FB", "friends complete " + i + ": " + data.optJSONObject(i));
                                    JSONObject dataobj= data.optJSONObject(i);
                                    user.name = dataobj.optString("name");
                                    user.id = dataobj.optString("id");
                                    user.link = dataobj.optString("link");
                                    user.pictureUrl = "https://graph.facebook.com/" + user.id + "/picture?type=large";
                                    FriendList.add(user);
                                }

                                PrefUtils.setCurrentFriends(FriendList, getApplicationContext());
                                User user = PrefUtils.getCurrentUser(getApplicationContext());
                                AccountUtils.setActiveAccount(getApplicationContext(), user.name);
                                onAuthSuccess(user.name, true);
                            } else {
                                Toast.makeText(LoginActivity.this,
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
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            if(response.getError() == null) {
                                User user = new User();
                                LOGD("FB", "complete " + object);
                                LOGD("FB", object.optString("name"));
                                LOGD("FB", object.optString("link"));
                                LOGD("FB", object.optString("id"));
                                LOGD("FB", object.optString("email"));
                                LOGD("FB", object.optString("gender"));

                                try {
                                    user.id = object.optString("id");
                                    user.email = object.optString("email");
                                    user.link = object.getString("link");
                                    user.name = object.optString("name");
                                    user.gender = object.optString("gender");
                                    user.accessToken = accessToken.getToken();
                                    user.pictureUrl = "https://graph.facebook.com/" + user.id + "/picture?type=large";
                                    PrefUtils.setCurrentUser(user, getApplicationContext());
                                    Bundle parameters = new Bundle();
                                    parameters.putString("fields", "id,link,name,email,picture,birthday,education");
                                    friendsRequest.setParameters(parameters);
                                    friendsRequest.executeAsync();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this,
                                        response.getError().getErrorMessage(),
                                        Toast.LENGTH_SHORT).show();
                                startActivityForVersion(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        }
                    }
            );

            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,link,name,email,gender,birthday,picture"); //items listed depend on facebook app's permission permission
            meRequest.setParameters(parameters);
            meRequest.executeAsync();
        }

        //登入取消
        @Override
        public void onCancel() {
            // App code
            LOGD("FB", "CANCEL");
        }

        //登入失敗
        @Override
        public void onError(FacebookException exception) {
            // App code
            LOGD("FB", exception.toString());
        }
    };

    private void startActivityForVersion(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(
                            LoginActivity.this).toBundle());
        }
        else {
            startActivity(intent);
        }
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
        LOGD(TAG, "onAuthSuccess, account " + accountName + ", newlyAuthenticated=" + newlyAuthenticated);
        Log.d("0314", "onAuthSuccess, account " + accountName + ", newlyAuthenticated=" + newlyAuthenticated);

        if (newlyAuthenticated) {
            LOGD(TAG, "Enabling auto sync on content provider for onreaccount " + accountName);
            SyncHelper.updateSyncInterval(this, account);
            SyncHelper.requestManualSync(account);
        }

        //TODO register api for google account

        mQueue = CustomVolleyRequestQueue.getInstance(LoginActivity.this).getRequestQueue();
        User user = PrefUtils.getCurrentUser(getApplicationContext());
        String user_name = user.name;

        try {
            user_name = URLEncoder.encode(user_name, "UTF-8");
            user_name = user_name.replaceAll(" ", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        }

        CustomJSONObjectRequest jsonRequest_q = null;
        String url = HOST_NAME + "register/"+user_name+"/"+user.id;
        jsonRequest_q = new CustomJSONObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    hideProgressDialog();
                    String result = response.getString("content");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                    overridePendingTransition( 0, R.anim.fade_out);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
            }
        });

        mQueue.add(jsonRequest_q);
//        registerGCMClient();
    }

    @Override
    public void onAuthFailure(String accountName) {
        LOGD(TAG, "Auth failed for account " + accountName);
    }

    @Override
    public void onPlusInfoLoaded(String accountName) {
        LOGD(TAG, "PlusInfoLoaded for account " + accountName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle the select {@code startActivityForResult} from
        // {@code enforceActiveGoogleAccount()} when a Google Account wasn't present on the device.
        if (requestCode == SELECT_GOOGLE_ACCOUNT_RESULT) {
            if (resultCode == RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount acct = result.getSignInAccount();
                User user = new User();
                user.id = acct.getId();
                user.email = acct.getEmail();
                user.name = acct.getDisplayName();
                user.pictureUrl = String.valueOf(acct.getPhotoUrl());
                PrefUtils.setCurrentUser(user, getApplicationContext());
                AccountUtils.setActiveAccount(this, user.name);
                onAuthSuccess(user.name, true);
//                handleSignInResult(result);
            } else {
                LOGW(TAG, "A Google Account is required to use this application.");
                // This application requires a Google Account to be selected.
                finish();
            }

            return;
        } else if (requestCode == SELECT_FACEBOOK_ACCOUNT_RESULT) {
            if (resultCode == RESULT_OK) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            } else {
                LOGW(TAG, "A facebook Account is required to use this application.");
                // This application requires a facebook Account to be selected.
                finish();
            }
        }

        if (mLoginAndAuthHelper == null || !mLoginAndAuthHelper.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void signIn() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SELECT_GOOGLE_ACCOUNT_RESULT);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            LOGD("0927", acct.getDisplayName());
            LOGD("0927", acct.getEmail());
            LOGD("0927", acct.getId());
            LOGD("0927", String.valueOf(acct.getPhotoUrl()));
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        //
                    }
                });
    }
}
