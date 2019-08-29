package com.streamhash.streamview.model;

import java.io.Serializable;

/**
 * Created by codegama on 16/10/17.
 */

public class Cast implements Serializable {

    private int id;
    private String name;
    private String desc;
    private String type;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Cast(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Cast() {
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

}
