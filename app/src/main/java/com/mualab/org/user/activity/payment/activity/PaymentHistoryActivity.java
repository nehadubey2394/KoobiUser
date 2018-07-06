package com.mualab.org.user.activity.payment.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.payment.adapter.PaymentHistoryAdapter;
import com.mualab.org.user.activity.payment.modle.PaymentHistory;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import views.refreshview.CircleHeaderView;
import views.refreshview.OnRefreshListener;
import views.refreshview.RjRefreshLayout;

public class PaymentHistoryActivity extends AppCompatActivity implements View.OnClickListener{
    private PaymentHistoryAdapter paymentHistoryAdapter;
    private List<PaymentHistory> paymentHistories;
    private TextView tvDiscount,tvHistory,tvRefrell,tvNoData,tvHeaderTitle;
    private LinearLayout tabDiscount, tabRefrell,tabHistory,ll_progress;
    private long mLastClickTime = 0;
    private RecyclerView rycHistory;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int clickId = 1;
    private RjRefreshLayout mRefreshLayout;
    private boolean isPulltoRefrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);
        init();
        setViewId();
    }

    private void init(){
        paymentHistories = new ArrayList<>();
        paymentHistoryAdapter = new PaymentHistoryAdapter(PaymentHistoryActivity.this,paymentHistories);
    }

    private void setViewId(){
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile.setVisibility(View.GONE);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.payment_history));
        tvNoData = findViewById(R.id.tvNoData);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvRefrell = findViewById(R.id.tvRefrell);
        tvHistory = findViewById(R.id.tvHistory);

        tabDiscount = findViewById(R.id.tabDiscount);
        tabRefrell = findViewById(R.id.tabRefrell);
        tabHistory = findViewById(R.id.tabHistory);
        ll_progress = findViewById(R.id.ll_progress);

        rycHistory = findViewById(R.id.rycHistory);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(PaymentHistoryActivity.this, LinearLayoutManager.VERTICAL, false);
        layoutManager1.scrollToPositionWithOffset(0, 0);
        rycHistory.setLayoutManager(layoutManager1);
        rycHistory.setAdapter(paymentHistoryAdapter);

        mRefreshLayout =  findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(PaymentHistoryActivity.this);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                scrollListener.resetState();
                isPulltoRefrash = true;
                apiForPaymentHistory(0,false);
            }

            @Override
            public void onLoadMore() {
            }
        });

        if(scrollListener==null) {
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager1) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                  //  if (totalItemsCount>19) {
                        paymentHistoryAdapter.showLoading(true);
                        apiForPaymentHistory(page, false);
                    //}
                }
            };
        }

        rycHistory.addOnScrollListener(scrollListener);

        if(paymentHistories.size()==0) {
            apiForPaymentHistory(0,true);
        }

        btnBack.setOnClickListener(this);
        tabHistory.setOnClickListener(this);
        tabRefrell.setOnClickListener(this);
        tabDiscount.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 900) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()) {
            case R.id.tabHistory:
                if (clickId != 1) {
                    clickId = 1;
                    tvNoData.setVisibility(View.GONE);
                    tvHeaderTitle.setText(getString(R.string.payment_history));
                    tabHistory.setBackgroundResource(R.drawable.bg_tab_selected);
                    tabDiscount.setBackgroundResource(R.drawable.bg_tab_unselected);
                    tabRefrell.setBackgroundResource(R.drawable.bg_tab_midile_unselected);
                    tvHistory.setTextColor(getResources().getColor(R.color.white));
                    tvRefrell.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvDiscount.setTextColor(getResources().getColor(R.color.colorPrimary));
                    apiForPaymentHistory(0, true);
                }
                break;

            case R.id.tabRefrell:
                if (clickId != 2) {
                    clickId = 2;
                    tvNoData.setVisibility(View.GONE);
                    tvHeaderTitle.setText(getString(R.string.refrell_earnings));
                    tabRefrell.setBackgroundResource(R.drawable.bg_tab_middle_selected);
                    tabHistory.setBackgroundResource(R.drawable.bg_second_tab_unselected);
                    tabDiscount.setBackgroundResource(R.drawable.bg_tab_unselected);
                    tvRefrell.setTextColor(getResources().getColor(R.color.white));
                    tvHistory.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvDiscount.setTextColor(getResources().getColor(R.color.colorPrimary));
                    // apiForPaymentHistory(0, true);
                    rycHistory.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                    tvNoData.setText("Under developemnet");
                }
                break;

            case R.id.tabDiscount:
                if (clickId != 3) {
                    clickId = 3;
                    tvNoData.setVisibility(View.GONE);
                    tvHeaderTitle.setText(getString(R.string.discount_code));
                    tabHistory.setBackgroundResource(R.drawable.bg_second_tab_unselected);
                    tabDiscount.setBackgroundResource(R.drawable.bg_second_tab_selected);
                    tabRefrell.setBackgroundResource(R.drawable.bg_tab_midile_unselected);
                    tvDiscount.setTextColor(getResources().getColor(R.color.white));
                    tvHistory.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvRefrell.setTextColor(getResources().getColor(R.color.colorPrimary));
                    // apiForPaymentHistory(0, true);
                    rycHistory.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                    tvNoData.setText("Under developemnet");
                }
                break;

            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }

    private void apiForPaymentHistory(final int page,final boolean isEnableProgress){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(PaymentHistoryActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForPaymentHistory(page,isEnableProgress);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("userType", "user");
        params.put("page", String.valueOf(page));
        params.put("limit", "10");

        HttpTask task = new HttpTask(new HttpTask.Builder(PaymentHistoryActivity.this, "paymentList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    String total = js.getString("total");

                    ll_progress.setVisibility(View.GONE);

                    if (status.equalsIgnoreCase("success")) {
                        rycHistory.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                        paymentHistoryAdapter.showLoading(false);

                        if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);
                            int prevSize = paymentHistories.size();
                            paymentHistories.clear();
                            paymentHistoryAdapter.notifyItemRangeRemoved(0, prevSize);
                        }

                        if (page == 0) {
                            paymentHistories.clear();
                        }
                        parsingOfBooking(js,page);

                    }else {
                        rycHistory.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                        tvNoData.setText(message);
                        if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                        paymentHistoryAdapter.notifyDataSetChanged();
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    ll_progress.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                    tvNoData.setText("Something went wrong!");
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    ll_progress.setVisibility(View.GONE);
                    if(isPulltoRefrash){
                        isPulltoRefrash = false;
                        mRefreshLayout.stopRefresh(false, 500);
                        int prevSize = paymentHistories.size();
                        paymentHistories.clear();
                        paymentHistoryAdapter.notifyItemRangeRemoved(0, prevSize);
                    }
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
        ll_progress.setVisibility(isEnableProgress?View.VISIBLE:View.GONE);
    }

    private void parsingOfBooking(JSONObject js,final int page) {
        try {
            JSONArray jsonArray = js.getJSONArray("paymentList");

            if (jsonArray != null && jsonArray.length() != 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Gson gson = new Gson();
                    JSONObject object = jsonArray.getJSONObject(i);
                    PaymentHistory item = gson.fromJson(String.valueOf(object), PaymentHistory.class);
                    item._id = Integer.parseInt(object.getString("_id"));
                    paymentHistories.add(item);
                }

            } else if (page==0 && (jsonArray != null ? jsonArray.length() : 0) ==0){
                rycHistory.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
            }
            paymentHistoryAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 20 && resultCode != 0) {
            if (data != null) {
                apiForPaymentHistory(0,true);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        KeyboardUtil.hideKeyboard(Objects.requireNonNull(this.getCurrentFocus()),this);
        finish();
    }
}
