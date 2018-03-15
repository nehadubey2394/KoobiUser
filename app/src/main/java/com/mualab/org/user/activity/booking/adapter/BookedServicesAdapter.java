package com.mualab.org.user.activity.booking.adapter;


import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.booking.BookingInfo;

import java.util.ArrayList;


public class BookedServicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingInfo> artistsList;

    // Constructor of the class
    public BookedServicesAdapter(Context context, ArrayList<BookingInfo> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_for_booking5, parent, false);
        return new ViewHolder(view);

    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {


        final ViewHolder holder = ((ViewHolder) viewHolder);
        final BookingInfo item = artistsList.get(position);

        holder.tvSsName.setText(item.sServiceName);
        holder.tvAsName.setText(item.artistService);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAsName,tvSsName;
        AppCompatButton btnEditService;
        private ViewHolder(View itemView)
        {
            super(itemView);

            btnEditService = itemView.findViewById(R.id.btnEditService);
            tvAsName = itemView.findViewById(R.id.tvAsName);
            tvSsName = itemView.findViewById(R.id.tvSsName);

            btnEditService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MyToast.getInstance(context).showSmallCustomToast("Under Developement");
                }
            });
        }
    }

}