package com.mualab.org.user.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.mualab.org.user.utils.constants.Constant;

/**
 * Created by Mindiii on 1/25/2018.
 */

public class LocationDetector {
    private Context context;

    public void showLocationSettingDailod(Context mContext){
        this.context = mContext;

        android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(mContext);
        builder1.setTitle("GPS Services Not Active");
        builder1.setMessage("Please enable Location Services and GPS");
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);

                    }
                });

        android.app.AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    public boolean checkLocationPermission(Context mContext) {
        this.context = mContext;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constant.MY_PERMISSIONS_REQUEST_LOCATION);

            return false;
        } else {
            return true;
        }
    }

    public  boolean isLocationEnabled(Context mContext) {
        int locationMode = 0;
        String locationProviders;
        this.context = mContext;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }

    public  boolean isGpsEnabled(Context mContext) {
        LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        boolean isLocation = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(gps_enabled || network_enabled) {
            isLocation = true;
        }else {
            isLocation = false;
        }
        return isLocation;
    }

}
