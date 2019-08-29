package com.streamhash.streamview.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.Invoice;
import com.streamhash.streamview.model.SubscriptionPlan;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.adapter.PlanAdapter;
import com.streamhash.streamview.ui.fragment.bottomsheet.InvoiceBottomSheet;
import com.streamhash.streamview.ui.fragment.bottomsheet.PaymentBottomSheet;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.ParserUtils;
import com.streamhash.streamview.util.UiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.streamhash.streamview.network.APIConstants.*;
import static com.streamhash.streamview.ui.fragment.bottomsheet.PaymentBottomSheet.PAY_PAL_REQUEST_CODE;

public class PlansActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        PaymentBottomSheet.PaymentsInterface,
        OnLoadMoreVideosListener {

    PlanAdapter planAdapter;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.plansList)
    RecyclerView plansRecycler;
    ArrayList<SubscriptionPlan> plans = new ArrayList<>();
    APIInterface apiInterface;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmerFrameLayout;
    PaymentBottomSheet.PaymentsInterface paymentsInterface;

    InvoiceBottomSheet invoiceSheet;
    private Invoice invoice;

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (llmanager.findLastCompletelyVisibleItemPosition() == (planAdapter.getItemCount() - 1)) {
                planAdapter.showLoading();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans2);
        ButterKnife.bind(this);
        swipe.setOnRefreshListener(this);
        paymentsInterface = this;
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);
        setUpAvailablePlans();
    }

    private void setUpAvailablePlans() {
        plansRecycler.setLayoutManager(new LinearLayoutManager(this));
        planAdapter = new PlanAdapter(this, this, plans);
        plansRecycler.setAdapter(planAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        getAvailablePlans(0);
    }

    protected void getAvailablePlans(int skip) {
        if (skip == 0) {
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            noResultLayout.setVisibility(View.GONE);
            plansRecycler.setVisibility(View.GONE);
            plansRecycler.addOnScrollListener(scrollListener);
        }

        Call<String> call = apiInterface.getAvaliablePlans(id, token, subProfileId, skip);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                shimmerFrameLayout.setVisibility(View.GONE);
                if (skip == 0) {
                    plans.clear();
                    if (swipe.isRefreshing()) swipe.setRefreshing(false);
                    shimmerFrameLayout.setVisibility(View.GONE);
                }
                if (swipe.isRefreshing()) swipe.setRefreshing(false);

                JSONObject availablePlansObj = null;
                try {
                    availablePlansObj = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (availablePlansObj != null) {
                    if (availablePlansObj.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        JSONArray plansArray = availablePlansObj.optJSONArray(Params.DATA);
                        for (int i = 0; i < plansArray.length(); i++) {
                            JSONObject planObj = plansArray.optJSONObject(i);
                            SubscriptionPlan plan = ParserUtils.parsePlan(planObj);
                            plans.add(plan);
                        }
                        if (plansArray.length() == 0) {
                            plansRecycler.removeOnScrollListener(scrollListener);
                        }
                        onDataChanged();
                    } else {
                        UiUtils.showShortToast(getApplicationContext(), availablePlansObj.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                shimmerFrameLayout.setVisibility(View.GONE);
                noResultLayout.setVisibility(View.VISIBLE);
                NetworkUtils.onApiError(PlansActivity.this);
            }
        });
    }

    private void onDataChanged() {
        planAdapter.notifyDataSetChanged();
        noResultLayout.setVisibility(plans.isEmpty() ? View.VISIBLE : View.GONE);
        plansRecycler.setVisibility(plans.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String paymentId;
        if (requestCode == PAY_PAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        Timber.d(paymentDetails);
                        JSONObject details = new JSONObject(paymentDetails);
                        paymentId = details.optJSONObject("response").optString("id");
                        sendPayPalPaymentToBackend(paymentId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                onPaymentFailed(getString(R.string.something_went_wrong));
            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            onPaymentFailed(getString(R.string.invalid_payment));
        }
    }

    private void sendPayPalPaymentToBackend(String paymentId) {
        UiUtils.showLoadingDialog(this);
        Call<String> call = apiInterface.makePayPalPlanPayment(id, token, subProfileId
                , invoice.getPlan().getId()
                , paymentId
                , invoice.getCouponCode());

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject paymentObject = null;
                try {
                    paymentObject = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (paymentObject != null) {
                    if (paymentObject.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        Toast.makeText(PlansActivity.this, paymentObject.optString("message"), Toast.LENGTH_SHORT).show();
                        invoice.setPaymentId(paymentId);
                        onPaymentSucceeded(invoice);
                    } else {
                        UiUtils.hideLoadingDialog();
                        Toast.makeText(PlansActivity.this, paymentObject.optString("error_messages"), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(PlansActivity.this);
            }
        });
    }

    @Override
    public void onPaymentSucceeded(Invoice invoice) {
        invoice.setStatus(getString(R.string.success));
        invoiceSheet = InvoiceBottomSheet.getInstance(invoice);
        invoiceSheet.show(getSupportFragmentManager(), invoiceSheet.getTag());
    }

    @Override
    public void onPaymentFailed(String failureReason) {
    }

    @Override
    public void onMakePayPalPayment(Invoice invoice) {
        this.invoice = invoice;
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(invoice.getPaidAmount())), invoice.getCurrencySymbol(), invoice.getTitle(),
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PaymentBottomSheet.config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAY_PAL_REQUEST_CODE);
    }

    @Override
    public void onLoadMore(int skip) {
        getAvailablePlans(skip);
    }
}
