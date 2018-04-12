package com.mualab.org.user.activity.explore.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.CommentsActivity;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.listner.FeedsListner;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.WrapContentLinearLayoutManager;

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

import static android.app.Activity.RESULT_OK;

public class FeedDetailFragment extends Fragment {

    private Context mContext;
    private RjRefreshLayout mRefreshLayout;
    private FeedAdapter adapter;
    private Feeds feed;
    private Uri uri;
    private FeedsListner feedsListner;
    private List<Feeds> list = new ArrayList<>();


    private boolean isPulltoRefrash;


    public FeedDetailFragment() {
        // Required empty public constructor
    }


    public static FeedDetailFragment newInstance(String feedId, Feeds feed) {
        FeedDetailFragment fragment = new FeedDetailFragment();
        Bundle args = new Bundle();
        args.putString("uri", feedId);
        args.putSerializable("feed", feed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        
        if(context instanceof FeedsListner){
            feedsListner = (FeedsListner) context;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list.clear();
        if (getArguments() != null) {
            feed = (Feeds) getArguments().getSerializable("feed");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvFeed = view.findViewById(R.id.rvFeed);
        WrapContentLinearLayoutManager lm = new WrapContentLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(null);
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        mRefreshLayout =  view.findViewById(R.id.mSwipeRefreshLayout);
        final CircleHeaderView header = new CircleHeaderView(getContext());
        mRefreshLayout.addHeader(header);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                isPulltoRefrash = true;
                getUpdatedFeed();
            }

            @Override
            public void onLoadMore() {
            }
        });

        list.add(feed);
        adapter = new FeedAdapter(mContext, list, new FeedAdapter.Listener() {
            @Override
            public void onCommentBtnClick(Feeds feed, int pos) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("feed_id", feed._id);
                intent.putExtra("feedPosition", 0);
                intent.putExtra("feed", feed);
                startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
            }

            @Override
            public void onLikeListClick(Feeds feed) {
                if(feedsListner!=null)
                    feedsListner.addFragment(LikeFragment.newInstance(feed._id, Mualab.currentUser.id), true);
            }

            @Override
            public void onFeedClick(Feeds feed, int index, View v) {

            }

            @Override
            public void onClickProfileImage(Feeds feed, ImageView v) {

            }
        });

        rvFeed.setAdapter(adapter);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Feeds feed = (Feeds) data.getSerializableExtra("feed");
            list.get(0).commentCount = feed.commentCount;
            adapter.notifyItemChanged(0);
        }
    }

    private void getUpdatedFeed(){
        Map<String, String> map = new HashMap<>();
        map.put("feedId", ""+feed._id);
        map.put("userId", ""+Mualab.currentUser.id);
        Mualab.getInstance().getRequestQueue().cancelAll("feed"+feed._id);
        new HttpTask(new HttpTask.Builder(mContext, "feedDetails", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    // String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        list.clear();
                        if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(true, 500);
                        }
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
                        if(isPulltoRefrash){
                            isPulltoRefrash = false;
                            mRefreshLayout.stopRefresh(false, 500);

                        }
                        adapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                if(isPulltoRefrash){
                    isPulltoRefrash = false;
                    mRefreshLayout.stopRefresh(false, 500);

                }
            }
        }).setParam(map)).execute("feed"+feed._id);
    }

}
