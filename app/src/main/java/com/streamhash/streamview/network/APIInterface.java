package com.streamhash.streamview.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import static com.streamhash.streamview.network.APIConstants.APIs;
import static com.streamhash.streamview.network.APIConstants.Params;

public interface APIInterface {

    @FormUrlEncoded
    @POST(APIs.GET_APP_CONFIG)
    Call<String> getAppConfigs(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token);

    @FormUrlEncoded
    @POST(APIs.REGISTER)
    Call<String> signUpUser(@Field(Params.EMAIL) String email
            , @Field(Params.PASSWORD) String password
            , @Field(Params.NAME) String name
            , @Field(Params.MOBILE) String mobile
            , @Field(Params.LOGIN_BY) String loginBy
            , @Field(Params.DEVICE_TYPE) String deviceType
            , @Field(Params.DEVICE_TOKEN) String deviceToken);


    @FormUrlEncoded
    @POST(APIs.LOGIN)
    Call<String> loginUser(@Field(Params.EMAIL) String email
            , @Field(Params.PASSWORD) String password
            , @Field(Params.LOGIN_BY) String loginBy
            , @Field(Params.DEVICE_TYPE) String deviceType
            , @Field(Params.DEVICE_TOKEN) String deviceToken);


    @FormUrlEncoded
    @POST(APIs.SOCIAL_LOGIN)
    Call<String> socialLoginUser(@Field(Params.SOCIAL_UNIQUE_ID) String socialUniqueId
            , @Field(Params.LOGIN_BY) String loginBy
            , @Field(Params.EMAIL) String email
            , @Field(Params.NAME) String name
            , @Field(Params.MOBILE) String mobile
            , @Field(Params.PICTURE) String picture
            , @Field(Params.DEVICE_TYPE) String deviceType
            , @Field(Params.DEVICE_TOKEN) String deviceToken);

    @FormUrlEncoded
    @POST(APIs.LOGOUT)
    Call<String> logOutUser(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token);

    @FormUrlEncoded
    @POST(APIs.NOTIFICATION_COUNT)
    Call<String> getNotificationCount(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId);

    @FormUrlEncoded
    @POST(APIs.DELETE_ACCOUNT)
    Call<String> deleteAccount(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.PASSWORD) String password);

    @FormUrlEncoded
    @POST(APIs.FORGOT_PASSWORD)
    Call<String> forgotPassword(@Field(Params.EMAIL) String email);

    @FormUrlEncoded
    @POST(APIs.CHANGE_PASSWORD)
    Call<String> changePassword(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.OLD_PASSWORD) String curPassword
            , @Field(Params.PASSWORD) String newPassword
            , @Field(Params.CONFIRM_PASSWORD) String newPasswordConfirm);

    //    @Headers({"Content-Type: multipart/form-data"})
    @Multipart
    @POST(APIs.UPDATE_USER_PROFILE)
    Call<String> updateUserProfile(@Part(Params.ID) RequestBody id
            , @Part(Params.TOKEN) RequestBody token
            , @Part(Params.EMAIL) RequestBody email
            , @Part(Params.NAME) RequestBody name
            , @Part(Params.DESCRIPTION) RequestBody description
            , @Part MultipartBody.Part picture);


    @FormUrlEncoded
    @POST(APIs.NOTIFICATION_SETTING_UPDATE)
    Call<String> updateNotificationSetting(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.NOTIFICATION_TYPE) String type
            , @Field(Params.STATUS) int status);

    @FormUrlEncoded
    @POST(APIs.GET_SUB_PROFILES)
    Call<String> getSubProfiles(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.DEVICE_TYPE) String deviceType);

    //    @Headers({"Content-Type: multipart/form-data"})
    @Multipart
    @POST(APIs.ADD_SUB_PROFILE)
    Call<String> addSubProfile(@Part(Params.ID) RequestBody id
            , @Part(Params.TOKEN) RequestBody token
            , @Part(Params.NAME) RequestBody name
            , @Part MultipartBody.Part picture);

    //    @Headers({"Content-Type: multipart/form-data"})
    @Multipart
    @POST(APIs.EDIT_SUB_PROFILE)
    Call<String> editSubProfile(@Part(Params.ID) RequestBody id
            , @Part(Params.TOKEN) RequestBody token
            , @Part(Params.SUB_PROFILE_ID) RequestBody subProfileId
            , @Part(Params.NAME) RequestBody name
            , @Part MultipartBody.Part picture);

    @FormUrlEncoded
    @POST(APIs.DELETE_SUB_PROFILE)
    Call<String> deleteSubProfile(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.DELETE_SUB_PROFILE) int subProfileToDelete);

    @FormUrlEncoded
    @POST(APIs.GET_CARDS)
    Call<String> getCards(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId);

    @FormUrlEncoded
    @POST(APIs.ADD_CARD)
    Call<String> addCard(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.CARD_TOKEN) String cardToken);

    @FormUrlEncoded
    @POST(APIs.DELETE_CARD)
    Call<String> deleteCard(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.USER_CARD_ID) int cardId
            , @Field(Params.POSITION) int position);


    @FormUrlEncoded
    @POST(APIs.MAKE_DEFAULT_CARD)
    Call<String> makeCardDefault(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.USER_CARD_ID) int cardId);

    @GET(APIs.GET_STATIC_PAGE)
    Call<String> getStaticPage(@Query(Params.PAGE_TYPE) String pageType);

    @GET(APIs.GET_SPAM_REASONS)
    Call<String> getSpamReasons();

    @FormUrlEncoded
    @POST(APIs.ADD_TO_SPAM)
    Call<String> addToSpam(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.REASON) String spamReason);

    @FormUrlEncoded
    @POST(APIs.REMOVE_FROM_SPAM)
    Call<String> removeFromSpam(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);


    @FormUrlEncoded
    @POST(APIs.GET_WISHLIST)
    Call<String> getWishListItems(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);


    @FormUrlEncoded
    @POST(APIs.GET_VIDEOS_FOR_SEASON)
    Call<String> getVideosForSeason(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.PAGE_TYPE) String pageType
            , @Field(Params.CATEGORY_ID) int categoryId
            , @Field(Params.SUB_CATEGORY_ID) int subCategoryId
            , @Field(Params.GENRE_ID) int genreId
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.SPAM_VIDEOS)
    Call<String> getSpamVideos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.CAST_VIDEOS)
    Call<String> getCastVideos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.CAST_CREW_ID) int castCrewId
            , @Field(Params.DEVICE_TYPE) String deviceType
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.HISTORY_VIDEOS)
    Call<String> getHistoryVideos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.AVALIABLE_PLANS)
    Call<String> getAvaliablePlans(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.MY_PLANS)
    Call<String> getMyPlans(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);


    @FormUrlEncoded
    @POST(APIs.PAID_VIDEOS)
    Call<String> getMyPaidVideos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.CATEGORIES)
    Call<String> getCategories(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.NOTIFICATIONS)
    Call<String> getNotifications(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip);


    @FormUrlEncoded
    @POST(APIs.GET_SINGLE_VIDEO_DATA)
    Call<String> getVideoData(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);

    @FormUrlEncoded
    @POST(APIs.GET_SINGLE_VIDEO_RELATED_DATA)
    Call<String> getVideoRelatedData(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);


    @FormUrlEncoded
    @POST(APIs.GET_VIDEO_CONTENT_FOR)
    Call<String> getVideoContentFor(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.PAGE_TYPE) String pageType
            , @Field(Params.CATEGORY_ID) int categoryId
            , @Field(Params.SUB_CATEGORY_ID) int subCategoryId
            , @Field(Params.GENRE_ID) int genreId);

    @FormUrlEncoded
    @POST(APIs.GET_VIDEO_CONTENT_DYNAMIC_FOR)
    Call<String> getVideoContentDynamicFor(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.PAGE_TYPE) String pageType
            , @Field(Params.CATEGORY_ID) int categoryId
            , @Field(Params.SUB_CATEGORY_ID) int subCategoryId
            , @Field(Params.GENRE_ID) int genreId
            , @Field(Params.SKIP) int skip);

    @FormUrlEncoded
    @POST(APIs.CONTINUE_WATCHING_STORE)
    Call<String> continueWatchingStorePos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.DURATION) int elapsed);

    @FormUrlEncoded
    @POST(APIs.CONTINUE_WATCHING_END)
    Call<String> continueWatchingEnd(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);

    @FormUrlEncoded
    @POST(APIs.MAKE_PAY_PAL_PAYMENT)
    Call<String> makePayPalPlanPayment(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SUBSCRIPTION_ID) int subscriptionId
            , @Field(Params.PAYMENT_ID) String paymentId
            , @Field(Params.COUPON_CODE) String couponCode);

    @FormUrlEncoded
    @POST(APIs.MAKE_STRIPE_PAYMNET)
    Call<String> makeStripePayment(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SUBSCRIPTION_ID) int subscriptionId
            , @Field(Params.COUPON_CODE) String couponCode);

    @FormUrlEncoded
    @POST(APIs.MAKE_STRIPE_PPV)
    Call<String> makeStripePPV(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.COUPON_CODE) String couponCode);

    @FormUrlEncoded
    @POST(APIs.MAKE_PAYPAL_PPV)
    Call<String> makePayPalPPV(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.PAYMENT_ID) String paymentId
            , @Field(Params.COUPON_CODE) String couponCode);

    @FormUrlEncoded
    @POST(APIs.APPLY_COUPON_CODE)
    Call<String> applyCouponCode(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SUBSCRIPTION_ID) int subscriptionId
            , @Field(Params.COUPON_CODE) String couponCode);

    @FormUrlEncoded
    @POST(APIs.APPLY_PPV_CODE)
    Call<String> applyPPVCode(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.COUPON_CODE) String couponCode);


    @FormUrlEncoded
    @POST(APIs.AUTO_RENEWAL_ENABLE)
    Call<String> autoRenewalEnable(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId);

    @FormUrlEncoded
    @POST(APIs.CANCEL_SUBSCRIPTION)
    Call<String> cancelSubscription(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId);

    @FormUrlEncoded
    @POST(APIs.DOWNLOADED_VIDEOS)
    Call<String> downloadedVideos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId);

    @FormUrlEncoded
    @POST(APIs.DOWNLOADED_STATUS_UPDATE)
    Call<String> downloadStatusUpdate(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.STATUS) int status);


    @FormUrlEncoded
    @POST(APIs.ADD_TO_WISH_LIST)
    Call<String> toggleWishList(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.STATUS) int isInWishList);

    @FormUrlEncoded
    @POST(APIs.ADD_TO_HISTORY)
    Call<String> addToHistory(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);

    @FormUrlEncoded
    @POST(APIs.LIKE_VIDEO)
    Call<String> likeVideo(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);

    @FormUrlEncoded
    @POST(APIs.UNLIKE_VIDEO)
    Call<String> unLikeVideo
            (@Field(Params.ID) int id
                    , @Field(Params.TOKEN) String token
                    , @Field(Params.SUB_PROFILE_ID) int subProfileId
                    , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);

    @FormUrlEncoded
    @POST(APIs.CLEAR_HISTORY)
    Call<String> clearHistory(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.STATUS) int status);

    @FormUrlEncoded
    @POST(APIs.CLEAR_WISH_LIST)
    Call<String> clearWishList(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.CLEAR_ALL_STATUS) int status);


    @FormUrlEncoded
    @POST(APIs.SEARCH_VIDEOS)
    Call<String> searchVideos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SEARCH_TERM) String searchTerm);

    @FormUrlEncoded
    @POST(APIs.CLEAR_SPAM_LIST)
    Call<String> clearSpamLsit(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId
            , @Field(Params.STATUS) int status);

    @FormUrlEncoded
    @POST(APIs.SEE_ALL_URL)
    Call<String> moreVideosList(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.URL_TYPE) String urlType
            , @Field(Params.PAGE_TYPE) String pageType
            , @Field(Params.URL_PAGE_ID) int urlPageId
            , @Field(Params.CATEGORY_ID) int categoryId
            , @Field(Params.SUB_CATEGORY_ID) int subCateId
            , @Field(Params.CAST_CREW_ID) int castNCrewId
            , @Field(Params.GENRE_ID) int genreId
            , @Field(Params.SKIP) int skip
    );


    @FormUrlEncoded
    @POST(APIs.SUGGESTION_VIDEOS)
    Call<String> getSuggestionVideos(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.SUB_PROFILE_ID) int subProfileId
            , @Field(Params.SKIP) int skip
            , @Field(Params.CATEGORY_ID) int categoryId
            , @Field(Params.SUB_CATEGORY_ID) int subCateId
            , @Field(Params.GENRE_ID) int genreId
            , @Field(Params.PAGE_TYPE) String pageType
    );

    @FormUrlEncoded
    @POST(APIs.PPV_END)
    Call<String> ppvEnd(@Field(Params.ID) int id
            , @Field(Params.TOKEN) String token
            , @Field(Params.ADMIN_VIDEO_ID) int adminVideoId);
}
