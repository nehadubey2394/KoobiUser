package com.mualab.org.user.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 28/12/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    private Listner listner;

    public void setListner(Listner listner){
        this.listner = listner;
    }

    public interface Listner{
        void onNetworkChange(boolean isConnected);
    }


    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        Log.e("network reciever", "Sulod sa network reciever");

        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {

                if(listner!=null){
                    listner.onNetworkChange(false);
                }else {
                    Toast.makeText(context, context.getString(R.string.error_msg_network), Toast.LENGTH_SHORT).show();
                }
            } else {
                if(listner!=null)
                    listner.onNetworkChange(true);
                //new ResumeForceExitPause(context).execute();
            }

        }
    }
}