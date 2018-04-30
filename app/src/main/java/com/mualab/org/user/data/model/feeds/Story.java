package com.mualab.org.user.data.model.feeds;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by mindiii on 26/8/17.
 */

public class Story implements Serializable {

    @SerializedName("_id")
    public int id;
    public String userId;

    @SerializedName("type")
    public String storyType;

    public String myStory;
    public String videoThumb;
    public String crd;

}



