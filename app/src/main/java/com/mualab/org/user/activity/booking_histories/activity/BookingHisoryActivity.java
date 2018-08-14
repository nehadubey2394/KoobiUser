package com.mualab.org.user.activity.booking_histories.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.adapter.FutureBookingAdapter;
import com.mualab.org.user.activity.booking_histories.adapter.PastBookingAdapter;
import com.mualab.org.user.activity.booking_histories.model.BookingHistory;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
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

import views.refreshview.OnRefreshListener;
import views.refreshview.RjRefreshLayout;

public class BookingHisoryActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout tabPast, tabFuture,ll_progress;
    private TextView tvHeaderTitle,tvPast,tvFuture,tvNoData;
    private long mLastClickTime = 0;
    private RecyclerView rycPastBooking,rycFutureBooking;
    private EndlessRecyclerViewScrollListener scrollListener1,scrollListener2;
    private FutureBookingAdapter futureBookingAdapter;
    private PastBookingAdapter pastBookingAdapter;
    private List<BookingHistory> futureBookings;
    private List<BookingHistory> pastBookings;
    private String sBookingTYpe = "past";
    private int clickId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_hisory);
        if(getCurrentFocus()!=null) {
            KeyboardUtil.hideKeyboard(getCurrentFocus(),BookingHisoryActivity.this);
        }
        init();
        setViewId();
    }

    private void init(){
        futureBookings = new ArrayList<>();
        pastBookings = new ArrayList<>();
        futureBookingAdapter = new FutureBookingAdapter(BookingHisoryActivity.this,futureBookings);
        pastBookingAdapter = new PastBookingAdapter(BookingHisoryActivity.this,pastBookings);
    }

    private void setViewId(){
        ImageView btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.past_booking));
        tvNoData = findViewById(R.id.tvNoData);
        ImageView ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile.setVisibility(View.GONE);

        tvFuture = findViewById(R.id.tvFuture);
        tvPast = findViewById(R.id.tvPast);
        tabPast = findViewById(R.id.tabPast);
        tabFuture = findViewById(R.id.tabFuture);
        ll_progress = findViewById(R.id.ll_progress);

        rycPastBooking = findViewById(R.id.rycPastBooking);
        rycFutureBooking = findViewById(R.id.rycFutureBooking);

        LinearLayoutManager layoutManager1 = new LinearLayoutManager(BookingHisoryActivity.this, LinearLayoutManager.VERTICAL, false);
        layoutManager1.scrollToPositionWithOffset(0, 0);
        rycFutureBooking.setLayoutManager(layoutManager1);
        rycFutureBooking.setAdapter(futureBookingAdapter);

        LinearLayoutManager layoutManager2 = new LinearLayoutManager(BookingHisoryActivity.this, LinearLayoutManager.VERTICAL, false);
        layoutManager2.scrollToPositionWithOffset(0, 0);
        rycPastBooking.setLayoutManager(layoutManager2);
        rycPastBooking.setHasFixedSize(true);
        rycPastBooking.setAdapter(pastBookingAdapter);


        if(scrollListener1==null) {
            scrollListener1 = new EndlessRecyclerViewScrollListener(layoutManager1) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    //  if (totalItemsCount>19) {
                    futureBookingAdapter.showLoading(true);
                    apiForGetBooking(page, false);
                    // }
                }
            };
        }
        if(scrollListener2==null) {
            scrollListener2 = new EndlessRecyclerViewScrollListener(layoutManager2) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    // if (totalItemsCount>19) {
                    pastBookingAdapter.showLoading(true);
                    apiForGetBooking(page, false);
                    //}
                }
            };
        }

        rycFutureBooking.addOnScrollListener(scrollListener1);
        rycPastBooking.addOnScrollListener(scrollListener2);

        if(pastBookings.size()==0) {
            apiForGetBooking(0,true);
        }

        btnBack.setOnClickListener(this);
        tabPast.setOnClickListener(this);
        tabFuture.setOnClickListener(this);
    }

    private void apiForGetBooking(final int page,final boolean isEnableProgress){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingHisoryActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetBooking(page,isEnableProgress);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("type", sBookingTYpe);
        params.put("page", String.valueOf(page));
        params.put("limit", "10");

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingHisoryActivity.this, "userBooking", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    String total = js.getString("total");

                    ll_progress.setVisibility(View.GONE);

                    if (status.equalsIgnoreCase("success")) {
                        futureBookingAdapter.showLoading(false);
                        pastBookingAdapter.showLoading(false);

                        if (page == 0) {
                            futureBookings.clear();
                            pastBookings.clear();
                        }
                        parsingOfBooking(js,page);

                    }else {
                        rycFutureBooking.setVisibility(View.GONE);
                        rycPastBooking.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                        tvNoData.setText(message);
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
            JSONArray jsonArray = js.getJSONArray("Booking");

            if (jsonArray != null && jsonArray.length() != 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    Gson gson = new Gson();
                    JSONObject object = jsonArray.getJSONObject(i);
                    BookingHistory item = gson.fromJson(String.valueOf(object), BookingHistory.class);
                    item._id = Integer.parseInt(object.getString("_id"));
                    item.isExpand = false;

                    if (sBookingTYpe.equals("past"))
                        pastBookings.add(item);
                    else
                        futureBookings.add(item);
                }

                if (sBookingTYpe.equals("past") && pastBookings.size()!=0) {
                    rycPastBooking.setVisibility(View.VISIBLE);
                    rycFutureBooking.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.GONE);

                }else if (futureBookings.size()!=0){
                    rycFutureBooking.setVisibility(View.VISIBLE);
                    rycPastBooking.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.GONE);
                }

            } else if (page==0 && (jsonArray != null ? jsonArray.length() : 0) ==0){
                rycFutureBooking.setVisibility(View.GONE);
                rycPastBooking.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
            }
            futureBookingAdapter.notifyDataSetChanged();
            pastBookingAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 900) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (v.getId()){
            case R.id.tabPast:
                if (clickId!=1){
                    clickId = 1;
                    sBookingTYpe = "past";
                    rycFutureBooking.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.GONE);
                    tabPast.setBackgroundResource(R.drawable.bg_tab_selected);
                    tabFuture.setBackgroundResource(R.drawable.bg_tab_unselected);
                    tvPast.setTextColor(getResources().getColor(R.color.white));
                    tvFuture.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvHeaderTitle.setText(getString(R.string.past_booking));
                    apiForGetBooking(0,true);
                }
                break;

            case R.id.tabFuture:
                if (clickId!=2) {
                    clickId = 2;
                    sBookingTYpe = "future";
                    //     rycFutureBooking.setVisibility(View.VISIBLE);
                    rycPastBooking.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.GONE);
                    tabFuture.setBackgroundResource(R.drawable.bg_second_tab_selected);
                    tabPast.setBackgroundResource(R.drawable.bg_second_tab_unselected);
                    tvPast.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvFuture.setTextColor(getResources().getColor(R.color.white));
                    tvHeaderTitle.setText(getString(R.string.future_booking));
                    apiForGetBooking(0,true);
                }
                break;

            case R.id.btnBack:
                onBackPressed();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 30 && resultCode != 0) {
            if (data != null) {
                apiForGetBooking(0,true);
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
