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
    private String ratting;
    private String posterUrl;
    private String slate;
    private String summery;
    private String plot;
    private String genre;
    private String votes;
    private String runTime;
    private String metaScore;
    private String delta;
    private String country;
    private String detailPosterUrl;
    private String trailerUrl;
    private String galleryUrl;

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

    public ImdbObject(String title, String top, String year, String description, String ratting,
                      String posterUrl, String slate, String summery, String plot, String genre,
                      String votes, String runTime, String metaScore, String delta, String country, String trailerUrl, String galleryUrl) {
        this.title = title;
        this.top = top;
        this.description = description;
        this.ratting = ratting;
        this.year = year;
        this.detailUrl = detailUrl;
        this.ratting = ratting;
        this.posterUrl = posterUrl;
        this.slate = slate;
        this.summery = summery;
        this.plot = plot;
        this.genre = genre;
        this.votes = votes;
        this.runTime = runTime;
        this.metaScore = metaScore;
        this.country = country;
        this.delta = delta;
        this.detailPosterUrl = detailPosterUrl;
        this.trailerUrl = trailerUrl;
        this.galleryUrl = galleryUrl;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getTop() {
        return top;
    }

    public String getRatting() {
        return ratting;
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

    public String getDetailPosterUrl() {
        return detailPosterUrl;
    }

    public void setPosterUrl(String imageUrl) {
        this.posterUrl = posterUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}