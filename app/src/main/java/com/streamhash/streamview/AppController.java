package com.streamhash.streamview;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class AppController extends Application {

    private static AppController mInstance;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG)
            Fabric.with(this, new Crashlytics());
        mInstance = this;
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/poppins_medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
    }


}

