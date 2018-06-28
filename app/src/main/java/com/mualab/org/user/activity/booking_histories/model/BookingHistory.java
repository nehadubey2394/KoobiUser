package com.mualab.org.user.activity.booking_histories.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BookingHistory implements Serializable{
    /*{
"_id":19,
"bookingDate":"2018-06-15",
"bookingTime":"03:40 PM",
"bookStatus":"3",
"location":"MINDIII Systems Pvt. Ltd., Main Road, Brajeshwari Extension, Pipliyahana, Indore, Madhya Pradesh, India",
"paymentType":3,
"paymentStatus":1,
"reviewByUser":"Test Hello",
"reviewByArtist":"",
"userRating":3,
"artistRating":0,
"reviewStatus":0,
"timeCount":940,
"totalPrice":"20",
"artistId":44,
"userName":"john",
"profileImage":"http://koobi.co.uk:5000/uploads/profile/1525847402211.jpg",
"artistService":[
"new hair color "
]
},*/
    public int _id;
    public String bookingDate,bookingTime,bookStatus,location,paymentType,paymentStatus,reviewByUser,
            reviewByArtist,userRating,artistRating,reviewStatus,timeCount,totalPrice,artistId,userName,profileImage;

    public List<String> artistService = new ArrayList<>();
    public boolean isExpand ;
}
