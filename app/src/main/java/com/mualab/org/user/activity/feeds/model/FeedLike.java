package com.mualab.org.user.activity.feeds.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dharmraj on 14/3/18.
 */

public class FeedLike {

    @SerializedName("_id")
    public int id;
    public int likeById;
    public int followingStatus;
    public String profileImage = "";
    public String fullName;
    public String userName;
    public String firstName;
    public String lastName;
}
