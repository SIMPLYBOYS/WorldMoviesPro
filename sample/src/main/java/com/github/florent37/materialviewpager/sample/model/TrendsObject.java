package com.github.florent37.materialviewpager.sample.model;

import org.json.JSONArray;

import java.io.Serializable;

/**
 * Created by aaron on 2016/6/17.
 */
public class TrendsObject implements Serializable {
    private String title;
    private String top;
    private String detailUrl;
    private String posterUrl;
    private String trailerUrl;
    private String galleryUrl;
    private transient JSONArray cast;
    private transient JSONArray staff;
    private transient JSONArray data;
    private String story;
    private String mainInfo;

    public class GalleryItem {
        private String type;
        private String url;

        public String getType() {
            return type;
        }

        public String getUrl() {
            return url;
        }
    }

    public TrendsObject(String title, String top, String detailUrl, String posterUrl, String trailerUrl,
                        JSONArray cast, JSONArray staff, JSONArray data, String story, String mainInfo, String galleryUrl) {
        this.title = title;
        this.top = top;
        this.detailUrl = detailUrl;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.detailUrl = detailUrl;
        this.galleryUrl = galleryUrl;
        this.cast = cast;
        this.staff = staff;
        this.data = data;
        this.story = story;
        this.mainInfo = mainInfo;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public String getGalleryUrl() { return galleryUrl; }

    public String getMainInfo() { return mainInfo; }

    public JSONArray getCast() { return cast;}

    public JSONArray getStaff() { return staff; }

    public String getStory() { return story; }

    public JSONArray getData() { return data; }

    public String getTitle() {
        return title;
    }

    public String getTop() {
        return top;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String imageUrl) {
        this.posterUrl = posterUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}


