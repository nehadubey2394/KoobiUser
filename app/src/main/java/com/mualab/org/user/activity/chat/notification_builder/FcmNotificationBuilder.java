package com.mualab.org.user.activity.chat.notification_builder;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
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
    private static final String AUTH_KEY = "key=" + "AAAAK1vRFPE:APA91bFDJlGE-pK5f7JarrELoglCDCZl2Bnnm495IBiYjWXte8BInV8ZSdNT9fcW-xx96LQFIQAAGiwvMXYpK8ap6uJX6qfiPXfMCEwbGbfd7KMXtSSm9MLdfpD6AhdpbHbzSQbew5wF";

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
    private String mUid,type;
    private String mFirebaseToken;
    private String mReceiverFirebaseToken;

    private FcmNotificationBuilder() {

    }

    public static FcmNotificationBuilder initialize() {
        return new FcmNotificationBuilder();
    }

    public FcmNotificationBuilder title(String title) {
        mTitle = title;
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

    public FcmNotificationBuilder uid(String uid) {
        mUid = uid;
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
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onGetAllUsersFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    private JSONObject getValidJsonBody() throws JSONException {
        JSONObject jsonObjectBody = new JSONObject();
        JSONObject data = new JSONObject();
        //JSONObject notificationDict = new JSONObject();

        /*  //IOS
            let notificationDict = ["body": checkForNULL(obj: self.strTextNotification),
                                    "title": checkForNULL(obj: strName ),
                                    "icon": "icon",
                                    "sound": "default",
                                    "badge": "1",
                                    "message": self.strTextNotification,
                                    "notifincationType": "15",
                                    "type": "chat",
                                    "click_action": "ChatActivity",
                                    "opponentChatId": checkForNULL(obj: self.strMyChatId)]

            let finalDict = ["to":checkForNULL(obj:self.objChatHistoryModel.strOpponentFireBaseToken),
                             "data": checkForNULL(obj:messageDict),
                             "priority" : "high",
                             "notification": checkForNULL(obj:notificationDict),
                             "sound": "default"] as [String : Any]*/

        data.put("body",mMessage);
        data.put(KEY_TITLE, mUsername);
        data.put("icon","icon");
        data.put("sound", "default");
        data.put("badge", "1");
        data.put("notifincationType", "15");
        data.put("click_action","ChatActivity");
        data.put("opponentChatId", mUid);
        data.put("type", type);
        data.put(KEY_TEXT, mMessage);

       /* data.put(KEY_USERNAME, mUsername);
        data.put(KEY_UID, mUid);
        data.put(KEY_FCM_TOKEN, mFirebaseToken);
        data.put("ChatTitle", mTitle);
        data.put("sound", "default");
        data.put("title",mTitle);*/
        //data.put("priority","high");

        data.put("other_key", true);
        data.put("content_available", true);


        Map<String,Object> params = new HashMap<>();
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
