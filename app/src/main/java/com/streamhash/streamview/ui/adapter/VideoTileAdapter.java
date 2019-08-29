package com.streamhash.streamview.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.streamhash.streamview.R;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.ui.activity.PlayerActivity;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.ui.activity.YouTubePlayerActivity;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.UiUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.streamhash.streamview.network.APIConstants.Params.ORIGINAL;

public class VideoTileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIDEO_SECTION_TYPE_ORIGINALS = 1;
    public static final int VIDEO_SECTION_TYPE_NORMAL = 2;

    private LayoutInflater inflater;
    private Context context;
    private ArrayList<Video> videos;
    private int viewType;
    private boolean isInSinglePage;

    public VideoTileAdapter(Context context, ArrayList<Video> videos, int viewType, boolean isInSinglePage) {
        this.context = context;
        this.videos = videos;
        this.viewType = viewType;
        this.isInSinglePage = isInSinglePage;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (isInSinglePage) {
            view = inflater.inflate(R.layout.item_video_large, viewGroup, false);
            return new NormalVideoViewHolder(view);
        } else {

        }
        switch (viewType) {
            case VIDEO_SECTION_TYPE_ORIGINALS:
                view = inflater.inflate(R.layout.item_video_long, viewGroup, false);
                return new OriginalsViewHolder(view);
            default:
                view = inflater.inflate(R.layout.item_video, viewGroup, false);
                return new NormalVideoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Video video = videos.get(position);
        switch (viewType) {
            case VIDEO_SECTION_TYPE_ORIGINALS:
                OriginalsViewHolder originalsViewHolder = (OriginalsViewHolder) viewHolder;
                GlideApp.with(context).load(video.getThumbNailUrl()).thumbnail(0.5f)
                        .into(originalsViewHolder.item);
                originalsViewHolder.videoTileRoot.setOnClickListener(v -> {
                    takeToVideoPage(video);
                });
                originalsViewHolder.videoTileRoot.setOnLongClickListener(v -> {
                    UiUtils.showShortToast(context, video.getTitle());
                    return true;
                });
                break;

            default:
                NormalVideoViewHolder normalVideoViewHolder = (NormalVideoViewHolder) viewHolder;
                GlideApp.with(context).load(video.getThumbNailUrl()).thumbnail(0.5f)
                        .into(normalVideoViewHolder.item);
                normalVideoViewHolder.videoTileRoot.setOnClickListener(v -> {
                    takeToVideoPage(video);
                });
                normalVideoViewHolder.videoTileRoot.setOnLongClickListener(v -> {
                    UiUtils.showShortToast(context, video.getTitle());
                    return true;
                });

                break;
        }
    }

    private void takeToVideoPage(Video video) {
        if (video.isTrailerVideo()) {
            String url = "";
            if (video.getResolutions() != null)
                url = video.getResolutions().get(ORIGINAL);
            switch (video.getVideoType()) {
                case VIDEO_MANUAL:
                case VIDEO_OTHER:
                    if (url != null && Uri.parse(url) != null) {
                        Intent toPlayer = new Intent(context, PlayerActivity.class);
                        toPlayer.putExtra(PlayerActivity.VIDEO_ID, video.getAdminVideoId());
                        toPlayer.putExtra(PlayerActivity.VIDEO_URL, url);
                        toPlayer.putExtra(PlayerActivity.VIDEO_SUBTITLE, video.getSubTitleUrl());
                        context.startActivity(toPlayer);
                    } else {
                        Toast.makeText(context, "Something wrong with the trailer video. Sorry for the inconvenience.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case VIDEO_YOUTUBE:
                    Intent toYouTubePlayer = new Intent(context, YouTubePlayerActivity.class);
                    toYouTubePlayer.putExtra(YouTubePlayerActivity.VIDEO_ID, video.getAdminVideoId());
                    toYouTubePlayer.putExtra(YouTubePlayerActivity.VIDEO_URL, url);
                    context.startActivity(toYouTubePlayer);
                    break;
            }
        } else {
            Intent toVideo = new Intent(context, VideoPageActivity.class);
            toVideo.putExtra(VideoPageActivity.VIDEO_ID, video.getAdminVideoId());
            context.startActivity(toVideo);
        }
    }


    @Override
    public int getItemCount() {
        return videos.size();
    }

    class NormalVideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.videoTileImg)
        ImageView item;
        @BindView(R.id.videoTileRoot)
        ViewGroup videoTileRoot;

        NormalVideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class OriginalsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.videoTileImg)
        ImageView item;
        @BindView(R.id.videoTileRoot)
        ViewGroup videoTileRoot;

        OriginalsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
