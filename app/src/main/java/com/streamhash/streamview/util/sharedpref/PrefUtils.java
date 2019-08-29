package com.streamhash.streamview.util.sharedpref;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    private static PrefUtils mPrefUtils;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;

    private PrefUtils() {
    }

    private PrefUtils(Context context) {
        mContext = context;
        mSharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    public static synchronized PrefUtils getInstance(Context context) {

        if (mPrefUtils == null) {
            mPrefUtils = new PrefUtils(context.getApplicationContext());
        }
        return mPrefUtils;
    }


    public void setValue(String key, String value) {
        mSharedPreferencesEditor.putString(key, value);
        mSharedPreferencesEditor.commit();
    }


    public void setValue(String key, int value) {
        mSharedPreferencesEditor.putInt(key, value);
        mSharedPreferencesEditor.commit();
    }

    public void setValue(String key, double value) {
        setValue(key, Double.toString(value));
    }

    public void setValue(String key, long value) {
        mSharedPreferencesEditor.putLong(key, value);
        mSharedPreferencesEditor.commit();
    }

    public void setValue(String key, boolean value) {
        mSharedPreferencesEditor.putBoolean(key, value);
        mSharedPreferencesEditor.commit();
    }

    public String getStringValue(String key, String defaultValue) {
        return mSharedPreferences.getString(key, defaultValue);
    }

    public int getIntValue(String key, int defaultValue) {
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public long getLongValue(String key, long defaultValue) {
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public boolean getBoolanValue(String keyFlag, boolean defaultValue) {
        return mSharedPreferences.getBoolean(keyFlag, defaultValue);
    }

    public void removeKey(String key) {
        if (mSharedPreferencesEditor != null) {
            mSharedPreferencesEditor.remove(key);
            mSharedPreferencesEditor.commit();
        }
    }

    public void clear() {
        mSharedPreferencesEditor.clear().commit();
    }
}