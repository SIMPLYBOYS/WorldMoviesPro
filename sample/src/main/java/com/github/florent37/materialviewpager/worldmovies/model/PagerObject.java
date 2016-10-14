package com.github.florent37.materialviewpager.worldmovies.model;

/**
 * Created by aaron on 2016/9/29.
 */

public class PagerObject {
    private int imageId;
    private String imageName;
    private String imageUrl;
    public PagerObject(String imageUrl, String imageName) {
        this.imageUrl = imageUrl;
        this.imageName = imageName;
    }
    public int getImageId() {
        return imageId;
    }
    public String getImageUrl() { return imageUrl;}
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public String getImageName() {
        return imageName;
    }
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
