package com.mualab.org.user.activity.notification.fragments;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.booking_histories.activity.BookingDetailActivity;
import com.mualab.org.user.activity.feeds.FeedSingleActivity;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.my_profile.activity.UserProfileActivity;
import com.mualab.org.user.activity.notification.adapter.NotificationAdapter;
import com.mualab.org.user.activity.notification.model.Notification;
import com.mualab.org.user.activity.story.StoreActivityTest;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.feeds.LiveUserInfo;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.refreshview.CircleHeaderView;
import views.refreshview.OnRefreshListener;
import views.refreshview.RjRefreshLayout;


public class NotificationFragment extends BaseFragment implements NotificationAdapter.Listener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private RecyclerView rycNotification;
    private TextView tvNoData;
    private List<Notification> notificationList;
    private NotificationAdapter notificationAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private LinearLayout ll_loadingBox;
    private RjRefreshLayout mRefreshLayout;
    private boolean isPulltoRefrash;
    private ArrayList<LiveUserInfo> liveUserList = new ArrayList<>();

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View rootView) {
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(mContext, notificationList, this);

        ll_loadingBox = rootView.findViewById(R.id.ll_loadingBox);
        tvNoData = rootView.findViewById(R.id.tvNoData);
        mRefreshLayout = rootView.findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(getContext());
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                scrollListener.resetState();
                isPulltoRefrash = true;
                apiForGetNotifications(0);
            }

            @Override
            public void onLoadMore() {
            }
        });
        rycNotification = rootView.findViewById(R.id.rycNotification);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPositionWithOffset(0, 0);
        rycNotification.setLayoutManager(layoutManager);
        rycNotification.setAdapter(notificationAdapter);

        if (scrollListener == null) {
            scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if (totalItemsCount > 19) {
                        notificationAdapter.showLoading(true);
                        apiForGetNotifications(page);
                    }
                }
            };
        }

        rycNotification.addOnScrollListener(scrollListener);

        if (notificationList.size() == 0) {
            ll_loadingBox.setVisibility(View.VISIBLE);
            apiForGetNotifications(0);
        }
    }

    private void apiForGetNotifications(final int page) {
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        apiForGetNotifications(page);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("type", "");
        params.put("page", String.valueOf(page));
        params.put("limit", "20");

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, "getNotificationList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    ll_loadingBox.setVisibility(View.GONE);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    String total = js.getString("total");

                    if (status.equalsIgnoreCase("success")) {
                        notificationAdapter.showLoading(false);

                        if (page == 0) {
                            notificationList.clear();
                        }

                        rycNotification.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);

                        JSONArray jsonArray = js.getJSONArray("notificationList");
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);
                            int prevSize = notificationList.size();
                            notificationList.clear();
                            notificationAdapter.notifyItemRangeRemoved(0, prevSize);
                        }
                        if (jsonArray != null && jsonArray.length() != 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Gson gson = new Gson();
                                JSONObject object = jsonArray.getJSONObject(i);
                                Notification item = gson.fromJson(String.valueOf(object), Notification.class);
                                notificationList.add(item);
                            }
                        } else if (notificationList.size() == 0) {
                            rycNotification.setVisibility(View.GONE);
                            tvNoData.setVisibility(View.VISIBLE);
                        }
                        notificationAdapter.notifyDataSetChanged();
                    } else {
                        rycNotification.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                        if (isPulltoRefrash) {
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    tvNoData.setVisibility(View.VISIBLE);
                    tvNoData.setText("Something went wrong!");
                    ll_loadingBox.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try {
                    ll_loadingBox.setVisibility(View.GONE);
                    if (isPulltoRefrash) {
                        isPulltoRefrash = false;
                        mRefreshLayout.stopRefresh(false, 500);
                        int prevSize = notificationList.size();
                        notificationList.clear();
                        notificationAdapter.notifyItemRangeRemoved(0, prevSize);
                    }
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")) {
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
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

    @Override
    public void onNotificationClick(int pos) {
        String notifincationType = notificationList.get(pos).notifincationType;
        String notifyId = notificationList.get(pos).notifyId;
        String userName = notificationList.get(pos).userName;
        String urlImageString = notificationList.get(pos).profileImage;
        String senderId = notificationList.get(pos).senderId;

        String usertype = notificationList.get(pos).userType;
        switch (notifincationType) {
            case "13":
                //ibtnFeed.callOnClick();
                liveUserList.clear();
                LiveUserInfo me = new LiveUserInfo();
                me.id = Integer.parseInt(senderId);
                me.userName = userName;
                me.profileImage = urlImageString;
                me.storyCount = 0;
                liveUserList.add(me);
                Intent intent_story = new Intent(mContext, StoreActivityTest.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", liveUserList);
                args.putInt("position", 0);
                intent_story.putExtra("BUNDLE", args);
                startActivity(intent_story);
                break;

            case "7":
                // ibtnFeed.callOnClick();
                Intent intent1 = new Intent(mContext, FeedSingleActivity.class);
                intent1.putExtra("feedId", notifyId);
                startActivity(intent1);

                break;

            case "11":
                //ibtnFeed.callOnClick();
                Intent intent_like_comment = new Intent(mContext, FeedSingleActivity.class);
                intent_like_comment.putExtra("feedId", notifyId);
                startActivity(intent_like_comment);

                break;

            case "9":
                // ibtnFeed.callOnClick();
                Intent intent_comment = new Intent(mContext, FeedSingleActivity.class);
                intent_comment.putExtra("feedId", notifyId);
                startActivity(intent_comment);
                break;

            case "10":
                //ibtnFeed.callOnClick();
                Intent intent_like_post = new Intent(mContext, FeedSingleActivity.class);
                intent_like_post.putExtra("feedId", notifyId);
                startActivity(intent_like_post);

                break;

            case "12":
                //ibtnFeed.callOnClick();
                if (usertype.equals("user")) {
                    Intent intent_user_profile = new Intent(mContext, UserProfileActivity.class);
                    intent_user_profile.putExtra("userId", notifyId);
                    startActivity(intent_user_profile);

                } else {
                    Intent intent_user_profile = new Intent(mContext, ArtistProfileActivity.class);
                    intent_user_profile.putExtra("feedId", notifyId);
                    startActivity(intent_user_profile);

                }

                break;

            case "1":
                Intent booking1 = new Intent(mContext, BookingDetailActivity.class);
                booking1.putExtra("bookingId", notifyId);
                booking1.putExtra("artistName", userName);
                booking1.putExtra("notification_list","list");
                booking1.putExtra("notification_list","list");
                booking1.putExtra("artistProfile", urlImageString);
                startActivity(booking1);

                break;

            case "2":
                Intent booking2 = new Intent(mContext, BookingDetailActivity.class);
                booking2.putExtra("bookingId", notifyId);
                booking2.putExtra("artistName", userName);
                booking2.putExtra("notification_list","list");
                booking2.putExtra("key","notification");

                booking2.putExtra("artistProfile", urlImageString);
                startActivity(booking2);
                // ((MainActivity) mContext).finish();


                break;

            case "3":
                Intent booking3 = new Intent(mContext, BookingDetailActivity.class);
                booking3.putExtra("bookingId", notifyId);
                booking3.putExtra("artistName", userName);
                booking3.putExtra("notification_list","list");
                booking3.putExtra("key","notification");

                booking3.putExtra("artistProfile", urlImageString);

                startActivity(booking3);
                // ((MainActivity) mContext).finish();

                break;


            case "4":
                Intent booking4 = new Intent(mContext, BookingDetailActivity.class);
                booking4.putExtra("bookingId", notifyId);
                booking4.putExtra("artistName", userName);
                booking4.putExtra("artistName", userName);
                booking4.putExtra("key","notification");

                booking4.putExtra("artistProfile", urlImageString);
                startActivity(booking4);
                // ((MainActivity) mContext).finish();

                break;


            case "5":
                Intent booking5 = new Intent(mContext, BookingDetailActivity.class);
                booking5.putExtra("bookingId", notifyId);
                booking5.putExtra("artistName", userName);
                booking5.putExtra("notification_list","list");
                booking5.putExtra("key","notification");

                booking5.putExtra("artistProfile", urlImageString);
                startActivity(booking5);
                //   ((MainActivity) mContext).finish();
                break;


            case "6":
                Intent booking6 = new Intent(mContext, BookingDetailActivity.class);
                booking6.putExtra("bookingId", notifyId);
                booking6.putExtra("artistName", userName);
                booking6.putExtra("notification_list","list");
                booking6.putExtra("key","notification");
                booking6.putExtra("artistProfile", urlImageString);
                startActivity(booking6);
                //   ((MainActivity) mContext).finish();
                break;

            case "16":
                Intent intent_taged =new Intent(mContext, FeedSingleActivity.class);
                intent_taged.putExtra("feedId",notifyId);
                startActivity(intent_taged);

                break;


        }

    }
}
