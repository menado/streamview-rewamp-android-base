package com.streamhash.streamview.ui.activity;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.AppUtils;
import com.streamhash.streamview.util.UiUtils;

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

public class ForgotPasswordActivity extends BaseActivity {

    @BindView(R.id.ed_email)
    EditText edEmail;
    @BindView(R.id.layout_email)
    TextInputLayout layoutEmail;
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);
    }

    @OnClick({R.id.submit_btn, R.id.back_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.submit_btn:
                if (validateFields()) {
                    sendConfirmationMail();
                }
                break;
            case R.id.back_btn:
                onBackPressed();
                break;
        }
    }

    private boolean validateFields() {
        if (edEmail.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(this, getString(R.string.email_cant_be_empty));
            return false;
        }
        if (!AppUtils.isValidEmail(edEmail.getText().toString())) {
            UiUtils.showShortToast(this, getString(R.string.enter_valid_email));
            return false;
        }
        return true;
    }

    private void sendConfirmationMail() {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.forgotPassword(edEmail.getText().toString());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject forgotPasswordResponse = null;
                try {
                    forgotPasswordResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (forgotPasswordResponse != null) {
                    if (forgotPasswordResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(ForgotPasswordActivity.this, forgotPasswordResponse.optString(Params.MESSAGE));
                        finish();
                    } else {
                        UiUtils.showShortToast(ForgotPasswordActivity.this, forgotPasswordResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(ForgotPasswordActivity.this);
            }
        });
    }
}
