package com.mualab.org.user.activity.feeds;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseListner;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.adapter.LiveUserAdapter;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.feeds.Feeds;
import com.mualab.org.user.data.model.feeds.LiveUserInfo;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.listner.FeedsListner;
import com.mualab.org.user.utils.WrapContentLinearLayoutManager;
import com.mualab.org.user.utils.constants.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedSingleActivity extends AppCompatActivity implements View.OnClickListener,FeedAdapter.Listener, LiveUserAdapter.Listner {
    private FeedAdapter adapter;
    private Feeds feed;
    private List<Feeds> list = new ArrayList<>();
    private Feeds feeds;

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

        init();
        getUpdatedFeed();
    }


    public void init(){
        RecyclerView rvFeed =findViewById(R.id.rvFeed);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Post");
        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(FeedSingleActivity.this, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);
        adapter = new FeedAdapter(FeedSingleActivity.this, list, new FeedAdapter.Listener() {
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

            }

            @Override
            public void onFeedClick(Feeds feed, int index, View v) {

            }

            @Override
            public void onClickProfileImage(Feeds feed, ImageView v) {

            }
        });

        rvFeed.setAdapter(adapter);
        rvFeed.scrollToPosition(0);

        btnBack.setOnClickListener(this);

    }


    private void getUpdatedFeed(){
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

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
        addFragment(LikeFragment.newInstance(feed._id, Mualab.currentUser.id), true);
    }

    @Override
    public void onFeedClick(Feeds feed, int index, View v) {

    }

    @Override
    public void onClickProfileImage(Feeds feed, ImageView v) {

    }

    @Override
    public void onClickedUserStory(LiveUserInfo storyUser, int position) {

    }

    private void addFragment(Fragment fragment, boolean addToBackStack) {
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
}
