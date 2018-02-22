package com.mualab.org.user.activity.booking;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.mualab.org.user.activity.booking.adapter.AdapterBusinessDays;
import com.mualab.org.user.activity.booking.fragment.BookingFragment1;
import com.mualab.org.user.activity.booking.fragment.BookingFragment2;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.booking.BusinessDay;
import com.mualab.org.user.model.booking.TimeSlot;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingActivity extends AppCompatActivity implements View.OnClickListener{
    public ArtistsSearchBoard item;
    private String mParam1;
    public static TextView title_booking,tvBuisnessName;
    public static LinearLayout lyReviewPost;
    public static LinearLayout lyArtistDetail;
    private List<BusinessDay> businessDays;
    private AdapterBusinessDays adapter;
    private TextView tvOpeningTime;
    private RatingBar rating;
    private ImageView ivHeaderProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Intent i = getIntent();
        item =  i.getParcelableExtra("item");
        mParam1 = i.getStringExtra("mParam");

        initView();
    }

    private void initView(){
        businessDays = new ArrayList<>();
        adapter = new AdapterBusinessDays(BookingActivity.this, businessDays);
        title_booking = findViewById(R.id.tvHeaderTitle2);
        tvBuisnessName = findViewById(R.id.tvBuisnessName);
        lyReviewPost = findViewById(R.id.lyReviewPost);
        lyReviewPost.setVisibility(View.VISIBLE);
        lyArtistDetail = findViewById(R.id.lyArtistDetail);
        lyArtistDetail.setVisibility(View.VISIBLE);
        tvBuisnessName.setVisibility(View.GONE);
        tvOpeningTime = findViewById(R.id.tvOpeningTime);
        rating = findViewById(R.id.rating);
        ImageButton ibtnChat2 = findViewById(R.id.ibtnChat2);
        ImageView ivHeaderUser2 = findViewById(R.id.ivHeaderUser2);
        ImageView ivHeaderBack2 = findViewById(R.id.ivHeaderBack2);
        ivHeaderProfile = findViewById(R.id.ivHeaderProfile);

        tvOpeningTime.setOnClickListener(this);

        apiForGetBusinessTime();

        // addFragment(new BookingFragment2(), false, R.id.flBookingContainer);

       /* if (mParam1.equals("1")){
        }else {
            addFragment(new BookingFragment1(), false, R.id.flBookingContainer);
        }*/

        ivHeaderBack2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivHeaderBack2 :
                onBackPressed();
                break;

            case R.id.tvOpeningTime :
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

        if (!item.profileImage.equals(""))
            Picasso.with(BookingActivity.this).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivHeaderProfile);
        addFragment(new BookingFragment2(), false, R.id.flBookingContainer);

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

    private void apiForGetBusinessTime(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(BookingActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetBusinessTime();
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
        // params.put("appType", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(BookingActivity.this, "artistDetail", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        JSONObject jsonObject = js.getJSONObject("artistDetail");
                    //    item = new ArtistsSearchBoard();
                        item._id = jsonObject.getString("_id");
                        item.userName = jsonObject.getString("userName");
                        item.firstName = jsonObject.getString("firstName");
                        item.lastName = jsonObject.getString("lastName");
                        item.profileImage = jsonObject.getString("profileImage");
                        item.ratingCount = jsonObject.getString("ratingCount");
                        item.reviewCount = jsonObject.getString("reviewCount");
                        item.postCount = jsonObject.getString("postCount");
                        item.businessName = jsonObject.getString("businessName");

                        JSONArray artistArray = jsonObject.getJSONArray("openingTime");
                        if (artistArray!=null) {
                            businessDays.clear();

                            for (int i=0; i<artistArray.length(); i++){
                                JSONObject object = artistArray.getJSONObject(i);
                                BusinessDay day = new BusinessDay();

                                day.isOpen = true;
                                day.dayId = Integer.parseInt(object.getString("day"));

                                switch (day.dayId){
                                    case 0:
                                        day.dayName = getString(R.string.mon);
                                        break;
                                    case 1:
                                        day.dayName = getString(R.string.tues);
                                        break;
                                    case 2:
                                        day.dayName = getString(R.string.wednes);
                                        break;
                                    case 3:
                                        day.dayName = getString(R.string.thurs);
                                        break;
                                    case 4:
                                        day.dayName = getString(R.string.fri);
                                        break;
                                    case 5:
                                        day.dayName = getString(R.string.satur);
                                        break;
                                    case 6:
                                        day.dayName = getString(R.string.sun);
                                        break;
                                }
                                TimeSlot timeSlot = new TimeSlot(day.dayId);
                                timeSlot.startTime = object.getString("startTime");
                                timeSlot.endTime = object.getString("endTime");
                                day.addTimeSlot(timeSlot);
                                //   ArtistsSearchBoard item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                businessDays.add(day);

                            }
                            adapter.notifyDataSetChanged();
                            updateView();
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
                    if (helper.error_Messages(error).contains("Session"))
                        Mualab.getInstance().getSessionManager().logout();
                    MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
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
            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_in,0,0);
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


    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
