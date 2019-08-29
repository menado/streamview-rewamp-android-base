package com.streamhash.streamview.ui.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
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

import static com.streamhash.streamview.network.APIConstants.*;

public class TestNetworkActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.our_server)
    TextView ourServer;
    @BindView(R.id.internet_server)
    TextView internetServer;
    @BindView(R.id.testDisplayArea)
    LinearLayout testDisplayArea;
    @BindView(R.id.checkNetworkImage)
    ImageView checkNetworkImage;
    @BindView(R.id.testStatus)
    TextView testStatus;
    @BindView(R.id.errorDescription)
    TextView errorDescription;
    @BindView(R.id.testingProgress)
    ProgressBar testingProgress;
    @BindView(R.id.controlArea)
    LinearLayout controlArea;
    @BindView(R.id.testBtn)
    Button testBtn;

    Drawable successDrawable;
    Drawable failedDrawable;
    Drawable emptyDrawable;
    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_network);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);

        successDrawable = getResources().getDrawable(R.drawable.ic_done_white_24dp);
        failedDrawable = getResources().getDrawable(R.drawable.ic_close_white_24dp);
        emptyDrawable = getResources().getDrawable(android.R.color.transparent);

        testDisplayArea.setVisibility(View.GONE);
        testingProgress.setVisibility(View.INVISIBLE);
        checkNetworkImage.setVisibility(View.VISIBLE);

        testStatus.setVisibility(View.VISIBLE);
        testStatus.setText(getString(R.string.check_your_network_title));
        errorDescription.setVisibility(View.VISIBLE);
        errorDescription.setText(String.format(getString(R.string.test_your_network_from), getString(R.string.app_name)));
    }


    @OnClick(R.id.testBtn)
    public void onViewClicked() {
        testDisplayArea.setVisibility(View.VISIBLE);
        testingProgress.setVisibility(View.VISIBLE);
        checkNetworkImage.setVisibility(View.GONE);
        ourServer.setCompoundDrawablesRelativeWithIntrinsicBounds(emptyDrawable, null, null, null);
        internetServer.setCompoundDrawablesRelativeWithIntrinsicBounds(emptyDrawable, null, null, null);

        PrefUtils preferences = PrefUtils.getInstance(this);
        Call<String> call = apiInterface.getAppConfigs(preferences.getIntValue(PrefKeys.USER_ID, -1)
                , preferences.getStringValue(PrefKeys.SESSION_TOKEN, ""));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                testingProgress.setVisibility(View.INVISIBLE);
                testBtn.setText(getString(R.string.test_again));
                JSONObject networkCheckResponse = null;
                try {
                    networkCheckResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (networkCheckResponse != null) {
                    if (networkCheckResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        updateViews(true, getString(R.string.network_check_success));
                        ourServer.setCompoundDrawablesRelativeWithIntrinsicBounds(successDrawable, null, null, null);
                        internetServer.setCompoundDrawablesRelativeWithIntrinsicBounds(successDrawable, null, null, null);
                    } else {
                        updateViews(false, String.format("Something wrong with %s server", getString(R.string.app_name)));
                        ourServer.setCompoundDrawablesRelativeWithIntrinsicBounds(failedDrawable, null, null, null);
                        internetServer.setCompoundDrawablesRelativeWithIntrinsicBounds(successDrawable, null, null, null);
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                testingProgress.setVisibility(View.INVISIBLE);
                updateViews(false, getString(R.string.test_failed_desc));
                ourServer.setCompoundDrawablesRelativeWithIntrinsicBounds(failedDrawable, null, null, null);
                internetServer.setCompoundDrawablesRelativeWithIntrinsicBounds(failedDrawable, null, null, null);
            }
        });
    }

    private void updateViews(boolean isSuccess, String message) {
        errorDescription.setVisibility(isSuccess ? View.GONE : View.VISIBLE);
        testStatus.setText(isSuccess ? getString(R.string.network_check_success) : getString(R.string.network_check_failed));
        if (!isSuccess)
            errorDescription.setText(message);
    }
}
