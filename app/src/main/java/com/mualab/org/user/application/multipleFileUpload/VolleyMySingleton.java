package com.mualab.org.user.application.multipleFileUpload;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Farooq Khan on 14-Jul-16.
 */
public class VolleyMySingleton {

    private static VolleyMySingleton sInstance = null;
    private RequestQueue mRequestQueue;
    private Context context;
    static Context con;

    public VolleyMySingleton(Context c) {
        context = c;
        con = c;
        mRequestQueue = Volley.newRequestQueue(context);
    }


    public static VolleyMySingleton getInstance() {
        if (sInstance == null) {
            sInstance = new VolleyMySingleton(con);
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }


}