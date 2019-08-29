package com.streamhash.streamview.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.model.SettingsItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

    private LayoutInflater inflate;
    private ArrayList<SettingsItem> settingItems;


    public SettingsAdapter(Context context, ArrayList<SettingsItem> settings) {
        this.settingItems = settings;
        inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflate.inflate(R.layout.item_setting, viewGroup, false);
        return new SettingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder viewHolder, int position) {
        SettingsItem profileControlItem = settingItems.get(position);
        viewHolder.settingName.setText(profileControlItem.getSettingName());
        viewHolder.settingSubName.setText(profileControlItem.getSettingSubName());
        viewHolder.root.setOnClickListener(profileControlItem.getClickListener());
    }

    @Override
    public int getItemCount() {
        return settingItems.size();
    }

    class SettingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.settingName)
        TextView settingName;
        @BindView(R.id.settingSubName)
        TextView settingSubName;
        @BindView(R.id.settingItem)
        ViewGroup root;

        SettingViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
