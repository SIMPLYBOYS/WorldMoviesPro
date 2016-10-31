package com.github.florent37.materialviewpager.worldmovies.settings;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.login.LoginManager;
import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.framework.CredentialsHandler;
import com.github.florent37.materialviewpager.worldmovies.framework.SpinnerPreference;
import com.github.florent37.materialviewpager.worldmovies.http.CustomJSONObjectRequest;
import com.github.florent37.materialviewpager.worldmovies.http.CustomVolleyRequestQueue;
import com.github.florent37.materialviewpager.worldmovies.login.LoginActivity;
import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.github.florent37.materialviewpager.worldmovies.service.QuickstartPreferences;
import com.github.florent37.materialviewpager.worldmovies.service.RegistrationIntentService;
import com.github.florent37.materialviewpager.worldmovies.service.UnRegistrationIntentService;
import com.github.florent37.materialviewpager.worldmovies.ui.BaseActivity;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.DrawShadowFrameLayout;
import com.github.florent37.materialviewpager.worldmovies.util.AccountUtils;
import com.github.florent37.materialviewpager.worldmovies.util.UIUtils;
import com.github.florent37.materialviewpager.worldmovies.util.UsersUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Spotify;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/8/25.
 */
public class SettingsActivity extends BaseActivity {
    private Toolbar toolbar;
    private static final int REQUEST_CODE = 1337;
    private String TAG = SettingsFragment.class.getSimpleName();
    public static String spotiryToken;
    private RequestQueue mQueue;
    private Context mContext;

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_SETTINGS;
    }

    @Override
    protected void onActionBarAutoShowOrHide(boolean shown) {
        super.onActionBarAutoShowOrHide(shown);
        DrawShadowFrameLayout frame = (DrawShadowFrameLayout) findViewById(R.id.main_content);
        frame.setShadowVisible(shown, shown);
        toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        toolbar.setTitle(R.string.description_genre);
        toolbar.setTitleTextColor(Color.BLACK);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        setSupportActionBar(toolbar);
        registerHideableHeaderView(findViewById(R.id.headerbar));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Translucent navigation bar
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        String[] list = getResources().getStringArray(R.array.month);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, list);
        setContentView(R.layout.activity_setting);
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            LOGD(TAG, "response " + response.getType());
            switch (response.getType()) {
                case CODE:
                    LOGD(TAG, "Got code: " + response.getCode());
                    CredentialsHandler.setCode(this, response.getCode());
                    mQueue = CustomVolleyRequestQueue.getInstance(this).getRequestQueue();
                    CustomJSONObjectRequest jsonRequest_q = null;
                    String code = CredentialsHandler.getCode(this);
                    String url = Config.HOST_NAME+"access_refresh_token?code="+code;

                    jsonRequest_q = new CustomJSONObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                long expires_in = response.getLong("expires_in");
                                String token = response.getString("access_token");
                                String refresh_token = response.getString("refresh_token");
                                CredentialsHandler.setToken(mContext, token, expires_in, TimeUnit.SECONDS);
                                CredentialsHandler.setRefreshToken(mContext, refresh_token);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            Toast.makeText(mContext, "Remote Server connect fail!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mQueue.add(jsonRequest_q);
                    break;
                // Response was successful and contains auth token
                case TOKEN:
                    LOGD(TAG, "Got token: " + response.getAccessToken());
                    spotiryToken = response.getAccessToken();
                    CredentialsHandler.setToken(this, response.getAccessToken(), response.getExpiresIn(), TimeUnit.SECONDS);
//                        startMainActivity(response.getAccessToken());
                    break;
                // Auth flow returned an error
                case ERROR:
                    LOGD(TAG, "Auth error: " + response.getError());
                    break;
                // Most likely auth flow was cancelled
                default:
                    LOGD(TAG, "Auth result: " + response.getType());
            }
        }
    }

    /**
     * The Fragment is added via the R.layout.activity_setting layout xml.
     */
    public static class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener  {
        private String TAG = SettingsFragment.class.getSimpleName();
        //private SettingActivity mActivity;
        private Setting mSetting;
        private SwitchPreference mNotificationType, mSpotify, mRepeat, mBackgroundPlay, mPlayBySpotify;
        private SpinnerPreference spinner;
        private Preference mLogout;
        private Activity activity;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_prefs);
            activity = getActivity();
            mSetting = Setting.getInstance();
            mSpotify = (SwitchPreference) findPreference(Setting.LINK_ACCOUNT);
            spinner = (SpinnerPreference) findPreference(Setting.COUNTRY_SPINNER);
            mNotificationType = (SwitchPreference) findPreference(Setting.NOTIFICATION_MODEL);
            mPlayBySpotify = (SwitchPreference) findPreference(Setting.PLAY_BY_SPOTIFY);
            mRepeat = (SwitchPreference) findPreference(Setting.REPEAT);
            mBackgroundPlay = (SwitchPreference) findPreference(Setting.BACKGROUND_PLAY);
            mLogout = findPreference(Setting.LOGOUT);
            mLogout.setSummary("登出");
            mLogout.setOnPreferenceClickListener(this);
            mSpotify.setIcon(R.drawable.spotify);
            mSpotify.setSummary("Spotify");
            mSpotify.setOnPreferenceClickListener(this);
            spinner.setTitle("預設國家");
            spinner.setSummary("Choose default country for searching");
            mNotificationType.setOnPreferenceClickListener(this);
            mNotificationType.setSummary("接收推播");
            mPlayBySpotify.setOnPreferenceClickListener(this);
            mPlayBySpotify.setSummary("" + "Play by Spotify");
            mRepeat.setOnPreferenceClickListener(this);
            mRepeat.setSummary("自動重播");
            mBackgroundPlay.setOnPreferenceClickListener(this);
            mBackgroundPlay.setSummary("背景播放");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            boolean gcmToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
            spotiryToken = CredentialsHandler.getToken(getActivity());

            if (spotiryToken == null)
                mSpotify.setChecked(false);
            else
                mSpotify.setChecked(true);

            if (gcmToken)
                mNotificationType.setChecked(true);
            else
                mNotificationType.setChecked(false);

        }

        private void setContentTopClearance(int clearance) {
            if (getView() != null) {
                getView().setPadding(getView().getPaddingLeft(), clearance,
                        getView().getPaddingRight(), getView().getPaddingBottom());
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (mSpotify == preference) {
                if (spotiryToken != null) {
                    CredentialsHandler.clearToken(getActivity());
                    spotiryToken = null;
                } else {
                    final AuthenticationRequest request = new AuthenticationRequest.Builder(Config.CLIENT_ID, AuthenticationResponse.Type.CODE, Config.REDIRECT_URI)
                            .setScopes(new String[]{"user-read-private", "streaming"})
                            .setShowDialog(true)
                            .build();
                    AuthenticationClient.openLoginActivity(getActivity(), REQUEST_CODE, request);
                }
//                showUpdateDialog();
                return true;
            } else if (mNotificationType == preference) {
                mNotificationType.setChecked(mNotificationType.isChecked());
                if (mNotificationType.isChecked()) {
                    Activity activity = getActivity();
                    Intent intent = new Intent(activity, RegistrationIntentService.class);
                    activity.startService(intent);
                } else {
                    Activity activity = getActivity();
                    Intent intent = new Intent(activity, UnRegistrationIntentService.class);
                    activity.startService(intent);
                }
                mSetting.setNotificationModel(mNotificationType.isChecked() ? Notification.FLAG_AUTO_CANCEL : Notification.FLAG_ONGOING_EVENT);
                Log.i(TAG, mSetting.getAutoUpdate() + "");
                return true;
            } else if (mRepeat == preference) {
                mRepeat.setChecked(mRepeat.isChecked());
                mSetting.setReat(mRepeat.isChecked());
                //TODO Toast : "only premium spotify user can do that"
                return true;
            } else if (mBackgroundPlay == preference) {
                mBackgroundPlay.setChecked(mBackgroundPlay.isChecked());
                mSetting.setBackgroundPlay(mBackgroundPlay.isChecked());
                return true;
            } else if (mPlayBySpotify == preference) {
                mPlayBySpotify.setChecked(mPlayBySpotify.isChecked());
                mSetting.setPlayBySpotify(mPlayBySpotify.isChecked());
            } else if (mLogout == preference) {
                UsersUtils.clearCurrentUser(activity);
                String friends = UsersUtils.getCurrentFriends(activity);
                Gson gson = new Gson();
                List<User> friendslist = gson.fromJson(friends, new TypeToken<List<User>>(){}.getType());
                UsersUtils.clearCurrentFriends(activity);
                AccountUtils.clearActiveAccount(activity);
                LoginManager.getInstance().logOut();
                Intent i=  new Intent(activity, LoginActivity.class);
                startActivityForVersion(i);
                activity.finish();
            } else if (spinner == preference) {
                Toast.makeText(getActivity(), "Coming soon!", Toast.LENGTH_SHORT).show();
            }
            return false;
        }

        private void startActivityForVersion(Intent intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(
                                activity).toBundle());
            }
            else {
                startActivity(intent);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            // configure the fragment's top clearance to take our overlaid controls (Action Bar
            // and spinner box) into account.
            int actionBarSize = UIUtils.calculateActionBarSize(getActivity());
            DrawShadowFrameLayout drawShadowFrameLayout =
                    (DrawShadowFrameLayout) getActivity().findViewById(R.id.main_content);
            if (drawShadowFrameLayout != null) {
                drawShadowFrameLayout.setShadowTopOffset(actionBarSize);
            }
            setContentTopClearance(actionBarSize);
        }


        private void showUpdateDialog() {
            //将 SeekBar 放入 Dialog 的方案 http://stackoverflow.com/questions/7184104/how-do-i-put-a-seek-bar-in-an-alert-dialog
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogLayout = inflater.inflate(R.layout.update_dialog, (ViewGroup) getActivity().findViewById(R.id.dialog_root));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(dialogLayout);
            final AlertDialog alertDialog = builder.create();
            final SeekBar mSeekBar = (SeekBar) dialogLayout.findViewById(R.id.time_seekbar);
            final TextView tvShowHour = (TextView) dialogLayout.findViewById(R.id.tv_showhour);
            TextView tvDone = (TextView) dialogLayout.findViewById(R.id.done);
            mSeekBar.setMax(24);
            mSeekBar.setProgress(mSetting.getAutoUpdate());
            tvShowHour.setText(String.format("每%s小时",mSeekBar.getProgress()));
            alertDialog.show();

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvShowHour.setText(String.format("每%s小时",mSeekBar.getProgress()));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            tvDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSetting.setAutoUpdate(mSeekBar.getProgress());
                    mSpotify.setSummary(mSetting.getAutoUpdate() == 0 ? "禁止刷新" : "每" + mSetting.getAutoUpdate() + "小时更新");
                    //需要再调用一次才能生效设置 不会重复的执行onCreate()， 而是会调用onStart()和onStartCommand()。
                    //getActivity().startService(new Intent(getActivity(), AutoUpdateService.class));
                    alertDialog.dismiss();

                }
            });
        }
    }

}
