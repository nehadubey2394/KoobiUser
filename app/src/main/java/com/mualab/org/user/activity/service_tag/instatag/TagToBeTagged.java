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
 */

package com.tagfeature.instatag;

import android.os.Parcel;
import android.os.Parcelable;

public class TagToBeTagged implements Parcelable {
    private String unique_tag_id;
    private Double x_co_ord;
    private Double y_co_ord;

    public TagToBeTagged(String unique_tag_id, Double x_co_ord, Double y_co_ord) {
        this.unique_tag_id = unique_tag_id;
        this.x_co_ord = x_co_ord;
        this.y_co_ord = y_co_ord;

    }

    public String getUnique_tag_id() {
        return unique_tag_id;
    }

    public void setUnique_tag_id(String unique_tag_id) {
        this.unique_tag_id = unique_tag_id;
    }

    public Double getX_co_ord() {
        return x_co_ord;
    }

    public void setX_co_ord(Double x_co_ord) {
        this.x_co_ord = x_co_ord;
    }

    public Double getY_co_ord() {
        return y_co_ord;
    }

    public void setY_co_ord(Double y_co_ord) {
        this.y_co_ord = y_co_ord;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.unique_tag_id);
        dest.writeValue(this.x_co_ord);
        dest.writeValue(this.y_co_ord);
    }

    private TagToBeTagged(Parcel in) {
        this.unique_tag_id = in.readString();
        this.x_co_ord = (Double) in.readValue(Double.class.getClassLoader());
        this.y_co_ord = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Creator<TagToBeTagged> CREATOR =
            new Creator<TagToBeTagged>() {
                @Override
                public TagToBeTagged createFromParcel(Parcel source) {
                    return new TagToBeTagged(source);
                }

                @Override
                public TagToBeTagged[] newArray(int size) {
                    return new TagToBeTagged[size];
                }
            };
}
