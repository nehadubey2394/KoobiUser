package com.mualab.org.user.activity.chat.notification_builder;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FcmNotificationBuilder {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "FcmNotificationBuilder";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    //live key
    private static final String AUTH_KEY = "key=" + "AAAAK1vRFPE:APA91bFDJlGE-pK5f7JarrELoglCDCZl2Bnnm495IBiYjWXte8BInV8ZSdNT9fcW-xx96LQFIQAAGiwvMXYpK8ap6uJX6qfiPXfMCEwbGbfd7KMXtSSm9MLdfpD6AhdpbHbzSQbew5wF";

    //dev key
  //  private static final String AUTH_KEY = "key=" + "AAAAohAm9co:APA91bHNZDmtwzyLIdcjl4P7Vw7uoq7YwtKiuRsF5Ld5DHkXJBUZmmT4_Wcj6wXeKonhIorQEbivo3nILXfescBWH01HCmoQiOKUhwQgyP1mKByn43xpFTfXKU74E43TWkePKjaRjE7hHtsBh-FgSHTWk43xMPzFew";

    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    // json related keys
    private static final String KEY_TO = "to";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "message";
    private static final String KEY_DATA = "data";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "uid";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private String mTitle;
    private String mMessage;
    private String mUsername;
    private String mUid,type,clickAction,adminId;
    private String mFirebaseToken;
    private String mReceiverFirebaseToken;
    private List<String> registrationId = new ArrayList<>();

    private FcmNotificationBuilder() {

    }

    public static FcmNotificationBuilder initialize() {
        return new FcmNotificationBuilder();
    }

    public FcmNotificationBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public FcmNotificationBuilder registrationId(List<String>registrationId) {
        this.registrationId = registrationId;
        return this;
    }

    public FcmNotificationBuilder message(String message) {
        mMessage = message;
        return this;
    }

    public FcmNotificationBuilder username(String username) {
        mUsername = username;
        return this;
    }

    public FcmNotificationBuilder type(String type) {
        this.type = type;
        return this;
    }

    public FcmNotificationBuilder clickAction(String clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public FcmNotificationBuilder uid(String uid) {
        mUid = uid;
        return this;
    }

    public FcmNotificationBuilder adminId(String adminId) {
        adminId = adminId;
        return this;
    }

    public FcmNotificationBuilder firebaseToken(String firebaseToken) {
        mFirebaseToken = firebaseToken;
        return this;
    }

    public FcmNotificationBuilder receiverFirebaseToken(String receiverFirebaseToken) {
        mReceiverFirebaseToken = receiverFirebaseToken;
        return this;
    }

    public void send() {
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MEDIA_TYPE_JSON, getValidJsonBody().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert requestBody != null;
        Request request = new Request.Builder()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .addHeader(AUTHORIZATION, AUTH_KEY)
                .url(FCM_URL)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, IOException e) {
                Log.e(TAG, "onGetAllUsersFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                Log.e(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    private JSONObject getValidJsonBody() throws JSONException {
        JSONObject jsonObjectBody = new JSONObject();
        JSONObject data = new JSONObject();
        //JSONObject notificationDict = new JSONObject();
         /* data.put(KEY_USERNAME, mUsername);
        data.put(KEY_UID, mUid);
        data.put(KEY_FCM_TOKEN, mFirebaseToken);
        data.put("ChatTitle", mTitle);
        data.put("sound", "default");
        data.put("title",mTitle);*/
        //data.put("priority","high");


        data.put("body",mMessage);
        data.put(KEY_TITLE, mUsername);
        data.put("icon","icon");
        data.put("sound", "default");
        data.put("badge", "1");
        data.put("notifincationType", "15");
        data.put("click_action",clickAction);
        data.put("opponentChatId", mUid);
        data.put(KEY_TEXT, mMessage);
        data.put("other_key", true);
        data.put("content_available", true);

        if (type.equals("groupChat"))
            data.put("adminId", adminId);
        else
            data.put("type", type);

        Map<String,Object> params = new HashMap<>();
        if (registrationId.size()!=0)
            params.put("registration_ids", registrationId);
        else
            params.put("to", mReceiverFirebaseToken);

        params.put("title", mTitle);
        params.put("sound", "default");
        params.put("priority", "high");
        params.put("data", data);
        params.put("notification", data);

      /*  jsonObjectBody.put(KEY_DATA, jsonObjectData);
        jsonObjectBody.put(KEY_NOTIFICATION, jsonObjectData);
        jsonObjectBody.put(KEY_TO, mReceiverFirebaseToken);
*/
        return new JSONObject(params);
    }
}
