package com.mualab.org.user.model.booking;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Mindiii on 2/6/2018.
 */

public class BookingServices3 implements Serializable{
    public String _id,title,completionTime,inCallPrice,outCallPrice;
    @SerializedName("isOutCall")
    public boolean isOutCall;
}
