package com.mualab.org.user.activity.explore.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.mualab.org.user.activity.explore.FeedDetailActivity;
import com.mualab.org.user.activity.explore.adapter.ExploreGridViewAdapter;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.ScreenUtils;
import com.mualab.org.user.util.WrapContentGridLayoutManager;

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


public class SearchFeedFragment extends Fragment implements ExploreGridViewAdapter.Listener {
    public static String TAG = SearchFeedFragment.class.getName();

    private Context mContext;
    private TextView tv_msg;
    private ProgressBar progress_bar;
    private LinearLayout ll_progress;
    private RecyclerView rvFeed;
    private RjRefreshLayout mRefreshLayout;
    private EndlessRecyclerViewScrollListener endlesScrollListener;
    private List<Feeds> feeds;
    private ExploreGridViewAdapter feedAdapter;

   // private int fragCount;
    private ExSearchTag exSearchTag;
    private boolean isPulltoRefrash;


    public SearchFeedFragment() {
        // Required empty public constructor
    }


    public static SearchFeedFragment newInstance(ExSearchTag searchKey) {
        SearchFeedFragment fragment = new SearchFeedFragment();
        Bundle args = new Bundle();
        args.putSerializable("searchKey", searchKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feeds = new ArrayList<>();
        if (getArguments() != null) {
            //fragCount = getArguments().getInt("fragCount");
            exSearchTag = (ExSearchTag) getArguments().getSerializable("searchKey");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_feed, container, false);
    }

    private void initView(View view){
        rvFeed = view.findViewById(R.id.rvFeed);
        tv_msg = view.findViewById(R.id.tv_msg);
        progress_bar = view.findViewById(R.id.progress_bar);
        ll_progress = view.findViewById(R.id.ll_loadingBox);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        int mNoOfColumns = ScreenUtils.calculateNoOfColumns(mContext.getApplicationContext());
        WrapContentGridLayoutManager wgm = new WrapContentGridLayoutManager(mContext,
                mNoOfColumns<3?3:mNoOfColumns, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(wgm);
        rvFeed.setHasFixedSize(true);

       /* Drawable divider = ContextCompat.getDrawable(mContext, R.drawable.divider_transprant);
        rvFeed.addItemDecoration(new GridDividerItemDecoration(divider, divider, mNoOfColumns));*/

        feedAdapter = new ExploreGridViewAdapter(mContext, feeds, this);
        endlesScrollListener = new EndlessRecyclerViewScrollListener(wgm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                feedAdapter.showHideLoading(true);
                searchFeed(page, false);
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
                searchFeed(0, false);
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "onLoadMore: ");
            }
        });

        showLoading();
        searchFeed(0, true);
    }

    private void showLoading(){
        ll_progress.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tv_msg.setText(getString(R.string.loading));
    }


    @Override
    public void onFeedClick(Feeds feed, int index) {
        startActivity(new Intent(mContext, FeedDetailActivity.class).putExtra("feed",feed));
    }



    /*Api call and parse methods */
    private void searchFeed(final int page, final boolean isEnableProgress){

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        searchFeed(page, isEnableProgress);
                    }

                }
            }).show();
        }


        Map<String, String> params = new HashMap<>();
        if(exSearchTag.type == ExSearchTag.SearchType.TOP || exSearchTag.type == ExSearchTag.SearchType.PEOPLE){
            params.put("userId", ""+exSearchTag.id);
            params.put("findData", ""+exSearchTag.id);
        }else {
            params.put("userId", ""+Mualab.currentUser.id);
            params.put("findData", ""+exSearchTag.title.replace("#",""));
        }
        params.put("type", exSearchTag.getType());
        params.put("feedType", "");
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", "20");
        // params.put("appType", "user");
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(mContext, "userFeed", new HttpResponceListner.Listener() {
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
        progress_bar.setVisibility(View.GONE);
        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");

            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllUserFeeds");
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

                if(feeds.size()==0){
                    rvFeed.setVisibility(View.GONE);
                    ll_progress.setVisibility(View.VISIBLE);
                    tv_msg.setVisibility(View.VISIBLE);
                    tv_msg.setText(getString(R.string.no_data_found));
                }



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
            progress_bar.setVisibility(View.GONE);
            tv_msg.setText(getString(R.string.msg_some_thing_went_wrong));
            feedAdapter.notifyDataSetChanged();
        }
    }

}
