package com.mualab.org.user.activity.feeds.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.LikeListAdapter;
import com.mualab.org.user.activity.feeds.model.FeedLike;
import com.mualab.org.user.model.feeds.Feeds;
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

    private Context mContext;
    private EditText ed_search;
    private List<FeedLike> likedList;
    private RecyclerView recyclerView;
    private LikeListAdapter likeListAdapter;


    private TextView tv_no_likes_found;
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

        recyclerView = view.findViewById(R.id.recyclerView);
        tv_no_likes_found = view.findViewById(R.id.tv_no_likes_found);
        ed_search = view.findViewById(R.id.ed_search);


        likeListAdapter = new LikeListAdapter(mContext, likedList, myUserId);
        recyclerView.setAdapter(likeListAdapter);


        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                apiForLikesList(ed_search.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        apiForLikesList("");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ed_search = null;
        recyclerView = null;
        likeListAdapter = null;
        likedList = null;
    }


    private void apiForLikesList(String search) {

        Map<String, String> map = new HashMap<>();
        map.put("feedId", ""+feedId);
        map.put("page", "0");
        map.put("limit", "20");
        map.put("search", search);
        map.put("userId", ""+myUserId);

        new HttpTask(new HttpTask.Builder(mContext, "likeList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    likedList.clear();
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
                            tv_no_likes_found.setVisibility(View.VISIBLE);
                        } else {
                            tv_no_likes_found.setVisibility(View.GONE);
                        }
                        likeListAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        }).setProgress(false)
        .setParam(map)).execute("likeList");
    }
}
