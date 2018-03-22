package com.mualab.org.user.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;

import com.mualab.org.user.R;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
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
import com.mualab.org.user.util.ScreenUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddStoryActivity extends AppCompatActivity implements View.OnClickListener{

    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

    private static final int STATE_TAKE_PHOTO = 0;
    private static final int STATE_TAKE_VIDEO = 1;
    private static final int STATE_SETUP_PHOTO = 3;
    private static final int STATE_SETUP_VIDEO = 4;

    View vTakePhotoRoot;
    View vShutter;
    ImageView ivTakenPhoto;
    ViewSwitcher vUpperPanel;
    ViewSwitcher vLowerPanel;
    CameraView cameraView;
    VideoView videoPreview;
    boolean isCameraSession;
    // RecyclerView rvFilters;
    Button btnTakePhoto;
    ImageButton btnCameraMode, btnFlashLight;
    Chronometer mChronometer;


    private int currentState;
    private Flash flashMode;

    private File photoPath;
    private Uri videoUri;
    private List<Uri> mSelected;

    // video record support variables.
    private View.OnTouchListener touchListener;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        initView();
        updateStatusBarColor();
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
                SizeSelectors.biggest() // If none is// found, take the biggest
        );
        cameraView.setPictureSize(result);

        //final GestureDetector gestureDetector = new GestureDetector(mContext, new SingleTapConfirm());
        touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction()==MotionEvent.ACTION_DOWN ){
                    recordVideo();
                    Toast.makeText(AddStoryActivity.this, "ACTION_DOWN", Toast.LENGTH_SHORT).show();
                }
                else if (event.getAction()==MotionEvent.ACTION_UP){
                    recordVideo();
                    Toast.makeText(AddStoryActivity.this, "ACTION_UP", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

        };

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
            }

            @Override
            public void onPictureTaken(byte[] jpeg) {
                super.onPictureTaken(jpeg);

                CameraUtils.decodeBitmap(jpeg, 1000, 1000, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {
                        showTakenPicture(bitmap);

                        File f = new File(AddStoryActivity.this.getCacheDir(), "tmp.jpg");
                        try {

                            f.createNewFile();
                            //Convert bitmap to byte array
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 80 /*ignored for PNG*/, bos);
                            byte[] bitmapdata = bos.toByteArray();

                            //write the bytes in file
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(bitmapdata);
                            fos.flush();
                            fos.close();

                            mSelected = new ArrayList<>();
                            mSelected.add(Uri.fromFile(f));


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                videoUri = Uri.fromFile(video);
                updateState(STATE_SETUP_VIDEO);
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
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

        vTakePhotoRoot = findViewById(R.id.vPhotoRoot);
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
        videoPreview = findViewById(R.id.videoPreview);
        mChronometer = findViewById(R.id.tvVideoTimer);

        btnTakePhoto.setOnClickListener(this);
        btnFlashLight.setOnClickListener(this);
        findViewById(R.id.btnAccept).setOnClickListener(this);
        findViewById(R.id.ivBack).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.switchCamera).setOnClickListener(this);
        findViewById(R.id.btnCameraMode).setOnClickListener(this);
        isCameraSession = true;

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateStatusBarColor() {
        if (ScreenUtils.isAndroid5()) {
            getWindow().setStatusBarColor(0xff111111);
        }
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

    private void startIntroAnimation() {
        vUpperPanel.animate().translationY(0).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR);
        vLowerPanel.animate().translationY(0).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR).start();
    }

   /* @Override
    public CameraHost getCameraHost() {
        return new MyCameraHost(mContext);
    }*/

    @Override
    public void onClick(View v) {
       // PopupVideoHintManager.getInstance().dismiss();
        switch (v.getId()){

            case R.id.btnTakePhoto:

                if(currentState== STATE_TAKE_PHOTO){
                    btnTakePhoto.setEnabled(false);
                    cameraView.capturePicture();
                    animateShutter();

                }else if(currentState == STATE_SETUP_VIDEO){
                  //  PopupVideoHintManager.getInstance().toggleFromView(btnTakePhoto);
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

            case R.id.ivBack:
            case R.id.btnBack:
                onBackPress();
                break;


            /*case R.id.record:
                recordVideo();
                break;*/

            case R.id.switchCamera:
                if(cameraView.getFacing()== Facing.BACK){
                    cameraView.setFacing(Facing.FRONT);
                }else {
                    cameraView.setFacing(Facing.BACK);
                }
                break;

            case R.id.btnFlashLight:

                flashMode =  cameraView.getFlash();
                if(flashMode==null){
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

               /* if(currentState == STATE_SETUP_VIDEO){



                }else {


                }*/
                break;

            case R.id.btnCameraMode:
                flashMode = Flash.OFF;
                cameraView.setFlash(flashMode);
                btnFlashLight.setImageResource(R.drawable.ic_flash_off_white);

                if(currentState == STATE_TAKE_VIDEO){
                    isCameraSession = true;
                    btnCameraMode.setImageResource(R.drawable.ic_videocam_white);
                    currentState = STATE_TAKE_PHOTO;
                    btnTakePhoto.setText("");
                    updateState(currentState);
                    btnTakePhoto.setOnTouchListener(null);
                    btnTakePhoto.setOnClickListener(this);
                    cameraView.setSessionType(SessionType.PICTURE);

                }else if(currentState == STATE_TAKE_PHOTO){
                    isCameraSession = false;
                    currentState = STATE_TAKE_VIDEO;
                    cameraView.setSessionType(SessionType.VIDEO);
                    btnTakePhoto.setText("REC");
                    btnCameraMode.setImageResource(R.drawable.ic_photo_camera_white);
                    updateState(STATE_TAKE_VIDEO);
                    btnTakePhoto.setOnTouchListener(touchListener);
                }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK && requestCode == Constant.POST_FEED_DATA){
            finish();
        }
    }

    boolean isStartRecord;
    private void recordVideo(){
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
                showToast("Remove callback");
                photoPath = null;
                videoUri = null;
                mChronometer.stop();
            }else {

                handler= new Handler();
                handler.postDelayed(runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File file = AddStoryActivity.this.getExternalCacheDir();
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
    }



    private void onBackPress(){
        if (currentState == STATE_SETUP_PHOTO) {
            btnTakePhoto.setEnabled(true);
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            updateState(STATE_TAKE_PHOTO);
        } else if (currentState == STATE_SETUP_VIDEO) {
            btnTakePhoto.setEnabled(true);
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            updateState(STATE_TAKE_VIDEO);
        }else  {
            onBackPressed();
        }
    }


    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }


    private void showTakenPicture(Bitmap bitmap) {
        vUpperPanel.showNext();
        vLowerPanel.showNext();
        ivTakenPhoto.setImageBitmap(bitmap);
        updateState(STATE_SETUP_PHOTO);
    }

   /* @Override
    public void onBackPressed() {
        if (currentState == STATE_SETUP_PHOTO) {
            btnTakePhoto.setEnabled(true);
            vUpperPanel.showNext();
            vLowerPanel.showNext();
            updateState(STATE_TAKE_PHOTO);
        } else {
            super.onBackPressed();
        }
    }*/

    private void updateState(int state) {
        currentState = state;
        if (currentState == STATE_TAKE_PHOTO) {
            mChronometer.setVisibility(View.GONE);
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
            mChronometer.setVisibility(View.VISIBLE);
            mChronometer.setBase(SystemClock.elapsedRealtime());
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ivTakenPhoto.setVisibility(View.GONE);
                    if(videoPreview.isPlaying()){
                        videoPreview.pause();
                        videoPreview.stopPlayback();
                    }
                }
            }, 400);

        }else if(currentState == STATE_SETUP_VIDEO && videoUri!=null){
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(videoUri.getPath());

        }else if (currentState == STATE_SETUP_PHOTO) {
            vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
            vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
            ivTakenPhoto.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.GONE);
        }else {
            if(videoPreview.isPlaying()){
                videoPreview.pause();
                videoPreview.stopPlayback();
            }
        }
    }

}
