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
import android.view.View;
import android.view.Window;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.story.draj_camera.internal.BaseGalleryFragment;
import com.mualab.org.user.activity.story.draj_camera.internal.CameraIntentKey;
import com.mualab.org.user.activity.story.draj_camera.internal.CameraUriInterface;
import com.mualab.org.user.activity.story.draj_camera.internal.PlaybackVideoFragment;
import com.mualab.org.user.activity.story.draj_camera.util.CameraUtil;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dharmraj on 8/3/18.
 **/

public abstract class BaseStoryActivity extends AppCompatActivity implements BaseStoryInterface{

    public static final int PERMISSION_RC = 69;

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


    private int mCameraPosition = CAMERA_POSITION_FRONT;
    private int mFlashMode = FLASH_MODE_OFF;
    private boolean mRequestingPermission;
    private long mRecordingStart = -1;
    private long mRecordingEnd = -1;
    private long mLengthLimit = -1;


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
    protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_RC) showInitialRecorder();
    }


    @Override
    public void onBackPressed() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (frag != null) {
            if (frag instanceof PlaybackVideoFragment && allowRetry()) {
                onRetry(((CameraUriInterface) frag).getOutputUri());
                return;
            } else if (frag instanceof CaptureStoryFragment) {
                ((CaptureStoryFragment) frag).cleanup();
            } else if (frag instanceof BaseGalleryFragment && allowRetry()) {
                onRetry(((CameraUriInterface) frag).getOutputUri());
                return;
            }
        }
        else super.onBackPressed();
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

    @StringRes
    @Override
    public int labelConfirm() {
        return getIntent()
                .getIntExtra(
                        CameraIntentKey.LABEL_CONFIRM,
                        useStillshot() ? R.string.mcam_use_stillshot : R.string.mcam_use_video);
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
        return getIntent().getBooleanExtra(CameraIntentKey.STILL_SHOT, false);
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
    public void setFlashModes(int modes) {
        mFlashMode = modes;
    }

    @Override
    public void setFlashModeVideo(int mFlashMode) {
        this.mFlashMode = mFlashMode;
    }

    @Override
    public boolean shouldHideCameraFacing() {
        return !getIntent().getBooleanExtra(CameraIntentKey.ALLOW_CHANGE_CAMERA, false);
    }
}
