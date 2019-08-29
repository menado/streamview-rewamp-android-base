package com.streamhash.streamview.ui.fragment;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.streamhash.streamview.R;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.model.VideoSection;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.activity.MainActivity;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.ui.adapter.BannerAdapter;
import com.streamhash.streamview.ui.adapter.VideoSectionsAdapter;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.ParserUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.model.Video.VideoType.VIDEO_YOUTUBE;
import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;
import static com.streamhash.streamview.network.APIConstants.Params.ERROR_MESSAGE;
import static com.streamhash.streamview.network.APIConstants.Params.SUCCESS;
import static com.streamhash.streamview.util.Fragments.HOME_FRAGMENTS;
import static com.streamhash.streamview.util.ParserUtils.parseVideoSections;

public class VideoContentFragment extends Fragment implements VideoSectionsAdapter.VideoSectionsInterface {

    public static final String TYPE_HOME = "HOME";
    public static final String TYPE_CATEGORY = "CATEGORY";
    public static final String TYPE_SUB_CATEGORY = "SUB_CATEGORY";
    public static final String TYPE_GENRE = "GENRE";
    public static final String TYPE_KIDS = "KIDS";
    public static final String PAGE_TYPE = "pageType";
    public static final String TYPE_FILMS = "FILMS";
    public static final String TYPE_SERIES = "SERIES";
    public static final String CATEGORY_ID = "categoryId";
    public static final String SUB_CATEGORY_ID = "subCategoryId";
    public static final String GENRE_ID = "genreId";
    public static final String TITLE = "title";
    public static String page_type, category_id;
    MainActivity activity;
    Unbinder unbinder;
    BannerAdapter bannerAdapter;
    VideoSectionsAdapter videoSectionAdapter;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.homeToolbar)
    Toolbar homeToolbar;
    @BindView(R.id.category_toolbar)
    Toolbar categoryToolbar;
    @BindView(R.id.recyclerView)
    RecyclerView videoSectionsRecycler;
    @BindView(R.id.nestedScrollView)
    NestedScrollView nestedScrollView;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmer;
    @BindView(R.id.addToMyList)
    TextView addToMyList;
    @BindView(R.id.app_header_icon)
    View appHeaderIcon;
    @BindView(R.id.bannerLayout)
    ViewGroup bannerLayout;
    @BindView(R.id.contentView)
    ViewGroup contentView;
    @BindView(R.id.series)
    TextView series;
    @BindView(R.id.films)
    TextView films;
    @BindView(R.id.kid)
    TextView kid;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.view)
    View viewLayout;

    @BindDrawable(R.drawable.ic_done_white_24dp)
    Drawable addedToWishListDrawable;
    @BindDrawable(R.drawable.ic_add_white_24dp)
    Drawable notAddedToWishListDrawable;

    private APIInterface apiInterface;
    private PrefUtils prefUtils;
    private ArrayList<Video> bannerVideos = new ArrayList<>();
    private ArrayList<VideoSection> videoSections = new ArrayList<>();
    private RecyclerView.OnScrollListener videoSectionsScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager == null) return;
            if (layoutManager.findLastVisibleItemPosition() == (videoSections.size() - 1)) {
                videoSectionAdapter.showLoading();
            }
        }
    };
    private String pageType;
    private int categoryId;
    private int subCategoryId;
    private int genreId;

    public static VideoContentFragment getInstance(String pageType, int categoryId, int subCategoryId, int genreId) {
        VideoContentFragment videoContentFragment = new VideoContentFragment();
        page_type = pageType;
        category_id = String.valueOf(categoryId);
        Bundle bundle = new Bundle();
        bundle.putString(VideoContentFragment.PAGE_TYPE, pageType);
        bundle.putInt(VideoContentFragment.CATEGORY_ID, categoryId);
        bundle.putInt(VideoContentFragment.SUB_CATEGORY_ID, subCategoryId);
        bundle.putInt(VideoContentFragment.GENRE_ID, genreId);
        videoContentFragment.setArguments(bundle);
        return videoContentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        prefUtils = PrefUtils.getInstance(getActivity());
        homeToolbar.getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryToolbar.setVisibility(View.GONE);
        homeToolbar.setVisibility(View.VISIBLE);
        Bundle bundle = getArguments();
        appHeaderIcon.setOnClickListener(null);
        if (bundle != null) {
            switch (bundle.getString(PAGE_TYPE, TYPE_HOME)) {
                case TYPE_SERIES:
                    series.setVisibility(View.VISIBLE);
                    appHeaderIcon.setOnClickListener(v -> onActivityBackPressed());
                    break;
                case TYPE_FILMS:
                    films.setVisibility(View.VISIBLE);
                    appHeaderIcon.setOnClickListener(v -> onActivityBackPressed());
                    break;
                case TYPE_KIDS:
                    kid.setVisibility(View.VISIBLE);
                    appHeaderIcon.setOnClickListener(v -> onActivityBackPressed());
                    break;
                case TYPE_CATEGORY:
                    homeToolbar.setVisibility(View.GONE);
                    categoryToolbar.setVisibility(View.VISIBLE);
                    categoryToolbar.setTitle(CategoryFragment.categoryBeingViewed);
                    categoryToolbar.setNavigationOnClickListener(v -> onActivityBackPressed());
                    homeToolbar.setVisibility(View.GONE);
                    viewLayout.setVisibility(View.GONE);
                    break;
                default:
                    kid.setVisibility(View.VISIBLE);
                    films.setVisibility(View.VISIBLE);
                    series.setVisibility(View.VISIBLE);
                    break;
            }
        }


        if (bundle != null) {
            pageType = bundle.getString(PAGE_TYPE);
            categoryId = bundle.getInt(CATEGORY_ID);
            subCategoryId = bundle.getInt(SUB_CATEGORY_ID);
            genreId = bundle.getInt(GENRE_ID);
            getVideoSections();
        }
    }

    private void onActivityBackPressed() {
        if (getActivity() != null)
            getActivity().onBackPressed();
    }

    //Banner wishListAdapter
    private void setUpBannerItems(JSONObject bannerObj) {
        if (bannerObj != null) {
            JSONArray data = bannerObj.optJSONArray(Params.DATA);
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    try {
                        JSONObject bannerVideoObj = data.getJSONObject(i);
                        Video bannerVideo = new Video();
                        bannerVideo.setThumbNailUrl(bannerVideoObj.optString(Params.BANNER_IMAGE));
                        bannerVideo.setTitle(bannerVideoObj.optString(Params.TITLE));
                        bannerVideo.setAdminVideoId(bannerVideoObj.optInt(Params.ADMIN_VIDEO_ID));
                        bannerVideo.setCategoryId(bannerVideoObj.optInt(Params.CATEGORY_ID));
                        bannerVideo.setSubCategoryId(bannerVideoObj.optInt(Params.SUB_CATEGORY_ID));
                        bannerVideo.setInWishList(bannerVideoObj.optInt(Params.WISHLIST_STATUS) == 1);
                        bannerVideo.setGenreId(bannerVideoObj.optInt(Params.GENRE_ID));
                        bannerVideo.setVideoType(VIDEO_YOUTUBE);
                        bannerVideos.add(bannerVideo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        bannerAdapter = new BannerAdapter(activity, bannerVideos);
        if (bannerVideos.size() > 0)
            addToMyList.setCompoundDrawablesRelativeWithIntrinsicBounds(null, bannerVideos.get(0).isInWishList()
                    ? addedToWishListDrawable : notAddedToWishListDrawable, null, null);
        viewPager.setAdapter(bannerAdapter);
        bannerLayout.setVisibility(bannerVideos.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /**
     * Method to get the videos for the fragment in appropriate screens
     */
    private void getVideoSections() {
        shimmer.startShimmer();
        shimmer.setVisibility(View.VISIBLE);
        noResultLayout.setVisibility(View.GONE);

        contentView.setVisibility(View.GONE);
        Call<String> call = apiInterface.getVideoContentFor(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , pageType
                , categoryId
                , subCategoryId
                , genreId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!isAdded()) return;

                shimmer.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);
                JSONObject homeResponse = null;
                try {
                    homeResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (homeResponse != null) {
                    videoSections.clear();
                    if (homeResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        parseContentAndSetDisplay(homeResponse);
                    } else {
                        UiUtils.showShortToast(activity, homeResponse.optString(Params.ERROR_MESSAGE));
                    }
                }

                //Show empty
                if (bannerVideos.isEmpty() && videoSections.isEmpty()) {
                    contentView.setVisibility(View.GONE);
                    noResultLayout.setVisibility(View.VISIBLE);
                } else {
                    contentView.setVisibility(View.VISIBLE);
                    noResultLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (!isAdded()) return;
                contentView.setVisibility(View.VISIBLE);
                shimmer.setVisibility(View.GONE);
                NetworkUtils.onApiError(activity);
            }
        });

    }

    private void getVideoSectionsDynamicWith(int skip) {
        Call<String> call = apiInterface.getVideoContentDynamicFor(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , pageType, categoryId, subCategoryId, genreId, skip);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    videoSectionAdapter.dismissLoading();
                    JSONObject homeDynamicResponse = null;
                    try {
                        homeDynamicResponse = new JSONObject(response.body());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (homeDynamicResponse != null) {
                        if (homeDynamicResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                            JSONArray dynamicVideoArr = homeDynamicResponse.optJSONArray(Params.DATA);
                            parseAndAddVideoSections(dynamicVideoArr);
                            //Should be after adding because scroll listener should only act on adding first set of dynamic content
                            if (skip == 0)
                                videoSectionsRecycler.addOnScrollListener(videoSectionsScrollListener);
                            if (dynamicVideoArr != null && dynamicVideoArr.length() == 0)
                                videoSectionsRecycler.removeOnScrollListener(videoSectionsScrollListener);
                        } else {
                            UiUtils.showShortToast(activity, homeDynamicResponse.optString(Params.ERROR_MESSAGE));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(activity);
            }
        });
    }

    private void parseContentAndSetDisplay(JSONObject videoResponseObj) {
        bannerVideos.clear();

        //Banner items
        JSONObject bannerObj = videoResponseObj.optJSONObject(Params.BANNER);
        setUpBannerItems(bannerObj);

        //Video sections
        //Set wishListAdapter
        videoSectionAdapter = new VideoSectionsAdapter(activity, this, videoSections, page_type, category_id);
        videoSectionsRecycler.setLayoutManager(new LinearLayoutManager(activity));
        videoSectionsRecycler.setItemAnimator(new DefaultItemAnimator());
        videoSectionsRecycler.setAdapter(videoSectionAdapter);
        JSONArray data = videoResponseObj.optJSONArray(Params.DATA);
        parseAndAddVideoSections(data);

        JSONObject originals = videoResponseObj.optJSONObject(Params.ORIGINALS);
        parseAndAddOriginalVideoSections(originals);

        //Dynamic stuff
        videoSectionAdapter.setDynamicSectionPadding();
        videoSectionsRecycler.setNestedScrollingEnabled(false);
//        videoSectionAdapter.showLoading();

        getVideoSectionsDynamicWith(0);
    }

    private void parseAndAddVideoSections(JSONArray normalSections) {
        //Setup Normal sections
        if (normalSections != null) {
            ArrayList<VideoSection> videoSection = parseVideoSections(normalSections);
            videoSections.addAll(videoSection);
            videoSectionAdapter.notifyDataSetChanged();
        }
    }

    private void parseAndAddOriginalVideoSections(JSONObject originals) {
        //Setup Originals sections
        VideoSection originalsSection = ParserUtils.parseOriginalsVideos(originals);
        if (originalsSection != null) {
            videoSections.add(originalsSection);
            videoSectionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.addToMyList, R.id.playBannerBtn, R.id.bannerInfoText, R.id.series, R.id.films, R.id.kid})
    public void onViewClicked(View view) {
        Video video = null;
        if (bannerVideos.size() > 0)
            video = bannerVideos.get(0);
        switch (view.getId()) {
            case R.id.addToMyList:
                if (video == null) return;
                addBannerItemToMyList(video);
                break;
            case R.id.playBannerBtn:
                if (video == null) return;
                /*switch (video.getVideoType()) {
                    case VIDEO_MANUAL:
                    case VIDEO_OTHER:
                        Intent toPlayer = new Intent(activity, PlayerActivity.class);
                        toPlayer.putExtra(PlayerActivity.VIDEO_ID, video.getAdminVideoId());
                        toPlayer.putExtra(PlayerActivity.VIDEO_URL, video.getVideoUrl());
                        toPlayer.putExtra(PlayerActivity.VIDEO_SUBTITLE, video.getSubTitleUrl());
                        activity.startActivity(toPlayer);
                        break;
                    case VIDEO_YOUTUBE:
                        Intent toYouTubePlayer = new Intent(activity, YouTubePlayerActivity.class);
                        toYouTubePlayer.putExtra(YouTubePlayerActivity.VIDEO_ID, video.getAdminVideoId());
                        toYouTubePlayer.putExtra(YouTubePlayerActivity.VIDEO_URL, video.getVideoUrl());
                        activity.startActivity(toYouTubePlayer);
                        break;
                }
                break;*/
            case R.id.bannerInfoText:
                if (video == null) return;
                Intent toVideo = new Intent(activity, VideoPageActivity.class);
                toVideo.putExtra(VideoPageActivity.VIDEO_ID, video.getAdminVideoId());
                activity.startActivity(toVideo);
                break;
            case R.id.series:
                VideoContentFragment contentFragment = VideoContentFragment.getInstance(VideoContentFragment.TYPE_SERIES
                        , 0
                        , 0
                        , 0);
                replaceFragmentWithAnimation(contentFragment, HOME_FRAGMENTS[5], false);
                break;
            case R.id.films:
                contentFragment = VideoContentFragment.getInstance(VideoContentFragment.TYPE_FILMS
                        , 0
                        , 0
                        , 0);
                replaceFragmentWithAnimation(contentFragment, HOME_FRAGMENTS[6], false);
                break;
            case R.id.kid:
                contentFragment = VideoContentFragment.getInstance(VideoContentFragment.TYPE_KIDS
                        , 0
                        , 0
                        , 0);
                replaceFragmentWithAnimation(contentFragment, HOME_FRAGMENTS[7], false);
                break;
        }
    }

    private void addBannerItemToMyList(Video video) {
        toggleWishList(video);
    }

    private void toggleWishList(Video video) {
        UiUtils.showLoadingDialog(activity);
        Call<String> call = apiInterface.toggleWishList(
                prefUtils.getIntValue(PrefKeys.USER_ID, 0)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , video.getAdminVideoId()
                , video.isInWishList() ? 0 : 1);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject toggleWishListStatus = null;
                try {
                    toggleWishListStatus = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (toggleWishListStatus != null) {
                    if (toggleWishListStatus.optString(SUCCESS).equals(Constants.TRUE)) {
                        video.setInWishList(!video.isInWishList());
                        addToMyList.setCompoundDrawablesRelativeWithIntrinsicBounds(null, video.isInWishList()
                                ? addedToWishListDrawable : notAddedToWishListDrawable, null, null);
                    } else {
                        UiUtils.showShortToast(activity, toggleWishListStatus.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(activity);
            }
        });
    }

    public void replaceFragmentWithAnimation(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        MainActivity.CURRENT_FRAGMENT = tag;
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onLoadMore(int skip) {
        getVideoSectionsDynamicWith(skip);
    }
}
