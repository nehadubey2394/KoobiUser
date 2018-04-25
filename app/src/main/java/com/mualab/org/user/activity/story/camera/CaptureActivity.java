package com.mualab.org.user.activity.story.camera;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.mualab.org.user.activity.story.camera.internal.BaseCaptureActivity;
import com.mualab.org.user.activity.story.camera.internal.CameraFragment;

public class CaptureActivity extends BaseCaptureActivity {

  @Override
  @NonNull
  public Fragment getFragment() {
    return CameraFragment.newInstance();
  }
}
