package com.streamhash.streamview.model;

import java.io.Serializable;

/**
 * Created by codegama on 16/10/17.
 */

public class SubProfile implements Serializable {

    private int id;
    private String name;
    private String image;
    private boolean last;

    public SubProfile(int id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    @Override
    public String toString() {
        return "SubProfile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", last=" + last +
                '}';
    }
}
