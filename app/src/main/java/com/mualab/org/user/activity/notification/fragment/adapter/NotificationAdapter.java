package com.mualab.org.user.activity.notification.fragment.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.notification.fragment.model.Notification;
import com.squareup.picasso.Picasso;


import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Notification> notificationList;
    private boolean showLoader;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;

    // Constructor of the class
    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==notificationList.size()-1){
            return showLoader?VIEWTYPE_LOADER:VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    /*   @Override
       public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
           View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followers_layout, parent, false);
           return new ViewHolder(view);

       }*/
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_staff_item_layout, parent, false);
        // return new ViewHolder(view);
        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_list, parent, false);
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
        final Notification item = notificationList.get(position);

        holder.tvName.setText(item.firstName);
        holder.tvTimeElapsed.setText(item.timeElapsed);
      //  holder.tvMsg.setText(item.message);
        String currentString = item.message;
        String[] separated = currentString.split(" ",2);
        String sName = separated[0];
        String msg = separated[1];

        String changedText = "<font color='black'>" + sName + "</font>";

       // String sourceString = "<big><b>" + changedText + "</b></big> " + msg;
        String sourceString = "<big>" + changedText + "</big> " + msg;

        holder.tvMsg.setText(Html.fromHtml(sourceString));

        if (item.type.equals("social")){
            holder.tvName.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.ivUserImg.setBackground(context.getResources().getDrawable(R.drawable.bg_blue_circle_profile));

        }else {
            holder.tvName.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            holder.ivUserImg.setBackground(context.getResources().getDrawable(R.drawable.bg_pink_circle_profile));
        }

        if (!item.profileImage.equals("")){
            Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivUserImg);
        }else {
            holder.ivUserImg.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvName,tvMsg,tvTimeElapsed;
        CircleImageView ivUserImg;
        private ViewHolder(View itemView)
        {
            super(itemView);
            ivUserImg = itemView.findViewById(R.id.ivUserImg);
            tvTimeElapsed = itemView.findViewById(R.id.tvTimeElapsed);
            tvName = itemView.findViewById(R.id.tvName);
            tvMsg = itemView.findViewById(R.id.tvMsg);


        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){

            }

        }
    }

}