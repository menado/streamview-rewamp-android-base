package com.streamhash.streamview.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import com.downloader.PRDownloader;
import com.streamhash.streamview.R;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.download.Downloader;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class NetworkUtils {
    private NetworkUtils() {

    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    @SuppressLint("HardwareIds")
    public static String getDeviceToken(Context context) {
        return Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static void onApiError(Context context) {
        UiUtils.hideLoadingDialog();
        UiUtils.showShortToast(context, context.getString(R.string.something_went_wrong));
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static int getUniqueId(String url, String dirPath, String fileName) {
        String string = url + File.separator + dirPath + File.separator + fileName;

        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);

        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString().hashCode();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void downloadVideo(Context context, int adminVideoId, String name, String url) {
        String fileName = MessageFormat.format("{0}.30.{1}.mp4", adminVideoId, name);
        Downloader downloader = new Downloader(context);
        String savePath = context.getExternalFilesDir(null).getPath() + "/" + PrefUtils.getInstance(context)
                .getIntValue(PrefKeys.USER_ID, 0);

        //get download id ahead of time
        int notificationId = getUniqueId(url, savePath, fileName);

        downloader.setOnDownloadListener(context);
        downloader.downloadVideo(PRDownloader.download(url, savePath, fileName).build(), notificationId);

        UiUtils.showShortToast(context, String.format("Starting download: %s", name));
    }

}
