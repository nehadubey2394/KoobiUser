package com.mualab.org.user.activity.booking_histories.adapter;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.activity.BookingDetailActivity;
import com.mualab.org.user.activity.booking_histories.model.BookingHistory;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.payment.activity.PaymentActivity;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class FutureBookingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;
    private List<BookingHistory> futureBookings;
    private boolean showLoader;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;

    // Constructor of the class
    public FutureBookingAdapter(Activity context, List<BookingHistory> futureBookings) {
        this.context = context;
        this.futureBookings = futureBookings;
    }

    @Override
    public int getItemCount() {
        return futureBookings.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==futureBookings.size()-1){
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_future_booking, parent, false);
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
        final BookingHistory item = futureBookings.get(position);

        holder.tvtName.setText(item.userName);
        holder.tvDate.setText(item.bookingDate);
        holder.tvService.setText(item.artistService.get(0));
        holder.tvPrice.setText("£"+item.totalPrice);
        holder.tvDate.setText(changeDateFormate(item.bookingDate));

        if (item.bookStatus.equals("0")){
            holder.btnWaiting.setVisibility(View.VISIBLE);
            holder.btnWaiting.setText(context.getString(R.string.a_waiting_confirmation));
        }else if (item.bookStatus.equals("3") && item.paymentStatus.equals("0")){
            holder.btnWaiting.setVisibility(View.VISIBLE);
            holder.btnWaiting.setText(R.string.pay);
        }else {
            holder.btnWaiting.setVisibility(View.GONE);
        }

        if (item.paymentType.equals("3")){
            holder.btnWaiting.setVisibility(View.GONE);
        }/*else {
            holder.btnWaiting.setText(R.string.pay);
            holder.btnWaiting.setVisibility(View.VISIBLE);
        }*/

        if (!item.profileImage.equals("")){
            Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvtName,tvDate,tvService,tvPrice;
        View lineView;
        ImageView ivProfile,ivChat;
        AppCompatButton btnWaiting;
        RelativeLayout rlContainer;
        private ViewHolder(View itemView)
        {
            super(itemView);
            lineView = itemView.findViewById(R.id.lineView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivChat = itemView.findViewById(R.id.ivChat);
            tvtName = itemView.findViewById(R.id.tvtName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvService = itemView.findViewById(R.id.tvService);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnWaiting = itemView.findViewById(R.id.btnWaiting);
            rlContainer = itemView.findViewById(R.id.rlContainer);

            rlContainer.setOnClickListener(this);
            btnWaiting.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnWaiting:
                    final BookingHistory item = futureBookings.get(getAdapterPosition());
                    if (item.bookStatus.equals("3") && (item.paymentStatus.equals("0") && item.paymentType.equals("2"))){
                       /* Intent intent = new Intent(new Intent(context,PaymentActivity.class));
                        intent.putExtra("itemDetail",item);*/
                        Intent intent = new Intent(context, PaymentActivity.class);
                        intent.putExtra("bookingId",  String.valueOf(item._id));
                        intent.putExtra("totalPrice",  item.totalPrice);
                        context.startActivityForResult(intent, 30);
                    }/*else if (item.bookStatus.equals("3") && item.paymentStatus.equals("0")){
                        apiForPaymentByCash();
                    }*/
                    break;

                case  R.id.rlContainer:
                    final BookingHistory item2 = futureBookings.get(getAdapterPosition());
                    Intent intent = new Intent(context, BookingDetailActivity.class);
                    intent.putExtra("bookingId",  String.valueOf(item2._id));
                    intent.putExtra("artistName",  item2.userName);
                    intent.putExtra("artistProfile",  item2.profileImage);
                    context.startActivityForResult(intent, 2);
                    // MyToast.getInstance(context).showDasuAlert("Under developement");
                    break;
            }

        }
    }
/*
    private void apiForPaymentByCash(){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(context, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForPaymentByCash();
                    }
                }
            }).show();
        }

        String url = Uri.parse("cardPayment")
                .buildUpon()
                .appendQueryParameter("number", "")
                .appendQueryParameter("cvv", "")
                .appendQueryParameter("exp_month", "")
                .appendQueryParameter("exp_year", "")
                .appendQueryParameter("id", "")
                .build().toString();

        String sParam = "number="+number+"cvv"+sCvv+"exp_month"+expireMnth+"exp_year"+expireYear+"id"+bookingId;

        HttpTask task = new HttpTask(new HttpTask.Builder(context, url, new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        MyToast.getInstance(context).showDasuAlert(message);
                        Intent intent = new Intent();
                        intent.putExtra("isChanged", "true");
                        ((PaymentActivity)context).setResult(RESULT_OK, intent);
                        ((PaymentActivity)context).finish();
                    }else {
                        MyToast.getInstance(context).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(context);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setMethod(Request.Method.GET)
                */
    /*.setBody(params, HttpTask.ContentType.APPLICATION_JSON)*//*
);

        task.execute(this.getClass().getName());
    }
*/

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