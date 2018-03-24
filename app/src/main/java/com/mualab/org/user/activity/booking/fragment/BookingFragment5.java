package com.mualab.org.user.activity.booking.fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private double cLat,cLng;
    private boolean isConfirmbookingClicked = false,isEditLoc = false;
    private Session session;

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
        session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();
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

        tvStaffArtistName.setText(bookingInfo.artistName);

        if (!bookingInfo.profilePic.equals("")){
            Picasso.with(mContext).load(bookingInfo.profilePic).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivSelectStaffProfile);
        }

        if (Mualab.currentLocationForBooking!=null){
            cLat = Mualab.currentLocationForBooking.lat;
            cLng = Mualab.currentLocationForBooking.lng;
            location = getCurrentAddress(cLat,cLng);
        }

        if (!session.getUserChangedLocLat().equals("") && !session.getUserChangedLocLng().equals("")){
            cLat = Double.parseDouble(session.getUserChangedLocLat());
            cLng = Double.parseDouble(session.getUserChangedLocLng());
            location = getCurrentAddress(cLat,cLng);
        }

        if (bookingInfo.serviceType.equals("2")){
            btnEditLocation.setVisibility(View.VISIBLE);
            tvArtistLoc.setText(location);
        }
        else {
            btnEditLocation.setVisibility(View.GONE);
            if (!bookingInfo.artistAddress.equals(""))
                tvArtistLoc.setText(bookingInfo.artistAddress);
            else
                tvArtistLoc.setText("NA");
        }


        RecyclerView rycBookingInfo = rootView.findViewById(R.id.rycSelectedServ);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        rycBookingInfo.setLayoutManager(layoutManager2);
        rycBookingInfo.setNestedScrollingEnabled(false);
        rycBookingInfo.setAdapter(adapter);

        selectedServices.clear();
        selectedServices.addAll(BookingFragment4.arrayListbookingInfo);

        Collections.sort(selectedServices, new Comparator<BookingInfo>() {
            public int compare(BookingInfo o1, BookingInfo o2) {
                return o1.dateTime.compareTo(o2.dateTime);
            }
        });
        firstBooking = selectedServices.get(0);
        tvBookingDate.setText(firstBooking.date);
        tvBookingTime.setText(firstBooking.time);

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
                isEditLoc = true;
                isConfirmbookingClicked = false;
                getLocation();
                break;

            case R.id.btnConfirmBooking:
                isConfirmbookingClicked = true;
                if (bookingInfo.serviceType.equals("2")){
                    if (cLng!=0.0 && cLat!=0.0){
                        distance(Mualab.currentLocationForBooking.lat,Mualab.currentLocationForBooking.lng);
                    }else {
                        MyToast.getInstance(mContext).showDasuAlert("Enter your location");
                    }
                }else {
                    apiForConfirmBooking();
                }

                break;
        }
    }

    private void apiForConfirmBooking(){
        final Session session = Mualab.getInstance().getSessionManager();
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

        if (bookingInfo.serviceType.equals("2") && !location.equals(""))
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
                        if (isEditLoc){
                            session.setUserChangedLocLat(String.valueOf(cLat));
                            session.setUserChangedLocLng(String.valueOf(cLng));
                        }

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
                cLat = latLng.latitude;
                cLng = latLng.longitude;
                tvArtistLoc.setText(place.getName());
                location = ""+place.getName();
                distance(latLng.latitude,latLng.longitude);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(mContext, data);
                // TODO: Handle the error.
                Log.i("", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void distance (double lat_b, double lng_b)
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
            if (isConfirmbookingClicked && isEditLoc)
                apiForConfirmBooking();
            else if (isConfirmbookingClicked)
                apiForConfirmBooking();
        }
        else {
            if (isEditLoc)
                isEditLoc = false;

            MyToast.getInstance(mContext).showDasuAlert("Selected artist services is not available at this location");
        }

    }

    private String getCurrentAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(mContext, Locale.getDefault());
        String sAddress = "";
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);   String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            sAddress = knownName+" "+postalCode+" "+city+" "+state+" "+country;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sAddress;
    }
}