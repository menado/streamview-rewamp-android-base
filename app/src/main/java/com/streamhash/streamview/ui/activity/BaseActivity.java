package com.streamhash.streamview.ui.activity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate;
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener;
import com.bumptech.glide.Glide;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BaseActivity extends AppCompatActivity implements OnLocaleChangedListener {

    PrefUtils prefUtils;
    int id, subProfileId;
    String token;

    private LocalizationActivityDelegate localizationDelegate = new LocalizationActivityDelegate(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localizationDelegate.addOnLocaleChangedListener(this);
        localizationDelegate.onCreate(savedInstanceState);
        prefUtils = PrefUtils.getInstance(this);
        id = prefUtils.getIntValue(PrefKeys.USER_ID, -1);
        token = prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "");
        subProfileId = prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1);
    }

    @Override
    public void onResume() {
        super.onResume();
        localizationDelegate.onResume(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(localizationDelegate.attachBaseContext(newBase)));
    }

    @Override
    public Context getApplicationContext() {
        return localizationDelegate.getApplicationContext(super.getApplicationContext());
    }

    @Override
    public Resources getResources() {
        return localizationDelegate.getResources(super.getResources());
    }

    public final void setLanguage(String language) {
        localizationDelegate.setLanguage(this, language);
    }

    public final void setLanguage(Locale locale) {
        localizationDelegate.setLanguage(this, locale);
    }

    public final void setDefaultLanguage(String language) {
        localizationDelegate.setDefaultLanguage(language);
    }

    public final void setDefaultLanguage(Locale locale) {
        localizationDelegate.setDefaultLanguage(locale);
    }

    public final Locale getCurrentLanguage() {
        return localizationDelegate.getLanguage(this);
    }

    // Just override method locale change event
    @Override
    public void onBeforeLocaleChanged() {
    }

    @Override
    public void onAfterLocaleChanged() {
    }

}
