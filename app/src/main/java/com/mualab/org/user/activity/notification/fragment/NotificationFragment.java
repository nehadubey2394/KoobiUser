package com.mualab.org.user.activity.notification.fragment;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.FollowersActivity;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.notification.fragment.adapter.NotificationAdapter;
import com.mualab.org.user.activity.notification.fragment.model.Notification;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NotificationFragment extends BaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private RecyclerView rycNotification;
    private TextView tvNoData;
    private List<Notification> notificationList;
    private NotificationAdapter notificationAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    // TODO: Rename and change types of parameters
    private String mParam1;


    public NotificationFragment() {
        // Required empty public constructor
    }


    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("param1");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification, container, false);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View rootView){
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(mContext,notificationList);

        tvNoData = rootView.findViewById(R.id.tvNoData);
        rycNotification = rootView.findViewById(R.id.rycNotification);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPositionWithOffset(0, 0);
        rycNotification.setLayoutManager(layoutManager);
        rycNotification.setAdapter(notificationAdapter);

        if(scrollListener==null) {
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    notificationAdapter.showLoading(true);
                    apiForGetNotifications(page);
                }
            };
        }

        if (notificationList.size()==0)
            apiForGetNotifications(0);
    }

    private void apiForGetNotifications(final int page){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetNotifications(page);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId",String.valueOf(user.id));
        params.put("type", "");
        params.put("page", String.valueOf(page));
        params.put("limit", "");

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "getNotificationList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    String total = js.getString("total");

                    if (status.equalsIgnoreCase("success")) {
                        notificationAdapter.showLoading(false);

                        if (page==0) {
                            notificationList.clear();
                        }

                        rycNotification.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);

                        JSONArray jsonArray = js.getJSONArray("notificationList");
                        if (jsonArray!=null && jsonArray.length()!=0) {
                            for (int i=0; i<jsonArray.length(); i++){
                                Gson gson = new Gson();
                                JSONObject object = jsonArray.getJSONObject(i);
                                Notification item = gson.fromJson(String.valueOf(object), Notification.class);
                                notificationList.add(item);
                            }
                        }else {
                            rycNotification.setVisibility(View.GONE);
                            tvNoData.setVisibility(View.VISIBLE);
                        }
                        notificationAdapter.notifyDataSetChanged();
                    }else {
                        rycNotification.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
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
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
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
}
