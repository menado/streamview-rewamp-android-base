package com.streamhash.streamview.util;

import com.streamhash.streamview.model.Card;
import com.streamhash.streamview.model.Cast;
import com.streamhash.streamview.model.DownloadUrl;
import com.streamhash.streamview.model.GenreSeason;
import com.streamhash.streamview.model.SubscriptionPlan;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.model.VideoSection;
import com.streamhash.streamview.ui.adapter.VideoTileAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.streamhash.streamview.model.Video.*;
import static com.streamhash.streamview.network.APIConstants.Params;

public class ParserUtils {
    private ParserUtils() {

    }

    public static Video parseVideoData(JSONObject dataObj) {
        Video video = new Video();
        video.setTrailerVideo(false);
        video.setAdminVideoId(dataObj.optInt(Params.ADMIN_VIDEO_ID));
        video.setAdminUniqueId(dataObj.optString(Params.ADMIN_UNIQUE_ID));
        video.setCategoryId(dataObj.optInt(Params.CATEGORY_ID));
        video.setSubCategoryId(dataObj.optInt(Params.SUB_CATEGORY_ID));
        video.setGenreId(dataObj.optInt(Params.GENRE_ID));
        video.setTitle(dataObj.optString(Params.TITLE));
        video.setDescription(dataObj.optString(Params.DESCRIPTION));
        video.setDetail(dataObj.optString(Params.DETAILS));
        video.setThumbNailUrl(dataObj.optString(Params.MOBILE_IMAGE));
        video.setPublishTime(dataObj.optString(Params.PUBLISH_TIME));
        video.setAge(dataObj.optInt(Params.AGE));
        video.setVideoUrl(dataObj.optString(Params.VIDEO_URL));
        video.setShareUrl(dataObj.optString(Params.SHARE_LINK));
        video.setSubTitleUrl(dataObj.optString(Params.SUBTITLE_URL));
        video.setDuration(dataObj.optString(Params.DURATION));
        video.setRatings(dataObj.optInt(Params.RATINGS));
        video.setViewCount(dataObj.optInt(Params.WATCH_COUNT));
        video.setPayPerView(dataObj.optInt(Params.SHOULD_DISPLAY_PPV) == 1);
        video.setSeekHere(dataObj.optInt(Params.SEEK_HERE));
        video.setInWishList(dataObj.optInt(Params.WISHLIST_STATUS) == 1);
        video.setInHistory(dataObj.optInt(Params.HISTORY_STATUS) == 1);
        video.setInSpam(dataObj.optInt(Params.IS_SPAM) == 1);
        video.setSeasonVideo(dataObj.optInt(Params.IS_SEASON_VIDEO) == 1);
        video.setLiked(dataObj.optInt(Params.IS_LIKED) == 1);
        video.setKidsVideo(dataObj.optInt(Params.IS_KIDS) == 1);
        video.setLikes(dataObj.optInt(Params.LIKES));
        video.setCurrency(dataObj.optString(Params.CURRENCY));
        video.setUserSubscribed(dataObj.optInt(Params.USER_TYPE) == 1);
        video.setAmount(dataObj.optInt(Params.PPV_AMOUNT));
        video.setResolutions(parseResolutions(dataObj.optJSONObject(Params.MAIN_VIDEO_RESOLUTIONS)));
        video.setDownloadResolutions(parseDownloadResolutions(dataObj.optJSONArray(Params.DOWNLOAD_URLS)));
        video.setCasts(parseCastAndCrew(dataObj.optJSONArray(Params.CAST_CREW)));
        video.setChoosePlans(parseChoosePlan(dataObj.optJSONArray(Params.PPV_TYPE_CONTENT)));

        PayPerViewType payPerViewType;
        switch (dataObj.optInt(Params.PPV_PAGE_TYPE)) {
            case 1:
                payPerViewType = PayPerViewType.ONE;
                break;
            case 2:
                payPerViewType = PayPerViewType.TWO;
                break;
            case 3:
            default:
                payPerViewType = PayPerViewType.THREE;
                break;
        }
        video.setPayPerViewType(payPerViewType);

        SubscriptionType subscriptionType;
        switch (dataObj.optInt(Params.TYPE_OF_SUBSCRIPTION)) {
            case 1:
                subscriptionType = SubscriptionType.ONE;
                break;
            case 2:
                subscriptionType = SubscriptionType.TWO;
                break;
            case 3:
            default:
                subscriptionType = SubscriptionType.THREE;
                break;
        }
        video.setSubscriptionType(subscriptionType);

        VideoType videoType;
        switch (dataObj.optInt(Params.VIDEO_TYPE)) {
            case 2:
                videoType = VideoType.VIDEO_YOUTUBE;
                break;
            case 3:
                videoType = VideoType.VIDEO_OTHER;
                break;
            case 1:
            default:
                videoType = VideoType.VIDEO_MANUAL;
                break;
        }
        video.setVideoType(videoType);

        video.setDownloadable(dataObj.optInt(Params.DOWNLOAD_BUTTON_STATUS) != 0);
        DownloadStatus downloadStatus;
        switch (dataObj.optInt(Params.DOWNLOAD_BUTTON_STATUS)) {
            case 0:
                downloadStatus = DownloadStatus.DO_NOT_SHOW_DOWNLOAD;
                break;
            case 1:
                downloadStatus = DownloadStatus.SHOW_DOWNLOAD;
                break;
            case 2:
                downloadStatus = DownloadStatus.DOWNLOAD_PROGRESS;
                break;
            case 3:
                downloadStatus = DownloadStatus.DOWNLOAD_COMPLETED;
                break;
            case 4:
                downloadStatus = DownloadStatus.NEED_TO_SUBSCRIBE;
                break;
            case 5:
            default:
                downloadStatus = DownloadStatus.NEED_TO_PAY;
                break;
        }
        video.setDownloadStatus(downloadStatus);
        return video;
    }

    public static void parseVideoRelatedData(Video video, JSONObject dataObj) {
        video.setSeasonVideo(dataObj.optInt(Params.IS_SEASON_VIDEO) == 1);
        video.setTrailerVideos(parseTrailerVideos(dataObj.optJSONArray(Params.TRAILER_SECTION)));
        video.setGenreSeasons(parseGenreSeasons(video, dataObj.optJSONArray(Params.GENRES)));
    }

    private static ArrayList<Video> parseTrailerVideos(JSONArray trailerArr) {
        ArrayList<Video> trailers = new ArrayList<>();
        if (trailerArr == null)
            return trailers;

        for (int i = 0; i < trailerArr.length(); i++) {
            try {
                JSONObject trailerObj = trailerArr.getJSONObject(i);
                Video trailer = new Video();
                trailer.setTrailerVideo(true);
                trailer.setTitle(trailerObj.optString(Params.NAME));
                VideoType videoType;
                switch (trailerObj.optInt(Params.VIDEO_TYPE)) {
                    case 2:
                        videoType = VideoType.VIDEO_YOUTUBE;
                        break;
                    case 3:
                        videoType = VideoType.VIDEO_OTHER;
                        break;
                    case 1:
                    default:
                        videoType = VideoType.VIDEO_MANUAL;
                        break;
                }
                trailer.setVideoType(videoType);
                trailer.setThumbNailUrl(trailerObj.optString(Params.DEFAULT_IMAGE));
                trailer.setResolutions(parseResolutions(trailerObj.optJSONObject(Params.RESOLUTIONS)));
                trailer.setVideoUrl(trailer.getResolutions().get(Params.ORIGINAL));
                trailers.add(trailer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return trailers;
    }

    private static HashMap<String, String> parseResolutions(JSONObject resolutionsObj) {
        HashMap<String, String> resolutions = new HashMap<>();
        if (resolutionsObj == null)
            return resolutions;

        Iterator<String> keys = resolutionsObj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            resolutions.put(key, resolutionsObj.optString(key));
        }
        return resolutions;
    }

    private static ArrayList<DownloadUrl> parseDownloadResolutions(JSONArray downloadResolutionsArr) {
        ArrayList<DownloadUrl> downloadUrls = new ArrayList<>();
        if (downloadResolutionsArr == null)
            return downloadUrls;

        for (int i = 0; i < downloadResolutionsArr.length(); i++) {
            try {
                JSONObject downloadResObj = downloadResolutionsArr.getJSONObject(i);
                downloadUrls.add(new DownloadUrl(downloadResObj.optString(Params.TITLE),
                        downloadResObj.optString(Params.LINK),
                        downloadResObj.optString(Params.TYPE)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return downloadUrls;
    }

    private static ArrayList<GenreSeason> parseGenreSeasons(Video curVideo, JSONArray seasons) {
        ArrayList<GenreSeason> genreSeasons = new ArrayList<>();

        int currentSeasonId = -1;

        for (int i = 0; i < seasons.length(); i++) {
            try {
                JSONObject object = seasons.getJSONObject(i);
                boolean isSelfSeason = object.optInt(Params.IS_SELECTED) == 1;

                GenreSeason genre = new GenreSeason();
                genre.setId(object.optInt(Params.GENRE_ID));
                genre.setName(object.optString(Params.GENRE_NAME));
                genreSeasons.add(genre);

                if (isSelfSeason)
                    currentSeasonId = genre.getId();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        curVideo.setSeasonId(currentSeasonId);

        return genreSeasons;
    }


    public static SubscriptionPlan parsePlan(JSONObject planObj) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(planObj.optInt(Params.SUBSCRIPTION_ID));
        plan.setTitle(planObj.optString(Params.TITLE));
        plan.setDescription(planObj.optString(Params.DESCRIPTION));
        plan.setAmount(planObj.optDouble(Params.AMOUNT));
        plan.setCurrency(planObj.optString(Params.CURRENCY));
        plan.setNoOfAccounts(planObj.optInt(Params.NO_OF_ACCOUNTS));
        plan.setPaymentId(planObj.optString(Params.PAYMENT_ID));
        plan.setPopular(planObj.optInt(Params.POPULAR_STATUS) == 1);
        plan.setCouponAmt(planObj.optString(Params.COUPON_AMT));
        plan.setCouponCode(planObj.optString(Params.COUPON_CODE));
        plan.setActivePlan(planObj.optInt(Params.ACTIVE_PLAN) == 1);
        plan.setCancelled(planObj.optInt(Params.CANCEL_STATUS) == 0);
        plan.setPaymentStatus(planObj.optString(Params.PAYMENT_STATUS));
        plan.setMonths(planObj.optInt(Params.PLAN));
        plan.setExpires(planObj.optString(Params.EXPIRIES_ON));
        plan.setPaymentMode(planObj.optString(Params.PAYMENT_MODE));
        plan.setTotalAmt(planObj.optDouble(Params.TOTAL_AMT));
        return plan;
    }

    public static VideoSection parseOriginalsVideos(JSONObject originalsObj) {
        VideoSection original = null;
        JSONArray originalVideoItems = originalsObj.optJSONArray(Params.DATA);
        ArrayList<Video> originalVideos = parseVideoItemsArray(originalVideoItems);
        if (!originalVideos.isEmpty()) {
            original = new VideoSection("Originals"
                    , originalsObj.optString(Params.SEE_ALL_URL)
                    , originalsObj.optString(Params.URL_TYPE)
                    , originalsObj.optString(Params.URL_PAGE_ID)
                    , VideoTileAdapter.VIDEO_SECTION_TYPE_ORIGINALS
                    , originalVideos);
        }
        return original;
    }

    public static ArrayList<VideoSection> parseVideoSections(JSONArray videoSectionsObj) {
        ArrayList<VideoSection> videoSections = new ArrayList<>();
        for (int i = 0; i < videoSectionsObj.length(); i++) {
            try {
                JSONObject videoSectionObj = videoSectionsObj.getJSONObject(i);
                JSONArray videosForThisSection = videoSectionObj.optJSONArray(Params.DATA);
                if (videosForThisSection != null) {
                    ArrayList<Video> videos = parseVideoItemsArray(videosForThisSection);
                    if (!videos.isEmpty()) {
                        videoSections.add(new VideoSection(
                                videoSectionObj.optString(Params.TITLE)
                                , videoSectionObj.optString(Params.SEE_ALL_URL)
                                , videoSectionObj.optString(Params.URL_TYPE)
                                , videoSectionObj.optString(Params.URL_PAGE_ID)
                                , VideoTileAdapter.VIDEO_SECTION_TYPE_NORMAL
                                , videos));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return videoSections;
    }

    public static ArrayList<Video> parseVideoItemsArray(JSONArray videoArray) {
        ArrayList<Video> videos = new ArrayList<>();
        for (int i = 0; i < videoArray.length(); i++) {
            try {
                JSONObject object = videoArray.getJSONObject(i);
                Video video = parseVideoData(object);
                videos.add(video);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return videos;
    }

    private static ArrayList<Cast> parseCastAndCrew(JSONArray castCrewObj) {
        ArrayList<Cast> casts = new ArrayList<>();

        if (castCrewObj == null)
            return casts;

        for (int i = 0; i < castCrewObj.length(); i++) {
            try {
                JSONObject castItem = castCrewObj.getJSONObject(i);
                casts.add(new Cast(castItem.optInt(Params.CAST_CREW_ID)
                        , castItem.optString(Params.NAME)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return casts;
    }

    private static ArrayList<Cast> parseChoosePlan(JSONArray castCrewObj) {
        ArrayList<Cast> choose = new ArrayList<>();

        if (castCrewObj == null)
            return choose;

        for (int i = 0; i < castCrewObj.length(); i++) {
            try {
                JSONObject content = castCrewObj.getJSONObject(i);
                Cast cont = new Cast();
                cont.setName(content.optString("title"));
                cont.setDesc(content.optString("description"));
                cont.setType(content.optString("type"));
                choose.add(cont);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return choose;
    }

    public static Card parseCardData(JSONObject cardObj) {
        Card card = new Card();
        card.setDefault(cardObj.optInt(Params.IS_DEFAULT) == 1);
        card.setId(cardObj.optInt(Params.USER_CARD_ID));
        card.setCardToken(cardObj.optString(Params.CARD_TOKEN));
        card.setCvv(cardObj.optString(Params.CARD_CVV));
        card.setLast4(cardObj.optString(Params.CARD_LAST_FOUR));
        card.setMonth(cardObj.optString(Params.CARD_MONTH));
        card.setYear(cardObj.optString(Params.CARD_YEAR));
        return card;
    }

    public static Video parsePaidVideoData(JSONObject paidVideoObj) {
        Video paidVideo = new Video();
        paidVideo.setAdminVideoId(paidVideoObj.optInt(Params.ADMIN_VIDEO_ID));
        paidVideo.setPayPerViewId(paidVideoObj.optInt(Params.PAY_PER_VIEW_ID));
        paidVideo.setTitle(paidVideoObj.optString(Params.TITLE));
        paidVideo.setThumbNailUrl(paidVideoObj.optString(Params.PICTURE));
        paidVideo.setDescription(paidVideoObj.optString(Params.DESCRIPTION));
        paidVideo.setPaidDate(paidVideoObj.optString(Params.PAID_DATE));
        paidVideo.setCouponAmount(paidVideoObj.optDouble(Params.COUPON_AMT));
        paidVideo.setAmount(paidVideoObj.optDouble(Params.TOTAL_AMT));
        paidVideo.setAmount(paidVideoObj.optInt(Params.AMOUNT));
        paidVideo.setCurrency(paidVideoObj.optString(Params.CURRENCY));
        paidVideo.setCouponCode(paidVideoObj.optString(Params.COUPON_CODE));
        paidVideo.setPaymentMode(paidVideoObj.optString(Params.PAYMENT_MODE));
        paidVideo.setPaymentId(paidVideoObj.optString(Params.PAYMENT_ID));
        paidVideo.setTypeOfSubscription(paidVideoObj.optString(Params.TYPE_OF_SUBSCRIPTION));
        paidVideo.setDuration(paidVideoObj.optString(Params.DURATION));
        return paidVideo;
    }
}
