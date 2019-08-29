package com.streamhash.streamview.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.streamhash.streamview.R;
import com.streamhash.streamview.model.SubProfile;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.ui.adapter.SubProfileAdapter;
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

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class ManageSubProfileActivity extends BaseActivity {

    public static final String IS_EDIT_MODE = "isEditMode";
    SubProfileAdapter subProfileAdapter;
    @BindView(R.id.profilesRecyclerView)
    RecyclerView profilesRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    APIInterface apiInterface;
    private ArrayList<SubProfile> subProfiles = new ArrayList<>();
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_profile);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        Intent caller = getIntent();
        isEditMode = caller.getBooleanExtra(IS_EDIT_MODE, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpToolbar();
        setUpProfiles();
    }

    private void setUpToolbar() {
        if (isEditMode) toolbar.setNavigationOnClickListener(v -> onBackPressed());
        else toolbar.setNavigationIcon(null);
        toolbar.setTitle(isEditMode ? getString(R.string.manage_profile) : getString(R.string.who_s_watching));
    }

    private void setUpProfiles() {
        subProfileAdapter = new SubProfileAdapter(this, subProfiles, isEditMode);
        profilesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        profilesRecyclerView.setHasFixedSize(true);
        profilesRecyclerView.setAdapter(subProfileAdapter);
        getProfiles();
    }

    protected void getProfiles() {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.getSubProfiles(
                id
                , token
                , Constants.ANDROID);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                subProfiles.clear();
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
                                SubProfile subProfile = new SubProfile(user.optInt(Params.SUB_PROFILE_USER_ID)
                                        , user.optString(Params.NAME)
                                        , user.optString(Params.PICTURE));
                                if (subProfile.getId() == 0)
                                    if (!isEditMode)
                                        continue;
                                subProfiles.add(subProfile);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        subProfileAdapter.notifyDataSetChanged();
                    } else {
                        UiUtils.showShortToast(ManageSubProfileActivity.this, subProfilesResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(ManageSubProfileActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isEditMode)
            super.onBackPressed();
    }
}
