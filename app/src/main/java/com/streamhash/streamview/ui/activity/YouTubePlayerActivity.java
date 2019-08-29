package com.streamhash.streamview.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.AppUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.streamhash.streamview.network.APIConstants.Params.SUCCESS;

public class YouTubePlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    public static final String VIDEO_URL = "videoUrl";
    public static final String VIDEO_ID = "videoId";
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    @BindView(R.id.youtube_view)
    YouTubePlayerView youTubePlayer;
    private String videoUrl = "";
    private int videoId;
    private MyPlayerStateChangeListener playerStateChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_youtube_player);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            playerStateChangeListener = new MyPlayerStateChangeListener();
            videoUrl = intent.getStringExtra(YouTubePlayerActivity.VIDEO_URL);
            videoId = intent.getIntExtra(YouTubePlayerActivity.VIDEO_ID, 0);
            youTubePlayer.initialize(getString(R.string.youtube_api_key), this);
        } else {
            UiUtils.showShortToast(this, getString(R.string.something_went_wrong));
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(getString(R.string.youtube_api_key), this);
        }
    }

    private YouTubePlayer.Provider getYouTubePlayerProvider() {
        return youTubePlayer;
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.setPlayerStateChangeListener(playerStateChangeListener);
        youTubePlayer.setFullscreen(true);
        youTubePlayer.setShowFullscreenButton(false);
        youTubePlayer.loadVideo(AppUtils.getYouTubeIdFromUrl(videoUrl));
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, getString(R.string.failed_initialize), Toast.LENGTH_LONG).show();
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, 1).show();
        } else {
            @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String errorMessage = String.format(
                    getString(R.string.error_player), youTubeInitializationResult.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void addVideoToHistory(int adminVideoId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        PrefUtils prefUtils = PrefUtils.getInstance(this);
        Call<String> call = apiInterface.addToHistory(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , adminVideoId);
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
                NetworkUtils.onApiError(YouTubePlayerActivity.this);
            }
        });
    }

    private void onPPVCompleted(int adminVideoId) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        PrefUtils prefUtils = PrefUtils.getInstance(this);
        Call<String> call = apiInterface.ppvEnd(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , adminVideoId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
               /* JSONObject addToHistoryResponse = null;
                try {
                    addToHistoryResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (addToHistoryResponse != null) {

                }*/
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(YouTubePlayerActivity.this);
            }
        });
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {
            addVideoToHistory(videoId);
            onPPVCompleted(videoId);
            onBackPressed();
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
        }
    }

}


