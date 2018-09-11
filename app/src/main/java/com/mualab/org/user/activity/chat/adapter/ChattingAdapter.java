package com.mualab.org.user.activity.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.listner.DateTimeScrollListner;
import com.mualab.org.user.activity.chat.model.Chat;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int VIEW_TYPE_ME  = 1;
    private int VIEW_TYPE_OTHER = 2;

    private Context context;
    private List<Chat> chatList;
    private String myUid ;
    private DateTimeScrollListner listener;
    private String changedDate = "",dateText="";

    public ChattingAdapter(Context context, List<Chat> chatList, String myId,DateTimeScrollListner listener) {
        this.context = context;
        this.chatList = chatList;
        this.myUid = myId;
        this.listener=listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == VIEW_TYPE_ME){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_right_side_view,parent,false);
            return new MyViewHolder(view);
        }else if(viewType == VIEW_TYPE_OTHER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_left_side_view,parent,false);
            return new OtherViewHolder(view);
        }

        return  new OtherViewHolder(null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int pos = position - 1;
        int tempPos = (pos == -1) ? pos + 1 : pos;

        Chat chat = chatList.get(position);

        if(TextUtils.equals(chat.senderId,myUid)){
            ((MyViewHolder)holder).myBindData(chat,position,tempPos);
        }else {
            ((OtherViewHolder)holder).otherBindData(chat,position,tempPos);
        }

    }

    @Override
    public int getItemViewType(int position) {

        if (TextUtils.equals(chatList.get(position).senderId,myUid )) {
            return VIEW_TYPE_ME;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv_sender_msg,tv_send_time,tv_my_date_label;
        ImageView iv_for_sender,iv_msg_status;
        ProgressBar progress_bar;

        MyViewHolder(View itemView) {
            super(itemView);
            tv_sender_msg = itemView.findViewById(R.id.tv_sender_msg);
            iv_msg_status = itemView.findViewById(R.id.iv_msg_status);
            iv_for_sender = itemView.findViewById(R.id.iv_for_sender);
            tv_send_time = itemView.findViewById(R.id.tv_send_time);
            progress_bar = itemView.findViewById(R.id.progress_bar);
            tv_my_date_label = itemView.findViewById(R.id.tv_my_date_label);

        }

        void myBindData(final Chat chat, int position,int tempPos){

            if(chat.messageType == 1){
                iv_for_sender.setVisibility(View.VISIBLE);
                tv_sender_msg.setVisibility(View.GONE);
                progress_bar.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(chat.message).fit().into(iv_for_sender, new Callback() {
                    @Override
                    public void onSuccess() {
                        progress_bar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(chat.message)
                                .fit().placeholder(R.drawable.gallery_placeholder)
                                .error(R.drawable.gallery_placeholder).into(iv_for_sender);
                        progress_bar.setVisibility(View.GONE);
                    }
                });

                //  Glide.with(context).load(chat.message).fitCenter().placeholder(R.drawable.gallery_placeholder).into(iv_for_sender);
            }else {
                iv_for_sender.setVisibility(View.GONE);
                tv_sender_msg.setVisibility(View.VISIBLE);
                tv_sender_msg.setText(chat.message);
                progress_bar.setVisibility(View.GONE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tv_send_time.setText(date);

                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    tv_my_date_label.setVisibility(View.GONE);
                }

                /*String newDate = getDateBanner((Long) chat.timestamp);*/

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (listener!=null)
                listener.onScrollChange(position,chat.timestamp);

        }
    }

    public class OtherViewHolder extends RecyclerView.ViewHolder{
        TextView tv_other_msg,tv_other_name,tv_other_msg_time,tv_my_date_label;
        ImageView iv_other_img,iv_othr_msg_status;
        ProgressBar progress_bar;

        OtherViewHolder(View itemView) {
            super(itemView);
            tv_other_msg = itemView.findViewById(R.id.tv_other_msg);
            tv_other_name = itemView.findViewById(R.id.tv_other_name);
            tv_other_msg_time = itemView.findViewById(R.id.tv_other_msg_time);
            iv_other_img = itemView.findViewById(R.id.iv_other_img);
            iv_othr_msg_status = itemView.findViewById(R.id.iv_othr_msg_status);
            tv_my_date_label = itemView.findViewById(R.id.tv_my_date_label);
            progress_bar = itemView.findViewById(R.id.progress_bar);
        }

        void otherBindData(final Chat chat, int position,int tempPos){
            tv_other_name.setVisibility(View.GONE);

            if(chat.messageType == 1){
                progress_bar.setVisibility(View.VISIBLE);
                iv_other_img.setVisibility(View.VISIBLE);
                tv_other_msg.setVisibility(View.GONE);

                Picasso.with(context)
                        .load(chat.message).fit().into(iv_other_img, new Callback() {
                    @Override
                    public void onSuccess() {
                        progress_bar.setVisibility(View.GONE);
                    }
                    @Override
                    public void onError() {
                        Picasso.with(context).load(chat.message)
                                .fit().placeholder(R.drawable.gallery_placeholder)
                                .error(R.drawable.gallery_placeholder).into(iv_other_img);
                        progress_bar.setVisibility(View.GONE);
                    }
                });

                //  Glide.with(context).load(chat.message).fitCenter().placeholder(R.drawable.gallery_placeholder).into(iv_other_img);

            }else {
                tv_other_msg.setVisibility(View.VISIBLE);
                iv_other_img.setVisibility(View.GONE);
                tv_other_msg.setText(chat.message);
                progress_bar.setVisibility(View.GONE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tv_other_msg_time.setText(date);
                //   getDateBanner((Long) chat.timestamp,tv_my_date_label);
                /* String newDate = getDateBanner((Long) chat.timestamp);*/

                if (!chat.banner_date.equals(chatList.get(tempPos).banner_date)) {
                    tv_my_date_label.setText(chat.banner_date);
                    tv_my_date_label.setVisibility(View.VISIBLE);
                } else {
                    tv_my_date_label.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            if (listener!=null)
                listener.onScrollChange(position,chat.timestamp);

        }
    }

  /*  private String getDateBanner(long timestamp){
        SimpleDateFormat df = new  SimpleDateFormat("dd MMMM yyyy",Locale.getDefault());
        String newDate = df.format(timestamp);

        Date c = Calendar.getInstance().getTime();
        String currentDate = df.format(c);
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DATE, -1);
        java.sql.Date yesterday = new java.sql.Date(cal.getTimeInMillis());

        String beforeOneDay = formateDateFromstring("yyyy-MM-dd", "dd MMMM yyyy",yesterday.toString());

      *//*  if (changedDate.equals(checkDate)){
            changedDate = checkDate;
            tv_days_status.setVisibility(View.GONE);
        }else {
            changedDate = checkDate;
            tv_days_status.setVisibility(View.VISIBLE);
        }*//*

        if(currentDate.equals(newDate)){
            dateText = "Today";
        }else if(beforeOneDay.equals(newDate)){
            dateText = "Yesterday";
        }else
            dateText = newDate;

        return newDate;
    }

    public String formateDateFromstring(String inputFormat, String outputFormat, String inputDate){

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputDate;
    }*/

}
