package com.mualab.org.user.task;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * Created by mindiii on 22/8/17.
 */

public class UploadVideoTask {
    public static final String UPLOAD_URL = API.BASE_URL + "user/addFeeds";

    private int serverResponseCode;

    public String upload(String authToken, String file, String feedtypevalue, String captionvalue, String latitudevalue, String longitudevalue, String issharevalue, String cityvalue) {

        String fileName = file;
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(file);
        if (!sourceFile.isFile()) {
            Log.e("Huzza", "Source File Does not exist");
            return null;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
        /* *//**//*   FileInputStream fileInputStream = new FileInputStream(file);
            URL url = new URL(UPLOAD_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("authToken", authToken);
            Log.v("userAuthToken", authToken);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("feed",file);
            conn.setRequestProperty("mimeType", "video/mp4");
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            //dos.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"feed\";filename=\"" + file + "\"" + lineEnd);
            dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
            dos.writeBytes("Content-Length: " + file.length() + lineEnd);
            dos.writeBytes(lineEnd);
            int bufferLength = 1*1024*1024;


            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();
            //  Log.d(TAG, "img size  " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, bufferLength);
            ///  Log.d(TAG, "bufferlength = " + bufferLength + " & buffersize = " + bufferSize);

            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            // Log.d(TAG, "bytesread = " + bytesRead);


            for (int i = 0; i <buffer.length; i += bufferSize) {

                if (buffer.length - i >= bufferSize) {
                    dos.write(buffer, i, bufferSize);
                } else {
                    dos.write(buffer, i, buffer.length - i);
                }
            }*/
            URL url = new URL(UPLOAD_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("authToken", authToken);
            //  conn.setRequestProperty("feed", fileName);
            //conn.setRequestProperty("mimeType", "video/mp4");
            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"feed\";filename=\"" + fileName + "\"" + lineEnd);
            dos.writeBytes("Content-Type: video/mp4" + lineEnd);
            dos.writeBytes("Content-Length: " + fileName.length() + lineEnd);
            dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            Log.i("Huzza", "Initial .available : " + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // dos.writeBytes(lineEnd);
            // dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            fileInputStream.close();
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"feedType\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(feedtypevalue + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"caption\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(captionvalue + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"latitude\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(latitudevalue + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"longitude\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(longitudevalue + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"isShare\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(issharevalue + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            dos.writeBytes("Content-Disposition: form-data; name=\"city\"" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(cityvalue + lineEnd);
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.flush();
            dos.close();

            serverResponseCode = conn.getResponseCode();


        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (serverResponseCode == 200) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn
                        .getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
                rd.close();
            } catch (IOException ioex) {
            }
            return sb.toString();
        } else {
            return "Could not upload";
        }
    }


    public String sendMultipart(String authToken, String file, Map<String, String> textdata) {

        final String twoHyphens = "--";
        final String boundary = "*****" + UUID.randomUUID().toString() + "*****";
        final String lineEnd = "\r\n";
        final int maxBufferSize = 4 * 1024;// 1024*1024*1;

        long tailLen = twoHyphens.length() + boundary.length() + lineEnd.length();
        String metadataPart = "Content-Disposition: form-data; name=\"" + "feed" + "\"; filename=\"" + file + "\"" + lineEnd;
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

            URL connectionURL = new URL(UPLOAD_URL);
            HttpURLConnection connection = (HttpURLConnection) connectionURL.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            //connection.setRequestProperty("Content-length:", "" + contentLen);
            connection.setFixedLengthStreamingMode((int) contentLen);
            connection.setRequestProperty("authToken", authToken);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "feed" + "\"; filename=\"" + file + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: multipart/form-data" + lineEnd);
            //outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes("Content-Length: " + file.length() + lineEnd);
            outputStream.writeBytes(lineEnd);

            FileInputStream fileInputStream = new FileInputStream(file);
            int bytesAvailable = fileInputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];

            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            int progress = bytesRead;
            int prev = progress;
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);

                int pro = (int) (progress * 1.0 / len * 100);
                if (pro >= 100) {
                    pro = 99;
                }
                //c_pro = pro;
//                System.out.println(pro);
                //adapter.notifyDataSetChanged();
                //publishProgress();

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
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            in.close();
            String jsonString = response.toString();
            //JSONObject jobj = new JSONObject(jsonString);

//            if (pro_dialog != null) pro_dialog.setMessage("100%");

            return jsonString;
        } catch (IOException e) {
            return "";
        }
    }
}
