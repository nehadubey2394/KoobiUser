package com.mualab.org.user.adapter.feeds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.model.feeds.LiveUserInfo;
import com.mualab.org.user.model.feeds.MyStory;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mualab.org.user.fragment.feeds.FeedsFragment.FEED_TAB;
import static com.mualab.org.user.fragment.feeds.FeedsFragment.SEARCH_TAB;

/**
 * Created by mindiii on 9/8/17.
 */

public class LiveUserAdapter extends RecyclerView.Adapter<LiveUserAdapter.ViewHolder> {

    private Context mContext;
    private List<LiveUserInfo> liveUserList;
    private List<MyStory> myStoryList;
    private int feedState;

    public LiveUserAdapter(Context mContext, List<LiveUserInfo> liveUserList, int feedState) {
        this.mContext = mContext;
        this.liveUserList = liveUserList;
        this.feedState = feedState;
        myStoryList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_user_image_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.tvUserName.setText(liveUserList.get(position).fullName);
        if (feedState == FEED_TAB) {
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
        }


        if (!liveUserList.get(position).profileImage.equals(""))
            Picasso.with(mContext)
                    .load(liveUserList.get(position).profileImage).placeholder(R.drawable.defoult_user_img)
                    .fit()
                    .into(holder.ivUserImg);
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
        /*    Intent intent = new Intent(mContext, StoriesActivity.class);
            Bundle args = new Bundle();
            args.putSerializable("ARRAYLIST", (Serializable) liveUserList);
            args.putInt("position", getAdapterPosition());
            intent.putExtra("BUNDLE", args);
            mContext.startActivity(intent);
*/
        }
    }
}
