package com.streamhash.streamview.ui.adapter;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.SubscriptionPlan;
import com.streamhash.streamview.ui.activity.MyPlansActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MyPlanAdapter extends RecyclerView.Adapter<MyPlanAdapter.ViewHolder> {

    private Context context;
    private ArrayList<SubscriptionPlan> myPlans;
    private LayoutInflater inflater;
    private DisplayAutoRenewal displayAutoRenewal;
    private OnLoadMoreVideosListener listener;


    public MyPlanAdapter(MyPlansActivity myPlansActivity, OnLoadMoreVideosListener listener, ArrayList<SubscriptionPlan> myPlansList, DisplayAutoRenewal displayAutoRenewal) {
        this.context = myPlansActivity;
        this.listener = listener;
        this.myPlans = myPlansList;
        this.displayAutoRenewal = displayAutoRenewal;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.item_plan, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        SubscriptionPlan plans = myPlans.get(i);

        viewHolder.title.setText(plans.getTitle());
        viewHolder.perMonth.setText(String.format("/%d", plans.getMonths()));

        viewHolder.viewDetails.setOnClickListener(view -> viewFullSubscription(plans, i));
        viewHolder.noOfAcc.setText(String.format("%s%s",
                context.getString(R.string.no_of_accounts), plans.getNoOfAccounts()));
        viewHolder.amount.setText(plans.getAmountWithCurrency());
        viewHolder.date.setText(plans.getExpires());
        viewHolder.backAmt.setText(plans.getAmountWithCurrency());

        if (plans.getActivePlan() && plans.getOriginalAmount() == 0) {
            displayAutoRenewal.dataChanged(plans.getOriginalAmount());
        }
    }

    @Override
    public int getItemCount() {
        return myPlans.size();
    }

    //My Plans full details
    private void viewFullSubscription(SubscriptionPlan plans, int i) {
        Dialog dialog = new Dialog(context, R.style.AppTheme_NoActionBar);
        dialog.setContentView(R.layout.layout_payment_select);
        Toolbar toolbar = dialog.findViewById(R.id.toolbar);
        TextView planName = dialog.findViewById(R.id.planName);
        TextView amount = dialog.findViewById(R.id.amount);
        TextView desc = dialog.findViewById(R.id.desc);
        TextView select = dialog.findViewById(R.id.select);
        TextView noOfAcc = dialog.findViewById(R.id.noOfAcc);
        TextView expiry = dialog.findViewById(R.id.expiry);
        TextView totalAmt = dialog.findViewById(R.id.totalAmt);
        TextView paymentId = dialog.findViewById(R.id.paymentId);
        TextView paymentMode = dialog.findViewById(R.id.paymentMode);
        TextView couponAmt = dialog.findViewById(R.id.couponAmt);
        TextView perMonth = dialog.findViewById(R.id.perMonth);
        TextView couponCodeText = dialog.findViewById(R.id.couponCodeText);
        TextView couponAmtText = dialog.findViewById(R.id.couponAmtText);
        TextView couponCode = dialog.findViewById(R.id.couponCode);
        toolbar.setNavigationOnClickListener(v -> dialog.cancel());
        desc.setText(Html.fromHtml(plans.getDescription()));
        select.setVisibility(View.GONE);
        planName.setText(plans.getTitle());
        amount.setText(plans.getAmountWithCurrency());
        perMonth.setText("/" + plans.getMonths());
        expiry.setText(plans.getExpires());
        paymentMode.setText(plans.getPaymentMode());
        totalAmt.setText("$ " + plans.getTotalAmt());
        couponAmt.setText("$ " + plans.getCouponAmt());
        paymentId.setText(plans.getPaymentId());
        couponCode.setText(plans.getCouponCode());
        if (couponCode.getText().toString().equalsIgnoreCase("")) {
            couponAmt.setVisibility(View.GONE);
            couponCode.setVisibility(View.GONE);
            couponAmtText.setVisibility(View.GONE);
            couponCodeText.setVisibility(View.GONE);
        }
        dialog.show();
    }

    public void showLoading() {
        if (listener != null)
            listener.onLoadMore(myPlans.size());
    }

    public interface DisplayAutoRenewal {
        void dataChanged(double orginalAmt);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.noOfAcc)
        TextView noOfAcc;
        @BindView(R.id.amount)
        TextView amount;
        @BindView(R.id.perMonth)
        TextView perMonth;
        @BindView(R.id.viewDetails)
        TextView viewDetails;
        @BindView(R.id.backAmt)
        TextView backAmt;
        @BindView(R.id.date)
        TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
