package com.mualab.org.user.activity.chat.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class GroupChat implements Serializable {
    public int readStatus;
    public String message;
    public int messageType;
    public String reciverId;
    public String senderId;
    public String memberCount;
    public String userName;
    public Object timestamp;

    @Exclude
    public  String banner_date;

    public Map<String,Object> readMember = new HashMap<>();
}
