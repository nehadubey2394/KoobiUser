package com.mualab.org.user.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 21/12/17.
 */

public class MySnackBar {


    /*public static void show(String message){
        if(Mualab.snackPosition!=null)
            Snackbar.make(Mualab.snackPosition, message, Snackbar.LENGTH_SHORT).setAction("ok", null).show();
    }

    public static void showLong(String message){
        if(Mualab.snackPosition!=null)
            Snackbar.make(Mualab.snackPosition, message, Snackbar.LENGTH_LONG).setAction("ok", null).show();
    }*/

    public static void show(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("ok", null).show();
    }

    public static void showSnackbar(Context context, View view, String message) {
        //Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null);
        Snackbar snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View sbView = snack.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.show();
    }

    public static void showSnackbarSort(Context context, View view, String message) {
        //Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG).setAction("Action", null);
        Snackbar snack = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View sbView = snack.getView();
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.show();
    }
}
