package com.mualab.org.user.model.feeds;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by mindiii on 9/8/17.
 **/

public class LiveUserInfo implements Serializable {

    @SerializedName("_id")
    public int id;

    @SerializedName("count")
    public int storyCount = 1;

    public String isLive;

    public String userName;

    public String firstName;

    public String lastName;

    public String profileImage;

    public String fullName;

}
