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
    @SerializedName("serviceName")
    public String serviceName;
    @SerializedName("subServies")
    public ArrayList<SubServices> arrayList = new ArrayList<>();
    public boolean isExpand = false,isSubItemChecked = false;
}
