package com.mualab.org.user.activity.story.core;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.story.camera.TimeLimitReachedException;
import com.mualab.org.user.activity.story.camera.internal.CameraIntentKey;
import com.mualab.org.user.activity.story.camera.internal.CameraUriInterface;
import com.mualab.org.user.activity.story.camera.util.CameraUtil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dharmraj on 8/3/18.
 **/

public abstract class BaseStoryActivity extends AppCompatActivity implements BaseStoryInterface{

    public static final int PERMISSION_RC = 69;
    private static final String TAG = BaseStoryActivity.class.getName();

    @IntDef({CAMERA_POSITION_UNKNOWN, CAMERA_POSITION_BACK, CAMERA_POSITION_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraPosition {}
    public static final int CAMERA_POSITION_UNKNOWN = 0;
    public static final int CAMERA_POSITION_FRONT = 1;
    public static final int CAMERA_POSITION_BACK = 2;

    @IntDef({FLASH_MODE_OFF, FLASH_MODE_ON, FLASH_MODE_AUTO, FLASH_MODE_TOURCH, })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlashMode {}
    public static final int FLASH_MODE_OFF = 0;
    public static final int FLASH_MODE_ON = 1;
    public static final int FLASH_MODE_AUTO = 2;
    public static final int FLASH_MODE_TOURCH = 3;

    @IntDef({CAMERA_MODE_PICTURE, CAMERA_MODE_VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CameraMode {}
    public static final int CAMERA_MODE_PICTURE = 0;
    public static final int CAMERA_MODE_VIDEO = 1;

    private int mFlashMode = FLASH_MODE_OFF;
    private int mCameraMode = CAMERA_MODE_PICTURE;
    private int mCameraPosition = CAMERA_POSITION_FRONT;

    private boolean mRequestingPermission;
    private long mRecordingStart = -1;
    private long mRecordingEnd = -1;
    private long mLengthLimit = -1;
    private boolean mDidRecord = false;
    private boolean isFlashModesVideo = false;
    //private List<Integer> mFlashModes;


    private String mUri = null;
    private String mUploadUri = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mcam_activity_videocapture);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int primaryColor = getIntent().getIntExtra(CameraIntentKey.PRIMARY_COLOR, 0);
            final boolean isPrimaryDark = CameraUtil.isColorDark(primaryColor);
            final Window window = getWindow();
            window.setStatusBarColor(CameraUtil.darkenColor(primaryColor));
            window.setNavigationBarColor(isPrimaryDark ? primaryColor : Color.BLUE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final View view = window.getDecorView();
                int flags = view.getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                view.setSystemUiVisibility(flags);
            }
        }


        if (null == savedInstanceState) {
            checkPermissions();
            mLengthLimit = getIntent().getLongExtra(CameraIntentKey.LENGTH_LIMIT, -1);
        } else {
            mCameraPosition = savedInstanceState.getInt("camera_position", -1);
            mRequestingPermission = savedInstanceState.getBoolean("requesting_permission", false);
            mRecordingStart = savedInstanceState.getLong("recording_start", -1);
            mRecordingEnd = savedInstanceState.getLong("recording_end", -1);
            mLengthLimit = savedInstanceState.getLong(CameraIntentKey.LENGTH_LIMIT, -1);
            mFlashMode = savedInstanceState.getInt("flash_mode");
        }

    }


    private void checkPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showInitialRecorder();
            return;
        }
        final boolean cameraGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED;
        final boolean audioGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED;

        final boolean audioNeeded = !useStillshot();

        String[] perms = null;
        if (cameraGranted) {
            if (audioNeeded && !audioGranted) {
                perms = new String[] {Manifest.permission.RECORD_AUDIO};
            }
        } else {
            if (audioNeeded && !audioGranted) {
                perms = new String[] {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
            } else {
                perms = new String[] {Manifest.permission.CAMERA};
            }
        }

        if (perms != null) {
            ActivityCompat.requestPermissions(this, perms, PERMISSION_RC);
            mRequestingPermission = true;
        } else {
            showInitialRecorder();
        }
    }

    @NonNull
    public abstract Fragment getFragment();

    public final Fragment createFragment() {
        Fragment frag = getFragment();
        frag.setArguments(getIntent().getExtras());
        return frag;
    }

    private void showInitialRecorder() {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, createFragment()).commit();
    }


    @Override
    public final void onRetry(@Nullable String outputUri) {
        //if (outputUri != null) deleteOutputFile(outputUri);
        //if (!shouldAutoSubmit() || restartTimerOnRetry()) setRecordingStart(-1);
        if (getIntent().getBooleanExtra(CameraIntentKey.RETRY_EXITS, false)) {
            setResult(
                    RESULT_OK,
                    new Intent().putExtra(RjCamera.STATUS_EXTRA, RjCamera.STATUS_RETRY));
            finish();
            return;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, createFragment()).commit();
    }


    @Override
    public final void useMedia(String uri) {
        if (uri != null) {
            Log.d(TAG, "useMedia: upload uri: " + uri);
            mUri = uri;
//      setResult(
//          Activity.RESULT_OK,
//          getIntent()
//              .putExtra(MaterialCamera.STATUS_EXTRA, MaterialCamera.STATUS_RECORDED)
//              .setDataAndType(Uri.parse(uri), useStillshot() ? "image/jpeg" : "video/mp4"));
            //saveMediaToMemory(uri);
        }
//    finish();
    }


    @Override
    protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_RC) showInitialRecorder();
    }


    @Override
    public void onBackPressed() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (frag != null) {
            if (frag instanceof VideoPreviewFragment && allowRetry()) {
                onRetry(((CameraUriInterface) frag).getOutputUri());
                return;
            } else if (frag instanceof CaptureStoryFragment) {
                ((CaptureStoryFragment) frag).cleanup();
            } else if (frag instanceof PhotoPreviewFragment && allowRetry()) {
                onRetry(((CameraUriInterface) frag).getOutputUri());
                return;
            }
        }
        super.onBackPressed();
    }


    @Override
    public final boolean shouldAutoSubmit() {
        return getIntent().getBooleanExtra(CameraIntentKey.AUTO_SUBMIT, false);
    }

    @Override
    public void onShowStillshot(String outputUri) {
        if (shouldAutoSubmit()) {
            useMedia(outputUri);
        } else {
            Fragment frag = PhotoPreviewFragment.newInstance(outputUri, allowRetry(), getIntent().getIntExtra(CameraIntentKey.PRIMARY_COLOR, 0));
            getSupportFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
        }
    }


    @Override
    public final void onShowPreview(@Nullable final String outputUri, boolean countdownIsAtZero) {
        if ((shouldAutoSubmit() && (countdownIsAtZero || !allowRetry() || !hasLengthLimit())) || outputUri == null) {
            if (outputUri == null) {
                setResult(
                        RESULT_CANCELED,
                        new Intent().putExtra(RjCamera.ERROR_EXTRA, new TimeLimitReachedException()));
                finish();
                return;
            }
            useMedia(outputUri);
        } else {
            if (!hasLengthLimit() || !continueTimerInPlayback()) {
                // No countdown or countdown should not continue through playback, reset timer to 0
                setRecordingStart(-1);
            }
            Fragment frag = VideoPreviewFragment.newInstance(outputUri, allowRetry(), getIntent().getIntExtra(CameraIntentKey.PRIMARY_COLOR, 0));
            getSupportFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
        }
    }

    @Override
    public void addToStory(String uri) {
        Log.d(TAG, "addToStory: adding file to story.");
       /* initProgressBar();
        if(isMediaVideo(uri)){
            if(mUploadUri == null){
                Log.d(TAG, "addToStory: Video was not saved. Beginning compression.");
                mDeleteCompressedMedia = true;
                saveTempAndCompress(uri);
            }
            else{
                Log.d(TAG, "addToStory: video has been saved. Now uploading.");
                Log.d(TAG, "addToStory: upload uri: " + mUploadUri);
                finishActivityAndUpload();
            }
        }
        else{
            if(mUploadUri == null){
                Log.d(TAG, "addToStory: Image was not saved. Now uploading");
                mDeleteCompressedMedia = true;
                mUploadUri = uri;
                finishActivityAndUpload();
            }
            else{
                Log.d(TAG, "addToStory: Image has been saved. Now uploading.");
                Log.d(TAG, "addToStory: upload uri: " + mUploadUri);
                finishActivityAndUpload();
            }
        }*/

    }



    /*Video Camera methods*/

    @Override
    public void setRecordingStart(long start) {
        mRecordingStart = start;
        if (start > -1 && hasLengthLimit()) setRecordingEnd(mRecordingStart + getLengthLimit());
        else setRecordingEnd(-1);
    }

    @Override
    public long getRecordingStart() {
        return mRecordingStart;
    }

    @Override
    public void setRecordingEnd(long end) {
        mRecordingEnd = end;
    }

    @Override
    public long getRecordingEnd() {
        return mRecordingEnd;
    }

    @Override
    public long getLengthLimit() {
        return mLengthLimit;
    }

    @Override
    public boolean hasLengthLimit() {
        return getLengthLimit() > -1;
    }

    @Override
    public boolean countdownImmediately() {
        return getIntent().getBooleanExtra(CameraIntentKey.COUNTDOWN_IMMEDIATELY, false);
    }

    @Override
    public void setCameraPosition(int position) {
        mCameraPosition = position;
    }

    @Override
    public void toggleCameraPosition() {
        if (getCurrentCameraPosition() == CAMERA_POSITION_FRONT) {
            // Front, go to back if possible
           setCameraPosition(CAMERA_POSITION_BACK);
        } else {
            // Back, go to front if possible
            setCameraPosition(CAMERA_POSITION_FRONT);
        }
    }




    @Override
    public boolean restartTimerOnRetry() {
        return getIntent().getBooleanExtra(CameraIntentKey.RESTART_TIMER_ON_RETRY, false);
    }

    @Override
    public boolean continueTimerInPlayback() {
        return getIntent().getBooleanExtra(CameraIntentKey.CONTINUE_TIMER_IN_PLAYBACK, false);
    }

    @Override
    public int getCurrentCameraPosition() {
        return mCameraPosition;
    }

    @Override
    public final boolean allowRetry() {
        return getIntent().getBooleanExtra(CameraIntentKey.ALLOW_RETRY, true);
    }


    @Override
    public void setDidRecord(boolean didRecord) {
        mDidRecord = didRecord;
    }

    @Override
    public boolean didRecord() {
        return mDidRecord;
    }

    @Override
    public int getFlashMode() {
        return mFlashMode;
    }

    @Override
    public int getFlashModeVideo() {
        return mFlashMode;
    }


    @Override
    public void toggleFlashMode() {
        if (isFlashModesVideo) {
            if(mFlashMode == FLASH_MODE_TOURCH){
                mFlashMode = FLASH_MODE_OFF;
            } else{
                mFlashMode = FLASH_MODE_TOURCH;
            }
        }
        else {
            if(mFlashMode == FLASH_MODE_OFF){
                mFlashMode = FLASH_MODE_ON;
            } else if(mFlashMode == FLASH_MODE_ON){
                mFlashMode = FLASH_MODE_AUTO;
            }else if(mFlashMode == FLASH_MODE_AUTO){
                mFlashMode = FLASH_MODE_OFF;
            }
        }
    }

    @Override
    public void setCameraMode(int cameraMode) {
        mCameraMode = cameraMode;
    }

    @Override
    public void setFlashModes(boolean flashModes) {
        this.isFlashModesVideo = flashModes;
    }

    @Override
    public boolean shouldHideFlash() {
        return !useStillshot();
    }


    @DrawableRes
    @Override
    public int iconPause() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_PAUSE, R.drawable.evp_action_pause);
    }

    @DrawableRes
    @Override
    public int iconPlay() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_PLAY, R.drawable.evp_action_play);
    }

    @DrawableRes
    @Override
    public int iconRestart() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_RESTART, R.drawable.evp_action_restart);
    }

    @DrawableRes
    @Override
    public int iconRearCamera() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_REAR_CAMERA, R.drawable.ic_camera_rear_white);
    }

    @DrawableRes
    @Override
    public int iconFrontCamera() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_FRONT_CAMERA, R.drawable.ic_camera_rear_white);
    }

    @DrawableRes
    @Override
    public int iconStop() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_STOP, R.drawable.mcam_action_stop);
    }

    @DrawableRes
    @Override
    public int iconRecord() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_RECORD, R.drawable.mcam_action_capture);
    }

    @StringRes
    @Override
    public int labelRetry() {
        return getIntent().getIntExtra(CameraIntentKey.LABEL_RETRY, R.string.mcam_retry);
    }

    @Deprecated
    @StringRes
    @Override
    public int labelUseVideo() {
        return getIntent().getIntExtra(CameraIntentKey.LABEL_CONFIRM, R.string.mcam_use_video);
    }


    @DrawableRes
    @Override
    public int iconStillshot() {
        return getIntent()
                .getIntExtra(CameraIntentKey.ICON_STILL_SHOT, R.drawable.mcam_action_stillshot);
    }

//  @Override
//  public void setUseStillshot(boolean bool) {
//    getIntent().putExtra(CameraIntentKey.STILL_SHOT, bool);
//  }

    @Override
    public boolean useStillshot() {
        return mCameraMode == CAMERA_MODE_PICTURE;
    }

    @DrawableRes
    @Override
    public int iconFlashAuto() {
        return getIntent()
                .getIntExtra(CameraIntentKey.ICON_FLASH_AUTO, R.drawable.mcam_action_flash_auto);
    }

    @DrawableRes
    @Override
    public int iconFlashOn() {
        return getIntent().getIntExtra(CameraIntentKey.ICON_FLASH_ON, R.drawable.mcam_action_flash);
    }

    @DrawableRes
    @Override
    public int iconFlashOff() {
        return getIntent()
                .getIntExtra(CameraIntentKey.ICON_FLASH_OFF, R.drawable.mcam_action_flash_off);
    }


    @Override
    public boolean shouldHideCameraFacing() {
        return !getIntent().getBooleanExtra(CameraIntentKey.ALLOW_CHANGE_CAMERA, false);
    }

    @Override
    public long maxAllowedFileSize() {
        return getIntent().getLongExtra(CameraIntentKey.MAX_ALLOWED_FILE_SIZE, -1);
    }
}
