package com.streamhash.streamview.model;

import java.io.Serializable;

/**
 * Created by codegama on 21/9/17.
 */

public class Category implements Serializable {

    private int id;
    private String title;
    private String thumbnailUrl;

    public Category() {

    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
