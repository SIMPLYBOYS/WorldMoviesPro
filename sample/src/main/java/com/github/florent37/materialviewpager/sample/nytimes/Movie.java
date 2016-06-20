package com.github.florent37.materialviewpager.sample.nytimes;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by aaron on 2016/3/21.
 */
public class Movie implements Serializable {
    public String headline;
    public String publication_date;
    public String summary_short;
    public String link;
    public String picUrl;

    public String getHeadline(){
        return headline;
    }

    public String getSummary_short() { return summary_short;}

    public String getLink() { Log.d("0612", link); return link;}

    public String getPicUrl() { return picUrl;}

    public String getPublication_date() {
        return publication_date;
    }

    public Movie(String headline, String publication_date, String summary_short, String link, String picUrl) {
        this.headline = headline;
        this.publication_date = publication_date;
        this.summary_short = summary_short;
        this.link = link;
        this.picUrl = picUrl;
    }
}

