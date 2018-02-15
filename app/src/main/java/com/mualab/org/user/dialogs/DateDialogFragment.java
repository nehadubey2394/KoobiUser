package com.mualab.org.user.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.mualab.org.user.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by dharmraj on 2/1/18.
 */

@SuppressLint("ValidFragment")
public class DateDialogFragment extends DialogFragment {

    public static String TAG = "DateDialogFragment";
    private Context mContext;
    private Calendar sDate;
    private DateDialogFragmentListener sListener;

    @SuppressLint("ValidFragment")
    public DateDialogFragment(@NonNull Context context) {
        this.mContext = context;
    }

    public static DateDialogFragment newInstance(Context context, int titleResource, Calendar date){
        DateDialogFragment dialog  = new DateDialogFragment(context);
        dialog.sDate = date;
        Bundle args = new Bundle();
        args.putInt("title", titleResource);
        dialog.setArguments(args);
        return dialog;
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    //create new Calendar object for date chosen
                    //this is done simply combine the three args into one
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
                    //call back to the DateDialogFragment listener
                    if(sListener!=null)
                        sListener.dateDialogFragmentDateSet(newDate);

                }
            };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
       // return new DatePickerDialog(sContext, dateSetListener, sDate.get(Calendar.YEAR), sDate.get(Calendar.MONTH), sDate.get(Calendar.DAY_OF_MONTH));
        DatePickerDialog dialog = new DatePickerDialog(mContext,
                AlertDialog.THEME_HOLO_LIGHT,
                dateSetListener,
                sDate.get(Calendar.YEAR),
                sDate.get(Calendar.MONTH),
                sDate.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMaxDate(new Date().getTime());
        return dialog;
    }

    public interface DateDialogFragmentListener{
        public void dateDialogFragmentDateSet(Calendar date);
    }

    public void setDateDialogFragmentListener(DateDialogFragmentListener listener){
        sListener = listener;
    }
}
