package com.mualab.org.user.activity.story.core;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.mualab.org.user.activity.story.camera.ICallback;
import com.mualab.org.user.activity.story.camera.internal.CameraIntentKey;
import com.mualab.org.user.activity.story.camera.util.CameraUtil;
import com.mualab.org.user.activity.story.camera.util.ImageUtil;
import com.mualab.org.user.dialogs.Progress;
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

import java.io.File;

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
    private boolean isFlashModesVideo;

    private boolean mIsRecording;

   /* private int mIconTextColor;
    private int mRecordButtonColor;
    private int mIconTextColorDark;*/

    private Context mContext;



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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        cameraView = view.findViewById(R.id.cameraView);

        setupFlashMode();

        mButtonBack.setOnClickListener(this);
        mButtonVideo.setOnClickListener(this);
        mButtonStillshot.setOnClickListener(this);
        mButtonFacing.setOnClickListener(this);
        mButtonFlash.setOnClickListener(this);
        mButtonCameraMode.setOnClickListener(this);

        if (savedInstanceState != null) mOutputUri = savedInstanceState.getString("output_uri");
       /* mRecordButtonColor = ContextCompat.getColor(mContext, R.color.colordarkRed);
        int primaryColor = getArguments().getInt(CameraIntentKey.PRIMARY_COLOR);
        if (CameraUtil.isColorDark(primaryColor)) {
            mIconTextColor = ContextCompat.getColor(getActivity(), R.color.mcam_color_light);
            mIconTextColorDark = ContextCompat.getColor(getActivity(), R.color.mcam_color_dark);
            primaryColor = CameraUtil.darkenColor(primaryColor);
        } else {
            mIconTextColor = ContextCompat.getColor(getActivity(), R.color.mcam_color_dark);
            mIconTextColorDark = ContextCompat.getColor(getActivity(), R.color.mcam_color_dark);
        }
        mRecordButtonColor = ContextCompat.getColor(getActivity(), R.color.colordarkRed);
        view.findViewById(R.id.controlsFrame);
//    .setBackgroundColor(primaryColor);
        mRecordDuration.setTextColor(mIconTextColor);*/




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
                cameraView.stop();
                Progress.show(getContext());
                CameraUtils.decodeBitmap(jpeg, 3000, 3000, new CameraUtils.BitmapCallback() {
                    @Override
                    public void onBitmapReady(Bitmap bitmap) {

                       /* mButtonStillshot.setEnabled(true);
                        Progress.hide(getContext());
                        mInterface.onShowStillshot(mOutputUri);*/

                        final File outputPic = CameraUtil.makeTempFile(mContext,
                                getArguments().getString(CameraIntentKey.SAVE_DIR), "IMG_", ".jpg");
                        // lets save the image to disk
                        ImageUtil.saveToDiskAsync(bitmap, outputPic, new ICallback() {
                            @Override
                            public void done(Exception e) {
                                if (e == null) {
                                    Progress.hide(getContext());
                                    mOutputUri = Uri.fromFile(outputPic).toString();
                                    mInterface.onShowStillshot(mOutputUri);
                                    mButtonStillshot.setEnabled(true);
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onVideoTaken(File video) {
                super.onVideoTaken(video);
                cameraView.stop();
                mOutputUri = String.valueOf(Uri.fromFile(video));
               // mInterface.onShowStillshot(mOutputUri);
                mInterface.onShowPreview(mOutputUri, false);
                mButtonStillshot.setEnabled(true);
            }

            @Override
            public void onOrientationChanged(int orientation) {
                super.onOrientationChanged(orientation);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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

        /*final int orientation = Degrees.getActivityOrientation(getActivity());
        Log.d(TAG, "startRecordingVideo: setting orientation: " + orientation);
        getActivity().setRequestedOrientation(orientation);*/
        assert mInterface != null;
        mInterface.setDidRecord(true);

        try {
            // UI
            mButtonVideo.setImageResource(mInterface.iconStop());
            mButtonFacing.setVisibility(View.GONE);
            mButtonCameraMode.setVisibility(View.GONE);
            mButtonBack.setVisibility(View.GONE);
            mButtonFlash.setVisibility(View.GONE);

            // Only start counter if count down wasn't already started
            if (!mInterface.hasLengthLimit()) {
                mInterface.setRecordingStart(System.currentTimeMillis());
                startCounter();
            }

            // Start recording
            File file = CameraUtil.makeTempFile(mContext,
                    getArguments().getString(CameraIntentKey.SAVE_DIR), "VID_", ".mp4");
            cameraView.startCapturingVideo(file);
            mButtonVideo.setEnabled(false);
            mButtonVideo.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            mButtonVideo.setEnabled(true);
                        }
                    },
                    300);

            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            mInterface.setRecordingStart(-1);
            stopRecordingVideo(false);
        }
        return true;
    }

    public void stopRecordingVideo(boolean reachedZero) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Log.d(TAG, "stopRecordingVideo: ending recording session.");

        mButtonVideo.setEnabled(true);
        cameraView.stopCapturingVideo();
        if(reachedZero)
            cameraView.start();

        stopCounter();

        if (mInterface.hasLengthLimit() && mInterface.shouldAutoSubmit() && (mInterface.getRecordingStart() < 0 )) {
            mInterface.onShowPreview(mOutputUri, reachedZero);
            return;
        }

        if (!mInterface.didRecord()) mOutputUri = null;

        mButtonVideo.setImageResource(mInterface.iconRecord());

        mButtonFacing.setVisibility(View.VISIBLE);
        mButtonCameraMode.setVisibility(View.VISIBLE);
        mButtonBack.setVisibility(View.VISIBLE);
        mButtonFlash.setVisibility(View.VISIBLE);

      /*  if (mInterface.getRecordingStart() > -1 && getActivity() != null)
            mInterface.onShowPreview(mOutputUri, reachedZero);*/

        //stopCounter();
    }


    @Override
    public final void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("output_uri", mOutputUri);
    }


    private void invalidateFlash(boolean toggle) {
        if (toggle) mInterface.toggleFlashMode();
        setupFlashMode();
    }

    private void toggleFlashVideo(boolean toggle) {
        if (toggle) mInterface.toggleFlashMode();
        setupFlashModeVideo();
        setupFlashMode();
    }

    private void setupFlashModeVideo() {
        Log.d(TAG, "setupFlashModeVideo: setting up flash mode for video.");
        if (mIsRecording) {
            mButtonFlash.setVisibility(View.GONE);
        } else {
            mButtonFlash.setVisibility(View.VISIBLE);
        }
    }

    private void setupFlashMode() {
        Flash flashMode = null;
        int res =  mInterface.iconFlashOff();

        if(mInterface.useStillshot()){
            switch (mInterface.getFlashMode()) {

                case FLASH_MODE_OFF:
                    flashMode = Flash.OFF;
                    res = mInterface.iconFlashOff();
                    break;

                case FLASH_MODE_ON:
                    flashMode = Flash.ON;
                    res = mInterface.iconFlashOn();
                    break;

                case FLASH_MODE_AUTO:
                    flashMode = Flash.AUTO;
                    res = mInterface.iconFlashAuto();
                    break;
                default:
                    break;
            }
        }
        else if(!mInterface.useStillshot()){
            switch (mInterface.getFlashModeVideo()) {
                case FLASH_MODE_TOURCH:
                    flashMode = Flash.TORCH;
                    res = mInterface.iconFlashOn();
                    break;
                case FLASH_MODE_OFF:
                    flashMode = Flash.OFF;
                    res = mInterface.iconFlashOff();
                default:
                    break;
            }
        }

        if (flashMode != null) {
            cameraView.setFlash(flashMode);
            mButtonFlash.setImageResource(res);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.mButtonBack:
                mInterface.onRetry(mOutputUri);
                break;

            case R.id.mButtonFlash:
                if(mInterface.useStillshot()){
                    invalidateFlash(true);
                }else if(!mInterface.useStillshot())
                    toggleFlashVideo(true);
                break;

            case R.id.mButtonFacing:
                switchCameraFaceing();
                break;

            case R.id.mButtonCameraMode:
                switchCameraMode();
                break;

            case R.id.mButtonStillshot:
                mButtonStillshot.setEnabled(false);
                cameraView.capturePicture();
                break;

            case R.id.videoButton:

                if (mIsRecording) {
                    stopRecordingVideo(false);
                    mIsRecording = false;
                } else {
                    mIsRecording = startRecordingVideo();
                }

                break;
        }
    }


    private void switchCameraFaceing(){
        mInterface.toggleCameraPosition();

        Facing facing = mInterface.getCurrentCameraPosition()==
                BaseStoryActivity.CAMERA_POSITION_FRONT?Facing.BACK:Facing.FRONT;
        cameraView.setFacing(facing);
        setupFlashMode();
        setupFlashModeVideo();
    }

    private void switchCameraMode(){

        int res = 0;
        if(mInterface.useStillshot()){
            mInterface.setCameraMode(BaseStoryActivity.CAMERA_MODE_VIDEO);
            cameraView.setSessionType(SessionType.VIDEO);
            mInterface.setFlashModes(true);

            res = R.drawable.ic_photo_camera_white;
            mButtonVideo.setVisibility(View.VISIBLE);
            mRecordDuration.setVisibility(View.VISIBLE);
            mButtonStillshot.setVisibility(View.GONE);

        }else {
            mInterface.setCameraMode(BaseStoryActivity.CAMERA_MODE_PICTURE);
            cameraView.setSessionType(SessionType.PICTURE);
            mInterface.setFlashModes(false);

            res = R.drawable.ic_videocam_white;
            mButtonVideo.setVisibility(View.GONE);
            mRecordDuration.setVisibility(View.GONE);
            mButtonStillshot.setVisibility(View.VISIBLE);
        }
        mButtonCameraMode.setImageResource(res);
        setupFlashMode();
    }
}
