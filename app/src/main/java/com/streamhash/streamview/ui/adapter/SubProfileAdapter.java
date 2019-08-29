package com.streamhash.streamview.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.model.SubProfile;
import com.streamhash.streamview.ui.activity.SplashActivity;
import com.streamhash.streamview.ui.activity.SubProfileEditActivity;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import java.util.List;

/**
 * Created by codegama on 16/10/17.
 */

public class SubProfileAdapter extends RecyclerView.Adapter<SubProfileAdapter.ProfileViewHolder> {

    private Context context;
    private List<SubProfile> subProfiles;
    private LayoutInflater inflater;
    private boolean isEditMode;
    private int activeSubProfileId;

    public SubProfileAdapter(Context context, List<SubProfile> subProfiles, boolean isEditMode) {
        this.context = context;
        this.subProfiles = subProfiles;
        this.isEditMode = isEditMode;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        activeSubProfileId = prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, 0);
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(isEditMode ? R.layout.item_sub_profile : R.layout.item_user_small, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {
        final SubProfile subProfile = subProfiles.get(position);
        boolean isActiveProfile = activeSubProfileId == subProfile.getId();
        if (!isEditMode) {
            holder.userRoot.setScaleX(isActiveProfile ? 1.1f : 1.0f);
            holder.userRoot.setScaleY(isActiveProfile ? 1.1f : 1.0f);
            holder.photoLayout.setBackground(isActiveProfile
                    ? context.getResources().getDrawable(R.drawable.drawable_white_border_rect)
                    : null);
        }
        holder.userImage.setAlpha(isEditMode ? 0.3f : 1.0f);
        holder.userEditImg.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        if (subProfile.getId() == 0) {
            holder.userEditImg.setVisibility(View.GONE);
        }
        holder.userName.setText(subProfile.getName());
        GlideApp.with(context)
                .load(subProfile.getId() != 0 ? subProfile.getImage()
                        : R.drawable.ic_add_person)
                .centerCrop()
                .into(holder.userImage);
        holder.userRoot.setOnClickListener(v -> {
            if (isEditMode || subProfile.getId() == 0) {
                Intent intent = new Intent(context, SubProfileEditActivity.class);
                intent.putExtra(SubProfileEditActivity.IS_EDITING, true);
                intent.putExtra(SubProfileEditActivity.ID, subProfile.getId());
                intent.putExtra(SubProfileEditActivity.NAME, subProfile.getName());
                intent.putExtra(SubProfileEditActivity.PICTURE, subProfile.getImage());
                intent.putExtra(SubProfileEditActivity.COUNT, subProfiles.size());
                context.startActivity(intent);
            } else {
                if (!isActiveProfile)
                    setActiveProfileAndReloadApp(subProfile);
            }
        });
    }

    private void setActiveProfileAndReloadApp(SubProfile subProfile) {
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        prefUtils.setValue(PrefKeys.ACTIVE_SUB_PROFILE, subProfile.getId());
        Intent reloadAppWithNewProfile = new Intent(context, SplashActivity.class);
        reloadAppWithNewProfile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(reloadAppWithNewProfile);
    }

    @Override
    public int getItemCount() {
        return subProfiles.size();
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        ImageView userImage, userEditImg;
        View userRoot;
        ViewGroup photoLayout;

        ProfileViewHolder(View itemView) {
            super(itemView);
            userImage = itemView.findViewById(R.id.userImage);
            userEditImg = itemView.findViewById(R.id.userEditImg);
            userName = itemView.findViewById(R.id.userName);
            userRoot = itemView.findViewById(R.id.userRoot);
            photoLayout = itemView.findViewById(R.id.photoLayout);
        }
    }
}
