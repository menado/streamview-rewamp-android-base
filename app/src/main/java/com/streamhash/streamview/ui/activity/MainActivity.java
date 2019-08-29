package com.streamhash.streamview.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.streamhash.streamview.R;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.network.events.APIEvent;
import com.streamhash.streamview.ui.fragment.CategoryFragment;
import com.streamhash.streamview.ui.fragment.DownloadsFragment;
import com.streamhash.streamview.ui.fragment.SearchFragment;
import com.streamhash.streamview.ui.fragment.SettingsFragment;
import com.streamhash.streamview.ui.fragment.VideoContentFragment;
import com.streamhash.streamview.util.AppUtils;
import com.streamhash.streamview.util.ConfigParser;
import com.streamhash.streamview.util.DisplayUtils;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.download.Downloader;
import com.streamhash.streamview.util.sharedpref.PrefHelper;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.streamhash.streamview.util.Fragments.HOME_FRAGMENTS;

public class MainActivity extends BaseActivity {

    public static final String FIRST_TIME = "firstTime";
    public static String CURRENT_FRAGMENT;
    public Fragment fragment;

    @BindView(R.id.networkStatusBar)
    TextView networkStatusBar;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.bottom_navigation)
    AHBottomNavigation bottomNavigation;

    //Fragments
    APIInterface apiInterface;
    VideoContentFragment contentFragment;
    PrefUtils prefUtils;
    BroadcastReceiver connectivityReceiver;
    private boolean doubleTapToExitApp = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        prefUtils = PrefUtils.getInstance(this);
        apiInterface = APIClient.getClient().create(APIInterface.class);

        //Permission check
        AppUtils.permissionCheck(this);
        sendUnreportedDownloadStatuses();

        Intent caller = getIntent();
        if (caller != null) {
            if (prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0) == 0
                    || caller.getBooleanExtra(FIRST_TIME, false)) {
                Intent toWhoIsWatching = new Intent(this, ManageSubProfileActivity.class);
                toWhoIsWatching.putExtra(ManageSubProfileActivity.IS_EDIT_MODE, false);
                startActivity(toWhoIsWatching);
            } else {
                getConfigFromBackEnd();
                setUpBottomNavBar();
                contentFragment = VideoContentFragment.getInstance(VideoContentFragment.TYPE_HOME, 0, 0, 0);
                replaceFragmentWithAnimation(contentFragment, HOME_FRAGMENTS[0], false);
            }
        }
    }

    private void sendUnreportedDownloadStatuses() {
        Timber.d("Sending unreported stuff");
        try {
            if (NetworkUtils.isNetworkConnected(MainActivity.this)) {
                String pendingCancels = prefUtils.getStringValue(PrefKeys.CANCEL_VIDEOS_DOWNLOAD, "");
                String cancelIds[] = new String[0];
                try {
                    cancelIds = pendingCancels.split("\\.+");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (String id : cancelIds) {
                    if (id.trim().length() > 0) {
                        try {
                            Downloader.downloadCanceled(MainActivity.this, Integer.parseInt(id));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Report backend: Delete "failed reporting to backend" videos
        try {
            if (NetworkUtils.isNetworkConnected(MainActivity.this)) {
                String pendingCancels = prefUtils.getStringValue(PrefKeys.DELETE_VIDEOS_DOWNLOAD, "");
                String cancelIds[] = new String[0];
                try {
                    cancelIds = pendingCancels.split("\\.+");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (String id : cancelIds) {
                    if (id.trim().length() > 0) {
                        try {
                            Downloader.downloadDeleted(MainActivity.this, Integer.parseInt(id));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getConfigFromBackEnd() {
        Call<String> call = apiInterface.getAppConfigs(id
                , token);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                JSONObject configResponse = null;
                try {
                    configResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (configResponse != null) {
                    if (configResponse.optString(APIConstants.Params.SUCCESS).equals(APIConstants.Constants.TRUE)) {
                        JSONObject data = configResponse.optJSONObject(APIConstants.Params.DATA);
                        Timber.d(String.valueOf(ConfigParser.getConfig(data)));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
            }
        });
    }

    private void setUpBottomNavBar() {
        AHBottomNavigationItem navItemExplore = new AHBottomNavigationItem(getString(R.string.nav_bar_home), R.drawable.home);
        AHBottomNavigationItem navBarSaved = new AHBottomNavigationItem(getString(R.string.nav_bar_search), R.drawable.search);
        AHBottomNavigationItem navBarTrips = new AHBottomNavigationItem(getString(R.string.nav_bar_category), R.drawable.category);
        AHBottomNavigationItem navBarInbox = new AHBottomNavigationItem(getString(R.string.nav_bar_downloads), R.drawable.ic_download);
        AHBottomNavigationItem navBarProfile = new AHBottomNavigationItem(getString(R.string.more), R.drawable.menu);
        bottomNavigation.addItem(navItemExplore);
        bottomNavigation.addItem(navBarSaved);
        bottomNavigation.addItem(navBarTrips);
        bottomNavigation.addItem(navBarInbox);
        bottomNavigation.addItem(navBarProfile);
        bottomNavigation.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        bottomNavigation.setBehaviorTranslationEnabled(false);
        bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.search_bar_bg));
        bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.bottom_bar_item_idle));
        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        bottomNavigation.setForceTint(true);
        bottomNavigation.setCurrentItem(0);
        bottomNavigation.setTitleTextSize(DisplayUtils.dpToPx(10), DisplayUtils.dpToPx(10));

        //Set bottom bar selectedCheckInTime listener
        bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {



            CURRENT_FRAGMENT = HOME_FRAGMENTS[position];
            switch (position) {
                case 0:
                    contentFragment = VideoContentFragment.getInstance(VideoContentFragment.TYPE_HOME, 0, 0, 0);
                    replaceFragmentWithAnimation(contentFragment, HOME_FRAGMENTS[position], false);
                    break;
                case 1:
                    replaceFragmentWithAnimation(new SearchFragment(), HOME_FRAGMENTS[position], false);
                    break;
                case 2:
                    replaceFragmentWithAnimation(new CategoryFragment(), HOME_FRAGMENTS[position], false);
                    break;
                case 3:
                    replaceFragmentWithAnimation(new DownloadsFragment(), HOME_FRAGMENTS[position], false);
                    break;
                case 4:
                    replaceFragmentWithAnimation(new SettingsFragment(), HOME_FRAGMENTS[position], false);
                    break;
            }
            return true;
        });
    }

    private void clearFragmentBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private int getFragmentBackStackSize() {
        FragmentManager fm = getSupportFragmentManager();
        return fm.getBackStackEntryCount();
    }

    public void replaceFragmentWithAnimation(Fragment fragment, String tag, boolean toBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        CURRENT_FRAGMENT = tag;
        this.fragment = fragment;
        if (toBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        if (!CURRENT_FRAGMENT.equals(HOME_FRAGMENTS[0])) {
            if (getFragmentBackStackSize() > 0) {
                super.onBackPressed();
                return;
            } else {
                bottomNavigation.setCurrentItem(0);
                return;
            }
        }

        if (doubleTapToExitApp) {
            super.onBackPressed();
            finish();
            return;
        }

        doubleTapToExitApp = true;
        UiUtils.showShortToast(this, getString(R.string.press_back_again_exit));
        new Handler().postDelayed(() -> doubleTapToExitApp = false, 2000);
    }


    @Override
    public void onStart() {
        super.onStart();
        registerEventBus();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendUnreportedDownloadStatuses();
            }
        };
        registerReceiver(connectivityReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterEventBus();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiry(APIEvent event) {
        unregisterEventBus();
        logOutUserFromDevice();
    }

    private void logOutUserFromDevice() {
        PrefHelper.setUserLoggedOut(MainActivity.this);
        Intent restartActivity = new Intent(MainActivity.this, SplashActivity.class);
        restartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(restartActivity);
        MainActivity.this.finish();
    }

    private void registerEventBus() {
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

}
