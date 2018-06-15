package com.mualab.org.user.activity.artist_profile.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mindiii on 16/8/17.
 */

public class Comment {

    @SerializedName("_id")
    public int id;

    public String comment;

    public int commentLikeCount;
    public int commentById;
    public int isLike;

    public String userName;
    public String firstName;
    public String lastName;
    public String profileImage;
    public String timeElapsed;
    public String type;
}
