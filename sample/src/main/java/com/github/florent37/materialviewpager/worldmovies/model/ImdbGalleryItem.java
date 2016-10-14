package com.github.florent37.materialviewpager.worldmovies.model;

/**
 * Created by aaron on 2016/5/2.
 */
public class ImdbGalleryItem {
    private String imageUrl;

    public ImdbGalleryItem(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
