package com.mualab.org.user.activity.explore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.fragment.FeedDetailFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.listner.FeedsListner;
import com.mualab.org.user.data.model.feeds.Feeds;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.StatusBarUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.refreshview.RjRefreshLayout;

public class FeedDetailActivity extends AppCompatActivity implements FeedsListner{

    private FragmentManager fm;
    private List<Feeds> list;
    private Feeds feeds;
    private TextView tvTitle;
    private String title;
    private int index;

    private RjRefreshLayout mRefreshLayout;
    private boolean isPulltoRefrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);
        //  StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        if (args != null) {
            feeds = (Feeds) args.getSerializable("feed");
            index =  args.getInt("index", 0);
            list = (List<Feeds>) args.getSerializable("feeds");
            //feeds = (Feeds) intent.getExtras().getSerializable(" feed");
        }

        tvTitle = findViewById(R.id.tvHeaderTitle);
        // list = (List<Feeds>) args.getParcelableArrayList("feeds");


        if(feeds!=null){
            if(feeds.feedType.equals("image"))
                title = "Image";
            else title = "Video";
            setHeaderTitle(title);
            addFragment(FeedDetailFragment.newInstance(index,list), false);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        fm = getSupportFragmentManager();

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

       /* mRefreshLayout =  findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(this);
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPulltoRefrash = true;
                getUpdatedFeed(feeds._id);
            }

            @Override
            public void onLoadMore() {
            }
        });*/
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //setResult();
    }

    @Override
    public void setHeaderTitle(String title) {
        tvTitle.setText(title);
    }

    @Override
    public void showToast(String txt) {
        MyToast.getInstance(this).showDasuAlert(txt);
    }


    @Override
    public void onBackPressed() {
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            if(i==1) setHeaderTitle(title);
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_in,0,0);
            transaction.setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_left);
            transaction.add(R.id.container, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        while (i > 0) {
            fm.popBackStackImmediate();
            i--;
        }
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void backPress() {
        onBackPressed();
    }

    @Override
    public void getUpdatedFeed(int feedId) {

    }

    @Override
    public void apiForLikes(Feeds feed) {
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
}
