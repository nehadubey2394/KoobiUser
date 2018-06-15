package com.mualab.org.user.activity.artist_profile.model;

import java.io.Serializable;

public class UserProfileData implements Serializable {
    /*  "userDetail": [
        {
            "_id": 14,
            "firstName": "nihaal",
            "lastName": "Verma",
            "userName": "nihaal",
            "businessName": "nihaal beauty zone",
            "businesspostalCode": "",
            "buildingNumber": "12",
            "businessType": "independent",
            "profileImage": "http://koobi.co.uk:5000/uploads/profile/1524478387559.jpg",
            "email": "nihaal@gmail.com",
            "gender": "male",
            "dob": "2000-06-04",
            "address": "MINDIII Systems Pvt. Ltd., Brajeshwari Extension, Greater Brajeshwari, Indore, Madhya Pradesh, India",
            "address2": "",
            "countryCode": "+91",
            "contactNo": "7879006062",
            "userType": "artist",
            "followersCount": "0",
            "followingCount": "0",
            "serviceCount": "2",
            "certificateCount": "0",
            "postCount": "0",
            "reviewCount": "0",
            "ratingCount": "0",
            "bio": "",
            "radius": "5",
            "serviceType": 3,
            "isCertificateVerify": 0,
            "followerStatus": 0,
            "favoriteStatus": 0
        }
    ]
}*/
    public String _id,firstName,lastName,userName,businessName,businesspostalCode,buildingNumber,businessType,
            profileImage,email,gender,dob,address,address2,countryCode,contactNo,userType,followersCount,
            followingCount,serviceCount,certificateCount,postCount,reviewCount,ratingCount,radius,
            bio,serviceType,isCertificateVerify,followerStatus,favoriteStatus;
}
