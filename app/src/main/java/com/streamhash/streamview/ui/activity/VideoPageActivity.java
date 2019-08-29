package com.streamhash.streamview.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.streamhash.streamview.BuildConfig;
import com.streamhash.streamview.R;
import com.streamhash.streamview.model.Cast;
import com.streamhash.streamview.model.DownloadUrl;
import com.streamhash.streamview.model.GenreSeason;
import com.streamhash.streamview.model.Invoice;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.adapter.VideoListAdapter;
import com.streamhash.streamview.ui.adapter.VideoTileAdapter;
import com.streamhash.streamview.ui.fragment.bottomsheet.InvoiceBottomSheet;
import com.streamhash.streamview.ui.fragment.bottomsheet.PaymentBottomSheet;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.ParserUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.download.DownloadCompleteListener;
import com.streamhash.streamview.util.download.DownloadUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;
import static com.streamhash.streamview.network.APIConstants.Params.DATA;
import static com.streamhash.streamview.network.APIConstants.Params.ERROR_MESSAGE;
import static com.streamhash.streamview.network.APIConstants.Params.MESSAGE;
import static com.streamhash.streamview.network.APIConstants.Params.SUCCESS;
import static com.streamhash.streamview.ui.fragment.bottomsheet.PaymentBottomSheet.PAY_PAL_REQUEST_CODE;
import static com.streamhash.streamview.util.download.DownloadUtils.isValidVideoFile;
import static com.streamhash.streamview.util.download.DownloadUtils.isVideoFile;

public class VideoPageActivity extends BaseActivity implements DownloadCompleteListener,
        PaymentBottomSheet.PaymentsInterface,
        InvoiceBottomSheet.InvoiceInterface,
        VideoListAdapter.OnDataChangedListener {

    public static final String VIDEO_ID = "videoId";
    public static final String CANCELLED_OR_COMPLETED = "cancelledOrCompleted";
    public static final String ACTION_DOWNLOAD_UPDATE = BuildConfig.APPLICATION_ID + "ACTION_DOWNLOAD_UPDATE";

    @BindView(R.id.playVideoBtn)
    ImageView playVideoBtn;
    @BindView(R.id.seriesSpinner)
    Spinner seriesSpinner;
    @BindView(R.id.moreLikeThisRecycler)
    RecyclerView moreLikeThisRecycler;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.seriesLayout)
    ViewGroup seriesLayout;
    @BindView(R.id.trailersLayout)
    ViewGroup trailersLayout;
    @BindView(R.id.trailerRecycler)
    RecyclerView trailerRecycler;
    @BindView(R.id.noTrailers)
    View noTrailers;
    @BindView(R.id.videoThumbnail)
    ImageView videoThumbnail;
    @BindView(R.id.videoTitle)
    TextView videoTitle;
    @BindView(R.id.contextMenu)
    View contextMenu;
    @BindView(R.id.newOrOldText)
    TextView newOrOldText;
    @BindView(R.id.yearPublished)
    TextView yearPublished;
    @BindView(R.id.ageRestriction)
    TextView ageRestriction;
    @BindView(R.id.numSeasons)
    TextView numSeasons;
    @BindView(R.id.descHeader)
    TextView descHeader;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.starringText)
    TextView starringText;
    @BindView(R.id.myListToggle)
    TextView myListToggle;
    @BindView(R.id.wishListToggle)
    TextView wishListToggle;
    @BindView(R.id.noVideosForThisSeason)
    TextView noVideosForThisSeason;
    @BindView(R.id.shareBtn)
    TextView shareBtn;
    @BindView(R.id.videoContainer)
    LinearLayout videoContainer;
    @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.nestedScrollVideoPage)
    NestedScrollView nestedScrollVideoPage;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmer;
    @BindView(R.id.downloadingLayout)
    ViewGroup downloadingLayout;
    @BindView(R.id.downloadView)
    TextView downloadView;
    @BindView(R.id.playDownloadedView)
    TextView playDownloadedView;
    @BindView(R.id.downloadingView)
    LottieAnimationView downloadingView;
    @BindDrawable(R.drawable.ic_done_white_24dp)
    Drawable addedToWishListDrawable;
    @BindDrawable(R.drawable.ic_add_white_24dp)
    Drawable notAddedToWishListDrawable;
    @BindDrawable(R.drawable.liking)
    Drawable drawableLike;
    @BindDrawable(R.drawable.unliking)
    Drawable drawableUnlike;
    @BindView(R.id.payPerViewLayout)
    View payPerViewLayout;

    private Video video;
    private APIInterface apiInterface;
    private PrefUtils prefUtils;
    private VideoTileAdapter moreLikeThisAdapter;
    private VideoTileAdapter trailersAdapter;
    private VideoListAdapter seasonsAdapter;
    private TabLayout.Tab moreLikeThisTab;
    private TabLayout.Tab trailersTab;

    private Invoice invoice;

    /**
     * DS for showing season based/more like this videos
     */
    private ArrayList<Video> relatedVideos = new ArrayList<>();
    private View.OnClickListener needToSubscribeToDownloadListener = v -> new AlertDialog.Builder(this)
            .setTitle("Need Subscription")
            .setMessage("You need to subscribe to a plan to be able to download this video. Would you want to subscribe?")
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                Intent needPaymentIntent = new Intent(VideoPageActivity.this, PlansActivity.class);
                startActivity(needPaymentIntent);
            })
            .setNegativeButton(getString(R.string.no), null)
            .create().show();
    private View.OnClickListener downloadVideoListener = v -> {
        ArrayList<DownloadUrl> resolutions = video.getDownloadResolutions();
        if (resolutions.size() == 0)
            UiUtils.showShortToast(this, getString(R.string.no_suitable_download_urls));
        else if (resolutions.size() == 1)
            downloadVideo(resolutions.get(0).getUrl());
        else
            showDownloadResolutionDialogChooser(resolutions);
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getIntExtra(VIDEO_ID, 0) == video.getAdminVideoId()) {
                //true = completed, false= cancelled
                boolean isCancelledOrCompleted = intent.getBooleanExtra(VideoPageActivity.CANCELLED_OR_COMPLETED, false);
                if (isCancelledOrCompleted)
                    showPlayButton();
                else
                    showDownloadButton(downloadVideoListener);
            }
        }
    };
    private View.OnClickListener needToPayToDownloadListener = v -> new AlertDialog.Builder(this)
            .setTitle("Need payment")
            .setMessage("You need to pay for this video to be able to download it")
            .setIcon(R.mipmap.ic_launcher)
            .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            })
//            .setNegativeButton(getString(R.string.no), null)
            .create().show();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_page);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        prefUtils = PrefUtils.getInstance(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        //Tabs
        moreLikeThisTab = tabs.newTab().setText(getString(R.string.more_like_this));
        trailersTab = tabs.newTab().setText(getString(R.string.trailers_and_more));
        setUpTabView();
    }

    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(VideoPageActivity.ACTION_DOWNLOAD_UPDATE);
        registerReceiver(receiver, filter);
        super.onResume();
        loadVideo();
    }

    private void loadVideo() {
        Intent intent = getIntent();
        if (intent != null) {
            int adminVideoId = intent.getIntExtra(VIDEO_ID, -1);
            loadVideoData(adminVideoId);
            moreLikeThisTab.select();
        } else {
            UiUtils.showShortToast(this, getString(R.string.please_reopen));
            finish();
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
    }

    /**
     * Load video data from backend based on admin video subProfileIdUnderChange
     * Call loadGenreAndTrailers after you get successful response
     */
    private void loadVideoData(int adminVideoId) {
        shimmer.setVisibility(View.VISIBLE);
        nestedScrollVideoPage.setVisibility(View.GONE);
        Call<String> call = apiInterface.getVideoData(id, token, subProfileId, adminVideoId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                shimmer.setVisibility(View.GONE);
                nestedScrollVideoPage.setVisibility(View.VISIBLE);
                JSONObject videoResponse = null;
                try {
                    videoResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (videoResponse != null) {
                    if (videoResponse.optString(SUCCESS).equals(Constants.TRUE)) {
                        JSONObject data = videoResponse.optJSONObject(DATA);
                        video = ParserUtils.parseVideoData(data);
                        showVideoData();

                        //getVideo related data
                        loadGenreAndTrailers(adminVideoId);
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, videoResponse.optString(ERROR_MESSAGE));
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                shimmer.setVisibility(View.GONE);
                nestedScrollVideoPage.setVisibility(View.VISIBLE);
                finish();
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }

    /**
     * Loads trailers and More LIke this video / Season based videos with a spinner
     * Set a spinner after getting response based on whether video is Season based
     */
    private void loadGenreAndTrailers(int adminVideoId) {
        Call<String> call = apiInterface.getVideoRelatedData(id, token, subProfileId, adminVideoId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                JSONObject genreTrailerResponse = null;
                try {
                    genreTrailerResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (genreTrailerResponse != null) {
                    if (genreTrailerResponse.optString(SUCCESS).equals(Constants.TRUE)) {
                        JSONObject data = genreTrailerResponse.optJSONObject(DATA);
                        ParserUtils.parseVideoRelatedData(video, data);
                        parseAndShowGenres();
                        parseAndShowTrailers();

                        showMoreLikeThis();

                        seriesLayout.setVisibility(View.VISIBLE);
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, genreTrailerResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }

    /**
     * Setup tab views and add both tabs + click listeners
     */
    private void setUpTabView() {
        tabs.addTab(moreLikeThisTab);
        tabs.addTab(trailersTab);

        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        showMoreLikeThis();
                        break;
                    case 1:
                        showTrailers();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * Set adapter and Show trailer data
     */
    private void parseAndShowTrailers() {
        trailersAdapter = new VideoTileAdapter(this, video.getTrailerVideos(), VideoTileAdapter.VIDEO_SECTION_TYPE_NORMAL, true);
        trailerRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        trailerRecycler.setHasFixedSize(true);
        trailerRecycler.setAdapter(trailersAdapter);
        boolean isEmpty = video.getTrailerVideos().size() == 0;
        trailerRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        noTrailers.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    /**
     * Set moreLikethis adapater and show More like this/ Season based videos based on click of the spinner
     */
    private void parseAndShowGenres() {
        if (video.isSeasonVideo()) {
            seasonsAdapter = new VideoListAdapter(this, relatedVideos, VideoListAdapter.VideoListType.TYPE_SEASONS, this);
            moreLikeThisRecycler.setLayoutManager(new LinearLayoutManager(this));
            moreLikeThisRecycler.setHasFixedSize(true);
            moreLikeThisRecycler.setAdapter(seasonsAdapter);
        } else {
            moreLikeThisAdapter = new VideoTileAdapter(this, relatedVideos, VideoTileAdapter.VIDEO_SECTION_TYPE_NORMAL, true);
            moreLikeThisRecycler.setLayoutManager(new GridLayoutManager(this, 3));
            moreLikeThisRecycler.setHasFixedSize(true);
            moreLikeThisRecycler.setAdapter(moreLikeThisAdapter);
        }

        boolean spinnerRequired = video.isSeasonVideo() && video.getGenreSeasons().size() > 0;
        seriesSpinner.setVisibility(spinnerRequired ? View.VISIBLE : View.GONE);
        moreLikeThisTab.setText(video.isSeasonVideo() ? getString(R.string.browse_seasons) : getString(R.string.more_like_this));

        //Get videos
        if (video.isSeasonVideo()) {
            if (spinnerRequired) {
                int selectedPos = 0;

                //Get the seasons as a string list, Set this data to Spinner through adapter,
                ArrayList<GenreSeason> genreSeasons = video.getGenreSeasons();
                ArrayList<String> seasons = new ArrayList<>();
                for (int i = 0; i < genreSeasons.size(); i++) {
                    if (video.getSeasonId() == genreSeasons.get(i).getId())
                        selectedPos = i;
                    seasons.add(genreSeasons.get(i).getName());
                }


                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>
                        (this, android.R.layout.simple_spinner_item, seasons.toArray(new String[0]));
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item);
                seriesSpinner.setAdapter(spinnerArrayAdapter);
                seriesSpinner.setSelection(selectedPos);

                //On Item click, Get the season at the click position and get videos of that Season from backend
                seriesSpinner.setOnItemSelectedListener((new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        try {
                            video.setSeasonId(video.getGenreSeasons().get(position).getId());
                            getVideosForGenre(video.getSeasonId(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }));

            }
            getVideosForGenre(video.getSeasonId(), 0);
        } else {
            video.setSeasonId(-1);
            getSuggestionVideosWith(0);
        }
    }

    //Get suggestion videos
    private void getSuggestionVideosWith(int skip) {
        Call<String> call = apiInterface.getSuggestionVideos(id, token, subProfileId,
                skip, 0, 0, 0, "");
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                JSONObject suggestionVideosResponse = null;
                try {
                    suggestionVideosResponse = new JSONObject(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (suggestionVideosResponse != null) {
                    ArrayList<Video> tempSuggestions = new ArrayList<>();
                    if (suggestionVideosResponse.optString(SUCCESS).equals(Constants.TRUE)) {
                        JSONArray data = suggestionVideosResponse.optJSONArray(DATA);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject videoObj;
                            try {
                                videoObj = data.getJSONObject(i);
                                Video video = ParserUtils.parseVideoData(videoObj);
                                tempSuggestions.add(video);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (skip == 0) {
                            relatedVideos.clear();
                            relatedVideos.addAll(tempSuggestions);
                        } else
                            relatedVideos.addAll(tempSuggestions);

                        onMoreLikeThisDataChanged();
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, suggestionVideosResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    /**
     * Show trailers in layout
     */
    private void showTrailers() {
        trailersLayout.setVisibility(View.VISIBLE);
        seriesLayout.setVisibility(View.GONE);
    }

    /**
     * Show More like this tab in layout
     */
    private void showMoreLikeThis() {
        trailersLayout.setVisibility(View.GONE);
        seriesLayout.setVisibility(View.VISIBLE);
    }

    /**
     * COde to change view when data chnages in first tab (MOre like this)
     */
    private void onMoreLikeThisDataChanged() {
        boolean isEmpty = relatedVideos.size() == 0;
        moreLikeThisRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        noVideosForThisSeason.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (video.isSeasonVideo()) {
            seasonsAdapter.notifyDataSetChanged();
        } else {
            moreLikeThisAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Backend API, Get the videos
     */
    private void getVideosForGenre(int seasonId, int skip) {
        video.setSeasonId(seasonId);

        ArrayList<Video> videosForSeason = new ArrayList<>();
        Call<String> call = apiInterface.getVideosForSeason(id, token, subProfileId,
                "SERIES",
                video.getSubCategoryId(),
                video.getAdminVideoId(),
                seasonId,
                skip);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                JSONObject seasonVideoResponse = null;
                try {
                    seasonVideoResponse = new JSONObject(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (seasonVideoResponse != null) {
                    if (seasonVideoResponse.optString(SUCCESS).equals(Constants.TRUE)) {
                        JSONArray data = seasonVideoResponse.optJSONArray(DATA);
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject videoObj;
                            try {
                                videoObj = data.getJSONObject(i);
                                Video video = ParserUtils.parseVideoData(videoObj);
                                videosForSeason.add(video);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (skip == 0)
                            relatedVideos.clear();

                        relatedVideos.addAll(videosForSeason);
                        onMoreLikeThisDataChanged();
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, seasonVideoResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }

    /**
     * Showing video data in the UI
     */
    private void showVideoData() {
        GlideApp.with(getApplicationContext())
                .load(video.getThumbNailUrl())
                .thumbnail(0.5f)
                .into(videoThumbnail);

        payPerViewLayout.setVisibility(video.isPayPerView() ? View.VISIBLE : View.GONE);

        videoTitle.setText(video.getTitle());
        videoTitle.setSelected(true);
        newOrOldText.setText(MessageFormat.format("{0} Views", video.getViewCount()));
        yearPublished.setText(video.getPublishTime());
        ageRestriction.setText(MessageFormat.format("{0}+", video.getAge()));
        numSeasons.setText(MessageFormat.format("{0} likes", video.getLikes()));
        descHeader.setText(video.getDetail());
        description.setText(video.getDescription());
        myListToggle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, video.isInWishList()
                ? addedToWishListDrawable : notAddedToWishListDrawable, null, null);

        starringText.setText(getSpannableCastAndCrew(video.getCasts()), TextView.BufferType.SPANNABLE);
        starringText.setHighlightColor(Color.TRANSPARENT);
        starringText.setMovementMethod(LinkMovementMethod.getInstance());

        wishListToggle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, video.isLiked()
                ? drawableLike : drawableUnlike, null, null);

        showDownloadStatusInViews();
    }

    private void showDownloadStatusInViews() {
        if (video.isDownloadable()) {
            switch (video.getDownloadStatus()) {
                case DO_NOT_SHOW_DOWNLOAD:
                    showNotDownload();
                    break;
                case DOWNLOAD_PROGRESS:
                    showDownloadingButton();
                    break;
                case DOWNLOAD_COMPLETED:
                    showPlayButton();
                    break;
                case SHOW_DOWNLOAD:
                    showDownloadButton(downloadVideoListener);
                    break;
                case NEED_TO_SUBSCRIBE:
                    showDownloadButton(needToSubscribeToDownloadListener);
                    break;
                case NEED_TO_PAY:
                    showDownloadButton(needToPayToDownloadListener);
                    break;
            }
        } else {
            showNotDownload();
        }
    }

    /**
     * Show resolution chooser dialog
     */
    private void showDownloadResolutionDialogChooser(ArrayList<DownloadUrl> downloadUrls) {
        ArrayList<String> resolutionNames = new ArrayList<>();
        for (int i = 0; i < downloadUrls.size(); i++) {
            resolutionNames.add(downloadUrls.get(i).getTitle());
        }
        String[] resolutions = resolutionNames.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_download_quality))
                .setSingleChoiceItems(resolutions, 0, (dialog, which) -> {
                    downloadVideo(downloadUrls.get(which).getUrl());
                    dialog.dismiss();
                })
                .setIcon(R.mipmap.ic_launcher)
                .create().show();
    }

    private void downloadVideo(String downloadUrl) {
        Toast.makeText(this, downloadUrl, Toast.LENGTH_SHORT).show();
        NetworkUtils.downloadVideo(this, video.getAdminVideoId(), video.getTitle(), downloadUrl);
        downloadView.setVisibility(View.GONE);
        downloadingView.setVisibility(View.VISIBLE);
    }

    /**
     * Get click spannable text for cast and crew
     */
    private SpannableString getSpannableCastAndCrew(ArrayList<Cast> casts) {
        starringText.setVisibility(casts.size() == 0 ? View.GONE : View.VISIBLE);
        if (casts.size() == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder(getString(R.string.cast) + ": ");
        builder.append(casts.get(0).getName());
        for (int i = 1; i < casts.size(); i++) {
            builder.append(", ")
                    .append(casts.get(i).getName());
        }

        String castsString = builder.toString();
        SpannableString castStr = new SpannableString(castsString);
        for (int i = 0; i < casts.size(); i++) {
            Cast cast = casts.get(i);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Intent toCast = new Intent(VideoPageActivity.this, CastVideosActivity.class);
                    toCast.putExtra(CastVideosActivity.CAST_CREW_ID, cast.getId());
                    toCast.putExtra(CastVideosActivity.CAST_CREW_NAME, cast.getName());
                    startActivity(toCast);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            int index = castsString.indexOf(cast.getName());
            int len = cast.getName().length();
            castStr.setSpan(clickableSpan, index, index + len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return castStr;
    }


    @OnClick({R.id.myListToggle, R.id.wishListToggle, R.id.shareBtn})
    protected void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.myListToggle:
                toggleWishList(video.isInWishList());
                break;

            case R.id.wishListToggle:
                toggleLikeVideo(video.isLiked());
                break;

            case R.id.shareBtn:
                shareVideoButton();
                break;
        }
    }

    /**
     * Backend toggle wishlist status
     */
    private void toggleWishList(boolean inWishList) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.toggleWishList(
                id
                , token
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , video.getAdminVideoId()
                , inWishList ? 0 : 1);
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
                        myListToggle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, video.isInWishList()
                                ? addedToWishListDrawable : notAddedToWishListDrawable, null, null);
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, toggleWishListStatus.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }

    /**
     * Share video info through text
     */
    private void shareVideoButton() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this video: " + video.getTitle()
                + "\n\n" + video.getShareUrl());
        startActivity(shareIntent);
    }

    /**
     * Backend toggle like status and update the like count
     */
    private void toggleLikeVideo(boolean liked) {
        UiUtils.showLoadingDialog(this);
        Call<String> call;
        if (liked)
            call = apiInterface.unLikeVideo(
                    id
                    , token
                    , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                    , video.getAdminVideoId());
        else
            call = apiInterface.likeVideo(
                    id
                    , token
                    , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                    , video.getAdminVideoId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject likeUnlikeResponse = null;
                try {
                    likeUnlikeResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (likeUnlikeResponse != null) {
                    if (likeUnlikeResponse.optString(SUCCESS).equals(Constants.TRUE)) {
                        video.setLiked(!video.isLiked());
                        long likeCount = likeUnlikeResponse.optLong(Params.LIKE_COUNT);
                        video.setLikes(likeCount);
                        numSeasons.setText(MessageFormat.format("{0} likes", video.getLikes()));
                        wishListToggle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, video.isLiked()
                                ? drawableLike : drawableUnlike, null, null);
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, likeUnlikeResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }

    @OnClick(R.id.playVideoBtn)
    public void onVideoPlayClicked() {
        try {
            switch (video.getVideoType()) {
                case VIDEO_MANUAL:
                case VIDEO_OTHER:
                    if (video.isPayPerView()) {
                        switch (video.getPayPerViewType()) {
                            case ONE:
                                showPPVSheet();
                                break;
                            case TWO:
                                choosePlanDialog();
                                break;
                        }
                    } else {
                        Intent toPlayer = new Intent(this, PlayerActivity.class);
                        toPlayer.putExtra(PlayerActivity.VIDEO_ID, video.getAdminVideoId());
                        toPlayer.putExtra(PlayerActivity.VIDEO_URL, video.getVideoUrl());
                        toPlayer.putExtra(PlayerActivity.VIDEO_ELAPSED, video.getSeekHere());
                        toPlayer.putExtra(PlayerActivity.VIDEO_SUBTITLE, video.getSubTitleUrl());
                        this.startActivity(toPlayer);
                    }
                    break;
                case VIDEO_YOUTUBE:
                    if (video.isPayPerView()) {
                        switch (video.getPayPerViewType()) {
                            case ONE:
                                showPPVSheet();
                                break;

                            case TWO:
                                choosePlanDialog();
                        }
                    } else {
                        Intent toYouTubePlayer = new Intent(this, YouTubePlayerActivity.class);
                        toYouTubePlayer.putExtra(YouTubePlayerActivity.VIDEO_ID, video.getAdminVideoId());
                        toYouTubePlayer.putExtra(YouTubePlayerActivity.VIDEO_URL, video.getVideoUrl());
                        this.startActivity(toYouTubePlayer);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPPVSheet() {
        PaymentBottomSheet paymentBottomSheet = new PaymentBottomSheet();
        if (paymentBottomSheet.setPaymentsInterface(this, null, video)) {
            UiUtils.showShortToast(this, getString(R.string.something_went_wrong));
        } else {
            paymentBottomSheet.show(getSupportFragmentManager(), paymentBottomSheet.getTag());
        }
    }

    @OnClick(R.id.contextMenu)
    protected void onContextMenuClicked() {
        final ClipboardManager manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        PopupMenu popupMenu = new PopupMenu(this, contextMenu);
        popupMenu.getMenuInflater().inflate(R.menu.popup_single_video, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.embedlink:
                    ClipData data = ClipData.newPlainText("Embedded Link", video.getShareUrl());
                    manager.setPrimaryClip(data);
                    UiUtils.showShortToast(VideoPageActivity.this, getString(R.string.copy_clipboard));
                    return true;
                case R.id.report:
                    showSpamDialog();
                    return true;
            }
            return false;
        });
        popupMenu.show();
    }

    /**
     * Show spam dialog by getting reasons and do call <code>addToSpamInBackend</code>.
     */
    private void showSpamDialog() {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.getSpamReasons();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject spamReasonsResponse = null;
                try {
                    spamReasonsResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (spamReasonsResponse != null) {
                    if (spamReasonsResponse.optString(SUCCESS).equals(Constants.TRUE)) {
                        JSONArray data = spamReasonsResponse.optJSONArray(DATA);
                        CharSequence[] reasons = null;
                        if (data != null && data.length() > 0) {
                            reasons = new CharSequence[data.length()];
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.optJSONObject(i);
                                reasons[i] = obj.optString(Params.VALUE);
                            }
                        }
                        if (reasons != null && reasons.length > 0) {
                            CharSequence[] finalReasons = reasons;
                            new AlertDialog.Builder(VideoPageActivity.this)
                                    .setTitle(getString(R.string.reason_to_report))
                                    .setItems(reasons, (dialogInterface, i) -> addToSpamInBackend(finalReasons[i].toString()))
                                    .create().show();
                        } else {
                            addToSpamInBackend("Nothing");
                        }
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, spamReasonsResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }


    /**
     * Backend add video to spam API with Spam reason obtained from reasons dialog
     */
    private void addToSpamInBackend(String spamReason) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.addToSpam(id
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , video.getAdminVideoId()
                , spamReason);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject addToSpamResponse = null;
                try {
                    addToSpamResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (addToSpamResponse != null) {
                    if (addToSpamResponse.optString(SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(VideoPageActivity.this, addToSpamResponse.optString(MESSAGE));
                        finish();
                    } else {
                        UiUtils.showShortToast(VideoPageActivity.this, addToSpamResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }

    /**
     * Use this to show download button along with listeners
     */
    public void showDownloadButton(View.OnClickListener listener) {
        downloadView.setVisibility(View.VISIBLE);
        downloadingView.setVisibility(View.GONE);
        playDownloadedView.setVisibility(View.GONE);
        downloadView.setOnClickListener(listener);
    }

    @Override
    public void onPaymentSucceeded(Invoice invoice) {
        invoice.setStatus(getString(R.string.success));
        InvoiceBottomSheet invoiceSheet = InvoiceBottomSheet.getInstance(invoice);
        invoiceSheet.show(getSupportFragmentManager(), invoiceSheet.getTag());
        loadVideo();
    }

    @Override
    public void onPaymentFailed(String failureReason) {
        UiUtils.showShortToast(this, failureReason);
    }

    /**
     * Use this to show play button along with listeners
     */
    public void showPlayButton() {
        downloadView.setVisibility(View.GONE);
        downloadingView.setVisibility(View.GONE);
        playDownloadedView.setVisibility(View.VISIBLE);
        playDownloadedView.setOnClickListener(v -> playVideoInOffline());
    }

    private void playVideoInOffline() {
        try {
            String fileUri = DownloadUtils.getVideoPath(this, video.getAdminVideoId(), video.getVideoUrl());
            Uri videoUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".myprovider", new File(fileUri));
            if (isValidVideoFile(this, videoUri) && isVideoFile(video.getVideoUrl())) {
                Intent toVideo = new Intent(this, PlayerActivity.class);
                toVideo.putExtra(PlayerActivity.VIDEO_ID, video.getAdminVideoId());
                toVideo.putExtra(PlayerActivity.VIDEO_URL, fileUri);
                toVideo.putExtra(PlayerActivity.VIDEO_ELAPSED, video.getSeekHere());
                toVideo.putExtra(PlayerActivity.VIDEO_SUBTITLE, video.getSubTitleUrl());
                startActivity(toVideo);
            } else {
                UiUtils.showShortToast(this, getString(R.string.problem_with_downloaded_video));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Use this to not show download button
     */
    private void showNotDownload() {
        downloadView.setVisibility(View.GONE);
        downloadingView.setVisibility(View.GONE);
        playDownloadedView.setVisibility(View.GONE);
    }

    /**
     * Use this to show downloading button along with listeners
     */
    public void showDownloadingButton() {
        downloadingView.setVisibility(View.VISIBLE);
        downloadView.setVisibility(View.GONE);
        playDownloadedView.setVisibility(View.GONE);
    }

    @Override
    public void downloadCompleted(int adminVideoId) {
        showPlayButton();
    }

    @Override
    public void downloadCancelled(int adminVideoId) {
        showDownloadStatusInViews();
    }

    public void choosePlanDialog() {
        Dialog dialog = new Dialog(VideoPageActivity.this, R.style.AppTheme_NoActionBar);
        dialog.setContentView(R.layout.choose_payment_dialog);
        CardView ppv = dialog.findViewById(R.id.ppvLayout);
        CardView subLayout = dialog.findViewById(R.id.subLayout);
        TextView ppvTitle = dialog.findViewById(R.id.ppvTitle);
        TextView ppvDesc = dialog.findViewById(R.id.ppvDesc);
        TextView subTitle = dialog.findViewById(R.id.subTitle);
        TextView subDesc = dialog.findViewById(R.id.subDesc);

        ppvTitle.setText(video.getChoosePlans().get(0).getName());
        ppvDesc.setText(video.getChoosePlans().get(0).getDesc());
        subTitle.setText(video.getChoosePlans().get(1).getName());
        subDesc.setText(video.getChoosePlans().get(1).getDesc());

        ppv.setOnClickListener(view -> {
            dialog.dismiss();
            showPPVSheet();
        });

        subLayout.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), PlansActivity.class);
            startActivity(i);
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String paymentId;
        if (requestCode == PAY_PAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Timber.d(paymentDetails);
                        JSONObject details = new JSONObject(paymentDetails);
                        paymentId = details.optJSONObject("response").optString("id");
                        sendPayPalPaymentToBackend(paymentId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                onPaymentFailed(getString(R.string.something_went_wrong));
            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            onPaymentFailed(getString(R.string.invalid_payment));
        }
    }

    private void sendPayPalPaymentToBackend(String paymentId) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.makePayPalPPV(id, token, subProfileId
                , invoice.getVideo().getAdminVideoId()
                , paymentId
                , invoice.getCouponCode());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject paymentObject = null;
                try {
                    paymentObject = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (paymentObject != null) {
                    if (paymentObject.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        Toast.makeText(VideoPageActivity.this, paymentObject.optString("message"), Toast.LENGTH_SHORT).show();
                        invoice.setPaymentId(paymentId);
                        onPaymentSucceeded(invoice);
                    } else {
                        UiUtils.hideLoadingDialog();
                        Toast.makeText(VideoPageActivity.this, paymentObject.optString("error_messages"), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(VideoPageActivity.this);
            }
        });
    }

    @Override
    public void onMakePayPalPayment(Invoice invoice) {
        this.invoice = invoice;
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(invoice.getPaidAmount())), invoice.getCurrencySymbol(), invoice.getTitle(),
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PaymentBottomSheet.config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAY_PAL_REQUEST_CODE);
    }

    @Override
    public void playVideo(Video video) {
        video.setPayPerView(false);
        onVideoPlayClicked();
    }

    @Override
    public void onDataChanged() {

    }
}
