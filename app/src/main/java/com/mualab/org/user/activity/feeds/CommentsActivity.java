package com.mualab.org.user.activity.feeds;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.image.cropper.CropImage;
import com.image.cropper.CropImageView;
import com.image.picker.ImagePicker;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.CommentAdapter;
import com.mualab.org.user.activity.feeds.model.Comment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.KeyboardUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    public static String TAG = CommentsActivity.class.getName();

    private TextView tv_no_comments;
    private EditText ed_comments;
    private LinearLayout ll_loadingBox;
    private ProgressBar progress_bar;
    private AppCompatButton btn_post_comments;
    private RecyclerView recyclerView;
    ImageView ivCamera;

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
        setStatusbarColor();
        Intent intent = getIntent();
        if (intent != null) {
            feedPosition = intent.getIntExtra("feedPosition",0);
            feed = (Feeds) intent.getSerializableExtra("feed");
        }

        ed_comments =  findViewById(R.id.ed_comments);
        tv_no_comments =  findViewById(R.id.tv_no_comments);
        recyclerView =  findViewById(R.id.recyclerView);
        btn_post_comments =  findViewById(R.id.btn_post_comments);
        ll_loadingBox =  findViewById(R.id.ll_loadingBox);
        progress_bar =  findViewById(R.id.progress_bar);
        ivCamera =  findViewById(R.id.ivCamera);


        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.pickImage(CommentsActivity.this);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getCommentList(page);
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
                    KeyboardUtil.hideKeyboard(btn_post_comments, CommentsActivity.this);
                    apiForAddComments(commnets, null);
                }else {
                    Animation shake = AnimationUtils.loadAnimation(CommentsActivity.this, R.anim.shake);
                    btn_post_comments.startAnimation(shake);
                }

                //recyclerView.smoothScrollToPosition(0);
            }
        });

        commentList.clear();
        ll_loadingBox.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tv_no_comments.setText(getString(R.string.loading));
        getCommentList(0);
    }



    // check permission or Get image from camera or gallery
    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            } else {
                ImagePicker.pickImage(this);
            }
        } else {
            ImagePicker.pickImage(this);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = null;
        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).
                            setAspectRatio(400, 400)
                            .setMaxCropResultSize(600, 600).start(this);
                } else {
                    MyToast.getInstance(CommentsActivity.this).showSmallMessage(
                            getString(R.string.msg_some_thing_went_wrong));
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());

                    if (bitmap != null) {
                        apiForAddComments("", bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
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
        map.put("limit",  "50");

        new HttpTask(new HttpTask.Builder(this, "commentList", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("Responce", response);
                progress_bar.setVisibility(View.GONE);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        ll_loadingBox.setVisibility(View.GONE);
                        JSONArray array = js.getJSONArray("commentList");
                        Gson gson = new Gson();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Comment comment = gson.fromJson(String.valueOf(jsonObject), Comment.class);
                            commentList.add(comment);
                        }
                        //recyclerView.smoothScrollToPosition(0);
                        commentAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(commentList.size()-1);
                    }else {

                        if(commentList.size()==0) {
                            tv_no_comments.setVisibility(View.VISIBLE);
                            tv_no_comments.setText(getString(R.string.no_comment_yet));
                        }else {
                            ll_loadingBox.setVisibility(View.GONE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(commentList.size()==0) {
                        tv_no_comments.setVisibility(View.VISIBLE);
                        tv_no_comments.setText(getString(R.string.no_comment_yet));
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                if(commentList.size()==0) {
                    tv_no_comments.setVisibility(View.VISIBLE);
                    tv_no_comments.setText(getString(R.string.msg_some_thing_went_wrong));
                }

            }
        }).setProgress(false)
        .setParam(map)).execute(TAG);
    }

    private void apiForAddComments(final String comments, final Bitmap bitmap) {

        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("feedId", ""+feed._id);
        map.put("postUserId", ""+feed.userId);
        map.put("type", bitmap==null?"text":"image");
        if(comments!=null)
            map.put("comment", comments);

        new HttpTask(new HttpTask.Builder(this, "addComment", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("Responce", response);
                btn_post_comments.setEnabled(true);
                String status="";
                try {
                    JSONObject js = new JSONObject(response);
                    status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        isDataUpdated = true;
                        ed_comments.setText("");
                        Comment comment = new Comment();

                        feed.commentCount = js.getInt("commentCount");
                        comment.id = js.getInt("_id");
                        comment.comment = comments;
                        comment.firstName = Mualab.currentUser.firstName;
                        comment.firstName = Mualab.currentUser.lastName;
                        comment.userName = Mualab.currentUser.userName;
                        comment.profileImage = Mualab.currentUser.profileImage;
                        comment.timeElapsed = "1 second ago";
                        comment.commentLikeCount = 0;
                        comment.isLike = 0;
                        commentList.add(comment);

                    }if (commentList.size() == 0) {
                        tv_no_comments.setVisibility(View.VISIBLE);
                    } else {
                        tv_no_comments.setVisibility(View.GONE);
                    }

                    commentAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(commentList.size()-1);


                } catch (JSONException e) {
                    e.printStackTrace();

                    if(!TextUtils.isEmpty(status) && status.equals("success")){
                        commentList.clear();
                        scrollListener.resetState();
                        getCommentList(0);
                    } else if(commentList.size()==0) {
                        tv_no_comments.setVisibility(View.VISIBLE);
                        tv_no_comments.setText(getString(R.string.msg_some_thing_went_wrong));
                    }
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                btn_post_comments.setEnabled(true);
                if(commentList.size()==0) {
                    tv_no_comments.setVisibility(View.VISIBLE);
                    tv_no_comments.setText(getString(R.string.msg_some_thing_went_wrong));
                }
            }}).setProgress(true)
                .setParam(map)).postImage("comment", bitmap);
    }



    protected void setStatusbarColor(){
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }
}
