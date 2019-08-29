package com.streamhash.streamview.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Category;
import com.streamhash.streamview.ui.activity.MainActivity;
import com.streamhash.streamview.ui.fragment.CategoryFragment;
import com.streamhash.streamview.ui.fragment.VideoContentFragment;
import com.streamhash.streamview.util.GlideApp;
import com.streamhash.streamview.util.UiUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.streamhash.streamview.util.Fragments.HOME_FRAGMENTS;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.VideoTileHolder> {

    private VideoContentFragment contentFragment;
    private LayoutInflater inflater;
    private MainActivity context;
    private ArrayList<Category> categories;
    private OnLoadMoreVideosListener listener;

    public CategoriesAdapter(MainActivity context, OnLoadMoreVideosListener listener, ArrayList<Category> categories) {
        this.context = context;
        this.listener = listener;
        this.categories = categories;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public VideoTileHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.item_category, viewGroup, false);
        return new VideoTileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoTileHolder videoTileHolder, int position) {
        Category category = categories.get(position);
        GlideApp.with(context).load(category.getThumbnailUrl())
                .into(videoTileHolder.categoryImg);
        videoTileHolder.categoryName.setText(category.getTitle());
        videoTileHolder.categoryRoot.setOnClickListener(v -> {
            CategoryFragment.categoryBeingViewed = category.getTitle();
            contentFragment = VideoContentFragment.getInstance(VideoContentFragment.TYPE_CATEGORY, category.getId(), 0, 0);
            replaceFragmentWithAnimation(contentFragment, HOME_FRAGMENTS[2]);
        });
        videoTileHolder.categoryRoot.setOnLongClickListener(v -> {
            UiUtils.showShortToast(context, category.getTitle());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    private void replaceFragmentWithAnimation(Fragment fragment, String tag) {
        FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();
        MainActivity.CURRENT_FRAGMENT = tag;
        transaction.addToBackStack(tag);
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }

    public void showLoading() {
        if(listener!=null)
            listener.onLoadMore(categories.size());
    }

    class VideoTileHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.categoryImg)
        ImageView categoryImg;
        @BindView(R.id.categoryName)
        TextView categoryName;
        @BindView(R.id.categoryRoot)
        ViewGroup categoryRoot;

        VideoTileHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
