package com.mualab.org.user.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by dharmraj on 28/12/17.
 */

public class KeyboardUtil {

    public static void hideKeyboard(View view, Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(
                Objects.requireNonNull(activity.getCurrentFocus()).getWindowToken(), 0);
    }

    public static void showKeyboard(View view, Context context){
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(view.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
    }

  /*  public static hideSoftKeyboard(Activity context) {
        if(context.getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        }
    }*/

}
