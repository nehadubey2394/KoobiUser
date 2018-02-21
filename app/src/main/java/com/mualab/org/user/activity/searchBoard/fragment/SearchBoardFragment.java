package com.mualab.org.user.activity.searchBoard.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.searchBoard.RefineArtistActivity;
import com.mualab.org.user.activity.searchBoard.adapter.SearchBoardAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.Helper;
import com.mualab.org.user.util.LocationDetector;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SearchBoardFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    private RecyclerView rvSearchBoard;
    private ImageView ivStar;

    private SearchBoardAdapter listAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ArrayList<ArtistsSearchBoard>artistsList;

    private boolean isFavClick = false;
    private String lat = "22.757062", lng = "75.882186";


    public static SearchBoardFragment newInstance() {
        return new SearchBoardFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(artistsList==null)
            artistsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_board, container, false);
        // Inflate the layout for this fragment
        rvSearchBoard = view.findViewById(R.id.rvSearchBoard);
        ivStar = view.findViewById(R.id.ivStar);
        CardView cvFilter = view.findViewById(R.id.cvFilter);
        CardView cvFavourite = view.findViewById(R.id.cvFavourite);
        cvFilter.setOnClickListener(this);
        cvFavourite.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rvSearchBoard.setLayoutManager(layoutManager);
        listAdapter = new SearchBoardAdapter(mContext, artistsList);
        rvSearchBoard.setAdapter(listAdapter);

        if(scrollListener==null)
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    listAdapter.showLoading(true);
                    apiForGetArtist(page, true);
                    //apiForLoadMoreArtist(page);
                }
            };

        // Adds the scroll listener to RecyclerView
        rvSearchBoard.addOnScrollListener(scrollListener);


        if(artistsList.size()==0)
            getDeviceLocation();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cvFilter:
                startActivity(new Intent(mContext,  RefineArtistActivity.class));
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;

            case R.id.cvFavourite:
                if (!isFavClick){
                    isFavClick = true;
                    ivStar.setImageResource(R.drawable.fill_star_ico);
                } else {
                    isFavClick = false;
                    ivStar.setImageResource(R.drawable.star_ico);
                }
                MyToast.getInstance(mContext).showSmallCustomToast("Under developement");
                break;

        }
    }

    private void getDeviceLocation() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constant.MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                LocationDetector locationDetector = new LocationDetector();
                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
                if (locationDetector.isLocationEnabled(getActivity()) &&
                        locationDetector.checkLocationPermission(getActivity())) {

                    mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                lat = String.valueOf(latitude);
                                lng = String.valueOf(longitude);
                                apiForGetArtist(0, false);
                            }
                        }
                    });

                }else {
                    locationDetector.showLocationSettingDailod(getActivity());
                }
            }
        }else {
            apiForGetArtist(0,false);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getDeviceLocation();
                    }

                } else {
                    //Toast.makeText(mContext, "Permission Denied", Toast.LENGTH_LONG).show();
                    apiForGetArtist(0,false);
                }
            }

        }
    }

    private void apiForGetArtist(final int page, final boolean isLoadMore){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetArtist(page, isLoadMore);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("latitude", lat);
        params.put("longitude", lng);
        params.put("distance", "10");
        params.put("page", ""+page);
        params.put("limit", "10");
        params.put("service", "");
        params.put("serviceType", "");
        params.put("day", "");
        params.put("time", "");
        // params.put("appType", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "artistSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        listAdapter.showLoading(false);
                        JSONArray artistArray = js.getJSONArray("artistList");

                        if (artistArray!=null) {
                            for (int i=0; i<artistArray.length(); i++){
                                Gson gson = new Gson();
                                JSONObject jsonObject = artistArray.getJSONObject(i);
                                ArtistsSearchBoard item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                artistsList.add(item);

                            }
                            listAdapter.notifyDataSetChanged();
                        }
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
                    if (helper.error_Messages(error).contains("Session"))
                        Mualab.getInstance().getSessionManager().logout();
                    MyToast.getInstance(mContext).showSmallCustomToast(helper.error_Messages(error));
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(!isLoadMore)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));

        task.execute(this.getClass().getName());
    }
}