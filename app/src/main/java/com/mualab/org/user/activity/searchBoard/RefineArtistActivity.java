package com.mualab.org.user.activity.searchBoard;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.MainActivity;
import com.mualab.org.user.activity.searchBoard.adapter.RefineServiceExpandListAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listner.DatePickerListener;
import com.mualab.org.user.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.SearchBoard.RefineServices;
import com.mualab.org.user.model.SearchBoard.RefineSubServices;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.DatePickerFragment;
import com.mualab.org.user.util.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static com.mualab.org.user.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;

public class RefineArtistActivity extends AppCompatActivity implements View.OnClickListener,DatePickerListener {
    private ExpandableListView lvExpandable;
    private boolean isServiceOpen = false;
    private ImageView ivPrice,ivDistance;
    private TextView tv_refine_dnt,tv_refine_loc;
    private RefineServiceExpandListAdapter expandableListAdapter;
    private ArrayList<RefineServices>services;
    private String mainServId = "",sortType ="",sortSearch ="",serviceType="",lat="",lng="",date_time="",format,time="",subServiceId = "",location="";
    private int mHour,mMinute,dayId;
    private RefineSearchBoard refineSearchBoard ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refine_artist);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            refineSearchBoard = (RefineSearchBoard) bundle.getSerializable("params");
        }
        initView();
        setViewId();
    }

    private void initView(){
        if (refineSearchBoard!=null){
            services = refineSearchBoard.refineServices;
            expandableListAdapter = new RefineServiceExpandListAdapter(RefineArtistActivity.this, services);
        }else {
            services = new ArrayList<>();
            expandableListAdapter = new RefineServiceExpandListAdapter(RefineArtistActivity.this, services);
        }
    }

    private void setViewId(){
        ImageView ivBack = findViewById(R.id.ivHeaderBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.titel_refine);
        ivBack.setVisibility(View.VISIBLE);

        ivPrice = findViewById(R.id.ivPrice);
        ivDistance = findViewById(R.id.ivDistance);

        tv_refine_dnt = findViewById(R.id.tv_refine_dnt);
        tv_refine_loc = findViewById(R.id.tv_refine_loc);
        CheckBox chbOutcall = findViewById(R.id.chbOutcall);

        AppCompatButton btnApply = findViewById(R.id.btnApply);
        AppCompatButton btnClear = findViewById(R.id.btnClear);

        chbOutcall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    serviceType = "1";
                }else {
                    serviceType = "";
                }
            }
        });

        lvExpandable = findViewById(R.id.lvService);
        RelativeLayout rlService = findViewById(R.id.rlService);
        RelativeLayout rlPrice = findViewById(R.id.rlPrice);
        RelativeLayout rlDistance = findViewById(R.id.rlDistance);
        RelativeLayout rlRefineLocation = findViewById(R.id.rlRefineLocation);
        RelativeLayout rlDnT = findViewById(R.id.rlDnT);

        RadioGroup rdgOrder =  findViewById(R.id.rdgOrder);
        final AppCompatRadioButton rbAscending =  findViewById(R.id.rbAscending);
        final RadioButton rbDescending =  findViewById(R.id.rbDescending);

        rdgOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (rbAscending.isChecked()) {
                    sortType = "";
                    rbDescending.setTextColor(getResources().getColor(R.color.text_color));
                    rbAscending.setTextColor(getResources().getColor(R.color.colorPrimary));

                }
                if (rbDescending.isChecked()){
                    rbAscending.setTextColor(getResources().getColor(R.color.text_color));
                    rbDescending.setTextColor(getResources().getColor(R.color.colorPrimary));
                    sortType = "1";
                }
            }
        });

        if (refineSearchBoard!=null){
            lat = refineSearchBoard.latitude;
            lng = refineSearchBoard.longitude;
            subServiceId = refineSearchBoard.subservice;
            mainServId = refineSearchBoard.service;
            serviceType = refineSearchBoard.serviceType;
            sortSearch = refineSearchBoard.sortSearch;
            sortType = refineSearchBoard.sortType;
            time = refineSearchBoard.time;
            date_time = refineSearchBoard.date;
            location = refineSearchBoard.location;
            dayId = Integer.parseInt(refineSearchBoard.day);

            if (!date_time.equals(""))
                tv_refine_dnt.setText(date_time+" " + ":" + time);
            tv_refine_loc.setText(location);

            if (serviceType.equals("price")) {
                ivPrice.setImageResource(R.drawable.active_price_ico);
                ivDistance.setImageResource(R.drawable.route_ico);
            }
            if (serviceType.equals("1")){
                chbOutcall.setChecked(true);
            }
            if (sortType.equals("1")){
                rbDescending.setChecked(true);
            }
        }else {
            apiForGetAllServices();
        }



        lvExpandable.setAdapter(expandableListAdapter);

        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {

            }
        });

        lvExpandable.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                RefineServices item = services.get(groupPosition);
                item.isExpand = true;
            }
        });

        // Listview Group collasped listener
        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                RefineServices item = services.get(groupPosition);
                item.isExpand = false;

            }
        });


        lvExpandable.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int groupPosition, long l) {
                RefineServices servicesItem = services.get(groupPosition);
                ArrayList<RefineSubServices> arrayList = servicesItem.getArrayList();

                if (servicesItem.isChecked.equals("0")){
                    servicesItem.isChecked = "1";
                    servicesItem.isSubItemChecked = false;
                    expandableListAdapter.notifyDataSetChanged();

                }else {
                    servicesItem.isChecked = "0";
                    servicesItem.isSubItemChecked = false;
                    expandableListAdapter.notifyDataSetChanged();

                  /*  if (arrayList.size()==0){
                        servicesItem.isChecked = "0";
                        servicesItem.isSubItemChecked = false;
                        expandableListAdapter.notifyDataSetChanged();

                    }*/
                }
                return false;
            }
        });

        lvExpandable.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {

                return false;
            }
        });

        ivBack.setOnClickListener(this);
        rlService.setOnClickListener(this);
        rlPrice.setOnClickListener(this);
        rlDistance.setOnClickListener(this);
        rlDnT.setOnClickListener(this);
        rlRefineLocation.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnApply.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivHeaderBack:
                onBackPressed();
                break;

            case R.id.rlRefineLocation:
                getAddress();
                break;

            case R.id.rlDnT:
                datePicker();
                break;

            case R.id.rlService:
                if (!isServiceOpen){
                    isServiceOpen = true;
                    lvExpandable.setVisibility(View.VISIBLE);
                } else {
                    isServiceOpen = false;
                    lvExpandable.setVisibility(View.GONE);
                }
                break;
            case R.id.rlPrice:
                ivPrice.setImageResource(R.drawable.active_price_ico);
                ivDistance.setImageResource(R.drawable.route_ico);
                sortSearch = "price";
                break;
            case R.id.rlDistance:
                ivPrice.setImageResource(R.drawable.price_ico);
                ivDistance.setImageResource(R.drawable.active_route_ico);
                sortSearch ="distance";
                break;
            case R.id.btnClear :
                tv_refine_loc.setText("");
                tv_refine_dnt.setText("");

                Intent intent2 = new Intent(RefineArtistActivity.this, MainActivity.class);
                //intent.putExtra("refineSearchBoard",refineSearchBoard);
                startActivity(intent2);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;

            case R.id.btnApply :
                // List<Map<String,String>> list = new ArrayList<Map<String, String>>();
                // list.clear();
                RefineSearchBoard refineSearchBoard = new RefineSearchBoard();

                for (RefineServices services :services ){

                    if (services.isChecked.equals("1")){
                        if (services.getArrayList().size()!=0){
                            for (int j = 0; j < services.getArrayList().size(); j++) {
                                RefineSubServices subItem = services.getArrayList().get(j);
                                if (subItem.isChecked.equals("1")){
                                    if(subServiceId.equals("") && mainServId.equals("")){
                                        subServiceId =  subItem.id;
                                        mainServId =  subItem.serviceId;
                                    }
                                    else {
                                        subServiceId = subServiceId + "," + subItem.id;
                                        if (!mainServId.contains(subItem.serviceId))
                                            mainServId = mainServId + "," + subItem.serviceId;
                                    }
                                }

                            }
                        }
                    }

                }
               /* for (RefineServices services :services ){
                    if (services.isChecked.equals("1")){
                        if (services.getArrayList().size()!=0){
                            for (int j = 0; j < services.getArrayList().size(); j++) {
                                RefineSubServices subItem = services.getArrayList().get(j);
                                ArrayList<RefineSubServices> arrayList = new ArrayList<>();
                                if (subItem.isChecked.equals("1")){
                                    services.setArrayList(arrayList);
                                }
                            }
                        }
                        refineSearchBoard.refineServices.add(services);
                    }
                }
*/
                refineSearchBoard.refineServices.addAll(services);
                refineSearchBoard.day = ""+dayId;
                refineSearchBoard.latitude = lat;
                refineSearchBoard.longitude = lng;
                refineSearchBoard.service = mainServId;
                refineSearchBoard.subservice = subServiceId;
                refineSearchBoard.serviceType = serviceType;
                refineSearchBoard.sortSearch = sortSearch;
                refineSearchBoard.sortType = sortType;
                refineSearchBoard.time = time;
                refineSearchBoard.date = date_time;
                refineSearchBoard.location = location;

                Intent intent = new Intent(RefineArtistActivity.this, MainActivity.class);
                intent.putExtra("refineSearchBoard",refineSearchBoard);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                break;
        }
    }

    private void getAddress() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(RefineArtistActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    private void apiForGetAllServices(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(RefineArtistActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetAllServices();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        // params.put("appType", "user");


        HttpTask task = new HttpTask(new HttpTask.Builder(RefineArtistActivity.this, "allCategory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        JSONArray jsonArray = js.getJSONArray("serviceList");
                        services.clear();
                        if (jsonArray!=null) {
                            for (int i=0; i<jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                RefineServices service = new RefineServices();
                                service.id = jsonObject.getString("_id");
                                service.title = jsonObject.getString("title");

                                JSONArray subArray = jsonObject.getJSONArray("subService");
                                ArrayList<RefineSubServices> arrayList = new ArrayList<>();

                                for (int j = 0; j < subArray.length(); j++) {
                                    RefineSubServices subItem = new RefineSubServices();
                                    JSONObject jsonObject2 = subArray.getJSONObject(j);
                                    subItem.id = jsonObject2.getString("_id").trim();
                                    subItem.image = jsonObject2.getString("image").trim();
                                    subItem.title = jsonObject2.getString("title").trim();
                                    subItem.serviceId = jsonObject2.getString("serviceId").trim();
                                    subItem.isChecked = "0";
                                    arrayList.add(subItem);
                                }
                                service.setArrayList(arrayList);
                                services.add(service);
                            }
                            expandableListAdapter.notifyDataSetChanged();
                        }else {
                            MyToast.getInstance(RefineArtistActivity.this).showSmallCustomToast("No Artist available!");
                        }
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(RefineArtistActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session"))
                        Mualab.getInstance().getSessionManager().logout();
                    MyToast.getInstance(RefineArtistActivity.this).showSmallCustomToast(helper.error_Messages(error));
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));

        task.execute(this.getClass().getName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                tv_refine_loc.setText(place.getName());
                location = ""+place.getName();
                LatLng latLng = place.getLatLng();
                lat = String.valueOf(latLng.latitude);
                lng = String.valueOf(latLng.longitude);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void datePicker(){
        // Get Current Date
        final Calendar c = GregorianCalendar.getInstance();
        int mYear = c.get(GregorianCalendar.YEAR);
        int mMonth = c.get(GregorianCalendar.MONTH);
        int mDay = c.get(GregorianCalendar.DAY_OF_MONTH);
        dayId = c.get(GregorianCalendar.DAY_OF_WEEK)-1;
        String weekday = new DateFormatSymbols().getShortWeekdays()[dayId];

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,AlertDialog.THEME_HOLO_LIGHT,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                        Date date = new Date(year, monthOfYear, dayOfMonth-1);
                        dayId = date.getDay()-1;
                        date_time = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                        //*************Call Time Picker Here ********************
                        tiemPicker();

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void tiemPicker(){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,AlertDialog.THEME_HOLO_LIGHT,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay == 0) {

                            hourOfDay += 12;

                            format = "AM";
                        }
                        else if (hourOfDay == 12) {

                            format = "PM";

                        }
                        else if (hourOfDay > 12) {

                            hourOfDay -= 12;

                            format = "PM";

                        }
                        else {

                            format = "AM";
                        }

                        mHour = hourOfDay;
                        mMinute = minute;
                        time = hourOfDay + ":" + minute+" "+format;

                        tv_refine_dnt.setText(date_time+" "+hourOfDay + ":" + minute+" "+format);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    @Override
    public void onDateSet(int year, int month, int day, int cal_type) {
        tv_refine_dnt.setText(day + "/" + (month + 1) + "/" + year);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RefineArtistActivity.this, MainActivity.class);
        intent.putExtra("refineSearchBoard",refineSearchBoard);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
