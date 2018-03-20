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
import com.mualab.org.user.activity.booking.fragment.BookingFragment4;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.model.booking.BookingServices3;
import com.mualab.org.user.model.booking.SubServices;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.util.Util;

import java.util.ArrayList;


public class Booking3ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingServices3> artistsList;
    private boolean showLoader;
    private String serviceTitle;
    private ArtistsSearchBoard item;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    private  boolean isOutCallSelect;
    private SubServices subServices;
    private Util utility;
    // Constructor of the class
    public Booking3ServiceAdapter(Context context, ArrayList<BookingServices3> artistsList, String serviceTitle,ArtistsSearchBoard item,boolean isOutCallSelect,SubServices subServices) {
        this.context = context;
        this.artistsList = artistsList;
        this.serviceTitle = serviceTitle;
        this.item = item;
        this.isOutCallSelect = isOutCallSelect;
        this.subServices = subServices;
        utility = new Util(context);
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
        double prise = 0.0;
        if (isOutCallSelect){
            prise = Double.parseDouble(item.outCallPrice);
        }else {
            prise = Double.parseDouble(item.inCallPrice);
        }
        holder.tvAmount.setText(""+prise);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvAmount,tvLastService,tvTime;
        LinearLayout lyServiceDetail;
        private ViewHolder(View itemView)
        {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            lyServiceDetail = itemView.findViewById(R.id.lyServiceDetail);
            tvLastService = itemView.findViewById(R.id.tvLastService);
            tvTime = itemView.findViewById(R.id.tvTime);

            lyServiceDetail.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.lyServiceDetail:
                    Session session = Mualab.getInstance().getSessionManager();
                    User user = session.getUser();
                    BookingServices3 services3 = artistsList.get(getAdapterPosition());

                    BookingInfo bookingInfo = new BookingInfo();
                    bookingInfo.artistService = services3.title;
                    bookingInfo.msId = services3._id;
                    bookingInfo.time = services3.completionTime;
                    bookingInfo.sServiceName = subServices.subServiceName;
                    bookingInfo.ssId = subServices.subServiceId;
                    bookingInfo.sId = subServices.serviceId;
                    bookingInfo.artistName = item.userName;
                    bookingInfo.profilePic = item.profileImage;
                    bookingInfo.artistId = item._id;
                    bookingInfo.artistAddress = item.address;
                    bookingInfo.userId = String.valueOf(user.id);

                    if (isOutCallSelect) {
                        bookingInfo.preperationTime = item.outCallpreprationTime;
                        bookingInfo.serviceType = "2";
                        bookingInfo.price = Double.parseDouble(services3.outCallPrice);
                    }else {
                        bookingInfo.price = Double.parseDouble(services3.inCallPrice);
                        bookingInfo.preperationTime = item.inCallpreprationTime;
                        bookingInfo.serviceType = "1";
                    }

                    int ctMinuts = 0,ptMinuts;

                    if (services3.completionTime.contains(":")){
                        String hours,min;
                        String[] separated = services3.completionTime.split(":");
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

                    }


                    if (((BookingActivity)context).item.businessType.equals("independent")){
                        ((BookingActivity)context).addFragment(
                                BookingFragment4.newInstance(serviceTitle,item._id,bookingInfo), true, R.id.flBookingContainer);

                    }else {
                        ((BookingActivity)context).addFragment(
                                BookingFragment1.newInstance(serviceTitle,item, bookingInfo), true, R.id.flBookingContainer);

                    }

                    break;

            }
        }
    }

}