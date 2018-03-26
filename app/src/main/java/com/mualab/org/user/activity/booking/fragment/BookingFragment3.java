package com.mualab.org.user.activity.booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.Booking3ServiceAdapter;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.booking.BookingServices3;
import com.mualab.org.user.model.booking.SubServices;

import java.util.ArrayList;


public class BookingFragment3 extends Fragment {
    private Booking3ServiceAdapter adapter;
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private SubServices subServices;
    private ArtistsSearchBoard item;
    private  boolean isOutCallSelect,fromConfirmBooking = false;

    public BookingFragment3() {
        // Required empty public constructor
    }

    public static BookingFragment3 newInstance(boolean fromConfirmBooking,SubServices subServices, ArtistsSearchBoard item, boolean isOutCallSelect) {
        BookingFragment3 fragment = new BookingFragment3();
        Bundle args = new Bundle();
        args.putBoolean("param1", fromConfirmBooking);
        args.putSerializable("param2", subServices);
        args.putSerializable("param3", item);
        args.putBoolean("param4", isOutCallSelect);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BookingActivity.lyReviewPost.setVisibility(View.GONE);
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
        if (getArguments() != null) {
            fromConfirmBooking = getArguments().getBoolean("param1");
            isOutCallSelect = getArguments().getBoolean("param4");
            subServices = (SubServices) getArguments().getSerializable("param2");
            item = (ArtistsSearchBoard) getArguments().getSerializable("param3");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking3, container, false);
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

        ArrayList<BookingServices3> arrayList;

        // if (fromConfirmBooking)
        //     arrayList = subServices.bookedArtistServices;
        //  else
        arrayList = subServices.artistservices;


        adapter = new Booking3ServiceAdapter(mContext, arrayList,item,isOutCallSelect, subServices,fromConfirmBooking);

        // arrayList.clear();
        // addService();
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(mParam1);
        RecyclerView rvLastService = rootView.findViewById(R.id.rvLastService);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvLastService.setLayoutManager(layoutManager);
        // rvLastService.setMotionEventSplittingEnabled(false);

        rvLastService.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
    }
}