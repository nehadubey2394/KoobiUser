package com.mualab.org.user.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by mindiii on 27/5/16.
 */
public class SetFont {

    public static void setfontRagular(TextView view, Context context) {

        Typeface face = Typeface.createFromAsset(context.getResources().getAssets(), "@font/roboto_regular.ttf");
        view.setTypeface(face);
    }

    public static void setfontMedium(TextView view, Context context) {

        Typeface face = Typeface.createFromAsset(context.getResources().getAssets(), "@font/roboto_regular.ttf");
        view.setTypeface(face);
    }

    public static void setfontLight(TextView view, Context context) {

        Typeface face = Typeface.createFromAsset(context.getResources().getAssets(), "font/Raleway_Light.ttf");
        view.setTypeface(face);
    }

    public static void setfontSemiBold(TextView view, Context context) {

        Typeface face = Typeface.createFromAsset(context.getResources().getAssets(), "font/Raleway_SemiBold.ttf");
        view.setTypeface(face);
    }
}
