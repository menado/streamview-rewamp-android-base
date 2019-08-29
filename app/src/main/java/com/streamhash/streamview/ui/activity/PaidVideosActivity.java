package com.streamhash.streamview.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.adapter.PaidVideosAdapter;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.ParserUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class PaidVideosActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, OnLoadMoreVideosListener {

    PaidVideosAdapter paidVideosAdapter;
    ArrayList<Video> paidVideos = new ArrayList<>();
    @BindView(R.id.list)
    RecyclerView paidVideoRecycler;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    APIInterface apiInterface;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmerFrameLayout;

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (llmanager.findLastCompletelyVisibleItemPosition() == (paidVideosAdapter.getItemCount() - 1)) {
                paidVideosAdapter.showLoading();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paid_videos);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        setUpNotifications();
        swipe.setOnRefreshListener(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);
    }

    private void setUpNotifications() {
        paidVideoRecycler.setLayoutManager(new LinearLayoutManager(this));
        paidVideosAdapter = new PaidVideosAdapter(this, this, paidVideos);
        paidVideoRecycler.setAdapter(paidVideosAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPaidVideosList(0);
    }

    protected void getPaidVideosList(int skip) {
        if (skip == 0) {
            paidVideoRecycler.setOnScrollListener(scrollListener);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            noResultLayout.setVisibility(View.GONE);
            paidVideoRecycler.setVisibility(View.GONE);
        }

        PrefUtils prefUtils = PrefUtils.getInstance(PaidVideosActivity.this);
        Call<String> call = apiInterface.getMyPaidVideos(id, token,
                prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1),
                skip);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (skip == 0) {
                    paidVideos.clear();
                    if (swipe.isRefreshing()) swipe.setRefreshing(false);
                    shimmerFrameLayout.setVisibility(View.GONE);
                }

                JSONObject paidVideosObject = null;
                try {
                    paidVideosObject = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (paidVideosObject != null) {
                    if (paidVideosObject.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        JSONArray paidVideosArray = paidVideosObject.optJSONArray(Params.DATA);
                        for (int i = 0; i < paidVideosArray.length(); i++) {
                            JSONObject paidVideoObj = paidVideosArray.optJSONObject(i);
                            Video paidVideo = ParserUtils.parsePaidVideoData(paidVideoObj);
                            paidVideos.add(paidVideo);
                        }
                        if (paidVideosArray.length() == 0) {
                            paidVideoRecycler.removeOnScrollListener(scrollListener);
                        }
                        onDateChanged();
                    } else {

                        UiUtils.showShortToast(PaidVideosActivity.this, paidVideosObject.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                noResultLayout.setVisibility(View.VISIBLE);
                shimmerFrameLayout.setVisibility(View.GONE);
                paidVideoRecycler.setVisibility(View.GONE);
                NetworkUtils.onApiError(PaidVideosActivity.this);
            }
        });
    }

    private void onDateChanged() {
        paidVideosAdapter.notifyDataSetChanged();
        noResultLayout.setVisibility(paidVideos.isEmpty() ? View.VISIBLE : View.GONE);
        paidVideoRecycler.setVisibility(paidVideos.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @Override
    public void onLoadMore(int skip) {
        getPaidVideosList(skip);
    }
}
