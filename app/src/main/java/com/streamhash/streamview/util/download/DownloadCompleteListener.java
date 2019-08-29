package com.streamhash.streamview.util.download;

public interface DownloadCompleteListener {
    void downloadCompleted(int adminVideoId);

    void downloadCancelled(int adminVideoId);
}
