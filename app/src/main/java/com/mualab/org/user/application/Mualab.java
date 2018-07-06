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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mualab.org.user.BuildConfig;
import com.mualab.org.user.activity.people_tag.models.TaggedPhoto;
import com.mualab.org.user.data.model.Location;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.local.prefs.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dharmraj on 21/12/17.
 **/

public class Mualab extends Application {

    public static final String TAG = Mualab.class.getSimpleName();
    public static boolean IS_DEBUG_MODE = BuildConfig.DEBUG;

    public static Mualab mInstance;
    public static User currentUser;
    public static Location currentLocation;
    public static Location currentLocationForBooking;
   // public static DatabaseReference ref;
   public static boolean isStoryUploaded;

    private Session session;
    private RequestQueue mRequestQueue;

    //service tag
    private SharedPreferences mSharedPreferences;
    private static final String SHARED_PREF_NAME = "insta_tag_preferences";

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

        AndroidNetworking.initialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY);
        }
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


    public ArrayList<TaggedPhoto> getTaggedPhotos() {
        String json = getString(Keys.TAGGED_PHOTOS.getKeyName());
        ArrayList<TaggedPhoto> taggedPhotoArrayList;
        if (!json.equals("")) {
            taggedPhotoArrayList =
                    new Gson().fromJson(json, new TypeToken<ArrayList<TaggedPhoto>>() {
                    }.getType());
        } else {
            taggedPhotoArrayList = new ArrayList<>();
        }
        return taggedPhotoArrayList;
    }

    public void setTaggedPhotos(ArrayList<TaggedPhoto> taggedPhotoArrayList) {
        putString(Keys.TAGGED_PHOTOS.getKeyName(), toJson(taggedPhotoArrayList));
    }

    private enum Keys {
        TAGGED_PHOTOS("TAGGED_PHOTOS");
        private final String keyName;

        Keys(String label) {
            this.keyName = label;
        }

        public String getKeyName() {
            return keyName;
        }
    }


    public static String toJson(Object object) {
        try {
            Gson gson = new Gson();
            return gson.toJson(object);
        } catch (Exception e) {
            Log.e(SHARED_PREF_NAME, "Error In Converting ModelToJson", e);
        }
        return "";
    }

}
