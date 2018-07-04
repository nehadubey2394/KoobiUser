package com.mualab.org.user.activity.artist_profile.activity;

import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.adapter.ServicesAdapter;
import com.mualab.org.user.activity.artist_profile.adapter.SubServiceExpandListAdapter;
import com.mualab.org.user.activity.artist_profile.listner.OnServiceClickListener;
import com.mualab.org.user.activity.artist_profile.model.ArtistCategory;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.ArtistServices;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.BookingServices3;
import com.mualab.org.user.data.model.booking.SubServices;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArtistServicesActivity extends AppCompatActivity implements OnServiceClickListener {
    private List<ArtistCategory> servicesList;
    private List<SubServices> subServicesList;
    private ExpandableListView lvExpandable;
    private TextView tvNoData;
    private SubServiceExpandListAdapter expandableListAdapter;
    private ServicesAdapter servicesAdapter;
    private LinearLayout llCategory;
    private String artistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_services);
        Intent i = getIntent();
        artistId =  i.getStringExtra("artistId");
        initView();
    }

    private void initView(){
        servicesList = new ArrayList<>();
        subServicesList = new ArrayList<>();
        servicesAdapter = new ServicesAdapter(ArtistServicesActivity.this, servicesList);
        servicesAdapter.setCustomListener(ArtistServicesActivity.this);
        expandableListAdapter = new SubServiceExpandListAdapter(ArtistServicesActivity.this, subServicesList);
        setView();
    }

    private void setView(){
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.text_services));
        llCategory = findViewById(R.id.llCategory);

        RecyclerView rycService = findViewById(R.id.rycService);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ArtistServicesActivity.this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.scrollToPositionWithOffset(0, 0);
        rycService.setLayoutManager(layoutManager);
        rycService.setAdapter(servicesAdapter);
        lvExpandable = findViewById(R.id.lvExpandable);
        tvNoData = findViewById(R.id.tvNoData);
        lvExpandable.setAdapter(expandableListAdapter);

        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {

            }
        });

        lvExpandable.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                ArtistCategory item = servicesList.get(groupPosition);
            }
        });

        // Listview Group collasped listener
        lvExpandable.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                ArtistCategory item = servicesList.get(groupPosition);

            }
        });

        lvExpandable.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                return true;
            }
        });

        lvExpandable.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {
                onChildClickListener(expandableListView,view,groupPosition,childPosition,l);
                return false;
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        apiForGetAllServices();
    }

    private void onChildClickListener(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l){
        ArtistCategory servicesItem = servicesList.get(groupPosition);
        //SubServices subServices = servicesItem.arrayList.get(childPosition);
    }

    private void apiForGetAllServices(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistServicesActivity.this, new NoConnectionDialog.Listner() {
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
        params.put("artistId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistServicesActivity.this, "artistService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        lvExpandable.setVisibility(View.VISIBLE);
                        llCategory.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                        servicesList.clear();
                        subServicesList.clear();

                        JSONArray allServiceArray = js.getJSONArray("artistServices");
                        if (allServiceArray!=null) {

                            for (int j=0; j<allServiceArray.length(); j++){
                                JSONObject object = allServiceArray.getJSONObject(j);
                                ArtistCategory services = new ArtistCategory();
                                services.serviceId = object.getString("serviceId");
                                services.serviceName = object.getString("serviceName");

                                services.isSelect = j == 0;

                                JSONArray subServiesArray = object.getJSONArray("subServies");
                                if (subServiesArray!=null) {
                                    for (int k=0; k<subServiesArray.length(); k++){
                                        JSONObject jObj = subServiesArray.getJSONObject(k);
                                        //   Gson gson = new Gson();
                                        //  SubServices subServices = gson.fromJson(String.valueOf(jObj), SubServices.class);
                                        SubServices subServices = new SubServices();
                                        subServices._id = jObj.getString("_id");
                                        subServices.serviceId = jObj.getString("serviceId");
                                        subServices.subServiceId = jObj.getString("subServiceId");
                                        subServices.subServiceName = jObj.getString("subServiceName");

                                        JSONArray artistservices = jObj.getJSONArray("artistservices");
                                        for (int m=0; m<artistservices.length(); m++){
                                            JSONObject jsonObject3 = artistservices.getJSONObject(m);
                                            Gson gson2 = new Gson();
                                            BookingServices3 services3 = gson2.fromJson(String.valueOf(jsonObject3), BookingServices3.class);

                                            if (!services3.outCallPrice.equals("0") || !services3.outCallPrice.equals("null")){
                                                services3.isOutCall3 = true;
                                                subServices.isOutCall2 = true;
                                                services.isOutCall = true;
                                            }
                                            subServices.artistservices.add(services3);
                                        }

                                        services.arrayList.add(subServices);
                                    }
                                }
                                servicesList.add(services);
                            }
                            servicesAdapter.notifyDataSetChanged();
                            subServicesList.addAll(servicesList.get(0).arrayList);
                            expandableListAdapter.notifyDataSetChanged();

                            for(int i=0; i < expandableListAdapter.getGroupCount(); i++)
                                lvExpandable.expandGroup(i);
                        }
                    }else {
                        lvExpandable.setVisibility(View.GONE);
                        llCategory.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Progress.hide(ArtistServicesActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
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

    @Override
    public void onServiceClick(int position) {
        for(ArtistCategory artistCategory : servicesList)
            artistCategory.isSelect = false;
        servicesAdapter.notifyDataSetChanged();
        ArtistCategory category = servicesList.get(position);
        category.isSelect = true;
        servicesAdapter.notifyItemChanged(position);

        subServicesList.clear();
        subServicesList.addAll(servicesList.get(position).arrayList);
        expandableListAdapter.notifyDataSetChanged();

        for(int i=0; i < expandableListAdapter.getGroupCount(); i++)
            lvExpandable.expandGroup(i);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
