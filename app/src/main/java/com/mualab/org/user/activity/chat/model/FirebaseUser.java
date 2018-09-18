package com.mualab.org.user.activity.chat.model;

import java.io.Serializable;

public class FirebaseUser implements Serializable{
    public String firebaseToken,profilePic,userName,userType,authToken;
    public long uId,isOnline,banAdmin;
    public Object lastActivity;
}
