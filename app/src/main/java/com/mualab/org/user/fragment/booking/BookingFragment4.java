package com.mualab.org.user.fragment.booking;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.adapter.booking.Booking3ServiceAdapter;
import com.mualab.org.user.model.booking.BookinServices3;

import java.util.ArrayList;


public class BookingFragment4 extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private ArrayList<BookinServices3>arrayList;
    private Booking3ServiceAdapter adapter;
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;


    public BookingFragment4() {
        // Required empty public constructor
    }

    public static BookingFragment4 newInstance(String param1, String param2) {
        BookingFragment4 fragment = new BookingFragment4();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking4, container, false);
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


    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BookingActivity.lyReviewPost.setVisibility(View.GONE);
        BookingActivity.title_booking.setText(mParam1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BookingActivity.lyReviewPost.setVisibility(View.GONE);
        BookingActivity.title_booking.setText(mParam1);
    }
}
