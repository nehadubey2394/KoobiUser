package com.mualab.org.user.activity.artist_profile.model;

/**
 * Created by dharmraj on 2/2/18.
 */

public class Certificate {
/*"certificateImage":"http://koobi.co.uk:5000/uploads/certificateImage/1526306052177.jpg",
"status":0,
"crd":"2018-05-14T12:50:01.233Z",
"upd":"2018-05-14T12:50:01.233Z",
"_id":6,
"artistId":49,
"__v":0*/
    public int id;
    public int status;
    public boolean isSelected;
    public String imageUri,certificateImage;

    public Certificate(){

    }

    public Certificate(int id){
        this.id = id;
    }

}
