package com.streamhash.streamview.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.streamhash.streamview.R;
import com.streamhash.streamview.listener.OnLoadMoreVideosListener;
import com.streamhash.streamview.model.SubscriptionPlan;
import com.streamhash.streamview.network.APIClient;
import com.streamhash.streamview.network.APIInterface;
import com.streamhash.streamview.ui.adapter.MyPlanAdapter;
import com.streamhash.streamview.util.NetworkUtils;
import com.streamhash.streamview.util.ParserUtils;
import com.streamhash.streamview.util.UiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.streamhash.streamview.network.APIConstants.Constants;
import static com.streamhash.streamview.network.APIConstants.Params;

public class MyPlansActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        MyPlanAdapter.DisplayAutoRenewal,
        OnLoadMoreVideosListener {

    @BindView(R.id.autoRenewal)
    Switch autoRenewal;
    @BindView(R.id.autoRenewalLayout)
    LinearLayout autoRenewalLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.myPlansRecycler)
    RecyclerView myPlansRecycler;
    MyPlanAdapter myPlanAdapter;
    ArrayList<SubscriptionPlan> myPlans = new ArrayList<>();
    APIInterface apiInterface;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.noResultLayout)
    TextView noResultLayout;
    @BindView(R.id.shimmer)
    ShimmerFrameLayout shimmerFrameLayout;

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager llmanager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (llmanager.findLastCompletelyVisibleItemPosition() == (myPlanAdapter.getItemCount() - 1)) {
                myPlanAdapter.showLoading();
            }
        }
    };


    private CompoundButton.OnCheckedChangeListener autoRenewCheckChangeListener
            = (buttonView, isChecked) -> {
        if (autoRenewal.isChecked()) {
            new AlertDialog.Builder(MyPlansActivity.this)
                    .setTitle(getString(R.string.autoRenewalMsg))
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> enableAutoRenewal())
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                        dialogInterface.cancel();
                        rollBackAutoRenewToggle();
                    })
                    .create().show();
        } else {
            new AlertDialog.Builder(MyPlansActivity.this)
                    .setTitle(getString(R.string.cancelRenewal))
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> cancelSubscription())
                    .setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
                        dialogInterface.cancel();
                        rollBackAutoRenewToggle();
                    })
                    .create().show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plans);
        ButterKnife.bind(this);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        apiInterface = APIClient.getClient().create(APIInterface.class);
        autoRenewal = findViewById(R.id.autoRenewal);
        autoRenewalLayout = findViewById(R.id.autoRenewalLayout);
        swipe.setOnRefreshListener(this);
        setUpMyPlans();

        autoRenewal.setOnCheckedChangeListener(autoRenewCheckChangeListener);

    }

    private void setUpMyPlans() {
        myPlanAdapter = new MyPlanAdapter(MyPlansActivity.this, this, myPlans, this);
        myPlansRecycler.setLayoutManager(new LinearLayoutManager(this));
        myPlansRecycler.setAdapter(myPlanAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        getAvailablePlans(0);
    }

    protected void getAvailablePlans(int skip) {
        if (skip == 0) {
            myPlansRecycler.setOnScrollListener(scrollListener);
            shimmerFrameLayout.setVisibility(View.VISIBLE);
            myPlansRecycler.setVisibility(View.GONE);
            noResultLayout.setVisibility(View.GONE);
            autoRenewalLayout.setVisibility(View.GONE);
        }

        Call<String> call = apiInterface.getMyPlans(id, token, subProfileId, skip);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (skip == 0) {
                    myPlans.clear();
                    if (swipe.isRefreshing()) swipe.setRefreshing(false);
                    shimmerFrameLayout.setVisibility(View.GONE);
                }

                JSONObject myPlansResponse = null;
                try {
                    myPlansResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (myPlansResponse != null) {
                    if (myPlansResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        JSONArray plansArray = myPlansResponse.optJSONArray(Params.DATA);
                        for (int i = 0; i < plansArray.length(); i++) {
                            JSONObject planObj = plansArray.optJSONObject(i);
                            SubscriptionPlan plan = ParserUtils.parsePlan(planObj);
                            myPlans.add(plan);
                        }
                        if (plansArray.length() == 0) {
                            myPlansRecycler.removeOnScrollListener(scrollListener);
                        }
                        onDataChanged();
                    } else {
                        UiUtils.showShortToast(getApplicationContext(), myPlansResponse.optString(Params.ERROR_MESSAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                shimmerFrameLayout.setVisibility(View.GONE);
                NetworkUtils.onApiError(MyPlansActivity.this);
            }
        });
    }

    private void onDataChanged() {
        myPlanAdapter.notifyDataSetChanged();
        noResultLayout.setVisibility(myPlans.isEmpty() ? View.VISIBLE : View.GONE);
        myPlansRecycler.setVisibility(myPlans.isEmpty() ? View.GONE : View.VISIBLE);
        if (myPlans.size() == 0
                || (myPlans.size() == 1 && myPlans.get(0).getOriginalAmount() == 0))
            autoRenewalLayout.setVisibility(View.GONE);
        else {
            autoRenewalLayout.setVisibility(View.VISIBLE);
            if (!myPlans.isEmpty())
                onAutoRenewUpdate(myPlans.get(0).isCancelled());
        }
    }

    @Override
    public void onRefresh() {
        onResume();
    }

    protected void enableAutoRenewal() {
        UiUtils.showLoadingDialog(MyPlansActivity.this);
        Call<String> call = apiInterface.autoRenewalEnable(id, token, subProfileId);

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
                        Toast.makeText(MyPlansActivity.this, paymentObject.optString(Params.MESSAGE), Toast.LENGTH_SHORT).show();
                    } else {
                        rollBackAutoRenewToggle();
                        Toast.makeText(MyPlansActivity.this, paymentObject.optString(Params.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                rollBackAutoRenewToggle();
                NetworkUtils.onApiError(MyPlansActivity.this);
            }
        });
    }

    protected void cancelSubscription() {
        UiUtils.showLoadingDialog(MyPlansActivity.this);
        Call<String> call = apiInterface.cancelSubscription(id, token, subProfileId);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                UiUtils.hideLoadingDialog();
                JSONObject cancelAutoRenewalResponse = null;
                try {
                    cancelAutoRenewalResponse = new JSONObject(response.body());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (cancelAutoRenewalResponse != null) {
                    if (cancelAutoRenewalResponse.optString(Params.SUCCESS).equals(Constants.TRUE)) {
                        Toast.makeText(MyPlansActivity.this, cancelAutoRenewalResponse.optString(Params.MESSAGE), Toast.LENGTH_SHORT).show();
                    } else {
                        rollBackAutoRenewToggle();
                        Toast.makeText(MyPlansActivity.this, cancelAutoRenewalResponse.optString(Params.ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                UiUtils.hideLoadingDialog();
                rollBackAutoRenewToggle();
                NetworkUtils.onApiError(MyPlansActivity.this);
            }
        });
    }

    private void rollBackAutoRenewToggle() {
        autoRenewal.setOnCheckedChangeListener(null);
        autoRenewal.setChecked(!autoRenewal.isChecked());
        autoRenewal.setOnCheckedChangeListener(autoRenewCheckChangeListener);
    }

    public void onAutoRenewUpdate(boolean isCancelled) {
        autoRenewal.setOnCheckedChangeListener(null);
        autoRenewal.setChecked(isCancelled);
        autoRenewal.setOnCheckedChangeListener(autoRenewCheckChangeListener);
    }

    @Override
    public void dataChanged(double orginalAmt) {
        if (orginalAmt == 0) {
            autoRenewalLayout.setVisibility(View.GONE);
        } else {
            autoRenewalLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadMore(int skip) {
        getAvailablePlans(skip);
    }
}
