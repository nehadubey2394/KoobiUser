package com.mualab.org.user.data.remote;

/**
 * Created by Administrator on 6/18/2016.
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import static com.mualab.org.user.data.remote.API.BASE_URL;


public class HttpMultipartTask extends AsyncTask<Void, Void, String> {

    private File file;
    private Map<String, String> params;
    private ProgressDialog pro_dialog = null;
    private AsyncEventListener eventListener;
    private int c_pro = 0;

    public HttpMultipartTask(Map<String, String> params, File file, ProgressDialog progress, AsyncEventListener eventListner) {
        this.file = file;
        this.params = params;
        this.pro_dialog = progress;
        this.eventListener = eventListner;
    }

    @Override
    protected String doInBackground(Void... params) {
        // File file = new File(filePath);
        return sendMultipart(BASE_URL + "user/addFeeds", "feed", file, this.params);
    }

    /*
 * (non-Javadoc)
 *
 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
 */
    @Override
    protected void onPostExecute(String result) {
        eventListener.onShowBubble(pro_dialog, result);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        System.out.println(c_pro);
        if (pro_dialog != null) {
            pro_dialog.setMessage("Uploading..." + String.valueOf(c_pro) + "%");
        }
        //adapter.notifyDataSetChanged();
//        if( mItem.mView != null ) {
//            mItem.mView.updateProgress();
//        }
    }

    private String sendMultipart(String url, String filefield, File file, Map<String, String> textdata) {

        final String twoHyphens = "--";
        final String boundary = "*****" + UUID.randomUUID().toString() + "*****";
        final String lineEnd = "\r\n";
        final int maxBufferSize = 4 * 1024;// 1024*1024*1;

        long tailLen = twoHyphens.length() + boundary.length() + lineEnd.length();
        String metadataPart = "Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + file.getPath() + "\"" + lineEnd;
        String contentType = "Content-Type: video/mp4" + lineEnd;
        //  String transEncoding = "Content-Transfer-Encoding: binary" + lineEnd + lineEnd;

        String paramString = "";
        for (Map.Entry<String, String> entry : textdata.entrySet()) {
            paramString += twoHyphens + boundary + lineEnd;
            paramString += "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd;
            //  paramString += "Content-Type: multipart/form-data" + lineEnd;
            paramString += lineEnd;
            paramString += entry.getValue();
            paramString += lineEnd;
        }

        DataOutputStream outputStream;

        try {
            long len = file.length();

            String keekAlive = "Connection" + "Keep-Alive";
            long contentLen = metadataPart.length() +
                    contentType.length() +
                    // transEncoding.length() +
                    tailLen + lineEnd.length() +
                    len +
                    lineEnd.length() +
                    paramString.length() +
                    tailLen;
            String content = "Content-length: " + contentLen;
            //long contentLength = metadataPart.length() + contentType.length() + transEncoding.length() + len + content.length() + paramString.length()
            //        + keekAlive.length() + lineEnd.length() + tailLen;

            URL connectionURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) connectionURL.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            //connection.setRequestProperty("Content-length:", "" + contentLen);
            connection.setFixedLengthStreamingMode((int) contentLen);


            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + file.getPath() + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: multipart/form-data" + lineEnd);
            //outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);

            FileInputStream fileInputStream = new FileInputStream(file);
            int bytesAvailable = fileInputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            int progress = bytesRead;
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);

                int pro = (int) (progress * 1.0 / len * 100);
                if (pro >= 100) {
                    pro = 99;
                }
                c_pro = pro;
//                System.out.println(pro);
                //adapter.notifyDataSetChanged();
                publishProgress();

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                progress += bytesRead;
            }

            outputStream.writeBytes(lineEnd);

            for (Map.Entry<String, String> entry : textdata.entrySet()) {
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: multipart/form-data" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(entry.getValue());
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            outputStream.flush();
            outputStream.close();

            //------------------------- receive response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            in.close();
            return response.toString();
        } catch (IOException e) {
            return "";
        }
    }


    public interface AsyncEventListener {
        void onShowBubble(ProgressDialog progress, String jsonString);
    }
}
