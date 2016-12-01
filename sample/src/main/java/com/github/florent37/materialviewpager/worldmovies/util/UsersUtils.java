package com.github.florent37.materialviewpager.worldmovies.util;

import android.content.Context;
import android.util.Log;

import com.github.florent37.materialviewpager.worldmovies.model.User;
import com.google.gson.Gson;

import java.util.List;

public class UsersUtils {

    public static void setCurrentUser(User currentUser, Context context) {
        Log.d("1115", "setCurrentUser");
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(context, "user_prefs", 0);
        complexPreferences.putObject("current_user_value", currentUser);
        complexPreferences.commit();
    }

    public static User getCurrentUser(Context context) {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(context, "user_prefs", 0);
        User currentUser = complexPreferences.getObject("current_user_value", User.class);
        return currentUser;
    }

    public static void clearCurrentUser( Context context) {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(context, "user_prefs", 0);
        complexPreferences.clearObject();
        complexPreferences.commit();
    }

    public static void setCurrentFriends(List<User> FriendList, Context context) {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(context, "friends_prefs", 0);
        complexPreferences.putObject("current_friends_list", FriendList);
        complexPreferences.commit();
    }

    public static void clearCurrentFriends(Context context) {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(context, "friends_prefs", 0);
        complexPreferences.clearObject();
        complexPreferences.commit();
    }

    public static String getCurrentFriends(Context context) {
        ComplexPreferences complexPreferences = ComplexPreferences.getComplexPreferences(context, "friends_prefs", 0);
        List<User> FriendList = complexPreferences.getObject("current_friends_list", List.class);
        Gson gson = new Gson();
        String json = gson.toJson(FriendList, List.class);
        return json;
    }
}
