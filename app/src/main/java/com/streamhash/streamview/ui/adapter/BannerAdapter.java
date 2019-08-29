package com.streamhash.streamview.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.streamhash.streamview.R;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.util.GlideApp;

import java.util.ArrayList;

public class BannerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Video> data;

    public BannerAdapter(Context activity, ArrayList<Video> data) {
        this.context = activity;
        this.data = data;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        ((ViewPager) collection).removeView((View) view);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view;
        ImageView bannerImage;
        view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_banner_video, container, false);
        try {
            bannerImage = view.findViewById(R.id.image);
            GlideApp.with(context).load(data.get(position).getThumbNailUrl()).thumbnail(0.5f)
                    .into(bannerImage);

            bannerImage.setOnClickListener(view1 -> {
                Intent i = new Intent(context, VideoPageActivity.class);
                i.putExtra("videoId", data.get(position).getAdminVideoId());
                context.startActivity(i);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        container.addView(view);
        return view;
    }
}
