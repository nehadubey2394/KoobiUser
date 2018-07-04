package com.mualab.org.user.activity.artist_profile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.listner.OnServiceClickListener;
import com.mualab.org.user.activity.artist_profile.model.ArtistCategory;

import java.util.List;


public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ViewHolder> {
    private Context context;
    private String s = "";
    private List<ArtistCategory> itemList;
    private OnServiceClickListener customButtonListener = null;

    // Constructor of the class
    public ServicesAdapter(Context context, List<ArtistCategory> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setCustomListener(OnServiceClickListener customButtonListener){
        this.customButtonListener = customButtonListener;
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }


    // specify the row layout file and click for each row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ViewHolder(view);
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {
        ArtistCategory service = itemList.get(listPosition);
        if (service.isSelect)
            holder.rlService.setBackground(context.getResources().getDrawable(R.drawable.bg_green_background));
        else
            holder.rlService.setBackground(context.getResources().getDrawable(R.drawable.bg_pink_cylender));

        holder.tvService.setText(service.serviceName);

    }

    // Static inner class to initialize the views of rows
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvService;
        RelativeLayout rlService;

        ViewHolder(View itemView) {
            super(itemView);
            tvService =  itemView.findViewById(R.id.tvService);
            rlService = itemView.findViewById(R.id.rlService);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (customButtonListener != null) {
                customButtonListener.onServiceClick(getAdapterPosition());
            }
        }
    }

}
