package com.mualab.org.user.activity.make_booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.make_booking.BookingActivity;
import com.mualab.org.user.activity.make_booking.adapter.BookingSelectStaffAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.booking.BookingInfo;
import com.mualab.org.user.data.model.booking.StaffInfo;

import java.util.ArrayList;
import java.util.List;


public class BookingFragment1 extends Fragment {
    private BookingSelectStaffAdapter staffAdapter;
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ArtistsSearchBoard item;
    private BookingInfo bookingInfo;
    private boolean isEdit;
    private List<StaffInfo> staffList;

    public BookingFragment1() {
        // Required empty public constructor
    }

    public static BookingFragment1 newInstance(String param1, ArtistsSearchBoard  item,BookingInfo bookingInfo,boolean isEdit) {
        BookingFragment1 fragment = new BookingFragment1();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putSerializable("param2", item);
        args.putSerializable("param3", bookingInfo);
        args.putBoolean("param4", isEdit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
            item = (ArtistsSearchBoard) getArguments().getSerializable("param2")  ;
            bookingInfo = (BookingInfo) getArguments().getSerializable("param3");
            isEdit = getArguments().getBoolean("param4");
            if(mContext instanceof BookingActivity) {
                ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
            }
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
        staffList = item.findArtistByServiceId(Integer.parseInt(bookingInfo.msId));

        //  staffAdapter = new BookingSelectStaffAdapter(mContext, staffList,bookingInfo,isEdit);
        StaffInfo staffInfo = new StaffInfo();
        staffInfo.staffImage = item.profileImage;
        staffInfo.staffName = item.userName;
        staffInfo.job = "";
        staffInfo.mediaAccess = "";
        staffInfo.holiday = "";
        staffInfo.staffId = bookingInfo.artistId;
        staffList.add(staffInfo);

        staffAdapter = new BookingSelectStaffAdapter(mContext, staffList,bookingInfo,isEdit);

    }

    private void setViewId(View rootView){
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setTitleVisibility(getString(R.string.title_booking));
        }

        TextView tvNoData = rootView.findViewById(R.id.tvNoData);
        RecyclerView rvBookingSelectStaff = rootView.findViewById(R.id.rvBookingSelectStaff);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvBookingSelectStaff.setLayoutManager(layoutManager);
        rvBookingSelectStaff.setAdapter(staffAdapter);

        if (staffList.size()==0){
            rvBookingSelectStaff.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        }else {
            rvBookingSelectStaff.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setTitleVisibility(mParam1);

                ((BookingActivity) mContext).setTitleVisibility(mParam1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setTitleVisibility(mParam1);
            Mualab.getInstance().cancelAllPendingRequests();
        }
    }
}