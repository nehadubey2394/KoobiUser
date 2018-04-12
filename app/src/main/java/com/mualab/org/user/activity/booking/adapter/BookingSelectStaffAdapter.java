package com.mualab.org.user.activity.booking.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.fragment.BookingFragment4;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.model.booking.BookingServices3;
import com.mualab.org.user.model.booking.BookingStaff;
import com.mualab.org.user.model.booking.SubServices;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class BookingSelectStaffAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingStaff> artistsList;
    private BookingInfo bookingInfo;
    private boolean isEdit;

    // Constructor of the class
    public BookingSelectStaffAdapter(Context context, ArrayList<BookingStaff> artistsList, BookingInfo bookingInfo,boolean isEdit) {
        this.context = context;
        this.artistsList = artistsList;
        this.bookingInfo = bookingInfo;
        this.isEdit = isEdit;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_select_staff_item_layout, parent, false);
        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final BookingStaff item = artistsList.get(position);

        holder.tvStaffArtistName.setText(item.userName);
        holder.tvSpaciality.setText(item.serviceName);
        if (!item.profileImage.equals("")){
            Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivSelectStaffProfile);
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
            ((BookingActivity)context).addFragment(
                    BookingFragment4.newInstance("Booking",isEdit,bookingInfo), true, R.id.flBookingContainer);

        }
    }

}