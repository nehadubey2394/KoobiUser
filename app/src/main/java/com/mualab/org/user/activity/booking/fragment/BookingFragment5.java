package com.mualab.org.user.activity.booking.fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.BookedServicesAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.mualab.org.user.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;


public class BookingFragment5 extends Fragment implements View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1,totalPrice,location="";
    private BookingInfo bookingInfo,firstBooking;
    private BookedServicesAdapter adapter;
    private ArrayList<BookingInfo> selectedServices;
    int pos = BookingFragment4.arrayListbookingInfo.size()-1;
    private ArtistsSearchBoard item;
    private TextView tvArtistLoc;

    public BookingFragment5() {
        // Required empty public constructor
    }

    public static BookingFragment5 newInstance(BookingInfo bookingInfo) {
        BookingFragment5 fragment = new BookingFragment5();
        Bundle args = new Bundle();
        args.putSerializable("bookingInfo", bookingInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BookingActivity.lyReviewPost.setVisibility(View.GONE);
        BookingActivity.lyArtistDetail.setVisibility(View.GONE);
        BookingActivity.tvBuisnessName.setVisibility(View.VISIBLE);
        if (getArguments() != null) {
            bookingInfo = (BookingInfo) getArguments().getSerializable("bookingInfo");
            if (bookingInfo != null)
                item = bookingInfo.item;

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking5, container, false);
        initView();
        setViewId(rootView);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    private void initView(){
        selectedServices = new ArrayList<>();
        adapter = new BookedServicesAdapter((AppCompatActivity) getActivity(), selectedServices,item);
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        AppCompatButton btnEditDate = rootView.findViewById(R.id.btnEditDate);
        AppCompatButton btnEditLocation = rootView.findViewById(R.id.btnEditLocation);
        AppCompatButton btnConfirmBooking = rootView.findViewById(R.id.btnConfirmBooking);
        CircleImageView ivSelectStaffProfile = rootView.findViewById(R.id.ivSelectStaffProfile);
        TextView tvStaffArtistName = rootView.findViewById(R.id.tvStaffArtistName);
        tvArtistLoc = rootView.findViewById(R.id.tvArtistLoc);
        TextView tvBookingDate = rootView.findViewById(R.id.tvBookingDate);
        TextView tvBookingTime = rootView.findViewById(R.id.tvBookingTime);
        TextView tvTotalPrice = rootView.findViewById(R.id.tvTotalPrice);

        firstBooking = BookingFragment4.arrayListbookingInfo.get(pos);
        tvBookingDate.setText(firstBooking.date);
        tvBookingTime.setText(firstBooking.time);
        tvStaffArtistName.setText(bookingInfo.artistName);

        if (!bookingInfo.profilePic.equals("")){
            Picasso.with(mContext).load(bookingInfo.profilePic).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivSelectStaffProfile);
        }
        if (!bookingInfo.artistAddress.equals(""))
            tvArtistLoc.setText(bookingInfo.artistAddress);
        else
            tvArtistLoc.setText("NA");

        if (bookingInfo.serviceType.equals("2"))
            btnEditLocation.setVisibility(View.VISIBLE);
        else
            btnEditLocation.setVisibility(View.GONE);

        RecyclerView rycBookingInfo = rootView.findViewById(R.id.rycSelectedServ);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        rycBookingInfo.setLayoutManager(layoutManager2);
        rycBookingInfo.setNestedScrollingEnabled(false);
        rycBookingInfo.setAdapter(adapter);

        selectedServices.clear();
        selectedServices.addAll(BookingFragment4.arrayListbookingInfo);
        adapter.notifyDataSetChanged();

        double price = 0;

        for (int i = 0; i<selectedServices.size(); i++){
            BookingInfo item = selectedServices.get(i);
            price = price+item.price;
        }
        totalPrice = String.valueOf(price);
        tvTotalPrice.setText("£"+totalPrice);
        btnEditDate.setOnClickListener(this);
        btnEditLocation.setOnClickListener(this);
        btnConfirmBooking.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
        BookingActivity.tvBuisnessName.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
        BookingActivity.tvBuisnessName.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnEditDate:
                MyToast.getInstance(mContext).showSmallCustomToast("Under Developement");
                break;
            case R.id.btnEditLocation:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                                getLocation();
                            }
                        }
                    }).show();
                }else
                    getLocation();

                break;
            case R.id.btnConfirmBooking:
                apiForConfirmBooking();
                break;
        }
    }

    private void apiForConfirmBooking(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForConfirmBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", firstBooking.artistId);
        params.put("totalPrice", totalPrice);
        params.put("discountPrice", "");
        params.put("bookingdate", firstBooking.selectedDate);
        params.put("bookingTime", firstBooking.time);
        // params.put("customerType", "1");
        params.put("voucherId", "");
        params.put("paymentType", "1");

        if (item.serviceType.equals("2") && !location.equals(""))
            params.put("location", location);
        else
            params.put("location", firstBooking.artistAddress);

        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "confirmBooking", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                        ((BookingActivity)mContext).finish();
                    }else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    Progress.hide(mContext);
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

    private void getLocation() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(((BookingActivity)mContext));
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(mContext, data);
                LatLng latLng = place.getLatLng();
                distance(latLng.latitude,latLng.longitude,place);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(mContext, data);
                // TODO: Handle the error.
                Log.i("", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void distance (double lat_b, double lng_b,Place place )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-item.latitude);
        double lngDiff = Math.toRadians(lng_b-item.longitude);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(item.longitude)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        double radius = Double.parseDouble(item.radius);
        // int meterConversion = 1609;

        // double diff = distance * meterConversion;

        if (radius>=distance) {
            tvArtistLoc.setText(place.getName());
            location = ""+place.getName();
        }
        else
            MyToast.getInstance(mContext).showDasuAlert("Selected artist services is not available at this location");

    }
}