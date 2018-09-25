package com.mualab.org.user.notification;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.ChatActivity;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.splash.SplashActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static String TAG = MyFirebaseMessagingService.class.getName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String body = remoteMessage.getData().get("body");
            String title = remoteMessage.getData().get("title");
            String userName = remoteMessage.getData().get("userName");
            String notifincationType = remoteMessage.getData().get("notifincationType");

            String userType = "",notifyId="",urlImageString="";

            if (notifincationType.equals("15")){
                String userId = remoteMessage.getData().get("opponentChatId");
               // scheduleJob();
                chatNotification(body,title,notifincationType,userId);
            }else {
                userType = remoteMessage.getData().get("userType");
                notifyId = remoteMessage.getData().get("notifyId");
                urlImageString = remoteMessage.getData().get("urlImageString");
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
              //  scheduleJob();
                handleNow(urlImageString,body,title,notifincationType,notifyId,userName,userType);
            }
        }


    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(String urlImageString, String body, String title, String notifincationType, String notifyId,String userName,String userType) {
        sendNotification(urlImageString,body,title,notifincationType,notifyId,userName, userType);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void chatNotification(String body, String title, String notifincationType, String userId) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("opponentChatId",userId);
        intent.putExtra("body",body);
        intent.putExtra("title",title);
        intent.putExtra("userName",title);
        intent.putExtra("notifincationType",notifincationType);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Abc";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int num = (int) System.currentTimeMillis();
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.customnotification);
        PendingIntent resultIntent = PendingIntent.getActivity(this, num, intent, PendingIntent.FLAG_ONE_SHOT);

        remoteViews.setViewVisibility(R.id.iv_for_profile,8);
        remoteViews.setViewVisibility(R.id.tvOtherText,0);
        // remoteViews.setImageViewResource(getResources().getDrawable(R.drawable.ic_launcher));
        remoteViews.setTextViewText(R.id.title, getString(R.string.app_name));
        remoteViews.setTextViewText(R.id.text, title);
        remoteViews.setTextViewText(R.id.tvOtherText, body);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo_small)
                .setTicker(body)
                .setContentTitle(title)
                .setContentIntent(resultIntent)
                .setSound(defaultSoundUri)
                .setContent(remoteViews);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        assert notificationManager != null;
        notificationManager.notify(num, notificationBuilder.build());
    }

    private void sendNotification(String urlImageString, String body, String title, String notificationType,
                                  String notifyId,String userName, String userType) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notificationType",notificationType);
        intent.putExtra("urlImageString",urlImageString);
        intent.putExtra("body",body);
        intent.putExtra("notifyId",notifyId);
        intent.putExtra("userType",userType);
        intent.putExtra("title",title);
        intent.putExtra("userName",userName);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Abc";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int num = (int) System.currentTimeMillis();
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.customnotification);
        PendingIntent resultIntent = PendingIntent.getActivity(this, num, intent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap bitmap = null;
        if (!urlImageString.equals(""))
            bitmap = getBitmapFromURL(urlImageString);

        remoteViews.setImageViewBitmap(R.id.iv_for_profile, bitmap);
        // remoteViews.setImageViewResource(getResources().getDrawable(R.drawable.ic_launcher));
        remoteViews.setTextViewText(R.id.title, getString(R.string.app_name));
        remoteViews.setTextViewText(R.id.text, body);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo_small)
                .setTicker(body)
                .setContentIntent(resultIntent)
                .setSound(defaultSoundUri)
                .setContent(remoteViews);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(mChannel);
        }
        assert notificationManager != null;
        notificationManager.notify(num, notificationBuilder.build());
    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            myBitmap = getRoundedCornerBitmap(myBitmap);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private  Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 100;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
