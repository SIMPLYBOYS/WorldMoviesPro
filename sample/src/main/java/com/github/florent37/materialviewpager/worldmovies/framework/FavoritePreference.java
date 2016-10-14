package com.github.florent37.materialviewpager.worldmovies.framework;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by aaron on 2016/8/15.
 */
public class FavoritePreference {
    public static final String PREFS_NAME = "APP";
    public static final String FAVORITES = "Favorite";
    public FavoritePreference() {
        super();
    }
    public void storeFavorites(Context context, List favorites) {
// used for store arrayList in json format
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);
        editor.putString(FAVORITES, jsonFavorites);
        editor.commit();
    }
    public ArrayList loadFavorites(Context context) {
// used for retrieving arraylist from json formatted string
        SharedPreferences settings;
        List favorites;
        if (context == null)
            return null;
        settings = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            String [] favoriteItems = gson.fromJson(jsonFavorites, String[].class);
            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList(favorites);
        } else
            return null;
        return (ArrayList) favorites;
    }
    public void addFavorite(Context context, String favorite) {
        List favoriteList = loadFavorites(context);
        if (favoriteList == null)
            favoriteList = new ArrayList();
        favoriteList.add(favorite);
        storeFavorites(context, favoriteList);
    }
    public void removeFavorite(Context context, String favor) {
        ArrayList favorites = loadFavorites(context);
        if (favorites != null) {
            favorites.remove(favor);
            storeFavorites(context, favorites);
        }
    }
}

