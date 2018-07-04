package com.mualab.org.user.activity.payment.adapter;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.activity.BookingDetailActivity;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.payment.modle.PaymentHistory;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class PaymentHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;
    private List<PaymentHistory> paymentHistories;
    private boolean showLoader;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;

    // Constructor of the class
    public PaymentHistoryAdapter(Activity context, List<PaymentHistory> paymentHistories) {
        this.context = context;
        this.paymentHistories = paymentHistories;
    }

    @Override
    public int getItemCount() {
        return paymentHistories.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==paymentHistories.size()-1){
            return showLoader?VIEWTYPE_LOADER:VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_staff_item_layout, parent, false);
        // return new ViewHolder(view);
        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment_history, parent, false);
                return new ViewHolder(view);

            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
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
        final PaymentHistory item = paymentHistories.get(position);

        holder.tvUserName.setText(item.artistName);
        holder.tvServices.setText(item.artistService.get(0));
        holder.tvPrice.setText("£"+item.totalPrice);
        holder.tvDate.setText(changeDateFormate(item.bookingDate));

        if (item.paymentStatus.equals("1")){
            holder.tvPaymentStatus.setText("Paid");
            holder.tvPaymentStatus.setTextColor(context.getResources().getColor(R.color.primary_green));
        }else {
            holder.tvPaymentStatus.setTextColor(context.getResources().getColor(R.color.red));
            holder.tvPaymentStatus.setText("Pending");
        }

        if (!item.artistProfileImage.equals("")){
            Picasso.with(context).load(item.artistProfileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvUserName,tvServices,tvPrice,tvPaymentStatus,tvDate;
        ImageView ivProfile;
        RelativeLayout rlContainer;
        private ViewHolder(View itemView)
        {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfilePic);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvServices = itemView.findViewById(R.id.tvServices);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            rlContainer = itemView.findViewById(R.id.rlContainer);

            rlContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case  R.id.rlContainer:
                    final PaymentHistory item = paymentHistories.get(getAdapterPosition());
                    Intent intent = new Intent(context, BookingDetailActivity.class);
                    intent.putExtra("bookingId",  String.valueOf(item._id));
                    intent.putExtra("artistName",  item.artistName);
                    intent.putExtra("artistProfile",  item.artistProfileImage);
                    context.startActivityForResult(intent, 20);
                    break;
            }

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