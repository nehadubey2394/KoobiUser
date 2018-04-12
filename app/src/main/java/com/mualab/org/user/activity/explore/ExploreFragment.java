package com.mualab.org.user.activity.explore;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.BaseFragment;
import com.mualab.org.user.activity.BaseListner;
import com.mualab.org.user.activity.explore.adapter.ExploreGridViewAdapter;
import com.mualab.org.user.activity.feeds.adapter.LiveUserAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.listner.SearchViewListner;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.model.feeds.LiveUserInfo;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.ScreenUtils;
import com.mualab.org.user.util.WrapContentGridLayoutManager;
import com.mualab.org.user.util.decorator.GridDividerItemDecoration;
import com.mualab.org.user.util.decorator.ItemDecorationGridColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.refreshview.CircleHeaderView;
import views.refreshview.OnRefreshListener;
import views.refreshview.RjRefreshLayout;


/*Dharmraj Acarya*/
public class ExploreFragment extends BaseFragment implements View.OnClickListener,
        ExploreGridViewAdapter.Listener, LiveUserAdapter.Listner {

    public static String TAG = ExploreFragment.class.getName();

    private Context mContext;
    private BaseListner baseListner;

    private TextView tvImages, tvVideos, tv_msg;
    private LinearLayout ll_progress;
    private ProgressBar progress_bar;
    private RjRefreshLayout mRefreshLayout;

    /*Adapters*/
   // private LiveUserAdapter liveUserAdapter;
    //private ArrayList<LiveUserInfo> liveUserList;

    private ExploreGridViewAdapter feedAdapter;
    private RecyclerView rvFeed;
    private List<Feeds> feeds;
    private EndlessRecyclerViewScrollListener endlesScrollListener;

    private String feedType = "image";
    private int lastFeedTypeId;
    private boolean isPulltoRefrash;



    public ExploreFragment() {
        // Required empty public constructor
    }


    public static ExploreFragment newInstance() {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
       // args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if(context instanceof BaseListner){
            baseListner = (BaseListner) mContext;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Mualab.getInstance().cancelPendingRequests(TAG);
        baseListner = null;
        mContext = null;
        tvImages = null;
        tvVideos = null;
        tv_msg = null;
        ll_progress = null;
        endlesScrollListener = null;
        feedAdapter = null;
        //liveUserList = null;
        feeds = null;
        rvFeed = null;
        lastFeedTypeId = 0;
    }

    @Override
    public void onClickedUserStory(LiveUserInfo storyUser, int position) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feeds = new ArrayList<>();
        //liveUserList = new ArrayList<>();
       // liveUserList.clear();
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }*/
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_explore, container, false);
        initView(view);
        return view;
    }

    private void initView(View view){
        tvImages = view.findViewById(R.id.tvImages);
        tvVideos =  view.findViewById(R.id.tvVideos);
        //RecyclerView rvMyStory = view.findViewById(R.id.recyclerView);
        rvFeed = view.findViewById(R.id.rvFeed);
        tv_msg = view.findViewById(R.id.tv_msg);
        ll_progress = view.findViewById(R.id.ll_loadingBox);
        progress_bar = view.findViewById(R.id.progress_bar);

        view.findViewById(R.id.ly_images).setOnClickListener(this);
        view.findViewById(R.id.ly_videos).setOnClickListener(this);
        view.findViewById(R.id.searchView).setOnClickListener(this);
        //liveUserAdapter = new LiveUserAdapter(mContext, liveUserList, this);
        //rvMyStory.setAdapter(liveUserAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int mNoOfColumns = ScreenUtils.calculateNoOfColumns(mContext.getApplicationContext());
        WrapContentGridLayoutManager wgm = new WrapContentGridLayoutManager(mContext,
                mNoOfColumns<3?3:mNoOfColumns, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(wgm);
        rvFeed.setHasFixedSize(true);

        Drawable divider = ContextCompat.getDrawable(mContext, R.drawable.divider_transprant);
        //rvFeed.addItemDecoration(new GridDividerItemDecoration(divider, divider, 2));
       /* rvFeed.addItemDecoration(new ItemDecorationGridColumns(
                getResources().getDimensionPixelSize(R.dimen.grid_list_spacing),
                mNoOfColumns));*/

        feedAdapter = new ExploreGridViewAdapter(mContext, feeds, this);
        endlesScrollListener = new EndlessRecyclerViewScrollListener(wgm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                feedAdapter.showHideLoading(true);
                apiForGetAllFeeds(page, 10, false);
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);


        mRefreshLayout =  view.findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(getContext());
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

        /*rvFeed.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
            }});*/

        if(feeds!=null && feeds.size()==0){
            updateViewType(R.id.ly_images);
        } else feedAdapter.notifyDataSetChanged();
        //getStoryList();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ly_images:
            case R.id.ly_videos:
                updateViewType(view.getId());
                break;

            case R.id.searchView:
                startActivity(new Intent(mContext, ExplorSearchActivity.class));
                //baseListner.addFragment(ExploreSearchFragment.newInstance(), true);
                break;
        }
    }

    private void updateViewType(int id) {
        tvVideos.setTextColor(getResources().getColor(R.color.text_color));
        tvImages.setTextColor(getResources().getColor(R.color.text_color));
        endlesScrollListener.resetState();
        int prevSize = feeds.size();
        switch (id) {
            case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_images){
                    feeds.clear();
                    feedType = "image";
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    showLoading();
                    apiForGetAllFeeds( 0, 10, true);
                }
                break;

            case R.id.ly_videos:
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_videos){
                    feeds.clear();
                    feedType = "video";
                    feedAdapter.notifyItemRangeRemoved(0, prevSize);
                    showLoading();
                    apiForGetAllFeeds( 0, 10, true);
                }
                break;
        }

        lastFeedTypeId = id;
    }

    private void showLoading(){
        ll_progress.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tv_msg.setText(getString(R.string.loading));
    }

    /*private void getStoryList(){
        Map<String, String> params = new HashMap<>();
        // params.put("feedType", feedType);

        new HttpTask(new HttpTask.Builder(mContext, "getMyStoryUser", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray array = js.getJSONArray("myStoryList");

                        for (int i = 0; i < array.length(); i++) {
                            Gson gson = new Gson();
                            JSONObject jsonObject = array.getJSONObject(i);
                            LiveUserInfo live = gson.fromJson(String.valueOf(jsonObject), LiveUserInfo.class);

                            if(live.id == Mualab.currentUser.id){
                                LiveUserInfo me = liveUserList.get(0);
                                me.firstName = live.firstName;
                                me.lastName = live.lastName;
                                me.fullName = live.firstName+" "+live.lastName;
                                me.storyCount = live.storyCount;
                                me.userName = "You";

                            }else liveUserList.add(live);
                        }

                        liveUserAdapter.notifyDataSetChanged();
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    Progress.hide(mContext);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }})
                .setParam(params)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.APPLICATION_JSON))
                .execute(TAG);
    }*/

    private void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress){

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
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
        params.put("type", "explore");
        params.put("userId", ""+Mualab.currentUser.id);
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(mContext, "getAllFeeds", new HttpResponceListner.Listener() {
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
                    }else MyToast.getInstance(mContext).showSmallMessage(message);
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

                if(feeds.size()==0){
                    rvFeed.setVisibility(View.GONE);
                    tv_msg.setVisibility(View.VISIBLE);
                    tv_msg.setText(getString(R.string.no_data_found));
                }

                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size()==0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setText(getString(R.string.no_data_found));
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
    public void onFeedClick(Feeds feed, int index) {
       // baseListner.addFragment();
        Intent intent = new Intent(mContext, FeedDetailActivity.class);
        intent.putExtra("feed",feed);
        startActivity(intent);
        //MyToast.getInstance(mContext).showDasuAlert(getString(R.string.under_development));
    }
}
