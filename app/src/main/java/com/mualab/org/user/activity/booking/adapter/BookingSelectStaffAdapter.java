package com.mualab.org.user.activity.booking.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.fragment.BookingFragment4;
import com.mualab.org.user.data.model.booking.BookingInfo;
import com.mualab.org.user.data.model.booking.StaffInfo;
import com.mualab.org.user.data.model.booking.StaffServices;
import com.mualab.org.user.utils.Util;
import com.squareup.picasso.Picasso;

import java.util.List;


public class BookingSelectStaffAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<StaffInfo> artistsList;
    private BookingInfo bookingInfo;
    private boolean isEdit;

    // Constructor of the class
    public BookingSelectStaffAdapter(Context context, List<StaffInfo> artistsList, BookingInfo bookingInfo, boolean isEdit) {
        this.context = context;
        this.artistsList = artistsList;
        this.bookingInfo = bookingInfo;
        this.isEdit = isEdit;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_select_staff_item_layout, parent, false);
        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final StaffInfo item = artistsList.get(position);

        holder.tvStaffArtistName.setText(item.staffName);
        holder.tvSpaciality.setText(item.job);
        if (!item.staffImage.equals("")){
            Picasso.with(context).load(item.staffImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivSelectStaffProfile);
        }else {
            holder.ivSelectStaffProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvStaffArtistName,tvSpaciality;
        ImageView ivSelectStaffProfile;
        private ViewHolder(View itemView)
        {
            super(itemView);

            ivSelectStaffProfile = itemView.findViewById(R.id.ivSelectStaffProfile);
            tvStaffArtistName = itemView.findViewById(R.id.tvStaffArtistName);
            tvSpaciality = itemView.findViewById(R.id.tvSpaciality);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final StaffInfo item = artistsList.get(getAdapterPosition());
            Util utility  = new Util(context);
            StaffServices staffServices = item.staffServices.get(0);
            bookingInfo.staffId = staffServices.artistId;
            if (bookingInfo.isOutCallSelect) {
                bookingInfo.price = Double.parseDouble(staffServices.outCallPrice);
            }else {
                bookingInfo.price = Double.parseDouble(staffServices.inCallPrice);
            }

            int ctMinuts = 0,ptMinuts;

            if (staffServices.completionTime.contains(":")){
                String hours,min;
                String[] separated = staffServices.completionTime.split(":");
                hours = separated[0];
                min = separated[1];
                ctMinuts = utility.getTimeInMin(Integer.parseInt(hours),Integer.parseInt(min));
            }

            if (bookingInfo.preperationTime.contains(":")){
                String hours,min;
                String[] separated = bookingInfo.preperationTime.split(":");
                hours = separated[0];
                min = separated[1];
                ptMinuts = utility.getTimeInMin(Integer.parseInt(hours),Integer.parseInt(min));

                bookingInfo.serviceTime = "00:"+(ptMinuts+ctMinuts);
                bookingInfo.endTime = ""+(ptMinuts+ctMinuts);
                bookingInfo.editEndTime = ""+(ptMinuts+ctMinuts);

            }

            ((BookingActivity)context).addFragment(
                    BookingFragment4.newInstance("Booking",isEdit,bookingInfo), true, R.id.flBookingContainer);

        }
    }

}