package com.mualab.org.user.activity.story.core;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Created by dharmraj on 8/3/18.
 **/

public class CaptureStoreActivity extends BaseStoryActivity{

    @Override
    @NonNull
    public Fragment getFragment() {
        return CaptureStoryFragment.newInstance();
    }
}
