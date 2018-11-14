package com.mualab.org.user.activity.make_booking.adapter;


import android.app.Dialog;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.daimajia.swipe.SwipeLayout;
import com.loopeer.shadow.ShadowView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.make_booking.BookingActivity;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment1;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment4;
import com.mualab.org.user.activity.make_booking.fragment.BookingFragment5;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.booking.StaffInfo;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.model.booking.BookingInfo;
import com.mualab.org.user.data.model.booking.BookingServices3;
import com.mualab.org.user.data.model.booking.SubServices;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Booking3ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingServices3> artistsList;
    private boolean isOutCallSelect,fromConfirmBooking,isClicked = false;
    private String serviceTitle,bookingId="";
    private ArtistsSearchBoard item;
    private SubServices subServices;
    private Util utility;
    private   long mLastClickTime = 0;

    // Constructor of the class
    public Booking3ServiceAdapter(Context context, ArrayList<BookingServices3> artistsList,
                                  ArtistsSearchBoard item,SubServices subServices,boolean fromConfirmBooking, BookingInfo info) {
        this.context = context;
        this.artistsList = artistsList;
        this.item = item;
        this.subServices = subServices;
        this.fromConfirmBooking = fromConfirmBooking;
        utility = new Util(context);
        this.serviceTitle = subServices.subServiceName;
        this.isOutCallSelect = info.isOutCallSelect;
        this.bookingId = info.bookingId;
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking3_last_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final BookingServices3 item = artistsList.get(position);
        holder.tvLastService.setText(item.title);

        String totalTime = item.completionTime;
        if (totalTime.contains(":")){
            String[] separated = totalTime.split(":");
            String hours = separated[0]+" hrs ";
            String min = separated[1]+" min";

            if (hours.equals("00 hrs "))
                holder.tvTime.setText(min);
            else if (!hours.equals("00 hrs ") && min.equals("00 min"))
                holder.tvTime.setText(hours);
            else
                holder.tvTime.setText(hours+min);

        }
        double prise = 0.0;
        if (isOutCallSelect){
            prise = Double.parseDouble(item.outCallPrice);
        }else {
            prise = Double.parseDouble(item.inCallPrice);
        }
        holder.tvAmount.setText(""+String.format("%.2f", prise));

        if (fromConfirmBooking){
            if (item.isBooked()) {
                holder.lyFrontView.setShadowColor(context.getResources().getColor(R.color.shadow_green));
                holder.sample1.setSwipeEnabled(true);
            }
            else {
                holder.sample1.setSwipeEnabled(false);
                holder.lyFrontView.setShadowColor(context.getResources().getColor(R.color.gray2));
            }
        }else {
            holder.sample1.setSwipeEnabled(false);
            holder.lyFrontView.setShadowColor(context.getResources().getColor(R.color.gray2));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvAmount,tvLastService,tvTime;
        LinearLayout lyServiceDetail,lyRemove;
        SwipeLayout sample1;
        ShadowView lyFrontView;
        private ViewHolder(View itemView)
        {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            lyServiceDetail = itemView.findViewById(R.id.lyServiceDetail);
            lyRemove = itemView.findViewById(R.id.lyRemove);
            tvLastService = itemView.findViewById(R.id.tvLastService);
            tvTime = itemView.findViewById(R.id.tvTime);
            sample1 = itemView.findViewById(R.id.sample1);
            lyFrontView = itemView.findViewById(R.id.lyFrontView);
            lyFrontView.setShadowDy(context.getResources().getDimension(R.dimen.shadow_width));
            lyFrontView.setShadowDx(context.getResources().getDimension(R.dimen.shadow_width));

            lyServiceDetail.setOnClickListener(this);
            lyRemove.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (view.getId()){
                case R.id.lyRemove:
                    if (fromConfirmBooking){
                        BookingServices3 mServices3 = artistsList.get(getAdapterPosition());
                        mServices3.setBooked(false);
                        notifyDataSetChanged();

                        for (BookingInfo bookingInfo : BookingFragment4.arrayListbookingInfo) {
                            if (mServices3._id.equals(bookingInfo.msId)){
                                bookingId = bookingInfo.bookingId;
                                BookingFragment4.arrayListbookingInfo.remove(bookingInfo);
                                break;
                            }
                        }

                        apiForDeleteBookedService();
                    }
                    break;

                case R.id.lyServiceDetail:
                    Session session = Mualab.getInstance().getSessionManager();
                    User user = session.getUser();
                    BookingServices3 services3 = artistsList.get(getAdapterPosition());
                    services3.setBooked(true);

                    BookingInfo bookingInfo = new BookingInfo();
                    //add data from artist main services
                    bookingInfo.artistService = services3.title;
                    bookingInfo.msId = services3._id;
                    bookingInfo.time = services3.completionTime;

                    //data from subservices
                    bookingInfo.sServiceName = subServices.subServiceName;
                    bookingInfo.ssId = subServices.subServiceId;
                    bookingInfo.sId = subServices.serviceId;
                    bookingInfo.subServices = subServices;
                    bookingInfo.isOutCallSelect = isOutCallSelect;
                    bookingInfo.bookingId = bookingId;
                    //       subServices.bookedArtistServices.addAll(artistsList);

                    //add data from services
                    bookingInfo.artistName = item.userName;
                    bookingInfo.profilePic = item.profileImage;
                    bookingInfo.artistId = item._id;
                    bookingInfo.artistAddress = item.address;
                    bookingInfo.item = item;
                    bookingInfo.userId = String.valueOf(user.id);
                    //new in multiple staff
                    bookingInfo.outCallPrice = services3.outCallPrice;
                    bookingInfo.inCallPrice = services3.inCallPrice;
                    bookingInfo.completionTime = services3.completionTime;

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
                        bookingInfo.editEndTime = ""+(ptMinuts+ctMinuts);

                    }

                    if (item.businessType.equals("independent")){
                        if (fromConfirmBooking){
                            ((BookingActivity)context).addFragment(BookingFragment4.newInstance(subServices.subServiceName,true,bookingInfo), true, R.id.flBookingContainer);
                        }else {
                            ((BookingActivity)context).addFragment(
                                    BookingFragment4.newInstance(subServices.subServiceName,false,bookingInfo), true, R.id.flBookingContainer);
                        }


                    }else {
                        ((BookingActivity) context).addFragment(
                                BookingFragment1.newInstance(serviceTitle, item, bookingInfo, fromConfirmBooking), true, R.id.flBookingContainer);
                    }
                    break;

            }
        }
    }

    private void apiForDeleteBookedService(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(context, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForDeleteBookedService();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        if (!bookingId.equals(""))
            params.put("bookingId", bookingId);
        // params.put("artistId", item._id);
        // params.put("userId", String.valueOf(user.id));

        HttpTask task = new HttpTask(new HttpTask.Builder(context, "deleteBookService", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        if (BookingFragment4.arrayListbookingInfo.size()==0){
                            BookingFragment4.arrayListbookingInfo.clear();
                            ((BookingActivity)context).finish();
                        }else {
                            FragmentManager fm = ((BookingActivity)context).getSupportFragmentManager();
                            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                            BookingInfo bookingInfo = BookingFragment4.arrayListbookingInfo.get(0);
                            ((BookingActivity)context).addFragment(
                                    BookingFragment5.newInstance(bookingInfo), true, R.id.flBookingContainer);
                        }


                    }else {
                        MyToast.getInstance(context).showDasuAlert(message);
                    }
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
                        //      MyToast.getInstance(BookingActivity.this).showSmallCustomToast(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

}