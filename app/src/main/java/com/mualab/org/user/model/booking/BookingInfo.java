package com.mualab.org.user.model.booking;

import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mindiii on 2/15/2018.
 */

public class BookingInfo implements Serializable {
    public String artistService,sServiceName,date,time,sId,ssId,msId,artistName,profilePic,artistId,serviceType,endTime,preperationTime,serviceTime,userId,artistAddress,selectedDate,bookingId="";
    public double price;
    public int id;
    public ArrayList<BookingServices3> artistsList = new ArrayList<>();
    public SubServices subServices;
    public  boolean isOutCallSelect;
    public ArtistsSearchBoard item;
}
