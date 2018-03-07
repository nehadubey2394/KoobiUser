package com.mualab.org.user.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by dharmraj on 7/3/18.
 **/

public class SuziLoader {
    private static final String LOG = "SuziLoader";
    private static String mPath = "";
    private ImageView mImageView;
    private static String Type = "micro";
    private Context mContext;


    public SuziLoader into(ImageView view) {
        mImageView = view;
        return this;
    }

    public SuziLoader with(Context context) {
        mContext = context;
        return this;
    }

    public SuziLoader load(String path) {
        mPath = path;
        return this;
    }

    public void show() {
        if (mPath == null || mPath.isEmpty()) {
            Log.e(LOG, "Video path is not specified");
            return;
        }

        if (mImageView == null) {
            Log.e(LOG, "ImageView is not specified");
            return;
        }
        if (mContext == null) {
            Log.e(LOG, "Context is not specified");
            return;
        }
        new LoadImageInBackground(mImageView, mPath).execute();
    }

    public SuziLoader type(String type) {
        Type = type;
        return this;
    }

    private int getType() {
        if (Type.equals("micro") || Type.equals("MICRO")) {
            return MediaStore.Video.Thumbnails.MICRO_KIND;
        }
        return MediaStore.Video.Thumbnails.MINI_KIND;
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadImageInBackground extends AsyncTask<String, Integer, String> {

        ImageView mView;
        String mPath = "";

        @Override
        protected String doInBackground(String... strings) {

           // final Bitmap bitmap;
            try {
                //bitmap = retriveVideoFrameFromVideo(mPath);
                final Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mPath, getType());
                Handler mainHandler = new Handler(mContext.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mView.setImageBitmap(bitmap);
                    }
                };
                mainHandler.post(myRunnable);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return null;
        }

        public LoadImageInBackground(ImageView view, String path) {
            mPath = path;
            mView = view;
        }
    }


    public static Bitmap retriveVideoFrameFromVideo(String p_videoPath)
            throws Throwable {
        Bitmap m_bitmap = null;
        MediaMetadataRetriever m_mediaMetadataRetriever = null;
        try {
            m_mediaMetadataRetriever = new MediaMetadataRetriever();
            m_mediaMetadataRetriever.setDataSource(p_videoPath);
            m_bitmap = m_mediaMetadataRetriever.getFrameAtTime();

        } catch (Exception m_e) {
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String p_videoPath)"
                            + m_e.getMessage());
        }
        finally {
            if (m_mediaMetadataRetriever != null) {
                m_mediaMetadataRetriever.release();
            }
        }
        return m_bitmap;
    }
}
