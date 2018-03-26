package com.mualab.org.user.activity.booking.adapter;


import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.daimajia.swipe.SwipeLayout;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.fragment.BookingFragment4;
import com.mualab.org.user.activity.booking.fragment.BookingFragment5;
import com.mualab.org.user.activity.booking.listner.DeleteServiceListener;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.Helper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BookingInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingInfo> artistsList;
    private boolean isEdit = false;
    private  String bookingId ="";
    private DeleteServiceListener deleteServiceListener = null;

    public void setCustomListener(DeleteServiceListener deleteServiceListener){
        this.deleteServiceListener = deleteServiceListener;
    }

    // Constructor of the class
    public BookingInfoAdapter(Context context, ArrayList<BookingInfo> artistsList,boolean isEdit) {
        this.isEdit = isEdit;
        this.context = context;
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

        if (isEdit){
            if (item.date.equals("Select date") && item.time.equals("and time")) {
                holder.sample1.setSwipeEnabled(true);
            }else {
                holder.sample1.setSwipeEnabled(false);
            }
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
                    if (isEdit) {
                        if (deleteServiceListener != null) {
                            deleteServiceListener.onRemoveClick(getAdapterPosition());
                        }
                    }
                }
            });
        }
    }

}