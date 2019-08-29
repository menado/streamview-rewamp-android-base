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

import static com.streamhash.streamview.ui.adapter.VideoListAdapter.*;

public class HistoryActivity extends BaseActivity implements OnLoadMoreVideosListener, SwipeRefreshLayout.OnRefreshListener, OnDataChangedListener {

    @BindView(R.id.historyRecycler)
    RecyclerView historyRecycler;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    APIInterface apiInterface;
    ArrayList<Video> historyVideos = new ArrayList<>();
    VideoListAdapter historyAdapter;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.clearAll)
    TextView clearAll;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmerFrameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);
        swipe.setOnRefreshListener(this);
        setUpHistoryVideos();
    }

    private void setUpHistoryVideos() {
        historyAdapter = new VideoListAdapter(this, historyVideos, VideoListType.TYPE_HISTORY, this);
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(historyAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getHistoryVideos(0);
    }

    protected void getHistoryVideos(int skip) {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        noResultLayout.setVisibility(View.GONE);
        historyRecycler.setVisibility(View.GONE);
        clearAll.setVisibility(View.GONE);
        PrefUtils prefUtils = PrefUtils.getInstance(HistoryActivity.this);
        Call<String> call = apiInterface.getHistoryVideos(id
                , token
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , skip);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (skip == 0) historyVideos.clear();
                if (swipe.isRefreshing()) swipe.setRefreshing(false);
                shimmerFrameLayout.setVisibility(View.GONE);
                JSONObject historyListResponse = null;
                try {
                    historyListResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (historyListResponse != null) {
                    if (historyListResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        JSONArray historyListArray = historyListResponse.optJSONArray(APIConstants.Params.DATA);
                        try {
                            for (int i = 0; i < historyListArray.length(); i++) {
                                JSONObject object = historyListArray.optJSONObject(i);
                                Video video = new Video();
                                video.setAdminVideoId(object.optInt(APIConstants.Params.ADMIN_VIDEO_ID));
                                video.setTitle(object.optString(APIConstants.Params.TITLE));
                                video.setThumbNailUrl(object.optString(APIConstants.Params.DEFAULT_IMAGE));
                                video.setDescription(object.optString(APIConstants.Params.DESCRIPTION));
                                historyVideos.add(video);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if (skip != 0) {
                            historyAdapter.dismissLoading();
                        }
                        onDataChanged();
                    } else {
                        UiUtils.showShortToast(HistoryActivity.this, historyListResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                noResultLayout.setVisibility(View.VISIBLE);
                NetworkUtils.onApiError(HistoryActivity.this);
            }
        });
    }


    public void onDataChanged() {
        historyAdapter.notifyDataSetChanged();
        noResultLayout.setVisibility(historyVideos.isEmpty() ? View.VISIBLE : View.GONE);
        historyRecycler.setVisibility(historyVideos.isEmpty() ? View.GONE : View.VISIBLE);
        clearAll.setVisibility(historyVideos.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @Override
    public void onLoadMore(int skip) {
        historyAdapter.showLoading();
        getHistoryVideos(skip);
    }


    @OnClick(R.id.clearAll)
    public void onViewClicked() {
        new AlertDialog.Builder(HistoryActivity.this)
                .setMessage("Are you sure to clear your History?")
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    historyAdapter.clearHistoryinBackend(0, 1);
                })
                .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel())
                .create().show();
    }
}
