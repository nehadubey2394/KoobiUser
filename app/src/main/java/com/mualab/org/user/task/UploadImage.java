package com.mualab.org.user.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.image.picker.ImageUtils;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.application.multipleFileUpload.MultiPartRequest;
import com.mualab.org.user.application.multipleFileUpload.StringParser;
import com.mualab.org.user.application.multipleFileUpload.Template;
import com.mualab.org.user.application.multipleFileUpload.VolleyMySingleton;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.util.media.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dharmraj on 26/10/17.
 **/

public class UploadImage {

    private RequestQueue mRequest;
    private MultiPartRequest mMultiPartRequest;

    private List<Uri> mSelectedImages;
    private Map<String, String> params;
    private String authToken;
    private Context mContext;
    private Listner listner;

    public UploadImage(Context mContext, String authToken, Map<String, String> map, List<Uri> mSelectedImages, Listner listner) {
        this.mContext = mContext;
        this.authToken = authToken;
        this.params = map;
        this.listner = listner;
        this.mSelectedImages = mSelectedImages;
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void execute() {
        VolleyMySingleton volleySingleton = new VolleyMySingleton(mContext);
        mRequest = Mualab.getInstance().getRequestQueue();
        mRequest.start();

        List<File> images = new ArrayList<>();
        for (int index = 0, size = mSelectedImages.size(); index < size; index++) {

            Uri uri = mSelectedImages.get(index);
            if(uri.toString().contains("/storage/emulated/0/Android/data/com.mualab.org.user/cache/i_prefix")){
                String str = uri.toString().replace("/storage/emulated/0/Android/data/com.mualab.org.user/cache/","");
                images.add(new File(mContext.getExternalCacheDir(), str));
            }else {
                String authority =uri.getAuthority();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),uri);
                    File file = FileUtils.savebitmap(mContext, bitmap, "tmp"+index);
                    images.add(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mMultiPartRequest = new MultiPartRequest(API.BASE_URL + "addFeed",new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listner.onError("error");
                try{
                    if (StringParser.getCode(error.toString()).equals(Template.Query.VALUE_CODE_SUCCESS)) {
                        showToast(StringParser.getMessage(error.toString()));
                    } else {
                        showToast("Error\n" +StringParser.getMessage(error.toString()));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.Listener() {
            @Override
            public void onResponse(Object response) {
                listner.onResponce(response.toString());
                Log.e("ADDVECH", "setResponse: " + response.toString());
            }
        }, images,params, authToken);
        //Set tag, diperlukan ketika akan menggagalkan request/cancenl request
        mMultiPartRequest.setTag("MultiRequest");
        //Set retry policy, untuk mengatur socket time out, retries. Bisa disetting lewat template
        mMultiPartRequest.setRetryPolicy(new DefaultRetryPolicy(Template.VolleyRetryPolicy.SOCKET_TIMEOUT,
                Template.VolleyRetryPolicy.RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Menambahkan ke request queue untuk diproses
        mRequest.add(mMultiPartRequest);
    }


    public interface Listner {
        void onResponce(String responce);
        void onError(String error);
    }

   private void showToast(String message){
        if(TextUtils.isEmpty(message)){
            MyToast.getInstance(mContext).showSmallMessage(message);
        }
    }
}
