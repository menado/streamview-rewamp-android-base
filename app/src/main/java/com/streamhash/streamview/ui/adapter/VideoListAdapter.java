package com.streamhash.streamview.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.activity.HistoryActivity;
import com.streamhash.streamview.ui.activity.SpamVideosActivity;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.ui.activity.WishListActivity;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Params.ERROR_MESSAGE;
import static com.streamhash.streamview.network.APIConstants.Params.SUCCESS;

public class VideoListAdapter extends RecyclerView.Adapter {
    private static final int VIDEOS = 1;
    private static final int LOADING = 2;
    private APIInterface apiInterface;
    private LayoutInflater inflater;
    private Context context;
    private boolean isLoading = true;
    private OnLoadMoreVideosListener listener;
    private ArrayList<Video> videos;
    private VideoListType videoListType;
    private OnDataChangedListener dataChanged;

    public VideoListAdapter(Context activity, ArrayList<Video> singleItem, VideoListType videoListType, OnDataChangedListener dataChanged) {
        this.context = activity;
        this.videos = singleItem;
        this.videoListType = videoListType;
        this.dataChanged = dataChanged;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.apiInterface = APIClient.getClient().create(APIInterface.class);
    }

    public void setOnLoadMoreVideosListener(OnLoadMoreVideosListener onLoadMoreVideosListener) {
        this.listener = onLoadMoreVideosListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        if (i == LOADING) {
            view = inflater.inflate(R.layout.loading, viewGroup, false);
            return new LoadingViewHolder(view);
        } else {
            switch (videoListType) {
                case TYPE_SEASONS:
                    view = inflater.inflate(R.layout.item_season_video_in_list, viewGroup, false);
                    return new VideoViewHolder(view);
                case TYPE_HISTORY:
                case TYPE_SPAM:
                case TYPE_WISH_LIST:
                case TYPE_OTHERS:
                default:
                    view = inflater.inflate(R.layout.item_video_in_list, viewGroup, false);
                    return new VideoViewHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof VideoViewHolder) {
            final VideoViewHolder holder = (VideoViewHolder) viewHolder;
            Video singleItem = videos.get(i);
            GlideApp.with(context).load(singleItem.getThumbNailUrl()).thumbnail(0.5f)
                    .into(holder.wishListImage);
            holder.desc.setText(singleItem.getDescription());
            holder.title.setText(singleItem.getTitle());
            holder.layout.setOnClickListener(view -> {
                if (videoListType != VideoListType.TYPE_SPAM) {
                    Intent intent = new Intent(context, VideoPageActivity.class);
                    intent.putExtra(VideoPageActivity.VIDEO_ID, singleItem.getAdminVideoId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            switch (videoListType) {
                case TYPE_SEASONS:
                    holder.duration.setText(singleItem.getDuration());
                    break;
            }

            holder.layout.setOnLongClickListener(view -> {
                switch (videoListType) {
                    case TYPE_HISTORY:
                    case TYPE_SPAM:
                    case TYPE_WISH_LIST:
                    case TYPE_OTHERS:
                    default:
                        showPopupMenu(view, i);
                        break;

                    case TYPE_SEASONS:
                        break;
                }
                return true;
            });
        }
    }

    private void showPopupMenu(View view, final int position) {
        Context activity = new ContextThemeWrapper(context, R.style.AppTheme_NoActionBar);
        PopupMenu popupMenu = new PopupMenu(activity, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_clear, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.remove:
                    switch (videoListType) {
                        case TYPE_SPAM:
                            new AlertDialog.Builder(context)
                                    .setMessage("Are you sure to clear Spam?")
                                    .setPositiveButton(context.getString(R.string.yes), (dialogInterface, i) -> {
                                        dialogInterface.cancel();
                                        clearSpamVideosInBackend(position, 0);
                                        removeAtPosition(position);
                                        if (videos.size() == 0) {
                                            Intent recreate = new Intent(context, SpamVideosActivity.class);
                                            recreate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            context.startActivity(recreate);
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel())
                                    .create().show();
                            break;
                        case TYPE_HISTORY:
                            new AlertDialog.Builder(context)
                                    .setMessage("Are you sure to clear History?")
                                    .setPositiveButton(context.getString(R.string.yes), (dialogInterface, i) -> {
                                        dialogInterface.cancel();
                                        clearHistoryinBackend(position, 0);
                                        removeAtPosition(position);
                                        if (videos.size() == 0) {
                                            Intent recreate = new Intent(context, HistoryActivity.class);
                                            recreate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            context.startActivity(recreate);
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel())
                                    .create().show();
                            break;
                        case TYPE_WISH_LIST:
                            new AlertDialog.Builder(context)
                                    .setMessage("Are you sure to clear WishList?")
                                    .setPositiveButton(context.getString(R.string.yes), (dialogInterface, i) -> {
                                        dialogInterface.cancel();
                                        clearWishlistInBackend(position, 0);
                                        removeAtPosition(position);
                                        if (videos.size() == 0) {
                                            Intent recreate = new Intent(context, WishListActivity.class);
                                            recreate.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            context.startActivity(recreate);
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.no), (dialogInterface, i) -> dialogInterface.cancel())
                                    .create().show();
                            break;
                        case TYPE_SEASONS:
                            break;
                        case TYPE_OTHERS:
                            popupMenu.getMenu().findItem(R.id.remove).setVisible(false);
                            break;
                    }
            }
            return true;
        });
        popupMenu.show();
    }

    public void showLoading() {
        if (isLoading && videos != null && listener != null) {
            isLoading = false;
            new android.os.Handler().post(() -> {
                videos.add(null);
                notifyItemInserted(videos.size() - 1);
                listener.onLoadMore(videos.size());
            });
        }
    }

    public void dismissLoading() {
        if (videos != null && videos.size() > 0) {
            videos.remove(videos.size() - 1);
            notifyItemRemoved(videos.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (videos.get(position) != null) {
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
        return videos == null ? 0 : videos.size();
    }

    public void clearWishlistInBackend(int position, int status) {
        int id = videos.get(position).getAdminVideoId();
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        Call<String> call = apiInterface.clearWishList(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , id
                , status);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                JSONObject clearWishListResponse = null;
                try {
                    clearWishListResponse = new JSONObject(response.body());

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (clearWishListResponse != null) {
                    if (clearWishListResponse.optString(SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        if (status == 1) {
                            videos.clear();
                            notifyDataSetChanged();
                        }
                        dataChanged.onDataChanged();
                    } else {
                        UiUtils.showShortToast(context, clearWishListResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(context);
            }
        });
    }

    public void clearHistoryinBackend(int position, int status) {
        UiUtils.showLoadingDialog(context);
        int id = videos.get(position).getAdminVideoId();
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        Call<String> call = apiInterface.clearHistory(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , id
                , status);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject clearWishListResponse = null;
                try {
                    clearWishListResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (clearWishListResponse != null) {
                    if (clearWishListResponse.optString(SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        if (status == 1) {
                            videos.clear();
                            notifyDataSetChanged();
                        }
                        dataChanged.onDataChanged();
                    } else {
                        UiUtils.showShortToast(context, clearWishListResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(context);
            }
        });
    }

    private void clearSpamVideosInBackend(int position, int status) {
        UiUtils.showLoadingDialog(context);
        int id = videos.get(position).getAdminVideoId();
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        Call<String> call = apiInterface.clearSpamLsit(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0)
                , id
                , status);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject clearWishListResponse = null;
                try {
                    clearWishListResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (clearWishListResponse != null) {
                    if (clearWishListResponse.optString(SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        if (status == 1) {
                            videos.clear();
                            notifyDataSetChanged();
                        }
                        dataChanged.onDataChanged();
                    } else {
                        UiUtils.showShortToast(context, clearWishListResponse.optString(ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(context);
            }
        });
    }

    private void removeAtPosition(int position) {
        try {
            videos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, videos.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum VideoListType {
        TYPE_SPAM,
        TYPE_WISH_LIST,
        TYPE_HISTORY,
        TYPE_OTHERS,
        TYPE_SEASONS
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress)
        ProgressBar indicatorView;

        LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class VideoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.wishListImage)
        ImageView wishListImage;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.desc)
        TextView desc;
        @BindView(R.id.layout)
        LinearLayout layout;
        @BindView(R.id.duration)
        TextView duration;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
