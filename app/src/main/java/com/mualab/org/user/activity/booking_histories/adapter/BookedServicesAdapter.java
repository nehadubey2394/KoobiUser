package com.mualab.org.user.activity.booking_histories.adapter;


import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.model.BookingInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class BookedServicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<BookingInfo> artistsList;

    // Constructor of the class
    public BookedServicesAdapter(Context context, List<BookingInfo> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_services, parent, false);
        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final BookingInfo item = artistsList.get(position);

        double price = Double.parseDouble(item.bookingPrice);

        holder.tvPrice.setText("£"+ String.format("%.2f", price));
        holder.tvPrice2.setText("£"+ String.format("%.2f", price));

        holder.tvStaffName.setText(item.staffName);
        holder.tvServiceName.setText(item.artistServiceName);
        holder.tvTime.setText(item.startTime);
        holder.tvDate.setText(changeDateFormate(item.bookingDate));

    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvServiceName,tvAssignrdStaff,tvStaffName,tvPrice2,tvPrice,tvDate,tvTime;
        private ViewHolder(View itemView)
        {
            super(itemView);

            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvAssignrdStaff = itemView.findViewById(R.id.tvAssignrdStaff);
            tvStaffName = itemView.findViewById(R.id.tvStaffName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPrice2 = itemView.findViewById(R.id.tvPrice2);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

    }

    private String changeDateFormate(String sDate){
        SimpleDateFormat inputDf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputtDf = new SimpleDateFormat("dd/MM/yyyy");
        Date formatedDate = null;
        String date = "";
        try {
            formatedDate = inputDf.parse(sDate);
            date =  outputtDf.format(formatedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


}