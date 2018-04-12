package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hendraanggrian.widget.SocialTextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.searchBoard.adapter.LoadingViewHolder;
import com.mualab.org.user.model.feeds.Feeds;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Dharmraj Acharya on 10/8/17.
 **/

@SuppressWarnings("WeakerAccess")
public class ExploreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected boolean showLoader;
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private Context mContext;
    private List<Feeds> feedItems;
    private Listener listener;

    public interface Listener{
        void onFeedClick(Feeds feed, int index, View v);
    }

    public void clear(){
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }


    public ExploreAdapter(Context mContext, List<Feeds> feedItems, Listener listener) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case FEED_TYPE:
                return new Holder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.feed_image_item_layout, parent, false));
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;
    }


    @Override
    public int getItemViewType(int position) {
        Feeds feed = feedItems.get(position);
        return feed==null?VIEW_TYPE_LOADING: FEED_TYPE;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }
        //  else {
        final Feeds feeds = feedItems.get(position);

        final Holder videoHolder = ((Holder) holder);
        if (!TextUtils.isEmpty(feeds.profileImage)) {
            Picasso.with(videoHolder.ivProfile.getContext())
                    .load(feeds.profileImage)
                    .fit()
                    .into(videoHolder.ivProfile);
        }else  Picasso.with(mContext)
                .load(R.drawable.defoult_user_img)
                .into(videoHolder.ivProfile);

        if (feeds.followingStatus == 1) {
            videoHolder.btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
            videoHolder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            videoHolder.btnFollow.setText(R.string.following);
        } else {
            videoHolder.btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
            videoHolder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            videoHolder.btnFollow.setText(R.string.follow);
        }
        videoHolder.tvUserName.setText(feeds.userName);
        videoHolder.tvPostTime.setText(feeds.crd);
        videoHolder.tvUserLocation.setText(TextUtils.isEmpty(feeds.location)?"N/A":feeds.location);
        videoHolder.tv_like_count.setText(String.valueOf(feeds.likeCount));
        videoHolder.tv_comments_count.setText(String.valueOf(feeds.commentCount));
        videoHolder.likeIcon.setChecked(feeds.isLike==1);
        //videoHolder.btnLike.setImageResource(feeds.isLike==1? R.drawable.active_like_ico : R.drawable.inactive_like_ico);

        if(!TextUtils.isEmpty(feeds.videoThumbnail)){
            Picasso.with(videoHolder.ivFeedCenter.getContext())
                    .load(feeds.videoThumbnail)
                    .placeholder(R.drawable.gallery_placeholder)
                    .into(videoHolder.ivFeedCenter);
        }else  Picasso.with(videoHolder.ivFeedCenter.getContext())
                .load(R.drawable.gallery_placeholder)
                .into(videoHolder.ivFeedCenter);

        if(!TextUtils.isEmpty(feeds.caption)){
            videoHolder.tv_text.setVisibility(View.VISIBLE);
            videoHolder.tv_text.setText(feeds.caption);
        }else videoHolder.tv_text.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }


    static class Holder extends RecyclerView.ViewHolder {
        private CheckBox likeIcon;
        private ImageView  ivFeedCenter;
        private ImageView ivLike;
        private ImageView ivProfile, ivShare, ivComments; //btnLike
        private LinearLayout ly_like_count, ly_comments;
        private TextView tvUserName, tvUserLocation, tvPostTime;
        private TextView tv_like_count, tv_comments_count;
        private SocialTextView tv_text;
        private AppCompatButton btnFollow;

        public Holder(View itemView) {
            super(itemView);
            /*Common ui*/
            ivProfile =  itemView.findViewById(R.id.iv_user_image);
            ivShare =  itemView.findViewById(R.id.iv_share);
            ivComments = itemView.findViewById(R.id.iv_comments);
            tv_text = itemView.findViewById(R.id.tv_text);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserLocation = itemView.findViewById(R.id.tv_location);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            tv_comments_count = itemView.findViewById(R.id.tv_comments_count);
            ly_like_count = itemView.findViewById(R.id.ly_like_count);
            ly_comments = itemView.findViewById(R.id.ly_comments);
            ivLike = itemView.findViewById(R.id.ivLike);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            ivFeedCenter =  itemView.findViewById(R.id.ivFeedCenter);
        }
    }
}
