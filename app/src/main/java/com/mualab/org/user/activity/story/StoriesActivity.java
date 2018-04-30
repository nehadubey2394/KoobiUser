package com.mualab.org.user.activity.story;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.data.model.feeds.LiveUserInfo;
import com.mualab.org.user.data.model.feeds.Story;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
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

import views.story.StoriesProgressView;


public class StoriesActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener,View.OnClickListener {

    ImageView iv_myStory,ivUserImg;

    private LiveUserInfo currentUser;
    int position,counter=0;

    private StoriesProgressView storiesProgressView;
    private ProgressBar progress_bar; // mProgressBar;

    private TextView tvUserName;
    private VideoView videoView;
    private RelativeLayout lyVideoView;
    private MediaController vidControl;
    long pressTime = 0L, limit = 500L;
    private File fileStorage = null,outputFile = null;
    private String sType;
    private MediaPlayer mp;
    private LinearLayout linearLy;
    private GestureDetector mDetector;
    private  final int SWIPE_MIN_DISTANCE = 120;
    private  final int SWIPE_MAX_OFF_PATH = 250;
    private  final int SWIPE_THRESHOLD_VELOCITY = 200;

    private List<Story> storyArrayList;
    private List<LiveUserInfo> liveUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_view);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");

        storyArrayList = new ArrayList<>();

        // get the gesture detector
        mDetector = new GestureDetector(this, new MyGestureListener());

        if (args != null) {
            liveUserList = (ArrayList<LiveUserInfo>) args.getSerializable("ARRAYLIST");
            position = args.getInt("position");
        } else finish();


        initView();
        updateCurrentUser();
        getPermissionAndPicImage();

        storiesProgressView.setStoriesListener(this);

        // bind reverse view
        View reverse = findViewById(R.id.reverse);
        // bind skip view
        View skip = findViewById(R.id.skip);


        skip.setOnClickListener(this);
        reverse.setOnClickListener(this);

        //  reverse.setOnTouchListener(onTouchListener);
        //  skip.setOnTouchListener(onTouchListener);

        // skip.setOnTouchListener(gestureListener);
        //reverse.setOnTouchListener(gestureListener);
        linearLy.setOnTouchListener(touchListener);
    }

    private void initView(){
        iv_myStory = findViewById(R.id.iv_myStory);
        progress_bar = findViewById(R.id.progress_bar);
        storiesProgressView = findViewById(R.id.stories);
        videoView = findViewById(R.id.videoView);
        ivUserImg = findViewById(R.id.iv_user_image);
        tvUserName =  findViewById(R.id.tv_user_name);
        lyVideoView = findViewById(R.id.lyVideoView);
        linearLy = findViewById(R.id.linearLy);

        vidControl = new MediaController(this);
        vidControl.setAnchorView(videoView);
        videoView.setMediaController(vidControl);
    }

    private void updateCurrentUser(){
        currentUser = liveUserList.get(position);

        if(!TextUtils.isEmpty(currentUser.profileImage)){
            Picasso.with(this).load(currentUser.profileImage)
                    .fit()
                    .placeholder(R.drawable.defoult_user_img)
                    .into(ivUserImg);
        }else  Picasso.with(this).load(R.drawable.defoult_user_img).into(ivUserImg);
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // pass the events to the gesture detector
            // a return value of true means the detector is handling it
            // a return value of false means the detector didn't
            // recognize the event
            return mDetector.onTouchEvent(event);

        }
    };

    @Override
    public void onNext() {
        // progress_bar.setVisibility(View.VISIBLE);

        if (counter< storyArrayList.size()){

            Story story = storyArrayList.get(++counter);

            if (story.storyType.equals("image")){
                lyVideoView.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                iv_myStory.setVisibility(View.VISIBLE);
                Picasso.with(StoriesActivity.this).load(story.myStory).
                        placeholder(R.color.dark_transperant)
                        .into(iv_myStory, new Callback() {
                            @Override
                            public void onSuccess() {
                                progress_bar.setVisibility(View.GONE);
                                storiesProgressView.setStoryDuration(3000L);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
            else if (story.storyType.equals("video")){
                sType = "video";
                videoView.setVisibility(View.VISIBLE);
                lyVideoView.setVisibility(View.VISIBLE);
                iv_myStory.setVisibility(View.GONE);
                videoView.setVideoURI(checkVideoCache(story.myStory));

                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(final MediaPlayer mediaPlayer) {
                        progress_bar.setVisibility(View.GONE);
                        mp = mediaPlayer;
                        storiesProgressView.setStoryDuration(mediaPlayer.getDuration());
                        mp.start();

                    }
                });
            }

        }
        //image.setImageResource(resources[++counter]);
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
//        progress_bar.setVisibility(View.VISIBLE);

        Story story = storyArrayList.get(--counter);

        if (story.storyType.equals("image")){
            lyVideoView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            iv_myStory.setVisibility(View.VISIBLE);
            Picasso.with(StoriesActivity.this).load(story.myStory).
                    placeholder(R.color.dark_transperant)
                    .into(iv_myStory, new Callback() {
                        @Override
                        public void onSuccess() {
                            progress_bar.setVisibility(View.GONE);
                            storiesProgressView.setStoryDuration(3000L);
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }
        else if (story.storyType.equals("video")){
            sType = "video";
            videoView.setVisibility(View.VISIBLE);
            lyVideoView.setVisibility(View.VISIBLE);
            iv_myStory.setVisibility(View.GONE);

            videoView.setVideoURI(checkVideoCache(story.myStory));

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mediaPlayer) {
                    progress_bar.setVisibility(View.GONE);
                    mp = mediaPlayer;
                    storiesProgressView.setStoryDuration(mediaPlayer.getDuration());
                    mp.start();
                }
            });

        }
    }

    @Override
    public void onComplete() {
        position++;
        if (position==liveUserList.size()){
            finish();
        }else {
            storiesProgressView.destroy();
            storiesProgressView.setStoriesListener(this);
            apiForMyStory();
        }

    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }

    private void apiForMyStory() {
        Map<String, String> map = new HashMap<>();
        // map.put("userId", Mualab.getInstance().getSessionManager().getUser().id);
        map.put("userId", ""+liveUserList.get(position).id);

        new HttpTask(new HttpTask.Builder(this, "myStory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    tvUserName.setText(liveUserList.get(position).fullName);

                    updateCurrentUser();


                    storyArrayList.clear();

                    if (status.equalsIgnoreCase("success") && !message.equalsIgnoreCase("No results found right now")) {
                        JSONArray array = js.getJSONArray("allMyStory");
                        counter=0;
                        Gson gson = new Gson();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Story story = gson.fromJson(String.valueOf(jsonObject), Story.class);
                            storyArrayList.add(story);
                        }

                        Story story = storyArrayList.get(counter);
                        loadStory(story);

                    }else {
                        finish();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.skip :
                if (storyArrayList.size()!=0)
                    storiesProgressView.skip();
                break;

            case R.id.reverse :
                if (storyArrayList.size()!=0)
                    storiesProgressView.reverse();
                break;

            case R.id.videoView :

                break;
        }
    }

    private void loadStory(Story story) {
        if (story.storyType.equals("image")){
            lyVideoView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            iv_myStory.setVisibility(View.VISIBLE);
            Picasso.with(StoriesActivity.this).load(story.myStory).
                    placeholder(R.color.dark_transperant)
                    .into(iv_myStory, new Callback() {
                        @Override
                        public void onSuccess() {
                            progress_bar.setVisibility(View.GONE);
                            storiesProgressView.setStoriesCount(storyArrayList.size());
                            storiesProgressView.setStoryDuration(3000L);
                            storiesProgressView.startStories();
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }
        else if (story.storyType.equals("video")){
            videoView.setVisibility(View.VISIBLE);
            lyVideoView.setVisibility(View.VISIBLE);
            iv_myStory.setVisibility(View.GONE);

            sType  ="video";
            //   vidUri = Uri.parse(story.myStory);
            videoView.setVideoURI(checkVideoCache(story.myStory));

            //  videoView.setVideoPath(story.myStory);
            storiesProgressView.setStoriesCount(storyArrayList.size());

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(final MediaPlayer mediaPlayer) {
                    progress_bar.setVisibility(View.GONE);
                    mp = mediaPlayer;
                    storiesProgressView.setStoryDuration(mediaPlayer.getDuration());
                    mp.start();
                    //videoView.start();
                    storiesProgressView.startStories();

                }
            });
            // Toast.makeText(StoreViewActivity.this, "Video type of story is under development", Toast.LENGTH_SHORT).show();
        }

    }


    public boolean isExternalStoragePresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private Uri checkVideoCache(String url){
        String downloadFileName = url.substring(url.lastIndexOf('/'), url.length());//Create file name by picking download file name from URL

        //Get File if SD card is present
        if (isExternalStoragePresent()) {
            fileStorage = new File(Environment.getExternalStorageDirectory() + "/" + "Mualab");
        } else
            Toast.makeText(StoriesActivity.this, "Oops!! There is no External storage in device.", Toast.LENGTH_SHORT).show();

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

    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE}, Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                if (liveUserList.size()!=0){
                    apiForMyStory();
                }
            }
        } else {
            if (liveUserList.size()!=0){
                apiForMyStory();
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
                        apiForMyStory();
                    }
                } else {
                    Toast.makeText(StoriesActivity.this, "YOU DENIED PERMISSION CANNOT SELECT IMAGE", Toast.LENGTH_LONG).show();
                }
            }

            break;

        }
    }

// String filePath = "mualab"+randomNo+"mp4";

    private class DownloadingTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // progress_bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (outputFile != null) {
                //    progress_bar.setVisibility(View.GONE);
                Toast.makeText(StoriesActivity.this, "Downloaded Successfully", Toast.LENGTH_SHORT).show();
            } else {
                //   progress_bar.setVisibility(View.GONE);
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
                    Log.e("StoreViewActivity", "Server returned HTTP " + c.getResponseCode() + " " + c.getResponseMessage());

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
                Log.e("StoreViewActivity", "Download Error Exception " + e.getMessage());
            }

            return null;
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent e) {
            //  Toast.makeText(StoriesActivity.this, "onDown", Toast.LENGTH_SHORT).show();
            // don't return fa  lse here or else none of the other
            // gestures will work

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getRawX();
            if (x<=250){
                if (storyArrayList.size()!=0)
                    storiesProgressView.reverse();
                // Toast.makeText(StoriesActivity.this, "onSingleTap left", Toast.LENGTH_SHORT).show();
            }else if (x>=500){
                if (storyArrayList.size()!=0)
                    storiesProgressView.skip();
                //    Toast.makeText(StoriesActivity.this, "onSingleTap right", Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN){
                pressTime = System.currentTimeMillis();
                storiesProgressView.pause();
                /*if (sType.equals("video") && mp!=null){
                        mp.start();
                    }*/
            }

            if (e.getAction() == MotionEvent.ACTION_MOVE){
                long now = System.currentTimeMillis();
                storiesProgressView.resume();
                // return limit < now - pressTime;
                //lastDuration = System.currentTimeMillis() - lastDown;
            }
            //  Toast.makeText(StoriesActivity.this, "onLongPress", Toast.LENGTH_SHORT).show();*/

        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //  storiesProgressView.resume();
            //  Toast.makeText(StoriesActivity.this, "onDoubleTap", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
          /*  if (e1.getX() < e2.getX()) {
                Toast.makeText(StoriesActivity.this, "swipe right", Toast.LENGTH_SHORT).show();
            }

            if (e1.getX() > e2.getX()) {
                Toast.makeText(StoriesActivity.this, "swipe left", Toast.LENGTH_SHORT).show();
            }
            if (e1.getY() < e2.getY()) {
                Toast.makeText(StoriesActivity.this, "swipe down", Toast.LENGTH_SHORT).show();
            }
            if (e1.getY() > e2.getY()) {
                Toast.makeText(StoriesActivity.this, "swipe up", Toast.LENGTH_SHORT).show();
            }*/
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            try {

                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
                    return false;
                }

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        if (diffX > 0) {
                            position--;
                            if (position==liveUserList.size()){
                                finish();
                            }else {
                                storiesProgressView.destroy();
                                storiesProgressView.setStoriesListener(StoriesActivity.this);
                                apiForMyStory();
                            }
                            // Toast.makeText(StoriesActivity.this, "swipe right", Toast.LENGTH_SHORT).show();
                        } else {
                            position++;
                            if (position==liveUserList.size()){
                                finish();
                            }else {
                                storiesProgressView.destroy();
                                storiesProgressView.setStoriesListener(StoriesActivity.this);
                                apiForMyStory();
                            }
                            // Toast.makeText(StoriesActivity.this, "swipe left", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        if (diffY > 0) {
                            finish();
                            //  Toast.makeText(StoriesActivity.this, "swipe down", Toast.LENGTH_SHORT).show();
                        } else {
                            // Toast.makeText(StoriesActivity.this, "swipe up", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }




        /*    try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
                    return false;
                }
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    position++;
                    if (position==liveUserList.size()){
                        finish();
                    }else {
                        storiesProgressView.destroy();
                        storiesProgressView.setStoriesListener(StoriesActivity.this);
                        apiForMyStory();
                    }
                    // Toast.makeText(StoriesActivity.this, "swipe left", Toast.LENGTH_SHORT).show();
                }
                // left to right swipe
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    position--;
                    if (position==liveUserList.size()){
                        finish();
                    }else {
                        storiesProgressView.destroy();
                        storiesProgressView.setStoriesListener(StoriesActivity.this);
                        apiForMyStory();
                    }
                    //Toast.makeText(StoriesActivity.this, "swipe right", Toast.LENGTH_SHORT).show();
                }
                else   if (Math.abs(diffY) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    if (diffY > 0) {
                        finish();
                        //     Toast.makeText(StoriesActivity.this, "swipe down", Toast.LENGTH_SHORT).show();
                    }else {
                        //  Toast.makeText(StoriesActivity.this, "swipe up", Toast.LENGTH_SHORT).show();

                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }*/
            return false;
        }
    }

}
