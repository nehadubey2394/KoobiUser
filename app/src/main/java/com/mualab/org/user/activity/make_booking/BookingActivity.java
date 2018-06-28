package com.mualab.org.user.activity.make_booking;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.make_booking.adapter.AdapterBusinessDays;
import com.mualab.org.user.activity.make_booking.background_service.ExpiredBookingJobService;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment1;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment2;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment4;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment5;
import com.mualab.org.user.activity.make_booking.listner.HideFilterListener;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.booking.StaffInfo;
import com.mualab.org.user.data.model.booking.StaffServices;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.BookingServices3;
import com.mualab.org.user.data.model.booking.BusinessDay;
import com.mualab.org.user.data.model.booking.Services;
import com.mualab.org.user.data.model.booking.SubServices;
import com.mualab.org.user.data.model.booking.TimeSlot;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingActivity extends AppCompatActivity implements View.OnClickListener,
        HideFilterListener {
    private ArtistsSearchBoard item;
    private TextView tvBuisnessName,title_booking;
    private LinearLayout lyReviewPost,lyArtistDetail;
    private ArrayList<BusinessDay> businessDays;
    private AdapterBusinessDays adapter;
    private RatingBar rating;
    private ImageView ivHeaderProfile;
    private String businessType;
    private  CountDownTimer countDownTimer;
    public static AppCompatActivity mcontext;
    private  long mLastClickTime = 0;

    public void setReviewPostVisibility(int visibility){
        if(lyReviewPost!=null)
            lyReviewPost.setVisibility(visibility);
    }

    public void setLyArtistDetailVisibility(int visibility){
        if(lyArtistDetail!=null)
            lyArtistDetail.setVisibility(visibility);
    }

    public void setTitleVisibility(String text){
        if(title_booking!=null) {
            title_booking.setVisibility(View.VISIBLE);
            title_booking.setText(text);
        }

    }

    public void setBuisnessNameVisibility(int visibility,String text){
        if(tvBuisnessName!=null) {
            tvBuisnessName.setVisibility(visibility);
            tvBuisnessName.setText(text);
        }
    }


    @Override
    public void onServiceAdded(boolean isShow) {
        BookingFragment2 frag = ((BookingFragment2) getSupportFragmentManager().findFragmentByTag("com.mualab.org.user.activity.make_booking.fragment.BookingFragment2"));
        frag.hideFilter(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Intent i = getIntent();
        item = (ArtistsSearchBoard) i.getSerializableExtra("item");
        businessType = item.businessType;
        mcontext = BookingActivity.this;
        initView();
    }

    private void initView(){

        businessDays = new ArrayList<>();
        //   businessDayOld  =  getBusinessdays();
        adapter = new AdapterBusinessDays(BookingActivity.this, businessDays);
        title_booking = findViewById(R.id.tvHeaderTitle2);
        tvBuisnessName = findViewById(R.id.tvBuisnessName);
        lyReviewPost = findViewById(R.id.lyReviewPost);
        lyReviewPost.setVisibility(View.VISIBLE);
        lyArtistDetail = findViewById(R.id.lyArtistDetail);
        lyArtistDetail.setVisibility(View.VISIBLE);
        tvBuisnessName.setVisibility(View.GONE);
        TextView tvOpeningTime = findViewById(R.id.tvOpeningTime);
        rating = findViewById(R.id.rating);
        ImageButton ibtnChat2 = findViewById(R.id.ibtnChat2);
        ImageView ivHeaderUser2 = findViewById(R.id.ivHeaderUser2);
        ImageView ivHeaderBack2 = findViewById(R.id.ivHeaderBack2);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);

        tvOpeningTime.setOnClickListener(this);

        apiForGetArtistDetail();

        ivHeaderBack2.setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switch (view.getId()){
            case R.id.ivHeaderBack2 :
                onBackPressed();
                break;

            case R.id.tvOpeningTime :
               /* view.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, mLastClickTime);
*/
                if (businessDays.size()!=0)
                    showDialog();
                break;
        }
    }

    private void updateView(){
        title_booking.setText(getString(R.string.title_booking));
        tvBuisnessName.setText(item.businessName);
        rating.setRating(Float.parseFloat(item.ratingCount));
        TextView tvArtistName = findViewById(R.id.tvArtistName);
        tvArtistName.setText(item.userName);

        if (!item.profileImage.equals("")){
            Picasso.with(BookingActivity.this).load(item.profileImage).placeholder(R.drawable.defoult_user_img).fit().into(ivHeaderProfile);
        }

        addFragment(BookingFragment2.newInstance(item,""), false, R.id.flBookingContainer);

    }

    public  void showDialog() {
        View DialogView = View.inflate(BookingActivity.this, R.layout.fragment_business_hours, null);

        final Dialog alertDailog = new Dialog(BookingActivity.this, android.R.style.Theme_Light);
        alertDailog.setCanceledOnTouchOutside(true);
        alertDailog.setCancelable(true);

        alertDailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDailog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDailog.getWindow().getAttributes().windowAnimations = R.style.TopBottomAnimation;
        alertDailog.setContentView(DialogView);

        TextView tvArtistName = DialogView.findViewById(R.id.tvArtistName);
        TextView tvOpeningTime = DialogView.findViewById(R.id.tvOpeningTime);
        TextView title_booking = findViewById(R.id.tvHeaderTitle2);
        RatingBar rating = DialogView.findViewById(R.id.rating);
        ImageView ivHeaderBack = DialogView.findViewById(R.id.ivHeaderBack2);
        ImageView ivHeaderProfile = DialogView.findViewById(R.id.ivHeaderProfile);

        rating.setRating(Float.parseFloat(item.ratingCount));

        tvArtistName.setText(item.userName);

        RecyclerView rvBusinessDay = DialogView.findViewById(R.id.rvBusinessDay);
        rvBusinessDay.setLayoutManager(new LinearLayoutManager(BookingActivity.this));
        rvBusinessDay.setAdapter(adapter);

        if (!item.profileImage.equals(""))
            Picasso.with(BookingActivity.this).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivHeaderProfile);


        ivHeaderBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDailog.cancel();
            }
        });

        alertDailog.show();
    }

    private List<BusinessDay> getBusinessdays(){
        /*BusinessProfile businessProfile =  preSession.getBusinessProfile();
        if(businessProfile!=null && businessProfile.businessDays!=null)
            return businessProfile.businessDays;
        else*/ return  createDefaultBusinessHours();
    }

    private  List<BusinessDay> createDefaultBusinessHours(){
        List<BusinessDay>businessDays = new ArrayList<>();
        BusinessDay day1 = new BusinessDay();
        day1.dayName = getString(R.string.mon);
        day1.isOpen = false;
        day1.dayId = 0;
        day1.addTimeSlot(new TimeSlot(0));

        BusinessDay day2 = new BusinessDay();
        day2.dayName = getString(R.string.tues);
        day2.isOpen = false;
        day2.dayId = 1;
        day2.addTimeSlot(new TimeSlot(1));

        BusinessDay day3 = new BusinessDay();
        day3.dayName = getString(R.string.wednes);
        day3.isOpen = false;
        day3.dayId = 2;
        day3.addTimeSlot(new TimeSlot(2));

        BusinessDay day4 = new BusinessDay();
        day4.dayName = getString(R.string.thurs);
        day4.isOpen = false;
        day4.dayId = 3;
        day4.addTimeSlot(new TimeSlot(3));

        BusinessDay day5 = new BusinessDay();
        day5.dayName = getString(R.string.fri);
        day5.isOpen = false;
        day5.dayId = 4;
        day5.addTimeSlot(new TimeSlot(4));

        BusinessDay day6 = new BusinessDay();
        day6.dayName = getString(R.string.satur);
        day6.isOpen = false;
        day6.dayId = 5;
        day6.addTimeSlot(new TimeSlot(5));

        BusinessDay day7 = new BusinessDay();
        day7.dayName = getString(R.string.sun );
        day7.isOpen = false;
        day7.dayId = 6;
        day7.addTimeSlot(new TimeSlot(6));

        businessDays.add(day1);
        businessDays.add(day2);
        businessDays.add(day3);
        businessDays.add(day4);
        businessDays.add(day5);
        businessDays.add(day6);
        businessDays.add(day7 );
        return businessDays;
    }

    private void apiForGetArtistDetail(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetArtistDetail();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        if (item.businessType.equals("independent")){
            params.put("businessType", "independent");
        }else {
            params.put("businessType", "business");
        }

        params.put("artistId", item._id);
        params.put("userId", String.valueOf(user.id));
        // params.put("appType", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "artistDetailNew", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        JSONObject jsonObject = js.getJSONObject("artistDetail");
                        item._id = jsonObject.getString("_id");
                        item.userName = jsonObject.getString("userName");
                        item.firstName = jsonObject.getString("firstName");
                        item.lastName = jsonObject.getString("lastName");
                        item.profileImage = jsonObject.getString("profileImage");
                        item.ratingCount = jsonObject.getString("ratingCount");
                        item.reviewCount = jsonObject.getString("reviewCount");
                        item.postCount = jsonObject.getString("postCount");

                        item.latitude = Double.parseDouble(jsonObject.getString("latitude"));
                        item.longitude = Double.parseDouble(jsonObject.getString("longitude"));
                        item.radius = jsonObject.getString("radius");

                        item.businessName = jsonObject.getString("businessName");
                        item.inCallpreprationTime = jsonObject.getString("inCallpreprationTime");
                        item.outCallpreprationTime = jsonObject.getString("outCallpreprationTime");
                        item.serviceType = jsonObject.getString("serviceType");

                        //  Gson mainGson = new Gson();
                        // item = mainGson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                        if (jsonObject.has("address"))
                            item.address = jsonObject.getString("address");
                        item.businessType = businessType;

                        JSONArray allServiceArray = jsonObject.getJSONArray("allService");
                        if (allServiceArray!=null) {
                            for (int j=0; j<allServiceArray.length(); j++){
                                JSONObject object = allServiceArray.getJSONObject(j);
                                Services services = new Services();
                                services.serviceId = object.getString("serviceId");
                                services.serviceName = object.getString("serviceName");

                                JSONArray subServiesArray = object.getJSONArray("subServies");
                                if (subServiesArray!=null) {
                                    for (int k=0; k<subServiesArray.length(); k++){
                                        JSONObject jObj = subServiesArray.getJSONObject(k);
                                        // Gson gson = new Gson();
                                        // SubServices subServices = gson.fromJson(String.valueOf(jObj), SubServices.class);
                                        SubServices subServices = new SubServices();
                                        subServices._id = jObj.getString("_id");
                                        subServices.serviceId = jObj.getString("serviceId");
                                        subServices.subServiceId = jObj.getString("subServiceId");
                                        subServices.subServiceName = jObj.getString("subServiceName");

                                        JSONArray artistservices = jObj.getJSONArray("artistservices");
                                        for (int m=0; m<artistservices.length(); m++){
                                            JSONObject jsonObject3 = artistservices.getJSONObject(m);
                                            // Gson gson3 = new Gson();
                                            // BookingServices3 services3 = gson3.fromJson(String.valueOf(jsonObject3), BookingServices3.class);
                                            // services3.setSelected(false);
                                            // services3.setBooked(false);
                                            BookingServices3 services3 = new BookingServices3();
                                            services3._id = jsonObject3.getString("_id");
                                            services3.title = jsonObject3.getString("title");
                                            services3.completionTime = jsonObject3.getString("completionTime");
                                            services3.outCallPrice = jsonObject3.getString("outCallPrice");
                                            services3.inCallPrice = jsonObject3.getString("inCallPrice");
                                            services3.setSelected(false);
                                            services3.setBooked(false);

                                            if ( !services3.outCallPrice.equals("null") || !services3.outCallPrice.equals("0")){
                                                services3.isOutCall3 = true;
                                                subServices.isOutCall2 = true;
                                                services.isOutCall = true;
                                            }
                                            subServices.artistservices.add(services3);
                                        }

                                        services.arrayList.add(subServices);
                                    }
                                    item.allService.add(services);
                                }
                            }
                        }

                        ArrayList<TimeSlot> timeSlots = new ArrayList<>();
                        JSONArray artistArray = jsonObject.getJSONArray("openingTime");
                        if (artistArray!=null) {
                            businessDays.clear();
                            HashMap<Integer,BusinessDay> hashMap = new HashMap<>();

                            for (int i=0; i<artistArray.length(); i++){

                                JSONObject object = artistArray.getJSONObject(i);
                                BusinessDay day = new BusinessDay();
                                day.dayId = Integer.parseInt(object.getString("day"));

                                switch (day.dayId){
                                    case 0:
                                        day.isOpen = true;
                                        day.dayName = getString(R.string.mon);
                                        break;
                                    case 1:
                                        day.isOpen = true;
                                        day.dayName = getString(R.string.tues);
                                        break;
                                    case 2:
                                        day.isOpen = true;
                                        day.dayName = getString(R.string.wednes);
                                        break;
                                    case 3:
                                        day.isOpen = true;
                                        day.dayName = getString(R.string.thurs);
                                        break;
                                    case 4:
                                        day.isOpen = true;
                                        day.dayName = getString(R.string.fri);
                                        break;
                                    case 5:
                                        day.isOpen = true;
                                        day.dayName = getString(R.string.satur);
                                        break;
                                    case 6:
                                        day.isOpen = true;
                                        day.dayName = getString(R.string.sun);
                                        break;
                                }

                                TimeSlot timeSlotNew = new TimeSlot(day.dayId);
                                timeSlotNew.startTime = object.getString("startTime");
                                timeSlotNew.endTime = object.getString("endTime");
                                timeSlotNew.dayId = day.dayId;

                                if (timeSlots.size()!=0){
                                    if (timeSlots.get(0).dayId == day.dayId){
                                        timeSlots.add(1,timeSlotNew);
                                    }else {
                                        timeSlots.clear();
                                        timeSlots.add(timeSlotNew);
                                    }
                                }else {
                                    timeSlots.add(timeSlotNew);
                                }

                                day.slots.addAll(timeSlots);

                                hashMap.put(day.dayId,day);
                                //        businessDayOld.add(day);

                            }

                            if (hashMap.size()!=0){

                                for (Object o : hashMap.entrySet()) {
                                    Map.Entry pair = (Map.Entry) o;
                                    BusinessDay businessDay = hashMap.get(pair.getKey());
                                    businessDays.add(businessDay);
                                    //    it.remove();
                                }
                                Collections.sort(businessDays, new Comparator<BusinessDay>() {
                                    @Override
                                    public int compare(BusinessDay a, BusinessDay b)
                                    {
                                        return a.dayId > b.dayId ? +1 : a.dayId < b.dayId ? -1 : 0;
                                    }
                                });
                            }

                            adapter.notifyDataSetChanged();
                            updateView();
                        }

                        JSONArray staffInfoArray = jsonObject.getJSONArray("staffInfo");
                        if (staffInfoArray!=null) {
                            for (int a=0; a<staffInfoArray.length(); a++) {
                                Gson gson = new Gson();
                                JSONObject staffInfoJSONObject = staffInfoArray.getJSONObject(a);
                                StaffInfo staffInfo = gson.fromJson(String.valueOf(staffInfoJSONObject), StaffInfo.class);

                                JSONArray staffHoursArray = staffInfoJSONObject.getJSONArray("staffHours");
                                if (staffHoursArray!=null) {
                                    for (int b = 0; b < staffHoursArray.length(); b++) {
                                        JSONObject staffTimeObj = staffHoursArray.getJSONObject(b);
                                        int day = Integer.parseInt(staffTimeObj.getString("day"));
                                        TimeSlot timeSlotNew = new TimeSlot(day);
                                        timeSlotNew.dayId = day;
                                        timeSlotNew.startTime = staffTimeObj.getString("startTime");
                                        timeSlotNew.endTime = staffTimeObj.getString("endTime");
                                        staffInfo.staffHours.add(timeSlotNew);
                                    }
                                }

                                JSONArray staffServiceArray = staffInfoJSONObject.getJSONArray("staffService");
                                if (staffServiceArray!=null) {
                                    for (int c = 0; c < staffServiceArray.length(); c++) {
                                        JSONObject staffServObj = staffServiceArray.getJSONObject(c);
                                        Gson gson2 = new Gson();
                                        StaffServices services = gson2.fromJson(String.valueOf(staffServObj), StaffServices.class);
                                        staffInfo.staffServices.add(services);
                                    }
                                }
                                item.staffInfo.add(staffInfo);
                            }
                        }
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
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

    public void addFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in,0,0);
            transaction.add(containerId, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }

    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        while (i > 0) {
            fm.popBackStackImmediate();
            i--;
        }
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    private void showAlertDailog(final FragmentManager fm,final int fCont){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(BookingActivity.this, R.style.MyDialogTheme);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Alert!");
        alertDialog.setMessage("Are you sure you want to permanently remove all selected services?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                dialog.cancel();
                apiForCancleBooking(fm,fCont);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }

    private void apiForCancleBooking(final FragmentManager fm,final int fCont){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForCancleBooking(fm,fCont);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", item._id);
        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "deleteAllBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        BookingFragment4.arrayListbookingInfo.clear();
                        if (fCont==0){
                            fm.popBackStack();
                            finish();
                        }else {
                           /* fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            BookingFragment2 frag = ((BookingFragment2) getSupportFragmentManager().findFragmentByTag("com.mualab.org.user.activity.make_booking.fragment.BookingFragment2"));
                            frag.hideFilter(false);*/
                            finish();
                        }
                    }else {
                        MyToast.getInstance(BookingActivity.this).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    Progress.hide(BookingActivity.this);
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

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flBookingContainer);

        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();

        if (i==1 && currentFragment instanceof BookingFragment5 && BookingFragment4.arrayListbookingInfo.size()>0){
            showAlertDailog(fm,i);
        }
        else if (currentFragment instanceof BookingFragment4 && BookingFragment4.arrayListbookingInfo.size()==0){
            finish();
        }
        else if (currentFragment instanceof BookingFragment1){
            fm.popBackStack();
        }
        else if (i==3 && BookingFragment4.arrayListbookingInfo.size()>0){
            showAlertDailog(fm,i);
        }
        else if (i==2 && BookingFragment4.arrayListbookingInfo.size()>0){
            showAlertDailog(fm,i);
        }
        else if (i==1 && BookingFragment4.arrayListbookingInfo.size()>0){
            showAlertDailog(fm,i);
        }
        else if (i==0 && BookingFragment4.arrayListbookingInfo.size()>0){
            showAlertDailog(fm,i);
        }
        else if (i > 0) {
            fm.popBackStack();
        }else  {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Session session = Mualab.getInstance().getSessionManager();
        session.setUserChangedLocLat("");
        session.setUserChangedLocLng("");
        item = null;
        countDownTimer = null;
        adapter = null;
        super.onDestroy();
        //session.setIsOutCallFilter(false);
    }

    @Override
    protected void onStop() {
        //   if (BookingFragment4.arrayListbookingInfo.size()!=0)
        // startTimer();
        super.onStop();
        //  if (BookingFragment4.arrayListbookingInfo.size()!=0)
        // startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    //Stop Countdown method
    public void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    //Start Countodwn method
    public void startTimer() {
        startService(new Intent(BookingActivity.this, ExpiredBookingJobService.class));

       /* countDownTimer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                //  millisUntilFinished / 1000;
            }
            public void onFinish() {
                //  countDownTimer = null;//set CountDownTimer to null
                if (BookingFragment4.arrayListbookingInfo.size()!=0)
                    apiForDeleteAllPendingBooking();
            }
        }.start();
*/
    }

    public static void refreshParentActivity(Context context) {
        Activity parentActivity = (Activity) context;
        Intent intent = parentActivity.getIntent();
        parentActivity.finish();
        context.startActivity(intent);
    }

    private void apiForDeleteAllPendingBooking(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(getApplicationContext(), new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForDeleteAllPendingBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(getApplicationContext(), "deleteUserBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        MyToast.getInstance(BookingActivity.this).showDasuAlert("Booking session has been expired, please try again");
                        BookingFragment4.arrayListbookingInfo.clear();
                        stopCountdown();
                        Session session = Mualab.getInstance().getSessionManager();
                        session.setUserChangedLocLat("");
                        session.setUserChangedLocLng("");
                        session.setUserChangedLocName("");
                        FragmentManager fm = getSupportFragmentManager();
                        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        stopService(new Intent(BookingActivity.this, ExpiredBookingJobService.class));
                        finish();
                    }else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
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
    }

}