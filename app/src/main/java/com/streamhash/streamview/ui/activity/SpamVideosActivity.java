package com.streamhash.streamview.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.adapter.VideoListAdapter;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;
import com.streamhash.streamview.util.UiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Params.ERROR_MESSAGE;
import static com.streamhash.streamview.network.APIConstants.Params.SUCCESS;
import static com.streamhash.streamview.ui.adapter.VideoListAdapter.*;

public class SpamVideosActivity extends BaseActivity implements OnLoadMoreVideosListener, SwipeRefreshLayout.OnRefreshListener, OnDataChangedListener {

    @BindView(R.id.spamRecycler)
    RecyclerView spamRecycler;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    APIInterface apiInterface;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.clearAll)
    TextView clearAll;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmerFrameLayout;
    private VideoListAdapter spamAdapter;
    private ArrayList<Video> spamVideos = new ArrayList<>();
    private RecyclerView.OnScrollListener spamScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (llmanager.findLastCompletelyVisibleItemPosition() == (spamAdapter.getItemCount() - 2)) {
                spamAdapter.showLoading();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spam_videos);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        swipe.setOnRefreshListener(this);
        setUpSpamVideos();
    }

    @Override
    public void onResume() {
        super.onResume();
        getSpamVideos(0);
    }

    private void setUpSpamVideos() {
        spamRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager
                .VERTICAL, false));
        spamAdapter = new VideoListAdapter(SpamVideosActivity.this, spamVideos, VideoListType.TYPE_SPAM, this);
        spamRecycler.setAdapter(spamAdapter);
        spamRecycler.addOnScrollListener(spamScrollListener);
    }

    protected void getSpamVideos(int skip) {
        if (skip == 0) {
            noResultLayout.setVisibility(View.GONE);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
        }
        PrefUtils prefUtils = PrefUtils.getInstance(SpamVideosActivity.this);
        Call<String> call = apiInterface.getSpamVideos(id
                , token
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , skip);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (skip == 0) spamVideos.clear();
                if (swipe.isRefreshing()) swipe.setRefreshing(false);
                shimmerFrameLayout.setVisibility(View.GONE);
                JSONObject spamListResponse = null;
                try {
                    spamListResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (spamListResponse != null) {
                    if (spamListResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        JSONArray spamListArray = spamListResponse.optJSONArray(APIConstants.Params.DATA);
                        for (int i = 0; i < spamListArray.length(); i++) {
                            JSONObject object = spamListArray.optJSONObject(i);
                            Video video = new Video();
                            video.setAdminVideoId(object.optInt(APIConstants.Params.ADMIN_VIDEO_ID));
                            video.setTitle(object.optString(APIConstants.Params.TITLE));
                            video.setThumbNailUrl(object.optString(APIConstants.Params.DEFAULT_IMAGE));
                            video.setDescription(object.optString(APIConstants.Params.DESCRIPTION));
                            spamVideos.add(video);
                        }
                        if (skip != 0) {
                            spamAdapter.dismissLoading();
                        }
                        onDateChanged();
                    } else {
                        UiUtils.showShortToast(SpamVideosActivity.this, spamListResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(SpamVideosActivity.this);
            }
        });
    }

    private void onDateChanged() {
        spamAdapter.notifyDataSetChanged();
        if (spamVideos.isEmpty()) {
            noResultLayout.setVisibility(View.VISIBLE);
            spamRecycler.setVisibility(View.GONE);
        } else {
            noResultLayout.setVisibility(View.GONE);
            spamRecycler.setVisibility(View.VISIBLE);
        }

        if (spamVideos.size() == 0) {
            clearAll.setVisibility(View.GONE);
        } else {
            clearAll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadMore(int skip) {
        spamAdapter.showLoading();
        getSpamVideos(skip);
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @OnClick(R.id.clearAll)
    public void onViewClicked() {
        new AlertDialog.Builder(SpamVideosActivity.this)
                .setMessage("Are you sure to clear the spam videos?")
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    clearSpamVideosFromBackend();
                })
                .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel())
                .create().show();
    }

    private void clearSpamVideosFromBackend() {
        UiUtils.showLoadingDialog(SpamVideosActivity.this);
        PrefUtils prefUtils = PrefUtils.getInstance(SpamVideosActivity.this);
        Call<String> call = apiInterface.clearSpamLsit(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , 0
                , 1);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject clearWishListResponse = null;
                try {
                    clearWishListResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (clearWishListResponse != null) {
                    if (clearWishListResponse.optString(SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        spamVideos.clear();
                        onDateChanged();
                    } else {
                        UiUtils.showShortToast(SpamVideosActivity.this, clearWishListResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(SpamVideosActivity.this);
            }
        });
    }

    @Override
    public void onDataChanged() {
        spamAdapter.notifyDataSetChanged();
        if (spamVideos.isEmpty()) {
            noResultLayout.setVisibility(View.VISIBLE);
            spamRecycler.setVisibility(View.GONE);
        } else {
            noResultLayout.setVisibility(View.GONE);
            spamRecycler.setVisibility(View.VISIBLE);
        }

        if (spamVideos.size() == 0) {
            clearAll.setVisibility(View.GONE);
        } else {
            clearAll.setVisibility(View.VISIBLE);
        }
    }
}
