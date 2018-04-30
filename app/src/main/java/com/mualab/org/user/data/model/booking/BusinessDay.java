package com.mualab.org.user.data.model.booking;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BusinessDay implements Serializable {

    /*"_id":679,
    "status":1,
    "crd":"2018-02-09T08:48:34.623Z",
    "upd":"2018-02-09T08:48:34.623Z",
    "day":0,
    "artistId":1,
    "startTime":"10:00 AM",
    "endTime":"07:00 PM",
    "__v":0*/

    public int id;
    public int dayId;
    public String dayName;
    public boolean isOpen;
    public ArrayList<TimeSlot> slots = new ArrayList<>();


    public void addTimeSlot(TimeSlot slot){
        if(slots==null) slots= new ArrayList<>();
        slots.add(slot);
    }

    public int getTimeSlotSize(){
        if(slots!=null) return slots.size();
        return 0;
    }

   /* public static class BookingTimeSlot{
        public int id = 1;
        public String startTime = "10:00 AM";
        public String endTime = "7:00 PM";
        public String slotTime = "10:00 AM - 7:00 PM";
    }
*/

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("dayName", dayName);
        result.put("slots", slots);
        return result;
    }

}
