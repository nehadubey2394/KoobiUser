package com.mualab.org.user.activity.make_booking.adapter;


import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.make_booking.BookingActivity;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment3;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.booking.BookingInfo;

import java.util.ArrayList;


public class BookedServicesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private AppCompatActivity context;
    private ArrayList<BookingInfo> artistsList;
    private ArtistsSearchBoard item;
    private  boolean fromConfirmBooking;
    // Constructor of the class
    public BookedServicesAdapter(AppCompatActivity context, ArrayList<BookingInfo> artistsList, ArtistsSearchBoard item) {
        this.context = context;
        this.artistsList = artistsList;
        this.item = item;
        this.fromConfirmBooking = fromConfirmBooking;
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
                    BookingInfo info = artistsList.get(getAdapterPosition());
                    FragmentManager fm = context.getSupportFragmentManager();
                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    info.bookStaffId = info.staffId;
                  /*  ((BookingActivity)context).addFragment(
                            BookingFragment3.newInstance(true,info.subServices,item,
                                    info.isOutCallSelect,info.bookingId,info.staffId), true, R.id.flBookingContainer);
                }
            });*/
                    ((BookingActivity)context).addFragment(
                            BookingFragment3.newInstance(true,info.subServices,item,info), true, R.id.flBookingContainer);
                }
            });
        }
    }

}