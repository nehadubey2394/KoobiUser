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
import com.mualab.org.user.model.booking.BookingStaff;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class BookingSelectStaffAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingStaff> artistsList;
    private boolean showLoader;
    private String serviceTitle;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    // Constructor of the class
    public BookingSelectStaffAdapter(Context context, ArrayList<BookingStaff> artistsList,String serviceTitle) {
        this.context = context;
        this.artistsList = artistsList;
        this.serviceTitle = serviceTitle;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchboard_item_layout, parent, false);
        return new ViewHolder(view);*/
        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_select_staff_item_layout, parent, false);
                ViewHolder viewHolder = new ViewHolder(view);
                return viewHolder;

            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public int getItemViewType(int position) {

        if (position != 0 && position == getItemCount() ) {
            return VIEWTYPE_LOADER;
        }
        return VIEWTYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) viewHolder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }

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
                    BookingFragment4.newInstance("Booking",""), true, R.id.flBookingContainer);

        }
    }

}