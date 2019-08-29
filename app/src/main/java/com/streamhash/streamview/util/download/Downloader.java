package com.streamhash.streamview.util.download;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.request.DownloadRequest;
import com.streamhash.streamview.BuildConfig;
import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.DownloadStatus;
import static com.streamhash.streamview.network.APIConstants.Params;
import static com.streamhash.streamview.util.download.DownloadUtils.getFileSize;

public class Downloader {
    private static PrefUtils prefUtils;
    private static APIInterface apiInterface;
    public Context context;
    private CharSequence channelName;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private HashMap<Integer, Long> map;
    private DownloadCompleteListener downloadCompleteListener;
    private List<Integer> downloadingTasks;


    public Downloader(Context context) {
        this.context = context;
        apiInterface = APIClient.getClient().create(APIInterface.class);
        prefUtils = PrefUtils.getInstance(context);
        this.downloadingTasks = new ArrayList<>();
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        map = new HashMap<>();

        channelName = context.getString(R.string.channel_name);
    }


    public static void downloadCompleted(final Context context, final int adminVideoId) {
        downloadStatusUpdate(context, adminVideoId, DownloadStatus.DOWNLOAD_COMPLETE_STATUS);
    }

    public static void downloadCanceled(final Context context, final int adminVideoId) {
        downloadStatusUpdate(context, adminVideoId, DownloadStatus.DOWNLOAD_CANCEL_STATUS);
    }

    public static void downloadStart(final Context context, final int adminVideoId) {
        downloadStatusUpdate(context, adminVideoId, DownloadStatus.DOWNLOAD_INITIATE_STATUS);
    }

    public static void downloadDeleted(final Context context, final int adminVideoId) {
        downloadStatusUpdate(context, adminVideoId, DownloadStatus.DOWNLOAD_DELETED_STATUS);
    }

    private static void removeFromSharedPrefDeleteList(Context context, int adminVideoId) {
        String pendingDeletes = prefUtils.getStringValue(PrefKeys.DELETE_VIDEOS_DOWNLOAD, "");
        pendingDeletes.replace(String.valueOf(adminVideoId), "");
        PrefUtils.getInstance(context).setValue(PrefKeys.DELETE_VIDEOS_DOWNLOAD, pendingDeletes);
    }

    private static void putDownloadForCancel(Context context, int adminVideoId) {
        String pendingCancels = prefUtils.getStringValue(PrefKeys.CANCEL_VIDEOS_DOWNLOAD, "");
        PrefUtils.getInstance(context).setValue(PrefKeys.CANCEL_VIDEOS_DOWNLOAD, pendingCancels + "." + adminVideoId);
    }

    private static void putDownloadForDelete(Context context, int adminVideoId) {
        String pendingDeletes = prefUtils.getStringValue(PrefKeys.DELETE_VIDEOS_DOWNLOAD, "");
        PrefUtils.getInstance(context).setValue(PrefKeys.DELETE_VIDEOS_DOWNLOAD, pendingDeletes + "." + adminVideoId);
    }

    private static void downloadStatusUpdate(final Context context, final int adminVideoId, final int downloadStatus) {
        if (prefUtils == null)
            prefUtils = PrefUtils.getInstance(context);
        if (apiInterface == null)
            apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<String> call = apiInterface.downloadStatusUpdate(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1)
                , adminVideoId
                , downloadStatus);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject downloadStatusUpdateResponse = null;
                try {
                    downloadStatusUpdateResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (downloadStatusUpdateResponse != null) {
                    if (!downloadStatusUpdateResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        handleNetworkFailure(context, adminVideoId, downloadStatus);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                handleNetworkFailure(context, adminVideoId, downloadStatus);
            }
        });
    }

    private static void handleNetworkFailure(Context context, int adminVideoId, int downloadStatus) {
        switch (downloadStatus) {
            case DownloadStatus.DOWNLOAD_CANCEL_STATUS:
                putDownloadForCancel(context, adminVideoId);
                break;
            case DownloadStatus.DOWNLOAD_COMPLETE_STATUS:
                break;
            case DownloadStatus.DOWNLOAD_DELETED_STATUS:
                removeFromSharedPrefDeleteList(context, adminVideoId);
                break;
            case DownloadStatus.DOWNLOAD_INITIATE_STATUS:
                break;
        }
    }

    public int downloadVideo(final DownloadRequest downloadRequest, final int notificationId) {
        final int adminVideoId = Integer.parseInt(downloadRequest.getFileName().split("\\.")[0]);

        if (!map.containsKey(notificationId)) {
            map.put(notificationId, 0L);
        }
        NotificationCompat.Action cancelAction = getCancelActionForVideoId(notificationId);
        builder = showDownloadingNotification(context, downloadRequest.getFileName(), "", cancelAction);
        manager.notify(notificationId, builder.build());

        downloadRequest.setOnStartOrResumeListener(() -> {
            Log.d("LOAD", "Started");
            downloadStart(context, adminVideoId);
        }).setOnPauseListener(() -> {
        }).setOnProgressListener(progress -> {
            if (progress.currentBytes - (map.get(notificationId)) > 300000) {
                map.put(notificationId, progress.currentBytes);
                builder.setContentText(getFileSize(progress.currentBytes) + " / " + getFileSize(progress.totalBytes));
                builder.setProgress(100, (int) ((progress.currentBytes * 100) / progress.totalBytes), false);
                manager.notify(notificationId, builder.build());
            }
        }).setOnCancelListener(() -> {
            manager.cancel(notificationId);
            UiUtils.showShortToast(context, context.getString(R.string.download_cancelled));
            downloadCanceled(context, adminVideoId);
            downloadCompleteListener.downloadCancelled(adminVideoId);
            //Sending broadcast to single video page
            sendBroadCastToSingleVideoPage(context, adminVideoId, false);
        });

        //start download
        int downloadId = downloadRequest.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                Log.d("LOAD", "Done");
                downloadCompleted(context, adminVideoId);
                downloadCompleteListener.downloadCompleted(adminVideoId);
                UiUtils.showShortToast(context, downloadRequest.getFileName().split("\\.")[2] + " stored for offline viewing!");
                manager.cancel(notificationId);
                //Sending broadcast to single video page
                sendBroadCastToSingleVideoPage(context, adminVideoId, true);
            }

            @Override
            public void onError(Error error) {
                downloadCanceled(context, adminVideoId);
                downloadCompleteListener.downloadCancelled(adminVideoId);
                UiUtils.showShortToast(context, "There was a problem storing " + downloadRequest.getFileName() + " offline!");
                manager.cancel(notificationId);
                //Sending broadcast to single video page
                sendBroadCastToSingleVideoPage(context, adminVideoId, false);
            }
        });

        return downloadId;
    }

    private void sendBroadCastToSingleVideoPage(Context context, int adminVideoId, boolean cancelledOrCompleted) {
        Intent intent = new Intent();
        intent.putExtra(VideoPageActivity.VIDEO_ID, adminVideoId);
        intent.putExtra(VideoPageActivity.CANCELLED_OR_COMPLETED, cancelledOrCompleted);
        intent.setAction(VideoPageActivity.ACTION_DOWNLOAD_UPDATE);
        context.sendBroadcast(intent);
    }

    private NotificationCompat.Action getCancelActionForVideoId(int notificationId) {
        Intent cancel = new Intent(BuildConfig.APPLICATION_ID + ".CANCEL_DOWNLOAD");
        cancel.putExtra(DownloadCancelReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        cancel.setClass(context, DownloadCancelReceiver.class);
        PendingIntent cancelIntent = PendingIntent.getBroadcast(context.getApplicationContext(), (int) System.currentTimeMillis(), cancel, 0);
        return new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel, "Cancel download", cancelIntent)
                .build();
    }

    private NotificationCompat.Builder showDownloadingNotification(Context context, String title, String message, NotificationCompat.Action cancelAction) {

        String channelId = "offlineVideos";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            mChannel.enableVibration(false);
            manager.createNotificationChannel(mChannel);
        }

        Intent notificationIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), (int) System.currentTimeMillis(), notificationIntent, 0);

        //cancel action


        return new NotificationCompat.Builder(context, channelId).setContentTitle(title)
                .setSubText("Fetching video offline..")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker("")
                .setOngoing(true)
                .setVibrate(new long[]{0l})
                .setContentIntent(contentIntent)
                .addAction(cancelAction)
                .setProgress(100, 0, false);
    }

    private void hideDownloadingNotification(final int id) {
        manager.cancel(id);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        for (int i = 0; i < downloadingTasks.size(); i++) {
            hideDownloadingNotification(downloadingTasks.get(i));
        }
    }

    public void setOnDownloadListener(Context context) {
        downloadCompleteListener = (VideoPageActivity) context;
    }
}