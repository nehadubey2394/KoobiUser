package views.videoplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.CheckResult;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class RjPlayer extends FrameLayout implements PlayerInterface {

    private MediaPlayer mPlayer;


    public RjPlayer(Context context) {
        super(context);
       // init(context, null);
    }

    public RjPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init(context, attrs);
    }

    public RjPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //init(context, attrs);
    }


    @Override
    public void start() {
        /*if (mPlayer == null) return;
        mPlayer.start();
        if (mCallback != null) mCallback.onStarted(this);
        if (mHandler == null) mHandler = new Handler();
        mHandler.post(mUpdateCounters);*/
//    mBtnPlayPause.setImageDrawable(mPauseDrawable);
    }

    @Override
    public void pause() {
       /* if (mPlayer == null || !isPlaying()) return;
        mPlayer.pause();
        if (mCallback != null) mCallback.onPaused(this);
        if (mHandler == null) return;
        mHandler.removeCallbacks(mUpdateCounters);*/
    }

    @Override
    public void stop() {
      /*  if (mPlayer == null) return;
        try {
            mPlayer.stop();
        } catch (Throwable ignored) {
        }
        if (mHandler == null) return;
        mHandler.removeCallbacks(mUpdateCounters);*/
    }

    @Override
    public void reset() {
        /*if (mPlayer == null) return;
        mIsPrepared = false;
        mPlayer.reset();
        mIsPrepared = false;*/
    }

    @Override
    public void release() {
      /*  mIsPrepared = false;

        if (mPlayer != null) {
            try {
                mPlayer.release();
            } catch (Throwable ignored) {
            }
            mPlayer = null;
            deleteOutputFile(mSource.getPath());
        }

        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCounters);
            mHandler = null;
        }*/
    }

    @Override
    public boolean isPrepared() {
        return false;
    }

    @CheckResult
    @Override
    public int getDuration() {
        if (mPlayer == null) return -1;
        return mPlayer.getDuration();
    }

   /* @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mProgressFrame.setVisibility(View.INVISIBLE);
        mIsPrepared = true;
        if (mCallback != null) mCallback.onPrepared(this);
        setControlsEnabled(true);

        if (mAutoPlay) {
            if (!mControlsDisabled && mHideControlsOnPlay) hideControls();
            start();
            if (mInitialPosition > 0) {
                seekTo(mInitialPosition);
                mInitialPosition = -1;
            }
        } else {
            // Hack to show first frame, is there another way?
            mPlayer.start();
            mPlayer.pause();
        }
    }*/

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {

    }
}
