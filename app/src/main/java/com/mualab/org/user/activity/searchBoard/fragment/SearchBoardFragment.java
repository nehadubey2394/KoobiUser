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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.mualab.org.user.R;

import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment4;
import com.mualab.org.user.activity.searchBoard.RefineArtistActivity;
import com.mualab.org.user.activity.searchBoard.adapter.SearchBoardAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.LocationDetector;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.refreshview.CircleHeaderView;
import views.refreshview.OnRefreshListener;
import views.refreshview.RjRefreshLayout;


public class SearchBoardFragment extends BaseFragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static String TAG = SearchBoardFragment.class.getName();

    private RecyclerView rvSearchBoard;
    private ImageView ivStar;
    private TextView tv_msg;
    private ProgressBar progress_bar;
    private LinearLayout ll_loadingBox;
    private SearchView searchview;
    private SearchBoardAdapter listAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private List<ArtistsSearchBoard> artistsList;
    private static boolean isFavClick = false;
    private boolean isPulltoRefrash;
    private RefineSearchBoard item;
    private String subServiceId = "", mainServId = "", searchKeyword = "", sortType = "0", sortSearch = "distance", serviceType = "", lat = "", lng = "", time = "", day = "", date;
    private RjRefreshLayout mRefreshLayout;

    public static SearchBoardFragment newInstance(RefineSearchBoard item, String param2) {
        SearchBoardFragment fragment = new SearchBoardFragment();
        Bundle args = new Bundle();
        args.putSerializable("param1", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //if(context instanceof MainActivity)
        //    ((MainActivity)context).setBgColor(R.color.screen_bg_color);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (RefineSearchBoard) getArguments().getSerializable("param1");
        }
        if (artistsList == null)
            artistsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_board, container, false);
        // Inflate the layout for this fragment
        rvSearchBoard = view.findViewById(R.id.rvSearchBoard);
        ivStar = view.findViewById(R.id.ivStar);
        tv_msg = view.findViewById(R.id.tv_msg);
        progress_bar = view.findViewById(R.id.progress_bar);
        ll_loadingBox = view.findViewById(R.id.ll_loadingBox);
        searchview = view.findViewById(R.id.searchview);
        CardView cvFilter = view.findViewById(R.id.cvFilter);
        CardView cvFavourite = view.findViewById(R.id.cvFavourite);

        initView();

        cvFilter.setOnClickListener(this);
        cvFavourite.setOnClickListener(this);
        return view;
    }

    private void initView() {
        if (item != null) {
            lat = item.latitude;
            lng = item.longitude;
            subServiceId = item.subservice;
            mainServId = item.service;
            serviceType = item.serviceType;
            sortSearch = item.sortSearch;
            sortType = item.sortType;
            isFavClick = item.isFavClick;
            time = item.time;
            if (item.day.equals("100")) {
                day = "";
            } else
                day = item.day;
            date = item.date;

            if (!lat.equals("") && !lng.equals("")) {
                Mualab.currentLocationForBooking.lat = Double.parseDouble(lat);
                Mualab.currentLocationForBooking.lng = Double.parseDouble(lng);
            }
        }
        if (isFavClick) {
            ivStar.setImageResource(R.drawable.fill_star_ico);
        } else {
            ivStar.setImageResource(R.drawable.star_ico);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout =  view.findViewById(R.id.mSwipeRefreshLayout);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        rvSearchBoard.setLayoutManager(layoutManager);
        listAdapter = new SearchBoardAdapter(mContext, artistsList);
        rvSearchBoard.setAdapter(listAdapter);
        rvSearchBoard.setItemAnimator(null);
        rvSearchBoard.setHasFixedSize(true);
        searchKeyword = "";
        KeyboardUtil.hideKeyboard(searchview, mContext);
        apiForDeleteAllPendingBooking();

        if (scrollListener == null)
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    listAdapter.showLoading(true);
                    if (isFavClick)
                        apiForGetFavArtist(page, true);
                    else
                        apiForGetArtist(page, true);
                }
            };

        // Adds the scroll listener to RecyclerView
        rvSearchBoard.addOnScrollListener(scrollListener);

        final CircleHeaderView header = new CircleHeaderView(getContext());

        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                scrollListener.resetState();
                isPulltoRefrash = true;
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);
                // apiForGetArtist(0, false);
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
            }
        });


        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                KeyboardUtil.hideKeyboard(searchview, mContext);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchKeyword = newText.trim();
                Mualab.getInstance().cancelPendingRequests(TAG);
                ll_loadingBox.setVisibility(View.VISIBLE);
                tv_msg.setVisibility(View.GONE);
                progress_bar.setVisibility(View.VISIBLE);
                scrollListener.resetState();
                if (isFavClick)
                    apiForGetFavArtist(0, false);
                else
                    apiForGetArtist(0, false);
                return false;
            }
        });

        if (artistsList.size() == 0) {
            tv_msg.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            ll_loadingBox.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.VISIBLE);
            tv_msg.setText(getString(R.string.loading));
            getDeviceLocation();
        }

    }

    private void showProgress() {
        ll_loadingBox.setVisibility(View.VISIBLE);
        tv_msg.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        ll_loadingBox.setVisibility(View.GONE);
        progress_bar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cvFilter:
                Intent intent = new Intent(mContext, RefineArtistActivity.class);
                intent.putExtra("params", item);
                intent.putExtra("param2", isFavClick);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                getActivity().finish();
                break;

            case R.id.cvFavourite:
                artistsList.clear();
                if (!isFavClick) {
                    showProgress();
                    isFavClick = true;
                    ivStar.setImageResource(R.drawable.fill_star_ico);
                    apiForGetFavArtist(0, false);
                } else {
                    showProgress();
                    isFavClick = false;
                    ivStar.setImageResource(R.drawable.star_ico);
                    apiForGetArtist(0, false);

                }

                break;

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
                    if (isFavClick)
                        apiForGetFavArtist(0, false);
                    else
                        apiForGetArtist(0, false);
                }
            }

        }
    }

    private synchronized void apiForGetArtist(final int page, final boolean isLoadMore) {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetArtist(page, isLoadMore);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("latitude", lat);
        params.put("longitude", lng);
        // params.put("distance", "10");
        params.put("page", "" + page);
        params.put("limit", "10");
        params.put("service", mainServId);
        params.put("serviceType", serviceType);
        params.put("day", day);
        params.put("time", time);
        params.put("subservice", subServiceId);
        params.put("sortSearch", sortSearch);
        params.put("sortType", sortType);
        params.put("text", searchKeyword);
        params.put("userId", String.valueOf(Mualab.currentUser.id));
        // params.put("appType", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "artistSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    tv_msg.setVisibility(View.VISIBLE);
                    tv_msg.setTextColor(mContext.getResources().getColor(R.color.text_color));
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    System.out.println("searchKeyword====" + searchKeyword + "&page " + page);

                    if (page == 0) artistsList.clear();

                    if (status.equalsIgnoreCase("success")) {

                        listAdapter.showLoading(false);
                        JSONArray artistArray = js.getJSONArray("artistList");
                        Gson gson = new Gson();
                        if (artistArray != null && artistArray.length() > 0) {
                            if(isPulltoRefrash){
                                isPulltoRefrash = false;
                                mRefreshLayout.stopRefresh(true, 500);
                                int prevSize = artistsList.size();
                                artistsList.clear();
                                listAdapter.notifyItemRangeRemoved(0, prevSize);
                            }
                            //rvSearchBoard.setVisibility(View.VISIBLE);
                            ll_loadingBox.setVisibility(View.GONE);

                            for (int i = 0; i < artistArray.length(); i++) {
                                JSONObject jsonObject = artistArray.getJSONObject(i);
                                ArtistsSearchBoard item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                String services = "";
                                if (item.service.size() != 0) {
                                    if (item.service.size() < 2) {
                                        services = item.service.get(0).title;
                                    } else {
                                        for (int j = 0; j < 2; j++) {
                                            if (services.equals("")) {
                                                services = item.service.get(j).title;
                                            } else {
                                                services = services + ", " + item.service.get(j).title;
                                            }
                                        }
                                    }
                                } else {
                                    services = "NA";
                                }
                                item.categoryName = services;
                                item.isFav = false;
                                artistsList.add(item);
                            }
                        }
                    }

                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        tv_msg.setText(R.string.no_artist_found);
                        ll_loadingBox.setVisibility(View.VISIBLE);
                        if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }

                    //  showToast(message);
                } catch (Exception e) {
                    tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                    e.printStackTrace();
                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        tv_msg.setText(R.string.no_artist_found);
                        ll_loadingBox.setVisibility(View.VISIBLE);
                        if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);
                    int prevSize = artistsList.size();
                    artistsList.clear();
                    listAdapter.notifyItemRangeRemoved(0, prevSize);
                }
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(TAG);
    }

    private synchronized void apiForGetFavArtist(final int page, final boolean isLoadMore) {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetFavArtist(page, isLoadMore);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("latitude", lat);
        params.put("longitude", lng);
        // params.put("distance", "10");
        params.put("page", "" + page);
        params.put("limit", "10");
        params.put("service", mainServId);
        params.put("serviceType", serviceType);
        params.put("day", day);
        params.put("time", time);
        params.put("subservice", subServiceId);
        params.put("sortSearch", sortSearch);
        params.put("sortType", sortType);
        params.put("text", searchKeyword);
        params.put("userId", String.valueOf(Mualab.currentUser.id));
        // params.put("appType", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "favoriteList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    tv_msg.setVisibility(View.VISIBLE);
                    tv_msg.setTextColor(mContext.getResources().getColor(R.color.text_color));
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    System.out.println("searchKeyword====" + searchKeyword + "&page " + page);

                    if (page == 0) artistsList.clear();

                    if (status.equalsIgnoreCase("success")) {

                        listAdapter.showLoading(false);
                        JSONArray artistArray = js.getJSONArray("artistList");
                        Gson gson = new Gson();
                        if (artistArray != null && artistArray.length() > 0) {
                            //rvSearchBoard.setVisibility(View.VISIBLE);
                            if(isPulltoRefrash){
                                isPulltoRefrash = false;
                                mRefreshLayout.stopRefresh(true, 500);
                                int prevSize = artistsList.size();
                                artistsList.clear();
                                listAdapter.notifyItemRangeRemoved(0, prevSize);
                            }
                            ll_loadingBox.setVisibility(View.GONE);

                            for (int i = 0; i < artistArray.length(); i++) {
                                JSONObject jsonObject = artistArray.getJSONObject(i);
                                ArtistsSearchBoard item = gson.fromJson(String.valueOf(jsonObject), ArtistsSearchBoard.class);
                                String services = "";
                                if (item.service.size() != 0) {
                                    if (item.service.size() < 2) {
                                        services = item.service.get(0).title;
                                    } else {
                                        for (int j = 0; j < 2; j++) {
                                            if (services.equals("")) {
                                                services = item.service.get(j).title;
                                            } else {
                                                services = services + ", " + item.service.get(j).title;
                                            }
                                        }
                                    }
                                } else {
                                    services = "NA";
                                }
                                item.categoryName = services;
                                item.isFav = true;
                                artistsList.add(item);
                            }
                        }
                    }

                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        tv_msg.setText(R.string.no_artist_found);
                        ll_loadingBox.setVisibility(View.VISIBLE);
                    }

                    //  showToast(message);
                } catch (Exception e) {
                    tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                    e.printStackTrace();
                    listAdapter.notifyDataSetChanged();
                    if (artistsList.size() == 0) {
                        tv_msg.setText(R.string.no_artist_found);
                        ll_loadingBox.setVisibility(View.VISIBLE);
                        if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);
                    int prevSize = artistsList.size();
                    artistsList.clear();
                    listAdapter.notifyItemRangeRemoved(0, prevSize);
                }
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        task.execute(TAG);
    }

    private void apiForDeleteAllPendingBooking() {
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForDeleteAllPendingBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "deleteUserBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        BookingFragment4.arrayListbookingInfo.clear();
                        Session session = Mualab.getInstance().getSessionManager();
                        session.setUserChangedLocLat("");
                        session.setUserChangedLocLng("");
                        session.setUserChangedLocName("");

                    } else {
                    }
                } catch (Exception e) {
                    Progress.hide(mContext);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        })
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
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
                                Mualab.currentLocation.lat = latitude;
                                Mualab.currentLocation.lng = longitude;

                                Mualab.currentLocationForBooking.lat = latitude;
                                Mualab.currentLocationForBooking.lng = longitude;

                                if (lng.equals("") && lat.equals("")) {
                                    lat = String.valueOf(latitude);
                                    lng = String.valueOf(longitude);
                                }

                                if (isFavClick)
                                    apiForGetFavArtist(0, false);
                                else
                                    apiForGetArtist(0, false);
                            }
                        }
                    });

                } else {
                    progress_bar.setVisibility(View.GONE);
                    tv_msg.setText(R.string.gps_permission_alert);
                    locationDetector.showLocationSettingDailod(getActivity());
                }
            }
        } else {
            if (isFavClick)
                apiForGetFavArtist(0, false);
            else
                apiForGetArtist(0, false);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Mualab.getInstance().getRequestQueue().cancelAll(TAG);
    }


}