package com.mualab.org.user.model.feeds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 9/8/17.
 */

public class Feeds implements Serializable {

    //public FeedType FEED_TYPE;
    // public static Feeds tmpFeed;
    public String profileImage;
    public int likeCount;
    public String fId;
    public String caption;
    public String upd;
    public int commentCount;
    public String likeStatus;
    public String videoThumbnail;
    public List<String> feedThumb = new ArrayList<>();
    public String city;
    public String crd;
    public String firebaseToken;
    public String feedType;
    public String userId;
    public String userName;
    public List<String> feed = new ArrayList<>();
    public String longitude;
    public String fullName;
    public String treadingPoint;
    public String latitude;
    public String isShare;
    public String chatId;
    public int viewPagerlastPos = 0;
}
