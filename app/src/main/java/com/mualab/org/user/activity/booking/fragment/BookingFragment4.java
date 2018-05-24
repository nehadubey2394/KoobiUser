package com.mualab.org.user.activity.booking.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.adapter.BookingInfoAdapter;
import com.mualab.org.user.activity.booking.adapter.TimeSlotAdapter;
import com.mualab.org.user.activity.booking.background_service.ExpiredBookingJobService;
import com.mualab.org.user.activity.booking.listner.DeleteServiceListener;
import com.mualab.org.user.activity.booking.listner.TimeSlotClickListener;
import com.mualab.org.user.activity.booking.listner.HideFilterListener;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.booking.StaffInfo;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.BookingInfo;
import com.mualab.org.user.data.model.booking.BookingServices3;
import com.mualab.org.user.data.model.booking.BookingTimeSlot;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import views.calender.data.CalendarAdapter;
import views.calender.data.Day;
import views.calender.widget.MyFlexibleCalendar;


public class BookingFragment4 extends Fragment implements View.OnClickListener,TimeSlotClickListener,DeleteServiceListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1,artistId,selectedDate,sMonth= "",sDay,currentTime,lat="",lng="";
    private ArrayList<BookingTimeSlot> bookingTimeSlots;
    private TimeSlotAdapter listAdapter;
    private BookingInfoAdapter bookingInfoAdapter;
    private int dayId;
    private RecyclerView rycTimeSlot;
    private TextView tvNoSlot;
    private BookingInfo bookingInfo;
    public static ArrayList<BookingInfo> arrayListbookingInfo = new ArrayList<>();
    private SimpleDateFormat input,dateFormat;
    private boolean alreadyAddedFound = false,isEdit = false,isRemoved = false;
    private MyFlexibleCalendar viewCalendar;
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
        //  args.putSerializable("param4", item);
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

        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setReviewPostVisibility(0);
            ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
        }

        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
            isEdit = getArguments().getBoolean("param2")  ;
            bookingInfo = (BookingInfo) getArguments().getSerializable("param3");
            // StaffInfo staffInfo = (StaffInfo) getArguments().getSerializable("param4");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_booking4, container, false);
        initView();
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        //if(context instanceof BookingActivity)
    }

    private void initView(){
        bookingTimeSlots = new ArrayList<>();
        artistId = bookingInfo.artistId;
        listAdapter = new TimeSlotAdapter(mContext, bookingTimeSlots);
        listAdapter.setCustomListener(BookingFragment4.this);
        bookingInfoAdapter = new BookingInfoAdapter(mContext, arrayListbookingInfo);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView(rootView);
    }

    private void setView(View rootView){
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setTitleVisibility(getString(R.string.title_booking));
        }

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
        bookingInfoAdapter.setCustomListener(BookingFragment4.this);


        viewCalendar =  rootView.findViewById(R.id.calendar);

        // init calendar
        Calendar cal = Calendar.getInstance();
        CalendarAdapter adapter = new CalendarAdapter(mContext, cal);
        viewCalendar.setAdapter(adapter);
        dateFormat = new SimpleDateFormat("EEE, d MMMM yyyy");
        dateFormat.setTimeZone(cal.getTimeZone());
        input = new SimpleDateFormat("yyyy-MM-dd");

        selectedDate = getCurrentDate();
        currentTime = getCurrentTime();

        bookingInfo.selectedDate = selectedDate;

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
                    break;
                }
            }
            if (!isEdit && alreadyAddedFound)
                MyToast.getInstance(mContext).showDasuAlert("This service is already added,Select another service");

            if (!isMatch){
                alreadyAddedFound = false;
                arrayListbookingInfo.add(bookingInfo);
            }else if (isEdit){
                arrayListbookingInfo.remove(bookingInfo);
                arrayListbookingInfo.add(0,bookingInfo);
            }
        }else{
            alreadyAddedFound = false;
            arrayListbookingInfo.add(bookingInfo);
        }

        if (isEdit){
            bookingInfo.date = "Select date";
            bookingInfo.time = "and time";
            bookingInfo.endTime = bookingInfo.editEndTime;
        }

        if (!alreadyAddedFound )
            Collections.reverse(arrayListbookingInfo);

        bookingInfoAdapter.notifyDataSetChanged();

        // bind events of calendar
        setCalenderClickListner();

        if (Mualab.currentLocationForBooking!=null){
            lat = String.valueOf(Mualab.currentLocationForBooking.lat);
            lng = String.valueOf(Mualab.currentLocationForBooking.lng);
        }


        if (arrayListbookingInfo.size()>1) {
            showSlotAccordingToSmallestDate();
        }else
            apiForGetSlots();

        btnCOnfirmBooking.setOnClickListener(this);
        btnAddMoreService.setOnClickListener(this);
        btnToday.setOnClickListener(this);
    }

    private void showSlotAccordingToSmallestDate(){
        ArrayList<BookingInfo> tempArrayList = new ArrayList<>();
        tempArrayList.addAll(arrayListbookingInfo);

        Collections.sort(tempArrayList, new Comparator<BookingInfo>() {
            public int compare(BookingInfo o1, BookingInfo o2) {
                return o1.selectedDate.compareTo(o2.selectedDate);
            }
        });

        if (!tempArrayList.get(1).date.equals("Select date")){
            String smallestDate = tempArrayList.get(1).selectedDate;

            if (smallestDate.contains("-")){

                SimpleDateFormat input,output;
                output = new SimpleDateFormat("y-M-d");
                input = new SimpleDateFormat("yyyy-MM-dd");

                try {
                    Date  formatedDate = input.parse(smallestDate);  // parse input
                    smallestDate =  output.format(formatedDate);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int year,month,day;
                String[] separated = smallestDate.split("-");
                year = Integer.parseInt(separated[0]);
                month = Integer.parseInt(separated[1]);
                day = Integer.parseInt(separated[2]);

                viewCalendar.select(new Day(year, month-1, day));
                viewCalendar.expand(500);
            }
        }

        // use methods
      /*  viewCalendar.addEventTag(2018, 8, 10);
        viewCalendar.addEventTag(2018, 8, 14);
        viewCalendar.addEventTag(2018, 8, 23);
*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCOnfirmBooking:
                if (arrayListbookingInfo.size()==0){
                    MyToast.getInstance(mContext).showDasuAlert("No service added!");
                    return;
                }
                if (arrayListbookingInfo.size()!=0 && isRemoved){
                    ((BookingActivity) mContext).addFragment(
                            BookingFragment5.newInstance(bookingInfo), true, R.id.flBookingContainer);
                    return;
                }
                if (bookingTimeSlots.size() != 0 || bookingTimeSlots.size() != 0 || (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time"))){

                    if (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time") && !bookingInfo.time.equals("12:00 AM")) {
                        apiForContinueBooking(false);
                    } else {
                        MyToast.getInstance(mContext).showDasuAlert("Please select service date and time");
                    }
                }else
                    MyToast.getInstance(mContext).showDasuAlert("There is no time slot available, select another date for booking");

                break;

            case R.id.btnToday:
             /*   MyFlexibleCalendar viewCalendar =  rootView.findViewById(R.id.calendar);
                viewCalendar.isFirstimeLoad = true;
                Calendar cal = Calendar.getInstance();
                CalendarAdapter adapter = new CalendarAdapter(mContext, cal);
                viewCalendar.setAdapter(adapter);
                viewCalendar.expand(500);
                setCalenderClickListner(viewCalendar);*/

                selectedDate = getCurrentDate();
                bookingInfo.selectedDate = selectedDate;
                viewCalendar.isFirstimeLoad = true;
                if (selectedDate.contains("-")){
                    int year,month,day;
                    String[] separated = selectedDate.split("-");
                    year = Integer.parseInt(separated[0]);
                    month = Integer.parseInt(separated[1]);
                    day = Integer.parseInt(separated[2]);

                    viewCalendar.select(new Day(year, month, day));
                    viewCalendar.expand(500);
                    selectedDate = year+"-"+month+"-"+day;
                }
                //     apiForGetSlots();
                break;

            case R.id.btnAddMoreService:
                if (arrayListbookingInfo.size()!=0 && isRemoved){
                    clearBackStack();
                    return;
                }

                if (arrayListbookingInfo.size()!=0){
                    if (bookingTimeSlots.size() != 0 || (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time"))){
                        if (!bookingInfo.date.equals("Select date") && !bookingInfo.time.equals("and time") && !bookingInfo.time.equals("12:00 AM")) {
                            apiForContinueBooking(true);
                        } else {
                            MyToast.getInstance(getActivity()).showDasuAlert("Please select service date and time");
                        }
                    } else
                        MyToast.getInstance(mContext).showDasuAlert("There is no time slot available, select another date for booking");

                }else {
                    clearBackStack();
                }

                break;
        }
    }

    private void setCalenderClickListner(){
        viewCalendar.setCalendarListener(new MyFlexibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {
                Day day = viewCalendar.getSelectedDay();
                viewCalendar.isFirstimeLoad = false;
                Log.i(getClass().getName(), "Selected Day: "
                        + day.getYear() + "/" + (day.getMonth() + 1) + "/" + day.getDay());

                Date date = new Date(day.getYear(), day.getMonth(), day.getDay()-1);
                dayId = date.getDay()-1;

                if (dayId==-1){
                    dayId = 6;
                }

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
                                if (!isRemoved){
                                    bookingInfo.date = "Select date";
                                    bookingInfo.time = "and time";
                                }
                                bookingInfo.endTime = bookingInfo.editEndTime;
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

        if (!alreadyAddedFound || isEdit){
            isRemoved = false;
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

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateAndTimeFormate = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

                    String newDateTime = bookingInfo.selectedDate+""+" "+bookingInfo.time;

                    bookingInfo.dateTime = dateAndTimeFormate.parse(newDateTime);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            listAdapter.notifyItemChanged(position);

            if (!bookingInfo.date.equals("") && !bookingInfo.date.equals("Select date"))
                bookingInfoAdapter.notifyDataSetChanged();
        }else {
            MyToast.getInstance(mContext).showDasuAlert("This service is already added,Select another service");
        }
    }

    @Override
    public void onRemoveClick(int position) {
        BookingInfo info = arrayListbookingInfo.get(position);
        isRemoved = true;
        if (info.bookingId!=null && !info.bookingId.equals(""))
            apiForDeleteBookedService(info);
        else {
            for (int i=0; i<info.subServices.artistservices.size();i++){
                BookingServices3 bookingServices = info.subServices.artistservices.get(i);
                if (bookingServices._id.equals(info.msId)){
                    bookingServices.setBooked(false);
                    break;
                }
            }
            arrayListbookingInfo.remove(info);
            bookingInfoAdapter.notifyDataSetChanged();
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
        params.put("currentTime", currentTime);
        params.put("serviceTime", bookingInfo.serviceTime);
        params.put("staffId", bookingInfo.staffId);

        if (isEdit) {
            params.put("type", "edit");
            params.put("bookingId",bookingInfo.bookingId);
        }
        else {
            params.put("type", "");
            params.put("bookingId","");
        }
        params.put("bookingTime", "");
        params.put("bookingDate", "");
        params.put("bookingCount", "");
        params.put("latitude", lat);
        params.put("longitude", lng);

        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "artistTimeSlotNew", new HttpResponceListner.Listener() {
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
        params.put("price", String.valueOf(bookingInfo.price));

        if (isEdit) {
            params.put("type", "edit");
            params.put("bookingId",bookingInfo.bookingId);
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

                    if (js.has("bookingId"))
                        bookingInfo.bookingId = js.getString("bookingId");

                    if (status.equalsIgnoreCase("success")) {
                        mContext.startService(new Intent(mContext, ExpiredBookingJobService.class));

                        ((BookingActivity)mContext).startTimer();

                        if (js.has("bookingId"))
                            bookingInfo.bookingId = js.getString("bookingId");

                        if (isAddMore) {
                            clearBackStack();
                        }
                        else {
                            ((BookingActivity) mContext).addFragment(
                                    BookingFragment5.newInstance(bookingInfo), true, R.id.flBookingContainer);
                        }
                    }else {
                        if (message.equals("Service already added") && isAddMore){
                            clearBackStack();
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

    private void apiForDeleteBookedService(final BookingInfo info){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForDeleteBookedService(info);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        if (!info.bookingId.equals(""))
            params.put("bookingId", info.bookingId);
        // params.put("artistId", item._id);
        // params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "deleteBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        for (int i=0; i<info.subServices.artistservices.size();i++){
                            BookingServices3 bookingServices = info.subServices.artistservices.get(i);
                            if (bookingServices._id.equals(info.msId)){
                                bookingServices.setBooked(false);
                                break;
                            }

                        }
                        arrayListbookingInfo.remove(info);
                        bookingInfoAdapter.notifyDataSetChanged();

                     /*   if (BookingFragment4.arrayListbookingInfo.size()==0){
                            BookingFragment4.arrayListbookingInfo.clear();
                            ((BookingActivity)context).finish();
                        }else {
                            FragmentManager fm = ((BookingActivity)context).getSupportFragmentManager();
                            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                            BookingInfo bookingInfo = BookingFragment4.arrayListbookingInfo.get(0);
                            ((BookingActivity)context).addFragment(
                                    BookingFragment5.newInstance(bookingInfo), true, R.id.flBookingContainer);
                        }*/


                    }else {
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

        task.execute(this.getClass().getName());
    }

    private void clearBackStack() {
        FragmentManager fm = ((BookingActivity)mContext).getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
        Mualab.getInstance().cancelAllPendingRequests();
        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setReviewPostVisibility(8);
            ((BookingActivity) mContext).setTitleVisibility(mParam1);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setReviewPostVisibility(8);
            ((BookingActivity) mContext).setTitleVisibility(mParam1);
        }
        super.onDestroy();

    }
}