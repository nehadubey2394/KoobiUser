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


       /* Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();
        if (user.id.equals(item.staffId)) {
            holder.tvStaffName.setText("My booking");
        }
        if (user.businessType.equals("independent")) {
            holder.llChangeStaff.setVisibility(View.GONE);
            holder.tvPrice2.setVisibility(View.VISIBLE);
        }
        else {
            holder.llChangeStaff.setVisibility(View.VISIBLE);
            holder.tvPrice2.setVisibility(View.GONE);
        }*/
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvServiceName,tvAssignrdStaff,tvStaffName,tvPrice2,tvPrice;
        private ViewHolder(View itemView)
        {
            super(itemView);

            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvAssignrdStaff = itemView.findViewById(R.id.tvAssignrdStaff);
            tvStaffName = itemView.findViewById(R.id.tvStaffName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvPrice2 = itemView.findViewById(R.id.tvPrice2);
        }

        @Override
        public void onClick(View view) {

            switch (view.getId()){
              /*  case R.id.btnChangeStaff:
                    BookingInfo bookingInfo = artistsList.get(getAdapterPosition());
                    if (listener != null) {
                        listener.onStaffSelect(getAdapterPosition(),bookingInfo);
                    }
                    break;*/
            }
        }

    }

}