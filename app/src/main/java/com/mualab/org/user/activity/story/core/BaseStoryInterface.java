package com.mualab.org.user.activity.story.core;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.mualab.org.user.activity.story.draj_camera.internal.BaseCaptureActivity;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Flash;

import java.util.List;

/**
 * Created by dharmraj on 8/3/18.
 */

public interface BaseStoryInterface {

    void onRetry(@Nullable String outputUri);

    void onShowPreview(@Nullable String outputUri, boolean countdownIsAtZero);

    void onShowStillshot(String outputUri);

    void setRecordingStart(long start);

    void setRecordingEnd(long end);

    long getRecordingStart();

    long getRecordingEnd();

    boolean hasLengthLimit();

    boolean countdownImmediately();

    long getLengthLimit();

    @BaseStoryActivity.CameraPosition
    int getCurrentCameraPosition();

    Facing getCameraFacing();

    void useMedia(String uri);

    void addToStory(String uri);

    boolean allowRetry();

    void setDidRecord(boolean didRecord);

    boolean didRecord();

    boolean restartTimerOnRetry();

    boolean continueTimerInPlayback();

    long maxAllowedFileSize();

    @DrawableRes
    int iconRecord();

    @DrawableRes
    int iconStop();

    @DrawableRes
    int iconFrontCamera();

    @DrawableRes
    int iconRearCamera();

    @DrawableRes
    int iconPlay();

    @DrawableRes
    int iconPause();

    @DrawableRes
    int iconRestart();

    @StringRes
    int labelRetry();

    @Deprecated
    @StringRes
    int labelUseVideo();

    @StringRes
    int labelConfirm();

    @DrawableRes
    int iconStillshot();

    /** @return true if we only want to take photographs instead of video capture */
    boolean useStillshot();

    @BaseStoryActivity.FlashMode
    int getFlashMode();

    @BaseStoryActivity.FlashMode
    int getFlashModeVideo();

    void setFlashModes(int modes);

    void setFlashModeVideo(int modes);

    boolean shouldHideCameraFacing();

    @DrawableRes
    int iconFlashAuto();

    @DrawableRes
    int iconFlashOn();

    @DrawableRes
    int iconFlashOff();



}
