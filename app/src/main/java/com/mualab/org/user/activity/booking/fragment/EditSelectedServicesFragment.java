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
import com.mualab.org.user.activity.booking.adapter.Booking3ServiceAdapter;
import com.mualab.org.user.activity.booking.adapter.EditSelectedServiceAdapter;
import com.mualab.org.user.model.booking.BookinServices3;

import java.util.ArrayList;


public class EditSelectedServicesFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private ArrayList<BookinServices3>arrayList;
    private EditSelectedServiceAdapter adapter;
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;


    public EditSelectedServicesFragment() {
        // Required empty public constructor
    }

    public static EditSelectedServicesFragment newInstance(String param1, String param2) {
        EditSelectedServicesFragment fragment = new EditSelectedServicesFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BookingActivity.lyReviewPost.setVisibility(View.GONE);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        arrayList = new ArrayList<>();
        adapter = new EditSelectedServiceAdapter(mContext,arrayList,mParam1);
        arrayList.clear();
        addService();
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(mParam1);
        RecyclerView rvLastService = rootView.findViewById(R.id.rvLastService);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvLastService.setLayoutManager(layoutManager);
        rvLastService.setAdapter(adapter);
    }

    private void addService(){
        BookinServices3 services3;
        for (int i = 0; i<10;i++){
            services3 = new BookinServices3();
            services3.sName = "Zero Trim";
            services3.time = "10 min";
            services3.price = "250";
            arrayList.add(services3);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
    }
}
