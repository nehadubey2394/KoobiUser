package com.mualab.org.user.activity.chat.model;

import java.io.Serializable;

public class Chat implements Serializable {
    public int readStatus;
    public String message;
    public int messageType;
    public String reciverId;
    public String senderId;
    public Object timestamp;
    public String banner_date;
}
