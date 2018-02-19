package com.mualab.org.user.dialogs;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 12/1/18.
 */

public class MyToast {

    private Context context;
    private static MyToast instance;

    /**
     * @param context
     */
    private MyToast(Context context) {
        this.context = context;
    }

    /**
     * @param context
     * @return
     */
    public synchronized static MyToast getInstance(Context context) {
        if (instance == null) {
            instance = new MyToast(context);
        }
        return instance;
    }

    /**
     * @param message
     */
    public void showLongMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param message
     */
    public void showSmallMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * The Toast displayed via this method will display it for short period of time
     *
     * @param message
     */
    public void showLongCustomToast(String message) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_custom_toast, null);
        TextView msgTv = layout.findViewById(R.id.tv_msg);
        msgTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();


    }

    /**
     * The toast displayed by this class will display it for long period of time
     *
     * @param message
     */
    public void showSmallCustomToast(String message) {

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_custom_toast, null);
        TextView msgTv = layout.findViewById(R.id.tv_msg);
        msgTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public void showDasuAlert(String message){
        showDasuAlert("Alert!", message);
    }
    public void showDasuAlert(String title, String message) {

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.custome_alert_layout, null);
        TextView tv_title = layout.findViewById(R.id.tv_title);
        TextView msgTv = layout.findViewById(R.id.tv_msg);
        tv_title.setText(title);
        msgTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
