package com.mualab.org.user.data.model.booking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StaffInfo implements Serializable {
    public String staffId,staffName,staffImage,job,mediaAccess,holiday;

    public List<TimeSlot> staffHours = new ArrayList<>();

    public List<StaffServices> staffServices = new ArrayList<>();
}
