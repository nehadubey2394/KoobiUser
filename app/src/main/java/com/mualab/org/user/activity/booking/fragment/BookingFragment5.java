package com.mualab.org.user.activity.booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.SelectedServices;
import com.mualab.org.user.model.booking.BookingInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class BookingFragment5 extends Fragment implements View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ArrayList<SelectedServices>selectedServices;
    private BookingInfo bookingInfo;

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
       /*  bookingInfos = new ArrayList<>();
        listAdapter = new TimeSlotAdapter(mContext, timeSlots);
        bookingInfoAdapter = new BookingInfoAdapter(mContext, bookingInfos);
        addItems();
        addServices();*/
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        AppCompatButton btnEditDate = rootView.findViewById(R.id.btnEditDate);
        AppCompatButton btnEditLocation = rootView.findViewById(R.id.btnEditLocation);
        AppCompatButton btnEditService = rootView.findViewById(R.id.btnEditService);
        CircleImageView ivSelectStaffProfile = rootView.findViewById(R.id.ivSelectStaffProfile);
        TextView tvStaffArtistName = rootView.findViewById(R.id.tvStaffArtistName);

        tvStaffArtistName.setText(bookingInfo.artistName);
        if (!bookingInfo.profilePic.equals("")){
            Picasso.with(mContext).load(bookingInfo.profilePic).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivSelectStaffProfile);
        }
/*
        RecyclerView rycBookingInfo = rootView.findViewById(R.id.rycBookingInfo);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        rycBookingInfo.setLayoutManager(layoutManager2);
        rycBookingInfo.setNestedScrollingEnabled(false);
        rycBookingInfo.setAdapter(bookingInfoAdapter);*/
        btnEditDate.setOnClickListener(this);
        btnEditLocation.setOnClickListener(this);
        btnEditService.setOnClickListener(this);
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
                MyToast.getInstance(mContext).showSmallCustomToast("Under Developement");
                break;
            case R.id.btnEditService:
                MyToast.getInstance(mContext).showSmallCustomToast("Under Developement");
                break;
        }
    }
}
