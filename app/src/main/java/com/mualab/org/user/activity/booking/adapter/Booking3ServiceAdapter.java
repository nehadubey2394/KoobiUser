package com.mualab.org.user.activity.booking.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.fragment.BookingFragment1;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.booking.fragment.BookingFragment4;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.booking.BookingServices3;
import com.mualab.org.user.session.Session;

import java.util.ArrayList;


public class Booking3ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingServices3> artistsList;
    private boolean showLoader;
    private String serviceTitle;
    private ArtistsSearchBoard item;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    private Session session = Mualab.getInstance().getSessionManager();

    // Constructor of the class
    public Booking3ServiceAdapter(Context context, ArrayList<BookingServices3> artistsList, String serviceTitle,ArtistsSearchBoard item) {
        this.context = context;
        this.artistsList = artistsList;
        this.serviceTitle = serviceTitle;
        this.item = item;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking3_last_list, parent, false);
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

        if (position != 0 && position == getItemCount()) {
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
        final BookingServices3 item = artistsList.get(position);
        holder.tvLastService.setText(item.title);

        String CurrentString = item.completionTime;
        if (CurrentString.contains(":")){
            String[] separated = CurrentString.split(":");
            String hours = separated[0]+" hrs ";
            String min = separated[1]+" min";
            holder.tvTime.setText(hours+min);
        }

        if (session.getIsOutCallFilter()){
            holder.tvAmount.setText(item.outCallPrice);
        }else {
            holder.tvAmount.setText(item.inCallPrice);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount,tvLastService,tvTime;
        LinearLayout lyServiceDetail;
        private ViewHolder(View itemView)
        {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            lyServiceDetail = itemView.findViewById(R.id.lyServiceDetail);
            tvLastService = itemView.findViewById(R.id.tvLastService);
            tvTime = itemView.findViewById(R.id.tvTime);

            lyServiceDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (((BookingActivity)context).item.businessType.equals("independent")){
                        ((BookingActivity)context).addFragment(
                                BookingFragment4.newInstance(serviceTitle,""), true, R.id.flBookingContainer);

                    }else {
                        ((BookingActivity)context).addFragment(
                                BookingFragment1.newInstance(serviceTitle,item), true, R.id.flBookingContainer);

                    }
                }
            });
        }
    }

}