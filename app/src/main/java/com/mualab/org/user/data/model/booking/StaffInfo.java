package com.mualab.org.user.data.model.booking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaffInfo implements Serializable {

    public String staffId,staffName,staffImage,job,mediaAccess,holiday;

    public List<TimeSlot> staffHours = new ArrayList<>();

    public List<StaffServices> staffServices = new ArrayList<>();

    private Map<Integer, StaffServices> serviceList = new HashMap<>();

    public StaffInfo(){

    }

    public StaffInfo (StaffInfo tmp){
        this.staffId = tmp.staffId;
        this.staffName = tmp.staffName;
        this.staffImage = tmp.staffImage;
        this.job = tmp.job;
        this.mediaAccess = tmp.mediaAccess;
        this.holiday = tmp.holiday;
        this.staffHours = tmp.staffHours;
    }

    public StaffInfo setSerVice(StaffServices tm){
        staffServices.clear();
        staffServices.add(tm);
        return this;
    }

    public StaffServices findServces(int id){
        if(serviceList.size()!=staffServices.size()){
            for(StaffServices tmp : staffServices){
                serviceList.put(Integer.parseInt(tmp.artistServiceId), tmp);
            }
        }
        return serviceList.get(id);
    }
}
