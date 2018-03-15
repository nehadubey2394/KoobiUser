package com.mualab.org.user.activity.feeds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.CommentAdapter;
import com.mualab.org.user.activity.feeds.model.Comment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    public static String TAG = CommentsActivity.class.getName();

    private TextView tv_no_comments;
    private EditText ed_comments;
    private AppCompatButton btn_post_comments;
    private RecyclerView recyclerView;

    private Feeds feed;
    private int feedPosition;
    private boolean isDataUpdated;

    private CommentAdapter commentAdapter;
    private ArrayList<Comment> commentList = new ArrayList<>();
    private EndlessRecyclerViewScrollListener scrollListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Intent intent = getIntent();
        if (intent != null) {
            feedPosition = intent.getIntExtra("feedPosition",0);
            feed = (Feeds) intent.getSerializableExtra("feed");
        }

        ed_comments =  findViewById(R.id.ed_comments);
        tv_no_comments =  findViewById(R.id.tv_no_comments);
        recyclerView =  findViewById(R.id.recyclerView);
        btn_post_comments =  findViewById(R.id.btn_post_comments);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                //getCommentList(page);
            }
        };

        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);

        commentAdapter = new CommentAdapter(this, commentList, new CommentAdapter.Listner() {
            @Override
            public void onItemChange() {
                isDataUpdated = true;
            }
        });
        commentAdapter.setFeedId(feed);
        recyclerView.setAdapter(commentAdapter);




        findViewById(R.id.iv_back_press).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_post_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commnets = ed_comments.getText().toString().trim();
                if (!TextUtils.isEmpty(commnets)){
                    btn_post_comments.setEnabled(false);
                    apiForAddComments(commnets);
                }

                //recyclerView.smoothScrollToPosition(0);
            }
        });

        commentList.clear();
        getCommentList(0);
    }

    @Override
    public void onBackPressed() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if(isDataUpdated){
            Intent output = new Intent();
            output.putExtra("feed", feed);
            output.putExtra("feedPosition", feedPosition);
            setResult(RESULT_OK, output);
        }

        super.onBackPressed();
    }

    private void getCommentList(int pageNo) {
        Map<String, String> map = new HashMap<>();
        map.put("feedId", ""+feed._id);
        map.put("userId",  ""+Mualab.currentUser.id);
        map.put("page",  ""+pageNo);

        new HttpTask(new HttpTask.Builder(this, "commentList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("Responce", response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray array = js.getJSONArray("followerList");
                        Gson gson = new Gson();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Comment comment = gson.fromJson(String.valueOf(jsonObject), Comment.class);
                            commentList.add(comment);
                        }
                        //recyclerView.smoothScrollToPosition(0);
                        commentAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        }).setProgress(false)
        .setParam(map)).execute(TAG);
    }

    private void apiForAddComments(final String comments) {

        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("feedId", ""+feed._id);
        map.put("comment", comments);

        new HttpTask(new HttpTask.Builder(this, "addComment", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("Responce", response);
                btn_post_comments.setEnabled(true);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        isDataUpdated = true;
                        ed_comments.setText("");
                        Comment comment = new Comment();
                        JSONObject commentCount = js.getJSONObject("commentCount");
                        feed.commentCount = commentCount.getInt("comment");
                        comment.id = commentCount.getString("_id");
                        comment.comment = comments;
                        comment.firstName = Mualab.currentUser.firstName;
                        comment.firstName = Mualab.currentUser.lastName;
                        comment.userName = Mualab.currentUser.userName;
                        comment.profileImage = Mualab.currentUser.profileImage;
                        comment.crd = "1 second ago";
                        comment.commentLikeCount = 0;
                        comment.isLike = 0;
                        commentList.add(comment);
                        // feeds.commentCount = feeds.commentCount+1;
                    }if (commentList.size() == 0) {
                        tv_no_comments.setVisibility(View.VISIBLE);
                    } else {
                        tv_no_comments.setVisibility(View.GONE);
                    }

                    commentAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(commentList.size()-1);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                btn_post_comments.setEnabled(true);
            }}).setProgress(true)
                .setParam(map)).execute(TAG);
    }
}
