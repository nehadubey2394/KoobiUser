package com.mualab.org.user.task;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.application.VolleyRequest.AppHelper;
import com.mualab.org.user.application.VolleyRequest.VolleyMultipartRequest;
import com.mualab.org.user.application.VolleyRequest.VolleySingleton;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.dialogs.ServerErrorDialog;
import com.mualab.org.user.helper.MyToast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.mualab.org.user.application.Mualab.IS_DEBUG_MODE;

/**
 * Created by dharmraj on 10/1/18.
 **/

public class HttpTask {
    private Context context;
    private HttpResponceListner.Listener listener;

    private String api;
    private int method = Request.Method.POST;
    private String bodyContentType;
    private int initialTimeoutMs;
    private int maxNumRetries;
    private float backoffMultiplier;
    private boolean retryPolicy;
    private boolean shouldCache;

    private Map<String, String> header;
    private Map<String, String> params;
    private Map<String, String> body;
    private String jsonObjectString;
    private String authToken;
    private String TAG;
    private boolean progress;

    public static class ContentType{
        public static final String FORM_DATA = "multipart/form-data; charset=UTF-8";
        public static final String APPLICATION_JSON = "application/json; charset=UTF-8";
        public static final String APPLICATION_TEXT = "application/text; charset=UTF-8";
        public static final String X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
    }

    public static class Builder {

        private static String BASE_URL = API.BASE_URL; //"http://koobi.co.uk/api/";
        //Required
        private String api;
        private Context context;
        private HttpResponceListner.Listener listener;

        private int method = Request.Method.POST;
        private int initialTimeoutMs = 2500;
        private int maxNumRetries = 1;
        private float backoffMultiplier = 1f;
        private boolean retryPolicy;
        private String bodyContentType = "application/x-www-form-urlencoded; charset=UTF-8";

        private Map<String, String> header;
        private Map<String, String> params;
        private Map<String, String> body;
        private String jsonObjectString;
        private String authToken;
        private String TAG;
        private boolean progress;

        public Builder(Context context, String api, HttpResponceListner.Listener listener) {
            this.api = api;
            this.context = context;
            this.listener = listener;
        }

        //Option

        public Builder setMethod(int method){
            this.method = method;
            return this;
        }

        public Builder setBaseURL(String baseURL){
            this.BASE_URL = baseURL;
            return this;
        }

        public Builder setBodyContentType(String contentType){
            this.bodyContentType = contentType;
            return this;
        }

        public Builder setHeader(Map<String, String> header){
            this.header = header;
            return this;
        }

        public Builder setParam(Map<String, String> params){
            this.params = params;
            return this;
        }

        public Builder addJsonObjectString(String jsonObjectString){
            this.jsonObjectString = jsonObjectString;
            return this;
        }

        public Builder setBody(Map<String, String> body){
            this.body = body;
            return this;
        }


        public Builder setBody(Map<String, String> body, String contentType){
            this.body = body;
            this.bodyContentType = contentType;
            return this;
        }

        public Builder setAuthToken(String authToken){
            this.authToken = authToken;
            return this;
        }

        public Builder setProgress(boolean progress){
            this.progress = progress;
            return this;
        }

        public Builder setRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier){
            this.initialTimeoutMs = initialTimeoutMs;
            this.maxNumRetries = maxNumRetries;
            this.backoffMultiplier = backoffMultiplier;
            this.retryPolicy = true;
            return this;
        }


        public HttpTask build(){
            return new HttpTask(this);
        }

    }

    public HttpTask(Builder builder){
        this.context = builder.context;
        this.api = builder.BASE_URL+builder.api;
        this.listener = builder.listener;
        this.method = builder.method;
        this.bodyContentType = builder.bodyContentType;
        this.initialTimeoutMs = builder.initialTimeoutMs;
        this.maxNumRetries = builder.maxNumRetries;
        this.backoffMultiplier = builder.backoffMultiplier;
        this.retryPolicy = builder.retryPolicy;
        this.header = builder.header;
        this.params = builder.params;
        this.body = builder.body;
        this.jsonObjectString = builder.jsonObjectString;
        this.authToken = builder.authToken;
        this.progress = builder.progress;
    }


    public void execute(String TAG){
        StringRequest request = new StringRequest(method, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(IS_DEBUG_MODE){
                    System.out.println(api+"\n"+response);
                }
                listener.onResponse(response, api);
                if(progress)
                    Progress.hide(context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.ErrorListener(error);
                if(progress)
                    Progress.hide(context);
                handleError(error);
            }
        }){
            @Override
            public String getBodyContentType() {
                return bodyContentType;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
               /* if(params!=null){
                    String string = new JSONObject(params).toString();
                    return string.getBytes();
                }
               return super.getBody();*/

                if(body!=null){
                    String string = new JSONObject(body).toString();
                    //new String(data, "UTF-8");
                    return string.getBytes();
                }

                if(jsonObjectString!=null){
                    return jsonObjectString.getBytes();
                }
                return super.getBody();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                if (params != null)
                    return params;
                return super.getParams();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if(header==null) header = new HashMap<>();
                if (authToken!=null) {
                    header.put("authToken", authToken);
                }
                return header;
            }
        };

        if(retryPolicy){
            request.setRetryPolicy(new DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier));
        }
        request.setShouldCache(shouldCache);
        if(progress)
            Progress.show(context);
        Mualab.getInstance().addToRequestQueue(request, TAG);
    }


    /*post image from multipart data form*/
    public void postImage(final String key, final Bitmap bitmap){
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, api, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);

                if(IS_DEBUG_MODE){
                    System.out.println(api+"\n"+response);
                }

                listener.onResponse(resultResponse, api);
                if(progress)
                    Progress.hide(context);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.ErrorListener(error);
                if(progress)
                    Progress.hide(context);
                handleError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if(header==null) header = new HashMap<>();
                if (authToken!=null) {
                    header.put("authToken", authToken);
                }
                return header;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (key != null && bitmap != null) {
                    params.put(key, new DataPart("tmpImage.jpg", AppHelper.getFileDataFromDrawable(bitmap), "image/jpeg"));
                }
                return params;
            }
        };

        if(progress)
            Progress.show(context);
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1f));
        VolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(multipartRequest);
    }


    private void handleError(VolleyError error){
        if(progress)
            Progress.hide(context);

        try{
            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                // HTTP Status Code: 401 Unauthorized
                MyToast.getInstance(context).showDasuAlert("Session Expired", "please login again.");
                Mualab.getInstance().getSessionManager().logout();

            }else if(error.getMessage().contains("java.net.ConnectException")){
                new ServerErrorDialog(context).show();
            }else {
                MyToast.getInstance(context).showDasuAlert("Server Error", "Looks like we are having some server issue.");
            }
        }catch (Exception e){
            e.printStackTrace();
            MyToast.getInstance(context).showDasuAlert("Server Error", "Looks like we are having some server issue.");
        }
    }
}
