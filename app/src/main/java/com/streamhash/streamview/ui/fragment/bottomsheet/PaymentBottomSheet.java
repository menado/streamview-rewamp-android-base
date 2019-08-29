package com.streamhash.streamview.ui.fragment.bottomsheet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.BottomSheetBackDismissListener;
import com.streamhash.streamview.model.Invoice;
import com.streamhash.streamview.model.SubscriptionPlan;
import com.streamhash.streamview.model.Video;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIConstants;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.activity.AddCardActivity;
import com.streamhash.streamview.ui.activity.PaymentsActivity;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.UiUtils;
import com.streamhash.streamview.util.sharedpref.PrefKeys;
import com.streamhash.streamview.util.sharedpref.PrefUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;
import static com.streamhash.streamview.network.APIConstants.Payments;
import static com.streamhash.streamview.util.DisplayUtils.makeBottomSheetFullScreen;

public class PaymentBottomSheet extends BottomSheetDialogFragment {

    public static final int PAY_PAL_REQUEST_CODE = 200;
    public static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Payments.PayPal.CLIENT_ID);
    @BindView(R.id.appBar)
    AppBarLayout appBar;
    @BindView(R.id.titleText)
    TextView titleText;
    @BindView(R.id.planName)
    TextView planName;
    @BindView(R.id.statusText)
    TextView statusText;
    @BindView(R.id.months)
    TextView months;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.amount)
    TextView amount;
    @BindView(R.id.amountLayout)
    LinearLayout amountLayout;
    @BindView(R.id.couponCode)
    TextView textCouponCode;
    @BindView(R.id.couponLayout)
    LinearLayout couponLayout;
    @BindView(R.id.couponAmt)
    TextView couponAmt;
    @BindView(R.id.couponAmtLayout)
    LinearLayout couponAmtLayout;
    @BindView(R.id.totalAmt)
    TextView totalAmt;
    @BindView(R.id.totalAmtLayout)
    LinearLayout totalAmtLayout;
    @BindView(R.id.et_couponCode)
    EditText etCouponCode;
    @BindView(R.id.layout_email)
    TextInputLayout layoutEmail;
    @BindView(R.id.applyCode)
    Button applyCode;
    @BindView(R.id.pay)
    Button pay;
    @BindView(R.id.beforeAppliedLayout)
    LinearLayout beforeAppliedLayout;
    @BindView(R.id.appliedCode)
    TextView appliedCode;
    @BindView(R.id.successMsg)
    TextView successMsg;
    @BindView(R.id.removeCode)
    Button removeCode;
    @BindView(R.id.appliedLayout)
    FrameLayout appliedLayout;
    @BindView(R.id.layoutPayment)
    LinearLayout layoutPayment;
    @BindView(R.id.paypal)
    Button payPal;
    @BindView(R.id.stripe)
    Button stripe;
    Unbinder unbinder;
    Context context;
    @BindView(R.id.gotCouponText)
    TextView gotCouponText;

    private SubscriptionPlan plan;
    private Video video;
    private Invoice invoice;
    private APIInterface apiInterface;
    private PaymentsInterface paymentsInterface;
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.layout_payment_info, null);
        unbinder = ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);
        dialog.setOnKeyListener(new BottomSheetBackDismissListener());
        makeBottomSheetFullScreen(context, mBottomSheetBehaviorCallback, contentView);
        setCancelable(false);
        toolbar.setNavigationOnClickListener(v -> dismiss());
        apiInterface = APIClient.getClient().create(APIInterface.class);

        setUpViewAndShowWhatsBeingPaidFor();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void setUpViewAndShowWhatsBeingPaidFor() {
        planName.setText(invoice.getTitle());
        amount.setText(String.format("%s%s", invoice.getCurrency(), invoice.getPaidAmount()));
        totalAmt.setText(String.format("%s%s", invoice.getCurrency(), invoice.getTotalAmount()));
        if (invoice.isPayingForPlan()) {
            months.setText(String.format("%s month(s)", invoice.getMonths()));
            applyCode.setOnClickListener(view -> applyCouponCode(etCouponCode.getText().toString(), plan.getId()));
        } else {
            months.setVisibility(View.GONE);
            statusText.setVisibility(View.GONE);
            applyCode.setOnClickListener(view -> applyPPVCouponCode(etCouponCode.getText().toString(), video.getAdminVideoId()));
        }

        gotCouponText.setVisibility(invoice.getTotalAmount() == 0 ? View.GONE : View.VISIBLE);
        couponAmtLayout.setVisibility(View.GONE);
        couponLayout.setVisibility(View.GONE);
        removeCode.setOnClickListener(view -> {
            //Invoice object changes
            invoice.setCouponCode("");
            invoice.setCouponApplied(false);
            invoice.setPaidAmount(invoice.getTotalAmount());

            //UI changes
            onCouponStuffChanged(invoice.isCouponApplied());
        });

        doPaymentButtonSetups();
    }


    @OnClick({R.id.paypal, R.id.stripe, R.id.pay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.paypal:
                getPayPalPayment();
                break;
            case R.id.stripe:
                new AlertDialog.Builder(context)
                        .setTitle("Are you sure you want to pay " + invoice.getCurrency() + invoice.getPaidAmount() + " ?")
                        .setPositiveButton(context.getString(R.string.yes), (dialogInterface, as) ->
                                sendStripePaymentToBackend(invoice.isPayingForPlan(),
                                        invoice.isPayingForPlan() ? plan.getId() : video.getAdminVideoId()))
                        .setNegativeButton(context.getString(R.string.no), (dialogInterface, as) -> dialogInterface.cancel()).create().show();
                break;
            case R.id.pay:
                sendPayPalPaymentToBackend(
                        invoice.isPayingForPlan() ? plan.getId() : video.getAdminVideoId(),
                        invoice.isCouponApplied() ? "coupon-discount" : "free-plan");
                break;
        }
    }

    private void getPayPalPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(String.valueOf(invoice.getPaidAmount())), invoice.getCurrencySymbol(), invoice.getTitle(),
                PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);
        if (paymentsInterface != null) {
            dismiss();
            paymentsInterface.onMakePayPalPayment(invoice);
        }
    }

    protected void applyCouponCode(String couponCode, int subId) {
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        UiUtils.showLoadingDialog(context);
        Call<String> call = apiInterface.applyCouponCode(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1)
                , subId
                , couponCode);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject couponResponse = null;
                try {
                    couponResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (couponResponse != null) {
                    if (couponResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        JSONObject couponObj = couponResponse.optJSONObject(Params.DATA);
                        //Invoice changes
                        invoice.setCouponCode(couponObj.optString(Params.COUPON_CODE));
                        invoice.setCouponApplied(true);
                        invoice.setPaidAmount(couponObj.optDouble(Params.REMAINING_AMT));
                        invoice.setCouponAmount(couponObj.optDouble(Params.COUPON_AMT));

                        onCouponStuffChanged(invoice.isCouponApplied());
                    } else {
                        UiUtils.hideLoadingDialog();
                        Toast.makeText(context, couponResponse.optString(Params.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(context);
            }
        });
    }

    private void onCouponStuffChanged(boolean couponApplied) {
        //Visibility
        appliedLayout.setVisibility(couponApplied ? View.VISIBLE : View.GONE);
        beforeAppliedLayout.setVisibility(couponApplied ? View.GONE : View.VISIBLE);
        gotCouponText.setVisibility(couponApplied ? View.GONE : View.VISIBLE);
        couponLayout.setVisibility(couponApplied ? View.VISIBLE : View.GONE);
        couponAmtLayout.setVisibility(couponApplied ? View.VISIBLE : View.GONE);
        doPaymentButtonSetups();

        //Display
        couponAmt.setText(String.format("%s%s", invoice.getCurrency(), invoice.getCouponAmount()));
        successMsg.setText(couponApplied ? getString(R.string.coupon_apply_successful) : null);
        textCouponCode.setText(invoice.getCouponCode());
        appliedCode.setText(invoice.getCouponCode());
        etCouponCode.setText(invoice.getCouponCode());
        amount.setText(String.format("%s%s", invoice.getCurrency(), invoice.getPaidAmount()));
        totalAmt.setText(String.format("%s%s", invoice.getCurrency(), invoice.getTotalAmount()));
    }

    private void doPaymentButtonSetups() {
        pay.setVisibility(invoice.getPaidAmount() == 0 ? View.VISIBLE : View.GONE);
        beforeAppliedLayout.setVisibility(invoice.getPaidAmount() == 0 ? View.GONE : View.VISIBLE);
        stripe.setVisibility(invoice.getPaidAmount() == 0 ? View.GONE : View.VISIBLE);
        payPal.setVisibility(invoice.getPaidAmount() == 0 ? View.GONE : View.VISIBLE);
    }


    //    ApplyCouponCode for PPv
    protected void applyPPVCouponCode(String couponCode, int adminVideoId) {
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        UiUtils.showLoadingDialog(context);
        Call<String> call = apiInterface.applyPPVCode(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1)
                , adminVideoId
                , couponCode);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject couponObject = null;
                try {
                    couponObject = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (couponObject != null) {
                    if (couponObject.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        JSONObject couponObj = couponObject.optJSONObject(Params.DATA);

                        //Invoice changes
                        invoice.setCouponCode(couponObj.optString(Params.COUPON_CODE));
                        invoice.setCouponApplied(true);
                        invoice.setPaidAmount(couponObj.optDouble(Params.REMAINING_AMT));
                        invoice.setCouponAmount(couponObj.optDouble(Params.COUPON_AMT));

                        onCouponStuffChanged(invoice.isCouponApplied());
                    } else {
                        UiUtils.hideLoadingDialog();
                        Toast.makeText(context, couponObject.optString(Params.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(context);
            }
        });
    }

    private void sendStripePaymentToBackend(boolean isPayingForPlan, int id) {
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        UiUtils.showLoadingDialog(context);

        Call<String> call;
        if (!isPayingForPlan)
            call = apiInterface.makeStripePPV(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                    , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                    , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1)
                    , id
                    , invoice.getCouponCode());
        else
            call = apiInterface.makeStripePayment(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                    , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                    , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1)
                    , id
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
                        UiUtils.showShortToast(context, paymentObject.optString(Params.MESSAGE) +
                                (!invoice.isPayingForPlan() ? "\nIf video doesn't play automatically, Please reopen the video page and play manually" : ""));
                        JSONObject payObject = paymentObject.optJSONObject(Params.DATA);
                        String paymentId = payObject.optString(Params.PAYMENT_ID);
                        invoice.setPaymentId(paymentId);
                        dismiss();
                        paymentsInterface.onPaymentSucceeded(invoice);
                    } else {
                        UiUtils.hideLoadingDialog();
                        UiUtils.showShortToast(context, paymentObject.optString(Params.ERROR_MESSAGE));
                        switch (paymentObject.optInt(Params.ERROR_CODE)) {
                            case APIConstants.ErrorCodes.NEED_TO_SUBSCRIBE_TO_MAKE_PAYMENT:
                                Intent i = new Intent(context, PaymentsActivity.class);
                                context.startActivity(i);
                                break;
                            case APIConstants.ErrorCodes.NO_DEFAULT_CARD_FOUND:
                            default:
                                Intent toAddCard = new Intent(getActivity(), AddCardActivity.class);
                                startActivity(toAddCard);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                NetworkUtils.onApiError(context);
            }
        });
    }

    private void sendPayPalPaymentToBackend(int id, String paymentId) {
        PrefUtils prefUtils = PrefUtils.getInstance(context);

        UiUtils.showLoadingDialog(context);
        Call<String> call;
        if (invoice.isPayingForPlan())
            call = apiInterface.makePayPalPlanPayment(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                    , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                    , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1)
                    , id
                    , paymentId
                    , invoice.getCouponCode());
        else
            call = apiInterface.makePayPalPPV(prefUtils.getIntValue(PrefKeys.USER_ID, -1)
                    , prefUtils.getStringValue(PrefKeys.SESSION_TOKEN, "")
                    , prefUtils.getIntValue(PrefKeys.ACTIVE_SUB_PROFILE, -1)
                    , id
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
                        Toast.makeText(context, paymentObject.optString("message"), Toast.LENGTH_SHORT).show();
                        invoice.setPaymentId(paymentId);
                        dismiss();
                        paymentsInterface.onPaymentSucceeded(invoice);
                    } else {
                        UiUtils.hideLoadingDialog();
                        Toast.makeText(context, paymentObject.optString("error_messages"), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                NetworkUtils.onApiError(context);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public boolean setPaymentsInterface(PaymentsInterface paymentsInterface, SubscriptionPlan plan, Video video) {
        if (plan == null && video == null)
            return true;

        this.paymentsInterface = paymentsInterface;
        this.invoice = new Invoice();
        this.invoice.setPlan(plan);
        this.invoice.setVideo(video);

        this.plan = invoice.getPlan();
        this.video = invoice.getVideo();

        invoice.setPayingForPlan(plan != null);

        if (invoice.isPayingForPlan()) {
            if (plan != null) {
                invoice.setTitle(plan.getTitle());
                invoice.setTotalAmount(plan.getAmount());
                invoice.setPaidAmount(plan.getAmount());
                invoice.setCouponAmount(0.0);
                invoice.setMonths(plan.getMonths());
                invoice.setCouponApplied(false);
                invoice.setCouponCode("");
                invoice.setCurrency(plan.getCurrency());
                invoice.setCurrencySymbol("USD");
            } else {
                return true;
            }
        } else {
            if (video != null) {
                invoice.setTitle(video.getTitle());
                invoice.setTotalAmount(video.getAmount());
                invoice.setPaidAmount(video.getAmount());
                invoice.setCouponAmount(0.0);
                invoice.setCouponApplied(false);
                invoice.setCouponCode("");
                invoice.setCurrency(video.getCurrency());
                invoice.setCurrencySymbol("USD");
            } else {
                return true;
            }
        }
        return false;
    }

    public interface PaymentsInterface {
        void onPaymentSucceeded(Invoice invoice);

        void onPaymentFailed(String failureReason);

        void onMakePayPalPayment(Invoice invoice);
    }
}
