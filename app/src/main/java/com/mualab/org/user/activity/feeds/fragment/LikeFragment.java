package com.mualab.org.user.activity.feeds.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.MainActivity;
import com.mualab.org.user.activity.feeds.adapter.LikeListAdapter;
import com.mualab.org.user.activity.feeds.model.FeedLike;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LikeFragment extends Fragment {

    private static String TAG = LikeFragment.class.getName();
    private Context mContext;
    private MainActivity activity;
    private EditText ed_search;
    private ProgressBar progress_bar;
    private LinearLayout ll_loadingBox;
    private RecyclerView recyclerView;
    private EndlessRecyclerViewScrollListener scrollListener;

    private List<FeedLike> likedList;
    private LikeListAdapter likeListAdapter;

    private TextView tvMsg;
    private TextView tvHeaderTitle;
    private ImageView ivAppIcon,ibtnChat, ivHeaderBack;
    private int feedId;
    private int myUserId;


    public LikeFragment() {
    }

    public static LikeFragment newInstance(int feedId, int userId) {
        LikeFragment fragment = new LikeFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("feedId", feedId);
        bundle.putInt("userId", userId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

        if(context instanceof MainActivity)
            activity = (MainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        likedList = new ArrayList<>();
        if (getArguments() != null) {
            feedId = getArguments().getInt("feedId");
            myUserId = getArguments().getInt("userId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_like, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(activity!=null){
            tvHeaderTitle = activity.findViewById(R.id.tvHeaderTitle);
            ivHeaderBack = activity.findViewById(R.id.ivHeaderBack);
            ibtnChat = activity.findViewById(R.id.ibtnChat);
            ivAppIcon = activity.findViewById(R.id.ivAppIcon);
            tvHeaderTitle.setVisibility(View.VISIBLE);
            ivHeaderBack.setVisibility(View.VISIBLE);
            ivAppIcon.setVisibility(View.GONE);
            ibtnChat.setVisibility(View.GONE);
            tvHeaderTitle.setText(R.string.likes);

            ivHeaderBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onBackPressed();
                }
            });
        }

        recyclerView = view.findViewById(R.id.recyclerView);
        tvMsg = view.findViewById(R.id.tvMsg);
        ed_search = view.findViewById(R.id.ed_search);
        ll_loadingBox = view.findViewById(R.id.ll_loadingBox);
        progress_bar = view.findViewById(R.id.progress_bar);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                String quary = ed_search.getText().toString();
                apiForLikesList(page, TextUtils.isEmpty(quary)?"":quary);
            }
        };


        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);

        likeListAdapter = new LikeListAdapter(mContext, likedList, myUserId);
        recyclerView.setAdapter(likeListAdapter);


        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String quary = ed_search.getText().toString();
                likedList.clear();
                scrollListener.resetState();
                likeListAdapter.notifyDataSetChanged();
                apiForLikesList(0, TextUtils.isEmpty(quary)?"":quary);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        ll_loadingBox.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tvMsg.setText(getString(R.string.loading));
        likedList.clear();
        apiForLikesList(0,"");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Mualab.getInstance().getRequestQueue().cancelAll(TAG);
        tvHeaderTitle.setVisibility(View.GONE);
        ivHeaderBack.setVisibility(View.GONE);
        ivAppIcon.setVisibility(View.VISIBLE);
        ibtnChat.setVisibility(View.VISIBLE);
        ibtnChat = null;
        ivHeaderBack = null;
        ll_loadingBox = null;
        ivAppIcon = null;
        tvHeaderTitle = null;
        ed_search = null;
        recyclerView = null;
        likeListAdapter = null;
        likedList = null;
    }

    private void apiForLikesList(final int page, String search) {

        Map<String, String> map = new HashMap<>();
        map.put("feedId", ""+feedId);
        map.put("page", ""+page);
        map.put("limit", "20");
        map.put("search", search.toLowerCase());
        map.put("userId", ""+myUserId);
        Mualab.getInstance().getRequestQueue().cancelAll(TAG);
        new HttpTask(new HttpTask.Builder(mContext, "likeList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        JSONArray array = js.getJSONArray("likeList");
                        for (int i = 0; i < array.length(); i++) {
                            Gson gson = new Gson();
                            JSONObject jsonObject = array.getJSONObject(i);
                            FeedLike likedListInfo = gson.fromJson(String.valueOf(jsonObject), FeedLike.class);
                            likedList.add(likedListInfo);
                        }
                        if (likedList.size() == 0) {
                            ll_loadingBox.setVisibility(View.VISIBLE);
                            tvMsg.setText(getString(R.string.no_like_yet));
                            tvMsg.setVisibility(View.VISIBLE);
                        } else {
                            ll_loadingBox.setVisibility(View.GONE);
                        }
                        likeListAdapter.notifyDataSetChanged();
                    }else {
                        if (likedList.size() == 0) {
                            tvMsg.setText(getString(R.string.no_like_yet));
                            tvMsg.setVisibility(View.VISIBLE);
                        } else {
                            ll_loadingBox.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    tvMsg.setText(getString(R.string.msg_some_thing_went_wrong));
                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
            }
        }).setProgress(false)
        .setParam(map)).execute(TAG);
    }
}
