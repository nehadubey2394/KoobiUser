package com.mualab.org.user.utils;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.listner.DatePickerListener;

import java.lang.reflect.Field;
import java.util.Calendar;

@SuppressLint("ValidFragment")
public class DatePickerFragment extends DialogFragment

implements DatePickerDialog.OnDateSetListener {
	
	private DatePickerListener listener;
    private static final String TAG = "DatePickerFragment";
    private Listener mListener = null;
    private int Cal_Type;
    private boolean startFromCurrentDate;
    private boolean endFromCurrentDate;

    public interface Listener 
    {
    public void onDateSett(int year, int month, int day);
    }

    public DatePickerFragment(int Cal_Type, boolean startFromCurrentDate, boolean endFromCurrentDate){
    	
    	this.Cal_Type = Cal_Type;
        this.startFromCurrentDate = startFromCurrentDate;
        this.endFromCurrentDate = endFromCurrentDate;
    }


@Override
public Dialog onCreateDialog(Bundle savedInstanceState) {

final Calendar c = Calendar.getInstance();
int year = c.get(Calendar.YEAR);
int month = c.get(Calendar.MONTH);
int day = c.get(Calendar.DAY_OF_MONTH);


// Create a new instance of DatePickerDialog and return it
//return  new DatePickerDialog(getActivity(), this, year, month, day);

 if(this.Cal_Type == Constant.CALENDAR_DAY){
	  
	  return hideyearCalendar( new DatePickerDialog(getActivity(), this, year, month, day));
  }

  if(this.Cal_Type == Constant.CALENDAR_DAY_PAST){

	  return showOnlyPastDate( new DatePickerDialog(getActivity(), this, year, month, day));
  }
    
return null;

}

public Dialog hideyearCalendar(DatePickerDialog datePickerDialog) {
    try
    {
         Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
         for (Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(datePickerDialog);

                    Field datePickerFields[] = datePickerDialogField.getType().getDeclaredFields();

                 }

              }
         datePickerDialog.setTitle("Select Date");
         
         if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
        	 datePickerDialog.getDatePicker().setCalendarViewShown(false);
         }
        if(this.startFromCurrentDate)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        return datePickerDialog;    
    }
    catch(Exception e)
    {
        Log.e(TAG, "EROOR"+e);
    return datePickerDialog;    
        
    }    
}


    public Dialog showOnlyPastDate(DatePickerDialog datePickerDialog) {
        try
        {
            Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
            for (Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(datePickerDialog);

                    Field datePickerFields[] = datePickerDialogField.getType().getDeclaredFields();

                }

            }
            datePickerDialog.setTitle("Select Date");

            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
                datePickerDialog.getDatePicker().setCalendarViewShown(false);
            }
            if(this.endFromCurrentDate)
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            return datePickerDialog;
        }
        catch(Exception e)
        {
            Log.e(TAG, "EROOR"+e);
            return datePickerDialog;

        }
    }


public Dialog only_Year(DatePickerDialog datePickerDialog) {
    try
    {
         Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
         for (Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker") || datePickerDialogField.getName().equals("mDateSpinner") || datePickerDialogField.getName().equals("mDateDialog")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(datePickerDialog);
                    Field datePickerFields[] = datePickerDialogField.getType().getDeclaredFields();
                    for (Field datePickerField : datePickerFields) {

                       if ("mDayPicker".equals(datePickerField.getName())||"mDaySpinner".equals(datePickerField.getName()) ||"mMonthSpinner".equals(datePickerField.getName()) ||"mMonthPicker".equals(datePickerField.getName())) { //mDayPicker
                          datePickerField.setAccessible(true);
                          Object dayPicker= new Object();
                          dayPicker = datePickerField.get(datePicker);
                          ((View) dayPicker).setVisibility(View.GONE);
                       }
                    }
                 }

              }
         datePickerDialog.setTitle("Select Year");
         if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
        	 datePickerDialog.getDatePicker().setCalendarViewShown(false);
         }
         
         if ( Build.VERSION.SDK_INT >= 21 ){
        	 datePickerDialog.getDatePicker().setCalendarViewShown(false);
        	 datePickerDialog.getDatePicker().setSpinnersShown(true);
         }
         
         if (Build.VERSION.SDK_INT >= 21) {
        	    int daySpinnerId = Resources.getSystem().getIdentifier("day", "id", "android");
        	    if (daySpinnerId != 0) {
        	        View daySpinner = datePickerDialog.getDatePicker().findViewById(daySpinnerId);
        	        if (daySpinner != null) {
        	            daySpinner.setVisibility(View.GONE);
        	        }
        	    }
        	}
         /*
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        	    int daySpinnerId = Resources.getSystem().getIdentifier("mMonthSpinner", "id", "android");
        	    if (daySpinnerId != 0) {
        	        View daySpinner = datePickerDialog.getDatePicker().findViewById(daySpinnerId);
        	        if (daySpinner != null) {
        	            daySpinner.setVisibility(View.GONE);
        	        }
        	    }
        	}*/

        return datePickerDialog;    
    }
    catch(Exception e)
    {
        Log.e(TAG, "EROOR"+e);
    return datePickerDialog;    
        
    }    
}


public Dialog only_Month(DatePickerDialog datePickerDialog) {
    try
    {
         Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
         for (Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(datePickerDialog);
                    Field datePickerFields[] = datePickerDialogField.getType().getDeclaredFields();
                    for (Field datePickerField : datePickerFields) {
                       if ("mDayPicker".equals(datePickerField.getName())|"mDaySpinner".equals(datePickerField.getName()) |"mYearSpinner".equals(datePickerField.getName()) |"mYearPicker".equals(datePickerField.getName())) { //mDayPicker
                          datePickerField.setAccessible(true);
                          Object dayPicker= new Object();
                          dayPicker = datePickerField.get(datePicker);
                          ((View) dayPicker).setVisibility(View.GONE);
                       }
                    }
                 }

              }
         datePickerDialog.setTitle("Select Month");
        
         if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
        	 datePickerDialog.getDatePicker().setCalendarViewShown(false);
         }

        return datePickerDialog;    
    }
    catch(Exception e)
    {
        Log.e(TAG, "EROOR"+e);
    return datePickerDialog;    
         
    }    
}

public void onDateSet(DatePicker view, int year, int month, int day) {

	this.listener.onDateSet(year, month, day, this.Cal_Type);
}
public void setonListener(Listener l){
    mListener = l;
}

public void setDateListener(DatePickerListener listener){
	
	this.listener = listener;
}
}