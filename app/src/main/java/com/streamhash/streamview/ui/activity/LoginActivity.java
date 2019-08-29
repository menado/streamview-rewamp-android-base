package com.streamhash.streamview.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
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

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class LoginActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final int RC_SIGN_IN = 100;

    @BindView(R.id.ed_email)
    EditText edEmail;
    @BindView(R.id.layout_email)
    TextInputLayout layoutEmail;
    @BindView(R.id.ed_password)
    EditText edPassword;
    @BindView(R.id.layout_password)
    TextInputLayout layoutPassword;

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestId()
            .requestProfile()
            .build();
    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    APIInterface apiInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        //Fb setup
        callbackManager = CallbackManager.Factory.create();
        logoutfromFacebook();

        //Google setup
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
    }


    @OnClick({R.id.btn_google_sign, R.id.btn_facebook, R.id.submit_btn, R.id.forgot_pass, R.id.join_now, R.id.help})
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
                        }
                    }

                    @Override
                    public void onCancel() {
                        LoginManager.getInstance().logOut();
                    }

                    @Override
                    public void onError(FacebookException error) {
                    }
                });
                break;
            case R.id.submit_btn:
                if (validateFields()) {
                    doLoginUser();
                }
                break;
            case R.id.forgot_pass:
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;
            case R.id.join_now:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
            case R.id.help:
                startActivity(new Intent(this, WebViewActivity.class));
                break;
        }
    }

    private boolean validateFields() {
        if (edEmail.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(this, getString(R.string.email_cant_be_empty));
            return false;
        }
        if (edPassword.getText().toString().trim().length() == 0) {
            UiUtils.showShortToast(this, getString(R.string.password_cant_be_empty));
            return false;
        }
        if (!AppUtils.isValidEmail(edEmail.getText().toString())) {
            UiUtils.showShortToast(this, getString(R.string.enter_valid_email));
            return false;
        }
        if (edPassword.getText().toString().length() < 6) {
            UiUtils.showShortToast(this, getString(R.string.minimum_six_characters));
            return false;
        }
        return true;
    }


    protected void doLoginUser() {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.loginUser(edEmail.getText().toString()
                , edPassword.getText().toString()
                , Constants.MANUAL_LOGIN
                , Constants.ANDROID
                , NetworkUtils.getDeviceToken(this));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject loginResponse = null;
                try {
                    loginResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (loginResponse != null) {
                    if (loginResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        UiUtils.showShortToast(LoginActivity.this, loginResponse.optString(Params.MESSAGE));
                        loginUserInDevice(loginResponse, Constants.MANUAL_LOGIN);
                    } else {
                        UiUtils.showShortToast(LoginActivity.this, loginResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
            }
        });
    }

    protected void doSocialLoginUser(String socialUniqueId, String email, String name, String picture, String loginBy) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.socialLoginUser(socialUniqueId
                , loginBy
                , email
                , name
                , ""
                , picture
                , Constants.ANDROID
                , NetworkUtils.getDeviceToken(this));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                if(response==null){
                    UiUtils.showShortToast(LoginActivity.this, getString(R.string.login_cancelled));
                    return;
                }
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
                        UiUtils.showShortToast(LoginActivity.this, "Welcome, " + name + "!");
                        loginUserInDevice(socialLoginResponse, loginBy);
                    } else {
                        UiUtils.showShortToast(LoginActivity.this, socialLoginResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(LoginActivity.this);
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
        toHome.putExtra(MainActivity.FIRST_TIME,  true);
        toHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(toHome);
        this.finish();
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
            Timber.d("signInResult:failed code=%s", e.getStatusCode());
            UiUtils.showShortToast(this, getString(R.string.login_cancelled));
        }
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void logoutfromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/permissions/", null, HttpMethod.DELETE, graphResponse -> LoginManager.getInstance().logOut()).executeAsync();
    }
}