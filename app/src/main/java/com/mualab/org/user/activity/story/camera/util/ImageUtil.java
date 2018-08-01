package com.mualab.org.user.activity.story.camera.util;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mualab.org.user.activity.story.camera.ICallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import static com.mualab.org.user.activity.story.camera.util.Degrees.DEGREES_270;
import static com.mualab.org.user.activity.story.camera.util.Degrees.DEGREES_90;


/** Created by tomiurankar on 06/03/16. */
public class ImageUtil {
  private static final String TAG = "ImageUtil";
  /**
   * Saves byte[] array to disk
   *
   * @param input byte array
   * @param output path to output file
   * @param callback will always return in originating thread
   */
  public static void saveToDiskAsync(final byte[] input, final File output, final ICallback callback) {
    final Handler handler = new Handler();
    new Thread() {
      @Override
      public void run() {
        try {
          FileOutputStream outputStream = new FileOutputStream(output);
//          outputStream.write(input);
          outputStream.write(compress(input));
          outputStream.flush();
          outputStream.close();

          handler.post(new Runnable() {
                    @Override
                    public void run() {
                      callback.done(null);
                    }});
        } catch (final Exception e) {
          handler.post(new Runnable() {
                    @Override
                    public void run() {
                      callback.done(e);
                    }});
        }
      }
    }.start();
  }

  public static byte[] compress(byte[] data) throws IOException {
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    deflater.finish();
    byte[] buffer = new byte[1024];
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer); // returns the generated code... index
      outputStream.write(buffer, 0, count);
    }
    outputStream.close();
    byte[] output = outputStream.toByteArray();
    Log.d(TAG, "Original: " + data.length / 1024 + " Kb");
    Log.d(TAG, "Compressed: " + output.length / 1024 + " Kb");
    return output;
  }



  public static void saveToDiskAsync(final Bitmap bitmap, final File output, final ICallback callback){
    final Handler handler = new Handler();
    new Thread() {
      @Override
      public void run() {
        try {
          //Convert bitmap to byte array
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          bitmap.compress(Bitmap.CompressFormat.PNG, 80 /*ignored for PNG*/, bos);
          byte[] bitmapdata = bos.toByteArray();
          //write the bytes in file
          FileOutputStream fos = new FileOutputStream(output);
          fos.write(bitmapdata);
          fos.flush();
          fos.close();
          handler.post(new Runnable() {
            @Override
            public void run() {
              callback.done(null);
            }});
        } catch (final Exception e) {
          handler.post(new Runnable() {
            @Override
            public void run() {
              callback.done(e);
            }});
        }
      }
    }.start();
  }

  /**
   * Rotates the bitmap per their EXIF flag. This is a recursive function that will be called again
   * if the image needs to be downsized more.
   *
   * @param inputFile Expects an JPEG file if corrected orientation wants to be set.
   * @return rotated bitmap or null
   */
  @Nullable
  public static Bitmap getRotatedBitmap(String inputFile, int reqWidth, int reqHeight) {
    final int rotationInDegrees = getExifDegreesFromJpeg(inputFile);

    Log.d(TAG, "getRotatedBitmap: rotation: " + rotationInDegrees);
    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(inputFile, opts);
    opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight, rotationInDegrees);
    opts.inJustDecodeBounds = false;

    Log.d(TAG, "getRotatedBitmap: input file: " + inputFile);
    final Bitmap origBitmap = BitmapFactory.decodeFile(inputFile, opts);
    Log.d(TAG, "getRotatedBitmap: original bitmap: " + origBitmap);

    if (origBitmap == null) return null;

    Matrix matrix = new Matrix();
    matrix.preRotate(rotationInDegrees);
    // we need not check if the rotation is not needed, since the below function will then return the same bitmap. Thus no memory loss occurs.

    return Bitmap.createBitmap(
        origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
  }

  private static int calculateInSampleSize(
          BitmapFactory.Options options, int reqWidth, int reqHeight, int rotationInDegrees) {

    // Raw height and width of image
    final int height;
    final int width;
    int inSampleSize = 1;

    // Check for rotation
    if (rotationInDegrees == DEGREES_90 || rotationInDegrees == DEGREES_270) {
      width = options.outHeight;
      height = options.outWidth;
    } else {
      height = options.outHeight;
      width = options.outWidth;
    }

    if (height > reqHeight || width > reqWidth) {
      // Calculate ratios of height and width to requested height and width
      final int heightRatio = Math.round((float) height / (float) reqHeight);
      final int widthRatio = Math.round((float) width / (float) reqWidth);

      // Choose the smallest ratio as inSampleSize value, this will guarantee
      // a final image with both dimensions larger than or equal to the
      // requested height and width.
      inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }
    return inSampleSize;
  }

  private static int getExifDegreesFromJpeg(String inputFile) {
    try {
      final ExifInterface exif = new ExifInterface(inputFile);
      final int exifOrientation =
          exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
      if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
        return 90;
      } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
        return 180;
      } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
        return 270;
      }
    } catch (IOException e) {
      Log.e("exif", "Error when trying to get exif data from : " + inputFile, e);
    }
    return 0;
  }

  public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
    ExifInterface ei = new ExifInterface(image_absolute_path);
    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

    switch (orientation) {
      case ExifInterface.ORIENTATION_ROTATE_90:
        return rotate(bitmap, 90);

      case ExifInterface.ORIENTATION_ROTATE_180:
        return rotate(bitmap, 180);

      case ExifInterface.ORIENTATION_ROTATE_270:
        return rotate(bitmap, 270);

      case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
        return flip(bitmap, true, false);

      case ExifInterface.ORIENTATION_FLIP_VERTICAL:
        return flip(bitmap, false, true);

      default:
        return bitmap;
    }
  }

  public static Bitmap rotate(Bitmap bitmap, float degrees) {
    Matrix matrix = new Matrix();
    matrix.postRotate(degrees);
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }

  public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
    Matrix matrix = new Matrix();
    matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
  }
  public static Uri getImageUri(Context inContext, Bitmap inImage) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
    return Uri.parse(path);
  }
}
