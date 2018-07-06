/*
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tagfeature.models;

import android.os.Parcel;
import android.os.Parcelable;


import com.tagfeature.instatag.TagToBeTagged;

import java.util.ArrayList;

public class TaggedPhoto implements Parcelable {
    private String id;
    private String imageUri;
    private ArrayList<TagToBeTagged> tagToBeTaggeds;

    public TaggedPhoto() {
    }

    public TaggedPhoto(String id, String imageUri, ArrayList<TagToBeTagged> tagToBeTaggeds) {
        this.id = id;
        this.imageUri = imageUri;
        this.tagToBeTaggeds = tagToBeTaggeds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public ArrayList<TagToBeTagged> getTagToBeTaggeds() {
        return tagToBeTaggeds;
    }

    public void setTagToBeTaggeds(ArrayList<TagToBeTagged> tagToBeTaggeds) {
        this.tagToBeTaggeds = tagToBeTaggeds;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.imageUri);
        dest.writeTypedList(this.tagToBeTaggeds);
    }

    private TaggedPhoto(Parcel in) {
        this.id = in.readString();
        this.imageUri = in.readString();
        this.tagToBeTaggeds = in.createTypedArrayList(TagToBeTagged.CREATOR);
    }

    public static final Creator<TaggedPhoto> CREATOR = new Creator<TaggedPhoto>() {
        @Override
        public TaggedPhoto createFromParcel(Parcel source) {
            return new TaggedPhoto(source);
        }

        @Override
        public TaggedPhoto[] newArray(int size) {
            return new TaggedPhoto[size];
        }
    };
}
