package com.mualab.org.user.activity.booking_histories.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mindiii on 3/16/2018.
 */

public class Bookings implements Serializable {
    public String _id,bookingDate,bookingTime,bookStatus,paymentType,paymentStatus,totalPrice,
            artistServiceName,location,transjectionId,isFinsh;

    public UserDetail userDetail;
   // public List<BookingInfo> todayBookingInfos = new ArrayList<>();
   // public List<BookingInfo> pendingBookingInfos = new ArrayList<>();
    /*{
booking":[
{
"_id":4,
"bookingDate":"2018-03-28",
"bookingTime":"10:00 AM",
"bookStatus":"1",
"paymentType":1,
"paymentStatus":0,
"totalPrice":"53.0",
"userDetail":[
{
"_id":1,
"userName":"pankaj",
"profileImage":"http://koobi.co.uk:3000/uploads/profile/1522044944762.jpg"
}
],
"bookingInfo":[
{
"_id":11,
"bookingPrice":"20",
"serviceId":3,
"subServiceId":4,
"artistServiceId":17,
"bookingDate":"2018-03-28",
"startTime":"10:00 AM",
"endTime":"10:50 AM",
"staffId":10,
"staffName":"shefali",
"staffImage":"http://koobi.co.uk:3000/uploads/profile/1522056616475.jpg",
"artistServiceName":"manicure"
},
]
},*/
}
