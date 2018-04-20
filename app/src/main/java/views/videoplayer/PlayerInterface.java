package views.videoplayer;

import android.support.annotation.CheckResult;
import android.support.annotation.FloatRange;

public interface PlayerInterface {

    @CheckResult
    int getDuration();

    void start();

    void pause();

    void stop();

    void reset();

    void release();

    @CheckResult
    boolean isPrepared();

    @CheckResult
    boolean isPlaying();

    void setVolume(
            @FloatRange(from = 0f, to = 1f) float leftVolume,
            @FloatRange(from = 0f, to = 1f) float rightVolume);

}
