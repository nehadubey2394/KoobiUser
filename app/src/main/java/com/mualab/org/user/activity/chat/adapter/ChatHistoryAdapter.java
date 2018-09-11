package com.mualab.org.user.activity.chat.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.ChatActivity;
import com.mualab.org.user.activity.chat.model.Chat;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.application.Mualab;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int VIEW_TYPE_SINGLE  = 1;
    private int VIEW_TYPE_GROUP = 2;

    private Context context;
    private List<ChatHistory> chatHistories;
    private boolean isTyping = false;
    private long mLastClickTime = 0;

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
        int pos = position - 1;
        int tempPos = (pos == -1) ? pos + 1 : pos;

        if(chatHistory.type.equals("user")){
            ((SingleChatViewHolder)holder).myBindData(chatHistory,position,tempPos);
        }else {
            ((GroupChatViewHolder)holder).otherBindData(chatHistory,position,tempPos);
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
        TextView tvUname,tvMsg,tvChatType,tvUnReadCount,tvTime,tvHistoryTime;
        CircleImageView ivProfile;
        RelativeLayout rlChatHistory,llHistoryDate;
        View vBottom,viewTop;

        SingleChatViewHolder(View itemView) {
            super(itemView);
            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatType = itemView.findViewById(R.id.tvChatType);
            tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount);
            rlChatHistory = itemView.findViewById(R.id.rlChatHistory);
            tvTime = itemView.findViewById(R.id.tvTime);
            llHistoryDate = itemView.findViewById(R.id.llHistoryDate);
            tvHistoryTime = itemView.findViewById(R.id.tvHistoryTime);
            vBottom = itemView.findViewById(R.id.vBottom);
            viewTop = itemView.findViewById(R.id.viewTop);

            rlChatHistory.setOnClickListener(this);

        }

        void myBindData(final ChatHistory chat,int position,int tempPos){
           /* if(chat.messageType == 1){
                ly_my_image_view.setVisibility(View.VISIBLE);
                tv_sender_msg.setVisibility(View.GONE);
               // Glide.with(context).load(chat.imageUrl).apply(new RequestOptions().placeholder(R.drawable.placeholder_chat_image)).into(iv_for_sender);
            }else {*/
            tvUname.setText(chat.userName);
            if (isTyping){
                tvMsg.setText("typing...");
                tvMsg.setTextColor(context.getResources().getColor(R.color.chatbox_blue));
            }else {
                if (chat.message.contains("https://firebasestorage.googleapis.com")| chat.messageType==1){
                    tvMsg.setText("Image");
                }else
                    tvMsg.setText(chat.message);
                tvMsg.setTextColor(context.getResources().getColor(R.color.grey));
            }

            if (chat.profilePic !=null && !chat.profilePic.isEmpty()) {
                Picasso.with(context).load(chat.profilePic).placeholder(R.drawable.defoult_user_img).
                        fit().into(ivProfile);
            }

            if (!chat.banner_date.equals(chatHistories.get(tempPos).banner_date)) {
                tvHistoryTime.setText(chat.banner_date);
                viewTop.setVisibility(View.GONE);
                llHistoryDate.setVisibility(View.VISIBLE);
                vBottom.setVisibility(View.GONE);
            } else {
                if (chat.banner_date.equals("Today") && position==0){
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.GONE);
                }else if (chat.banner_date.equals("Yesterday") && position==0){
                    tvHistoryTime.setText(chat.banner_date);
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.VISIBLE);
                }else {
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.VISIBLE);
                    llHistoryDate.setVisibility(View.GONE);
                }
            }

            if (position==(chatHistories.size()-1)){
                vBottom.setVisibility(View.VISIBLE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tvTime.setText(date);

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 600) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()){
                case R.id.rlChatHistory:
                    ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
                    String otherId = "";
                    if (!chatHistory.reciverId.equals(String.valueOf(Mualab.currentUser.id)))
                        otherId = chatHistory.reciverId;
                    else
                        otherId = chatHistory.senderId;

                    Intent chat_intent = new Intent(context, ChatActivity.class);
                    chat_intent.putExtra("userId",otherId);
                    context.startActivity(chat_intent);
                    break;
            }
        }
    }

    public class GroupChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvUname,tvMsg,tvChatType,tvUnReadCount,tvTime,tvHistoryTime;
        CircleImageView ivProfile;
        LinearLayout llHistoryDate;
        View vBottom,viewTop;

        GroupChatViewHolder(View itemView) {
            super(itemView);
            tvUname = itemView.findViewById(R.id.tvUname);
            tvMsg = itemView.findViewById(R.id.tvMsg);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatType = itemView.findViewById(R.id.tvChatType);
            tvUnReadCount = itemView.findViewById(R.id.tvUnReadCount);
            tvTime = itemView.findViewById(R.id.tvTime);
            llHistoryDate = itemView.findViewById(R.id.llHistoryDate);
            tvHistoryTime = itemView.findViewById(R.id.tvHistoryTime);
            vBottom = itemView.findViewById(R.id.vBottom);
            viewTop = itemView.findViewById(R.id.viewTop);
        }

        void otherBindData(final ChatHistory chat,int position,int tempPos){
            tvUname.setText(chat.userName);
            if (isTyping){
                tvMsg.setText("typing...");
                tvMsg.setTextColor(context.getResources().getColor(R.color.chatbox_blue));
            }else {
                if (chat.message.contains("https://firebasestorage.googleapis.com") | chat.messageType==1){
                    tvMsg.setText("Image");
                }else
                    tvMsg.setText(chat.message);
                tvMsg.setTextColor(context.getResources().getColor(R.color.grey));
            }

            if (chat.profilePic !=null && !chat.profilePic.isEmpty()) {
                Picasso.with(context).load(chat.profilePic).placeholder(R.drawable.defoult_user_img).
                        fit().into(ivProfile);
            }


            if (!chat.banner_date.equals(chatHistories.get(tempPos).banner_date)) {
                tvHistoryTime.setText(chat.banner_date);
                viewTop.setVisibility(View.GONE);
                llHistoryDate.setVisibility(View.VISIBLE);
                vBottom.setVisibility(View.GONE);
            } else {
                if (chat.banner_date.equals("Today") && position==0){
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.GONE);
                }else if (chat.banner_date.equals("Yesterday") && position==0){
                    tvHistoryTime.setText(chat.banner_date);
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.GONE);
                    llHistoryDate.setVisibility(View.VISIBLE);
                }else {
                    vBottom.setVisibility(View.GONE);
                    viewTop.setVisibility(View.VISIBLE);
                    llHistoryDate.setVisibility(View.GONE);
                }
            }

            if (position==(chatHistories.size()-1)){
                vBottom.setVisibility(View.VISIBLE);
            }

            SimpleDateFormat sd = new SimpleDateFormat("hh:mm a",Locale.getDefault());
            try {
                String date = sd.format(new Date((Long) chat.timestamp));
                tvTime.setText(date);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 600) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (v.getId()){
                case R.id.rlChatHistory:
                    /*ChatHistory chatHistory = chatHistories.get(getAdapterPosition());
                    String otherId = "";
                    if (!chatHistory.reciverId.equals(String.valueOf(Mualab.currentUser.id)))
                        otherId = chatHistory.reciverId;
                    else
                        otherId = chatHistory.senderId;

                    Intent chat_intent = new Intent(context, ChatActivity.class);
                    chat_intent.putExtra("userId",otherId);
                    context.startActivity(chat_intent);*/
                    break;
            }
        }
    }

    public void setTyping(boolean isTyping,int position){
        this.isTyping = isTyping;
        notifyItemChanged(position);
    }

    public void setTyping(boolean isTyping){
        this.isTyping = isTyping;
    }

    public void filterList(ArrayList<ChatHistory> filterdNames) {
        this.chatHistories = filterdNames;
        notifyDataSetChanged();
    }
}
