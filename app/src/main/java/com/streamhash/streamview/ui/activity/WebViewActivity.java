package com.streamhash.streamview.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.util.Freezable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import android.widget.ScrollView;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebViewActivity extends BaseActivity {

    public static final String PAGE_TYPE = "pageType";
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.progress)
    ProgressBar progress;
    APIInterface apiInterface;
    @BindView(R.id.staticText)
    TextView staticText;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);

        Intent caller = getIntent();
        int pageType = caller.getIntExtra(PAGE_TYPE, PageTypes.ABOUT);

        setUpWebViewDefaults();
        setUpRequiredWebPage(pageType);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebViewDefaults() {
        WebSettings webSettings = webView.getSettings();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                switch (newProgress) {
                    case 0:
                        progress.setVisibility(View.VISIBLE);
                        break;
                    case 100:
                        progress.setVisibility(View.GONE);
                        break;
                    default:
                        progress.setProgress(newProgress);
                }
            }
        });
    }

    private void setUpRequiredWebPage(int pageType) {
        String urlToLoad = "";
        switch (pageType) {
            case PageTypes.ABOUT:
                toolbar.setTitle(getString(R.string.about));
                urlToLoad = APIConstants.STATIC_PAGES.ABOUT_URL;
                scrollView.setVisibility(View.GONE);
                break;

            case PageTypes.TERMS:
                toolbar.setTitle(getString(R.string.terms_conditions));
                urlToLoad = APIConstants.STATIC_PAGES.TERMS_URL;
                getStaticPages(APIConstants.Params.TERMS);
                scrollView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;
            case PageTypes.HELP:
                toolbar.setTitle(getString(R.string.help));
                urlToLoad = APIConstants.STATIC_PAGES.HELP_URL;
                scrollView.setVisibility(View.GONE);
                break;

            case PageTypes.PRIVACY:
                toolbar.setTitle(getString(R.string.privacy_policy));
                urlToLoad = APIConstants.STATIC_PAGES.PRIVACY_URL;
                getStaticPages(APIConstants.Params.PRIVACY_POLICY);
                scrollView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                break;

            case PageTypes.SPEEDTEST:
                toolbar.setTitle(getString(R.string.speed_test));
                urlToLoad = APIConstants.STATIC_PAGES.SPEED_TEST_URL;
                scrollView.setVisibility(View.GONE);
                break;
        }
        webView.loadUrl(urlToLoad);
    }


    public static final class PageTypes {
        public static final int ABOUT = 0;
        public static final int TERMS = 1;
        public static final int HELP = 2;
        public static final int PRIVACY = 3;
        public static final int SPEEDTEST = 4;
    }

    private void getStaticPages(String pageType) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.getStaticPage(pageType);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject staticPagesResponse = null;
                try {
                    staticPagesResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (staticPagesResponse != null) {
                        if (staticPagesResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                            JSONObject data = staticPagesResponse.optJSONObject(APIConstants.Params.DATA);
                            staticText.setText(Html.fromHtml(data.optString(APIConstants.Params.DESCRIPTION)));
                        } else {
                            UiUtils.showShortToast(WebViewActivity.this, staticPagesResponse.optString(APIConstants.Params.ERROR_MESSAGE));
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(WebViewActivity.this);
            }
        });
    }
}
