package com.mualab.org.user.activity.booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.ServiceExpandListAdapter;
import com.mualab.org.user.activity.searchBoard.adapter.RefineServiceExpandListAdapter;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.SearchBoard.RefineServices;
import com.mualab.org.user.model.SearchBoard.RefineSubServices;
import com.mualab.org.user.model.booking.Services;
import com.mualab.org.user.model.booking.SubServices;

import java.util.ArrayList;


public class BookingFragment2 extends Fragment implements View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ExpandableListView lvExpandable;
    private ServiceExpandListAdapter expandableListAdapter;
    private ArrayList<Services>services;
    private ImageView ivOutcall;
    boolean isOutCallSelect = false;
    private ArtistsSearchBoard item;

    public BookingFragment2() {
        // Required empty public constructor
    }


    public static BookingFragment2 newInstance(ArtistsSearchBoard item, String param2) {
        BookingFragment2 fragment = new BookingFragment2();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
        if (getArguments() != null) {
            item =  getArguments().getParcelable("param1");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking2, container, false);
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
        services =  item.allService;
        expandableListAdapter = new ServiceExpandListAdapter(mContext, services);
       // services.clear();
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        ivOutcall = rootView.findViewById(R.id.ivOutcall);
        ivOutcall.setOnClickListener(this);

        ExpandableListView lvExpandable = rootView.findViewById(R.id.lvExpandable);
        lvExpandable.setAdapter(expandableListAdapter);
       // expandableListAdapter.notifyDataSetChanged();
        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {

            }
        });

        lvExpandable.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Services item = services.get(groupPosition);
                item.isExpand = true;
            }
        });

        // Listview Group collasped listener
        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Services item = services.get(groupPosition);
                item.isExpand = false;

            }
        });


        lvExpandable.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return false;
            }
        });

        lvExpandable.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                Services servicesItem = services.get(groupPosition);
                SubServices subServices = servicesItem.arrayList.get(childPosition);
                ((BookingActivity)mContext).addFragment(
                        BookingFragment3.newInstance(subServices.subServiceName,subServices,item), true, R.id.flBookingContainer);
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivOutcall :
                if (!isOutCallSelect){
                    isOutCallSelect = true;
                    ivOutcall.setImageResource(R.drawable.active_check_box);
                }else {
                    isOutCallSelect = false;
                    ivOutcall.setImageResource(R.drawable.inactive_checkbox);
                }
                break;
        }
    }
}
