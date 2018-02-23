package com.mualab.org.user.activity.booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import views.calender.data.CalendarAdapter;
import views.calender.data.Day;
import views.calender.widget.FlexibleCalendar;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.BookingInfoAdapter;
import com.mualab.org.user.activity.booking.adapter.TimeSlotAdapter;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.model.booking.BookingTimeSlot;

import java.util.ArrayList;
import java.util.Calendar;


public class BookingFragment4 extends Fragment implements View.OnClickListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ArrayList<BookingTimeSlot> bookingTimeSlots;
    private ArrayList<BookingInfo>bookingInfos;
    private TimeSlotAdapter listAdapter;
    private BookingInfoAdapter bookingInfoAdapter;

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
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
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
        bookingTimeSlots = new ArrayList<>();
        bookingInfos = new ArrayList<>();
        listAdapter = new TimeSlotAdapter(mContext, bookingTimeSlots);
        bookingInfoAdapter = new BookingInfoAdapter(mContext, bookingInfos);
        addItems();
        addServices();
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));

        AppCompatButton btnCOnfirmBooking = rootView.findViewById(R.id.btnCOnfirmBooking);

        RecyclerView rycTimeSlot = rootView.findViewById(R.id.rycTimeSlot);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL, false);
        rycTimeSlot.setLayoutManager(layoutManager);
        rycTimeSlot.setAdapter(listAdapter);

        RecyclerView rycBookingInfo = rootView.findViewById(R.id.rycBookingInfo);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        rycBookingInfo.setLayoutManager(layoutManager2);
        rycBookingInfo.setNestedScrollingEnabled(false);
        rycBookingInfo.setAdapter(bookingInfoAdapter);

        final FlexibleCalendar viewCalendar =  rootView.findViewById(R.id.calendar);

        // init calendar
        Calendar cal = Calendar.getInstance();
        CalendarAdapter adapter = new CalendarAdapter(mContext, cal);
        viewCalendar.setAdapter(adapter);

        // bind events of calendar
        viewCalendar.setCalendarListener(new FlexibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = viewCalendar.getSelectedDay();
                Log.i(getClass().getName(), "Selected Day: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());
            }

            @Override
            public void onItemClick(View v) {
                Day day = viewCalendar.getSelectedDay();
                Log.i(getClass().getName(), "The Day of Clicked View: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());
            }

            @Override
            public void onDataUpdate() {
                Log.i(getClass().getName(), "Data Updated");
            }

            @Override
            public void onMonthChange() {
                Log.i(getClass().getName(), "Month Changed"
                        + ". Current Year: " + viewCalendar.getYear()
                        + ", Current Month: " + (viewCalendar.getMonth() + 1));
            }

            @Override
            public void onWeekChange(int position) {
                Log.i(getClass().getName(), "Week Changed"
                        + ". Current Year: " + viewCalendar.getYear()
                        + ", Current Month: " + (viewCalendar.getMonth() + 1)
                        + ", Current Week position of Month: " + position);
            }
        });

        // use methods
      /*  viewCalendar.addEventTag(2018, 8, 10);
        viewCalendar.addEventTag(2018, 8, 14);
        viewCalendar.addEventTag(2018, 8, 23);
*/
        // viewCalendar.select(new Day(2018, 4, 22));

        btnCOnfirmBooking.setOnClickListener(this);
    }

    private void addItems(){
        BookingTimeSlot item;
        for(int i=0;i<6;i++) {
            item = new BookingTimeSlot();
            switch (i) {
              /*  case 0:
                    item.itemName = "Subscription";
                    item.itemImg = R.drawable.inactive_subscribe_icon;

                    break;*/
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
            bookingTimeSlots.add(item);
        }
    }

    private void addServices(){
        bookingInfos.clear();
        BookingInfo item;
        for(int i=0;i<2;i++) {
            item = new BookingInfo();
            switch (i) {
                case 0:
                    item.time = "10:00";
                    item.date = "Sun 10th November 2018";
                    item.sServiceName = "Evening Make Up";
                    item.price = "£70";
                    break;
                case 1:
                    item.time = "11:00";
                    item.date = "Mon 12th November 2018";
                    item.sServiceName = "Hair Cut";
                    item.price = "£60";
                    break;

            }
            bookingInfos.add(item);
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCOnfirmBooking:
                ((BookingActivity)mContext).addFragment(
                        BookingFragment5.newInstance(""), true, R.id.flBookingContainer);

                break;
        }
    }
}
