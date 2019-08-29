package com.streamhash.streamview.model;

public class NotificationItem {
    private int adminVideoId;
    private String image;
    private String title;
    private String time;

    public NotificationItem()
    {

    }

    public NotificationItem(int adminVideoId, String notifImage, String notifText, String notifTime) {
        this.adminVideoId = adminVideoId;
        this.image = notifImage;
        this.title = notifText;
        this.time = notifTime;
    }

    public int getAdminVideoId() {
        return adminVideoId;
    }

    public void setAdminVideoId(int adminVideoId) {
        this.adminVideoId = adminVideoId;
    }

    @Override
    public String toString() {
        return "ProfileNotificationItem{" +
                ", image='" + image + '\'' +
                ", title='" + title + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
