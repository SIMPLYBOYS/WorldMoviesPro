package com.github.florent37.materialviewpager.worldmovies.model;

import java.io.Serializable;

/**
 * Created by aaron on 2016/6/17.
 */
public class TrendsObject extends Object implements Serializable {
    private String title;
    private String top;
    private String detailUrl;
    private String posterUrl;
    private String trailerUrl;
    private String galleryUrl;
    private String cast;
    private String review;
    private String staff;
    private String data;
    private String story;
    private String mainInfo;
    private String rating;
    private String tomato;
    private String releaseDate;
    private int channel;
    private boolean bookmark;

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

    public class DataItem {
        private String data;
        public String getData() {
            return data;
        }
    }

    public class RatingItem {
        private String score;
        private String votes;
        public String getScore() { return score;}
        public String getVotes() { return votes;}
    }

    public class StaffItem {
        private String staff;
        private String url;

        public String getStaff() {
            return staff;
        }

        public String getUrl() {
            return url;
        }
    }

    public class TomatoItem {
        private String critics_score;
        private String audience_score;
        private String reviews;

        public String getCritics_score() { return critics_score; }
        public String getAudience_score() { return audience_score; }
    }

    public class CastItem {
        private String cast;
        private String link;
        private String as;
        private String avatar;

        public String getCast() {
            return cast;
        }

        public String getAs() {
            return as;
        }

        public String getAvatar() {
            return avatar;
        }

        public String getUrl() {
            return link;
        }
    }

    public void setBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }

    public Boolean getBookmark() { return this.bookmark;}

    public TrendsObject(String title, String top, String detailUrl, String posterUrl, String trailerUrl,
                        String cast, String review, String staff, String data, String story, String mainInfo,
                        String galleryUrl, String rating, String releaseDate, String tomato) {
        this.title = title;
        this.top = top;
        this.detailUrl = detailUrl;
        this.posterUrl = posterUrl;
        this.trailerUrl = trailerUrl;
        this.detailUrl = detailUrl;
        this.galleryUrl = galleryUrl;
        this.cast = cast;
        this.review = review;
        this.staff = staff;
        this.data = data;
        this.story = story;
        this.mainInfo = mainInfo;
        this.rating = rating;
        this.tomato = tomato;
        this.releaseDate = releaseDate;
        this.bookmark = false;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public String getGalleryUrl() { return galleryUrl; }

    public String getMainInfo() { return mainInfo; }

    public String getCast() { return cast;}

    public String getStaff() { return staff; }

    public String getStory() { return story; }

    public String getReview() { return review; }

    public String getData() { return data; }

    public String getRating() { return rating; }

    public String getTomato() { return tomato; }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() { return releaseDate; }

    public String getTop() {
        return top;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public int getChannel() { return channel; }

    public void setPosterUrl(String imageUrl) {
        this.posterUrl = posterUrl;
    }

    public void setChannel(int channel) { this.channel = channel;}

    public void setTitle(String title) {
        this.title = title;
    }
}


