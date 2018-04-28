package com.mualab.org.user.activity.story;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.CameraActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.feeds.LiveUserInfo;
import com.mualab.org.user.model.feeds.Story;
import com.mualab.org.user.webservice.HttpResponceListner;
import com.mualab.org.user.webservice.HttpTask;
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


public class StoreActivityTest extends SwipeBackActivity implements StoryStatusView.UserInteractionListener{

    private int currentIndex;
    private boolean isImmersive = true;

    private List<LiveUserInfo> liveUserList;
    private LiveUserInfo userInfo;
    // private StoryStatusView storyStatusView;
    private StoryStatusView storyStatusView;

    private ImageView ivPhoto, ivUserImg;
    private ProgressBar progress_bar;
    private RelativeLayout addMoreStory;

    private TextView tvUserName;
    private VideoView videoView;
    private RelativeLayout lyVideoView;
   // private MediaController vidControl;
    private MediaPlayer mediaPlayer;
    private File fileStorage;
    private File outputFile;

    private List<Story> storyList = new ArrayList<>();
    private long statusDuration = 3000L;
    private long DURATION = 500L;
    private int counter = 0;
    private boolean isRunningStory;
    private boolean isFirstTime = true;
    private boolean isStoryTypeVideo;

    private long pressTime = 0L;
    private long limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storyStatusView.pause();

                    if(isStoryTypeVideo && mediaPlayer!=null){
                        try{
                            videoView.pause();
                        }catch (Exception e){

                        }
                    }

                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storyStatusView.resume();
                    if(isStoryTypeVideo && mediaPlayer!=null){
                        try{
                            videoView.start();
                        }catch (Exception e){

                        }
                    }
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

        ivPhoto = findViewById(R.id.ivPhoto);
        progress_bar = findViewById(R.id.imageProgressBar);
        ivUserImg = findViewById(R.id.iv_user_image);
        tvUserName =  findViewById(R.id.tv_user_name);
        addMoreStory =  findViewById(R.id.addMoreStory);
        storyStatusView = findViewById(R.id.storiesStatus);

        lyVideoView = findViewById(R.id.lyVideoView);
        videoView = findViewById(R.id.videoView);
        //vidControl = new MediaController(this);
        //vidControl.setAnchorView(videoView);
        //videoView.setMediaController(vidControl);

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


    }


    @Override
    public void onPrev() {
        if (counter - 1 < 0 && currentIndex==0){
            return;
        }else if (counter - 1 < 0 && currentIndex>0){
            currentIndex--;
            storyStatusView.destroy();
            updateUI();
            getStories();

        }else {
            --counter;
            storyStatusView.pause();
            loadMediaFile();
        }
    }

    @Override
    public void onNext() {
        ++counter;
        storyStatusView.pause();
        if(counter<storyList.size()){
            loadMediaFile();
        }
    }

    @Override
    public void onComplete() {
        currentIndex++;
        isRunningStory = false;
        if(currentIndex<liveUserList.size()){
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

        if(story.storyType.equals("image")){
            isStoryTypeVideo = false;
            videoView.setVisibility(View.GONE);
            lyVideoView.setVisibility(View.GONE);
            Picasso.with(ivPhoto.getContext())
                    .load(story.myStory)
                    .error(R.drawable.bg_splash)
                    .into(ivPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            storyStatusView.setStoryDuration(statusDuration);
                            ivPhoto.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);
                            if(isFirstTime){
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
        }else if(story.storyType.equals("video")){
            Log.d("video", "inProgress");
            isStoryTypeVideo = true;
            Picasso.with(ivPhoto.getContext())
                    .load(story.videoThumb)
                    .error(R.drawable.bg_splash)
                    .into(ivPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                            ivPhoto.setVisibility(View.VISIBLE);
                            progress_bar.setVisibility(View.GONE);
                            if(isFirstTime){
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

            videoView.setVisibility(View.VISIBLE);
            lyVideoView.setVisibility(View.VISIBLE);
            ivPhoto.setVisibility(View.GONE);
            videoView.setVideoURI(checkVideoCache(story.myStory));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mediaPlayer) {
                    StoreActivityTest.this.mediaPlayer = mediaPlayer;
                    progress_bar.setVisibility(View.GONE);
                    storyStatusView.setDynamicStoryDuration(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    if(isFirstTime){
                        isFirstTime = false;
                        storyStatusView.startStories();
                    } else storyStatusView.resume();
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
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                }
            });

        }
    }

    private void showToast(String txt){
        MyToast.getInstance(this).showSmallMessage(txt);
    }

    private void updateUI(){
        counter = 0;
        userInfo = liveUserList.get(currentIndex);
        addMoreStory.setVisibility(userInfo.id == Mualab.currentUser.id?View.VISIBLE:View.GONE);
        tvUserName.setText(String.format("%s", userInfo.userName));
        if(TextUtils.isEmpty(userInfo.profileImage)){
            Picasso.with(this).load(R.drawable.defoult_user_img).fit().into(ivUserImg);
        }else Picasso.with(this).load(userInfo.profileImage).fit().into(ivUserImg);
    }


    private void resetViews(){
        //updateUI();
        isRunningStory = false;
        isFirstTime = true;
        storyStatusView.setStoriesCount(storyList.size());
        storyStatusView.setStoryDuration(statusDuration);
        storyStatusView.setStoriesListener(this);
    }



  /*  private void  updateView(){
        counter = 0;
        userInfo = liveUserList.get(currentIndex);
        addMoreStory.setVisibility(userInfo.id == Mualab.currentUser.id?View.VISIBLE:View.GONE);
        tvUserName.setText(String.format("%s %s", userInfo.firstName, userInfo.lastName));
        if(TextUtils.isEmpty(userInfo.profileImage)){
            Picasso.with(this).load(R.drawable.defoult_user_img).resize(200,200).into(ivUserImg);
        }else Picasso.with(this).load(userInfo.profileImage).fit().into(ivUserImg);
    }*/




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

            }})
                .setAuthToken(Mualab.getInstance().getSessionManager().getUser().authToken)
                .setBody(map , HttpTask.ContentType.APPLICATION_JSON))
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

        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
        storyStatusView.destroy();
    }


    private Uri checkVideoCache(String url){
        String downloadFileName = url.substring(url.lastIndexOf('/'), url.length());//Create file name by picking download file name from URL

        //Get File if SD card is present
        if (isExternalStoragePresent()) {
            fileStorage = new File(Environment.getExternalStorageDirectory() + "/" + "Mualab");
        } else
            Toast.makeText(StoreActivityTest.this, "Oops!! There is no External storage in device.", Toast.LENGTH_SHORT).show();

        //If File is not present create directory
        if (!fileStorage.exists()) {
            fileStorage.mkdir();
        }

        outputFile = new File(fileStorage, downloadFileName);//Create Output file in Main File

        //Create New File if not present
        if (!outputFile.exists()) {
            try {
                new DownloadingTask().execute(url);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            return Uri.fromFile(outputFile);
        }

        return Uri.parse(url);
    }


    public boolean isExternalStoragePresent() {
        if (Environment.getExternalStorageState().equals(

                Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }


    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE}, Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                if (liveUserList.size()!=0){
                    getStories();
                }
            }
        } else {
            if (liveUserList.size()!=0){
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
                    if (liveUserList.size()!=0){
                        getStories();
                    }
                } else {
                    Toast.makeText(StoreActivityTest.this, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show();
                }
            }

            break;

        }
    }

    // String filePath = "mualab"+randomNo+"mp4";

    @SuppressLint("StaticFieldLeak")
    private class DownloadingTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progress_bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (outputFile != null) {
                    //    progress_bar.setVisibility(View.GONE);
                  //  Toast.makeText(StoreActivityTest.this, "Downloaded Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    //   progress_bar.setVisibility(View.GONE);
                    Toast.makeText(StoreActivityTest.this, "Failed Download", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            super.onPostExecute(result);
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
                outputFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(outputFile);//Get OutputStream for NewFile Location
                InputStream is = c.getInputStream();//Get InputStream for connection

                byte[] buffer = new byte[1024];//Set buffer type
                int len1 = 0;//init length
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);//Write new file
                }
                //Close all connection after doing task
                fos.close();
                is.close();

            } catch (Exception e) {
                //Read exception if something went wrong
                e.printStackTrace();
                outputFile = null;
            }
            return null;
        }
    }
}

