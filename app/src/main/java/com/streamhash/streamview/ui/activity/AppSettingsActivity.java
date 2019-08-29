package com.streamhash.streamview.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class AppSettingsActivity extends BaseActivity {

    public static final String PUSH_NOTIFICATIONS = "push";
    public static final String EMAIL_NOTIFICATIONS = "email";

    @BindView(R.id.aboutDevice)
    TextView aboutDevice;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.pushNotificationToggle)
    SwitchCompat pushNotificationToggle;
    @BindView(R.id.emailNotificationToggle)
    SwitchCompat emailNotificationToggle;

    APIInterface apiInterface;

    boolean isPushOn = false;
    boolean isEmailOn = false;
    private CompoundButton.OnCheckedChangeListener pushListener
            = (buttonView, isChecked) -> updateNotificationSetting(PUSH_NOTIFICATIONS, buttonView, isPushOn);
    private CompoundButton.OnCheckedChangeListener emailListener
            = (buttonView, isChecked) -> updateNotificationSetting(EMAIL_NOTIFICATIONS, buttonView, isEmailOn);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);

        aboutDevice.setText(getAboutDeviceText());

        getPushNotificationStatus();
        getEmailNotificationStatus();
    }

    private String getAboutDeviceText() {
        return "Model: " + Build.MODEL + "\n" + "Name: " +
                Build.DEVICE + "\n" + "Serial: " + Build.SERIAL + "\n" +
                "Fingerprint: " + Build.FINGERPRINT + "\n" +
                "Board: " + Build.BOARD + "\n";
    }

    @OnClick({R.id.checkNetworkTile, R.id.speedTestTile, R.id.privacyTile, R.id.termsOfUseTile})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.checkNetworkTile:
                startActivity(new Intent(this, TestNetworkActivity.class));
                break;
            case R.id.speedTestTile:
                startActivity(new Intent(this, WebViewActivity.class)
                        .putExtra(WebViewActivity.PAGE_TYPE, WebViewActivity.PageTypes.SPEEDTEST));
                break;
            case R.id.privacyTile:
                startActivity(new Intent(this, WebViewActivity.class)
                        .putExtra(WebViewActivity.PAGE_TYPE, WebViewActivity.PageTypes.PRIVACY));
                break;
            case R.id.termsOfUseTile:
                startActivity(new Intent(this, WebViewActivity.class)
                        .putExtra(WebViewActivity.PAGE_TYPE, WebViewActivity.PageTypes.TERMS));
                break;
        }
    }

    private void getPushNotificationStatus() {
        isPushOn = PrefUtils.getInstance(this).getBoolanValue(PrefKeys.PUSH_NOTIFICATIONS, true);
        pushNotificationToggle.setOnCheckedChangeListener(null);
        pushNotificationToggle.setChecked(isPushOn);
        pushNotificationToggle.setOnCheckedChangeListener(pushListener);
    }

    private void getEmailNotificationStatus() {
        isEmailOn = PrefUtils.getInstance(this).getBoolanValue(PrefKeys.EMAIL_NOTIFICATIONS, true);
        emailNotificationToggle.setOnCheckedChangeListener(null);
        emailNotificationToggle.setChecked(isEmailOn);
        emailNotificationToggle.setOnCheckedChangeListener(emailListener);
    }

    protected void updateNotificationSetting(String pushSettingItem, CompoundButton button, boolean isOn) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.updateNotificationSetting(
                id
                , token
                , subProfileId
                , pushSettingItem
                , isOn ? 0 : 1);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject notifSettingUpdateResponse = null;
                try {
                    notifSettingUpdateResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (notifSettingUpdateResponse != null) {
                    if (notifSettingUpdateResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(AppSettingsActivity.this, notifSettingUpdateResponse.optString(Params.MESSAGE));
                        switch (pushSettingItem) {
                            case PUSH_NOTIFICATIONS:
                                isPushOn = !isOn;
                                PrefUtils.getInstance(AppSettingsActivity.this).setValue(PrefKeys.PUSH_NOTIFICATIONS, isPushOn);
                                break;
                            case EMAIL_NOTIFICATIONS:
                                isEmailOn = !isOn;
                                PrefUtils.getInstance(AppSettingsActivity.this).setValue(PrefKeys.EMAIL_NOTIFICATIONS, isEmailOn);
                                break;
                        }
                    } else {
                        UiUtils.showShortToast(AppSettingsActivity.this, notifSettingUpdateResponse.optString(Params.ERROR_MESSAGE));
                        rollBackPushToggled(button, isOn);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                rollBackPushToggled(button, isOn);
                NetworkUtils.onApiError(AppSettingsActivity.this);
            }
        });
    }

    private void rollBackPushToggled(CompoundButton button, boolean isOn) {
        button.setChecked(isOn);
    }
}
