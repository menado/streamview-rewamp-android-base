package com.streamhash.streamview.network;

import android.text.TextWatcher;

public class APIConstants {


    private APIConstants() {

    }

    public static class Payments {

        public static class PayPal {

            public static final String CLIENT_ID = "AaXkweZD5g9s0X3BsO0Y4Q-kNzbmLZaog0mbmVGrTT5IX0O73LoLVcHp17e6pkG7Vm04JEUuG6up30LD";
        }
    }

    public static class Constants {
        public static final String MANUAL_LOGIN = "manual";
        public static final String GOOGLE_LOGIN = "google";
        public static final String FACEBOOK_LOGIN = "facebook";
        public static final String ANDROID = "android";
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
        public static final String TRUE = "true";
        public static final String FALSE = "false";
    }


    static class URLs {
        static final String LIVE_URL = "http://adminview.streamhash.com/";
        static final String STAGING_URL = "http://admin-streamview.botfingers.com/";
        static final String BASE_URL = LIVE_URL;

        private URLs() {

        }
    }

    public static class ErrorCodes {
        public static final int NEED_TO_SUBSCRIBE_TO_MAKE_PAYMENT = 901;
        public static final int NO_DEFAULT_CARD_FOUND = 10000;
        static final int TOKEN_EXPIRED = 103;
        static final int SUB_PROFILE_DOESNT_EXIST = 3002;
        static final int USER_DOESNT_EXIST = 133;
        static final int USER_RECORD_DELETED_CONTACT_ADMIN = 3000;
        static final int INVALID_TOKEN = 104;
        static final int USER_LOGIN_DECLINED = 905;
        static final int EMAIL_NOT_ACTIVATED = 111;

        private ErrorCodes() {
        }

    }

    public static class APIs {
        public static final String API_STR = "userApi/";
        public static final String LOGIN = API_STR + "login";
        public static final String REGISTER = API_STR + "register";
        public static final String SOCIAL_LOGIN = REGISTER;
        public static final String FORGOT_PASSWORD = API_STR + "forgotpassword";
        public static final String CHANGE_PASSWORD = API_STR + "changePassword";
        public static final String DELETE_ACCOUNT = API_STR + "deleteAccount";
        public static final String LOGOUT = API_STR + "logout";
        public static final String USER_PROFILE = API_STR + "profile";
        public static final String UPDATE_USER_PROFILE = API_STR + "updateProfile";
        public static final String NOTIFICATION_SETTING_UPDATE = API_STR + "notification/settings";
        public static final String ADD_CARD = API_STR + "payment_card_add";
        public static final String GET_PAYMENT_METHODS_LIST = API_STR + "cards_list";
        public static final String DELETE_CARD = API_STR + "delete_card";
        public static final String MAKE_DEFAULT_CARD = API_STR + "default_card";
        public static final String GET_APP_CONFIG = "project/configurations";
        public static final String GET_STATIC_PAGE = API_STR + "pages/list";
        public static final String GET_WISHLIST = API_STR + "wishlists";
        public static final String SPAM_VIDEOS = API_STR + "spam_videos/list";
        public static final String HISTORY_VIDEOS = API_STR + "history/list";
        public static final String AVALIABLE_PLANS = API_STR + "subscription_plans";
        public static final String MY_PLANS = API_STR + "subscribedPlans";
        public static final String PAID_VIDEOS = API_STR + "ppv_list";
        public static final String CATEGORIES = API_STR + "v4/categories/list";
        public static final String NOTIFICATIONS = API_STR + "notifications";
        public static final String GET_SUB_PROFILES = API_STR + "active-profiles";
        public static final String ADD_SUB_PROFILE = API_STR + "add-profile";
        public static final String EDIT_SUB_PROFILE = API_STR + "edit-sub-profile";
        public static final String DELETE_SUB_PROFILE = API_STR + "sub_profiles/delete";
        public static final String GET_CARDS = API_STR + "card_details";
        public static final String GET_VIDEO_CONTENT_FOR = API_STR + "home_first_section";
        public static final String GET_SINGLE_VIDEO_DATA = API_STR + "videos/view";
        public static final String MAKE_PAY_PAL_PAYMENT = API_STR + "pay_now";
        public static final String ADD_TO_WISH_LIST = API_STR + "wishlists/operations";
        public static final String CLEAR_WISH_LIST = API_STR + "wishlists/operations";
        public static final String ADD_TO_HISTORY = API_STR + "addHistory";
        public static final String CLEAR_HISTORY = API_STR + "deleteHistory";
        public static final String LIKE_VIDEO = API_STR + "like_video";
        public static final String UNLIKE_VIDEO = API_STR + "dis_like_video";
        public static final String SEARCH_VIDEOS = API_STR + "searchVideo";
        public static final String ADD_TO_SPAM = API_STR + "spam_videos/add";
        public static final String REMOVE_FROM_SPAM = API_STR + "spam_videos/remove";
        public static final String GET_SPAM_REASONS = API_STR + "spam-reasons";
        public static final String MAKE_STRIPE_PAYMNET = API_STR + "stripe_payment";
        public static final String APPLY_COUPON_CODE = API_STR + "apply/coupon/subscription";
        public static final String APPLY_PPV_CODE = API_STR + "apply/coupon/ppv";
        public static final String MAKE_STRIPE_PPV = API_STR + "stripe_ppv";
        public static final String MAKE_PAYPAL_PPV = API_STR + "paypal_ppv";
        public static final String CANCEL_SUBSCRIPTION = API_STR + "cancel/subscription";
        public static final String AUTO_RENEWAL_ENABLE = API_STR + "autorenewal/enable";
        public static final String CLEAR_SPAM_LIST = API_STR + "remove_spam";
        public static final String CLEAR_HISTORY_VIDEOS = API_STR + "deleteHistory";
        public static final String CAST_VIDEOS = API_STR + "cast_crews/videos";
        public static final String GET_SINGLE_VIDEO_RELATED_DATA = API_STR + "videos/view/second";
        public static final String GET_VIDEOS_FOR_SEASON = API_STR + "genre_videos";
        public static final String SEE_ALL_URL = API_STR + "see_all";
        public static final String PPV_END = API_STR + "ppv_end";
        public static final String GET_VIDEO_CONTENT_DYNAMIC_FOR = API_STR + "home_second_section";
        public static final String DOWNLOADED_VIDEOS = API_STR + "downloaded/videos";
        public static final String DOWNLOADED_STATUS_UPDATE = API_STR + "video/download";
        public static final String NOTIFICATION_COUNT = API_STR + "notification/count";
        public static final String PAGES = API_STR + "pages/list";
        public static final String SUGGESTION_VIDEOS = API_STR + "suggestions";
        static final String CONTINUE_WATCHING_STORE = API_STR + "continue_watching_videos/save";
        static final String CONTINUE_WATCHING_END = API_STR + "oncomplete/video";

        private APIs() {
        }
    }

    public static class DownloadStatus {
        public static final int DOWNLOAD_INITIATE_STATUS = 1;
        public static final int DOWNLOAD_COMPLETE_STATUS = 4;
        public static final int DOWNLOAD_CANCEL_STATUS = 5;
        public static final int DOWNLOAD_DELETED_STATUS = 6;
    }

    public static class Params {
        public static final String ID = "id";
        public static final String TOKEN = "token";
        public static final String NAME = "name";
        public static final String EMAIL = "email";
        public static final String PASSWORD = "password";
        public static final String LOGIN_BY = "login_by";
        public static final String DEVICE_TOKEN = "device_token";
        public static final String DEVICE_TYPE = "device_type";
        public static final String MESSAGE = "message";
        public static final String ERROR_MESSAGE = "error_messages";
        public static final String USER_ID = "id";
        public static final String USER_NAME = "userName";
        public static final String SUCCESS = "success";
        public static final String PICTURE = "picture";
        public static final String DESCRIPTION = "description";
        public static final String OLD_PASSWORD = "old_password";
        public static final String CONFIRM_PASSWORD = "password_confirmation";
        public static final String SOCIAL_UNIQUE_ID = "social_unique_id";
        public static final String NOTIFICATION_TYPE = "type";
        public static final String STATUS = "status";
        public static final String NOTIF_PUSH_STATUS = "push_status";
        public static final String NOTIF_EMAIL_STATUS = "email_status";
        public static final String CARD_TOKEN = "card_token";
        public static final String USER_CARD_ID = "card_id";
        public static final String PAYMENT_MODES = "payment_modes";
        public static final String CARDS = "cards";
        public static final String IS_DEFAULT = "is_default";
        public static final String PAYMENT_MODE_IMAGE = "image";
        public static final String PAGE_TYPE = "page_type";
        public static final String DATA = "data";
        public static final String HISTORY = "history";
        public static final String ERROR_CODE = "error_code";
        public static final String CARD_LAST_FOUR = "last_four";
        public static final String CARD_NAME = "card_name";
        public static final String MOBILE = "mobile";
        public static final String SUB_PROFILE_ID = "sub_profile_id";
        public static final String SKIP = "skip";
        public static final String WISHLIST = "wishlist";
        public static final String ADMIN_VIDEO_ID = "admin_video_id";
        public static final String TITLE = "title";
        public static final String DEFAULT_IMAGE = "default_image";
        public static final String SUBSCRIPTION_ID = "subscription_id";
        public static final String PLAN = "plan";
        public static final String AMOUNT = "amount";
        public static final String CREATED_AT = "created_at";
        public static final String CURRENCY = "currency";
        public static final String NO_OF_ACCOUNTS = "no_of_account";
        public static final String POPULAR_STATUS = "popular_status";
        public static final String PAYMENT_ID = "payment_id";
        public static final String COUPON_AMT = "coupon_amount";
        public static final String COUPON_CODE = "coupon_code";
        public static final String COUPON = "coupon";
        public static final String ACTIVE_PLAN = "active_plan";
        public static final String CANCEL_STATUS = "cancelled_status";
        public static final String PAYMENT_STATUS = "payment_status";
        public static final String EXPIRIES_ON = "expiry_date";
        public static final String PAYMENT_MODE = "payment_mode";
        public static final String TOTAL_AMT = "total_amount";
        public static final String PAY_PER_VIEW_ID = "pay_per_view_id";
        public static final String PAID_DATE = "paid_date";
        public static final String TYPE_OF_SUBSCRIPTION = "type_of_subscription";
        public static final String TYPE_OF_USER = "type_of_user";
        public static final String USER_TYPE = "user_type";
        public static final String CATEGORIES = "categories";
        public static final String TIME = "time";
        public static final String IMG = "img";
        public static final String SUB_PROFILE_USER_ID = "id";
        public static final String POSITION = "position";
        public static final String CARD_NUMBER = "number";
        public static final String CARD_MONTH = "month";
        public static final String CARD_YEAR = "year";
        public static final String CARD_CVV = "cvv";
        public static final String CATEGORY_ID = "category_id";
        public static final String SUB_CATEGORY_ID = "sub_category_id";
        public static final String GENRE_ID = "genre_id";
        public static final String BANNER = "banner";
        public static final String ORIGINALS = "originals";
        public static final String MOBILE_IMAGE = "mobile_image";
        public static final String SEE_ALL_URL = "see_all_url";
        public static final String ERROR_MSG = "error_message";
        public static final String ADMIN_UNIQUE_ID = "admin_unique_id";
        public static final String PUBLISH_TIME = "publish_time";
        public static final String AGE = "age";
        public static final String VIDEO_URL = "video";
        public static final String SUBTITLE_URL = "video_subtitle";
        public static final String DURATION = "duration";
        public static final String TRAILER_VIDEO = "trailer_video";
        public static final String TRAILER_SUBTITLE = "trailer_subtitle";
        public static final String RATINGS = "ratings";
        public static final String WATCH_COUNT = "watch_count";
        public static final String IS_PPV = "is_pay_per_view";
        public static final String VIDEO_TYPE = "video_type";
        public static final String IS_KIDS = "is_kids_video";
        public static final String DOWNLOAD_STATUS = "download_status";
        public static final String DETAILS = "details";
        public static final String WISHLIST_STATUS = "wishlist_status";
        public static final String HISTORY_STATUS = "history_status";
        public static final String IS_LIKED = "is_liked";
        public static final String LIKES = "likes";
        public static final String CAST_CREW = "cast_crews";
        public static final String CAST_CREW_ID = "cast_crew_id";
        public static final String SEARCH_TERM = "key";
        public static final String LIKE_COUNT = "like_count";
        public static final String UN_LIKE_COUNT = "dislike_count";
        public static final String SHARE_LINK = "share_link";
        public static final String HISTORY_ID = "history_id";
        public static final String WISHLIST_ID = "wishlist_id";
        public static final String VALUE = "value";
        public static final String REASON = "reason";
        public static final String REMAINING_AMT = "remaining_amount";
        public static final String SERIES = "SERIES";
        public static final String HOME = "HOME";
        public static final String FLIMS = "FLIMS";
        public static final String CATEGORY = "CATEGORY";
        public static final String GENRES = "genres";
        public static final String GENRE_VIDEOS = "genre_videos";
        public static final String IS_SELECTED = "is_selected";
        public static final String IS_SEASON_VIDEO = "is_series";
        public static final String TRAILER_SECTION = "trailer_section";
        public static final String IMAGE = "image";
        public static final String RESOLUTIONS = "resolutions";
        public static final String MAIN_VIDEO_RESOLUTIONS = "main_video_resolutions";
        public static final String ORIGINAL = "original";
        public static final String URL_TYPE = "url_type";
        public static final String URL_PAGE_ID = "url_page_id";
        public static final String GENRE_NAME = "genre_name";
        public static final String DOWNLOAD_BUTTON_STATUS = "download_button_status";
        public static final String DOWNLOAD_URLS = "download_urls";
        public static final String TYPE = "type";
        public static final String LINK = "link";
        public static final String PPV_AMOUNT = "ppv_amount";
        public static final String PPV_PAGE_TYPE = "ppv_page_type";
        public static final String PPV_TYPE_CONTENT = "ppv_page_type_content";
        public static final String SEEK_HERE = "seek_time_in_seconds";
        public static final String DELETE_SUB_PROFILE = "delete_sub_profile_id";
        public static final String CLEAR_ALL_STATUS = "clear_all_status";
        public static final String COUNT = "count";
        public static final String IS_SPAM = "is_spam";
        public static final String BANNER_IMAGE = "banner_image";
        public static final String SHOULD_DISPLAY_PPV = "should_display_ppv";
        public static final String PRIVACY_POLICY = "privacy";
        public static final String TERMS = "terms";

        Params() {

        }
    }

    public static class STATIC_PAGES {
        public static final String TERMS = "terms";
        public static final String PRIVACY = "privacy";
        public static final String HELP = "help";
        public static final String ABOUT = "about";

        public static final String ABOUT_URL = "http://demo.streamhash.com/#/page/3";
        public static final String TERMS_URL = "http://demo.streamhash.com/#/page/7";
        public static final String HELP_URL = "http://demo.streamhash.com/#/page/5";
        public static final String SPEED_TEST_URL = "https://fast.com";
        public static final String PRIVACY_URL = "http://demo.streamhash.com/#/page/6";
    }


    //ADD try catch to all API calls
}
