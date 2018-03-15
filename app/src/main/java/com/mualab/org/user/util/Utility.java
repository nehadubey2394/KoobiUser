package com.mualab.org.user.util;

import android.content.Context;

/**
 * Created by Mindiii on 1/29/2018.
 */

public class Utility {
    private Context mContext;
    public Utility(Context context){
        this.mContext = context;
    }

    public String roundRatingWithSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f %c",
                count / Math.pow(1000, exp),
                "kMGTPE".charAt(exp-1));
    }

    public int getTimeInMin(int hours,int min){
        return hours*60 + min;
    }
}
