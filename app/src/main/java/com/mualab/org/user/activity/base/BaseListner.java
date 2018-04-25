package com.mualab.org.user.activity.base;

import android.support.v4.app.Fragment;

/**
 * Created by dharmraj on 14/3/18.
 **/

public interface BaseListner {
    void addFragment(Fragment fragment, boolean addToBackStack);
    void replaceFragment(Fragment fragment, boolean addToBackStack);
    void backPress();
}
