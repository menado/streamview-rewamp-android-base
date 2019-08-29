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
import com.streamhash.streamview.ui.activity.PlansActivity;
import com.streamhash.streamview.ui.fragment.bottomsheet.PaymentBottomSheet;
import com.streamhash.streamview.util.UiUtils;

import java.text.MessageFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {

    public static int position;

    private PlansActivity context;
    private ArrayList<SubscriptionPlan> planList;
    private LayoutInflater inflater;
    private OnLoadMoreVideosListener listener;

    public PlanAdapter(PlansActivity activity, OnLoadMoreVideosListener listener, ArrayList<SubscriptionPlan> subList) {
        this.context = activity;
        this.listener = listener;
        this.planList = subList;
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
        viewHolder.title.setText(planList.get(i).getTitle());
        viewHolder.noOfAcc.setText(MessageFormat.format("No of accounts: {0}", planList.get(i).getNoOfAccounts()));
        viewHolder.amount.setText(planList.get(i).getAmountWithCurrency());
        viewHolder.date.setVisibility(View.GONE);
        viewHolder.viewDetails.setOnClickListener(view -> viewFullSubscription(planList.get(i)));
        viewHolder.backAmt.setText(planList.get(i).getAmountWithCurrency());
        viewHolder.perMonth.setText(MessageFormat.format("/{0}", planList.get(i).getMonths()));
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    //Dialog to View Full description
    private void viewFullSubscription(SubscriptionPlan plan) {
        Dialog dialog = new Dialog(context, R.style.AppTheme_NoActionBar);
        dialog.setContentView(R.layout.view_plan_dialog);

        Toolbar toolbar = dialog.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> dialog.cancel());

        TextView noOfAcc = dialog.findViewById(R.id.noOfAcc);
        TextView planName = dialog.findViewById(R.id.planName);
        TextView amount = dialog.findViewById(R.id.amount);
        TextView desc = dialog.findViewById(R.id.desc);
        TextView select = dialog.findViewById(R.id.select);

        planName.setText(plan.getTitle());
        noOfAcc.setText(String.format("Number of Sub profiles: %s", plan.getNoOfAccounts()));
        amount.setText(plan.getAmountWithCurrency());
        desc.setText(Html.fromHtml(plan.getDescription()));
        select.setText(plan.getAmount() == 0 ? context.getString(R.string.pay) : context.getString(R.string.select));

        select.setOnClickListener(view -> {
            dialog.dismiss();
            PaymentBottomSheet paymentBottomSheet = new PaymentBottomSheet();
            if (paymentBottomSheet.setPaymentsInterface(context, plan, null)) {
                UiUtils.showShortToast(context, context.getString(R.string.something_went_wrong));
            } else {
                paymentBottomSheet.show(context.getSupportFragmentManager(), paymentBottomSheet.getTag());
            }
        });

        dialog.show();
    }

    public void showLoading() {
        if(listener!=null)
            listener.onLoadMore(planList.size());
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
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.backAmt)
        TextView backAmt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
