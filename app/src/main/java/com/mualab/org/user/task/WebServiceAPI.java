package com.mualab.org.user.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.application.VolleyRequest.AppHelper;
import com.mualab.org.user.application.VolleyRequest.VolleyMultipartRequest;
import com.mualab.org.user.application.VolleyRequest.VolleySingleton;
import com.mualab.org.user.dialogs.MDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.session.Session;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dharmraj on 14/7/17.
 **/

public class WebServiceAPI {

    private Context mContext;
    private String TAG;
    private Session session;
    private HttpResponceListner.Listener mListener;
    private HttpResponceListner.LoginRegistrationListener mLSListener;

    private int Method;
    private boolean isSelfErrorHandle;
    private boolean isProgressbarEnable = true;


    public WebServiceAPI(Context context) {
        super();
        this.mContext = context;
        this.TAG = WebServiceAPI.class.getName();
        session = new Session(context);
        Method = Request.Method.POST;
    }


    public WebServiceAPI(Context context, String TAG) {
        super();
        this.mContext = context;
        this.TAG = TAG;
        session = new Session(context);
        Method = Request.Method.POST;
    }

    public WebServiceAPI(Context context, String TAG, HttpResponceListner.Listener listener) {
        super();
        mListener = listener;
        this.mContext = context;
        this.TAG = TAG;
        session = new Session(context);
        Method = Request.Method.POST;
    }


    public WebServiceAPI(Context context, String TAG, HttpResponceListner.LoginRegistrationListener listener) {
        super();
        mLSListener = listener;
        this.mContext = context;
        this.TAG = TAG;
        session = new Session(context);
        Method = Request.Method.POST;
    }

    private static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
            // If JsonObject is ok then check for JSONArray
            try {
                new JSONArray(test);
                return true;
            } catch (JSONException ex1) {
                return false;
            }
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
                return true;
            } catch (JSONException ex1) {
                return false;
            }
        }
    }

    public void setMethod(int method) {
        Method = method;
    }

    public void ErrorHandle(boolean bool) {
        isSelfErrorHandle = bool;
    }

    public void enableProgressBar(boolean bool) {
        isProgressbarEnable = bool;
    }

    public void build(HttpResponceListner.Listener listener) {
        mListener = listener;
    }

    /* Gatting artist profile from server */
    public void LoginTask(final Map<String, String> params) {
        final String loginUrl = API.BASE_URL + "userLogin";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, loginUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("#" + response);
                        mLSListener.onResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mLSListener.ErrorListener(error);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };
        Mualab.getInstance().addToRequestQueue(stringRequest, TAG);
    }

    /* Gatting artist profile from server */
    public void signUpTask(final Map<String, String> params, final Bitmap bitmap) {
        final String signUpUrl = API.BASE_URL + "userRegistration";
        Progress.show(mContext);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, signUpUrl, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                Progress.hide(mContext);
                String resultResponse = new String(response.data);
                System.out.println(resultResponse);
                mLSListener.onResponse(resultResponse);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Progress.hide(mContext);
                String errorMessage = getNetworkMessage(error);
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;

            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                if (bitmap != null) {
                    params.put("profileImage", new DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));
                }
                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        VolleySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(multipartRequest);
        //ImLink.getInstance().addToRequestQueue(multipartRequest, TAG);
    }

    public void callMultiPartApi(final String url, final Map<String, String> params, final Bitmap bitmap) {
        callMultiPartApi(url, params, null, bitmap);
    }


    // for video

    // for image
    public void callMultiPartApi(final String url, final Map<String, String> params, final String key, final Bitmap bitmap) {
        Progress.show(mContext);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                API.BASE_URL + url, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                System.out.println(resultResponse);
                Progress.hide(mContext);
                mListener.onResponse(resultResponse, url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getNetworkMessage(error);
                Log.i("Error", errorMessage);
                error.printStackTrace();
                Progress.hide(mContext);
                mListener.ErrorListener(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authToken", session.getAuthToken());
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView

                if (key != null && bitmap != null) {
                    params.put(key, new DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));

                } else if (bitmap != null) {
                    params.put("profileImage", new DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));
                }


                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        VolleySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(multipartRequest);
        //ImLink.getInstance().addToRequestQueue(multipartRequest, TAG);
    }

    public void callMultiPartApiVideo(final String url, final Map<String, String> params, final String key, final File file) {
        Progress.show(mContext);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST,
                API.BASE_URL + url, new Response.Listener<NetworkResponse>() {

            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                System.out.println(resultResponse);
                Progress.hide(mContext);
                mListener.onResponse(resultResponse, url);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getNetworkMessage(error);
                Log.i("Error", errorMessage);
                error.printStackTrace();
                Progress.hide(mContext);
                mListener.ErrorListener(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authToken", session.getAuthToken());
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                if (key != null && file != null) {
                    params.put(key, new DataPart("feedvideo.mp4", AppHelper.getFileDataFromFile(file), "video/mp4"));

                } else if (file != null) {
                    params.put(key, new DataPart("feedvideo.mp4", AppHelper.getFileDataFromFile(file), "video/mp4"));
                }

                return params;
            }
        };

        // multipartRequest.setRetryPolicy(new DefaultRetryPolicy(90000, 0, 1f));
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy( 0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(mContext.getApplicationContext()).addToRequestQueue(multipartRequest);
        //ImLink.getInstance().addToRequestQueue(multipartRequest, TAG);
    }

    public void callApi(final String url, final Map<String, String> params) {
        callApi(url, Method, params);
    }

    public void callApi(final String url, int Method, final Map<String, String> params) {
        callApi(url, Method, params, false);
    }

    public void callApi(final String url, int Method, final Map<String, String> params, final boolean isSelfErrorHandle) {
        if (isProgressbarEnable)
            Progress.show(mContext);
        StringRequest stringRequest = new StringRequest(Method, API.BASE_URL + url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("#" + response);
                        Progress.hide(mContext);
                        mListener.onResponse(response, url);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Progress.hide(mContext);
                        if (isSelfErrorHandle)
                            handleError(error);
                        else
                            mListener.ErrorListener(error);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Map<String, String> params = new HashMap<>();
                //params.put("fireBaseId", id);
                if (params == null)
                    return super.getParams();
                else return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                if (session.isLoggedIn()) {
                    header.put("authToken", session.getAuthToken());
                }
                return header;
            }
        };
        Mualab.getInstance().addToRequestQueue(stringRequest, TAG);
    }

    private void handleError(VolleyError error) {
        handleError(mContext, error);
    }

    private void handleError(Context context, VolleyError error) {
        if (error != null && error.networkResponse != null && context != null) {

            try {
                //if(error.networkResponse.statusCode==400){
                MDialog dialog = new MDialog(context);
                dialog.showSessionError(context.getString(R.string.error_session_expired), ServerResponseCode.getmeesageCode(error.networkResponse.statusCode), "Ok");
               /* }else {
                    MySnackBar.show(WebServiceAPI.getNetworkMessage(error));
                }*/
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }

        } else
            Toast.makeText(context, "Something went wrong. please try again later", Toast.LENGTH_SHORT).show();
        // MySnackBar.show("Something went wrong. please try again later.");
    }

    public String getNetworkMessage(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = null;
                if (isJSONValid(result)) {
                    response = new JSONObject(result);
                }

                String status = "";
                String message = "";

                if (ServerResponseCode.getmeesageCode(networkResponse.statusCode).equals("Ok")) {
                    if (response.has("status")) status = response.getString("status");
                    if (response.has("message")) message = response.getString("message");
                    Log.e("Error Status", "" + status);
                    Log.e("Error Message", message);
                } else {
                    errorMessage = ServerResponseCode.getmeesageCode(networkResponse.statusCode);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.i("Error", errorMessage);
        return errorMessage;
    }
}
