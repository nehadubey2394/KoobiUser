package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.model.FeedLike;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.UnfollowDialog;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dharmraj Acharya on 16/8/17.
 **/

public class LikeListAdapter extends RecyclerView.Adapter<LikeListAdapter.ViewHolder> {


    private List<FeedLike> likedList;
    private Context mContext;
    private int myUserId;

    public LikeListAdapter(Context mContext, List<FeedLike> likedList, int myUserId) {
        this.likedList = likedList;
        this.mContext = mContext;
        this.myUserId = myUserId;
    }

    @Override
    public LikeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.likes_item_layout, parent, false);
        return new LikeListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FeedLike feedLike = likedList.get(position);

        if(TextUtils.isEmpty(feedLike.profileImage)){
            Picasso.with(mContext).load(R.drawable.defoult_user_img).fit().into(holder.iv_profileImage);
        }else {
            Picasso.with(mContext).load(feedLike.profileImage).fit()
                    .placeholder(R.drawable.defoult_user_img)
                    .error(R.drawable.defoult_user_img)
                    .into(holder.iv_profileImage);
        }

        holder.tv_user_name.setText(feedLike.userName);
        holder.btn_follow.setVisibility(feedLike.id==myUserId?View.GONE:View.VISIBLE);

        if (feedLike.followingStatus == 1) {

            holder.btn_follow.setBackgroundResource(R.drawable.button_effect_invert);
            holder.btn_follow.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            holder.btn_follow.setText("Following");

        } else if (feedLike.followingStatus == 0) {
            holder.btn_follow.setBackgroundResource(R.drawable.button_effect_invert);
            holder.btn_follow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            holder.btn_follow.setText("Follow");
        }

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUnfollow(feedLike, position, holder);
            }
        });

    }

    private void followUnfollow(final FeedLike feedLike, final int position, final ViewHolder holder ){

        if(feedLike.followingStatus==1){
            new UnfollowDialog(mContext, feedLike, new UnfollowDialog.UnfollowListner() {
                @Override
                public void onUnfollowClick() {
                    apiForFollowUnFollow(feedLike, position, holder);
                }
            });
        }else apiForFollowUnFollow(feedLike, position, holder);

    }

    @Override
    public int getItemCount() {
        return likedList.size();
    }

    private void apiForFollowUnFollow(final FeedLike feedLike, final int position, final ViewHolder holder ) {

        Map<String, String> map = new HashMap<>();
        map.put("userId", ""+Mualab.currentUser.id);
        map.put("followerId", ""+feedLike.id);
        holder.btn_follow.setText("");
        holder.progressBar.setVisibility(View.VISIBLE);
        new HttpTask(new HttpTask.Builder(mContext, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                holder.progressBar.setVisibility(View.GONE);
                holder.btn_follow.setEnabled(true);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (feedLike.followingStatus==0) {
                            feedLike.followingStatus = 1;
                        } else if (feedLike.followingStatus==1) {
                            feedLike.followingStatus = 0;
                        }
                    }
                    notifyItemChanged(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                holder.progressBar.setVisibility(View.GONE);
                holder.btn_follow.setEnabled(true);
                notifyItemChanged(position);
            }
        }).setParam(map)).execute("");


    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profileImage;
        Button btn_follow;
        TextView tv_user_name;
        ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_user_name =  itemView.findViewById(R.id.tv_user_name);
            iv_profileImage = itemView.findViewById(R.id.iv_profileImage);
            btn_follow =  itemView.findViewById(R.id.btn_follow);
            progressBar =  itemView.findViewById(R.id.progress_bar);

            btn_follow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                }
            });
        }
    }
}
