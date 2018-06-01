package com.mualab.org.user.data.model.SearchBoard;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Mindiii on 2/24/2018.
 */

@SuppressWarnings("serial")
public class RefineSearchBoard implements Serializable{
    public String latitude, longitude,page,limit,service,serviceType,day,time,subservice,date,location,sortSearch,sortType;
    public ArrayList<RefineServices> refineServices = new ArrayList<>();
    public ArrayList<RefineServices> tempSerevice = new ArrayList<>();
    public boolean isFavClick;
}
