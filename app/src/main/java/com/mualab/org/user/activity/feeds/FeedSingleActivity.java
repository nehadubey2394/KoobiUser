package com.mualab.org.user.activity.feeds;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseListner;
import com.mualab.org.user.activity.explore.fragment.FeedDetailFragment;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.adapter.LiveUserAdapter;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.activity.feeds.fragment.SingleFeedLikeFragment;
import com.mualab.org.user.activity.people_tag.instatag.InstaTag;
import com.mualab.org.user.activity.people_tag.instatag.TagToBeTagged;
import com.mualab.org.user.activity.people_tag.models.TagDetail;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.feeds.Feeds;
import com.mualab.org.user.data.model.feeds.LiveUserInfo;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listner.FeedsListner;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedSingleActivity extends AppCompatActivity implements View.OnClickListener,FeedAdapter.Listener, LiveUserAdapter.Listner,BaseListner,FeedsListner  {
    private FeedAdapter adapter;
    private Feeds feed;
    private List<Feeds> list = new ArrayList<>();
    private Feeds feeds;
    private LinearLayout ll_progress;
    private BaseListner feedsListner;
    private TextView tvHeaderTitle;
    private ViewPagerAdapter.LongPressListner longPressListner;
    private boolean isShow = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Intent intent=getIntent();
        if (intent!=null){
            String feedId = intent.getStringExtra("feedId");
            feed=new Feeds();
            feed._id=Integer.parseInt(feedId);
        }

        feedsListner = (BaseListner) FeedSingleActivity.this;

        init();
        getUpdatedFeed(true);

    }

    @SuppressLint("SetTextI18n")
    public void init(){
        RecyclerView rvFeed =findViewById(R.id.rvFeed);
        ll_progress = findViewById(R.id.ll_progress);
        ImageView btnBack = findViewById(R.id.btnBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.text_post);
        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(FeedSingleActivity.this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);
        adapter=new FeedAdapter(FeedSingleActivity.this,list,this);
        rvFeed.setAdapter(adapter);
        rvFeed.scrollToPosition(0);
        if(feeds!=null){
            String title;
            if(feeds.feedType.equals("image"))
                title = "Image";
            else title = "Video";
            setHeaderTitle(title);
            addFragment(FeedDetailFragment.newInstance(0,list), false);
        }
        btnBack.setOnClickListener(this);

    }


    private void getUpdatedFeed(final boolean enableprogress){

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(FeedSingleActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        getUpdatedFeed(true);
                    }

                }
            }).show();
        }

        Map<String, String> map = new HashMap<>();
        map.put("feedId", ""+feed._id);
        map.put("userId", ""+Mualab.currentUser.id);
        Mualab.getInstance().getRequestQueue().cancelAll("feed"+feed._id);
        new HttpTask(new HttpTask.Builder(FeedSingleActivity.this, "feedDetails", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    // String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        list.clear();
                        ll_progress.setVisibility(View.GONE);
                       /* if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(true, 500);
                        }*/
                        JSONArray array = js.getJSONArray("feedDetail");
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
                                if (jsonArray!=null && jsonArray.length() != 0) {

                                    for (int j = 0; j < jsonArray.length(); j++) {

                                        feed.peopleTagList = new ArrayList<>();

                                        JSONArray arrayJSONArray = jsonArray.getJSONArray(j);

                                        if (arrayJSONArray != null && arrayJSONArray.length() != 0) {

                                            for (int k = 0; k < arrayJSONArray.length(); k++) {
                                                JSONObject object = arrayJSONArray.getJSONObject(k);

                                                HashMap<String, TagDetail> tagDetails = new HashMap<>();

                                                String unique_tag_id = object.getString("unique_tag_id");
                                                double x_axis = Double.parseDouble(object.getString("x_axis"));
                                                double y_axis = Double.parseDouble(object.getString("y_axis"));

                                                JSONObject tagOjb = object.getJSONObject("tagDetails");
                                                TagDetail tag;
                                                if (tagOjb.has("tabType")) {
                                                    tag = gson.fromJson(String.valueOf(tagOjb), TagDetail.class);
                                      /*  tag.tabType = tagOjb.getString("tabType");
                                        tag.tagId = tagOjb.getString("tagId");
                                        tag.title = tagOjb.getString("title");
                                        tag.userType = tagOjb.getString("userType");*/
                                                } else {
                                                    JSONObject details = tagOjb.getJSONObject(unique_tag_id);
                                                    tag = gson.fromJson(String.valueOf(details), TagDetail.class);
                                      /*  tag.tabType = details.getString("tabType");
                                        tag.tagId = details.getString("tagId");
                                        tag.title = details.getString("title");
                                        tag.userType = details.getString("userType");*/
                                                }
                                                tagDetails.put(tag.title, tag);
                                                TagToBeTagged tagged = new TagToBeTagged();
                                                tagged.setUnique_tag_id(unique_tag_id);
                                                tagged.setX_co_ord(x_axis);
                                                tagged.setY_co_ord(y_axis);
                                                tagged.setTagDetails(tagDetails);

                                                feed.peopleTagList.add(tagged);
                                            }
                                            feed.taggedImgMap.put(j, feed.peopleTagList);
                                        }
                                    }

                                }


                                list.add(feed);

                            }catch (JsonParseException e){
                                e.printStackTrace();
                                FirebaseCrash.log(e.getLocalizedMessage());
                            }

                        } // loop end.

                        adapter.notifyDataSetChanged();

                    } else if (status.equals("fail")) {
                        /*if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }*/
                        adapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
               /* if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }*/

                ll_progress.setVisibility(enableprogress?View.VISIBLE:View.GONE);
            }
        }).setParam(map)).execute("feed"+feed._id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }



    private void showLargeImage(Feeds feeds, int index){
        View dialogView = View.inflate(FeedSingleActivity.this, R.layout.dialog_large_image_view, null);
        final Dialog dialog = new Dialog(FeedSingleActivity.this,android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
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

        Glide.with(FeedSingleActivity.this).load(feeds.feed.get(index)).placeholder(R.drawable.gallery_placeholder)
                .into(postImage.getTagImageView());

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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tvHeaderTitle.setText(R.string.text_post);
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

    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {
        Intent intent = new Intent(FeedSingleActivity.this, CommentsActivity.class);
        intent.putExtra("feed_id", feed._id);
        intent.putExtra("feedPosition", pos);
        intent.putExtra("feed", feed);
        startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
    }

    @Override
    public void onLikeListClick(Feeds feed) {
        replaceFragment(SingleFeedLikeFragment.newInstance(feed._id,Mualab.currentUser.id),true);


    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {
        showLargeImage(feed,index);
    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {

    }

    @Override
    public void onClickedUserStory(LiveUserInfo storyUser, int position) {

    }

/*    private void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_in,0,0);
           *//* transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left);*//*
            transaction.add(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }*/

    @Override
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_in,0,0);
           /* transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left);*/
            transaction.add(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_in,0,0);
           /* transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left);*/
            transaction.replace(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void backPress() {

    }

    @Override
    public void getUpdatedFeed(int feedId) {

    }

    @Override
    public void apiForLikes(Feeds feeds) {
        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("feedId", ""+feed._id);
        map.put("likeById", ""+Mualab.currentUser.id);
        map.put("userId", ""+feed.userId);
        map.put("type", "feed");// feed or comment
        Mualab.getInstance().getRequestQueue().cancelAll("like"+feed._id);

        new HttpTask(new HttpTask.Builder(this, "like", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("LIKE:",response);
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        }).setParam(map)).execute("like"+feed._id);
    }

    @Override
    public void setHeaderTitle(String title) {

    }

    @Override
    public void showToast(String txt) {

    }

    public Fragment addFragmentN(Fragment fragmentHolder) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            String fragmentName = fragmentHolder.getClass().getName();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            //fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setEnterTransition(null);
            }
            fragmentTransaction.add(R.id.container, fragmentHolder, fragmentName).addToBackStack(fragmentName);
            fragmentTransaction.commit();

            return fragmentHolder;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }



}
