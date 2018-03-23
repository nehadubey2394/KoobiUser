package com.mualab.org.user.model.booking;

import com.google.gson.annotations.SerializedName;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mindiii on 2/15/2018.
 */

public class BookingInfo implements Serializable {
   // @SerializedName("title")

    public transient String artistService,sServiceName,date,time,sId,ssId,msId,artistName,profilePic,artistId,serviceType,endTime,editEndTime,preperationTime,serviceTime,userId,artistAddress,selectedDate,bookingId="";
    public transient double price;
    public transient int id;
    public transient ArrayList<BookingServices3> artistsList = new ArrayList<>();
    public transient SubServices subServices;
    public transient  boolean isOutCallSelect;
    public transient ArtistsSearchBoard item;
}
