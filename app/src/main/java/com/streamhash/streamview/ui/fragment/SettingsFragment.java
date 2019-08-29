package com.streamhash.streamview.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.streamhash.streamview.R;
import com.streamhash.streamview.model.SettingsItem;
import com.streamhash.streamview.model.SubProfile;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.ui.activity.AppSettingsActivity;
import com.streamhash.streamview.ui.activity.MainActivity;
import com.streamhash.streamview.ui.activity.ManageSubProfileActivity;
import com.streamhash.streamview.ui.activity.NotificationsActivity;
import com.streamhash.streamview.ui.activity.ProfileViewActivity;
import com.streamhash.streamview.ui.activity.SplashActivity;
import com.streamhash.streamview.ui.adapter.SubProfileAdapter;
import com.streamhash.streamview.ui.adapter.SettingsAdapter;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefHelper;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class SettingsFragment extends Fragment {

    @BindView(R.id.profileRecyclerView)
    RecyclerView profileRecyclerView;
    @BindView(R.id.settingsRecycler)
    RecyclerView settingsRecycler;
    @BindView(R.id.loadingSubProfiles)
    ShimmerFrameLayout loadingSubProfiles;

    private Unbinder unbinder;
    private APIInterface apiInterface;
    private MainActivity activity;
    private SubProfileAdapter subProfileAdapter;
    private SettingsAdapter settingsAdapter;
    private ArrayList<SubProfile> subProfiles = new ArrayList<>();
    private ArrayList<SettingsItem> settingItems = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        apiInterface = APIClient.getClient().create(APIInterface.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpProfiles();
        setUpSettingItems();
    }

    private void setUpProfiles() {
        subProfileAdapter = new SubProfileAdapter(activity, subProfiles, false);
        profileRecyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        profileRecyclerView.setHasFixedSize(true);
        profileRecyclerView.setAdapter(subProfileAdapter);
        getProfiles();
    }

    private void setUpSettingItems() {
        getSettingItems();
        settingsRecycler.setLayoutManager(new LinearLayoutManager(activity));
        settingsRecycler.setHasFixedSize(true);
        settingsAdapter = new SettingsAdapter(activity, settingItems);
        settingsRecycler.setAdapter(settingsAdapter);
        //Get notification count and update adapter in success response
        getNotificationCountFromBackendAsync();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.managerProfiles)
    public void onManageProfile() {
        startActivity(new Intent(getActivity(), ManageSubProfileActivity.class));
    }

    protected void getProfiles() {
        loadingSubProfiles.setVisibility(View.VISIBLE);
        profileRecyclerView.setVisibility(View.GONE);
        PrefUtils prefUtils = PrefUtils.getInstance(getActivity());
        Call<String> call = apiInterface.getSubProfiles(
                prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , Constants.ANDROID);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    JSONObject subProfilesResponse = null;
                    try {
                        subProfilesResponse = new JSONObject(response.body());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (subProfilesResponse != null) {
                        subProfiles.clear();
                        if (subProfilesResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                            JSONArray data = subProfilesResponse.optJSONArray(Params.DATA);
                            for (int i = 0; i < data.length(); i++) {
                                try {
                                    JSONObject user = data.getJSONObject(i);
                                    subProfiles.add(new SubProfile(user.optInt(Params.SUB_PROFILE_USER_ID)
                                            , user.optString(Params.NAME)
                                            , user.optString(Params.PICTURE)));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            subProfileAdapter.notifyDataSetChanged();
                        } else {
                            UiUtils.showShortToast(activity, subProfilesResponse.optString(Params.ERROR_MESSAGE));
                        }
                    }
                    loadingSubProfiles.setVisibility(View.GONE);
                    profileRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(activity);
                if (isAdded()) {
                    loadingSubProfiles.setVisibility(View.GONE);
                    profileRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getSettingItems() {
        settingItems = new ArrayList<>();
        settingItems.add(new SettingsItem(getString(R.string.app_settings), ""
                , v -> startActivity(new Intent(activity, AppSettingsActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.account), ""
                , v -> startActivity(new Intent(activity, ProfileViewActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.notifications), ""
                , v -> startActivity(new Intent(activity, NotificationsActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.log_out), "", v -> logOutConfirm()));
    }

    private void logOutConfirm() {
        new AlertDialog.Builder(activity)
                .setMessage(String.format("%s logout?", getString(R.string.sure_to_text)))
                .setTitle(getString(R.string.logout_confirmation))
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> doLogOutUser())
                .setNegativeButton(getString(R.string.no), (dialog, which) -> {
                }).create().show();
    }

    protected void doLogOutUser() {
        UiUtils.showLoadingDialog(activity);
        PrefUtils preferences = PrefUtils.getInstance(activity);
        Call<String> call = apiInterface.logOutUser(
                preferences.getIntValue(PrefKeys.USER_ID, -1)
                , preferences.getStringValue(PrefKeys.SESSION_TOKEN, ""));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject logoutResponse = null;
                try {
                    logoutResponse = new JSONObject(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (logoutResponse != null)
                    if (logoutResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(activity, logoutResponse.optString(Params.MESSAGE));
                        logOutUserInDevice();
                    } else {
                        UiUtils.showShortToast(activity, logoutResponse.optString(Params.ERROR_MESSAGE));
                    }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(activity);
            }
        });
    }

    protected void getNotificationCountFromBackendAsync() {
        PrefUtils preferences = PrefUtils.getInstance(activity);
        Call<String> call = apiInterface.getNotificationCount(
                preferences.getIntValue(PrefKeys.USER_ID, -1)
                , preferences.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , preferences.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    JSONObject notificationCountResponse = null;
                    try {
                        notificationCountResponse = new JSONObject(response.body());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (notificationCountResponse != null)
                        if (notificationCountResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {

                            //get count
                            String count = notificationCountResponse.optString(Params.COUNT) + " Unread";

                            //Update count and update adapter
                            for (SettingsItem item : settingItems) {
                                if (item.getSettingName().equals(getString(R.string.notifications))) {
                                    item.setSettingSubName(count);
                                    settingsAdapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(activity);
            }
        });
    }

    private void logOutUserInDevice() {
        PrefHelper.setUserLoggedOut(activity);
        Intent restartActivity = new Intent(activity, SplashActivity.class);
        restartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(restartActivity);
        activity.finish();
    }

}
