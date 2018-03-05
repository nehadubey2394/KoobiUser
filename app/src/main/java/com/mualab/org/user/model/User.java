package com.mualab.org.user.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by dharmraj on 21/12/17.
 */

public class User implements Serializable{

    @SerializedName("_id")
    public int id;
    public String fullName;
    public String firstName;
    public String lastName;
    public String userName;
    public String businessName;
    public String gender;
    public String dob;
    public String profileImage;
    public String password;
    public String userType;

    public String countryCode;
    public String contactNo;
    public String email;

    public String address;
    public String latitude;
    public String longitude;

    public boolean otpVerified;
    public String otp;
    public String mailVerified;

    public String isLive;
    public String status;
    public String chatId;
    public String fireBaseId;
    public String socialId;
    public String firebaseToken;

    public String authToken;
    public String deviceToken;
    public String deviceType;
}
