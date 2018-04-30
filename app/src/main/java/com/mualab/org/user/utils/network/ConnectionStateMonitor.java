package com.mualab.org.user.utils.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * Created by dharmraj on 28/12/17.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    final NetworkRequest networkRequest;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ConnectionStateMonitor() {
        networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void enable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest , this);
    }

    // Likewise, you can have a disable method that simply calls ConnectivityManager#unregisterCallback(networkRequest) too.

    @Override
    public void onAvailable(Network network) {
        // Do what you need to do here
    }
}