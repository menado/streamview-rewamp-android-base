package com.streamhash.streamview.ui.fragment.bottomsheet;

import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.activity.SplashActivity;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefHelper;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class ChangePasswordBottomSheet extends BottomSheetDialogFragment {

    Unbinder unbinder;
    @BindView(R.id.ed_old_password)
    EditText edOldPassword;
    @BindView(R.id.layout_old_password)
    TextInputLayout layoutOldPassword;
    @BindView(R.id.ed_new_password)
    EditText edNewPassword;
    @BindView(R.id.layout_new_password)
    TextInputLayout layoutNewPassword;
    @BindView(R.id.ed_confirm_password)
    EditText edConfirmPassword;
    @BindView(R.id.layout_confirm_password)
    TextInputLayout layoutConfirmPassword;
    @BindView(R.id.submit_btn)
    Button submitBtn;

    APIInterface apiInterface;


    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.layout_change_password, null);
        unbinder = ButterKnife.bind(this, contentView);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        dialog.setContentView(contentView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.submit_btn)
    protected void changePasswordClicked() {
        if (validated()) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.change_pass))
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        changePasswordInBackend();
                    })
                    .setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel())
                    .create().show();
        }
    }

    private boolean validated() {
        if (edOldPassword.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(getActivity(), getString(R.string.password_cant_be_empty));
            return false;
        }
        if (edNewPassword.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(getActivity(), getString(R.string.password_cant_be_empty));
            return false;
        }
        if (edConfirmPassword.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(getActivity(), getString(R.string.password_cant_be_empty));
            return false;
        }
        if (!edNewPassword.getText().toString().equals(edConfirmPassword.getText().toString())) {
            UiUtils.showShortToast(getActivity(), getString(R.string.password_not_match));
            return false;
        }
        return true;
    }

    private void changePasswordInBackend() {
        UiUtils.showLoadingDialog(getActivity());
        PrefUtils prefUtils = PrefUtils.getInstance(getActivity());
        Call<String> call = apiInterface.changePassword(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , edOldPassword.getText().toString()
                , edNewPassword.getText().toString()
                , edConfirmPassword.getText().toString());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject changePasswordResponse = null;
                try {
                    changePasswordResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (changePasswordResponse != null) {
                    if (changePasswordResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(getActivity(), changePasswordResponse.optString(Params.MESSAGE));
                        doLogOutUser();
                    } else {
                        UiUtils.showShortToast(getActivity(), changePasswordResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(getActivity());
            }
        });
    }

    protected void doLogOutUser() {
        UiUtils.showLoadingDialog(getActivity());
        PrefUtils preferences = PrefUtils.getInstance(getActivity());
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
                    if (logoutResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        UiUtils.showLongToast(getActivity(), getString(R.string.login_again_with_new_password));
                        logOutUserInDevice();
                    } else {
                        UiUtils.showShortToast(getActivity(), logoutResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                    }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(getActivity());
            }
        });
    }

    private void logOutUserInDevice() {
        PrefHelper.setUserLoggedOut(getActivity());
        Intent restartActivity = new Intent(getActivity(), SplashActivity.class);
        restartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(restartActivity);
        getActivity().finish();
    }
}
