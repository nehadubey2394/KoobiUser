package com.mualab.org.user.activity.booking.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.listner.DeleteServiceListener;
import com.mualab.org.user.model.booking.BookingInfo;

import java.util.ArrayList;


public class BookingInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<BookingInfo> artistsList;
    private DeleteServiceListener deleteServiceListener = null;

    public void setCustomListener(DeleteServiceListener deleteServiceListener){
        this.deleteServiceListener = deleteServiceListener;
    }

    // Constructor of the class
    public BookingInfoAdapter(Context context, ArrayList<BookingInfo> artistsList) {
        this.artistsList = artistsList;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_info, parent, false);
        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {


        final ViewHolder holder = ((ViewHolder) viewHolder);
        final BookingInfo item = artistsList.get(position);

        if (item.date.equals("Select date") && item.time.equals("and time")) {
            holder.tvDateAndTime.setText(item.date+" "+item.time);
        }else
            holder.tvDateAndTime.setText(item.date+", "+item.time);

        holder.tvPrice.setText("£"+item.price);
        holder.tvServiceName.setText(item.artistService);

       // if (isEdit){
            if (item.date.equals("Select date") && item.time.equals("and time")) {
                holder.sample1.setSwipeEnabled(true);
            }else {
                holder.sample1.setSwipeEnabled(false);
            }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrice,tvServiceName,tvDateAndTime;
        SwipeLayout sample1;
        LinearLayout lyRemove;
        private ViewHolder(View itemView)
        {
            super(itemView);

            tvDateAndTime = itemView.findViewById(R.id.tvDateAndTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            sample1 = itemView.findViewById(R.id.sample1);
            lyRemove = itemView.findViewById(R.id.lyRemove);

            lyRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        if (deleteServiceListener != null) {
                            deleteServiceListener.onRemoveClick(getAdapterPosition());
                        }

                }
            });
        }
    }

}