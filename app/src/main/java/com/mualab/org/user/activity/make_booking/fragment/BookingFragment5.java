package com.mualab.org.user.activity.make_booking.fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.make_booking.BookingActivity;
import com.mualab.org.user.activity.make_booking.adapter.BookedServicesAdapter;
import com.mualab.org.user.activity.make_booking.background_service.ExpiredBookingJobService;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.BookingInfo;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.mualab.org.user.utils.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;


public class BookingFragment5 extends Fragment implements View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1,totalPrice,location="",paymentType = "2";
    private BookingInfo bookingInfo,firstBooking;
    private BookedServicesAdapter adapter;
    private ArrayList<BookingInfo> selectedServices;
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
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setLyArtistDetailVisibility(8);
            ((BookingActivity) mContext).setBuisnessNameVisibility(0,item.businessName);
        }
        setViewId(view);
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
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setTitleVisibility(getString(R.string.title_booking));
        }

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
            location = session.getUserChangedLocName();
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

        ArrayList<BookingInfo> tempArray = new ArrayList<>();
        tempArray.clear();
        tempArray.addAll(BookingFragment4.arrayListbookingInfo);

        Collections.sort(tempArray, new Comparator<BookingInfo>() {
            public int compare(BookingInfo o1, BookingInfo o2) {
                return o1.dateTime.compareTo(o2.dateTime);
            }
        });
        selectedServices.clear();
        selectedServices.addAll(tempArray);

        firstBooking = selectedServices.get(0);
        SimpleDateFormat dfInput = new SimpleDateFormat("EEE, d MMMM yyyy");
        SimpleDateFormat dfOutput = new SimpleDateFormat("d MMMM yyyy");
        Date formatedDate = null;
        try {
            formatedDate = dfInput.parse(firstBooking.date);
            tvBookingDate.setText(dfOutput.format(formatedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvBookingTime.setText(firstBooking.time);

        adapter.notifyDataSetChanged();

        double price = 0;

        for (int i = 0; i<selectedServices.size(); i++){
            BookingInfo item = selectedServices.get(i);
            price = price+item.price;
        }
        tvTotalPrice.setText("£"+String.format("%.2f", price));
        totalPrice = String.format("%.2f", price);
        btnEditDate.setOnClickListener(this);
        btnEditLocation.setOnClickListener(this);
        btnConfirmBooking.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
            ((BookingActivity) mContext).setBuisnessNameVisibility(8,"");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
            ((BookingActivity) mContext).setBuisnessNameVisibility(8,"");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnEditDate:
                MyToast.getInstance(mContext).showSmallCustomToast("Under Developement");
                break;
            case R.id.btnEditLocation:
                isConfirmbookingClicked = false;
                getLocation();
                break;

            case R.id.btnConfirmBooking:
                isConfirmbookingClicked = true;
                if (bookingInfo.serviceType.equals("2")){
                    if (cLng!=0.0 && cLat!=0.0){
                        distance(cLat,cLng);
                    }else {
                        MyToast.getInstance(mContext).showDasuAlert("Enter your location");
                    }
                }else {
                    if (item.bankStatus.equals("1")) {
                        showPaymentDialog();
                    }else {
                        paymentType = "3";
                        apiForConfirmBooking();
                    }
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
        params.put("paymentType", paymentType);

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
                        mContext.stopService(new Intent(mContext, ExpiredBookingJobService.class));
                        ((BookingActivity)mContext).stopCountdown();

                        session.setUserChangedLocLat("");
                        session.setUserChangedLocLng("");
                        session.setUserChangedLocName("");

                        new SweetAlertDialog(mContext, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Congratulation!")
                                .setContentText("Your booking request has been successfully sent to artist.")
                                .setConfirmText("OK")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        BookingFragment4.arrayListbookingInfo.clear();
                                        ((BookingActivity)mContext).finish();
                                    }
                                })
                                .show();

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

    public void showPaymentDialog() {
        View DialogView = View.inflate(mContext, R.layout.dialog_payment_type, null);

        final Dialog dialog = new Dialog(mContext);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(DialogView);

        RadioButton rdOnline = DialogView.findViewById(R.id.rdOnline);
        RadioButton rdCash = DialogView.findViewById(R.id.rdCash);
        RadioGroup rdGrp = DialogView.findViewById(R.id.rdGrp);
        AppCompatButton btnCancel = DialogView.findViewById(R.id.btnCancel);
        AppCompatButton btnDone = DialogView.findViewById(R.id.btnDone);

        if (item.bankStatus.equals("0")) {
            rdOnline.setVisibility(View.GONE);
            rdCash.setChecked(true);
            paymentType = "3";
        }
        else
            rdOnline.setVisibility(View.VISIBLE);

        rdGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId==R.id.rdOnline)
                    paymentType = "2";
                if (checkedId==R.id.rdCash)
                    paymentType = "3";
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (!paymentType.equals(""))
                    apiForConfirmBooking();
                else
                    MyToast.getInstance(mContext).showDasuAlert("Please select payment method");
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
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
                isEditLoc = true;
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

    private void distance(double lat2, double lon2) {
        double theta = item.longitude - lon2;
        double dist = Math.sin(deg2rad(item.latitude))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(item.latitude))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        double radius = Double.parseDouble(item.radius);

        if (radius>=dist) {
            if (isEditLoc){
                session.setUserChangedLocLat(String.valueOf(cLat));
                session.setUserChangedLocLng(String.valueOf(cLng));
                session.setUserChangedLocName(location);
            }
            if (isConfirmbookingClicked) {
                if (item.bankStatus.equals("1")) {
                    showPaymentDialog();
                }else {
                    paymentType = "3";
                    apiForConfirmBooking();
                }
            }


        }
        else {
            if (isEditLoc)
                isEditLoc = false;

            MyToast.getInstance(mContext).showDasuAlert("Selected artist services is not available at this location");
        }
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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
            sAddress = knownName+" "+city+" "+state+" "+country+" "+postalCode;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sAddress;
    }
}