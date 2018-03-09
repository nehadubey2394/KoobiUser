package com.mualab.org.user.activity.booking.fragment;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.BookingInfoAdapter;
import com.mualab.org.user.activity.booking.adapter.TimeSlotAdapter;
import com.mualab.org.user.activity.booking.listner.CustomAdapterButtonListener;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.model.booking.BookingTimeSlot;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class BookingFragment4 extends Fragment implements View.OnClickListener,CustomAdapterButtonListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1,artistId,selectedDate,sMonth= "",sDay;
    private ArrayList<BookingTimeSlot> bookingTimeSlots;
    private ArrayList<BookingInfo>bookingInfos;
    private TimeSlotAdapter listAdapter;
    private BookingInfoAdapter bookingInfoAdapter;
    private int dayId;

    public BookingFragment4() {
        // Required empty public constructor
    }

    public static BookingFragment4 newInstance(String param1, String mParam2) {
        BookingFragment4 fragment = new BookingFragment4();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", mParam2);
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
            artistId = getArguments().getString("param2")  ;
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
        listAdapter.setCustomListener(BookingFragment4.this);
        bookingInfoAdapter = new BookingInfoAdapter(mContext, bookingInfos);

        addServices();
    }

    private void setViewId(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));

        AppCompatButton btnCOnfirmBooking = rootView.findViewById(R.id.btnCOnfirmBooking);
        AppCompatButton btnAddMoreService = rootView.findViewById(R.id.btnAddMoreService);

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

        selectedDate = getCurrentDate();
        dayId = cal.get(GregorianCalendar.DAY_OF_WEEK)-2;

        // bind events of calendar
        viewCalendar.setCalendarListener(new FlexibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = viewCalendar.getSelectedDay();
                Log.i(getClass().getName(), "Selected Day: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());

                Date date = new Date(day.getYear(), day.getMonth(), day.getDay()-1);
                dayId = date.getDay()-1;

                int month = day.getMonth()+1;

                if (month < 10){
                    sMonth = "0"+month;
                }else {
                    sMonth = String.valueOf(month);
                }

                if (day.getDay()<10){
                    sDay = "0"+day.getDay();
                }else {
                    sDay = String.valueOf(day.getDay());
                }

                selectedDate = day.getYear()+"-"+sMonth+"-"+sDay;
                apiForGetSlots();
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

        apiForGetSlots();

        // use methods
      /*  viewCalendar.addEventTag(2018, 8, 10);
        viewCalendar.addEventTag(2018, 8, 14);
        viewCalendar.addEventTag(2018, 8, 23);
*/
        // viewCalendar.select(new Day(2018, 4, 22));

        btnCOnfirmBooking.setOnClickListener(this);
        btnAddMoreService.setOnClickListener(this);
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

    private void apiForGetSlots(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetSlots();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("day", String.valueOf(dayId));
        params.put("date", selectedDate);
        params.put("currentTime", getCurrentTime());
        params.put("serviceTime", "");

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "artistTimeSlot", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        bookingTimeSlots.clear();

                        JSONArray jsonArray = js.getJSONArray("timeSlots");
                        if (jsonArray!=null) {
                            for (int j=0; j<jsonArray.length(); j++){
                                BookingTimeSlot item = new BookingTimeSlot();
                                item.time = jsonArray.getString(j);
                                item.isSelected = "0";
                                bookingTimeSlots.add(item);
                            }
                        }else {
                            MyToast.getInstance(mContext).showSmallCustomToast(message);
                        }

                        listAdapter.notifyDataSetChanged();
                    }else {
                        MyToast.getInstance(mContext).showSmallCustomToast(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(mContext);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        //      MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private String getCurrentTime(){
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("hh:mm a");
        System.out.println("currentTime"+date.format(currentLocalTime));
        return date.format(currentLocalTime);
    }

    private String getCurrentDate(){
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        if (month < 10){
            sMonth = "0"+month;
        }else {
            sMonth = String.valueOf(month);
        }

        if (day<10){
            sDay = "0"+day;
        }else {
            sDay = String.valueOf(day);
        }
        return year+"-"+sMonth+"-"+sDay;
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

            case R.id.btnAddMoreService:

                FragmentManager fm = getActivity().getSupportFragmentManager();
                int count = fm.getBackStackEntryCount();
                for(int i = 0; i < count; ++i) {
                    if (i>0)
                        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
              /*  ((BookingActivity)mContext).addFragment(
                        BookingFragment5.newInstance(""), true, R.id.flBookingContainer);*/

                break;
        }
    }

    @Override
    public void onButtonClick(int position, String buttonText, int selectedCount) {
        BookingTimeSlot item =  bookingTimeSlots.get(position);

        for (int i = 0;i<bookingTimeSlots.size();i++){
            BookingTimeSlot timeSlot = bookingTimeSlots.get(i);
            timeSlot.isSelected = "0";
        }
        listAdapter.notifyDataSetChanged();
        // if (item.isSelected.equals("0"))
        item.isSelected = "1";
        //  else
        //     item.isSelected = "0";
        listAdapter.notifyItemChanged(position);
    }
}
