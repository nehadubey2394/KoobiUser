package com.mualab.org.user.activity.feeds.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mindiii on 16/8/17.
 */

public class Comment {

    @SerializedName("_id")
    public String id;

    public String comment;

    public int commentLikeCount;
    public int commentById;
    public int isLike;

    public String crd;

    public String userName;
    public String firstName;
    public String lastName;
    public String profileImage;
}
