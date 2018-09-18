package com.mualab.org.user.activity.story;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.camera.CameraActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.feeds.LiveUserInfo;
import com.mualab.org.user.data.model.feeds.Story;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.utils.transformers.SimpleGestureFilter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.statusstories.StoryStatusView;
import views.swipback.SwipeBackActivity;
import views.swipback.SwipeBackLayout;


public class StoriesActivity extends SwipeBackActivity implements StoryStatusView.UserInteractionListener, SimpleGestureFilter.SimpleGestureListener {
    private StoryStatusView storyStatusView;
    private ImageView ivPhoto, ivUserImg;
    private ProgressBar progress_bar;
    private RelativeLayout addMoreStory;
    private TextView tvUserName;
    private VideoView videoView;
    private RelativeLayout lyVideoView;
    private MediaPlayer mediaPlayer;
    private File fileStorage;
    private File outputFile;
    private LiveUserInfo userInfo;
    private List<LiveUserInfo> liveUserList;
    private List<Story> storyList = new ArrayList<>();
    private int currentIndex;
    private long statusDuration = 3000L;
    private int counter = 0;
    private boolean isRunningStory;
    private ImageButton img_btn;
    private boolean isFirstTime = true;
    private boolean isStoryTypeVideo;
    private FrameLayout parent;
    private long pressTime = 0L;
    private SimpleGestureFilter detector;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    // storyStatusView.pause();

                    if (isStoryTypeVideo && mediaPlayer != null) {
                        try {
                            videoView.pause();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storyStatusView.resume();
                    if (isStoryTypeVideo && mediaPlayer != null) {
                        try {
                            videoView.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    long limit = 500L;
                    return limit < now - pressTime;

                case MotionEvent.ACTION_SCROLL:
                    long now1 = System.currentTimeMillis();
                    storyStatusView.resume();
                    if (isStoryTypeVideo && mediaPlayer != null) {
                        try {
                            videoView.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    long limit1 = 500L;
                    return limit1 < now1 - pressTime;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);


        Bundle args = getIntent().getBundleExtra("BUNDLE");

        if (args != null) {
            liveUserList = (ArrayList<LiveUserInfo>) args.getSerializable("ARRAYLIST");
            currentIndex = args.getInt("position");
            detector = new SimpleGestureFilter(this, this);

        } else finish();


        ivPhoto = findViewById(R.id.ivPhoto);
        parent = findViewById(R.id.parent);
        img_btn = findViewById(R.id.img_btn);
        progress_bar = findViewById(R.id.imageProgressBar);
        ivUserImg = findViewById(R.id.iv_user_image);
        tvUserName = findViewById(R.id.tv_user_name);
        addMoreStory = findViewById(R.id.addMoreStory);
        storyStatusView = findViewById(R.id.storiesStatus);
        lyVideoView = findViewById(R.id.lyVideoView);
        videoView = findViewById(R.id.videoView);

        setDragEdge(SwipeBackLayout.DragEdge.TOP);

        addMoreStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StoriesActivity.this, CameraActivity.class));
                finish();
            }
        });

        img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {

                    if (isRunningStory)
                        storyStatusView.pause();
                } else {
                    storyStatusView.resume();
                }
                return true;
            }
        });


    }


    @Override
    public void onPrev() {
        if (counter - 1 >= 0 || currentIndex != 0) {
            if (counter - 1 < 0 && currentIndex > 0) {
                currentIndex--;
                storyStatusView.destroy();
                updateUI();
                getStories();

            } else {
                --counter;
                storyStatusView.pause();
                loadMediaFile();
            }
        }
    }

    @Override
    public void onNext() {
        ++counter;
        if (counter < storyList.size()) {
            storyStatusView.pause();
            loadMediaFile();
        }
    }

    @Override
    public void onComplete() {
        currentIndex++;
        isRunningStory = false;
        if (currentIndex < liveUserList.size()) {
            storyStatusView.destroy();
            updateUI();
            getStories();
        } else {
            finish();
        }
    }


    private void loadMediaFile() {

        final Story story = storyList.get(counter);
        progress_bar.setVisibility(View.VISIBLE);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress_bar.setProgress(65, true);
        }*/
        //  if(!isFirstTime) storyStatusView.pause();

        if (story.storyType.equals("image")) {
            isStoryTypeVideo = false;
            ivPhoto.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            lyVideoView.setVisibility(View.GONE);
            storyStatusView.setDynamicStoryDuration(statusDuration);
            Picasso.with(ivPhoto.getContext())
                    .load(story.myStory)
                    .centerCrop()
                    .fit()
                    .error(R.drawable.bg_splash)
                    .into(ivPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            //storyStatusView.setStoryDuration(statusDuration);
                            ivPhoto.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);
                            if (isFirstTime) {
                                isFirstTime = false;
                                storyStatusView.startStories();
                            } else storyStatusView.resume();
                        }

                        @Override
                        public void onError() {
                            storyStatusView.pause();
                            progress_bar.setVisibility(View.GONE);
                        }
                    });
        } else if (story.storyType.equals("video")) {
            Log.d("video", "inProgress");
            isStoryTypeVideo = true;
            Picasso.with(ivPhoto.getContext())
                    .load(story.videoThumb)
                    .error(R.drawable.bg_splash)
                    .into(ivPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            ivPhoto.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {
                        }
                    });
            if (videoView.getVisibility() == View.GONE) {
                videoView.setVisibility(View.VISIBLE);
                lyVideoView.setVisibility(View.VISIBLE);
            }


            lyVideoView.setVisibility(View.VISIBLE);
            ivPhoto.setVisibility(View.GONE);
            videoView.setVideoURI(checkVideoCache(story.myStory));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mediaPlayer) {
                    StoriesActivity.this.mediaPlayer = mediaPlayer;
                    progress_bar.setVisibility(View.GONE);
                    storyStatusView.setDynamicStoryDuration(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    if (isFirstTime) {
                        isFirstTime = false;
                        storyStatusView.startStories();
                    } else storyStatusView.resume();

                    mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                            mediaPlayer.start();
                        }
                    });
                }
            });


            final MediaPlayer.OnInfoListener onInfoToPlayStateListener = new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    switch (what) {
                        case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: {
                            progress_bar.setVisibility(View.GONE);
                            storyStatusView.resume();
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                            progress_bar.setVisibility(View.VISIBLE);
                            storyStatusView.pause();
                            //mp.pause();
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                            progress_bar.setVisibility(View.VISIBLE);
                            //mp.pause();
                            storyStatusView.pause();
                            return true;
                        }
                    }
                    return false;
                }
            };

            videoView.setOnInfoListener(onInfoToPlayStateListener);
        }
    }


    private void updateUI() {
        counter = 0;
        if (liveUserList.size() > 0) {
            userInfo = liveUserList.get(currentIndex);
            addMoreStory.setVisibility(userInfo.id == Mualab.currentUser.id ? View.VISIBLE : View.GONE);
            img_btn.setVisibility(userInfo.id == Mualab.currentUser.id ? View.GONE : View.VISIBLE);

            tvUserName.setText(String.format("%s", userInfo.userName));
            if (TextUtils.isEmpty(userInfo.profileImage)) {
                Picasso.with(this).load(R.drawable.defoult_user_img).fit().into(ivUserImg);
            } else Picasso.with(this).load(userInfo.profileImage).fit().into(ivUserImg);
        }
    }


    private void resetViews() {
        isRunningStory = false;
        isFirstTime = true;
        storyStatusView.destroy();
        storyStatusView.setStoriesCount(storyList.size());
        storyStatusView.setStoryDuration(statusDuration);

        storyStatusView.setStoriesListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(StoriesActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if (isConnected) {
                        dialog.dismiss();
                        getStories();
                    }
                }
            }).show();
        }
        Map<String, String> map = new HashMap<>();
        map.put("userId", "" + userInfo.id);
        new HttpTask(new HttpTask.Builder(this, "myStory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (message.equals("No record found")) {
                        onBackPressed();
                    }
                    storyList.clear();
                    //liveUserList.clear();

                    if (status.equalsIgnoreCase("success") && !message.equalsIgnoreCase("No results found right now")) {
                        JSONArray array = js.getJSONArray("allMyStory");
                        counter = 0;
                        Gson gson = new Gson();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Story story = gson.fromJson(String.valueOf(jsonObject), Story.class);
                            storyList.add(story);
                        }

                    /*    if (storyList.size() == 0) {
                            finish();
                        }*/

                        if (!isRunningStory) {
                            resetViews();
                            loadMediaFile();
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        })
                .setAuthToken(Mualab.getInstance().getSessionManager().getUser().authToken)
                .setBody(map, HttpTask.ContentType.APPLICATION_JSON))
                .execute("StoryAPI");
    }


    // Lifecycle events

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //mExoPlayerHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
        getPermissionAndPicImage();
        //getStories();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        storyStatusView.destroy();
    }


    private Uri checkVideoCache(String url) {
        String downloadFileName = url.substring(url.lastIndexOf('/'), url.length());//Create file name by picking download file name from URL

        //Get File if SD card is present
        if (isExternalStoragePresent()) {
            fileStorage = new File(Environment.getExternalStorageDirectory() + "/" + "Mualab");
        } else
            Toast.makeText(StoriesActivity.this, "Oops!! There is no External storage in device.", Toast.LENGTH_SHORT).show();

        boolean bool = false;
        //If File is not present create directory
        if (!fileStorage.exists()) bool = fileStorage.mkdir();

        if (bool) {
            outputFile = new File(fileStorage, downloadFileName);//Create Output file in Main File

            //Create New File if not present
            if (!outputFile.exists()) {
                try {
                    new DownloadingTask().execute(url);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return Uri.fromFile(outputFile);
            }
        }
        return Uri.parse(url);
    }


    public boolean isExternalStoragePresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                if (liveUserList.size() != 0) {
                    getStories();
                }
            }
        } else {
            if (liveUserList.size() != 0) {
                getStories();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (liveUserList.size() != 0) {
                        getStories();
                    }
                } else {
                    Toast.makeText(StoriesActivity.this, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show();
                }
            }

            break;
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void onSwipe(int direction) {
        String str = "";
        int size = liveUserList.size();
        boolean isUpdate = false;
//Todo check
        switch (direction) {
            case SimpleGestureFilter.SWIPE_RIGHT:
                currentIndex -= 1;
                if (0 < currentIndex) {
                    Log.e("what is data = ", "" + size + " = " + currentIndex);
                    updateUI();
                    getPermissionAndPicImage();

                    parent.startAnimation(AnimationUtils.loadAnimation(
                            StoriesActivity.this, R.anim.slide_in_from_right
                    ));

                    setDragEdge(SwipeBackLayout.DragEdge.RIGHT);
                    videoView.setVisibility(View.GONE);
                    ivPhoto.setVisibility(View.GONE);
                    resetViews();


                } else if (currentIndex == 0) {
                    finish();
                } else currentIndex += 1;


                break;
            case SimpleGestureFilter.SWIPE_LEFT:
                currentIndex += 1;
                if (size > currentIndex) {
                    Log.e("what is data = ", "" + size + " = " + currentIndex);
                    updateUI();
                    getPermissionAndPicImage();
                 /*   Animator anim =  AnimatorInflater.loadAnimator(this, R.animator.cube_left_out);
                    anim.setTarget(parent);
                    anim.setDuration(1000);
                    anim.start();*/
                    parent.startAnimation(AnimationUtils.loadAnimation(
                            StoriesActivity.this, R.anim.slide_in_from_left
                    ));
                    setDragEdge(SwipeBackLayout.DragEdge.LEFT);
                    videoView.setVisibility(View.GONE);
                    ivPhoto.setVisibility(View.GONE);
                    resetViews();


                } else if (!(size > currentIndex)) {
                    finish();
                } else currentIndex -= 1;

                break;
            case SimpleGestureFilter.SWIPE_DOWN:

                break;
            case SimpleGestureFilter.SWIPE_UP:

                break;

        }

    }

    @Override
    public void onDoubleTap() {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        // Call onTouchEvent of SimpleGestureFilter class
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadingTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progress_bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (outputFile == null) {
                Toast.makeText(StoriesActivity.this, "Failed Download", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);//Create Download URl
                HttpURLConnection c = (HttpURLConnection) url.openConnection();//Open Url Connection
                c.setRequestMethod("GET");//Set Request Method to "GET" since we are grtting data
                c.connect();//connect the URL Connection
                //If Connection response is not OK then show Logs
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.e("MyStoreViewActivity", "Server returned HTTP " + c.getResponseCode() + " " + c.getResponseMessage());

                }
                boolean bool = outputFile.createNewFile();

                if (bool) {
                    FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location
                    InputStream is = c.getInputStream();//Get InputStream for connection

                    byte[] buffer = new byte[1024];//Set buffer type
                    int len1;//init length
                    while ((len1 = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);//Write new file
                    }
                    //Close all connection after doing task
                    fos.close();
                    is.close();
                }

            } catch (Exception e) {
                //Read exception if something went wrong
                e.printStackTrace();
                outputFile = null;
            }
            return null;
        }
    }
}

