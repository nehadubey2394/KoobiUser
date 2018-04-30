package com.mualab.org.user.listner;

import com.mualab.org.user.activity.base.BaseListner;
import com.mualab.org.user.data.model.feeds.Feeds;

/**
 * Created by dharmraj on 10/4/18.
 */

public interface FeedsListner extends BaseListner{
    void getUpdatedFeed(int feedId);
    void apiForLikes(Feeds feeds);
    void setHeaderTitle(String title);
    void showToast(String txt);
}
