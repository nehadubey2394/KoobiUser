package com.mualab.org.user.data.model.SearchBoard;

import android.os.Parcel;
import android.os.Parcelable;

import com.mualab.org.user.data.model.ArtistServices;
import com.mualab.org.user.data.model.booking.BookingStaff;
import com.mualab.org.user.data.model.booking.Services;
import com.mualab.org.user.data.model.booking.StaffInfo;
import com.mualab.org.user.data.model.booking.StaffServices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mindiii on 16/1/18.
 */
public class ArtistsSearchBoard implements Serializable {
    public  String _id,reviewCount,profileImage,userName,firstName,postCount,businessName,
            lastName,distance,ratingCount,businessType,serviceType,inCallpreprationTime,outCallpreprationTime,address,categoryName,radius;
    public  boolean isOutCallSelected = false;
    public  ArrayList<ArtistServices>service;
    public  ArrayList<Services>allService = new ArrayList<>();
    public ArrayList<BookingStaff>staffList = new ArrayList<>();

    public ArrayList<StaffInfo>staffInfo = new ArrayList<>();

    public List<StaffInfo> findArtistByServiceId(int serviceId){
        List<StaffInfo> list = new ArrayList<>();

        for(StaffInfo tmpArtist : staffInfo){
            StaffServices service = tmpArtist.findServces(serviceId);
            if(service!=null)
                list.add(new StaffInfo(tmpArtist).setSerVice(service));
        }
        return list;
    }

    public double latitude,longitude;


 /*   @Override
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
        parcel.writeString(categoryName);
        parcel.writeString(radius);
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
        categoryName = in.readString();
        radius = in.readString();
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
    };*/
}
