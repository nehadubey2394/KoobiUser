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

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.Booking3ServiceAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.booking.BookingServices3;
import com.mualab.org.user.data.model.booking.SubServices;

import java.util.ArrayList;


public class BookingFragment3 extends Fragment {
    private Booking3ServiceAdapter adapter;
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1,bookingId="";
    private SubServices subServices;
    private ArtistsSearchBoard item;
    private  boolean isOutCallSelect,fromConfirmBooking = false;

    public BookingFragment3() {
        // Required empty public constructor
    }

    public static BookingFragment3 newInstance(boolean fromConfirmBooking,SubServices subServices,
                                               ArtistsSearchBoard item, boolean isOutCallSelect,String bookingId) {
        BookingFragment3 fragment = new BookingFragment3();
        Bundle args = new Bundle();
        args.putBoolean("param1", fromConfirmBooking);
        args.putSerializable("param2", subServices);
        args.putSerializable("param3", item);
        args.putBoolean("param4", isOutCallSelect);
        args.putString("param5", bookingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setReviewPostVisibility(8);
            ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
        }

        if (getArguments() != null) {
            fromConfirmBooking = getArguments().getBoolean("param1");
            isOutCallSelect = getArguments().getBoolean("param4");
            subServices = (SubServices) getArguments().getSerializable("param2");
            item = (ArtistsSearchBoard) getArguments().getSerializable("param3");
            bookingId =  getArguments().getString("param5");
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

        arrayList = subServices.artistservices;


        adapter = new Booking3ServiceAdapter(mContext, arrayList,item,isOutCallSelect,
                subServices,fromConfirmBooking,bookingId);

    }

    private void setViewId(View rootView){
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setTitleVisibility(mParam1);
        }

        RecyclerView rvLastService = rootView.findViewById(R.id.rvLastService);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvLastService.setLayoutManager(layoutManager);

        rvLastService.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setReviewPostVisibility(0);
            ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
            ((BookingActivity) mContext).setTitleVisibility(getString(R.string.title_booking));
            Mualab.getInstance().cancelAllPendingRequests();
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setReviewPostVisibility(0);
            ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
            ((BookingActivity) mContext).setTitleVisibility(getString(R.string.title_booking));
        }
        Mualab.getInstance().cancelAllPendingRequests();
        super.onDestroy();
    }
}