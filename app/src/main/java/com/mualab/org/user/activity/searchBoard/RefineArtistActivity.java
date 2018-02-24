package com.mualab.org.user.activity.searchBoard;


import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.mualab.org.user.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;

public class RefineArtistActivity extends AppCompatActivity implements View.OnClickListener,DatePickerListener {
    private ExpandableListView lvExpandable;
    private boolean isServiceOpen = false;
    private ImageView ivPrice,ivDistance;
    private TextView tv_refine_dob,tv_refine_loc;
    private RefineServiceExpandListAdapter expandableListAdapter;
    private ArrayList<RefineServices>services;
    private String subServiceId = "",mainServId = "",sortType ="",sortSearch ="distance",serviceType="",lat="",lng="";
    private CheckBox chbOutcall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refine_artist);
        initView();
        setViewId();
    }

    private void initView(){
        services = new ArrayList<>();
        expandableListAdapter = new RefineServiceExpandListAdapter(RefineArtistActivity.this, services);
        apiForGetAllServices();
    }

    private void setViewId(){
        ImageView ivBack = findViewById(R.id.ivHeaderBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.titel_refine);
        ivBack.setVisibility(View.VISIBLE);

        ivPrice = findViewById(R.id.ivPrice);
        ivDistance = findViewById(R.id.ivDistance);

        tv_refine_dob = findViewById(R.id.tv_refine_dob);
        tv_refine_loc = findViewById(R.id.tv_refine_loc);
        chbOutcall = findViewById(R.id.chbOutcall);

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
        RelativeLayout rlDob = findViewById(R.id.rlDob);

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
        //  MyToast.getInstance(RefineArtistActivity.this).showSmallCustomToast("Under developement");

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
             /*   RefineServices servicesItem = services.get(groupPosition);
                ArrayList<RefineSubServices> arrayList = servicesItem.getArrayList();

                RefineSubServices subModel = arrayList.get(childPosition);
                if (subModel.isChecked.equals("0")){
                    subModel.isChecked = "1";
                   // servicesItem.isSubItemChecked = true;
                   // servicesItem.isChecked = "1";
                    expandableListAdapter.notifyDataSetChanged();

                }else {
                    subModel.isChecked = "0";
                 //   servicesItem.isSubItemChecked = false;
                  //  servicesItem.isChecked = "0";
                    expandableListAdapter.notifyDataSetChanged();
                }*/
                return false;
            }
        });

        ivBack.setOnClickListener(this);
        rlService.setOnClickListener(this);
        rlPrice.setOnClickListener(this);
        rlDistance.setOnClickListener(this);
        rlDob.setOnClickListener(this);
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

            case R.id.rlDob:
                DatePickerFragment datePickerFragment = new DatePickerFragment(Constant.CALENDAR_DAY_PAST, true,false);
                datePickerFragment.setDateListener(this);
                datePickerFragment.show(getSupportFragmentManager(), "");
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
                tv_refine_dob.setText("");
                break;

            case R.id.btnApply :
                subServiceId = "";
                // List<Map<String,String>> list = new ArrayList<Map<String, String>>();
                // list.clear();
                for (RefineServices services :services ){
                    //HashMap<String,String> hashMap = new HashMap<>();
                 /*   if (mainServId.equals(""))
                        mainServId =  services.id;
                    else
                        mainServId = mainServId + "," + services.id;*/


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
                                    //     hashMap.put("mainServId",mainServId);
                                    //     hashMap.put("SubId", subServiceId);
                                }

                            }
                        }/*else {
                            hashMap.put("mainServId",mainServId);
                            hashMap.put("SubId", subServiceId);
                        }
                        if (hashMap.size()!=0)
                            list.add(hashMap);*/
                    }
                }

                RefineSearchBoard refineSearchBoard = new RefineSearchBoard();
                refineSearchBoard.day = "";
                refineSearchBoard.latitude = lat;
                refineSearchBoard.longitude = lng;
                refineSearchBoard.service = mainServId;
                refineSearchBoard.subservice = subServiceId;
                refineSearchBoard.serviceType = serviceType;
                refineSearchBoard.sortSearch = sortSearch;
                refineSearchBoard.sortType = sortType;


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
    @Override
    public void onDateSet(int year, int month, int day, int cal_type) {
        tv_refine_dob.setText(day + "/" + (month + 1) + "/" + year);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RefineArtistActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
