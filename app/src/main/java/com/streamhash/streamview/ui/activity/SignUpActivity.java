package com.streamhash.streamview.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.AppUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.streamhash.streamview.network.APIConstants.*;

public class SignUpActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 100;

    @BindView(R.id.ed_name)
    EditText edName;
    @BindView(R.id.layout_name)
    TextInputLayout layoutName;
    @BindView(R.id.ed_email)
    EditText edEmail;
    @BindView(R.id.layout_email)
    TextInputLayout layoutEmail;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.layout_password)
    TextInputLayout layoutPassword;
    @BindView(R.id.ed_phone)
    EditText edPhone;
    @BindView(R.id.layout_phone)
    TextInputLayout layoutPhone;

    APIInterface apiInterface;
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        //Fb setup
        callbackManager = CallbackManager.Factory.create();
        logoutfromFacebook();

        //Google setup
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
    }

    @OnClick({R.id.btn_google_sign, R.id.btn_facebook, R.id.submit_btn, R.id.already_member, R.id.help, R.id.backBtn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_google_sign:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.btn_facebook:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
                LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_VIEW_ONLY);
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        if (AccessToken.getCurrentAccessToken() != null) {
                            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), (object, response) -> {
                                Timber.d("%s", object);
                                doSocialLoginUser(object.optString("id")
                                        , object.optString("email")
                                        , object.optString("name")
                                        , "https://graph.facebook.com/" + object.optString("id") + "/picture?type=large"
                                        , Constants.FACEBOOK_LOGIN);
                            });
                            Bundle params = new Bundle();
                            params.putString("fields", "id,name,link,email,picture");
                            request.setParameters(params);
                            request.executeAsync();
                            onBackPressed();
                        }
                    }

                    @Override
                    public void onCancel() {
                        LoginManager.getInstance().logOut();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        UiUtils.showShortToast(SignUpActivity.this, getString(R.string.login_cancelled));
                    }
                });
                break;
            case R.id.submit_btn:
                if (validateFields()) {
                    doSignUpUser();
                }
                break;
            case R.id.already_member:
                finish();
                break;
            case R.id.help:
                startActivity(new Intent(this, WebViewActivity.class));
                break;
            case R.id.backBtn:
                onBackPressed();
                break;
        }
    }

    private void doSignUpUser() {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.signUpUser(
                edEmail.getText().toString()
                , edPassword.getText().toString()
                , edName.getText().toString()
                , edPhone.getText().toString()
                , APIConstants.Constants.MANUAL_LOGIN
                , APIConstants.Constants.ANDROID
                , NetworkUtils.getDeviceToken(this)
        );
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject signUpResponse = null;
                try {
                    signUpResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (signUpResponse != null) {
                    if (signUpResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(SignUpActivity.this, signUpResponse.optString(Params.MESSAGE));
                    } else {
                        UiUtils.showShortToast(SignUpActivity.this, signUpResponse.optString(Params.ERROR_MESSAGE));
                        if (!signUpResponse.optString(Params.ERROR_MESSAGE).contains("taken")) {
                            clearAllFields();
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(SignUpActivity.this);
            }
        });
    }

    private void clearAllFields() {
        edName.setText("");
        edPhone.setText("");
        edEmail.setText("");
        edPassword.setText("");
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
        if (edName.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(this, getString(R.string.names_cant_be_empty));
            return false;
        }
        if (edPassword.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(this, getString(R.string.password_cant_be_empty));
            return false;
        }
        if (edPassword.getText().toString().length() < 6) {
            UiUtils.showShortToast(this, getString(R.string.minimum_six_characters));
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String photoImg;
                try {
                    photoImg = account.getPhotoUrl().toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    photoImg = "";
                }
                doSocialLoginUser(account.getId()
                        , account.getEmail()
                        , account.getDisplayName()
                        , photoImg
                        , Constants.GOOGLE_LOGIN);
            }
        } catch (ApiException e) {
            Timber.d("signUpResult:failed code=%s", e.getStatusCode());
            UiUtils.showShortToast(this, getString(R.string.login_cancelled));
        }
    }

    protected void doSocialLoginUser(String socialUniqueId, String email, String name, String picture, String loginBy) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.socialLoginUser(socialUniqueId
                , loginBy
                , email
                , name
                , ""
                , picture
                , APIConstants.Constants.ANDROID
                , NetworkUtils.getDeviceToken(this));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject socialLoginResponse = null;
                try {
                    socialLoginResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (socialLoginResponse != null) {
                    if (socialLoginResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(SignUpActivity.this, "Welcome, " + name + "!");
                        loginUserInDevice(socialLoginResponse, loginBy);
                    } else {
                        UiUtils.showShortToast(SignUpActivity.this, socialLoginResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
            }
        });
    }

    private void loginUserInDevice(JSONObject data, String loginBy) {
        PrefHelper.setUserLoggedIn(this, data.optInt(Params.USER_ID)
                , data.optString(Params.TOKEN)
                , loginBy
                , data.optString(Params.EMAIL)
                , data.optString(Params.NAME)
                , data.optString(Params.MOBILE)
                , data.optString(Params.DESCRIPTION)
                , data.optString(Params.PICTURE)
                , data.optString(Params.NOTIF_PUSH_STATUS)
                , data.optString(Params.NOTIF_PUSH_STATUS));
        Intent toHome = new Intent(this, MainActivity.class);
        toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toHome);
        this.finish();
    }

    public void logoutfromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/permissions/", null, HttpMethod.DELETE, graphResponse -> LoginManager.getInstance().logOut()).executeAsync();
    }

}