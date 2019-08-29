package com.streamhash.streamview.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.ui.adapter.MoreVideosAdapter;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoreVideosActivity extends BaseActivity implements OnLoadMoreVideosListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.moreVideosRecycler)
    RecyclerView moreVideosRecycler;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    MoreVideosAdapter moreVideosAdapter;
    ArrayList<Video> moreVideos = new ArrayList<Video>();
    APIInterface apiInterface;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    Intent intent;
    String urlType = "";
    String pageType = "";
    String title = "";
    private int categoryId;
    private int subCategoryId;
    private int castNCrewId;
    private int genreId;
    private int pageUrlId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_videos2);
        ButterKnife.bind(this);
        swipe.setOnRefreshListener(this);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);
        intent = getIntent();
        setUpAdapter();
    }

    public void setUpAdapter() {
        moreVideosAdapter = new MoreVideosAdapter(getApplicationContext(), moreVideos);
        moreVideosAdapter.setOnLoadMoreVideosListener(this);
        moreVideosRecycler.setHasFixedSize(true);
        moreVideosRecycler.setLayoutManager(new GridLayoutManager(getApplicationContext(),3));
        moreVideosRecycler.setItemAnimator(new DefaultItemAnimator());
        moreVideosRecycler.setAdapter(moreVideosAdapter);
        moreVideosRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llmanager.findLastCompletelyVisibleItemPosition() == (moreVideosAdapter.getItemCount() - 1)) {
                    moreVideosAdapter.showLoading();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(intent.getExtras() != null)
        {
            urlType = intent.getStringExtra(APIConstants.Params.URL_TYPE);
            pageType = intent.getStringExtra(APIConstants.Params.PAGE_TYPE);
            pageUrlId = intent.getIntExtra(APIConstants.Params.URL_PAGE_ID, 0);
            categoryId = intent.getIntExtra(APIConstants.Params.CATEGORY_ID, 0);
            subCategoryId = intent.getIntExtra(APIConstants.Params.SUB_CATEGORY_ID, 0);
            castNCrewId = intent.getIntExtra(APIConstants.Params.CAST_CREW_ID, 0);
            genreId = intent.getIntExtra(APIConstants.Params.GENRE_ID, 0);
            title = intent.getStringExtra(APIConstants.Params.TITLE);
            toolbar.setTitle(title);

            getMoreVideosFromBackend(urlType,pageType,pageUrlId,categoryId,subCategoryId,castNCrewId,genreId,0);
        }
    }

    protected void getMoreVideosFromBackend(String urlType, String pageType, int urlPageId, int categoryId, int subCateId,
                                            int castNCrewId, int genreId, int skip) {
        PrefUtils prefUtils = PrefUtils.getInstance(MoreVideosActivity.this);

        UiUtils.showLoadingDialog(MoreVideosActivity.this);
        Call<String> call = apiInterface.moreVideosList(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , urlType
                , pageType
                , urlPageId
                , categoryId
                , subCateId
                , castNCrewId
                , genreId
                , skip);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (skip == 0) moreVideos.clear();
                if (swipe.isRefreshing()) swipe.setRefreshing(false);
                UiUtils.hideLoadingDialog();
                if (skip != 0) {
                    moreVideosAdapter.dismissLoading();
                }
                JSONObject moreVideosResponse = null;
                try {
                    moreVideosResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (moreVideosResponse != null) {
                    if (moreVideosResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        JSONArray wishListArray = moreVideosResponse.optJSONArray(APIConstants.Params.DATA);
                        for (int i = 0; i < wishListArray.length(); i++) {
                            JSONObject object = wishListArray.optJSONObject(i);
                            Video video = new Video();
                            video.setAdminVideoId(object.optInt(APIConstants.Params.ADMIN_VIDEO_ID));
                            video.setTitle(object.optString(APIConstants.Params.TITLE));
                            video.setCategoryId(object.optInt(APIConstants.Params.CATEGORY_ID));
                            video.setGenreId(object.optInt(APIConstants.Params.GENRE_ID));
                            video.setThumbNailUrl(object.optString(APIConstants.Params.MOBILE_IMAGE));
                            moreVideos.add(video);
                        }

                        onDataChanged();
                    } else {
                        UiUtils.showShortToast(MoreVideosActivity.this, moreVideosResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(MoreVideosActivity.this);
            }
        });
    }

    private void onDataChanged() {
        moreVideosAdapter.notifyDataSetChanged();
        if (moreVideos.isEmpty()) {
            noResultLayout.setVisibility(View.VISIBLE);
            moreVideosRecycler.setVisibility(View.GONE);
        } else {
            noResultLayout.setVisibility(View.GONE);
            moreVideosRecycler.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @Override
    public void onLoadMore(int skip) {
        moreVideosAdapter.showLoading();
        getMoreVideosFromBackend(urlType,pageType,pageUrlId,categoryId,subCategoryId,castNCrewId,genreId,skip);
    }
}
