package com.mualab.org.user.data.model.booking;

import com.google.gson.annotations.SerializedName;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mindiii on 2/15/2018.
 */

public class BookingInfo implements Serializable {
    @SerializedName("artistService")
    public String artistService;
    @SerializedName("sServiceName")
    public String sServiceName;
    @SerializedName("date")
    public String date;
    @SerializedName("time")
    public String time;
    @SerializedName("sId")
    public String sId;
    @SerializedName("ssId")
    public String ssId;
    @SerializedName("msId")
    public String msId;
    @SerializedName("artistName")
    public String artistName;
    @SerializedName("profilePic")
    public String profilePic;
    public String staffId;
    @SerializedName("artistId")
    public String artistId;
    @SerializedName("serviceType")
    public String serviceType;
    @SerializedName("endTime")
    public String endTime;
    @SerializedName("editEndTime")
    public String editEndTime;
    @SerializedName("preperationTime")
    public String preperationTime;
    @SerializedName("serviceTime")
    public String serviceTime;
    @SerializedName("userId")
    public String userId;
    @SerializedName("artistAddress")
    public String artistAddress;
    @SerializedName("selectedDate")
    public String selectedDate;
    @SerializedName("bookingId")
    public String bookingId;
    public double price;
    @SerializedName("id")
    public double id;
    @SerializedName("subServices")
    public SubServices subServices;
    @SerializedName("item")
    public ArtistsSearchBoard item;
    @SerializedName("isOutCallSelect")
    public boolean isOutCallSelect;
    @SerializedName("dateTime")
    public Date dateTime;

}
