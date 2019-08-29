package com.streamhash.streamview.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerListener;
import tcking.github.com.giraffeplayer2.PlayerManager;
import tcking.github.com.giraffeplayer2.VideoView;
import timber.log.Timber;
import tv.danmaku.ijk.media.player.IjkTimedText;

import static com.streamhash.streamview.network.APIConstants.Params.SUCCESS;

public class PlayerActivity extends BaseActivity implements PlayerListener {

    public static final String VIDEO_URL = "videoUrl";
    public static final String VIDEO_SUBTITLE = "videoSubtitle";
    public static final String VIDEO_ID = "videoId";
    public static final String VIDEO_ELAPSED = "videoElapsed";

    public static String[] resolutionKeys;
    public static String[] resolutionUrls;

    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.resolutionBtn)
    View resolutionBtn;

    private GiraffePlayer player;
    private VideoView surface;
    private String videoUrl;
    private String videoSubtitle;
    private AlertDialog resolutionDialog;
    private int currentResolution = 0;
    private boolean isVideoEnded = false;
    private int videoId;
    private int videoElapsed;

    private boolean firstPlay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        if (getIntent() != null) {
            videoId = getIntent().getIntExtra(PlayerActivity.VIDEO_ID, 0);
            videoUrl = getIntent().getStringExtra(PlayerActivity.VIDEO_URL);
            videoElapsed = getIntent().getIntExtra(PlayerActivity.VIDEO_ELAPSED, 0);
            videoSubtitle = getIntent().getStringExtra(PlayerActivity.VIDEO_SUBTITLE);
        }

        prepareVideoPlayer();
        prepareResolutionDialog();

        //everything done, play the video
        loadVideo();

        //add button listener after load stated
        resolutionBtn.setVisibility(View.VISIBLE);
    }

    /**
     * Resolution stuff
     */
    private void prepareResolutionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_resolution_text));
        builder.setSingleChoiceItems(resolutionKeys, currentResolution, (dialog, which) -> {
            storeCurrentPositionBeforeConfigChange();
            currentResolution = which;
            loadNewVideo(resolutionUrls[which]);
            dialog.dismiss();
        });
        resolutionDialog = builder.create();
    }

    private void prepareVideoPlayer() {
        surface = new VideoView(this, videoSubtitle);
        container.addView(surface);
        surface.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        surface.setPlayerListener(this);
    }

    @OnClick(R.id.resolutionBtn)
    protected void showResolutionDialog() {
        if (!resolutionDialog.isShowing())
            resolutionDialog.show();
    }

    private void seekToOldPositionAfterConfigChange() {
        if (videoElapsed != 0)
            player.seekTo(videoElapsed);
    }

    private void storeCurrentPositionBeforeConfigChange() {
        videoElapsed = (player.getCurrentPosition()/1000);
    }


    private void loadNewVideo(String newUrl) {
        videoUrl = newUrl;
        loadVideo();
        seekToOldPositionAfterConfigChange();
    }

    private void loadVideo() {
        if (surface != null) {
            surface.setVideoPath(videoUrl).getPlayer()
                    .setDisplayModel(GiraffePlayer.DISPLAY_FULL_WINDOW)
                    .start();
            player = PlayerManager.getInstance().getCurrentPlayer();
        }
        showBottomControl(false);
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null)
            player.start();
    }

    @Override
    protected void onPause() {
        if (player != null) {
            continueWatchingStorePos(player.getCurrentPosition());
            player.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.stop();
            PlayerManager.getInstance().releaseCurrent();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
        if (player != null) {
            player.onConfigurationChanged(newConfig);
            showBottomControl(false);
        }
    }

    protected void showBottomControl(boolean show) {
        findViewById(com.github.tcking.giraffeplayer2.R.id.app_video_bottom_box).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //Player implementations
    @Override
    public void onPrepared(GiraffePlayer giraffePlayer) {
        if (firstPlay) {
            giraffePlayer.seekTo(videoElapsed * 1000);
            firstPlay = false;
        }
    }

    @Override
    public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {

    }

    @Override
    public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
        return false;
    }

    @Override
    public void onCompletion(GiraffePlayer giraffePlayer) {
        isVideoEnded = true;
        addVideoToHistory(videoId);
        onPPVCompleted(videoId);
        onBackPressed();
    }


    private void onPPVCompleted(int adminVideoId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<String> call = apiInterface.ppvEnd(id, token, adminVideoId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    @Override
    public void onSeekComplete(GiraffePlayer giraffePlayer) {

    }

    @Override
    public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
        if (NetworkUtils.isNetworkConnected(PlayerActivity.this)) {
            new AlertDialog.Builder(PlayerActivity.this, R.style.AppTheme_Dialog)
                    .setMessage(getString(R.string.cant_play_video))
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                        dialogInterface.cancel();
                        finish();
                    })
                    .create().show();
        } else {
            UiUtils.showShortToast(PlayerActivity.this, getString(R.string.check_network));
        }
        return true;
    }

    @Override
    public void onPause(GiraffePlayer giraffePlayer) {
        if (isVideoEnded)
            continueWatchingEnded();
        else
            continueWatchingStorePos(giraffePlayer.getCurrentPosition());
    }

    @Override
    public void onRelease(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onStart(GiraffePlayer giraffePlayer) {
        showBottomControl(true);
    }

    @Override
    public void onTargetStateChange(int oldState, int newState) {

    }

    @Override
    public void onCurrentStateChange(int oldState, int newState) {

    }

    @Override
    public void onDisplayModelChange(int oldModel, int newModel) {

    }

    @Override
    public void onPreparing(GiraffePlayer giraffePlayer) {

    }

    @Override
    public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {

    }

    private void continueWatchingStorePos(int millis) {
        videoElapsed = millis / 1000;
        Call<String> call = APIClient.getClient().create(APIInterface.class)
                .continueWatchingStorePos(id, token, subProfileId,
                        videoId
                        , millis / 1000);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    private void continueWatchingEnded() {
        Call<String> call = APIClient.getClient().create(APIInterface.class)
                .continueWatchingEnd(id, token, subProfileId,
                        videoId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    private void addVideoToHistory(int adminVideoId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<String> call = apiInterface.addToHistory(id, token, subProfileId, adminVideoId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                JSONObject addToHistoryResponse = null;
                try {
                    addToHistoryResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (addToHistoryResponse != null) {
                    if (addToHistoryResponse.optString(SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        Timber.d("Added to history: %s", adminVideoId);
                    } else {
                        Timber.d("Could not be added to history: %s", adminVideoId);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(PlayerActivity.this);
            }
        });
    }
}
