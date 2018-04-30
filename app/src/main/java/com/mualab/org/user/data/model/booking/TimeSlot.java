package com.mualab.org.user.data.model.booking;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dharmraj on 5/1/18.
 */


public class TimeSlot implements Serializable {

    @SerializedName("_id")
    public int id = 1;

    @SerializedName("day")
    public int dayId = 1;

    @SerializedName("startTime")
    public String startTime = "10:00 AM";

    @SerializedName("endTime")
    public String endTime = "07:00 PM";

    public String slotTime = "10:00 AM - 07:00 PM";

    @SerializedName("status")
    public int status = 1;

    public TimeSlot(int dayId){
        this.dayId = dayId;
    }
}
