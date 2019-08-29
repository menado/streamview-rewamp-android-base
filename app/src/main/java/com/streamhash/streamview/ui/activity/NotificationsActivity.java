package com.streamhash.streamview.ui.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.NotificationItem;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.ui.adapter.BellNotificationAdapter;
import com.streamhash.streamview.util.UiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener, OnLoadMoreVideosListener {

    @BindView(R.id.notificationRecycler)
    RecyclerView notificationRecycler;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    APIInterface apiInterface;
    BellNotificationAdapter notificationAdapter;
    ArrayList<NotificationItem> notifications = new ArrayList<>();
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);
        swipe.setOnRefreshListener(this);
        setUpNotifications();
    }

    private void setUpNotifications() {
        notificationAdapter = new BellNotificationAdapter(this, notifications);
        notificationRecycler.setHasFixedSize(true);
        notificationRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        notificationRecycler.setItemAnimator(new DefaultItemAnimator());
        notificationRecycler.setAdapter(notificationAdapter);
        notificationAdapter.setOnLoadMoreVideosListener(this);
        notificationRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (llmanager.findLastCompletelyVisibleItemPosition() == (notificationAdapter.getItemCount() - 1)) {
                    notificationAdapter.showLoading();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getNotifications(0);
    }

    protected void getNotifications(int skip) {
        if (skip == 0)
            swipe.setRefreshing(true);

        Call<String> call = apiInterface.getNotifications(id
                , token
                , subProfileId
                , skip);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (skip == 0) notifications.clear();
                if (swipe.isRefreshing()) swipe.setRefreshing(false);

                JSONObject categoryListObject = null;
                try {
                    categoryListObject = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (categoryListObject != null) {
                    if (categoryListObject.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        JSONArray categoryArray = categoryListObject.optJSONArray(APIConstants.Params.DATA);
                        if (skip != 0) {
                            notificationAdapter.dismissLoading();
                        }
                        for (int i = 0; i < categoryArray.length(); i++) {
                            JSONObject object = categoryArray.optJSONObject(i);
                            NotificationItem notification = new NotificationItem();
                            notification.setAdminVideoId(object.optInt(APIConstants.Params.ADMIN_VIDEO_ID));
                            notification.setTitle(object.optString(APIConstants.Params.TITLE));
                            notification.setImage(object.optString(APIConstants.Params.IMG));
                            notification.setTime(object.optString(APIConstants.Params.TIME));
                            notifications.add(notification);
                        }
                        onDataChanged();
                    } else {
                        UiUtils.showShortToast(NotificationsActivity.this, categoryListObject.optString(APIConstants.Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(NotificationsActivity.this);
            }
        });
    }

    private void onDataChanged() {
        notificationAdapter.notifyDataSetChanged();
        noResultLayout.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        notificationRecycler.setVisibility(notifications.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @Override
    public void onLoadMore(int skip) {
        getNotifications(skip);
    }
}
