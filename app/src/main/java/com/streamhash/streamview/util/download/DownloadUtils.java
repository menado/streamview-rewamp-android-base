package com.streamhash.streamview.util.download;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.Toast;

import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import java.io.File;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DownloadUtils {
    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }

    public static String getFileSize(long size) {
        if (size <= 0)
            return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getFileName(String name) {
        try {
            return name.split("\\.")[2];
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getFileExpiry(String absolutePath) {
        try {
            File file = new File(absolutePath);
            Date lastModDate = new Date(file.lastModified());
            return getCountOfDays(lastModDate);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getVideoId(String name) {
        try {
            return Integer.parseInt(name.split("\\.")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getVideoDuration(Context context, String videoAbsolutePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, Uri.fromFile(new File(videoAbsolutePath)));
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();
        return getHhMmSs(Long.parseLong(time));
    }

    private static int getCountOfDays(Date lastModDate) {
        Calendar lastMod = Calendar.getInstance();
        lastMod.setTime(lastModDate);
        long msDiff = Calendar.getInstance().getTimeInMillis() - lastMod.getTimeInMillis();
        long daysDiff = TimeUnit.MILLISECONDS.toDays(msDiff);
        return (int) daysDiff;
    }


    public static String getHhMmSs(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static void deleteVideoFile(Context context, String absolutePath) {
        File deleteFile = new File(absolutePath);
        deleteFile.delete();
    }

    public static boolean isOfflineVideoExisting(Context context, int adminVideoId) {
        try {
            for (File tempFile : new File(context.getExternalFilesDir(null).getPath() + "/" + PrefUtils.getInstance(context).getIntValue(PrefKeys.USER_ID, 0)).listFiles()) {
                try {
                    String id = tempFile.getName().split("\\.")[0];
                    if (Integer.parseInt(id) == adminVideoId) {
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Downloader.downloadDeleted(context, adminVideoId);
        return false;
    }


    public static boolean isValidVideoFile(Context context, Uri videoUri) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);
            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
            return "yes".equals(hasVideo);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getVideoPath(Context context, int adminVideoId, String url) {
        try {
            for (File tempFile : new File(context.getExternalFilesDir(null).getPath() + "/" + PrefUtils.getInstance(context).getIntValue(PrefKeys.USER_ID, 0)).listFiles()) {
                try {
                    String id = tempFile.getName().split("\\.")[0];
                    if (Integer.parseInt(id) == adminVideoId) {
                        return tempFile.getAbsolutePath();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }
}
