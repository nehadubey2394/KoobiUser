package com.mualab.org.user.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.util.FileUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by dharmraj on 26/10/17.
 */

public class UploadImage {

    private List<Uri> mSelectedImages;
    private Map<String, String> map;
    private String authToken;
    private Context mContext;
    private Listner listner;

    public UploadImage(Context mContext, String authToken, Map<String, String> map, List<Uri> mSelectedImages, Listner listner) {
        this.mContext = mContext;
        this.authToken = authToken;
        this.map = map;
        this.listner = listner;
        this.mSelectedImages = mSelectedImages;
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public void execute() {
        AddEventTask task = new AddEventTask();
        task.execute();
    }

    private String uploadMultipleFile(String web_url) {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;

        String serverResponseMessage = null;
        String responce = null;

        try {

            URL url = new URL(web_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authtoken", authToken);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            List<File> event_images = new ArrayList<>();
            for (int index = 0, size = mSelectedImages.size(); index < size; index++) {
                File file = FileUtils.getFile(mContext, mSelectedImages.get(index));
                event_images.add(file);
            }

            for (int k = 0; k < event_images.size(); k++) {
                String key = "feed[]";
                dos.writeBytes("Content-Disposition: form-data;name=\"" + key + "\";filename=\"" + event_images.get(k).getName() + "\"" + lineEnd);
                dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                dos.writeBytes("Content-Length: " + event_images.get(k).length() + lineEnd);
                dos.writeBytes(lineEnd);

                int bufferLength = 3 * 1024 * 1024;
                FileInputStream fileInputStream = new FileInputStream(event_images.get(k).getAbsoluteFile());

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, bufferLength);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);


                for (int i = 0; i < buffer.length; i += bufferSize) {

                    if (buffer.length - i >= bufferSize) {
                        dos.write(buffer, i, bufferSize);
                    } else {
                        dos.write(buffer, i, buffer.length - i);
                    }
                }

                fileInputStream.close();
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
            }


            if (map != null) {

                for (Map.Entry<String, String> entry : map.entrySet()) {
                    System.out.println(entry.getKey() + "/" + entry.getValue());

                    String key = entry.getKey();
                    String value = entry.getValue();
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(value);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                }
            }

            //dos.writeBytes("Content-Type: text/plain; charset=UTF-8"+lineEnd);
            /*dos.writeBytes("Content-Disposition: form-data; name=\"eventName\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(title);
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);*/

            dos.flush();
            dos.close();

            int serverResponseCode = conn.getResponseCode();
            serverResponseMessage = conn.getResponseMessage();
            Log.i("filePathValue", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
            if (serverResponseCode <= 200) {
                serverResponseMessage = convertStreamToString(conn.getInputStream());
                System.out.println(serverResponseMessage);
                if (listner != null)
                    listner.onResponce(serverResponseMessage);
            } else {
                if (listner != null)
                    listner.onError(serverResponseMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponseMessage;

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    public interface Listner {
        void onResponce(String responce);

        void onError(String error);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    public class AddEventTask extends AsyncTask<Void, Void, Boolean> {

        String result;
        String resultSuccess;
        int serverResponseCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Progress.show(mContext);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";

                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                String serverResponseMessage = null;

                try {

                    URL url = new URL(API.BASE_URL + "user/addFeeds");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                   // conn.setConnectTimeout(2000);
                   // conn.setChunkedStreamingMode(1024);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("authToken", authToken);
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                    if (mSelectedImages != null) {

                        List<File> event_images = new ArrayList<>();

                        for (int index = 0, size = mSelectedImages.size(); index < size; index++) {

                            Uri uri = mSelectedImages.get(index);
                            String authority =uri.getAuthority();
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(),uri);
                            File file = FileUtils.savebitmap(mContext, bitmap, "tmp"+index);
                            event_images.add(file);
                        }

                        for (int k = 0; k < event_images.size(); k++) {
                            String key = "feed[]";
                            dos.writeBytes("Content-Disposition: form-data;name=\"" + "feed[" + k + "]" + "\";filename=\"" + event_images.get(k).getName() + "\"" + lineEnd);
                            dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                            dos.writeBytes("Content-Length: " + event_images.get(k).length() + lineEnd);
                            dos.writeBytes(lineEnd);

                            int bufferLength = 5 * 1024 * 1024;
                            FileInputStream fileInputStream = new FileInputStream(event_images.get(k).getAbsoluteFile());

                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, bufferLength);
                            buffer = new byte[bufferSize];
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);


                            for (int i = 0; i < buffer.length; i += bufferSize) {
                                if (buffer.length - i >= bufferSize) {
                                    dos.write(buffer, i, bufferSize);
                                } else {
                                    dos.write(buffer, i, buffer.length - i);
                                }
                            }
                            fileInputStream.close();
                            dos.writeBytes(lineEnd);
                            dos.writeBytes(twoHyphens + boundary + lineEnd);
                        }
                    }


                    if (map != null) {

                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            System.out.println(entry.getKey() + "/" + entry.getValue());
                            String key = entry.getKey();
                            String value = entry.getValue();

                            dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
                            dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                            dos.writeBytes(lineEnd);
                            dos.writeBytes(value);
                            dos.writeBytes(lineEnd);
                            dos.writeBytes(twoHyphens + boundary + lineEnd);
                        }
                    }

                    dos.flush();
                    dos.close();

                    serverResponseCode = conn.getResponseCode();
                    result = conn.getResponseMessage();
                    Log.i("filePathValue", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);
                    if (serverResponseCode <= 200) {
                        serverResponseMessage = convertStreamToString(conn.getInputStream());
                        System.out.println(serverResponseMessage);
                        resultSuccess = result;
                    } else {
                        result = conn.getErrorStream().toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result= e.getMessage();
                }

            } catch (Exception e) {
                e.printStackTrace();
                result= e.getMessage();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Progress.hide(mContext);
            if (serverResponseCode <= 200) {
                System.out.println(resultSuccess);
                if (listner != null)
                    listner.onResponce(resultSuccess);
            } else {
                if (listner != null)
                    listner.onError(result);
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Progress.hide(mContext);
        }
    }


    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=85;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
