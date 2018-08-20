package com.mualab.org.user.activity.booking_histories.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.adapter.BookedServicesAdapter;
import com.mualab.org.user.activity.booking_histories.model.BookingInfo;
import com.mualab.org.user.activity.booking_histories.model.Bookings;
import com.mualab.org.user.activity.booking_histories.model.UserDetail;
import com.mualab.org.user.activity.payment.activity.PaymentActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BookingDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private String bookingId="",artistName,artistProfile="",key="";
    private BookedServicesAdapter adapter;
    private TextView tvBookingDate,tvBookingTime,tvBookingLoc,tvUserName,tvBookingStatus,
            tvTransectionId,tvPayType,tvTotalPrice;
    private List<BookingInfo> bookingInfoList;
    private ImageView ivChat,ivCall,ivLocation,ivCancle;
    private LinearLayout llBottom;
    private Bookings item;
    private AppCompatButton btnPay;
    private boolean isChangedOccured = false;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);
        intent = getIntent();
        if (intent!=null){
            bookingId =  intent.getStringExtra("bookingId");
            artistName =  intent.getStringExtra("artistName");
            artistProfile =  intent.getStringExtra("artistProfile");
            if (intent.hasExtra("key"))
                key =  intent.getStringExtra("key");
        }
        initView();
        setViewId();
    }

    private void initView(){
        item = new Bookings();
        bookingInfoList = new ArrayList<>();
        adapter = new BookedServicesAdapter(BookingDetailActivity.this, bookingInfoList);
    }

    private void setViewId(){
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.appoinment));
        ImageView ivUserProfile = findViewById(R.id.ivUserProfile);
        ivUserProfile.setVisibility(View.GONE);

        CircleImageView ivSelectStaffProfile = findViewById(R.id.ivSelectStaffProfile);
        TextView tvStaffArtistName = findViewById(R.id.tvStaffArtistName);
        if (!artistProfile.equals("")){
            Picasso.with(BookingDetailActivity.this).load(artistProfile).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivSelectStaffProfile);
        }else
            ivSelectStaffProfile.setImageDrawable(getResources().getDrawable(R.drawable.defoult_user_img));

        tvStaffArtistName.setText(artistName);

        tvPayType = findViewById(R.id.tvPayType);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvTransectionId = findViewById(R.id.tvTransectionId);
        tvBookingStatus = findViewById(R.id.tvBookingStatus);

        btnPay = findViewById(R.id.btnPay);

        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvBookingTime = findViewById(R.id.tvBookingTime);
        tvBookingLoc = findViewById(R.id.tvBookingLoc);

        ivChat = findViewById(R.id.ivChat);
        ivCall = findViewById(R.id.ivCall);
        ivLocation = findViewById(R.id.ivLocation);
        ivCancle = findViewById(R.id.ivCancle);

        llBottom = findViewById(R.id.llBottom);

        RecyclerView rycServices = findViewById(R.id.rycServices);
        LinearLayoutManager layoutManager = new LinearLayoutManager(BookingDetailActivity.this);
        rycServices.setLayoutManager(layoutManager);
        rycServices.setAdapter(adapter);

        apiForGetBookingDetail(true);

        ivCancle.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnPay.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        long mLastClickTime = 0;
        if (SystemClock.elapsedRealtime() - mLastClickTime < 900) {
            return;
        }
        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnPay:
                if (item.bookStatus.equals("3") && (item.paymentStatus.equals("0") && item.paymentType.equals("2"))){
                    Intent intent = new Intent(new Intent(BookingDetailActivity.this,PaymentActivity.class));
                    intent.putExtra("bookingId",  String.valueOf(item._id));
                    intent.putExtra("totalPrice",  item.totalPrice);
                    startActivityForResult(intent, 40);
                }
                break;

            case R.id.ivCancle:
                showCancleDialog();
                break;

        }
    }

    public void showCancleDialog() {
        View DialogView = View.inflate(BookingDetailActivity.this, R.layout.dialog_layout_cancle_booking, null);

        final Dialog dialog = new Dialog(BookingDetailActivity.this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(DialogView);

        AppCompatButton btnYes = DialogView.findViewById(R.id.btnYes);
        AppCompatButton btnNo = DialogView.findViewById(R.id.btnNo);

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                actionForBooking("cancel",true);
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void actionForBooking(String type,boolean isCancelled){
        String serviceId = "",subServiceId = "",artistServiceId="";
        if (bookingInfoList.size()!=0){
            for (int i=0; i<bookingInfoList.size(); i++){
                BookingInfo bookingInfo = bookingInfoList.get(i);
                if (serviceId.equals("")){
                    serviceId = bookingInfo.serviceId;
                }else {
                    if (!serviceId.contains(bookingInfo.serviceId))
                        serviceId = serviceId + ","+bookingInfo.serviceId;
                }
                if (subServiceId.equals("")){
                    subServiceId = bookingInfo.subServiceId;
                }else {
                    if (!subServiceId.contains(bookingInfo.subServiceId))
                        subServiceId = subServiceId + ","+bookingInfo.subServiceId;
                }

                if (artistServiceId.equals("")){
                    artistServiceId = bookingInfo.artistServiceId;
                }else {
                    if (!artistServiceId.contains(bookingInfo.artistServiceId))
                        artistServiceId = artistServiceId + ","+bookingInfo.artistServiceId;
                }

            }
            apiForBookingAction(type,item,serviceId,subServiceId,artistServiceId,isCancelled);
        }/*else {
            if (bookingInfoList.size()!=0){

                for (int i=0; i<item.pendingBookingInfos.size(); i++){
                    BookingInfo bookingInfo = item.pendingBookingInfos.get(i);
                    if (serviceId.equals("")){
                        serviceId = bookingInfo.serviceId;
                    }else {
                        if (!serviceId.contains(bookingInfo.serviceId))
                            serviceId = serviceId + ","+bookingInfo.serviceId;
                    }
                    if (subServiceId.equals("")){
                        subServiceId = bookingInfo.subServiceId;
                    }else {
                        if (!subServiceId.contains(bookingInfo.subServiceId))
                            subServiceId = subServiceId + ","+bookingInfo.subServiceId;
                    }

                    if (artistServiceId.equals("")){
                        artistServiceId = bookingInfo.artistServiceId;
                    }else {
                        if (!artistServiceId.contains(bookingInfo.artistServiceId))
                            artistServiceId = artistServiceId + ","+bookingInfo.artistServiceId;
                    }

                }
                apiForBookingAction(type,item,serviceId,subServiceId,artistServiceId,isCancelled);
            }
        }*/

    }

    private void apiForGetBookingDetail(final boolean isProgressShow){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingDetailActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetBookingDetail(isProgressShow);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("bookingId", bookingId);

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingDetailActivity.this, "bookingDetails", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(BookingDetailActivity.this);
                    if (status.equalsIgnoreCase("success")) {

                        JSONArray array = js.getJSONArray("bookingDetails");
                        if (array!=null && array.length()!=0) {
                            for (int j = 0; j < array.length(); j++) {
                                String serviceName = "";

                                JSONObject object = array.getJSONObject(j);
                                item._id = object.getString("_id");
                                item.bookingDate = object.getString("bookingDate");
                                item.artistId = object.getString("artistId");
                                item.bookingTime = object.getString("bookingTime");
                                item.bookStatus = object.getString("bookStatus");
                                item.paymentType = object.getString("paymentType");
                                item.paymentStatus = object.getString("paymentStatus");
                                item.totalPrice = object.getString("totalPrice");
                                item.location = object.getString("location");
                                item.transjectionId = object.getString("transjectionId");
                                item.isFinsh = object.getString("isFinsh");

                                JSONArray arrUserDetail = object.getJSONArray("userDetail");
                                if (arrUserDetail != null && arrUserDetail.length() != 0) {
                                    for (int k = 0; k < arrUserDetail.length(); k++) {
                                        Gson gson = new Gson();
                                        JSONObject userObj = arrUserDetail.getJSONObject(k);
                                        item.userDetail = gson.fromJson(String.valueOf(userObj), UserDetail.class);
                                    }
                                }

                                JSONArray arrBookingInfo = object.getJSONArray("bookingInfo");
                                if (arrBookingInfo != null && arrBookingInfo.length() != 0) {
                                    for (int l = 0; l < arrBookingInfo.length(); l++) {
                                        JSONObject bInfoObj = arrBookingInfo.getJSONObject(l);
                                        BookingInfo bookingInfo = new BookingInfo();
                                        bookingInfo._Id = bInfoObj.getString("_id");
                                        bookingInfo.bookingPrice = bInfoObj.getString("bookingPrice");
                                        bookingInfo.serviceId = bInfoObj.getString("serviceId");
                                        bookingInfo.subServiceId = bInfoObj.getString("subServiceId");
                                        bookingInfo.artistServiceId = bInfoObj.getString("artistServiceId");
                                        bookingInfo.bookingDate = bInfoObj.getString("bookingDate");
                                        bookingInfo.startTime = bInfoObj.getString("startTime");
                                        bookingInfo.endTime = bInfoObj.getString("endTime");
                                        bookingInfo.staffId = bInfoObj.getString("staffId");
                                        bookingInfo.staffName = bInfoObj.getString("staffName");
                                        bookingInfo.staffImage = bInfoObj.getString("staffImage");
                                        bookingInfo.bookingStatus = item.bookStatus;
                                        bookingInfo.artistServiceName = bInfoObj.getString("artistServiceName");

                                        if (serviceName.equals("")) {
                                            serviceName = bookingInfo.artistServiceName;
                                        }

                                        bookingInfoList.add(bookingInfo);
                                        // bookingInfo.bookingDetail = item;
                                   /*     if (user.businessType.equals("independent")) {
                                            if (bookingInfo.staffId.equals(user.id)){
                                                if (item.bookStatus.equals("0")) {
                                                    item.pendingBookingInfos.add(bookingInfo);
                                                    bookingInfoList.add(bookingInfo);
                                                } else {
                                                    item.todayBookingInfos.add(bookingInfo);
                                                    bookingInfoList.add(bookingInfo);
                                                }

                                            }
                                        }else {
                                            if (item.bookStatus.equals("0")) {
                                                item.pendingBookingInfos.add(bookingInfo);
                                                bookingInfoList.add(bookingInfo);
                                            } else {
                                                item.todayBookingInfos.add(bookingInfo);
                                                bookingInfoList.add(bookingInfo);
                                            }
                                        }*/
                                    }
                                    item.artistServiceName = serviceName;
                                    upDateUI(item);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }

                    }else {
                        MyToast.getInstance(BookingDetailActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(BookingDetailActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        //      MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(isProgressShow)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void apiForBookingAction(final String type,final Bookings bookings,final String serviceId ,final String subServiceId ,final String artistServiceId,final  boolean isCancelled){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingDetailActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForBookingAction(type,bookings,serviceId,subServiceId,artistServiceId,isCancelled);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", bookings.artistId);
        params.put("userId", bookings.userDetail._id);
        params.put("bookingId", bookings._id);
        params.put("serviceId", serviceId);
        params.put("subserviceId", subServiceId);
        params.put("artistServiceId", artistServiceId);
        params.put("type", type);
        if (type.equals("complete")){
            params.put("paymentType ", item.paymentType);
        }

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingDetailActivity.this, "bookingAction", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        llBottom.setVisibility(View.VISIBLE);
                        // llBottom2.setVisibility(View.GONE);
                        isChangedOccured = true;

                        if (type.equals("reject")){
                            for (int i=0; i<bookingInfoList.size(); i++){
                                bookingInfoList.get(i).bookingStatus = "2";
                            }
                            ivCancle.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        }

                        if (isCancelled)
                            MyToast.getInstance(BookingDetailActivity.this).showDasuAlert("Request has been cancelled");
                        else
                            MyToast.getInstance(BookingDetailActivity.this).showDasuAlert(message);

                    }else {
                        MyToast.getInstance(BookingDetailActivity.this).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    Progress.hide(BookingDetailActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        //      MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void upDateUI(Bookings bookingInfo){
        tvBookingLoc.setText(bookingInfo.location);
        tvBookingDate.setText(changeDateFormate(bookingInfo.bookingDate));
        tvBookingTime.setText(bookingInfo.bookingTime);

        if (bookingInfo.paymentType.equals("2")){
            if (item.bookStatus.equals("3") && item.paymentStatus.equals("0"))
                btnPay.setVisibility(View.VISIBLE);
            else
                btnPay.setVisibility(View.GONE);

            btnPay.setText("Pay £"+bookingInfo.totalPrice);

            tvPayType.setText("Online");
        }else {
            tvPayType.setText("Cash");
        }

        double price = Double.parseDouble(bookingInfo.totalPrice);

        tvTotalPrice.setText("£"+price);

        if (!bookingInfo.transjectionId.equals(""))
            tvTransectionId.setText(bookingInfo.transjectionId);

        switch (bookingInfo.bookStatus) {
            case "0":
                //tvBookingStatus.setText(getString(R.string.booking)+" "+getString(R.string.pending));
                tvBookingStatus.setText(getString(R.string.a_waiting_confirmation));
                break;
            case "1":
                tvBookingStatus.setText(getString(R.string.booking)+" "+getString(R.string.confirmed));
                break;
            case "2":
                tvBookingStatus.setText(getString(R.string.booking)+" "+getString(R.string.text_cancelled));
                break;
            case "3":
                if (bookingInfo.paymentType.equals("2") && bookingInfo.paymentStatus.equals("0")){
                    tvBookingStatus.setText("Payment pending");
                }else {
                    tvBookingStatus.setText(getString(R.string.booking)+" "+getString(R.string.text_completed));
                }
                break;
        }

        switch (bookingInfo.bookStatus) {
            case "0":
                // llBottom2.setVisibility(View.VISIBLE);
                llBottom.setVisibility(View.VISIBLE);
                break;
            case "2":
                //   llBottom2.setVisibility(View.GONE);
                llBottom.setVisibility(View.VISIBLE);
                ivCancle.setVisibility(View.GONE);
                break;
            case "3":
                //   llBottom2.setVisibility(View.GONE);
                llBottom.setVisibility(View.VISIBLE);
                ivCancle.setVisibility(View.GONE);
                break;
            default:
                //  llBottom2.setVisibility(View.GONE);
                llBottom.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 40 && resultCode != 0) {
            if (data != null) {
                isChangedOccured = true;
                apiForGetBookingDetail(true);
            }
        }
    }

    private String changeDateFormate(String sDate){
        SimpleDateFormat inputDf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputtDf = new SimpleDateFormat("dd/MM/yyyy");
        Date formatedDate = null;
        String date = "";
        try {
            formatedDate = inputDf.parse(sDate);
            date =  outputtDf.format(formatedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @Override
    public void onBackPressed() {
        if(!key.equals("") && key.equals("main")){
            Intent intent=new Intent(BookingDetailActivity.this,BookingHisoryActivity.class);
            intent.putExtra("key","main");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        if (!isChangedOccured){
            finish();
        }
        else {
            Intent intent = new Intent();
            intent.putExtra("isChangedOccured", "true");
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
