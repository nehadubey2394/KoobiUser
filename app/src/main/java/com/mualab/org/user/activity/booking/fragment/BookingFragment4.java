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
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.internal.Utility;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.BookingInfoAdapter;
import com.mualab.org.user.activity.booking.adapter.TimeSlotAdapter;
import com.mualab.org.user.activity.booking.listner.CustomAdapterButtonListener;
import com.mualab.org.user.activity.booking.listner.HideFilterListener;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import views.calender.data.CalendarAdapter;
import views.calender.data.Day;
import views.calender.widget.MyFlexibleCalendar;


public class BookingFragment4 extends Fragment implements View.OnClickListener,CustomAdapterButtonListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1,artistId,selectedDate,sMonth= "",sDay,currentTime;
    private ArrayList<BookingTimeSlot> bookingTimeSlots;
    private TimeSlotAdapter listAdapter;
    private BookingInfoAdapter bookingInfoAdapter;
    private int dayId;
    private RecyclerView rycTimeSlot;
    private TextView tvNoSlot;
    private BookingInfo bookingInfo;
    public static ArrayList<BookingInfo> arrayListbookingInfo = new ArrayList<>();
    private SimpleDateFormat input,dateFormat;
    private boolean alreadyAddedFound = false,isEdit = false;
    //private MyFlexibleCalendar viewCalendar;
    private  View rootView;

    public BookingFragment4() {
        // Required empty public constructor
    }

    public static BookingFragment4 newInstance(String param1,boolean isEdit,BookingInfo bookingInfo) {
        BookingFragment4 fragment = new BookingFragment4();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putBoolean("param2", isEdit);
        args.putSerializable("param3", bookingInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideFilterListener listener = null;
        if(mContext instanceof HideFilterListener)
            listener = (HideFilterListener) mContext;

        if(listener!=null)
            listener.onServiceAdded(true);

        BookingActivity.lyReviewPost.setVisibility(View.VISIBLE);
        BookingActivity.lyArtistDetail.setVisibility(View.VISIBLE);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
            isEdit = getArguments().getBoolean("param2")  ;
            bookingInfo = (BookingInfo) getArguments().getSerializable("param3");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_booking4, container, false);
        initView();
        setView(rootView);
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
        artistId = bookingInfo.artistId;
        listAdapter = new TimeSlotAdapter(mContext, bookingTimeSlots);
        listAdapter.setCustomListener(BookingFragment4.this);
        bookingInfoAdapter = new BookingInfoAdapter(mContext, arrayListbookingInfo);
    }

    private void setView(View rootView){
        BookingActivity.title_booking.setText(getString(R.string.title_booking));

        AppCompatButton btnCOnfirmBooking = rootView.findViewById(R.id.btnCOnfirmBooking);
        AppCompatButton btnAddMoreService = rootView.findViewById(R.id.btnAddMoreService);
        AppCompatButton btnToday = rootView.findViewById(R.id.btnToday);

        rycTimeSlot = rootView.findViewById(R.id.rycTimeSlot);
        tvNoSlot = rootView.findViewById(R.id.tvNoSlot);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL, false);
        rycTimeSlot.setLayoutManager(layoutManager);
        rycTimeSlot.setAdapter(listAdapter);

        RecyclerView rycBookingInfo = rootView.findViewById(R.id.rycBookingInfo);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(mContext);
        rycBookingInfo.setLayoutManager(layoutManager2);
        rycBookingInfo.setNestedScrollingEnabled(false);
        rycBookingInfo.setAdapter(bookingInfoAdapter);

        MyFlexibleCalendar viewCalendar =  rootView.findViewById(R.id.calendar);

        // init calendar
        Calendar cal = Calendar.getInstance();
        CalendarAdapter adapter = new CalendarAdapter(mContext, cal);
        viewCalendar.setAdapter(adapter);

        dateFormat = new SimpleDateFormat("EEE, d MMMM yyyy");
        dateFormat.setTimeZone(cal.getTimeZone());
        input = new SimpleDateFormat("yyyy-MM-dd");

        selectedDate = getCurrentDate();
        bookingInfo.selectedDate = selectedDate;
        currentTime = getCurrentTime();
        dayId = cal.get(GregorianCalendar.DAY_OF_WEEK)-2;

        bookingInfo.date = "Select date";
        bookingInfo.time = "and time";

        if (arrayListbookingInfo.size()!=0){
            boolean isMatch=false;
            for (BookingInfo info : arrayListbookingInfo) {
                if (info.msId.equals(bookingInfo.msId)) {
                    isMatch=true;
                    bookingInfo = info;
                    alreadyAddedFound = true;
                    MyToast.getInstance(mContext).showDasuAlert("This service is already added,Select another service");
                    break;
                }
            }
            if (!isMatch){
                alreadyAddedFound = false;
                arrayListbookingInfo.add(bookingInfo);
            }
        }else{
            alreadyAddedFound = false;
            arrayListbookingInfo.add(bookingInfo);
        }
        if (!alreadyAddedFound)
            Collections.reverse(arrayListbookingInfo);

        bookingInfoAdapter.notifyDataSetChanged();

        // bind events of calendar
        setCalenderClickListner(viewCalendar);

        apiForGetSlots();

        // use methods
      /*  viewCalendar.addEventTag(2018, 8, 10);
        viewCalendar.addEventTag(2018, 8, 14);
        viewCalendar.addEventTag(2018, 8, 23);
*/
        // viewCalendar.select(new Day(2018, 4, 22));

        btnCOnfirmBooking.setOnClickListener(this);
        btnAddMoreService.setOnClickListener(this);
        btnToday.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCOnfirmBooking:
                if (bookingTimeSlots.size() != 0 || (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time"))){
                    if (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time") && !bookingInfo.time.equals("12:00 AM")) {
                        apiForContinueBooking(false);
                    } else {
                        MyToast.getInstance(mContext).showDasuAlert("Please select service date and time");
                    }
                } else
                    MyToast.getInstance(mContext).showDasuAlert("There is no time slot available, select another date for booking");
                break;

            case R.id.btnToday:
                MyFlexibleCalendar viewCalendar =  rootView.findViewById(R.id.calendar);
                viewCalendar.isFirstimeLoad = true;
                Calendar cal = Calendar.getInstance();
                CalendarAdapter adapter = new CalendarAdapter(mContext, cal);
                viewCalendar.setAdapter(adapter);
                viewCalendar.expand(500);
                setCalenderClickListner(viewCalendar);

                selectedDate = getCurrentDate();
                bookingInfo.selectedDate = selectedDate;
                apiForGetSlots();
                break;

            case R.id.btnAddMoreService:

                if (bookingTimeSlots.size() != 0 || (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time"))){
                    if (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time") && !bookingInfo.time.equals("12:00 AM")) {
                        apiForContinueBooking(true);
                    } else {
                        MyToast.getInstance(mContext).showDasuAlert("Please select service date and time");
                    }
                } else
                    MyToast.getInstance(mContext).showDasuAlert("There is no time slot available, select another date for booking");

                break;
        }
    }

    private void setCalenderClickListner(final MyFlexibleCalendar viewCalendar){
        viewCalendar.setCalendarListener(new MyFlexibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = viewCalendar.getSelectedDay();
                viewCalendar.isFirstimeLoad = false;
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
                bookingInfo.selectedDate = selectedDate;

                if (viewCalendar.isSelectedDay(day)) {
                    Calendar todayCal = Calendar.getInstance();
                    int cYear  = todayCal.get(Calendar.YEAR);
                    int cMonth  = todayCal.get(Calendar.MONTH)+1;
                    int cDay  = todayCal.get(Calendar.DAY_OF_MONTH);

                    int year = day.getYear();
                    int dayOfMonth =  day.getDay();

                    if (year>=cYear && month>=cMonth){
                        if (year==cYear && month==cMonth && dayOfMonth<cDay){
                            Log.i("","can't select previous date");
                        }else {
                            if (!alreadyAddedFound || isEdit){
                                bookingInfo.date = "Select date";
                                bookingInfo.time = "and time";
                                bookingInfoAdapter.notifyDataSetChanged();
                            }
                            if (dayOfMonth==cDay){
                                currentTime = getCurrentTime();
                            }else
                                currentTime = "12:00 AM";

                            apiForGetSlots();
                        }
                    }
                }
            }

            @Override
            public void onItemClick(View v) {
                viewCalendar.isFirstimeLoad = false;
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
        params.put("currentTime", currentTime);
        params.put("serviceTime", bookingInfo.serviceTime);

        params.put("bookingTime", "");
        params.put("bookingDate", selectedDate);
        params.put("bookingId","");
        params.put("bookingCount", "");

        if (isEdit) {
            params.put("type", "edit");
        }
        else {
            params.put("type", "");
        }
        params.put("bookingTime", "");
        params.put("bookingDate", "");
        params.put("bookingId","");
        params.put("bookingCount", "");

        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "artistTimeSlot", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        rycTimeSlot.setVisibility(View.VISIBLE);
                        tvNoSlot.setVisibility(View.GONE);
                        bookingTimeSlots.clear();

                        JSONArray jsonArray = js.getJSONArray("timeSlots");
                        if (jsonArray!=null && jsonArray.length()!=0) {
                            for (int j=0; j<jsonArray.length(); j++){
                                BookingTimeSlot item = new BookingTimeSlot();
                                item.time = jsonArray.getString(j);
                                item.isSelected = "0";
                                bookingTimeSlots.add(item);
                            }
                        }else {
                            rycTimeSlot.setVisibility(View.GONE);
                            tvNoSlot.setVisibility(View.VISIBLE);
                            //  MyToast.getInstance(mContext).showSmallCustomToast(message);
                        }

                        listAdapter.notifyDataSetChanged();
                    }else {
                        rycTimeSlot.setVisibility(View.GONE);
                        tvNoSlot.setVisibility(View.VISIBLE);
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

    private void apiForContinueBooking(final boolean isAddMore){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForContinueBooking(isAddMore);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("staff", artistId);
        params.put("serviceId", bookingInfo.sId);
        params.put("subServiceId", bookingInfo.ssId);
        params.put("artistServiceId", bookingInfo.msId);
        params.put("serviceType", bookingInfo.serviceType);
        params.put("bookingDate", selectedDate);
        params.put("startTime", bookingInfo.time);
        params.put("endTime", bookingInfo.endTime);

        if (isEdit) {
            params.put("bookingId", bookingInfo.bookingId);
        }else
            params.put("bookingId", "");

        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "bookArtist", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        bookingInfo.bookingId = js.getString("bookingId");

                        if (isAddMore) {
                            FragmentManager fm = ((BookingActivity)mContext).getSupportFragmentManager();
                            int count = fm.getBackStackEntryCount();
                            for (int i = 0; i < count; ++i) {
                                if (i > 0)
                                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        }
                        else {
                            ((BookingActivity) mContext).addFragment(
                                    BookingFragment5.newInstance(bookingInfo), true, R.id.flBookingContainer);
                        }
                    }else {
                        if (message.equals("Service already added") && isAddMore){
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            int count = fm.getBackStackEntryCount();
                            for (int i = 0; i < count; ++i) {
                                if (i > 0)
                                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        }else  if (message.equals("Service already added") && !isAddMore){
                            ((BookingActivity) mContext).addFragment(
                                    BookingFragment5.newInstance(bookingInfo), true, R.id.flBookingContainer);
                        }
                        if (!message.equals("Service already added"))
                            MyToast.getInstance(mContext).showDasuAlert(message);
                    }
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

    @Override
    public void onButtonClick(int position, String buttonText, int selectedCount) {
        if (!alreadyAddedFound || isEdit){
            BookingTimeSlot item =  bookingTimeSlots.get(position);

            for (int i = 0;i<bookingTimeSlots.size();i++){
                BookingTimeSlot timeSlot = bookingTimeSlots.get(i);
                timeSlot.isSelected = "0";
            }

            listAdapter.notifyDataSetChanged();
            // if (item.isSelected.equals("0"))
            item.isSelected = "1";
            bookingInfo.time = item.time;

            if (!bookingInfo.endTime.contains(":") && !bookingInfo.endTime.contains("PM") && !bookingInfo.endTime.contains("AM")){
                int min2  = Integer.parseInt(bookingInfo.endTime);

                // int finalMin = minuts1+min2;

                SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                Date d;
                try {
                    d = df.parse(item.time);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    cal.add(Calendar.MINUTE, min2);
                    bookingInfo.endTime = df.format(cal.getTime());

                    Date  formatedDate = input.parse(selectedDate);  // parse input
                    bookingInfo.date =  dateFormat.format(formatedDate);
                    bookingInfo.selectedDate =  selectedDate;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            listAdapter.notifyItemChanged(position);

            if (!bookingInfo.date.equals("") && !bookingInfo.date.equals("Select date"))
                bookingInfoAdapter.notifyDataSetChanged();
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
}
