package com.github.florent37.materialviewpager.worldmovies.model;

/**
 * Created by aaron on 2016/3/21.
 */
public class Movie {
    public int id;
    public String title;

    public int getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public Movie(int id, String title) {
        this.title = title;
        this.id = id;
    }
}

