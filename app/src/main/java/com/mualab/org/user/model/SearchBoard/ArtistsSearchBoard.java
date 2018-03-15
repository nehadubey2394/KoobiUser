package com.mualab.org.user.model.SearchBoard;

import android.os.Parcel;
import android.os.Parcelable;

import com.mualab.org.user.model.ArtistServices;
import com.mualab.org.user.model.booking.BookingStaff;
import com.mualab.org.user.model.booking.Services;

import java.util.ArrayList;

/**
 * Created by mindiii on 16/1/18.
 */
public class ArtistsSearchBoard implements Parcelable {

    public ArtistsSearchBoard(){}

    /*"_id":1,
"userName":"pankaj",
"firstName":"Pankaj",
"lastName":"Patidar",
"profileImage":"http://koobi.co.uk:3000/uploads/profile/1517822717364.jpg",
"ratingCount":"0",
"reviewCount":"0",
"postCount":"0",
"businessName":"Test",*/


    /*
 "_id":12,
"reviewCount":"0",
"profileImage":"cara2v.com:8000/uploads/profile/1516193068409.jpg",
"userName":"tt",
"firstName":"WEED",
"distance":0.6648645718639293,
"service":[
{
"_id":9,
"serviceId":1,
"subserviceId":1,
"description":"trgg",
"title":"Hardware"
}
]*/
    public  String _id,reviewCount,profileImage,userName,firstName,postCount,businessName,
            lastName,distance,ratingCount,businessType,serviceType,inCallpreprationTime,outCallpreprationTime,address;
    public  boolean isOutCallSelected = false;

    public  ArrayList<ArtistServices>service;
    public  ArrayList<Services>allService = new ArrayList<>();
    public ArrayList<BookingStaff>staffList = new ArrayList<>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_id);
        parcel.writeString(reviewCount);
        parcel.writeString(profileImage);
        parcel.writeString(userName);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeString(businessName);
        parcel.writeString(postCount);
        parcel.writeString(distance);
        parcel.writeString(ratingCount);
        parcel.writeString(businessType);
        parcel.writeString(serviceType);
        parcel.writeString(address);
    }

    private ArtistsSearchBoard(Parcel in) {
        _id = in.readString();
        reviewCount = in.readString();
        profileImage = in.readString();
        userName = in.readString();
        firstName = in.readString();
        postCount = in.readString();
        businessName = in.readString();
        lastName = in.readString();
        distance = in.readString();
        ratingCount = in.readString();
        businessType = in.readString();
        serviceType = in.readString();
        address = in.readString();
    }

    public static final Creator<ArtistsSearchBoard> CREATOR = new Creator<ArtistsSearchBoard>() {
        @Override
        public ArtistsSearchBoard createFromParcel(Parcel source) {
            return new ArtistsSearchBoard(source);
        }

        @Override
        public ArtistsSearchBoard[] newArray(int size) {
            return new ArtistsSearchBoard[size];
        }
    };
}
