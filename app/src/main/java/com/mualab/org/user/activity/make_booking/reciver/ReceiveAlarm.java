package com.mualab.org.user.activity.make_booking.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ReceiveAlarm extends BroadcastReceiver {
    public static final String COMMAND = "SENDER";
    public static final int SENDER_SRV_POSITIONING = 1;
    public static final int MIN_TIME_REQUEST = 3 * 1000;
    public static final String ACTION_REFRESH_SCHEDULE_ALARM =
            "com.mualab.org.user";

    private static Context _context;


    // received request from the calling service
    @Override
    public void onReceive(final Context context, Intent intent) {

    }


}
