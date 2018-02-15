package com.mualab.org.user.model.SearchBoard;

import android.os.Parcel;
import android.os.Parcelable;

import com.mualab.org.user.model.ArtistServices;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by mindiii on 16/1/18.
 */
public class ArtistsSearchBoard implements Parcelable {

    public ArtistsSearchBoard(){}
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
    public  String id,reviewCount,profileImage,userName,firstName,distance,rating;

    public  ArrayList<ArtistServices>service;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(reviewCount);
        parcel.writeString(profileImage);
        parcel.writeString(userName);
        parcel.writeString(firstName);
        parcel.writeString(distance);
        parcel.writeString(rating);
    }

    private ArtistsSearchBoard(Parcel in) {
        id = in.readString();
        reviewCount = in.readString();
        profileImage = in.readString();
        userName = in.readString();
        firstName = in.readString();
        distance = in.readString();
        rating = in.readString();
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
