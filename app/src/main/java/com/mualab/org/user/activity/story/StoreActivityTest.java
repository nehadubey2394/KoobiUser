package com.mualab.org.user.activity.story;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.CameraActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.feeds.LiveUserInfo;
import com.mualab.org.user.model.feeds.Story;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.story.StoriesProgressView;
import views.swipback.SwipeBackActivity;
import views.swipback.SwipeBackLayout;


public class StoreActivityTest extends SwipeBackActivity implements StoriesProgressView.StoriesListener{


    private int currentIndex;
    private int totalIndex;
    private boolean isImmersive = true;

    private List<LiveUserInfo> liveUserList;

    private LiveUserInfo userInfo;
    // private StoryStatusView storyStatusView;
    private StoriesProgressView storyStatusView;

    private ImageView ivPhoto, ivUserImg;
    private ProgressBar progress_bar;
    private RelativeLayout addMoreStory;


    private TextView tvUserName;
    private VideoView videoView;
    private MediaController vidControl;

    private List<Story> storyList = new ArrayList<>();
    private long statusDuration = 3000L;
    private long DURATION = 500L;
    private int counter = 0;
    private boolean isRunningStory;

    long pressTime = 0L;
    long limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storyStatusView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storyStatusView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_story);

        setDragEdge(SwipeBackLayout.DragEdge.TOP);
        Bundle args = getIntent().getBundleExtra("BUNDLE");
        if (args != null) {
            liveUserList = (ArrayList<LiveUserInfo>) args.getSerializable("ARRAYLIST");
            currentIndex = args.getInt("position");
        } else finish();


        totalIndex = liveUserList.size();
        ivPhoto = findViewById(R.id.ivPhoto);
        progress_bar = findViewById(R.id.imageProgressBar);
        storyStatusView = findViewById(R.id.stories);
        videoView = findViewById(R.id.videoView);
        ivUserImg = findViewById(R.id.iv_user_image);
        tvUserName =  findViewById(R.id.tv_user_name);
        addMoreStory =  findViewById(R.id.addMoreStory);

        vidControl = new MediaController(this);
        vidControl.setAnchorView(videoView);
        videoView.setMediaController(vidControl);
        storyStatusView = findViewById(R.id.storiesStatus);

        addMoreStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoreActivityTest.this, CameraActivity.class));
                finish();
            }
        });

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyStatusView.skip();
            }
        });

        // bind reverse view
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyStatusView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyStatusView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        findViewById(R.id.actions).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {

                    if(isRunningStory)
                        storyStatusView.pause();
                } else {
                    storyStatusView.resume();
                }
                return true;
            }
        });

        updateView();
        getStories();
    }


    private void showToast(String txt){
        MyToast.getInstance(this).showSmallMessage(txt);
    }


    @Override
    public void onNext() {
        storyStatusView.pause();
        ++counter;
        if(counter<storyList.size()){

            //storyStatusView.pause();
            //get image url
            String imageUrl = storyList.get(counter).myStory;

            //ImageViewTarget is the implementation of Target interface.
            //code for this ImageViewTarget is in the end
            //target = new ImageViewTarget(ivPhoto, progress_bar);
            progress_bar.setVisibility(View.VISIBLE);
            Picasso.with(ivPhoto.getContext())
                    .load(imageUrl)
                    .error(R.drawable.bg_splash)
                    .into(ivPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            storyStatusView.resume();
                            ivPhoto.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            storyStatusView.pause();
                            progress_bar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public void onPrev() {
        if (counter - 1 < 0) return;
        storyStatusView.pause();
        --counter;

        //get image url
        String imageUrl = storyList.get(counter).myStory;
        //ImageViewTarget is the implementation of Target interface.
        //code for this ImageViewTarget is in the end
        //target = new ImageViewTarget(ivPhoto, progress_bar);
        progress_bar.setVisibility(View.VISIBLE);
        Picasso.with(ivPhoto.getContext())
                .load(imageUrl)
                .error(R.drawable.bg_splash)
                .into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        storyStatusView.resume();
                        ivPhoto.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        storyStatusView.pause();
                        progress_bar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onComplete() {

        ++currentIndex;
        isRunningStory = false;
        if(currentIndex>liveUserList.size())
            finish();
        else {
            storyStatusView.destroy();
            updateView();
            getStories();

        }
    }


    private void startStories(){
        isRunningStory = true;
        storyStatusView.setStoriesCount(storyList.size());
        storyStatusView.setStoryDuration(statusDuration);
        // or
        // statusView.setStoriesCountWithDurations(statusResourcesDuration);
        storyStatusView.setStoriesListener(this);
        // storyStatusView.playStories();
        //target = new ImageViewTarget(ivPhoto, progress_bar);

        // storyStatusView.startStories();
        //storyStatusView.pause();
        progress_bar.setVisibility(View.VISIBLE);
        String imageUrl = storyList.get(counter).myStory;
        Picasso.with(ivPhoto.getContext())
                .load(imageUrl)
                .error(R.drawable.bg_splash)
                .into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        storyStatusView.startStories();
                        //storyStatusView.resume();
                        ivPhoto.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        storyStatusView.pause();
                        progress_bar.setVisibility(View.GONE);
                    }
                });
    }



    private void updateView(){
        counter = 0;
        userInfo = liveUserList.get(currentIndex);
        addMoreStory.setVisibility(userInfo.id == Mualab.getInstance().getSessionManager().getUser().id?View.VISIBLE:View.GONE);
        tvUserName.setText(String.format("%s %s", userInfo.firstName, userInfo.lastName));
        if(TextUtils.isEmpty(userInfo.profileImage)){
            Picasso.with(this).load(R.drawable.defoult_user_img).fit().into(ivUserImg);
        }else Picasso.with(this).load(userInfo.profileImage).fit().into(ivUserImg);
    }


    @Override
    public void onDestroy() {
        storyStatusView.destroy();
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isImmersive && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    private void getStories() {
        Map<String, String> map = new HashMap<>();
        // map.put("userId", Mualab.getInstance().getSessionManager().getUser().id);
        map.put("userId", ""+userInfo.id);

        new HttpTask(new HttpTask.Builder(this, "myStory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    storyList.clear();

                    if (status.equalsIgnoreCase("success") && !message.equalsIgnoreCase("No results found right now")) {
                        JSONArray array = js.getJSONArray("allMyStory");
                        counter=0;
                        Gson gson = new Gson();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Story story = gson.fromJson(String.valueOf(jsonObject), Story.class);
                            storyList.add(story);
                        }

                        if(!isRunningStory){
                            startStories();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }})
                .setAuthToken(Mualab.getInstance().getSessionManager().getUser().authToken)
                .setBody(map , HttpTask.ContentType.APPLICATION_JSON))
                .execute("StoryAPI");
    }
}

