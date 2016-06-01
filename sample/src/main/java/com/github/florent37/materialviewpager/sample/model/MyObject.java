package com.github.florent37.materialviewpager.sample.model;

/**
 * Created by aaron on 2016/4/5.
 */
public class MyObject {
    private String text;
    private String imageUrl;
    private String description;

    public MyObject(String location, String description, String imageUrl) {
        this.text = location;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    public String getLocation() {
        return text;
    }

    public void setLocation(String text) {
        this.text = text;
    }

    public String getDescription(){
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}