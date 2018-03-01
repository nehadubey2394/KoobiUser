package com.mualab.org.user.model.booking;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mindiii on 2/6/2018.
 */

public class SubServices implements Serializable{
    public  String _id,subServiceName,serviceId,subServiceId;
    @SerializedName("isOutCall")
    public boolean isOutCall;
    @SerializedName("artistservices")
    public   ArrayList<BookingServices3>artistservices = new ArrayList<>();
}
