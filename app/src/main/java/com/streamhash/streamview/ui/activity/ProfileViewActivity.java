package com.streamhash.streamview.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.streamhash.streamview.R;
import com.streamhash.streamview.model.SettingsItem;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.ui.adapter.SettingsAdapter;
import com.streamhash.streamview.ui.fragment.bottomsheet.ChangePasswordBottomSheet;
import com.streamhash.streamview.ui.fragment.bottomsheet.EditProfileBottomSheet;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefHelper;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class ProfileViewActivity extends BaseActivity {

    @BindView(R.id.userName)
    TextView userName;
    @BindView(R.id.userEmail)
    TextView userEmail;
    @BindView(R.id.userPhone)
    TextView userPhone;
    @BindView(R.id.userPicture)
    CircularImageView userPicture;
    @BindView(R.id.profileSettingRecycler)
    RecyclerView profileSettingRecycler;

    SettingsAdapter settingsAdapter;
    APIInterface apiInterface;
    private final String[] availableAppLanguages = new String[]{"English", "French"};
    private final String[] availableAppLanguageLocalStr = new String[]{"en", "fr"};
    private int currentLanguageIndex, selectedLanguageIndex;
    private String currentLanguage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);


        //Profile settings setup
        profileSettingRecycler.setLayoutManager(new LinearLayoutManager(this));
        profileSettingRecycler.setHasFixedSize(true);
        settingsAdapter = new SettingsAdapter(this, getSettingItems());
        profileSettingRecycler.setAdapter(settingsAdapter);

        currentLanguage = prefUtils.getStringValue(PrefKeys.APP_LANGUAGE_STRING, "");
        for (int i = 0; i < availableAppLanguageLocalStr.length; i++) {
            if (currentLanguage.equals(availableAppLanguageLocalStr[i])) {
                currentLanguageIndex = selectedLanguageIndex = i;
                break;
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setUpProfileDetails();
    }

    private void setUpProfileDetails() {
        PrefUtils prefUtils = PrefUtils.getInstance(this);
        String name = prefUtils.getStringValue(PrefKeys.USER_NAME, "");
        String phone = prefUtils.getStringValue(PrefKeys.USER_MOBILE, "");
        String email = prefUtils.getStringValue(PrefKeys.USER_EMAIL, "");
        String image = prefUtils.getStringValue(PrefKeys.USER_PICTURE, "");

        userName.setText(name);
        userPhone.setText(phone);
        userEmail.setText(email);
        GlideApp.with(this)
                .load(image)
                .into(userPicture);

        userEmail.setVisibility(email.length() == 0 ? View.GONE : View.VISIBLE);
        userPhone.setVisibility(phone.length() == 0 ? View.GONE : View.VISIBLE);
    }

    private ArrayList<SettingsItem> getSettingItems() {
        ArrayList<SettingsItem> settingItems = new ArrayList<>();
        if (PrefUtils.getInstance(this).getStringValue(PrefKeys.LOGIN_TYPE, "").equals(Constants.MANUAL_LOGIN)) {
            settingItems.add(new SettingsItem(getString(R.string.change_password), ""
                    , v -> {
                BottomSheetDialogFragment changePasswordBottomSheet = new ChangePasswordBottomSheet();
                changePasswordBottomSheet.show(getSupportFragmentManager(), changePasswordBottomSheet.getTag());
            }));
        }
        settingItems.add(new SettingsItem(getString(R.string.view_plans), ""
                , v -> startActivity(new Intent(this, PlansActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.my_plans), ""
                , v -> startActivity(new Intent(this, MyPlansActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.multiLanguage), ""
                , v -> changeLanguage()));
        settingItems.add(new SettingsItem(getString(R.string.wishlist), ""
                , v -> startActivity(new Intent(this, WishListActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.cards), ""
                , v -> startActivity(new Intent(this, PaymentsActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.paid_videos), ""
                , v -> startActivity(new Intent(this, PaidVideosActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.spam_videos), ""
                , v -> startActivity(new Intent(this, SpamVideosActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.history), ""
                , v -> startActivity(new Intent(this, HistoryActivity.class))));
        settingItems.add(new SettingsItem(getString(R.string.delete_acc), ""
                , v -> showDeleteAccountDialog()));
        return settingItems;
    }

    private void changeLanguage() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ProfileViewActivity.this, R.style.Theme_AppCompat_Dialog_Alert_ChooseLang);
        builder.setTitle(getString(R.string.update_language));
        builder.setSingleChoiceItems(availableAppLanguages, currentLanguageIndex, (dialog, which) -> selectedLanguageIndex = which);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
            currentLanguageIndex = selectedLanguageIndex;
            prefUtils.setValue(PrefKeys.APP_LANGUAGE_STRING, availableAppLanguageLocalStr[currentLanguageIndex]);
            prefUtils.setValue(PrefKeys.APP_LANGUAGE, availableAppLanguages[currentLanguageIndex]);
            setLanguage(availableAppLanguageLocalStr[currentLanguageIndex]);
        });
        builder.create().show();
    }

    private void showDeleteAccountDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_delete_account);
        TextView deleteTextDesc = dialog.findViewById(R.id.sureToDeleteTextDesc);
        EditText password = dialog.findViewById(R.id.deleteAccountPassword);
        View passwordLayout = dialog.findViewById(R.id.deleteAccountLayout);
        if (!PrefUtils.getInstance(this).getStringValue(PrefKeys.LOGIN_TYPE, "").equals(Constants.MANUAL_LOGIN)) {
            password.setVisibility(View.GONE);
            deleteTextDesc.setVisibility(View.GONE);
            passwordLayout.setVisibility(View.GONE);
        }
        Button yes = dialog.findViewById(R.id.yesBtn);
        Button no = dialog.findViewById(R.id.noBtn);
        yes.setOnClickListener(v -> deleteAccount(password.getText().toString()));
        no.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


    protected void deleteAccount(String password) {
        UiUtils.showLoadingDialog(this);
        PrefUtils preferences = PrefUtils.getInstance(this);
        Call<String> call = apiInterface.deleteAccount(
                preferences.getIntValue(PrefKeys.USER_ID, -1)
                , preferences.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , password);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject deleteAccountResponse = null;
                try {
                    deleteAccountResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (deleteAccountResponse != null)
                    if (deleteAccountResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(ProfileViewActivity.this, deleteAccountResponse.optString(Params.MESSAGE));
                        logOutUserInDevice();
                    } else {
                        UiUtils.showShortToast(ProfileViewActivity.this, deleteAccountResponse.optString(Params.ERROR_MESSAGE));
                    }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(ProfileViewActivity.this);
            }
        });
    }

    private void logOutUserInDevice() {
        PrefHelper.setUserLoggedOut(this);
        Intent restartActivity = new Intent(this, SplashActivity.class);
        restartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(restartActivity);
        this.finish();
    }

    @OnClick(R.id.userEditProfile)
    public void onEditProfile() {
        BottomSheetDialogFragment editProfileBottomSheet = new EditProfileBottomSheet();
        editProfileBottomSheet.show(getSupportFragmentManager(), editProfileBottomSheet.getTag());
    }

    @OnClick(R.id.back_btn)
    protected void backPressed() {
        onBackPressed();
    }
}
