package com.github.florent37.materialviewpager.worldmovies.nytimes;

import java.io.Serializable;

/**
 * Created by aaron on 2016/3/21.
 */
public class Movie implements Serializable {
    public String headline;
    public String description;
    public String summary_short;
    public String link;
    public String picUrl;
    public String editor;
    public String date;
    public boolean bookmark;
    public int channel;

    public String getHeadline() {
        return headline;
    }

    public String getSummary_short() { return summary_short;}

    public String getLink() { return link;}

    public String getPicUrl() { return picUrl;}

    public String getDescription() {
        return description;
    }

    public String getPublication_date() {return description; }

    public String getDate() { return date; }

    public String getEditor() { return editor; }

    public void setBookmark(boolean bookmark) {
        this.bookmark = bookmark;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getChannel() {
        return channel;
    }

    public Boolean getBookmark() { return this.bookmark;}

    public Movie(String headline, String description, String summary_short, String link, String picUrl, String editor, String date) {
        this.headline = headline;
        this.description = description;
        this.summary_short = summary_short;
        this.link = link;
        this.editor = editor;
        this.date = date;
        this.picUrl = picUrl;
        this.bookmark = false;
        this.channel = 0;
    }
}

