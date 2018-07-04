package com.mualab.org.user.activity.artist_profile.model;

import com.mualab.org.user.data.model.booking.SubServices;

import java.util.ArrayList;

public class ArtistCategory {
    public String serviceId;
    public boolean isOutCall,isSelect = false;
    public String serviceName;
    public ArrayList<SubServices> arrayList = new ArrayList<>();
}
