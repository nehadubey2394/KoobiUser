package com.mualab.org.user.activity.story.core;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;

import com.mualab.org.user.activity.story.draj_camera.util.CameraUtil;
import com.mualab.org.user.activity.story.draj_camera.util.Degrees;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Flash;

import static com.mualab.org.user.activity.story.core.BaseStoryActivity.FLASH_MODE_AUTO;
import static com.mualab.org.user.activity.story.core.BaseStoryActivity.FLASH_MODE_OFF;
import static com.mualab.org.user.activity.story.core.BaseStoryActivity.FLASH_MODE_ON;
import static com.mualab.org.user.activity.story.core.BaseStoryActivity.FLASH_MODE_TOURCH;


public class CaptureStoryFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CaptureStoryFragment.class.getName();

    protected ImageButton mButtonVideo;
    protected ImageButton mButtonStillshot;
    protected ImageButton mButtonCameraMode;
    protected ImageButton mButtonFacing;
    protected ImageButton mButtonFlash;
    protected TextView mRecordDuration;
    protected RelativeLayout mButtonBack;

    protected CameraView cameraView;
    protected BaseStoryInterface mInterface;
    protected String mOutputUri;

    protected Handler mPositionHandler;


    public CaptureStoryFragment() {
        // Required empty public constructor
    }

    private final Runnable mPositionUpdater = new Runnable() {
                @Override
                public void run() {
                    if (mInterface == null || mRecordDuration == null) return;
                    final long mRecordStart = mInterface.getRecordingStart();
                    final long mRecordEnd = mInterface.getRecordingEnd();
                    if (mRecordStart == -1 && mRecordEnd == -1) return;
                    final long now = System.currentTimeMillis();
                    if (mRecordEnd != -1) {
                        if (now >= mRecordEnd) {
                            stopRecordingVideo(true);
                        } else {
                            final long diff = mRecordEnd - now;
                            mRecordDuration.setText(String.format("-%s", CameraUtil.getDurationString(diff)));
                        }
                    } else {
                        mRecordDuration.setText(CameraUtil.getDurationString(now - mRecordStart));
                    }
                    if (mPositionHandler != null) mPositionHandler.postDelayed(this, 1000);
                }
            };



    public static CaptureStoryFragment newInstance() {
        CaptureStoryFragment fragment = new CaptureStoryFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_capture_story, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mButtonVideo =  view.findViewById(R.id.videoButton);
        mButtonFacing = view.findViewById(R.id.mButtonFacing);
        mButtonStillshot =  view.findViewById(R.id.mButtonStillshot);
        mButtonCameraMode =  view.findViewById(R.id.mButtonCameraMode);
        mButtonBack =  view.findViewById(R.id.mButtonBack);
        mRecordDuration = view.findViewById(R.id.mRecordDuration);
        mButtonFlash =  view.findViewById(R.id.mButtonFlash);

        setupFlashMode();

        mButtonBack.setOnClickListener(this);
        mButtonVideo.setOnClickListener(this);
        mButtonStillshot.setOnClickListener(this);
        mButtonFacing.setOnClickListener(this);
        mButtonFlash.setOnClickListener(this);

        if (savedInstanceState != null) mOutputUri = savedInstanceState.getString("output_uri");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseStoryInterface) {
            mInterface = (BaseStoryInterface) context;
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        mButtonVideo = null;
        mButtonStillshot = null;
        mButtonFacing = null;
        mButtonFlash = null;
        mRecordDuration = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraView.destroy();
        mInterface = null;
    }

    public void cleanup() {
        cameraView.destroy();
        stopCounter();
    }


    public final void stopCounter() {
        if (mPositionHandler != null) {
            mPositionHandler.removeCallbacks(mPositionUpdater);
            mPositionHandler = null;
        }
    }

    public final void startCounter() {
        if (mPositionHandler == null) mPositionHandler = new Handler();
        else mPositionHandler.removeCallbacks(mPositionUpdater);
        mPositionHandler.post(mPositionUpdater);
    }


    public boolean startRecordingVideo() {
        if (mInterface != null && mInterface.hasLengthLimit() && !mInterface.countdownImmediately()) {
            // Countdown wasn't started in onResume, start it now
            if (mInterface.getRecordingStart() == -1)
                mInterface.setRecordingStart(System.currentTimeMillis());
            startCounter();
            Log.d(TAG, "startRecordingVideo: starting recording session.");

        }

        final int orientation = Degrees.getActivityOrientation(getActivity());
        Log.d(TAG, "startRecordingVideo: setting orientation: " + orientation);
        getActivity().setRequestedOrientation(orientation);
        mInterface.setDidRecord(true);
        return true;
    }

    public void stopRecordingVideo(boolean reachedZero) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Log.d(TAG, "stopRecordingVideo: ending recording session.");

    }


    @Override
    public final void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("output_uri", mOutputUri);
    }



    private void setupFlashMode() {
        Flash flashMode = null;
        if(mInterface.useStillshot()){
            switch (mInterface.getFlashMode()) {
                case FLASH_MODE_AUTO:
                    flashMode = Flash.AUTO;
                    mButtonFlash.setImageResource(mInterface.iconFlashAuto());
                    break;
                case FLASH_MODE_ON:
                    flashMode = Flash.ON;
                    mButtonFlash.setImageResource(mInterface.iconFlashOn());
                    break;
                case FLASH_MODE_OFF:
                    flashMode = Flash.OFF;
                    mButtonFlash.setImageResource(mInterface.iconFlashOff());
                default:
                    break;
            }
            if (flashMode != null) {
                cameraView.setFlash(flashMode);
            }
        }
        else if(!mInterface.useStillshot()){
            switch (mInterface.getFlashModeVideo()) {
                case FLASH_MODE_TOURCH:
                    flashMode = Flash.TORCH;
                    mButtonFlash.setImageResource(mInterface.iconFlashOn());
                    break;
                case FLASH_MODE_OFF:
                    Log.d(TAG, "setFlashMode: video flash mode is OFF.");
                    flashMode = Flash.OFF;
                    mButtonFlash.setImageResource(mInterface.iconFlashOff());
                default:
                    break;
            }
            if (flashMode != null) {
                cameraView.setFlash(flashMode);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.mButtonBack:
                //mInterface.onRetry();
                break;

            case R.id.btnFlashLight:
                 setupFlashMode();
                break;
        }
    }

}
