package com.mualab.org.user.model.SearchBoard;

import com.mualab.org.user.model.SearchBoard.RefineSubServices;

import java.util.ArrayList;

/**
 * Created by Mindiii on 2/6/2018.
 */

public class RefineServices {
    /*"serviceList":[
{
"_id":1,
"title":"Hair",
"subService":[
{
"_id":1,
"image":"http://koobi.co.uk:3000/uploads/subservice/1512477301012.jpg",
"serviceId":1,
"title":"Braids & Twist"
},
{
"_id":2,
"image":"http://koobi.co.uk:3000/uploads/subservice/1512477371723.jpg",
"serviceId":1,
"title":"Hair Color"
}
]
},*/
    public String id,title;
    ArrayList<RefineSubServices> arrayList;
    public String isChecked = "0";
    public boolean isExpand = false,isSubItemChecked = false;

    public ArrayList<RefineSubServices> getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList<RefineSubServices> arrayList) {
        this.arrayList = arrayList;
    }
}
