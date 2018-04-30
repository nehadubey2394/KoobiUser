package com.mualab.org.user.activity.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.media.ImageVideoUtil;
import com.otaliastudios.cameraview.AspectRatio;
import com.otaliastudios.cameraview.CameraException;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraUtils;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;
import com.otaliastudios.cameraview.SessionType;
import com.otaliastudios.cameraview.SizeSelector;
import com.otaliastudios.cameraview.SizeSelectors;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import views.scaleview.ImageSource;
import views.scaleview.ScaleImageView;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int STATE_TAKE_PHOTO = 0;
    private static final int STATE_TAKE_VIDEO = 1;
    private static final int STATE_SETUP_PHOTO = 3;
    private static final int STATE_SETUP_VIDEO = 4;

    private View vShutter;
    private ScaleImageView ivTakenPhoto;
    private ViewSwitcher vUpperPanel;
    private ViewSwitcher vLowerPanel;
    private CameraView cameraView;
    private VideoView videoView;
    private boolean isCameraSession;
    private boolean isStartRecord;

    private Button btnTakePhoto;
    private ImageButton btnCameraMode, btnFlashLight;
    //private Chronometer mChronometer;

    private int currentState;
    private File photoPath;
    private Uri captureMediaUri;
    private boolean isVideoUri;

   /* long pressTime = 0L;
    long limit = 500L;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(!isCameraSession){
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressTime = System.currentTimeMillis();
                        startRecording();
                        return false;
                    case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
                        if(countDownTimer!=null) countDownTimer.onFinish();
                        //stopRecording();
                        return limit < now - pressTime;
                }
            }
            return false;
        }
    };*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initView();

        updateState(STATE_TAKE_PHOTO);
        cameraView.mapGesture(Gesture.PINCH, GestureAction.ZOOM); // Pinch to zoom!
        cameraView.mapGesture(Gesture.TAP, GestureAction.FOCUS_WITH_MARKER); // Tap to focus!
        cameraView.mapGesture(Gesture.LONG_TAP, GestureAction.CAPTURE); //

        SizeSelector width = SizeSelectors.minWidth(1000);
        SizeSelector height = SizeSelectors.minHeight(2000);
        SizeSelector dimensions = SizeSelectors.and(width, height); // Matches sizes bigger than 1000x2000.
        SizeSelector ratio = SizeSelectors.aspectRatio(AspectRatio.of(1, 1), 0); // Matches 1:1 sizes.
        SizeSelector result = SizeSelectors.or(
                SizeSelectors.and(ratio, dimensions), // Try to match both constraints
                ratio, // If none is found, at least try to match the aspect ratio
                SizeSelectors.biggest()); // If none is// found, take the biggest

        cameraView.setPictureSize(result);

        cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened(CameraOptions options) {
                super.onCameraOpened(options);
            }

            @Override
            public void onCameraClosed() {
                super.onCameraClosed();
            }

            @Override
            public void onCameraError(@NonNull CameraException exception) {
                super.onCameraError(exception);
                showToast(exception.getLocalizedMessage());
            }

            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);
                Progress.showProgressOnly(CameraActivity.this);
                CameraUtils.decodeBitmap(jpeg, 3000, 3000, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        cameraView.stop();
                        showTakenPicture(bitmap);
                        isVideoUri = false;
                        Progress.hide(CameraActivity.this);
                        /*File f = new File(CameraActivity.this.getCacheDir(), "tmp.jpg");
                        try {

                            f.createNewFile();
                            //Convert bitmap to byte array
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 80 *//*ignored for PNG*//*, bos);
                            byte[] bitmapdata = bos.toByteArray();
                            //write the bytes in file
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();

                            //mSelected = new ArrayList<>();
                           // mSelected.add(Uri.fromFile(f));
                            captureMediaUri = Uri.fromFile(f);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }
                });
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                cameraView.stop();
                captureMediaUri = Uri.fromFile(video);
                vUpperPanel.showNext();
                vLowerPanel.showNext();
                updateState(STATE_SETUP_VIDEO);
                videoView.setVideoURI(captureMediaUri);
                isVideoUri = true;
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                ViewGroup.LayoutParams lp = videoView.getLayoutParams();
                float videoWidth = mp.getVideoWidth();
                float videoHeight = mp.getVideoHeight();
                float viewWidth = videoView.getWidth();
                lp.height = (int) (viewWidth * (videoHeight / videoWidth));
                videoView.setLayoutParams(lp);
                playVideo();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
    }

    private void showToast(String str){
        if(!TextUtils.isEmpty(str))
            MyToast.getInstance(this).showSmallCustomToast(str);
    }

    private void initView(){
        //vTakePhotoRoot = findViewById(R.id.vPhotoRoot);
        vShutter = findViewById(R.id.vShutter);
        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);
        vUpperPanel = findViewById(R.id.vUpperPanel);
        vLowerPanel = findViewById(R.id.vLowerPanel);
        //rvFilters = findViewById(R.id.rvFilters);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnCameraMode = findViewById(R.id.btnCameraMode);
        btnFlashLight = findViewById(R.id.btnFlashLight);
        //ivBack = findViewById(R.id.ivBack);

        cameraView = findViewById(R.id.camera);
        videoView = findViewById(R.id.videoPreview);
        //mChronometer = findViewById(R.id.tvVideoTimer);

        btnTakePhoto.setOnClickListener(this);
        btnFlashLight.setOnClickListener(this);
       // findViewById(R.id.btnAccept).setOnClickListener(this);
        findViewById(R.id.retry).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.switchCamera).setOnClickListener(this);
        findViewById(R.id.btnCameraMode).setOnClickListener(this);
        findViewById(R.id.add_to_story).setOnClickListener(this);
        isCameraSession = true;
    }

    private void animateShutter() {
        vShutter.setVisibility(View.VISIBLE);
        vShutter.setAlpha(0.f);

        ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0f, 0.8f);
        alphaInAnim.setDuration(100);
        alphaInAnim.setStartDelay(100);
        alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0.8f, 0f);
        alphaOutAnim.setDuration(200);
        alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                vShutter.setVisibility(View.GONE);
            }
        });
        animatorSet.start();
    }


    @Override
    public void onClick(View v) {
        // PopupVideoHintManager.getInstance().dismiss();
        switch (v.getId()){

            case R.id.btnTakePhoto:

                if(currentState == STATE_TAKE_PHOTO){
                    btnTakePhoto.setEnabled(false);
                    cameraView.capturePicture();
                    animateShutter();

                }else if(currentState == STATE_TAKE_VIDEO){
                    if(isStartRecord){
                        btnTakePhoto.setBackgroundResource(R.drawable.btn_capture_video);
                        if(countDownTimer!=null) countDownTimer.onFinish();
                       // stopRecording();
                    }else {
                        btnTakePhoto.setBackgroundResource(R.drawable.btn_capture_video_active);
                        btnTakePhoto.setEnabled(false);
                        //startRecording();
                        startTimear();
                    }
                }
                break;

            case R.id.btnAccept:
                //PublishActivity.openWithPhotoUri(this, Uri.fromFile(photoPath));


                /*Intent intent = null;
                if (mSelected != null && mSelected.size() > 0) {
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", "");
                    intent.putExtra("feedType", Constant.IMAGE_STATE);
                    intent.putExtra("fromGallery", false);
                    intent.putParcelableArrayListExtra("imageUri", new ArrayList<Parcelable>(mSelected));
                } else if(videoUri!=null){
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", "");
                    intent.putExtra("feedType", Constant.VIDEO_STATE);
                    intent.putExtra("videoUri", videoUri.toString());
                    intent.putExtra("fromGallery", false);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                }

                if (intent != null) {
                    // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, Constant.POST_FEED_DATA);
                }*/
                break;


            case R.id.add_to_story:
                addMyStory();
                break;


            case R.id.retry:
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.switchCamera:
                if(cameraView.getFacing()== Facing.BACK){
                    cameraView.setFacing(Facing.FRONT);
                }else {
                    cameraView.setFacing(Facing.BACK);
                }
                break;

            case R.id.btnFlashLight:

                Flash flashMode = cameraView.getFlash();
                if(flashMode ==null){
                    flashMode = Flash.OFF;
                }

                if(isCameraSession){

                    if(flashMode == Flash.OFF){
                        flashMode = Flash.ON;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_on_white);

                    }else if(flashMode ==Flash.ON){
                        flashMode = Flash.AUTO;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_auto_white);

                    }else if(flashMode == Flash.AUTO){
                        flashMode = Flash.OFF;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_off_white);
                    }
                }else {

                    if(flashMode == Flash.TORCH || flashMode ==Flash.ON){
                        flashMode = Flash.OFF;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_off_white);

                    }else if(flashMode == Flash.OFF){
                        flashMode = Flash.TORCH;
                        cameraView.setFlash(flashMode);
                        btnFlashLight.setImageResource(R.drawable.ic_flash_on_white);
                    }
                }
                break;

            case R.id.btnCameraMode:
                changeCameraSessionMode();
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private synchronized void changeCameraSessionMode(){
        cameraView.setFlash(Flash.OFF);
        btnFlashLight.setImageResource(R.drawable.ic_flash_off_white);
        if(isCameraSession){
            isCameraSession = false;
            currentState = STATE_TAKE_VIDEO;
            cameraView.setSessionType(SessionType.VIDEO);
            btnTakePhoto.setText(R.string.rec);
            btnTakePhoto.setBackgroundResource(R.drawable.btn_capture_video);
            btnCameraMode.setImageResource(R.drawable.ic_photo_camera_white);
            updateState(STATE_TAKE_VIDEO);
            //btnTakePhoto.setOnTouchListener(onTouchListener);
        }else {
            isCameraSession = true;
            btnCameraMode.setImageResource(R.drawable.ic_videocam_white);
            currentState = STATE_TAKE_PHOTO;
            btnTakePhoto.setText("");
            updateState(currentState);
            //btnTakePhoto.setOnTouchListener(null);
            btnTakePhoto.setOnClickListener(this);
            cameraView.setSessionType(SessionType.PICTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == Constant.POST_FEED_DATA){
            finish();
        }
    }



    void playVideo() {
        if (videoView.isPlaying()) return;
        videoView.start();
    }

    private void stopVideo(){
        videoView.setVisibility(View.GONE);
        if(videoView.isPlaying()){
            videoView.pause();
            videoView.stopPlayback();
        }
    }

    private void stopRecording(){
        if(!isCameraSession || isStartRecord){
            cameraView.stopCapturingVideo();
            //mChronometer.stop();\
            if(countDownTimer!=null)
                countDownTimer.cancel();
        }
    }

    private void startRecording(){
        if(cameraView.getSessionType()!=SessionType.PICTURE){
            try {
                File file = CameraActivity.this.getExternalCacheDir();
                if (file != null) {
                    isStartRecord = true;
                    //mChronometer.setBase(SystemClock.elapsedRealtime());
                    photoPath = new File(file.getPath(), "tmp.mp4");
                    cameraView.startCapturingVideo(photoPath);
                    //mChronometer.start();
                    showToast("Start Recording...");
                }

            } catch (Exception e) {
                e.printStackTrace();
                isStartRecord = false;
            }
        }
        else btnTakePhoto.setEnabled(true);
    }

    //boolean isStartRecord;
    /*private void startRecording(){
        if(isStartRecord && mChronometer.getDrawingTime()>1){

            try {
                cameraView.stopCapturingVideo();
                cameraView.startCapturingVideo(photoPath);
                showToast("Stop Recording...");
                mChronometer.stop();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }else {

            if(handler!=null){
                handler.removeCallbacks(runnable);
                handler = null;
                photoPath = null;
                videoUri = null;
                mChronometer.stop();
                showToast("Remove callback");
            }else {

                handler= new Handler();
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File file = CameraActivity.this.getExternalCacheDir();
                            if (file != null) {
                                isStartRecord = true;
                                mChronometer.setBase(SystemClock.elapsedRealtime());
                                photoPath = new File(file.getPath(), "tmp.mp4");
                                cameraView.startCapturingVideo(photoPath);
                                mChronometer.start();
                                showToast("Start Recording...");
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            isStartRecord = false;
                        }
                    }
                },1000);
            }
        }
    }*/


    @Override
    public void onBackPressed() {
        if (currentState == STATE_SETUP_PHOTO) {
            btnTakePhoto.setEnabled(true);
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            updateState(STATE_TAKE_PHOTO);
            cameraView.start();
        } else if (currentState == STATE_SETUP_VIDEO) {
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            cameraView.start();
            updateState(STATE_TAKE_VIDEO);

            try{
                captureMediaUri = null;
                if(photoPath!=null)
                    photoPath.delete();
            } catch (Exception e){
                e.printStackTrace();
            }
        }else  {
            super.onBackPressed();
        }
    }



   /* private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }
*/

    private void showTakenPicture(Bitmap bitmap) {
        vUpperPanel.showNext();
        vLowerPanel.showNext();
        ivTakenPhoto.setImage(ImageSource.bitmap(bitmap));
        updateState(STATE_SETUP_PHOTO);
    }


    private void updateState(int state) {
        currentState = state;
        if (currentState == STATE_TAKE_PHOTO) {
            //mChronometer.setVisibility(View.GONE);
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivTakenPhoto.setVisibility(View.GONE);
                }
            }, 400);
        }else if(currentState==STATE_TAKE_VIDEO){
            //mChronometer.setVisibility(View.VISIBLE);
            //mChronometer.setBase(SystemClock.elapsedRealtime());
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivTakenPhoto.setVisibility(View.GONE);
                    stopVideo();
                }
            }, 400);

        }else if(currentState == STATE_SETUP_VIDEO){
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            videoView.setVisibility(View.VISIBLE);
            //videoView.setVideoPath(videoUri.getPath());

        }else if (currentState == STATE_SETUP_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            ivTakenPhoto.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
        }else {
            if(videoView.isPlaying()){
                videoView.pause();
                videoView.stopPlayback();
            }
            videoView.setVisibility(View.GONE);
        }
    }


    private void addMyStory(){

        if(ConnectionDetector.isConnected()){

            Map<String,String> map = new HashMap<>();
            map.put("userId", ""+Mualab.currentUser.id);
            map.put("type", isVideoUri?"video":"image");
            if(isVideoUri){

                ivTakenPhoto.setDrawingCacheEnabled(true);
               Bitmap bitmap = ivTakenPhoto.getDrawingCache();
              /* Bitmap bitmap = ImageVideoUtil.getVideoToThumbnil(photoPath., this,
                       MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);*/
                HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                    @Override
                    public void onResponse(String response, String apiName) {
                        try {
                            JSONObject js = new JSONObject(response);
                            String status = js.getString("status");
                            String message = js.getString("message");
                            if (status.equalsIgnoreCase("success")) {
                                finish();
                            }
                            else showToast(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void ErrorListener(VolleyError error) {
                        Log.d("res:", ""+error.getLocalizedMessage());
                    }})
                        .setParam(map)
                        .setAuthToken(Mualab.currentUser.authToken)
                        .setProgress(true));
                task.postFile("myStory", photoPath, bitmap);

            }else {
                Bitmap bitmap = ivTakenPhoto.getBitmap();
                HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                    @Override
                    public void onResponse(String response, String apiName) {
                        try {
                            JSONObject js = new JSONObject(response);
                            String status = js.getString("status");
                            String message = js.getString("message");
                            if (status.equalsIgnoreCase("success")) {
                                Mualab.isStoryUploaded = true;
                                finish();
                            }
                            else showToast(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void ErrorListener(VolleyError error) {
                        Log.d("res:", ""+error.getLocalizedMessage());
                    }})
                        .setParam(map)
                        .setAuthToken(Mualab.currentUser.authToken)
                        .setProgress(true));
                task.postImage("myStory", bitmap);
            }

        }else showToast(getString(R.string.error_msg_network));
    }


    private CountDownTimer countDownTimer;
    private boolean timerIsRunning;
    private void startTimear(){
        if(countDownTimer!=null)
            countDownTimer.cancel();
        timerIsRunning = true;
        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                btnTakePhoto.setText(String.valueOf(millisUntilFinished / 1000));
                if(57000<millisUntilFinished)
                    btnTakePhoto.setEnabled(true);
            }

            public void onFinish() {
                timerIsRunning = false;
                btnTakePhoto.setText("REC");
                btnTakePhoto.setEnabled(true);
                if(isStartRecord)
                    stopRecording();
            }
        }.start();
        startRecording();
    }
}
