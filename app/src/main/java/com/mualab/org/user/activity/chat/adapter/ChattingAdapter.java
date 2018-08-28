package com.mualab.org.user.activity.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.model.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChattingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int VIEW_TYPE_ME  = 1;
    private int VIEW_TYPE_OTHER = 2;

    private Context context;
    private List<Chat> chatList;
    private String myUid ;

    public ChattingAdapter(Context context, List<Chat> chatList, String myId) {
        this.context = context;
        this.chatList = chatList;
        this.myUid = myId;
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
        Chat chat = chatList.get(position);

        if(TextUtils.equals(chat.senderId,myUid)){
            ((MyViewHolder)holder).myBindData(chat);
        }else {
            ((OtherViewHolder)holder).otherBindData(chat);
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
        TextView tv_sender_msg,tv_send_time;
        ImageView iv_for_sender,iv_msg_status;

        MyViewHolder(View itemView) {
            super(itemView);
            tv_sender_msg = itemView.findViewById(R.id.tv_sender_msg);
            iv_msg_status = itemView.findViewById(R.id.iv_msg_status);
            iv_for_sender = itemView.findViewById(R.id.iv_for_sender);
            tv_send_time = itemView.findViewById(R.id.tv_send_time);

        }

        void myBindData(final Chat chat){
            if(chat.messageType == 1){
                iv_for_sender.setVisibility(View.VISIBLE);
                tv_sender_msg.setVisibility(View.GONE);
                Glide.with(context).load(chat.message).fitCenter().
                        placeholder(R.drawable.gallery_placeholder).into(iv_for_sender);
            }else {
                iv_for_sender.setVisibility(View.GONE);
                tv_sender_msg.setVisibility(View.VISIBLE);
                tv_sender_msg.setText(chat.message);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tv_send_time.setText(date);

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public class OtherViewHolder extends RecyclerView.ViewHolder{
        TextView tv_other_msg,tv_other_name,tv_other_msg_time;
        ImageView iv_other_img,iv_othr_msg_status;

        OtherViewHolder(View itemView) {
            super(itemView);
            tv_other_msg = itemView.findViewById(R.id.tv_other_msg);
            tv_other_name = itemView.findViewById(R.id.tv_other_name);
            tv_other_msg_time = itemView.findViewById(R.id.tv_other_msg_time);
            iv_other_img = itemView.findViewById(R.id.iv_other_img);
            iv_othr_msg_status = itemView.findViewById(R.id.iv_othr_msg_status);
        }

        void otherBindData(final Chat chat){
            tv_other_name.setVisibility(View.GONE);
            if(chat.messageType == 1){
                iv_other_img.setVisibility(View.VISIBLE);
                tv_other_msg.setVisibility(View.GONE);

                Glide.with(context).load(chat.message).fitCenter().
                        placeholder(R.drawable.gallery_placeholder).into(iv_other_img);

            }else {
                tv_other_msg.setVisibility(View.VISIBLE);
                iv_other_img.setVisibility(View.GONE);
                tv_other_msg.setText(chat.message);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tv_other_msg_time.setText(date);

            }catch (Exception ignored){

            }
        }
    }

    /*public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }*/
}
