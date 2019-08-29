package com.streamhash.streamview.gcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

public class InstanceIdServices extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        saveDeviceToken(refreshedToken);
    }

    private void saveDeviceToken(String token) {
        PrefUtils.getInstance(this).setValue(PrefKeys.FCM_TOKEN, token);
    }

}