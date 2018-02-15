package com.mualab.org.user.model.booking;

import java.util.ArrayList;

/**
 * Created by Mindiii on 2/6/2018.
 */

public class Services {
    public String id,sName;
    ArrayList<SubServices> arrayList;
    public boolean isExpand = false,isSubItemChecked = false;

    public ArrayList<SubServices> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<SubServices> arrayList) {
        this.arrayList = arrayList;
    }
}
