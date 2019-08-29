package com.streamhash.streamview.ui.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.ui.activity.VideoPageActivity;
import com.streamhash.streamview.util.GlideApp;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaidVideosAdapter extends RecyclerView.Adapter<PaidVideosAdapter.ViewHolder> {
    Context context;
    private ArrayList<Video> paidVideosList;
    private LayoutInflater inflater;
    private OnLoadMoreVideosListener listener;


    public PaidVideosAdapter(Context applicationContext, OnLoadMoreVideosListener listener, ArrayList<Video> paidVideosList) {
        this.context = applicationContext;
        this.listener = listener;
        this.paidVideosList = paidVideosList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.item_paid_video_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Video paidVideos = paidVideosList.get(i);
        GlideApp.with(context).load(paidVideos.getThumbNailUrl())
                .into(viewHolder.wishListImage);
        viewHolder.title.setText(paidVideos.getTitle());
        viewHolder.desc.setText(paidVideos.getTitle());
        viewHolder.exapandBtn.setOnClickListener(view -> {
            if (viewHolder.expandableView.getVisibility() == View.VISIBLE) {
                collapseView(viewHolder.expandableView);
                viewHolder.expandImg.setBackgroundResource(R.drawable.right_icon);
            } else {
                expandView(viewHolder.expandableView);
                viewHolder.expandImg.setBackgroundResource(R.drawable.up_icon);
            }
        });

        viewHolder.paidLayout.setOnClickListener(view -> singlePageView(paidVideosList.get(i)));
    }

    private void collapseView(final View expandableView) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int newHeight = (int) (100 * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        ValueAnimator slideAnimator = ValueAnimator.ofInt(newHeight, 0);
        slideAnimator.setInterpolator(new DecelerateInterpolator());
        slideAnimator.setDuration(300);
        slideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                expandableView.setVisibility(View.GONE);
            }
        });
        slideAnimator.addUpdateListener(animation -> {
            expandableView.getLayoutParams().height = (int) animation.getAnimatedValue();
            expandableView.requestLayout();
        });
        slideAnimator.start();
    }

    private void expandView(final View expandableView) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int newHeight = (int) (140 * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        ValueAnimator slideAnimator = ValueAnimator.ofInt(0, newHeight);
        slideAnimator.setInterpolator(new DecelerateInterpolator());
        slideAnimator.setDuration(300);
        expandableView.setVisibility(View.VISIBLE);
        slideAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        slideAnimator.addUpdateListener(animation -> {
            expandableView.requestLayout();
            expandableView.getLayoutParams().height = (int) animation.getAnimatedValue();
        });
        slideAnimator.start();
    }


    @Override
    public int getItemCount() {
        return paidVideosList.size();
    }

    private void singlePageView(Video paidVideo) {
        Dialog dialog = new Dialog(context, R.style.AppTheme_NoActionBar);
        dialog.setContentView(R.layout.layout_paid_video_invoice);
        Toolbar toolbar = dialog.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            dialog.cancel();
        });
        ImageView wishListImage = dialog.findViewById(R.id.wishListImage);
        TextView title = dialog.findViewById(R.id.title);
        TextView desc = dialog.findViewById(R.id.desc);
        TextView paidDate = dialog.findViewById(R.id.paidDate);
        TextView paymentMode = dialog.findViewById(R.id.paymentMode);
        TextView couponCode = dialog.findViewById(R.id.couponCode);
        TextView couponCodeText = dialog.findViewById(R.id.couponCodeText);
        TextView couponAmt = dialog.findViewById(R.id.couponAmt);
        TextView couponAmtText = dialog.findViewById(R.id.couponAmtText);
        TextView typeOfSub = dialog.findViewById(R.id.typeOfSub);
        TextView paymentId = dialog.findViewById(R.id.paymentId);
        TextView amount = dialog.findViewById(R.id.amount);
        FrameLayout image_layout = dialog.findViewById(R.id.image_layout);
        title.setText(paidVideo.getTitle());
        desc.setText(paidVideo.getTitle());
        paidDate.setText(paidVideo.getPaidDate());
        couponCode.setText(paidVideo.getCouponCode());
        paymentMode.setText(paidVideo.getPaymentMode());
        couponAmt.setText(String.valueOf(paidVideo.getCouponAmount()));
        typeOfSub.setText(paidVideo.getTypeOfSubscription());
        paymentId.setText(paidVideo.getPaymentId());
        amount.setText(String.format("%s %s", paidVideo.getCurrency(), paidVideo.getAmount()));
        GlideApp.with(context).load(paidVideo.getThumbNailUrl())
                .into(wishListImage);
        image_layout.setOnClickListener(view -> {
            Intent i = new Intent(context, VideoPageActivity.class);
            i.putExtra(VideoPageActivity.VIDEO_ID, paidVideo.getAdminVideoId());
            context.startActivity(i);
        });
        if (paidVideo.getCouponCode().equals("")) {
            couponAmt.setVisibility(View.GONE);
            couponCode.setVisibility(View.GONE);
            couponCodeText.setVisibility(View.GONE);
            couponAmtText.setVisibility(View.GONE);
        }
        dialog.show();
    }

    public void showLoading() {
        if (listener != null)
            listener.onLoadMore(paidVideosList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.wishListImage)
        ImageView wishListImage;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.desc)
        TextView desc;
        @BindView(R.id.videosAmount)
        TextView videosAmount;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.couponAmount)
        TextView couponAmount;
        @BindView(R.id.CouponCode)
        TextView CouponCode;
        @BindView(R.id.totalAmount)
        TextView totalAmount;
        @BindView(R.id.userType)
        TextView userType;
        @BindView(R.id.PPVType)
        TextView PPVType;
        @BindView(R.id.expandImg)
        ImageView expandImg;
        @BindView(R.id.exapandBtn)
        LinearLayout exapandBtn;
        @BindView(R.id.expandableView)
        RelativeLayout expandableView;
        @BindView(R.id.paidLayout)
        LinearLayout paidLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
