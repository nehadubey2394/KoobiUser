package com.mualab.org.user.activity.story.core;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by dharmraj on 8/3/18.
 */

public interface BaseStoryInterface {

    void onRetry(@Nullable String outputUri);

    void onShowPreview(@Nullable String outputUri, boolean countdownIsAtZero);

    void onShowStillshot(String outputUri);

    boolean shouldAutoSubmit();

    void setRecordingStart(long start);

    void setRecordingEnd(long end);

    long getRecordingStart();

    long getRecordingEnd();

    boolean hasLengthLimit();

    boolean countdownImmediately();

    long getLengthLimit();

    /*Camera methods*/

    @BaseStoryActivity.CameraPosition
    int getCurrentCameraPosition();

    void toggleCameraPosition();

    void setCameraPosition(int cameraPosition);

    void setDidRecord(boolean didRecord);

    boolean didRecord();

    long maxAllowedFileSize();


    /** @return true if we only want to take photographs instead of video capture */
    boolean useStillshot();

    void setCameraMode(int cameraMode);

    @BaseStoryActivity.FlashMode
    int getFlashMode();

    @BaseStoryActivity.FlashMode
    int getFlashModeVideo();

    void setFlashModes(boolean flashModes);

    void toggleFlashMode();

    boolean shouldHideFlash();

    boolean shouldHideCameraFacing();


    void useMedia(String uri);

    void addToStory(String uri);

    boolean allowRetry();





    boolean restartTimerOnRetry();

    boolean continueTimerInPlayback();

    /*icons methods*/

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

    @DrawableRes
    int iconStillshot();

    @DrawableRes
    int iconFlashAuto();

    @DrawableRes
    int iconFlashOn();

    @DrawableRes
    int iconFlashOff();
}
