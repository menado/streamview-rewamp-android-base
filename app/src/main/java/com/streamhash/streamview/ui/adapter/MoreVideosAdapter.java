package com.streamhash.streamview.ui.adapter;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.util.GlideApp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoreVideosAdapter extends RecyclerView.Adapter {
    private static final int VIDEOS = 1;
    private static final int LOADING = 2;

    private Context context;
    private boolean isLoading = true;
    private ArrayList<Video> moreVideos;
    private LayoutInflater layoutInflater;
    private OnLoadMoreVideosListener listener;

    public MoreVideosAdapter(Context applicationContext, ArrayList<Video> moreVideos) {
        this.context = applicationContext;
        this.moreVideos = moreVideos;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnLoadMoreVideosListener(OnLoadMoreVideosListener onLoadMoreVideosListener) {
        this.listener = onLoadMoreVideosListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == VIDEOS) {
            View view = layoutInflater.inflate(R.layout.item_video_large, viewGroup, false);
            return new VideoViewHolder(view);
        }
        View view = layoutInflater.inflate(R.layout.loading, viewGroup, false);
        return new LoadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof VideoViewHolder) {
            final VideoViewHolder holder = (VideoViewHolder) viewHolder;
            Video singleItem = moreVideos.get(i);
            GlideApp.with(context).load(singleItem.getThumbNailUrl()).thumbnail(0.5f)
                    .into(holder.videoTileImg);
            holder.videoTileRoot.setOnClickListener(view -> {
                Intent intent = new Intent(context, VideoPageActivity.class);
                intent.putExtra(VideoPageActivity.VIDEO_ID, singleItem.getAdminVideoId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }
    }

    public void showLoading() {
        if (isLoading && moreVideos != null && listener != null) {
            isLoading = false;
            new android.os.Handler().post(() -> {
                moreVideos.add(null);
                notifyItemInserted(moreVideos.size() - 1);
                listener.onLoadMore(moreVideos.size());
            });
        }
    }

    public void dismissLoading() {
        if (moreVideos != null && moreVideos.size() > 0) {
            moreVideos.remove(moreVideos.size() - 1);
            notifyItemRemoved(moreVideos.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (moreVideos.get(position) != null) {
            return VIDEOS;
        } else {
            return LOADING;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return moreVideos == null ? 0 : moreVideos.size();
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar indicatorView;

        LoadingViewHolder(View itemView) {
            super(itemView);
            indicatorView = itemView.findViewById(R.id.progress);
        }
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.videoTileImg)
        ImageView videoTileImg;
        @BindView(R.id.videoTileRoot)
        ViewGroup videoTileRoot;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
