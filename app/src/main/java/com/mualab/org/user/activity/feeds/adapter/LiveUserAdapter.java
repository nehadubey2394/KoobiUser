package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.mualab.org.user.R;
import com.mualab.org.user.data.model.feeds.LiveUserInfo;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Created by mindiii on 9/8/17.
 **/

public class LiveUserAdapter extends RecyclerView.Adapter<LiveUserAdapter.ViewHolder> {

    private Context mContext;
    private List<LiveUserInfo> liveUserList;
    private Listner listner;

    public interface Listner{
        void onClickedUserStory(LiveUserInfo storyUser, int position);
    }


    public LiveUserAdapter(Context mContext, List<LiveUserInfo> liveUserList, Listner listner) {
        this.mContext = mContext;
        this.liveUserList = liveUserList;
        this.listner = listner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_user_image_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        LiveUserInfo userInfo = liveUserList.get(position);
        holder.tvUserName.setText(String.format("%s", userInfo.userName));
        holder.ivAddLive.setVisibility(position==0?View.VISIBLE:View.GONE);
        if (!TextUtils.isEmpty(userInfo.profileImage))
            Picasso.with(mContext)
                    .load(userInfo.profileImage).placeholder(R.drawable.defoult_user_img)
                    .fit()
                    .into(holder.ivUserImg);

        /*if (feedState == FEED_TAB) {
            if (position == 0) {
                holder.ivAddLive.setVisibility(View.VISIBLE);
                holder.tv_live.setVisibility(View.GONE);
            } else if (position == 1) {
                holder.tv_live.setVisibility(View.VISIBLE);
            } else {
                holder.ivAddLive.setVisibility(View.GONE);
                holder.tv_live.setVisibility(View.GONE);
            }
        } else if (feedState == SEARCH_TAB) {
            if (position == 0) {
                holder.tv_live.setVisibility(View.VISIBLE);
            } else {
                holder.ivAddLive.setVisibility(View.GONE);
                holder.tv_live.setVisibility(View.GONE);
            }
        }*/
    }

    @Override
    public int getItemCount() {
        return liveUserList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivUserImg, ivAddLive;
        TextView tvUserName, tv_live;

         ViewHolder(View itemView) {
            super(itemView);
            tv_live = itemView.findViewById(R.id.tv_live);
            ivUserImg =  itemView.findViewById(R.id.iv_user_image);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            ivAddLive = itemView.findViewById(R.id.iv_add_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

             int pos = getAdapterPosition();
             LiveUserInfo userInfo = liveUserList.get(pos);
             listner.onClickedUserStory(userInfo, pos);
/*
             if(pos==0 && userInfo.storyCount==0){
                 mContext.startActivity(new Intent(mContext, CameraActivity.class));
             }else {
                 Intent intent = new Intent(mContext, StoreActivityTest.class);
                 Bundle args = new Bundle();
                 args.putSerializable("ARRAYLIST", (Serializable) liveUserList);
                 args.putInt("position", getAdapterPosition());
                 intent.putExtra("BUNDLE", args);
                 mContext.startActivity(intent);
             }*/
        }
    }
}
