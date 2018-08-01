package com.mualab.org.user.activity.explore.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dharmraj on 4/4/18.
 **/

public class ExSearchTag implements Serializable{

    @SerializedName("_id")
    public int id;
    public int type;
    public String title = "N/A";
    public String desc;
    public String userType;

    @SerializedName("profileImage")
    public String imageUrl;

    @SerializedName(value="userName", alternate={"location"})
    public String uniTxt;
    public String tag;

    @SerializedName(value = "postCount", alternate = "tagCount")
    public String postCount;


    public static class SearchType {
        public static final int TOP = 0;
        public static final int PEOPLE = 1;
        public static final int HASH_TAG = 2;
        public static final int SERVICE_TAG = 3;
        public static final int LOCATION = 4;
    }


    public String getType(){
        switch (type){
            case 0: return "user";
            case 1: return "user";
            case 2: return "hasTag";
            case 3: return "servicetag";
            case 4: return "place";
            default: return "";
        }
    }
}
