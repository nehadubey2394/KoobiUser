package com.mualab.org.user.activity.gellery.model;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

public class VideoLoader extends CursorLoader {

    // Get relevant columns for use later.
    private static final String[] PROJECTION = {
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Video.VideoColumns.TITLE
    };

/*
    // Return only video and image metadata.
    private static final String SELECTION = MediaStore.Video.VideoColumns.MEDIA_TYPE + "="
           *//* + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="*//*
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;*/

    private static final String ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC";
   // Uri queryUri = MediaStore.Files.getContentUri("external");

    public VideoLoader(Context context, String[] selectionArgs) {
        super(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION, null,
                selectionArgs, ORDER_BY);
    }
}
