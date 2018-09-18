package com.mualab.org.user.activity.chat.model;

import java.io.Serializable;

public class ChatHistory implements Serializable {
    public String message;
    public int messageType;
    public int unreadMessage;
    public int memberCount;
    public int favourite;
    public String reciverId;
    public String senderId;
    public String profilePic;
    public String memberType;
    public String userName;
    public String type;//user,group,broadcast
    public Object timestamp;

    public String banner_date;

}

/*favourite:
"0"
memberCount:
0
message:
"Hii"
messageType:
0
profilePic:
"http://koobi.co.uk:3000/uploads/profile/1534409..."
reciverId:
"2"
senderId:
"17"
timestamp:
1534933922637
type:
"user"
unreadMessage:
1
userName:
"Devendra"
*/