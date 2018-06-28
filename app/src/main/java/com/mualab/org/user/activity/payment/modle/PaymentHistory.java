package com.mualab.org.user.activity.payment.modle;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistory {
    /*"_id":1,
"bookingDate":"2018-06-27",
"bookingTime":"01:00 PM",
"bookStatus":"3",
"location":"MINDIII Systems Pvt. Ltd., Main Road, Brajeshwari Extension, Pipliyahana, Indore, Madhya Pradesh, India",
"paymentType":3,
"transjectionId":"txn_893055",
"paymentStatus":1,
"reviewByUser":"",
"reviewByArtist":"",
"userRating":0,
"artistRating":0,
"reviewStatus":0,
"timeCount":780,
"totalPrice":"44.00",
"artistId":49,
"artistName":"koobi",
"artistProfileImage":"http://koobi.co.uk:5000/uploads/profile/1527934612821.jpg",
"userId":55,
"userName":"priya",
"userProfileImage":"http://koobi.co.uk:5000/uploads/profile/1528088236542.jpg",
"artistService":[
"Hair color"
]*/
    public int _id;
    public String bookingDate,bookingTime,bookStatus,location,paymentType,paymentStatus,reviewByUser,
            reviewByArtist,userRating,artistRating,reviewStatus,timeCount,totalPrice,
            artistId,userName,userProfileImage,artistProfileImage,artistName,transjectionId;

    public List<String> artistService = new ArrayList<>();

}
