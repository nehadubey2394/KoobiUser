package com.mualab.org.user.utils;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Neha on 28/9/17.
 */

public class Helper {
    public  String error_Messages(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Something went wrong.";

        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout error";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);
                String status = response.getString("status");
                String message = response.getString("message");

                Log.e("Error Message", message);

                if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                    return errorMessage;
                } else if (networkResponse.statusCode == 401) {
                    //errorMessage = message + " Check your inputs";
                    errorMessage =  "Your Session is expired,please login again.";
                    return errorMessage;

                } else if (networkResponse.statusCode == 400) {
                    errorMessage = "Your Session is expired,please login again.";
                    return errorMessage;

                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message + " Something is getting wrong";
                    return errorMessage;
                }
                else if (networkResponse.statusCode == 300) {
                    errorMessage = message+" Session is expire.";
                    return errorMessage;
                }
                else if (message.equals("Invalid Auth Token")){
                    errorMessage = "Your Session is expired,please login again.";
                    return errorMessage;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Error", errorMessage);
        error.printStackTrace();
        return errorMessage;

    }
}
