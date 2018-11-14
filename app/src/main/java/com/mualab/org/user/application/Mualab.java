package com.mualab.org.user.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mualab.org.user.BuildConfig;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.Location;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.utils.Util;
import com.mualab.org.user.utils.constants.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mindiii on 21/12/17.
 **/

public class Mualab extends Application implements LifeCycleDelegateListner {

    public static final String TAG = Mualab.class.getSimpleName();
    public static boolean IS_DEBUG_MODE = BuildConfig.DEBUG;
    private Util utility;
    public static Mualab mInstance;
    public static User currentUser;
    public static Location currentLocation;
    public static Location currentLocationForBooking;
    // public static DatabaseReference ref;
    public static boolean isStoryUploaded;
    public static String currentChatUserId = "",currentGroupId = "";

    private Session session;
    private RequestQueue mRequestQueue;

    //service tag
    private SharedPreferences mSharedPreferences;
    private static final String SHARED_PREF_NAME = "koobi_tag_preferences";

    public static Mualab getInstance() {
        if (mInstance.mSharedPreferences == null) {
            mInstance.mSharedPreferences =
                    mInstance.getSharedPreferences(SHARED_PREF_NAME,
                            Context.MODE_PRIVATE);
        }
        return mInstance;
    }

    public static Map<String, String> feedBasicInfo = new HashMap<>();


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mInstance.getSessionManager();
        currentLocation = new Location();
        currentLocationForBooking = new Location();
        FirebaseApp.initializeApp(this);
        session.setIsOutCallFilter(false);
        // ref = FirebaseDatabase.getInstance().getReference();

        utility = new Util(getApplicationContext());

        AndroidNetworking.initialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY);
        }

        AppLifeCycle lifecycleHandler = new AppLifeCycle(this);
        registerLifecycle(lifecycleHandler);
    }

    public Session getSessionManager() {
        if (session == null)
            session = new Session(getApplicationContext());
        return session;
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null)
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void cancelAllPendingRequests() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    /*service tag*/
    private String getString(String key) {
        if (mSharedPreferences != null) {
            return mSharedPreferences.getString(key, "");
        }

        return "";
    }

    private void putString(String key, String value) {
        try {
            if (mSharedPreferences != null) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(key, value);
                editor.apply();
            }
        } catch (Exception e) {
            Log.e(SHARED_PREF_NAME, "Unable Put String in Shared preference", e);
        }
    }

    public void clear() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onAppBackgrounded() {
        utility.goToOnlineStatus(mInstance, Constant.offline);
    }

    @Override
    public void onAppForegrounded() {
        utility.goToOnlineStatus(mInstance, Constant.online);
    }

    private void registerLifecycle (AppLifeCycle lifecycleHandler){
        registerActivityLifecycleCallbacks(lifecycleHandler);
        registerComponentCallbacks(lifecycleHandler);
    }

}
