package com.mualab.org.user.activity.explore.model;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dharmraj on 4/4/18.
 */

public class ExSearchTag implements Serializable{

    @SerializedName("_id")
    public int id;
    public int type;
    public String title = "N/A";
    public String desc;

    @SerializedName("profileImage")
    public String imageUrl;

    @SerializedName(value="userName", alternate={"location"})
    public String uniTxt;
    public String tag;

    @SerializedName(value = "postCount", alternate = "tagCount")
    public String postCount;
}
