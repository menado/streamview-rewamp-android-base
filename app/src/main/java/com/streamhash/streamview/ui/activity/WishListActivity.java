package com.streamhash.streamview.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
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

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class WishListActivity extends BaseActivity implements OnLoadMoreVideosListener, SwipeRefreshLayout.OnRefreshListener, VideoListAdapter.OnDataChangedListener {

    @BindView(R.id.wishListRecycler)
    RecyclerView wishListRecycler;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    VideoListAdapter wishListAdapter;
    ArrayList<Video> wishListVideos = new ArrayList<>();
    APIInterface apiInterface;
    @BindView(R.id.clearAll)
    TextView clearAll;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmerFrameLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);
        ButterKnife.bind(this);
        swipe.setOnRefreshListener(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);
        setUpWishList();
    }

    private void setUpWishList() {
        wishListAdapter = new VideoListAdapter(this, wishListVideos, VideoListAdapter.VideoListType.TYPE_WISH_LIST, this);
        wishListAdapter.setOnLoadMoreVideosListener(this);
        wishListRecycler.setHasFixedSize(true);
        wishListRecycler.setLayoutManager(new LinearLayoutManager(this));
        wishListRecycler.setItemAnimator(new DefaultItemAnimator());
        wishListRecycler.setAdapter(wishListAdapter);
        wishListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llmanager.findLastCompletelyVisibleItemPosition() == (wishListAdapter.getItemCount() - 2)) {
                    wishListAdapter.showLoading();
                }
            }
        });
    }

    protected void getWishListItemsFromBackend(int skip) {
        if (wishListVideos.size() == 0)
            shimmerFrameLayout.setVisibility(View.VISIBLE);
        PrefUtils prefUtils = PrefUtils.getInstance(this);
        Call<String> call = apiInterface.getWishListItems(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , skip);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                shimmerFrameLayout.setVisibility(View.GONE);
                if (skip == 0) wishListVideos.clear();
                if (swipe.isRefreshing()) swipe.setRefreshing(false);

                JSONObject wishListResponse = null;
                try {
                    wishListResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (wishListResponse != null) {
                    if (wishListResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        JSONArray wishListArray = wishListResponse.optJSONArray(Params.DATA);
                        for (int i = 0; i < wishListArray.length(); i++) {
                            JSONObject object = wishListArray.optJSONObject(i);
                            Video video = new Video();
                            video.setAdminVideoId(object.optInt(Params.ADMIN_VIDEO_ID));
                            video.setTitle(object.optString(Params.TITLE));
                            video.setThumbNailUrl(object.optString(Params.DEFAULT_IMAGE));
                            video.setDescription(object.optString(Params.DESCRIPTION));
                            wishListVideos.add(video);
                        }
                        if (skip != 0) {
                            wishListAdapter.dismissLoading();
                        }
                        onDataChanged();
                    } else {
                        UiUtils.showShortToast(WishListActivity.this, wishListResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(WishListActivity.this);
            }
        });
    }

    public void onDataChanged() {
        wishListAdapter.notifyDataSetChanged();
        if (wishListVideos.isEmpty()) {
            noResultLayout.setVisibility(View.VISIBLE);
            wishListRecycler.setVisibility(View.GONE);
        } else {
            noResultLayout.setVisibility(View.GONE);
            wishListRecycler.setVisibility(View.VISIBLE);
        }

        if (wishListVideos.size() == 0) {
            clearAll.setVisibility(View.GONE);
        } else {
            clearAll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWishListItemsFromBackend(0);
    }

    @Override
    public void onLoadMore(int skip) {
        wishListAdapter.showLoading();
        getWishListItemsFromBackend(skip);
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @OnClick(R.id.clearAll)
    public void onClearAllClicked() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure to clear wishlist?")
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    wishListAdapter.clearWishlistInBackend(0, 1);
//                    onDataChanged();
                })
                .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel())
                .create().show();
    }
}
