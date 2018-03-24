package com.mualab.org.user.model.booking;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Mindiii on 2/5/2018.
 */

public class BookingStaff implements Serializable{
   @SerializedName("_id")
   public String _id;
   @SerializedName("userName")
   public String userName;
   @SerializedName("profileImage")
   public String profileImage;
   @SerializedName("spaciality")
   public String spaciality;
   @SerializedName("serviceName")
   public String serviceName;
}
