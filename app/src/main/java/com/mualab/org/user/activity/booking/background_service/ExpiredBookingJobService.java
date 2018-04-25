package com.mualab.org.user.activity.booking.background_service;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.VolleyError;
import com.mualab.org.user.activity.booking.fragment.BookingFragment4;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.model.User;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.webservice.HttpResponceListner;
import com.mualab.org.user.webservice.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.Helper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mindiii on 3/31/2018.
 */

public class ExpiredBookingJobService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final static String TAG = "ExpiredBookingJobService";

    public static final String COUNTDOWN_BR = "com.mualab.org.user";

    public CountDownTimer countDownTimer = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("", "Starting timer...");

        startTimer();
    }

    @Override
    public void onDestroy() {
        stopCountdown();
        Log.i("", "Timer cancelled");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //Stop Countdown method
    public void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    //Start Countodwn method
    public void startTimer() {
        countDownTimer = new CountDownTimer(500000, 1000) {
            public void onTick(long millisUntilFinished) {
                //  millisUntilFinished / 1000;
            }

            public void onFinish() {
                countDownTimer = null;//set CountDownTimer to null
                if (BookingFragment4.arrayListbookingInfo.size()!=0)
                    apiForDeleteAllPendingBooking();
            }
        }.start();

    }

    @Override
    public boolean stopService(Intent name) {
        stopCountdown();
        return super.stopService(name);
    }

    private void apiForDeleteAllPendingBooking(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(getApplicationContext(), new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForDeleteAllPendingBooking();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(getApplicationContext(), "deleteUserBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        BookingFragment4.arrayListbookingInfo.clear();
                        Session session = Mualab.getInstance().getSessionManager();
                        session.setUserChangedLocLat("");
                        session.setUserChangedLocLng("");
                        session.setUserChangedLocName("");
                        Log.i("", "All services deleted...");
                    }else {
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(false)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

}
