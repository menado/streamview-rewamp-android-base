package com.streamhash.streamview.model;

import android.view.View;

public class SettingsItem {

    private String settingName;
    private String settingSubName;
    private View.OnClickListener clickListener;

    public SettingsItem(String settingName, String settingSubName, View.OnClickListener click) {
        this.settingName = settingName;
        this.settingSubName = settingSubName;
        this.clickListener = click;
    }

    public String getSettingSubName() {
        return settingSubName;
    }

    public void setSettingSubName(String settingSubName) {
        this.settingSubName = settingSubName;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }


    public View.OnClickListener getClickListener() {
        return clickListener;
    }

}
