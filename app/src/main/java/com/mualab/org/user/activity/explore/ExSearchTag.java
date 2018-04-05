package com.mualab.org.user.activity.explore;


import com.google.gson.annotations.SerializedName;

/**
 * Created by dharmraj on 4/4/18.
 */

public class ExSearchTag {

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
}
