package com.mualab.org.user.model.feeds;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 9/8/17.
 */

public class Feeds implements Serializable {

    //public FeedType FEED_TYPE;
    // public static Feeds tmpFeed;
    public int _id;
    public String feedType;
    public List<String> feed = new ArrayList<>();
    public List<Feed> feedData = new ArrayList<>();
    public List<String> feedThumb = new ArrayList<>();

    public String caption;
    @SerializedName("location")
    public String location;
    public int likeCount;
    public int commentCount;
    public int followingStatus;
    public String crd;

    public List<User> userInfo;
    public String timeElapsed;

    /*tmp*/
    public String fullName;
    public String userName;
    public String profileImage;

    public int isLike;
    public String videoThumbnail;

   // public String city;
    public int userId;

    public String longitude;
    public String latitude;
    public int viewPagerlastPos = 0;

    public class Feed implements Serializable{
        public String feedPost;
        public String videoThumb;
    }

    public class User implements Serializable{
        public int _id;
        public String firstName;
        public String lastName;
        public String userName;
        public String profileImage;
    }
}
