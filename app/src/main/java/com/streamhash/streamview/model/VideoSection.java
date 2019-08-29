package com.streamhash.streamview.model;

import java.util.ArrayList;

public class VideoSection {
    private String title;
    private String seeAllUrl;
    private String urlType;
    private String urlPageId;
    private int viewType;
    private ArrayList<Video> videos;

    public String getUrlType() {
        return urlType;
    }

    public void setUrlType(String urlType) {
        this.urlType = urlType;
    }

    public String getUrlPageId() {
        return urlPageId;
    }

    public void setUrlPageId(String urlPageId) {
        this.urlPageId = urlPageId;
    }

    public VideoSection(String title, String seeAllUrl, String urlType, String urlPageId, int viewType, ArrayList<Video> videos) {
        this.title = title;
        this.seeAllUrl = seeAllUrl;
        this.viewType = viewType;
        this.urlType = urlType;
        this.urlPageId = urlPageId;
        this.videos = videos;
    }

    public String getSeeAllUrl() {
        return seeAllUrl;
    }

    public void setSeeAllUrl(String seeAllUrl) {
        this.seeAllUrl = seeAllUrl;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }
}

