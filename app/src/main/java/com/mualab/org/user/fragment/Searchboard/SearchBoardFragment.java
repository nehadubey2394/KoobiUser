package com.mualab.org.user.fragment.Searchboard;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.MainActivity;
import com.mualab.org.user.activity.searchBoard.RefineArtistActivity;
import com.mualab.org.user.adapter.searchboard.SearchBoardAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.helper.Helper;
import com.mualab.org.user.helper.MyToast;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.pagination.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.LocationDetector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SearchBoardFragment extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private ArrayList<ArtistsSearchBoard>artistsList;
    private RecyclerView rvSearchBoard;
    private SearchBoardAdapter listAdapter;
    private ImageView ivStar;
    private boolean isFavClick = false;
    private Context mContext;
    private String lat = "22.757062",lng = "75.882186";
    public static RelativeLayout rlHeader1;

    public SearchBoardFragment() {
        // Required empty public constructor
    }


    public static SearchBoardFragment newInstance(String param2) {
        SearchBoardFragment fragment = new SearchBoardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        apiForGetArtist(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_board, container, false);
        init();
        setViewId(view);
        // Inflate the layout for this fragment
        return view;
    }

    private void init(){
        artistsList = new ArrayList<>();
        listAdapter = new SearchBoardAdapter(getActivity(), artistsList);
        // addArtist();
    }

    private void setViewId(View view){
        rvSearchBoard = view.findViewById(R.id.rvSearchBoard);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvSearchBoard.setLayoutManager(layoutManager);
        rvSearchBoard.setAdapter(listAdapter);
        artistsList.clear();
        //apiForGetArtist(0);

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                listAdapter.showLoading(true);
                //apiForGetArtist(page);
                apiForLoadMoreArtist(page);
            }
        };

        // Adds the scroll listener to RecyclerView
        rvSearchBoard.addOnScrollListener(scrollListener);

        CardView cvFilter = view.findViewById(R.id.cvFilter);
        CardView cvFavourite = view.findViewById(R.id.cvFavourite);
        ivStar = view.findViewById(R.id.ivStar);

        cvFavourite.setOnClickListener(this);
        cvFilter.setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cvFilter:
                startActivity(new Intent(getActivity(),  RefineArtistActivity.class));
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
                MyToast.getInstance(getActivity()).showSmallCustomToast("Under developement");
                break;

        }
    }

    private void apiForGetArtist(final int page){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetArtist(page);
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

        new HttpTask(new HttpTask.Builder(mContext, "artistSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        Progress.hide(mContext);
                        listAdapter.showLoading(false);
                        JSONArray artistArray = js.getJSONArray("artistList");
                        if (artistArray!=null)
                        {
                            for (int i=0; i<artistArray.length(); i++){
                                ArtistsSearchBoard item = new ArtistsSearchBoard();
                                Gson gson = new Gson();
                                JSONObject jsonObject = artistArray.getJSONObject(i);
                                item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                artistsList.add(item);

                            }
                            listAdapter.notifyDataSetChanged();
                            // rvSearchBoard.smoothScrollToPosition(artistsList.size()-1);
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
            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON))
                .execute(this.getClass().getName());
    }

    private void apiForLoadMoreArtist(final int page){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForLoadMoreArtist(page);
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

        new HttpTask(new HttpTask.Builder(mContext, "artistSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        listAdapter.showLoading(false);

                        Progress.hide(mContext);

                        JSONArray artistArray = js.getJSONArray("artistList");
                        if (artistArray!=null)
                        {
                            for (int i=0; i<artistArray.length(); i++){
                                ArtistsSearchBoard item = new ArtistsSearchBoard();
                                Gson gson = new Gson();
                                JSONObject jsonObject = artistArray.getJSONObject(i);
                                item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                artistsList.add(item);

                            }
                            listAdapter.notifyDataSetChanged();
                            // rvSearchBoard.smoothScrollToPosition(artistsList.size()-1);
                        }
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }})
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON))
                .execute(this.getClass().getName());
    }

    private void getDeviceLocation() {
        LocationDetector locationDetector = new LocationDetector();
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (locationDetector.isLocationEnabled(getActivity()) &&
                locationDetector.checkLocationPermission(getActivity())) {

            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        // locateNearByBranch(location.getLatitude(),location.getLongitude());
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        lat = String.valueOf(latitude);
                        lng = String.valueOf(longitude);

                        // LocationRequest locationRequest = new LocationRequest();
                        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    }
                    apiForGetArtist(0);
                }
            });
        }
    }

}
