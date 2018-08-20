package com.mualab.org.user.activity.artist_profile.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.adapter.ArtistFeedAdapter;
import com.mualab.org.user.activity.artist_profile.model.Followers;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.make_booking.BookingActivity;
import com.mualab.org.user.activity.feeds.CommentsActivity;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.activity.people_tag.instatag.InstaTag;
import com.mualab.org.user.activity.people_tag.instatag.TagToBeTagged;
import com.mualab.org.user.activity.people_tag.models.TagDetail;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
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
import com.mualab.org.user.utils.KeyboardUtil;
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

public class ArtistProfileActivity extends AppCompatActivity implements View.OnClickListener,ArtistFeedAdapter.Listener{
    private String artistId,TAG = this.getClass().getName();;
    private User user;
    private TextView tvImages,tvVideos,tvFeeds,tv_msg,tv_no_data_msg,tv_dot1,tv_dot2,tv_profile_following,tv_profile_followers;
    private ImageView iv_profile_back ,iv_profile_forward,ivActive,ivFav;
    private LinearLayout lowerLayout1,lowerLayout2,ll_progress;
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
    private ArtistsSearchBoard item;
    private AppCompatButton btnFollow;
    private ViewPagerAdapter.LongPressListner longPressListner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_profile);
        Intent i = getIntent();
        item = (ArtistsSearchBoard) i.getSerializableExtra("item");
        artistId =  item._id;
        init();
    }

    private void init(){
        Session session = Mualab.getInstance().getSessionManager();
        user = session.getUser();
        feeds = new ArrayList<>();

        tvImages = findViewById(R.id.tv_image);
        tvVideos = findViewById(R.id.tv_videos);
        tvFeeds =  findViewById(R.id.tv_feed);
        tv_dot1 =  findViewById(R.id.tv_dot1);
        tv_dot2 =  findViewById(R.id.tv_dot2);
        tv_profile_following =  findViewById(R.id.tv_profile_following);
        tv_profile_followers =  findViewById(R.id.tv_profile_followers);

        btnFollow = findViewById(R.id.btnFollow);
        AppCompatButton btnBook = findViewById(R.id.btnBook);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivChat = findViewById(R.id.ivChat);
        ivChat.setVisibility(View.VISIBLE);
        ivFav =  findViewById(R.id.ivFav);
        ivFav.setVisibility(View.VISIBLE);
        ImageView ivUserProfile = findViewById(R.id.ivUserProfile);

        rvFeed = findViewById(R.id.rvFeed);
        LinearLayout lyImage = findViewById(R.id.ly_images);
        LinearLayout lyVideos = findViewById(R.id.ly_videos);
        LinearLayout lyFeed = findViewById(R.id.ly_feeds);

        LinearLayout llServices = findViewById(R.id.llServices);
        LinearLayout llCertificate = findViewById(R.id.llCertificate);
        LinearLayout llAboutUs = findViewById(R.id.llAboutUs);
        LinearLayout llFollowers = findViewById(R.id.llFollowers);
        LinearLayout llFollowing = findViewById(R.id.llFollowing);
        LinearLayout llPost = findViewById(R.id.llPost);

        lowerLayout2 =  findViewById(R.id.lowerLayout2);
        lowerLayout1 =  findViewById(R.id.lowerLayout);

        //  ImageView profile_btton_back = (ImageView) view.findViewById(R.id.profile_btton_back);
        iv_profile_back =  findViewById(R.id.iv_profile_back);
        iv_profile_forward =  findViewById(R.id.iv_profile_forward);

        tv_msg = findViewById(R.id.tv_msg);
        tv_no_data_msg = findViewById(R.id.tv_no_data_msg);
        ll_progress = findViewById(R.id.ll_progress);

        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(ArtistProfileActivity.this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new ArtistFeedAdapter(ArtistProfileActivity.this, feeds,  this);
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
        final CircleHeaderView header = new CircleHeaderView(ArtistProfileActivity.this);
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
        llServices.setOnClickListener(this);
        llCertificate.setOnClickListener(this);
        llAboutUs.setOnClickListener(this);
        llFollowers.setOnClickListener(this);
        llFollowing.setOnClickListener(this);
        llPost.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnFollow.setOnClickListener(this);
        btnBook.setOnClickListener(this);
        ivUserProfile.setOnClickListener(this);
        ivChat.setOnClickListener(this);
        ivFav.setOnClickListener(this);
        //  profile_btton_back.setOnClickListener(this);
        iv_profile_back.setOnClickListener(this);
        iv_profile_forward.setOnClickListener(this);

        iv_profile_back.setEnabled(false);
        iv_profile_forward.setEnabled(true);

        apiForGetProfile();
    }

    private void apiForGetProfile(){

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
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
        params.put("userId", artistId);
        params.put("loginUserId", String.valueOf(user.id));
        params.put("viewBy", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "getProfile", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    if(feeds!=null && feeds.size()==0)
                        updateViewType(R.id.ly_feeds);

                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(ArtistProfileActivity.this);
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray userDetail = js.getJSONArray("userDetail");
                        JSONObject object = userDetail.getJSONObject(0);
                        Gson gson = new Gson();

                        profileData = gson.fromJson(String.valueOf(object), UserProfileData.class);
                        item.businessType = profileData.businessType;
                        //   profileData = gson.fromJson(response, UserProfileData.class);
                        setProfileData(profileData);
                        // updateViewType(profileData,R.id.ly_videos);

                    }else {
                        MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert(message);
                    }
                    updateViewType(R.id.ly_feeds);

                } catch (Exception e) {
                    Progress.hide(ArtistProfileActivity.this);
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

            case R.id.ivFav:
                if (profileData.favoriteStatus.equals("1")) {
                    profileData.favoriteStatus = "0";
                    ivFav.setImageDrawable(getResources().getDrawable(R.drawable.inactive_like_ico));
                }
                else {
                    profileData.favoriteStatus = "1";
                    ivFav.setImageDrawable(getResources().getDrawable(R.drawable.active_like_ico));
                }
                apiForFavourite();
                break;

            case R.id.btnBook:
                Intent intent = new Intent(ArtistProfileActivity.this, BookingActivity.class);
                intent.putExtra("item",item);
                startActivity(intent);
                break;

            case R.id.btnFollow:
                int followersCount = Integer.parseInt(profileData.followersCount);

                if (profileData.followerStatus.equals("1")) {
                    profileData.followerStatus = "0";
                    btnFollow.setText("Follow");
                    followersCount--;
                }
                else {
                    profileData.followerStatus = "1";
                    btnFollow.setText("Unfollow");
                    followersCount++;
                }
                profileData.followersCount = String.valueOf(followersCount);
                tv_profile_followers.setText(""+followersCount);
                apiForGetFollowUnFollow();
                break;

            case R.id.llServices:
                Intent intent4 = new Intent(ArtistProfileActivity.this, ArtistServicesActivity.class);
                intent4.putExtra("artistId",artistId);
                startActivity(intent4);
                break;

            case R.id.llAboutUs:
                MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert("Under development");
                break;

            case R.id.ivChat:
                MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert("Under development");
                break;

            case R.id.llCertificate:
                Intent intent3 = new Intent(ArtistProfileActivity.this, CertificateActivity.class);
                intent3.putExtra("artistId",artistId);
                startActivityForResult(intent3, 10);
                break;

            case R.id.llFollowing:
                Intent intent1 = new Intent(ArtistProfileActivity.this,FollowersActivity.class);
                intent1.putExtra("isFollowers",false);
                intent1.putExtra("artistId",artistId);
                startActivityForResult(intent1,10);
                break;

            case R.id.llFollowers:
                Intent intent2 = new Intent(ArtistProfileActivity.this,FollowersActivity.class);
                intent2.putExtra("isFollowers",true);
                intent2.putExtra("artistId",artistId);
                startActivityForResult(intent2,10);
                //startActivity(new Intent(mContext,FollowersActivity.class));
                break;

            case R.id.llPost:
                updateViewType(R.id.ly_feeds);
                break;

            case R.id.iv_profile_back:
                iv_profile_back.setEnabled(false);
                iv_profile_forward.setEnabled(true);
                iv_profile_back.setColorFilter(ContextCompat.getColor(ArtistProfileActivity.this, R.color.gray), android.graphics.PorterDuff.Mode.MULTIPLY);
                iv_profile_forward.setColorFilter(ContextCompat.getColor(ArtistProfileActivity.this, R.color.text_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                Animation anim = AnimationUtils.loadAnimation(ArtistProfileActivity.this, R.anim.move_left);
                lowerLayout1.startAnimation(anim);
                lowerLayout2.setVisibility(View.GONE);
                lowerLayout1.setVisibility(View.VISIBLE);
                tv_dot1.setBackgroundResource(R.drawable.black_circle);
                tv_dot2.setBackgroundResource(R.drawable.bg_blank_black_circle);
                break;
            case R.id.iv_profile_forward:
                iv_profile_back.setEnabled(true);
                iv_profile_forward.setEnabled(false);
                iv_profile_forward.setColorFilter(ContextCompat.getColor(ArtistProfileActivity.this, R.color.gray), android.graphics.PorterDuff.Mode.MULTIPLY);
                iv_profile_back.setColorFilter(ContextCompat.getColor(ArtistProfileActivity.this, R.color.text_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                Animation anim2 = AnimationUtils.loadAnimation(ArtistProfileActivity.this, R.anim.move_right);
                lowerLayout2.startAnimation(anim2);
                lowerLayout1.setVisibility(View.GONE);
                lowerLayout2.setVisibility(View.VISIBLE);
                tv_dot1.setBackgroundResource(R.drawable.bg_blank_black_circle);
                tv_dot2.setBackgroundResource(R.drawable.black_circle);
                break;
        }
    }

    private void setProfileData(UserProfileData profileData){
        TextView  tv_ProfileName =  findViewById(R.id.tv_ProfileName);
        TextView   tv_username =  findViewById(R.id.tv_username);
        TextView   tvRatingCount =  findViewById(R.id.tvRatingCount);
        TextView   tv_distance =  findViewById(R.id.tv_distance);
        TextView   tv_profile_post =  findViewById(R.id.tv_profile_post);
        TextView  tvServiceCount =  findViewById(R.id.tvServiceCount);
        TextView  tvCertificateCount = findViewById(R.id.tvCertificateCount);
        CircleImageView iv_Profile =  findViewById(R.id.iv_Profile);
        ImageView ivActive =  findViewById(R.id.ivActive);
        RatingBar rating =  findViewById(R.id.rating);

        if (profileData!=null){
            if (profileData.favoriteStatus.equals("1")) {
                ivFav.setImageDrawable(getResources().getDrawable(R.drawable.active_like_ico));
            }
            else {
                ivFav.setImageDrawable(getResources().getDrawable(R.drawable.inactive_like_ico));
            }

            if (profileData.followerStatus.equals("1")) {
                btnFollow.setText("Unfollow");
            }
            else {
                btnFollow.setText("Follow");
            }


            tv_ProfileName.setText(profileData.firstName+" "+profileData.lastName);
            tv_distance.setText(profileData.radius+" Miles");
            tv_username.setText("@"+profileData.userName);
            tv_profile_followers.setText(profileData.followersCount);
            tv_profile_following.setText(profileData.followingCount);
            tv_profile_post.setText(profileData.postCount);
            tvRatingCount.setText("("+profileData.reviewCount+")");
            tvCertificateCount.setText(profileData.certificateCount);
            tvServiceCount.setText(profileData.serviceCount);
            rating.setRating(Float.parseFloat(profileData.ratingCount));

            if (profileData.isCertificateVerify.equals("1")){
                ivActive.setVisibility(View.VISIBLE);
            }else ivActive.setVisibility(View.GONE);

            if (!profileData.profileImage.isEmpty() && !profileData.profileImage.equals("")) {
                Picasso.with(ArtistProfileActivity.this).load(profileData.profileImage).placeholder(R.drawable.defoult_user_img).
                        fit().into(iv_Profile);
            }

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
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
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
        params.put("userId", artistId);
        params.put("viewBy", "user");
        params.put("loginUserId", String.valueOf(user.id));
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "profileFeed", new HttpResponceListner.Listener() {
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

                    }
                    else if (page==0) {
                        rvFeed.setVisibility(View.GONE);
                        tv_no_data_msg.setVisibility(View.VISIBLE);
                    }
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
                if (array.length()!=0){
                    tv_no_data_msg.setVisibility(View.GONE);
                    for (int i = 0; i < array.length(); i++) {

                        try{
                            JSONObject jsonObject = array.getJSONObject(i);
                            Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);
                            //   feed.taggedImgMap = new HashMap<>();
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

                            JSONArray jsonArray = jsonObject.getJSONArray("peopleTag");
                            if (jsonArray.length() != 0){

                                for (int j = 0; j < jsonArray.length(); j++) {

                                    feed.peopleTagList = new ArrayList<>();
                                    JSONArray arrayJSONArray = jsonArray.getJSONArray(j);

                                    for (int k = 0; k < arrayJSONArray.length(); k++) {
                                        JSONObject object = arrayJSONArray.getJSONObject(k);

                                        HashMap<String,TagDetail> tagDetails = new HashMap<>();

                                        String unique_tag_id = object.getString("unique_tag_id");
                                        double x_axis = Double.parseDouble(object.getString("x_axis"));
                                        double y_axis = Double.parseDouble(object.getString("y_axis"));

                                        JSONObject tagOjb = object.getJSONObject("tagDetails");
                                        TagDetail tag ;
                                        if (tagOjb.has("tabType")){
                                            tag = gson.fromJson(String.valueOf(tagOjb), TagDetail.class);
                                        }else {
                                            JSONObject details = tagOjb.getJSONObject(unique_tag_id);
                                            tag = gson.fromJson(String.valueOf(details), TagDetail.class);
                                        }
                                        tagDetails.put(tag.title, tag);
                                        TagToBeTagged tagged = new TagToBeTagged();
                                        tagged.setUnique_tag_id(unique_tag_id);
                                        tagged.setX_co_ord(x_axis);
                                        tagged.setY_co_ord(y_axis);
                                        tagged.setTagDetails(tagDetails);

                                        feed.peopleTagList.add(tagged);
                                    }
                                    feed.taggedImgMap.put(j,feed.peopleTagList);
                                }
                            }

                            feeds.add(feed);

                        }catch (JsonParseException e){
                            e.printStackTrace();
                            FirebaseCrash.log(e.getLocalizedMessage());
                        }
                    } // loop end.
                }else if (feeds.size()==0){
                    rvFeed.setVisibility(View.GONE);
                    tv_no_data_msg.setVisibility(View.VISIBLE);
                }

                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size()==0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setVisibility(View.VISIBLE);

                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }
                feedAdapter.notifyDataSetChanged();
            }else {
                rvFeed.setVisibility(View.GONE);
                tv_no_data_msg.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            feedAdapter.notifyDataSetChanged();
            //MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }
    }

    private void apiForGetFollowUnFollow(){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetFollowUnFollow();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("followerId", artistId);

        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        /*if (profileData.followerStatus.equals("1")) {
                            profileData.followerStatus = "0";
                            btnFollow.setText("Follow");
                        }
                        else {
                            profileData.followerStatus = "1";
                            btnFollow.setText("Unfollow");
                        }*/
                    }else {
                        MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(ArtistProfileActivity.this);
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
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void apiForFavourite(){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ArtistProfileActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForFavourite();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("artistId", artistId);
        if (profileData.favoriteStatus.equals("1"))
            params.put("type", "favorite");
        else
            params.put("type", "unfavorite");


        HttpTask task = new HttpTask(new HttpTask.Builder(ArtistProfileActivity.this, "addFavorite", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                       /* if (profileData.favoriteStatus.equals("1")) {
                            profileData.favoriteStatus = "0";
                            ivFav.setImageDrawable(getResources().getDrawable(R.drawable.inactive_like_ico));
                        }
                        else {
                            profileData.favoriteStatus = "1";
                            ivFav.setImageDrawable(getResources().getDrawable(R.drawable.active_like_ico));
                        }*/
                    }else {
                        MyToast.getInstance(ArtistProfileActivity.this).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(ArtistProfileActivity.this);
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
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void ParseAndUpdateUI(final int page,final String response) {

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
                if (array.length()!=0){
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
                        }
                    }
                }else if (page==0 || feeds.size()==0){
                    tv_no_data_msg.setVisibility(View.VISIBLE);
                    rvFeed.setVisibility(View.GONE);
                }
                // loop end.

                feedAdapter.notifyDataSetChanged();
                //updateViewType(R.id.ly_feeds);

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
        item = null;
        user = null;
        profileData = null;
    }

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(ArtistProfileActivity.this, CommentsActivity.class);
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
        //publicationQuickView(feed, index);
        showLargeImage(feed,index);
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
                if(CURRENT_FEED_STATE == Constant.FEED_STATE){
                    int pos = data.getIntExtra("feedPosition",0);
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
                MyToast.getInstance(ArtistProfileActivity.this).showSmallCustomToast(getString(R.string.under_development));
            }
        });

        Picasso.with(ArtistProfileActivity.this).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        if(TextUtils.isEmpty(feeds.profileImage))
            Picasso.with(ArtistProfileActivity.this).load(R.drawable.defoult_user_img).noPlaceholder().into(profileImage);
        else Picasso.with(ArtistProfileActivity.this).load(feeds.profileImage).noPlaceholder().into(profileImage);

        builder = new Dialog(ArtistProfileActivity.this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //noinspection ConstantConditions
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);
        builder.setCancelable(true);
        builder.show();
    }

    boolean isShow = false;
    private void showLargeImage(Feeds feeds, int index){
        View dialogView = View.inflate(ArtistProfileActivity.this, R.layout.dialog_large_image_view, null);
        final Dialog dialog = new Dialog(ArtistProfileActivity.this,android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.InOutAnimation;
        dialog.setContentView(dialogView);
        final InstaTag postImage = dialogView.findViewById(R.id.post_image);
        ImageView btnBack = dialogView.findViewById(R.id.btnBack);
        TextView tvCertiTitle = dialogView.findViewById(R.id.tvCertiTitle);
        tvCertiTitle.setText("Images");

        postImage.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        postImage.setRootWidth(postImage.getMeasuredWidth());
        postImage.setRootHeight(postImage.getMeasuredHeight());

        if (feeds.feed.get(index)!=null){
            Glide.with(ArtistProfileActivity.this).load(feeds.feed.get(index)).placeholder(R.drawable.gallery_placeholder)
                    .skipMemoryCache(false).into(postImage.getTagImageView());
        }

        postImage.setImageToBeTaggedEvent(taggedImageEvent);

        ArrayList<TagToBeTagged>tags =  feeds.taggedImgMap.get(index);
        if (tags!=null && tags.size()!=0){
            postImage.addTagViewFromTagsToBeTagged(tags,false);
            postImage.hideTags();
        }

        //   Picasso.with(mContext).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        longPressListner = new ViewPagerAdapter.LongPressListner() {
            @Override
            public void onLongPress() {
                if (!isShow) {
                    isShow = true;
                    postImage.showTags();
                }
                else {
                    isShow = false;
                    postImage.hideTags();
                }

            }
        };

        dialog.show();
    }

    private InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {
        @Override
        public void singleTapConfirmedAndRootIsInTouch(int x, int y) {
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (longPressListner != null)
                longPressListner.onLongPress();
        }

        @Override
        public void onSinglePress(MotionEvent e) {
        }
    };


    public void hideQuickView(){
        if(builder != null) builder.dismiss();
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
