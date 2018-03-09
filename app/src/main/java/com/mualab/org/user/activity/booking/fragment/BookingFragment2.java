package com.mualab.org.user.activity.booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.ServiceExpandListAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.booking.BookingServices3;
import com.mualab.org.user.model.booking.Services;
import com.mualab.org.user.model.booking.SubServices;
import com.mualab.org.user.session.Session;

import java.util.ArrayList;


public class BookingFragment2 extends Fragment implements View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ServiceExpandListAdapter expandableListAdapter;
    private ArrayList<Services>services,tempArrayList;
    private ImageView ivOutcall;
    boolean isOutCallSelect = false;
    private ArtistsSearchBoard item;
    private Session session;
    private  ExpandableListView lvExpandable;
    private TextView tvNoData;

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
        session = Mualab.getInstance().getSessionManager();
        tempArrayList = new ArrayList<>();
        services =  item.allService;
        tempArrayList.addAll(services);
        expandableListAdapter = new ServiceExpandListAdapter(mContext, services);
        // services.clear();
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));
        ivOutcall = rootView.findViewById(R.id.ivOutcall);
        RelativeLayout lyOutcall = rootView.findViewById(R.id.lyOutcall);

        if (session.getIsOutCallFilter()){
            isOutCallSelect = true;
            isOutCallSelect = true;
            ivOutcall.setImageResource(R.drawable.active_check_box);
        }else
            ivOutcall.setImageResource(R.drawable.inactive_checkbox);

        lyOutcall.setOnClickListener(this);

        tvNoData = rootView.findViewById(R.id.tvNoData);
        lvExpandable = rootView.findViewById(R.id.lvExpandable);

        if (item.serviceType.equals("2") && !session.getIsOutCallFilter()){
            lvExpandable.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        }else if (session.getIsOutCallFilter()){
            OutCallFilter();
        }else if (item.serviceType.equals("3")){
            InCallFilter();
        }

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
                onChildClickListener(expandableListView,view,groupPosition,childPosition,l);
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.lyOutcall :
                if (!isOutCallSelect){
                    isOutCallSelect = true;
                    OutCallFilter();
                    ivOutcall.setImageResource(R.drawable.active_check_box);
                }else {
                    isOutCallSelect = false;
                    InCallFilter();
                    ivOutcall.setImageResource(R.drawable.inactive_checkbox);
                }

                break;
        }
    }

    private void onChildClickListener(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l){
        Services servicesItem = services.get(groupPosition);
        SubServices subServices = servicesItem.arrayList.get(childPosition);
        ((BookingActivity)mContext).addFragment(
                BookingFragment3.newInstance(subServices.subServiceName,subServices,item,isOutCallSelect), true, R.id.flBookingContainer);
    }

    public void OutCallFilter() {
        services.clear();
        ArrayList<Services>tempArrayList2 = new ArrayList<>();
        tempArrayList2.addAll(tempArrayList);
        // tempArrayList.clear();
        if (isOutCallSelect){
            if (tempArrayList2.size()!=0){
                double outCallPrice = 0.0;

                for (int i = 0; i < tempArrayList2.size(); i++) {
                    Services mServices = tempArrayList2.get(i);
                    Services newService = new Services();
                    ArrayList<BookingServices3>services3ArrayList = new ArrayList<>();
                    ArrayList<SubServices> subServicesArrayList = new ArrayList<>();

                    for (int j = 0; j < mServices.arrayList.size(); j++) {
                        SubServices subServices = mServices.arrayList.get(j);
                        SubServices newSubServise = new SubServices();
                        subServicesArrayList.clear();

                        for (int k = 0; k < subServices.artistservices.size(); k++) {
                            BookingServices3 services3 = subServices.artistservices.get(k);
                            BookingServices3 newServices3 = new BookingServices3();
                            services3ArrayList.clear();

                            if (!services3.outCallPrice.equals("null")) {
                                outCallPrice = Double.parseDouble(services3.outCallPrice);
                                if (outCallPrice > 0) {
                                    newServices3.outCallPrice = services3.outCallPrice;
                                    newServices3._id = services3._id;
                                    newServices3.title = services3.title;
                                    newServices3.completionTime = services3.completionTime;
                                    newServices3.inCallPrice = services3.inCallPrice;
                                    newServices3.isOutCall = true;
                                    services3ArrayList.add(newServices3);
                                    newSubServise.artistservices.add(newServices3);
                                }
                            }

                        }

                        if (outCallPrice > 0 && newSubServise.artistservices.size()!=0) {
                            newSubServise._id = subServices._id;
                            newSubServise.isOutCall = true;
                            newSubServise.serviceId = subServices.serviceId;
                            newSubServise.subServiceId = subServices.subServiceId;
                            newSubServise.subServiceName = subServices.subServiceName;
                            subServicesArrayList.add(newSubServise);
                            newService.arrayList.add(newSubServise);
                        }
                    }
                    if (outCallPrice > 0 && newService.arrayList.size()!=0) {
                        newService.isOutCall = true;
                        newService.serviceName = mServices.serviceName;
                        newService.serviceId = mServices.serviceId;

                        services.add(newService);
                    }
                }
              /*  if (services.size() == 0) {
                    lvExpandable.setVisibility(View.GONE);
                    tvNoData.setVisibility(View.VISIBLE);
                } else {
                    expandableListAdapter.notifyDataSetChanged();
                    lvExpandable.setVisibility(View.VISIBLE);
                    tvNoData.setVisibility(View.GONE);
                }*/
            }
        }
        if (services.size()==0){
            lvExpandable.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        }else {
            expandableListAdapter.notifyDataSetChanged();
            lvExpandable.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
        }
    }

    public void InCallFilter() {
        services.clear();
        ArrayList<Services> tempArrayList2 = new ArrayList<>();
        tempArrayList2.addAll(tempArrayList);

        if (tempArrayList2.size() != 0) {
            double inCallPrice = 0.0;

            for (int i = 0; i < tempArrayList2.size(); i++) {
                Services mServices = tempArrayList2.get(i);
                Services newService = new Services();
                ArrayList<BookingServices3>services3ArrayList = new ArrayList<>();
                ArrayList<SubServices> subServicesArrayList = new ArrayList<>();

                for (int j = 0; j < mServices.arrayList.size(); j++) {
                    SubServices subServices = mServices.arrayList.get(j);
                    SubServices newSubServise = new SubServices();
                    subServicesArrayList.clear();

                    for (int k = 0; k < subServices.artistservices.size(); k++) {
                        BookingServices3 services3 = subServices.artistservices.get(k);
                        BookingServices3 newServices3 = new BookingServices3();
                        services3ArrayList.clear();

                        if (!services3.inCallPrice.equals("null")) {
                            inCallPrice = Double.parseDouble(services3.inCallPrice);
                            if (inCallPrice > 0) {
                                newServices3.outCallPrice = services3.outCallPrice;
                                newServices3._id = services3._id;
                                newServices3.title = services3.title;
                                newServices3.completionTime = services3.completionTime;
                                newServices3.inCallPrice = services3.inCallPrice;
                                newServices3.isOutCall = true;
                                services3ArrayList.add(newServices3);
                                newSubServise.artistservices.add(newServices3);
                            }
                        }

                    }

                    if (inCallPrice > 0 && services3ArrayList.size()!=0) {
                        newSubServise._id = subServices._id;
                        newSubServise.isOutCall = true;
                        newSubServise.serviceId = subServices.serviceId;
                        newSubServise.subServiceId = subServices.subServiceId;
                        newSubServise.subServiceName = subServices.subServiceName;
                        subServicesArrayList.add(newSubServise);
                        newService.arrayList.add(newSubServise);
                    }
                }
                if (inCallPrice > 0 && subServicesArrayList.size()!=0) {
                    newService.isOutCall = true;
                    newService.serviceName = mServices.serviceName;
                    newService.serviceId = mServices.serviceId;

                    services.add(newService);
                }
            }
            if (services.size() == 0) {
                lvExpandable.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
            } else {
                expandableListAdapter.notifyDataSetChanged();
                lvExpandable.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
            }
        }
    }
}
