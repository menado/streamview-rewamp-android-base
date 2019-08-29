package com.streamhash.streamview.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.adapter.OfflineVideoAdapter;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.ParserUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.download.DownloadUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.util.NetworkUtils.isNetworkConnected;
import static com.streamhash.streamview.util.download.DownloadUtils.getFileExpiry;
import static com.streamhash.streamview.util.download.DownloadUtils.getFileName;
import static com.streamhash.streamview.util.download.DownloadUtils.getVideoDuration;
import static com.streamhash.streamview.util.download.DownloadUtils.getVideoId;
import static com.streamhash.streamview.util.download.DownloadUtils.isOfflineVideoExisting;

public class DownloadsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OfflineVideoAdapter.OfflineVideoInterface {

    @BindView(R.id.offlineVideos)
    RecyclerView offlineVideosRecycler;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.noOfflineVideos)
    TextView noOfflineVideos;
    @BindView(R.id.loadingVideos)
    ProgressBar loadingVideos;
    Context activity;
    Unbinder unbinder;
    APIInterface apiInterface;
    OfflineVideoAdapter offlineVideoAdapter;
    ArrayList<Video> offLineVideos = new ArrayList<>();

    private void setVideoAdapter() {
        offlineVideoAdapter = new OfflineVideoAdapter(activity, offLineVideos);
        offlineVideoAdapter.setDownloadVideoListener(this);

        offlineVideosRecycler.setVisibility(offLineVideos.size() > 0 ? View.VISIBLE : View.GONE);
        noOfflineVideos.setVisibility(offLineVideos.size() > 0 ? View.GONE : View.VISIBLE);
        offlineVideosRecycler.setLayoutManager(new LinearLayoutManager(activity));
        offlineVideosRecycler.setAdapter(offlineVideoAdapter);

        swipe.setRefreshing(false);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_downloads, container, false);
        unbinder = ButterKnife.bind(this, view);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadOfflineVideos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onRefresh() {
        getAllOfflineVideos();
    }

    private void loadOfflineVideos() {
        if (isNetworkConnected(activity)) {
            getAllOfflineVideos();
        } else {
            getAllVideos();
            setVideoAdapter();
        }
    }

    private void getAllOfflineVideos() {
        loadingVideos.setVisibility(View.VISIBLE);
        noOfflineVideos.setVisibility(View.GONE);
        offlineVideosRecycler.setVisibility(View.GONE);
        PrefUtils prefUtils = PrefUtils.getInstance(getActivity());
        Call<String> call = apiInterface.downloadedVideos(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    loadingVideos.setVisibility(View.GONE);
                    swipe.setRefreshing(false);
                    JSONObject offlineVideosResponse = null;
                    try {
                        offlineVideosResponse = new JSONObject(response.body());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (offlineVideosResponse != null) {
                        if (offlineVideosResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                            JSONArray videos = offlineVideosResponse.optJSONArray(APIConstants.Params.DATA);
                            for (int i = 0; i < videos.length(); i++) {
                                try {
                                    JSONObject videoItem = videos.getJSONObject(i);
                                    Video videoItemFromWeb = ParserUtils.parseVideoData(videoItem);
                                    if (isOfflineVideoExisting(activity, videoItemFromWeb.getAdminVideoId())) {
                                        videoItemFromWeb.setVideoUrl(DownloadUtils.getVideoPath(activity,
                                                videoItemFromWeb.getAdminVideoId(),
                                                videoItemFromWeb.getVideoUrl()));
                                        offLineVideos.add(videoItemFromWeb);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            setVideoAdapter();
                        } else {
                            UiUtils.showShortToast(activity, offlineVideosResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(activity);
                loadingVideos.setVisibility(View.GONE);
                swipe.setRefreshing(false);
            }
        });
    }

    private void getAllVideos() {
        File dir = new File(activity.getExternalFilesDir(null).getPath() + "/" + PrefUtils.getInstance(activity).getIntValue(PrefKeys.USER_ID, 0));
        File allFiles[] = dir.listFiles();
        offLineVideos.clear();
        if (allFiles != null && allFiles.length > 0) {
            for (File file : allFiles) {
                if (!file.isDirectory()) {
                    String filePath = file.getPath();
                    String extension = filePath.substring(filePath.length() - 4);
                    if (extension != null && !extension.equals("") && extension.equals(".mp4")) {
                        Video item = new Video();
                        item.setAdminVideoId(getVideoId(file.getName()));
                        item.setTitle(getFileName(file.getName()));
                        item.setVideoUrl(file.getAbsolutePath());
                        int daysSinceDownloaded = getFileExpiry(file.getAbsolutePath());
                        int expiryNumDays;
                        try {
                            expiryNumDays = Integer.parseInt(file.getName().split("\\.")[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            expiryNumDays = 0;
                        }
                        if (expiryNumDays - daysSinceDownloaded < 0) {
                            continue;
                        } else {
                            item.setExpired(false);
                            item.setNumDaysToExpire(expiryNumDays - daysSinceDownloaded);
                        }

                        String duration;
                        try {
                            duration = getVideoDuration(activity, file.getAbsolutePath());
                        } catch (Exception e) {
                            duration = "--:--:--";
                            e.printStackTrace();
                        }
                        item.setDuration(duration);
                        offLineVideos.add(item);
                    }
                }
            }
        }
    }

    @Override
    public void onVideoDeleted(boolean isEmpty) {
        offlineVideosRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        noOfflineVideos.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
