package com.github.florent37.materialviewpager.worldmovies.settings;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;

import com.github.florent37.materialviewpager.worldmovies.app.AppApplication;

public class Setting {

    public static final String AUTO_UPDATE = "change_update_time";
    public static final String LINK_ACCOUNT = "connect_account";
    public static final String PLAY_BY_SPOTIFY = "play_by_spotify";
    public static final String CITY_NAME = "城市";
    public static final String HOUR = "current_hour";
    public static final String NOTIFICATION_MODEL = "notification_model";
    public static final String REPEAT = "repeat";
    public static final String BACKGROUND_PLAY = "background_play";
    public static int ONE_HOUR = 1000 * 60 * 60;
    private static Setting sInstance;
    private SharedPreferences mPrefs;

    public static Setting getInstance() {
        if (sInstance == null) {
            sInstance = new Setting(AppApplication.getInstance());
        }
        return sInstance;
    }

    private Setting(Context context) {
        mPrefs = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        //mPrefs.edit().putInt(CHANGE_ICONS, 1).apply();
    }

    public Setting putInt(String key, int value) {
        mPrefs.edit().putInt(key, value).apply();
        return this;
    }

    public int getInt(String key, int defValue) {
        return mPrefs.getInt(key, defValue);
    }

    public Setting putString(String key, String value) {
        mPrefs.edit().putString(key, value).apply();
        return this;
    }

    public String getString(String key, String defValue) {
        return mPrefs.getString(key, defValue);
    }

    public void setCurrentHour(int h){
        mPrefs.edit().putInt(HOUR,h).apply();
    }
    public int getCurrentHour(){
        return mPrefs.getInt(HOUR,0);
    }

    public void setAutoUpdate(int t) {
        mPrefs.edit().putInt(AUTO_UPDATE, t).apply();
    }

    public int getAutoUpdate() {
        return mPrefs.getInt(AUTO_UPDATE, 3);
    }

    public void setCityName(String name) {
        mPrefs.edit().putString(CITY_NAME, name).apply();
    }

    public String getCityName() {return mPrefs.getString(CITY_NAME, "台北");}

    public void setNotificationModel(int t) {
        mPrefs.edit().putInt(NOTIFICATION_MODEL, t).apply();
    }
    public int getNotificationModel() {
        return mPrefs.getInt(NOTIFICATION_MODEL, Notification.FLAG_AUTO_CANCEL);
    }

    public void setPlayBySpotify(boolean choice) {
        mPrefs.edit().putBoolean(PLAY_BY_SPOTIFY, choice).apply();
    }

    public boolean getPlayBySpotify() {
        return mPrefs.getBoolean(PLAY_BY_SPOTIFY, false);
    }

    public void setReat(boolean choice) {
        mPrefs.edit().putBoolean(REPEAT, choice).apply();
    }

    public boolean getReat() {
        return mPrefs.getBoolean(REPEAT, false);
    }

    public void setBackgroundPlay(boolean choice) {
        mPrefs.edit().putBoolean(BACKGROUND_PLAY, choice).apply();
    }

    public boolean getBackgroundPlay() {
        return mPrefs.getBoolean(BACKGROUND_PLAY, false);
    }
}
