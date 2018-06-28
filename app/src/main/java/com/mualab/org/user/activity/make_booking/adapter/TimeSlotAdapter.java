package com.mualab.org.user.activity.make_booking.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.make_booking.listner.TimeSlotClickListener;
import com.mualab.org.user.data.model.booking.BookingTimeSlot;

import java.util.ArrayList;


public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    private Context context;
    private String s = "";
    private ArrayList<BookingTimeSlot> itemList;
    private TimeSlotClickListener customButtonListener = null;

    public void setCustomListener(TimeSlotClickListener customButtonListener){
        this.customButtonListener = customButtonListener;
    }

    // Constructor of the class
    public TimeSlotAdapter(Context context, ArrayList<BookingTimeSlot> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }


    // specify the row layout file and click for each row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeslot, parent, false);
        return new ViewHolder(view);
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
        BookingTimeSlot bookingTimeSlot = itemList.get(listPosition);

        holder.tvTime.setText(bookingTimeSlot.time);

        if (bookingTimeSlot.isSelected.equals("1")){
            holder.rlTimeSlot.setBackground(context.getResources().getDrawable(R.drawable.bg_green_background));

        } else {
            holder.rlTimeSlot.setBackground(context.getResources().getDrawable(R.drawable.circle_gray_background));

        }

    }

    // Static inner class to initialize the views of rows
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTime;
        RelativeLayout rlTimeSlot;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime =  itemView.findViewById(R.id.tvTime);
            rlTimeSlot = itemView.findViewById(R.id.rlTimeSlot);
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            if (customButtonListener != null) {
                customButtonListener.onButtonClick(getAdapterPosition(),"",0);
            }
        }
    }

}
