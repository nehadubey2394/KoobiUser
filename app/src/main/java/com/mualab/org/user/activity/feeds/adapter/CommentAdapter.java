package com.mualab.org.user.activity.feeds.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.model.Comment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mindiii on 16/8/17.
 **/

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> commentList;
    private Context mContext;
    private Listner listner;
    private Feeds feed;

    public interface Listner{
        void onItemChange();
    }

    public void setFeedId(Feeds feed){
        this.feed = feed;
    }

    public CommentAdapter(Context mContext, List<Comment> commentList, Listner listner) {
        this.commentList = commentList;
        this.mContext = mContext;
        this.listner = listner;
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_item_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CommentAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final Comment commentListInfo = commentList.get(position);

        holder.iv_like.setImageResource(commentListInfo.isLike==1?
                R.drawable.active_like_ico:R.drawable.inactive_like_ico);
        holder.iv_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiForCommentLike(commentListInfo, position, holder);
            }
        });

        if(!TextUtils.isEmpty(commentListInfo.profileImage)){
            Picasso.with(mContext).load(commentListInfo.profileImage)
                    .fit()
                    .placeholder(R.drawable.defoult_user_img)
                    .error(R.drawable.defoult_user_img)
                    .into(holder.iv_profileImage);
        }else Picasso.with(mContext).load(R.drawable.defoult_user_img).into(holder.iv_profileImage);


        if(commentListInfo.type.equals("image")){
            holder.ivImg.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(commentListInfo.comment)
                    .fit()
                    .placeholder(R.drawable.gallery_placeholder)
                    .error(R.drawable.gallery_placeholder)
                    .into(holder.ivImg);
            holder.tv_comments.setVisibility(View.GONE);
        }else {
            holder.tv_comments.setVisibility(View.VISIBLE);
            holder.ivImg.setVisibility(View.GONE);
            holder.tv_comments.setText(commentListInfo.comment);
        }

        holder.tv_user_name.setText(commentListInfo.userName);
        holder.tv_comments.setText(commentListInfo.comment);
        holder.tv_like_count.setText(String.format("%s Like", commentListInfo.commentLikeCount));
        holder.tv_comments_time.setText(commentListInfo.timeElapsed);
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profileImage,ivImg, iv_like;
        TextView tv_user_name, tv_comments, tv_comments_time, tv_like_count;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_comments = itemView.findViewById(R.id.tv_comments);
            tv_comments_time = itemView.findViewById(R.id.tv_comments_time);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            iv_profileImage = itemView.findViewById(R.id.iv_profileImage);
            ivImg = itemView.findViewById(R.id.ivImg);
            iv_like = itemView.findViewById(R.id.iv_like);
        }
    }

    private void apiForCommentLike(final Comment comment, final int position, final ViewHolder holder) {

        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("commentId", ""+comment.id);
        map.put("feedId", ""+feed._id);
        map.put("likeById", ""+Mualab.currentUser.id);
        map.put("userId", ""+comment.commentById);
        map.put("type", "comment");

        holder.iv_like.setEnabled(false);

        new HttpTask(new HttpTask.Builder(mContext, "commentLike", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    holder.iv_like.setEnabled(true);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (comment.isLike==0) {
                            comment.isLike = 1;
                            comment.commentLikeCount++;
                        } else {
                            comment.isLike = 0;
                            comment.commentLikeCount--;
                        }
                    }
                    notifyItemChanged(position);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                holder.iv_like.setEnabled(true);
            }
        }).setParam(map).setProgress(true)).execute("commentLike");
    }
}
