package com.streamhash.streamview.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.ui.activity.PlayerActivity;
import com.streamhash.streamview.ui.fragment.DownloadsFragment;
import com.streamhash.streamview.util.download.DownloadUtils;
import com.streamhash.streamview.util.download.Downloader;
import com.streamhash.streamview.util.UiUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.streamhash.streamview.util.download.DownloadUtils.isValidVideoFile;
import static com.streamhash.streamview.util.download.DownloadUtils.isVideoFile;

public class OfflineVideoAdapter extends RecyclerView.Adapter<OfflineVideoAdapter.OfflineViewHolder> {
    private ArrayList<Video> offlineVideos;
    private Context context;
    private OfflineVideoInterface offlineVideoInterface;

    public OfflineVideoAdapter(Context context, ArrayList<Video> data) {
        this.context = context;
        this.offlineVideos = data;
    }


    public void setDownloadVideoListener(OfflineVideoInterface offlineVideoInterface) {
        this.offlineVideoInterface = offlineVideoInterface;
    }

    @Override
    public OfflineVideoAdapter.OfflineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_offline_video, parent, false);
        return new OfflineVideoAdapter.OfflineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final OfflineVideoAdapter.OfflineViewHolder holder, final int position) {
        final Video videoItem = offlineVideos.get(position);

        Bitmap thumbNail = ThumbnailUtils.
                createVideoThumbnail(videoItem.getVideoUrl(), MediaStore.Video.Thumbnails.MICRO_KIND);
        if (thumbNail == null)
            thumbNail = BitmapFactory.decodeResource(context.getResources(), R.drawable.back);


        holder.videoTitle.setText(videoItem.getTitle());
        holder.videoExpiry.setText(String.format("%d Days until expiry", videoItem.getNumDaysToExpire()));
        holder.videoDesc.setText(videoItem.getDescription());
        holder.videoDuration.setText(videoItem.getDuration());
        holder.videoThumb.setImageBitmap(thumbNail);

        if (videoItem.isInSpam()) {
            holder.root.setBackgroundColor(context.getResources().getColor(R.color.light_red));
            holder.root.setOnClickListener(v -> showSpamAndPlay(videoItem));
        } else {
            holder.root.setOnClickListener(v -> playVideo(videoItem));
        }

        //delete offline video
        holder.deleteVideo.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.deleteVideo);
            popup.inflate(R.menu.menu_delete_offline_video);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.deleteOfflineVideo:
                        new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.delete_confirmation))
                                .setMessage("Are you sure to delete the video from offline?")
                                .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> {
                                    //handle delete action click
                                    try {
                                        int adminVideoId = videoItem.getAdminVideoId();
                                        DownloadUtils.deleteVideoFile(context, videoItem.getVideoUrl());
                                        Downloader.downloadDeleted(context, adminVideoId);
                                        offlineVideos.remove(position);
                                        if (offlineVideoInterface != null)
                                            offlineVideoInterface.onVideoDeleted(offlineVideos.size() == 0);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, getItemCount());
                                        UiUtils.showShortToast(context, videoItem.getTitle() + " Removed from offline");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        UiUtils.showShortToast(context, "Something went wrong. Please try again!");
                                    }
                                })
                                .setNegativeButton(context.getString(R.string.no), null)
                                .setIcon(R.mipmap.ic_launcher)
                                .create().show();
                        break;
                }
                return false;
            });
            //displaying the popup
            popup.show();
        });
    }

    private void showSpamAndPlay(Video video) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.spam_video))
                .setMessage("You have marked this video as spam. You want to continue watching?")
                .setPositiveButton(context.getText(R.string.yes), (dialog, which) -> playVideo(video))
                .setNegativeButton(context.getText(R.string.no), null)
                .setIcon(R.mipmap.ic_launcher)
                .create().show();
    }

    @Override
    public int getItemCount() {
        return offlineVideos.size();
    }

    private void playVideo(Video video) {
        Uri videoUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".myprovider", new File(video.getVideoUrl()));
        if (isValidVideoFile(context, videoUri) && isVideoFile(video.getVideoUrl())) {
            Intent toVideo = new Intent(context, PlayerActivity.class);
            toVideo.putExtra(PlayerActivity.VIDEO_ID, video.getAdminVideoId());
            toVideo.putExtra(PlayerActivity.VIDEO_URL, video.getVideoUrl());
            toVideo.putExtra(PlayerActivity.VIDEO_ELAPSED, video.getSeekHere());
            toVideo.putExtra(PlayerActivity.VIDEO_SUBTITLE, video.getSubTitleUrl());
            context.startActivity(toVideo);
        } else {
            UiUtils.showShortToast(context, context.getString(R.string.problem_with_downloaded_video));
        }
    }


    public interface OfflineVideoInterface {
        void onVideoDeleted(boolean isEmpty);
    }

    class OfflineViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.thumbnail)
        ImageView videoThumb;
        @BindView(R.id.title)
        TextView videoTitle;
        @BindView(R.id.expiry)
        TextView videoExpiry;
        @BindView(R.id.description)
        TextView videoDesc;
        @BindView(R.id.videoDuration)
        TextView videoDuration;
        @BindView(R.id.deleteVideo)
        View deleteVideo;
        @BindView(R.id.offlineVideoItem)
        ViewGroup root;

        OfflineViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}