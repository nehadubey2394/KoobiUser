package com.mualab.org.user.data.remote;

/**
 * Created by mindiii on 3/8/17.
 */

public class ServerResponseCode {


    public static String getmeesageCode(int code) {
        String valueofmessage = "";
        switch (code) {

            case 101:
                valueofmessage = "Continue";
                break;
            case 200:
                valueofmessage = "Ok";
                break;
            case 202:
                valueofmessage = "Accepted";
                break;
            case 203:
                valueofmessage = "Non-Authoritative Information";
                break;
            case 204:
                valueofmessage = "No Content";
                break;
            case 300:
                valueofmessage = "Multiple Choices";
                break;
            case 302:
                valueofmessage = "Found";
                break;
            case 304:
                valueofmessage = "Not Modified";
                break;
            case 305:
                valueofmessage = "Use Proxy";
                break;
            case 400:
                valueofmessage = "Your session is expired please login again";
                break;
            case 404:
                valueofmessage = "Not Found";
                break;
            case 502:
                valueofmessage = "Bad Gateway";
                break;
            case 503:
                valueofmessage = "Service Unavailable";
                break;
            case 504:
                valueofmessage = "Gateway Timeout";
                break;
            case 505:
                valueofmessage = "HTTP Version Not Supported";
                break;

        }

        return valueofmessage;


    }


}
