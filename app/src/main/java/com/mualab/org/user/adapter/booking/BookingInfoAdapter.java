package com.mualab.org.user.adapter.booking;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.adapter.feeds.LoadingViewHolder;
import com.mualab.org.user.model.booking.BookingInfo;
import java.util.ArrayList;


public class BookingInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingInfo> artistsList;
    private boolean showLoader;

    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    // Constructor of the class
    public BookingInfoAdapter(Context context, ArrayList<BookingInfo> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_info, parent, false);
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

        if (position != 0 && position == getItemCount() - 1) {
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
        final BookingInfo item = artistsList.get(position);

        holder.tvDateAndTime.setText(item.date+" "+item.time);
        holder.tvPrice.setText(item.price);
        holder.tvServiceName.setText(item.sServiceName);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrice,tvServiceName,tvDateAndTime;
        private ViewHolder(View itemView)
        {
            super(itemView);

            tvDateAndTime = itemView.findViewById(R.id.tvDateAndTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
        }
    }

}