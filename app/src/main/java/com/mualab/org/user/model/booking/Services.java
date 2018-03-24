package com.mualab.org.user.model.booking;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mindiii on 2/6/2018.
 */

public class Services implements Serializable{
    @SerializedName("serviceId")
    public String serviceId;
    @SerializedName("isOutCall")
    public boolean isOutCall;
    @SerializedName("serviceName")
    public String serviceName;
    @SerializedName("subServies")
    public ArrayList<SubServices> arrayList = new ArrayList<>();
    @SerializedName("isExpand")
    public boolean isExpand = false;
    @SerializedName("isSubItemChecked")
    public  boolean isSubItemChecked = false;
}
