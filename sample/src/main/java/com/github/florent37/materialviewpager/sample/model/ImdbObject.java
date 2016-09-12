package com.github.florent37.materialviewpager.sample.model;

import java.io.Serializable;

/**
 * Created by aaron on 2016/4/4.
 */

public class ImdbObject implements Serializable {
    private String title;
    private String top;
    private String year;
    private String detailUrl;
    private String description;
    private String rating;
    private String posterUrl;
    private String cast;
    private String review;
    private String slate;
    private String summery;
    private String plot;
    private String genre;
    private String votes;
    private String runTime;
    private String metaScore;
    private String delta;
    private String type;
    private String country;
    private String detailPosterUrl;
    private String trailerUrl;
    private String galleryUrl;
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

    public class RatingItem {
        private String score;
        private String votes;
        public String getScore() { return score;}
        public String getVotes() { return votes;}
    }

    public ImdbObject(String title, String top, String year, String description, String rating,
                      String posterUrl, String slate, String summery, String plot, String genre,
                      String votes, String runTime, String metaScore, String delta, String country, String trailerUrl, String cast, String galleryUrl, String detailUrl) {
        this.title = title;
        this.top = top;
        this.description = description;
        this.year = year;
        this.detailUrl = detailUrl;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.slate = slate;
        this.summery = summery;
        this.plot = plot;
        this.genre = genre;
        this.votes = votes;
        this.runTime = runTime;
        this.metaScore = metaScore;
        this.country = country;
        this.cast = cast;
        this.delta = delta;
        this.detailPosterUrl = detailPosterUrl;
        this.trailerUrl = trailerUrl;
        this.galleryUrl = galleryUrl;
        this.bookmark = false;
    }

    public String getCast() { return cast;}

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getTop() {
        return top;
    }

    public String getRating() {
        return rating;
    }

    public String getYear() {
        return year;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getGalleryUrl() {
        return galleryUrl;
    }

    public String getSlate() {
        return slate;
    }

    public String getSummery() {
        return summery;
    }

    public String getPlot() {return plot;}

    public String getGenre() {
        String[] items = genre.replaceAll("\\[", "").replaceAll("\\]","").split(",");
        String result = "";
        for (int i = 0; i<items.length; i++){
            items[i] = items[i].replaceAll("\"","");
            if (i+1 < items.length)
                result += items[i] + ", ";
            else
                result += items[i];
        }
        return result;}

    public String getVotes() {return votes;}

    public String getRunTime() {return runTime;}

    public String getMetaScore() {return metaScore;}

    public int getDelta() {
        if (delta.equals("0"))
            return 0;
        else
            return Integer.parseInt(delta);
    }

    public String getCountry() {
        return country;
    }

    public String getReview() { return review; }

    public String getType() { return type; }

    public String getDetailPosterUrl() {
        return detailPosterUrl;
    }

    public void setPosterUrl(String imageUrl) {
        this.posterUrl = posterUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType (String type) { this.type = type; }
}