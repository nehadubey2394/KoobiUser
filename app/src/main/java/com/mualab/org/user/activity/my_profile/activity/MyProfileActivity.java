package com.mualab.org.user.activity.my_profile.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.FollowersActivity;
import com.mualab.org.user.activity.artist_profile.adapter.ArtistFeedAdapter;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.feeds.CommentsActivity;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.activity.my_profile.adapter.NavigationMenuAdapter;
import com.mualab.org.user.activity.my_profile.model.NavigationItem;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.feeds.Feeds;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.listner.RecyclerViewScrollListener;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import views.refreshview.CircleHeaderView;
import views.refreshview.OnRefreshListener;
import views.refreshview.RjRefreshLayout;

public class MyProfileActivity extends AppCompatActivity implements View.OnClickListener,ArtistFeedAdapter.Listener{
    private DrawerLayout drawer;
    private String TAG = this.getClass().getName();;
    private User user;
    private TextView tvImages,tvVideos,tvFeeds,tv_msg,tv_no_data_msg,tv_dot1,tv_dot2;
    private LinearLayout ll_progress;
    private RecyclerView rvFeed;
    private RjRefreshLayout mRefreshLayout;
    private RecyclerViewScrollListener endlesScrollListener;
    private int CURRENT_FEED_STATE = 0,lastFeedTypeId;
    private String feedType = "";
    private ArtistFeedAdapter feedAdapter;
    private List<Feeds> feeds;
    private boolean isPulltoRefrash = false;
    private  long mLastClickTime = 0;
    private UserProfileData profileData = null;
    private List<NavigationItem> navigationItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        init();
    }

    private void init(){
        Session session = Mualab.getInstance().getSessionManager();
        user = session.getUser();
        feeds = new ArrayList<>();
        navigationItems = new ArrayList<>();

        Toolbar toolbar =  findViewById(R.id.toolbar);
        LinearLayout lyImage = toolbar.findViewById(R.id.ly_images);
        LinearLayout lyVideos = toolbar.findViewById(R.id.ly_videos);
        LinearLayout lyFeed = toolbar.findViewById(R.id.ly_feeds);

        tvImages = toolbar.findViewById(R.id.tv_image);
        tvVideos = findViewById(R.id.tv_videos);
        tvFeeds =  findViewById(R.id.tv_feed);
        tv_dot1 =  findViewById(R.id.tv_dot1);
        tv_dot2 =  findViewById(R.id.tv_dot2);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivChat = findViewById(R.id.ivChat);
        ImageView ivUserProfile = findViewById(R.id.ivUserProfile);
        ImageView ivDrawer = findViewById(R.id.btnNevMenu);
        ivUserProfile.setVisibility(View.GONE);
        ivDrawer.setVisibility(View.VISIBLE);

        //   final AppBarLayout mainView = findViewById(R.id.appbar);
        ivDrawer.setVisibility(View.VISIBLE);

        NavigationView navigationView =  findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        drawer =  findViewById(R.id.drawer_layout);
        // navigationView.setNavigationItemSelectedListener(this);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);;
        drawer.setScrimColor(getResources().getColor(android.R.color.transparent));
        navigationView.setItemIconTintList(null);
        //  navigationView.setNavigationItemSelectedListener(this);
        //  NavigationMenuView navMenuView = (NavigationMenuView) navigationView.getChildAt(0);
        //  navMenuView.addItemDecoration(new DividerItemDecoration(MyProfileActivity.this,
        //         DividerItemDecoration.VERTICAL));

        addItems();

        RecyclerView rycslidermenu = findViewById(R.id.rycslidermenu);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyProfileActivity.this);
        rycslidermenu.setLayoutManager(layoutManager);
        NavigationMenuAdapter listAdapter = new NavigationMenuAdapter(MyProfileActivity.this, navigationItems,drawer);

        rycslidermenu.setAdapter(listAdapter);

        final RelativeLayout rlContent = findViewById(R.id.rlContent);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                // mainView.setTranslationX(slideOffset * drawerView.getWidth());
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
                float slideX = drawerView.getWidth() * slideOffset;
                rlContent.setTranslationX(-slideX);
            }
        });
        TextView user_name = findViewById(R.id.user_name);
        CircleImageView user_image = findViewById(R.id.user_image);
        User user = Mualab.getInstance().getSessionManager().getUser();
        Picasso.with(MyProfileActivity.this).load(user.profileImage).placeholder(R.drawable.defoult_user_img).
                fit().into(user_image);

        user_name.setText(user.firstName+" "+user.lastName);


        rvFeed = findViewById(R.id.rvFeed);

        LinearLayout llFollowers = findViewById(R.id.llFollowers);
        LinearLayout llFollowing = findViewById(R.id.llFollowing);
        LinearLayout llPost = findViewById(R.id.llPost);

        tv_msg = findViewById(R.id.tv_msg);
        tv_no_data_msg = findViewById(R.id.tv_no_data_msg);
        ll_progress = findViewById(R.id.ll_progress);

        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(MyProfileActivity.this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new ArtistFeedAdapter(MyProfileActivity.this, feeds,  this);
        endlesScrollListener = new RecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if(feedAdapter!=null){
                    feedAdapter.showHideLoading(true);
                    apiForGetAllFeeds(page, 10, false);
                }
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {

            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);

        mRefreshLayout =  findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(MyProfileActivity.this);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                endlesScrollListener.resetState();
                isPulltoRefrash = true;
                apiForGetAllFeeds(0, 10, false);
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
            }
        });

        rvFeed.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if(e.getAction() == MotionEvent.ACTION_UP)
                    hideQuickView();
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent event) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }});


        lyImage.setOnClickListener(this);
        lyVideos.setOnClickListener(this);
        lyFeed.setOnClickListener(this);
        llFollowers.setOnClickListener(this);
        llFollowing.setOnClickListener(this);
        llPost.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        ivUserProfile.setOnClickListener(this);
        ivChat.setOnClickListener(this);
        ivDrawer.setOnClickListener(this);

        apiForGetProfile();
    }

    private void apiForGetProfile(){

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(MyProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetProfile();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("loginUserId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(MyProfileActivity.this, "getProfile", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    if(feeds!=null && feeds.size()==0)
                        updateViewType(R.id.ly_feeds);

                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(MyProfileActivity.this);
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray userDetail = js.getJSONArray("userDetail");
                        JSONObject object = userDetail.getJSONObject(0);
                        Gson gson = new Gson();

                        profileData = gson.fromJson(String.valueOf(object), UserProfileData.class);

                        //   profileData = gson.fromJson(response, UserProfileData.class);
                        setProfileData(profileData);
                        // updateViewType(profileData,R.id.ly_videos);

                    }else {
                        MyToast.getInstance(MyProfileActivity.this).showDasuAlert(message);
                    }
                    updateViewType(R.id.ly_feeds);

                } catch (Exception e) {
                    Progress.hide(MyProfileActivity.this);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    updateViewType(R.id.ly_feeds);
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
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(getClass().getName());
    }

    private void setProfileData(UserProfileData profileData){
        TextView  tv_ProfileName =  findViewById(R.id.tv_ProfileName);
        TextView   tv_username =  findViewById(R.id.tv_username);
        TextView   tvRatingCount =  findViewById(R.id.tvRatingCount);
        TextView   tv_distance =  findViewById(R.id.tv_distance);
        TextView   tv_profile_post =  findViewById(R.id.tv_profile_post);
        TextView  tv_profile_following =  findViewById(R.id.tv_profile_following);
        TextView  tv_profile_followers =  findViewById(R.id.tv_profile_followers);
        CircleImageView iv_Profile =  findViewById(R.id.iv_Profile);
        ImageView ivActive =  findViewById(R.id.ivActive);
        RatingBar rating =  findViewById(R.id.rating);

        if (profileData!=null){
            tv_ProfileName.setText(profileData.firstName+" "+profileData.lastName);
            tv_distance.setText(profileData.radius+" Miles");
            tv_username.setText("@"+profileData.userName);
            tv_profile_followers.setText(profileData.followersCount);
            tv_profile_following.setText(profileData.followingCount);
            tv_profile_post.setText(profileData.postCount);
            tvRatingCount.setText("("+profileData.reviewCount+")");
            float ratingCount = Float.parseFloat(profileData.ratingCount);
            rating.setRating(ratingCount);

            Picasso.with(MyProfileActivity.this).load(profileData.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(iv_Profile);

        }

    }

    private void updateViewType(int id) {
        tvVideos.setTextColor(getResources().getColor(R.color.text_color));
        tvImages.setTextColor(getResources().getColor(R.color.text_color));
        tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
        endlesScrollListener.resetState();
        int prevSize = feeds.size();
        switch (id) {
            case R.id.ly_feeds:
                //addRemoveHeader(true);
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));

                if (lastFeedTypeId != R.id.ly_feeds){
                    feeds.clear();
                    feedType = "";
                    CURRENT_FEED_STATE = Constant.FEED_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds(0, 10, true);
                }
                break;

            case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_images){
                    feeds.clear();
                    feedType = "image";
                    CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds( 0, 10, true);
                }

                break;

            case R.id.ly_videos:
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_videos){
                    feeds.clear();
                    feedType = "video";
                    CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    apiForGetAllFeeds( 0, 10, true);
                }
                break;
        }

        lastFeedTypeId = id;
    }

    private void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress){

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(MyProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetAllFeeds(page, feedLimit, isEnableProgress);
                    }

                }
            }).show();
        }


        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", String.valueOf(feedLimit));
        params.put("type", "");
        params.put("userId", String.valueOf(user.id));
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(MyProfileActivity.this, "profileFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                ll_progress.setVisibility(View.GONE);
                feedAdapter.showHideLoading(false);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        //removeProgress();
                        ParseAndUpdateUI(response);

                    }else MyToast.getInstance(MyProfileActivity.this).showSmallMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    // MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                ll_progress.setVisibility(View.GONE);
                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }
                //MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }})
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute(TAG);
        ll_progress.setVisibility(isEnableProgress?View.VISIBLE:View.GONE);
    }

    private void ParseAndUpdateUI(final String response) {

        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllFeeds");
                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(true, 500);
                    int prevSize = feeds.size();
                    feeds.clear();
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                }

                Gson gson = new Gson();
                for (int i = 0; i < array.length(); i++) {

                    try{
                        JSONObject jsonObject = array.getJSONObject(i);
                        Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);

                        /*tmp get data and set into actual json format*/
                        if(feed.userInfo!=null && feed.userInfo.size()>0){
                            Feeds.User user = feed.userInfo.get(0);
                            feed.userName = user.userName;
                            feed.fullName = user.firstName+" "+user.lastName;
                            feed.profileImage = user.profileImage;
                            feed.userId = user._id;
                            feed.crd =feed.timeElapsed;
                        }

                        if(feed.feedData!=null && feed.feedData.size()>0){

                            feed.feed = new ArrayList<>();
                            feed.feedThumb = new ArrayList<>();

                            for(Feeds.Feed tmp : feed.feedData){
                                feed.feed.add(tmp.feedPost);
                                if(!TextUtils.isEmpty(feed.feedData.get(0).videoThumb))
                                    feed.feedThumb.add(tmp.feedPost);
                            }

                            if(feed.feedType.equals("video"))
                                feed.videoThumbnail = feed.feedData.get(0).videoThumb;
                        }

                        feeds.add(feed);

                    }catch (JsonParseException e){
                        e.printStackTrace();
                        FirebaseCrash.log(e.getLocalizedMessage());
                    }

                } // loop end.

                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size()==0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setVisibility(View.VISIBLE);

                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }
                feedAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            feedAdapter.notifyDataSetChanged();
            //MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mualab.getInstance().cancelPendingRequests(TAG);
        tvImages = null;
        tvVideos = null;
        tvFeeds = null;
        tv_msg = null;
        ll_progress = null;
        endlesScrollListener = null;
        feedAdapter = null;
        feeds = null;
        rvFeed = null;
        user = null;
        profileData = null;
    }

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(MyProfileActivity.this, CommentsActivity.class);
        intent.putExtra("feed_id", feed._id);
        intent.putExtra("feedPosition", pos);
        intent.putExtra("feed", feed);
        startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
    }

    @Override
    public void onLikeListClick(Feeds feed) {
        addFragment(LikeFragment.newInstance(feed._id, user.id), true);
    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {
        publicationQuickView(feed, index);
    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            apiForGetProfile();
            //apiForGetAllFeeds(0, 10, true);
        } else if (data != null){
            if (requestCode == Constant.ACTIVITY_COMMENT) {
                if (CURRENT_FEED_STATE == Constant.FEED_STATE) {
                    int pos = data.getIntExtra("feedPosition", 0);
                    Feeds feed = (Feeds) data.getSerializableExtra("feed");
                    feeds.get(pos).commentCount = feed.commentCount;
                    feedAdapter.notifyItemChanged(pos);
                }
            }
        }
    }

    /* frangment replace code */
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in,0,0);
            transaction.add(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }

    }

    private Dialog builder;
    public void publicationQuickView(Feeds feeds, int index){
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate( R.layout.dialog_image_detail_view, null);

        ImageView postImage = view.findViewById(R.id.ivFeedCenter);
        ImageView profileImage =  view.findViewById(R.id.ivUserProfile);
        TextView tvUsername =  view.findViewById(R.id.txtUsername);
        tvUsername.setText(feeds.userName);

        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideQuickView();
            }
        });

        view.findViewById(R.id.tvUnfollow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyToast.getInstance(MyProfileActivity.this).showSmallCustomToast(getString(R.string.under_development));
            }
        });

        Picasso.with(MyProfileActivity.this).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        if(TextUtils.isEmpty(feeds.profileImage))
            Picasso.with(MyProfileActivity.this).load(R.drawable.defoult_user_img).noPlaceholder().into(profileImage);
        else Picasso.with(MyProfileActivity.this).load(feeds.profileImage).noPlaceholder().into(profileImage);

        builder = new Dialog(MyProfileActivity.this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //noinspection ConstantConditions
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);
        builder.setCancelable(true);
        builder.show();
    }

    public void hideQuickView(){
        if(builder != null) builder.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()){
            case R.id.ly_feeds:
            case R.id.ly_images:
            case R.id.ly_videos:
                updateViewType(view.getId());
                break;

            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnNevMenu :
                if (drawer.isDrawerOpen(GravityCompat.END)) {
                    drawer.closeDrawer(GravityCompat.END);
                }
                else {
                    drawer.openDrawer(GravityCompat.END);
                }
                break;

            case R.id.llAboutUs:
                MyToast.getInstance(MyProfileActivity.this).showDasuAlert("Under development");
                break;

            case R.id.ivChat:
                MyToast.getInstance(MyProfileActivity.this).showDasuAlert("Under development");
                break;

            case R.id.llFollowing:
               /* Intent intent1 = new Intent(MyProfileActivity.this,FollowersActivity.class);
                intent1.putExtra("isFollowers",false);
                intent1.putExtra("artistId",user.id);
                startActivityForResult(intent1,10);*/
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("isFollowers",false);
                bundle1.putString("artistId", String.valueOf(user.id));
                Intent intent1 = new Intent(MyProfileActivity.this,FollowersActivity.class);
                intent1.putExtras(bundle1);
                startActivityForResult(intent1,10);
                break;

            case R.id.llFollowers:
                Bundle bundle2 = new Bundle();
                bundle2.putBoolean("isFollowers",true);
                bundle2.putString("artistId", String.valueOf(user.id));
                Intent intent2 = new Intent(MyProfileActivity.this,FollowersActivity.class);
                intent2.putExtras(bundle2);
                startActivityForResult(intent2,10);
                //startActivity(new Intent(mContext,FollowersActivity.class));
                break;

            case R.id.llPost:
                updateViewType(R.id.ly_feeds);
                break;

        }
    }

    private void addItems(){
        NavigationItem item;
        for(int i=0;i<8;i++) {
            item = new NavigationItem();
            switch (i) {
                case 0:
                    item.itemName = getString(R.string.edit_profile);
                    item.itemImg = R.drawable.profile_ico;

                    break;
                case 1:
                    item.itemName =getString(R.string.inbox);
                    item.itemImg = R.drawable.chat_ico;

                    break;

                case 2:
                    item.itemName = getString(R.string.title_booking);
                    item.itemImg = R.drawable.booking_ico;
                    break;

                case 3:
                    item.itemName = getString(R.string.payment_history);
                    item.itemImg = R.drawable.payment_history_ico;
                    break;

                case 4:
                    item.itemName = getString(R.string.rate_this_app);
                    item.itemImg = R.drawable.rating_star_ico;
                    break;
                case 5:
                    item.itemName = getString(R.string.payment_info);
                    item.itemImg = R.drawable.payment_info_ico;
                    break;
                case 6:
                    item.itemName = getString(R.string.about_mualab);
                    item.itemImg = R.drawable.slider_about_us_ico;
                    break;
                case 7:
                    item.itemName = "Logout";
                    item.itemImg = R.drawable.logout_ico;

                    break;

            }
            navigationItems.add(item);
        }
    }


    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}
