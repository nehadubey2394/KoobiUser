package com.image.picker;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dharmraj on 21/3/18.
 */

public class ImageUtils {
    private static final String BASE_IMAGE_NAME = "i_prefix_";

    private ImageUtils() {
    }

    public static String savePicture(Context context, Bitmap bitmap, String imageSuffix) {
        File savedImage = getTemporalFile(context, imageSuffix + ".jpeg");
        FileOutputStream fos = null;
        if (savedImage.exists()) {
            savedImage.delete();
        }
        try {
            fos = new FileOutputStream(savedImage.getPath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return savedImage.getAbsolutePath();
    }

    public static File getTemporalFile(Context context, String payload) {
        return new File(context.getExternalCacheDir(), BASE_IMAGE_NAME + payload);
    }

}
