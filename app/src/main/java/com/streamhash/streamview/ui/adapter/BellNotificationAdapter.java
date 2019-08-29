package com.streamhash.streamview.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.NotificationItem;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.util.GlideApp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BellNotificationAdapter extends RecyclerView.Adapter {
    private static final int NOTIFICATIONS = 1;
    private static final int LOADING = 2;
    private Context context;
    private boolean isLoading = true;
    private ArrayList<NotificationItem> notifications;
    private LayoutInflater inflater;
    private OnLoadMoreVideosListener listener;

    public BellNotificationAdapter(Context context, ArrayList<NotificationItem> listView) {
        this.context = context;
        this.notifications = listView;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnLoadMoreVideosListener(OnLoadMoreVideosListener onLoadMoreVideosListener) {
        this.listener = onLoadMoreVideosListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == NOTIFICATIONS) {
            View view = inflater.inflate(R.layout.item_bell_notification, viewGroup, false);
            return new NotificationsViewHolder(view);
        }
        View view = inflater.inflate(R.layout.loading, viewGroup, false);
        return new LoadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof NotificationsViewHolder) {
            final NotificationsViewHolder holder = (NotificationsViewHolder) viewHolder;
            NotificationItem notificationItem = notifications.get(i);
            GlideApp.with(context).load(notificationItem.getImage()).thumbnail(0.5f)
                    .into(holder.notificationImage);
            holder.notificationTitle.setText(notificationItem.getTitle());
            holder.notificationTime.setText(notificationItem.getTime());

            holder.notificationLayout.setOnClickListener(view -> {
                Intent intent = new Intent(context, VideoPageActivity.class);
                intent.putExtra(VideoPageActivity.VIDEO_ID, notificationItem.getAdminVideoId());
                context.startActivity(intent);
            });
        }
    }

    public void showLoading() {
        if (isLoading && notifications != null && listener != null) {
            isLoading = false;
            new Handler().post(() -> {
                notifications.add(null);
                notifyItemInserted(notifications.size() - 1);
                listener.onLoadMore(notifications.size());
            });
        }
    }

    public void dismissLoading() {
        if (notifications != null && notifications.size() > 0) {
            notifications.remove(notifications.size() - 1);
            notifyItemRemoved(notifications.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (notifications.get(position) != null) {
            return NOTIFICATIONS;
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
        return notifications == null ? 0 : notifications.size();
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        ProgressBar indicatorView;
        LoadingViewHolder(View itemView) {
            super(itemView);
            indicatorView = itemView.findViewById(R.id.progress);
        }
    }

    class NotificationsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.notificationImage)
        ImageView notificationImage;
        @BindView(R.id.desc)
        TextView notificationDesc;
        @BindView(R.id.notificationTime)
        TextView notificationTime;
        @BindView(R.id.notificationTitle)
        TextView notificationTitle;
        @BindView(R.id.notificationLayout)
        LinearLayout notificationLayout;

        NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
