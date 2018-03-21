package com.mualab.org.user.activity.booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.BookingSelectStaffAdapter;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.model.booking.BookingServices3;
import com.mualab.org.user.model.booking.BookingStaff;
import com.mualab.org.user.model.booking.SubServices;

import java.util.ArrayList;


public class BookingFragment1 extends Fragment {
    private BookingSelectStaffAdapter staffAdapter;
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ArtistsSearchBoard item;
    private BookingInfo bookingInfo;

    public BookingFragment1() {
        // Required empty public constructor
    }

    public static BookingFragment1 newInstance(String param1, ArtistsSearchBoard  item,BookingInfo bookingInfo) {
        BookingFragment1 fragment = new BookingFragment1();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putParcelable("param2", item);
        args.putSerializable("param3", bookingInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
            item = getArguments().getParcelable("param2")  ;
            bookingInfo = (BookingInfo) getArguments().getSerializable("param3");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking1, container, false);
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
        // staffList = new ArrayList<>();
        ArrayList<BookingStaff> staffList = item.staffList;
        staffAdapter = new BookingSelectStaffAdapter(mContext,item, staffList,mParam1,bookingInfo);
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        RecyclerView rvBookingSelectStaff = rootView.findViewById(R.id.rvBookingSelectStaff);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvBookingSelectStaff.setLayoutManager(layoutManager);
        rvBookingSelectStaff.setAdapter(staffAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BookingActivity.title_booking.setText(mParam1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BookingActivity.title_booking.setText(mParam1);
    }
}
