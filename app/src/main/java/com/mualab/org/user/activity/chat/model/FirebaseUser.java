package com.mualab.org.user.activity.chat.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class FirebaseUser implements Serializable{
    public String firebaseToken,profilePic,userName,userType,authToken;
    public int uId,isOnline,banAdmin;
    public Object lastActivity;
    @Exclude
    public boolean isChecked = false;


}
