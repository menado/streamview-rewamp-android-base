package com.streamhash.streamview.model;

import java.util.ArrayList;
import java.util.HashMap;

public class Video {
    private int adminVideoId;
    private int seasonId;
    private int categoryId;
    private int genreId;
    private int subCategoryId;
    private int ratings;
    private int age;
    private int numDaysToExpire;
    private int seekHere;
    private int payPerViewId;
    private long likes;
    private long viewCount;
    private double amount;
    private double couponAmount;
    private double totalAmount;
    private boolean isInWishList;
    private boolean isInHistory;
    private boolean isLiked;
    private boolean isInSpam;
    private boolean isPayPerView;
    private boolean isSeasonVideo;
    private boolean isKidsVideo;
    private boolean isExpired;
    private boolean isTrailerVideo;
    private boolean isInWeb;
    private boolean isDownloadable;
    private boolean isUserSubscribed;
    private String adminUniqueId;
    private String thumbNailUrl;
    private String videoUrl;
    private String shareUrl;
    private String subTitleUrl;
    private String title;
    private String subCategoryName;
    private String detail;
    private String currency;
    private String description;
    private String defaultImage;
    private String publishTime;
    private String duration;
    private String trailerDuration;
    private String paidDate;
    private String couponCode;
    private String paymentMode;
    private String paymentId;
    private String typeOfSubscription;
    private ArrayList<Cast> casts;
    private ArrayList<Cast> choosePlans;
    private ArrayList<GenreSeason> genreSeasons;
    private ArrayList<Video> trailerVideos;
    private ArrayList<DownloadUrl> downloadResolutions;
    private VideoType videoType;
    private DownloadStatus downloadStatus;
    private HashMap<String, String> resolutions;
    private PayPerViewType payPerViewType;
    private SubscriptionType subscriptionType;

    public Video() {

    }

    public boolean isInSpam() {
        return isInSpam;
    }

    public void setInSpam(boolean inSpam) {
        isInSpam = inSpam;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public PayPerViewType getPayPerViewType() {
        return payPerViewType;
    }

    public void setPayPerViewType(PayPerViewType payPerViewType) {
        this.payPerViewType = payPerViewType;
    }

    public String getTypeOfSubscription() {
        return typeOfSubscription;
    }

    public void setTypeOfSubscription(String typeOfSubscription) {
        this.typeOfSubscription = typeOfSubscription;
    }

    public int getPayPerViewId() {
        return payPerViewId;
    }

    public void setPayPerViewId(int payPerViewId) {
        this.payPerViewId = payPerViewId;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public double getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(double couponAmount) {
        this.couponAmount = couponAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public boolean isUserSubscribed() {
        return isUserSubscribed;
    }

    public void setUserSubscribed(boolean userSubscribed) {
        isUserSubscribed = userSubscribed;
    }

    public int getSeekHere() {
        return seekHere;
    }

    public void setSeekHere(int seekHere) {
        this.seekHere = seekHere;
    }

    public ArrayList<Cast> getChoosePlans() {
        return choosePlans;
    }

    public void setChoosePlans(ArrayList<Cast> choosePlans) {
        this.choosePlans = choosePlans;
    }

    public boolean isInWeb() {
        return isInWeb;
    }

    public void setInWeb(boolean inWeb) {
        isInWeb = inWeb;
    }

    public int getNumDaysToExpire() {
        return numDaysToExpire;
    }

    public void setNumDaysToExpire(int numDaysToExpire) {
        this.numDaysToExpire = numDaysToExpire;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public ArrayList<DownloadUrl> getDownloadResolutions() {
        return downloadResolutions;
    }

    public void setDownloadResolutions(ArrayList<DownloadUrl> downloadResolutions) {
        this.downloadResolutions = downloadResolutions;
    }

    public boolean isDownloadable() {
        return isDownloadable;
    }

    public void setDownloadable(boolean downloadable) {
        isDownloadable = downloadable;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public boolean isTrailerVideo() {
        return isTrailerVideo;
    }

    public void setTrailerVideo(boolean trailerVideo) {
        isTrailerVideo = trailerVideo;
    }

    public HashMap<String, String> getResolutions() {
        return resolutions;
    }

    public void setResolutions(HashMap<String, String> resolutions) {
        this.resolutions = resolutions;
    }

    public boolean isSeasonVideo() {
        return isSeasonVideo;
    }

    public void setSeasonVideo(boolean seasonVideo) {
        isSeasonVideo = seasonVideo;
    }

    public ArrayList<Video> getTrailerVideos() {
        return trailerVideos;
    }

    public void setTrailerVideos(ArrayList<Video> trailerVideos) {
        this.trailerVideos = trailerVideos;
    }

    public ArrayList<GenreSeason> getGenreSeasons() {
        return genreSeasons;
    }

    public void setGenreSeasons(ArrayList<GenreSeason> genreSeasons) {
        this.genreSeasons = genreSeasons;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public boolean isInWishList() {
        return isInWishList;
    }

    public void setInWishList(boolean inWishList) {
        isInWishList = inWishList;
    }

    public boolean isInHistory() {
        return isInHistory;
    }

    public void setInHistory(boolean inHistory) {
        isInHistory = inHistory;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public ArrayList<Cast> getCasts() {
        return casts;
    }

    public void setCasts(ArrayList<Cast> casts) {
        this.casts = casts;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTrailerDuration() {
        return trailerDuration;
    }

    public void setTrailerDuration(String trailerDuration) {
        this.trailerDuration = trailerDuration;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public boolean isPayPerView() {
        return isPayPerView;
    }

    public void setPayPerView(boolean payPerView) {
        isPayPerView = payPerView;
    }

    public boolean isKidsVideo() {
        return isKidsVideo;
    }

    public void setKidsVideo(boolean kidsVideo) {
        isKidsVideo = kidsVideo;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSubTitleUrl() {
        return subTitleUrl;
    }

    public void setSubTitleUrl(String subTitleUrl) {
        this.subTitleUrl = subTitleUrl;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    public VideoType getVideoType() {
        return videoType;
    }

    public void setVideoType(VideoType videoType) {
        this.videoType = videoType;
    }

    public String getAdminUniqueId() {
        return adminUniqueId;
    }

    public void setAdminUniqueId(String adminUniqueId) {
        this.adminUniqueId = adminUniqueId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(int subCategoryId) {
        this.subCategoryId = subCategoryId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getAdminVideoId() {
        return adminVideoId;
    }

    public void setAdminVideoId(int adminVideoId) {
        this.adminVideoId = adminVideoId;
    }

    public String getThumbNailUrl() {
        return thumbNailUrl;
    }

    public void setThumbNailUrl(String thumbNailUrl) {
        this.thumbNailUrl = thumbNailUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public int getRatings() {
        return ratings;
    }

    public void setRatings(int ratings) {
        this.ratings = ratings;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultImage() {
        return defaultImage;
    }

    public void setDefaultImage(String defaultImage) {
        this.defaultImage = defaultImage;
    }

    @Override
    public String toString() {
        return "Video{" +
                "adminVideoId=" + adminVideoId +
                ", adminUniqueId='" + adminUniqueId + '\'' +
                ", thumbNailUrl='" + thumbNailUrl + '\'' +
                ", seasonId=" + seasonId +
                ", videoType=" + videoType +
                ", videoUrl='" + videoUrl + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                ", subTitleUrl='" + subTitleUrl + '\'' +
                ", title='" + title + '\'' +
                ", categoryId=" + categoryId +
                ", genreId=" + genreId +
                ", subCategoryId=" + subCategoryId +
                ", subCategoryName='" + subCategoryName + '\'' +
                ", ratings=" + ratings +
                ", detail='" + detail + '\'' +
                ", isInWishList=" + isInWishList +
                ", isInHistory=" + isInHistory +
                ", isLiked=" + isLiked +
                ", likes=" + likes +
                ", currency='" + currency + '\'' +
                ", casts=" + casts +
                ", choosePlans=" + choosePlans +
                ", genreSeasons=" + genreSeasons +
                ", trailerVideos=" + trailerVideos +
                ", description='" + description + '\'' +
                ", defaultImage='" + defaultImage + '\'' +
                ", publishTime='" + publishTime + '\'' +
                ", age=" + age +
                ", duration='" + duration + '\'' +
                ", trailerDuration='" + trailerDuration + '\'' +
                ", viewCount=" + viewCount +
                ", isPayPerView=" + isPayPerView +
                ", isSeasonVideo=" + isSeasonVideo +
                ", numDaysToExpire=" + numDaysToExpire +
                ", amount=" + amount +
                ", isKidsVideo=" + isKidsVideo +
                ", isExpired=" + isExpired +
                ", isTrailerVideo=" + isTrailerVideo +
                ", isInWeb=" + isInWeb +
                ", downloadStatus=" + downloadStatus +
                ", resolutions=" + resolutions +
                ", downloadResolutions=" + downloadResolutions +
                ", isDownloadable=" + isDownloadable +
                ", seekHere=" + seekHere +
                ", isUserSubscribed=" + isUserSubscribed +
                ", payPerViewId=" + payPerViewId +
                ", paidDate='" + paidDate + '\'' +
                ", couponAmount=" + couponAmount +
                ", totalAmount=" + totalAmount +
                ", couponCode='" + couponCode + '\'' +
                ", paymentMode='" + paymentMode + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", typeOfSubscription='" + typeOfSubscription + '\'' +
                '}';
    }

    public enum VideoType {
        VIDEO_YOUTUBE, VIDEO_MANUAL, VIDEO_OTHER
    }

    public enum DownloadStatus {
        SHOW_DOWNLOAD, DOWNLOAD_PROGRESS, DOWNLOAD_COMPLETED, NEED_TO_SUBSCRIBE, NEED_TO_PAY, DO_NOT_SHOW_DOWNLOAD
    }

    public enum PayPerViewType {
        ONE, TWO, THREE
    }

    public enum SubscriptionType {
        ONE, TWO, THREE
    }
}
