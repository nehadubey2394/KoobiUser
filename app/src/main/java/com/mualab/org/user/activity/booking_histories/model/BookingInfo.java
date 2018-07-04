package com.mualab.org.user.activity.booking_histories.model;

import java.io.Serializable;

/**
 * Created by Mindiii on 3/28/2018.
 */

public class BookingInfo implements Serializable {
    public String _Id,bookingPrice,serviceId,subServiceId,artistServiceId,location,startTime,endTime,staffId,staffName,staffImage,artistServiceName
            ,bookingDate,bookingStatus,companyName;


    /*"_id":18,
"bookingPrice":"250.0",
"serviceId":2,
"subServiceId":3,
"artistServiceId":17,
"bookingDate":"2018-05-19",
"startTime":"10:00 AM",
"endTime":"12:00 PM",
"staffId":13,
"staffName":"vinod",
"staffImage":"http://koobi.co.uk:3000/uploads/profile/1524478277525.jpg",
"companyId":26,
"companyName":"Styles n' Smiles",
"companyImage":"http://koobi.co.uk:3000/uploads/profile/",
"artistServiceName":"Party Make-up"*/
    //public Bookings bookingDetail;

}
