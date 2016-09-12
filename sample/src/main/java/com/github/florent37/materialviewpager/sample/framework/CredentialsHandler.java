package com.github.florent37.materialviewpager.sample.framework;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import static com.github.florent37.materialviewpager.sample.util.LogUtils.LOGD;

public class CredentialsHandler {

    private static final String ACCESS_TOKEN_NAME = "webapi.credentials.access_token";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String ACCESS_CODE = "access_code";
    private static final String EXPIRES_AT = "expires_at";

    public static void setToken(Context context, String token, long expiresIn, TimeUnit unit) {
        Context appContext = context.getApplicationContext();

        long now = System.currentTimeMillis();
        long expiresAt = now + unit.toMillis(expiresIn);

        SharedPreferences sharedPref = getSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACCESS_TOKEN, token);
        editor.putLong(EXPIRES_AT, expiresAt);
        editor.apply();
    }

    public static void setRefreshToken(Context context, String token) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(REFRESH_TOKEN, token);
        editor.apply();
    }

    public static void setCode(Context context, String code) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ACCESS_CODE, code);
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context appContext) {
        return appContext.getSharedPreferences(ACCESS_TOKEN_NAME, Context.MODE_PRIVATE);
    }

    public static String getToken(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);

        String token = sharedPref.getString(ACCESS_TOKEN, null);
        long expiresAt = sharedPref.getLong(EXPIRES_AT, 0L);

        if (token == null || expiresAt < System.currentTimeMillis()) {
            LOGD("0827", token + " " + expiresAt +" " + System.currentTimeMillis());
            return null;
        }

        return token;
    }

    public static String getRefreshToken(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);

        String token = sharedPref.getString(REFRESH_TOKEN, null);

        if (token == null)
            return null;

        return token;
    }

    public static String getCode(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);

        String code = sharedPref.getString(ACCESS_CODE, null);

        if (code == null)
            return null;

        return code;
    }

    public static void clearToken(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);
        sharedPref.edit().remove(ACCESS_TOKEN).commit();
    }

    public static void clearCode(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);
        sharedPref.edit().remove(ACCESS_CODE).commit();
    }

    public static void clearRefreshToken(Context context) {
        Context appContext = context.getApplicationContext();
        SharedPreferences sharedPref = getSharedPreferences(appContext);
        sharedPref.edit().remove(REFRESH_TOKEN).commit();
    }
}
