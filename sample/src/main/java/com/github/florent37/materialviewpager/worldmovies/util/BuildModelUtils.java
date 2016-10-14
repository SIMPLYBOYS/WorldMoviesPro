package com.github.florent37.materialviewpager.worldmovies.util;

import android.content.Context;

import com.github.florent37.materialviewpager.worldmovies.model.ImdbObject;
import com.github.florent37.materialviewpager.worldmovies.model.TrendsObject;
import com.github.florent37.materialviewpager.worldmovies.trends.TrendsFavoritePreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.github.florent37.materialviewpager.worldmovies.util.LogUtils.LOGD;

/**
 * Created by aaron on 2016/9/3.
 */
public class BuildModelUtils {

    private static final String REQUEST_TAG = "titleRequest";
    private static final String TAG_TITLE = "title";
    private static final String TAG_YEAR = "year";
    private static final String TAG_DETAIL_URL = "detailUrl";
    private static final String TAG_TOP = "top";
    private static final String TAG_DATA = "data";
    private static final String TAG_INFO = "mainInfo";
    private static final String TAG_STAFF = "staff";
    private static final String TAG_REVIEW = "review";
    private static final String TAG_CAST = "cast";
    private static final String TAG_POSTER_URL = "posterUrl";
    private static final String TAG_RATING = "rating";
    private static final String TAG_TOMATO = "rottentomatoes";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_DETAIL_POSTER_URL = "poster";
    private static final String TAG_SUMMERY = "summery";
    private static final String TAG_PLOT = "plot";
    private static final String TAG_GENRE = "genres";
    private static final String TAG_VOTES = "votes";
    private static final String TAG_RUNTIME = "runtime";
    private static final String TAG_METASCORE = "metascore";
    private static final String TAG_SLATE = "slate";
    private static final String TAG_RELEASE = "releaseDate";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_STORY = "story";
    private static final String TAG_TRAILER = "trailerUrl";
    private static final String TAG_GALLERY_FULL = "gallery_full";
    private static final String TAG_DELTA = "delta";
    private static TrendsFavoritePreference favor;
    private static Context context;

    public static TrendsObject buildTrendsModel(JSONArray contents, boolean byTitle, int channel) throws JSONException {
        favor = new TrendsFavoritePreference();
        JSONObject c = contents.getJSONObject(0);
        String title = c.getString(TAG_TITLE);
        int top = c.getInt(TAG_TOP);;
        String releaseDate = "";
        String mainInfo = "";
        String story = "";
        String trailerUrl ="";
        String posterUrl = "";
        String detailUrl = "";
        String country = "";

        //----- start dummy GalleryUrl ----
        JSONObject jo = new JSONObject();
        jo.put("type", "full");
        jo.put("url", "");
        JSONArray galleryFullUrl = new JSONArray();
        galleryFullUrl.put(jo);
        //----- end dummy GalleryUrl ----

        JSONArray data = new JSONArray();
        JSONArray staff = new JSONArray();
        JSONArray cast = new JSONArray();
        JSONArray review = new JSONArray();
        JSONArray gallery = new JSONArray();
        JSONObject rating = new JSONObject();
        JSONObject tomato = new JSONObject();

        if (c.has(TAG_DATA))
            data = c.getJSONArray(TAG_DATA);
        if (c.has(TAG_INFO))
            mainInfo = c.getString(TAG_INFO);
        if (c.has(TAG_STAFF))
            staff = c.getJSONArray(TAG_STAFF);
        if (c.has(TAG_CAST))
            cast = c.getJSONArray(TAG_CAST);
        if (c.has(TAG_REVIEW))
            review = c.getJSONArray(TAG_REVIEW);
        if (c.has(TAG_GALLERY_FULL))
            gallery = c.getJSONArray(TAG_GALLERY_FULL);
        if (c.has(TAG_DETAIL_URL))
            detailUrl = c.getString(TAG_DETAIL_URL);
        if (c.has(TAG_TOMATO))
            tomato = c.getJSONObject(TAG_TOMATO);

        if (c.has(TAG_COUNTRY)) {
            country = c.getString(TAG_COUNTRY);
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("data", country);
            data.put(2, jsonObj);
        }

        story = c.getString(TAG_STORY);
        posterUrl = c.getString(TAG_POSTER_URL);
        trailerUrl = c.getString(TAG_TRAILER);
        rating = c.getJSONObject(TAG_RATING);
        releaseDate = c.getString(TAG_RELEASE);
        TrendsObject item = null;
        item = new TrendsObject(title, String.valueOf(top), detailUrl, posterUrl, trailerUrl, cast.toString(), review.toString(),
                staff.toString(), data.toString(), story, mainInfo, gallery.toString(), rating.toString(), releaseDate, tomato.toString());
        item.setChannel(channel);
            /*if (checkBookmark(title))
                item.setBookmark(true);*/

        return item;
    }

    public static boolean checkBookmark(String title) {
        ArrayList list = favor.loadFavorites(context);

        for (int i=0; i<list.size(); i++) {
            if (title.compareTo((String) list.get(i)) == 0) return true;
        }

        return false;
    };

    public static ImdbObject buildImdbModel(JSONArray contents) throws JSONException {
        JSONObject jsonObj = new JSONObject();
        JSONArray data = new JSONArray();
        JSONObject c = contents.getJSONObject(0);
        JSONObject d = null;
        int top = 0;
        String title = c.getString(TAG_TITLE);
        String year = "";
        String posterUrl = "http://www.imdb.com/title/tt1355631/mediaviewer/rm3798736128?ref_=tt_ov_i";
        String delta = "0";
        String detailUrl = "";

        if (c.has("detailContent")) {
            d = c.getJSONObject("detailContent");
        }

        JSONObject jo = new JSONObject();
        //----- start dummy GalleryUrl ----
        jo.put("type", "full");
        jo.put("url", "");
        JSONArray galleryFullUrl = new JSONArray();
        JSONArray cast = new JSONArray();
        galleryFullUrl.put(jo);
        //----- end dummy GalleryUrl ----

        if (c.has(TAG_TOP)) {
            top = c.getInt(TAG_TOP);
        }

        if (c.has(TAG_DATA)) {
            data = c.getJSONArray(TAG_DATA);
            jsonObj = (JSONObject) data.get(1);
            LOGD("0811", String.valueOf(data));
        }

        year = c.has(TAG_YEAR) ? c.getString(TAG_YEAR) : c.getString(TAG_RELEASE);

        if (c.has(TAG_DETAIL_URL))
            detailUrl = c.getString(TAG_DETAIL_URL);

        String description= c.getString(TAG_DESCRIPTION);
        String rating = c.getString(TAG_RATING);

        if (c.has(TAG_POSTER_URL)) {
            posterUrl = c.getString(TAG_POSTER_URL);
        }

        if (c.has(TAG_CAST)) {
            cast = c.getJSONArray(TAG_CAST);
        }

        String plot = c.has(TAG_PLOT) ? c.getString(TAG_PLOT) : c.getString("story");
        String genre = c.has(TAG_GENRE) ? c.getString(TAG_GENRE) : "";
        String votes = c.has(TAG_VOTES) ? c.getString(TAG_VOTES) : "";
        String runTime = c.has(TAG_RUNTIME) ? c.getString(TAG_RUNTIME) : "";
        String metaScore = c.has(TAG_METASCORE) ? c.getString(TAG_METASCORE) : "";

        if (runTime.compareTo("") == 0) {
            jsonObj = new JSONObject();
            jsonObj = (JSONObject) data.get(4);
            runTime = jsonObj.getString("data");
        }

        if (genre.compareTo("") == 0)
            genre = c.getString("genre");

        String summery = d != null ? d.getString(TAG_SUMMERY) : c.getString("story");
        String country = d != null ? d.getString(TAG_COUNTRY) : c.getString(TAG_COUNTRY);

        if (c.has(TAG_GALLERY_FULL)) {
            galleryFullUrl = c.getJSONArray(TAG_GALLERY_FULL);
        }

        String trailerUrl;
        String slate;

        if (c.has(TAG_TRAILER))
            trailerUrl = c.getString(TAG_TRAILER);
        else
            trailerUrl = "N/A";

        if (d != null)
            slate = d.has(TAG_SLATE) ? d.getString(TAG_SLATE) : "N/A";
        else
            slate = "N/A";

        ImdbObject movie = new ImdbObject(title, String.valueOf(top), year, description,
                rating, posterUrl, slate, summery, plot,
                genre, votes, runTime, metaScore, delta, country,
                trailerUrl, cast.toString(), galleryFullUrl.toString(), detailUrl);

        return movie;
    }
}
