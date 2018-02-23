package com.mualab.org.user.activity.booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.model.SelectedServices;

import java.util.ArrayList;


public class BookingFragment5 extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ArrayList<SelectedServices>selectedServices;

    public BookingFragment5() {
        // Required empty public constructor
    }

    public static BookingFragment5 newInstance(String param1) {
        BookingFragment5 fragment = new BookingFragment5();
        Bundle args = new Bundle();
        args.putString("param1", param1);
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
            mParam1 = getArguments().getString("param1");
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
/*
        RecyclerView rycBookingInfo = rootView.findViewById(R.id.rycBookingInfo);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        rycBookingInfo.setLayoutManager(layoutManager2);
        rycBookingInfo.setNestedScrollingEnabled(false);
        rycBookingInfo.setAdapter(bookingInfoAdapter);*/

    }

/*
    private void addItems(){
        BookingTimeSlot item;
        for(int i=0;i<6;i++) {
            item = new BookingTimeSlot();
            switch (i) {
                case 0:
                    item.time = "10:00";
                    item.isSelected = "1";
                    break;
                case 1:
                    item.time = "11:00";
                    item.isSelected = "0";
                    break;

                case 2:
                    item.time = "12:30";
                    item.isSelected = "0";
                    break;

                case 3:
                    item.time = "1:00";
                    item.isSelected = "0";
                    break;

                case 4:
                    item.time = "1:30";
                    item.isSelected = "0";
                    break;

                case 5:
                    item.time = "2:00";
                    item.isSelected = "0";
                    break;

            }
            selectedServices.add(item);
        }
    }
*/

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
}
