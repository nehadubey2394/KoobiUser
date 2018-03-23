package com.mualab.org.user.activity.booking.adapter;


import android.app.Dialog;
import android.content.Context;
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
import com.facebook.internal.Utility;
import com.loopeer.shadow.ShadowView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.booking.fragment.BookingFragment1;
import com.mualab.org.user.activity.booking.fragment.BookingFragment4;
import com.mualab.org.user.activity.booking.fragment.BookingFragment5;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.booking.BookingInfo;
import com.mualab.org.user.model.booking.BookingServices3;
import com.mualab.org.user.model.booking.SubServices;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.Helper;
import com.mualab.org.user.util.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Booking3ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<BookingServices3> artistsList;
    private boolean isOutCallSelect,fromConfirmBooking;
    private String serviceTitle,bookingId="";
    private ArtistsSearchBoard item;
    private SubServices subServices;
    private Util utility;
    // Constructor of the class
    public Booking3ServiceAdapter(Context context, ArrayList<BookingServices3> artistsList, ArtistsSearchBoard item,boolean isOutCallSelect,SubServices subServices,boolean fromConfirmBooking) {
        this.context = context;
        this.artistsList = artistsList;
        this.item = item;
        this.isOutCallSelect = isOutCallSelect;
        this.subServices = subServices;
        this.fromConfirmBooking = fromConfirmBooking;
        utility = new Util(context);
        this.serviceTitle = subServices.subServiceName;
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

        if (fromConfirmBooking){
            if (item.isBooked())
                holder.lyFrontView.setShadowColor(context.getResources().getColor(R.color.shadow_green));
            else {
                holder.sample1.setSwipeEnabled(false);
                holder.lyFrontView.setShadowColor(context.getResources().getColor(R.color.gray2));
            }
            holder.sample1.setSwipeEnabled(true);
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
            switch (view.getId()){
                case R.id.lyRemove:
                    if (fromConfirmBooking){
                        BookingServices3 mServices3 = artistsList.get(getAdapterPosition());
                        mServices3.setBooked(false);
                        notifyDataSetChanged();
                        for (int i=0;i<BookingFragment4.arrayListbookingInfo.size();i++) {
                            BookingInfo bookingInfo = BookingFragment4.arrayListbookingInfo.get(i);
                            for (int j=0;j<artistsList.size();j++) {
                                BookingServices3 bookingServices = artistsList.get(j);
                                if (bookingServices._id.equalsIgnoreCase(bookingInfo.msId)){
                                    bookingId = bookingInfo.bookingId;
                                    BookingFragment4.arrayListbookingInfo.remove(i);
                                    break;
                                }
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
                    bookingInfo.artistsList.addAll(artistsList);

                    //data from subservices
                    bookingInfo.sServiceName = subServices.subServiceName;
                    bookingInfo.ssId = subServices.subServiceId;
                    bookingInfo.sId = subServices.serviceId;
                    bookingInfo.subServices = subServices;
                    bookingInfo.isOutCallSelect = isOutCallSelect;

                    //add data from services
                    bookingInfo.artistName = item.userName;
                    bookingInfo.profilePic = item.profileImage;
                    bookingInfo.artistId = item._id;
                    bookingInfo.artistAddress = item.address;
                    bookingInfo.item = item;
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
                        //  if (fromConfirmBooking)
                        //  bookingInfo.editEndTime = ""+(ptMinuts+ctMinuts);
                    }


                    if (((BookingActivity)context).item.businessType.equals("independent")){
                        if (fromConfirmBooking){
                            ((BookingActivity)context).addFragment(
                                    BookingFragment4.newInstance(subServices.subServiceName,true,bookingInfo), true, R.id.flBookingContainer);
                        }else {
                            ((BookingActivity)context).addFragment(
                                    BookingFragment4.newInstance(subServices.subServiceName,false,bookingInfo), true, R.id.flBookingContainer);
                        }


                    }else {
                        ((BookingActivity)context).addFragment(
                                BookingFragment1.newInstance(serviceTitle,item, bookingInfo,fromConfirmBooking), true, R.id.flBookingContainer);

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
                            int count = fm.getBackStackEntryCount();
                            for (int i = 0; i < count; ++i) {
                                if (i > 0)
                                    fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }

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