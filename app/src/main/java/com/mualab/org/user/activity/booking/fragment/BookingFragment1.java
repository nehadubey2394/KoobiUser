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
import com.mualab.org.user.model.booking.BookingStaff;

import java.util.ArrayList;


public class BookingFragment1 extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private ArrayList<BookingStaff>staffList;
    private BookingSelectStaffAdapter staffAdapter;
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;


    public BookingFragment1() {
        // Required empty public constructor
    }

    public static BookingFragment1 newInstance(String param1, String param2) {
        BookingFragment1 fragment = new BookingFragment1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
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
        staffList = new ArrayList<>();
        staffAdapter = new BookingSelectStaffAdapter(mContext,staffList);
        staffList.clear();
        addStaff();
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        RecyclerView rvBookingSelectStaff = rootView.findViewById(R.id.rvBookingSelectStaff);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvBookingSelectStaff.setLayoutManager(layoutManager);
        rvBookingSelectStaff.setAdapter(staffAdapter);
    }

    private void addStaff(){
        BookingStaff staff;
        for (int i = 0; i<10;i++){
            staff = new BookingStaff();
            staff.userName = "Jamie Bloggs";
            staff.spaciality = "Hair Expert";
            staff.profileImage = "http://koobi.co.uk/uploads/profile/1517824082281.jpg";
            staffList.add(staff);
        }
        staffAdapter.notifyDataSetChanged();
    }
}
