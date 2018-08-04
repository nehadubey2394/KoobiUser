package com.mualab.org.user.activity.people_tag.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class TagDetail implements Serializable{
    public String tabType,tagId,title,userType;

    public TagDetail(){

    }
    public TagDetail(String tabType, String tagId, String title,String userType){
        this.tabType = tabType;
        this.tagId = tagId;
        this.title = title;
        this.userType = userType;
    }

/*    private TagDetail(Parcel in) {
        tabType = in.readString();
        tagId = in.readString();
        title = in.readString();
    }

    public static final Creator<TagDetail> CREATOR = new Creator<TagDetail>() {
        @Override
        public TagDetail createFromParcel(Parcel in) {
            return new TagDetail(in);
        }

        @Override
        public TagDetail[] newArray(int size) {
            return new TagDetail[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tabType);
        dest.writeString(tagId);
        dest.writeString(title);
    }*/
}
