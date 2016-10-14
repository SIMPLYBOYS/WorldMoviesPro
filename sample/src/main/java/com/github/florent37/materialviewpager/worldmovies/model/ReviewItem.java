package com.github.florent37.materialviewpager.worldmovies.model;

/**
 * Created by aaron on 2016/7/25.
 */

public class ReviewItem {
    private String name;
    private String avatar;
    private String topic;
    private String text;
    private float point;
    private String date;

    public ReviewItem (String avatar, String name, String date, String topic, String text, float point) {
        this.avatar = avatar;
        this.name = name;
        this.date = date;
        this.topic = topic;
        this.text = text;
        this.point = point;
    }

    public String getAvatar() { return avatar;}

    public String getViewer() { return name;}

    public String getDate() { return date;}

    public String getTopic() { return topic;}

    public String getContent() { return text;}

    public float getPoint() { return point;}

}