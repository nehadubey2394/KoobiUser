package com.mualab.org.user.activity.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.ChatActivity;
import com.mualab.org.user.activity.chat.model.Chat;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int VIEW_TYPE_SINGLE  = 1;
    private int VIEW_TYPE_GROUP = 2;

    private Context context;
    private List<ChatHistory> chatHistories;

    public ChatHistoryAdapter(Context context, List<ChatHistory> chatHistories) {
        this.context = context;
        this.chatHistories = chatHistories;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == VIEW_TYPE_SINGLE){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history,parent,false);
            return new SingleChatViewHolder(view);
        }else if(viewType == VIEW_TYPE_GROUP) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat_history,parent,false);
            return new GroupChatViewHolder(view);
        }

        return  new SingleChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatHistory chatHistory = chatHistories.get(position);
        if(chatHistory.type.equals("user")){
            ((SingleChatViewHolder)holder).myBindData(chatHistory);
        }else {
            ((GroupChatViewHolder)holder).otherBindData(chatHistory);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chatHistories.get(position).type.equals("user")) {
            return VIEW_TYPE_SINGLE;
        } else {
            return VIEW_TYPE_GROUP;
        }
    }

    @Override
    public int getItemCount() {
        return chatHistories.size();
    }

    public class SingleChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUname,tvMsg,tvChatType,tvUnReadCount,tvTime;
        CircleImageView ivProfile;

        SingleChatViewHolder(View itemView) {
            super(itemView);
            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatType = itemView.findViewById(R.id.tvChatType);
            tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount);
            tvTime = itemView.findViewById(R.id.tvTime);

        }

        void myBindData(final ChatHistory chat){
           /* if(chat.messageType == 1){
                ly_my_image_view.setVisibility(View.VISIBLE);
                tv_sender_msg.setVisibility(View.GONE);
               // Glide.with(context).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.placeholder_chat_image)).into(iv_for_sender);
            }else {*/
            tvUname.setText(chat.userName);
            tvMsg.setText(chat.message);
            // }

            if (chat.profilePic !=null && !chat.profilePic.isEmpty()) {
                Picasso.with(context).load(chat.profilePic).placeholder(R.drawable.defoult_user_img).
                        fit().into(ivProfile);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tvTime.setText(date);

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onClick(View v) {
            ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
            Intent chat_intent = new Intent(context, ChatActivity.class);
            chat_intent.putExtra("userId",chatHistory.reciverId);
            context.startActivity(chat_intent);
        }
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder{
        TextView tvUname,tvMsg,tvChatType,tvUnReadCount,tvTime;
        CircleImageView ivProfile;

        GroupChatViewHolder(View itemView) {
            super(itemView);
            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatType = itemView.findViewById(R.id.tvChatType);
            tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void otherBindData(final ChatHistory chat){

          /*  if(chat.image == 1){
                ly_other_image_view.setVisibility(View.VISIBLE);
                other_message.setVisibility(View.GONE);
                Glide.with(context).load(chat.imageUrl).apply(new RequestOptions().
                placeholder(R.drawable.placeholder_chat_image)).into(iv_other_img);
            }else {*/
            tvUname.setText(chat.message);
            tvMsg.setText(chat.message);
            // }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tvTime.setText(date);

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
