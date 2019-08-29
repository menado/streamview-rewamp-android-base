package com.streamhash.streamview.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.streamhash.streamview.ui.adapter.VideoListAdapter;
import com.streamhash.streamview.util.NetworkUtils;
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

import static com.streamhash.streamview.ui.adapter.VideoListAdapter.*;

public class CastVideosActivity extends BaseActivity implements OnLoadMoreVideosListener, SwipeRefreshLayout.OnRefreshListener, OnDataChangedListener {

    public static final String CAST_CREW_ID = "castCrewId";
    public static final String CAST_CREW_NAME = "castCrewName";

    @BindView(R.id.castRecycler)
    RecyclerView castVideosRecycler;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;

    APIInterface apiInterface;
    VideoListAdapter castVideosAdapter;
    ArrayList<Video> castVideos = new ArrayList<>();
    private int castCrewId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast_videos);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        swipe.setOnRefreshListener(this);

        if (getIntent() != null) {
            castCrewId = getIntent().getIntExtra(CAST_CREW_ID, 0);
            String castCrewName = getIntent().getStringExtra(CAST_CREW_NAME);
            toolbar.setTitle("Videos of: " + castCrewName);
            setUpCastVideos();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getCastCrewVideos(0);
    }

    private void setUpCastVideos() {
        castVideosRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager
                .VERTICAL, false));
        castVideosAdapter = new VideoListAdapter(this, castVideos, VideoListType.TYPE_OTHERS, this);
        castVideosRecycler.setAdapter(castVideosAdapter);
        castVideosRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llmanager.findLastCompletelyVisibleItemPosition() == (castVideosAdapter.getItemCount() - 1)) {
                    castVideosAdapter.showLoading();

                }
            }
        });
    }

    protected void getCastCrewVideos(int skip) {
        if (skip == 0)
            swipe.setRefreshing(true);

        Call<String> call = apiInterface.getCastVideos(id
                , token
                , subProfileId
                , castCrewId
                , APIConstants.Constants.ANDROID
                , skip);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (skip == 0) castVideos.clear();
                if (swipe.isRefreshing()) swipe.setRefreshing(false);

                JSONObject castVideoResponse = null;
                try {
                    castVideoResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (castVideoResponse != null) {
                    if (castVideoResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        if (skip != 0) {
                            castVideosAdapter.dismissLoading();
                        }
                        JSONArray spamListArray = castVideoResponse.optJSONArray(APIConstants.Params.DATA);
                        for (int i = 0; i < spamListArray.length(); i++) {
                            JSONObject object = spamListArray.optJSONObject(i);
                            Video video = new Video();
                            video.setAdminVideoId(object.optInt(APIConstants.Params.ADMIN_VIDEO_ID));
                            video.setTitle(object.optString(APIConstants.Params.TITLE));
                            video.setThumbNailUrl(object.optString(APIConstants.Params.DEFAULT_IMAGE));
                            video.setDescription(object.optString(APIConstants.Params.DESCRIPTION));
                            castVideos.add(video);
                        }
                        onDataChanged();
                    } else {
                        UiUtils.showShortToast(CastVideosActivity.this, castVideoResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                if (swipe.isRefreshing()) swipe.setRefreshing(false);
                NetworkUtils.onApiError(CastVideosActivity.this);
            }
        });
    }

    @Override
    public void onLoadMore(int skip) {
        getCastCrewVideos(skip);
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @Override
    public void onDataChanged() {
        castVideosAdapter.notifyDataSetChanged();
        noResultLayout.setVisibility(castVideos.isEmpty() ? View.VISIBLE : View.GONE);
        castVideosRecycler.setVisibility(castVideos.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
