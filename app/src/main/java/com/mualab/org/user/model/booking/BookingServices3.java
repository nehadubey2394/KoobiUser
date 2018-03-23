package com.mualab.org.user.model.booking;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Mindiii on 2/6/2018.
 */

public class BookingServices3 implements Serializable{
    @SerializedName("_id")
    public String _id;

    @SerializedName("title")
    public String title;

    @SerializedName("completionTime")
    public String completionTime;

    @SerializedName("inCallPrice")
    public String inCallPrice;

    @SerializedName("outCallPrice")
    public String outCallPrice;

    @SerializedName("isSelected")
    private boolean isSelected;

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }

    @SerializedName("isBooked")
    private boolean isBooked;
    @SerializedName("isOutCall3")
    public boolean isOutCall3;
}
