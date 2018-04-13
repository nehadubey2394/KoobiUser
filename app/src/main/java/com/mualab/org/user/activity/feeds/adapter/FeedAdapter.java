package com.mualab.org.user.activity.feeds.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.SocialTextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.SearchFeedActivity;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.UnfollowDialog;
import com.mualab.org.user.listner.OnDoubleTapListener;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


/**
 * Created by Dharmraj Acharya on 10/8/17.
 **/

@SuppressWarnings("WeakerAccess")
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected boolean showLoader;
    private final int TEXT_TYPE = 0;
    private final int IMAGE_TYPE = 1;
    private final int VIDEO_TYPE = 2;
    private final int VIEW_TYPE_LOADING = 3;

    private Context mContext;
    private List<Feeds> feedItems;
    private Listener listener;
    private boolean loading;

    public void showHideLoading(boolean b) {
        loading = b;
    }


    public interface Listener{
        void onCommentBtnClick(Feeds feed, int pos);
        void onLikeListClick(Feeds feed);
        void onFeedClick(Feeds feed, int index, View v);
        void onClickProfileImage(Feeds feed,ImageView v);
    }

    public void clear(){
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }


    public FeedAdapter(Context mContext, List<Feeds> feedItems, Listener listener) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TEXT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_text_item_layout, parent, false);
                FeedTextHolder textHolder = new FeedTextHolder(view);
                setupTextFeedClickableViews(textHolder);
                return textHolder;
            case IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_image_item_layout, parent, false);
                CellFeedViewHolder cellFeedViewHolder = new CellFeedViewHolder(view);
                setupClickableViews(cellFeedViewHolder);
                return cellFeedViewHolder;
            case VIDEO_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_video_item_layout, parent, false);
                FeedVideoHolder feedViepoHolder = new FeedVideoHolder(view);
                setupFeedVideoClickableViews(feedViepoHolder);
                return feedViepoHolder;

            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;
    }


    @Override
    public int getItemViewType(int position) {
        Feeds feed = feedItems.get(position);
       /* if(position==feedItems.size()-1) {
            if (loading) {
                return VIEW_TYPE_LOADING;
            }
        }
*/
        switch (feed.feedType) {
            case "text":
                return TEXT_TYPE;
            case "image":
                return IMAGE_TYPE;
            case "video":
                return VIDEO_TYPE;
            default:
                return VIEW_TYPE_LOADING;
        }
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
        final Feeds feeds = feedItems.get(position);

        if(holder instanceof Holder){
            Holder h = (Holder) holder;
            if (!TextUtils.isEmpty(feeds.profileImage)) {
                Picasso.with(mContext).load(feeds.profileImage).fit().into(h.ivProfile);
            }else  Picasso.with(mContext).load(R.drawable.defoult_user_img).into(h.ivProfile);

            h.tvUserName.setText(feeds.userName);
            h.tvPostTime.setText(feeds.crd);
            h.tvUserLocation.setText(TextUtils.isEmpty(feeds.location)?"N/A":feeds.location);
            h.tv_like_count.setText(String.valueOf(feeds.likeCount));
            h.tv_comments_count.setText(String.valueOf(feeds.commentCount));
            h.likeIcon.setChecked(feeds.isLike==1);
            h.tv_text.setText(feeds.caption);

            if(!TextUtils.isEmpty(feeds.caption)){
                h.tv_text.setVisibility(View.VISIBLE);
                h.tv_text.setText(feeds.caption);
            }else h.tv_text.setVisibility(View.GONE);

            if(feeds.userId==Mualab.currentUser.id){
                h.btnFollow.setVisibility(View.GONE);
            }else {
                h.btnFollow.setVisibility(View.VISIBLE);
                if (feeds.followingStatus == 1) {
                    h.btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
                    h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    h.btnFollow.setText(R.string.following);
                } else {
                    h.btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
                    h.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    h.btnFollow.setText(R.string.follow);
                }
            }
        }


        switch (feeds.feedType) {
          /*  case "text":
                final FeedTextHolder textHolder = ((FeedTextHolder) holder);

                if (!TextUtils.isEmpty(feeds.profileImage)) {
                    Picasso.with(mContext)
                            .load(feeds.profileImage)
                            .fit()
                            .into(textHolder.ivProfile);
                }else  Picasso.with(mContext)
                        .load(R.drawable.defoult_user_img)
                        .into(textHolder.ivProfile);

                if (feeds.followingStatus == 1) {
                    textHolder.btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
                    textHolder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    textHolder.btnFollow.setText(R.string.following);
                } else {
                    textHolder.btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
                    textHolder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    textHolder.btnFollow.setText(R.string.follow);
                }
                textHolder.tvUserName.setText(feeds.userName);
                textHolder.tvPostTime.setText(feeds.crd);
                textHolder.tvUserLocation.setText(TextUtils.isEmpty(feeds.location)?"N/A":feeds.location);
                textHolder.tv_like_count.setText(String.valueOf(feeds.likeCount));
                textHolder.tv_comments_count.setText(String.valueOf(feeds.commentCount));
                textHolder.likeIcon.setChecked(feeds.isLike==1);
               // textHolder.btnLike.setImageResource(feeds.isLike==1? R.drawable.active_like_ico : R.drawable.inactive_like_ico);
                textHolder.tv_text.setText(feeds.caption);
                //ResizableTextView.doResizeTextView(textHolder.tv_text, 50 , "View More", true);
                break;*/

            case "image":{

                final CellFeedViewHolder imageHolder = ((CellFeedViewHolder) holder);

               /* if (!TextUtils.isEmpty(feeds.profileImage)) {
                    Picasso.with(imageHolder.ivProfile.getContext())
                            .load(feeds.profileImage)
                            .fit()
                            .into(imageHolder.ivProfile);
                }else  Picasso.with(mContext)
                        .load(R.drawable.defoult_user_img)
                        .into(imageHolder.ivProfile);

                if (feeds.followingStatus == 1) {
                    imageHolder.btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
                    imageHolder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    imageHolder.btnFollow.setText(R.string.following);
                } else {
                    imageHolder.btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
                    imageHolder.btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    imageHolder.btnFollow.setText(R.string.follow);
                }

                imageHolder.btnFollow.setText(feeds.followingStatus==1?"Following":"Follow");
                imageHolder.tvUserName.setText(feeds.userName);
                imageHolder.tvPostTime.setText(feeds.crd);
                imageHolder.tvUserLocation.setText(TextUtils.isEmpty(feeds.location)?"N/A":feeds.location);


                imageHolder.tv_like_count.setText(String.valueOf(feeds.likeCount));
                imageHolder.tv_comments_count.setText(String.valueOf(feeds.commentCount));
                imageHolder.likeIcon.setChecked(feeds.isLike==1);*/
                //imageHolder.btnLike.setImageResource(feeds.isLike==1? R.drawable.active_like_ico : R.drawable.inactive_like_ico);
/*

                if(!TextUtils.isEmpty(feeds.caption)){
                    imageHolder.tv_text.setVisibility(View.VISIBLE);
                    imageHolder.tv_text.setText(feeds.caption);
                }else imageHolder.tv_text.setVisibility(View.GONE);
*/

                imageHolder.weakRefAdapter = new WeakReference<>(new ViewPagerAdapter(mContext, feeds.feed, new ViewPagerAdapter.Listner() {
                    @Override
                    public void onSingleTap() {
                            int pos = imageHolder.weakRefViewPager.get().getCurrentItem();
                            if (feeds.feedType.equalsIgnoreCase("image")) {
                                listener.onFeedClick(feeds, pos, imageHolder.rl_imageView);

                            } /*else if (feeds.feedType.equalsIgnoreCase("video")) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(Uri.parse(feeds.feed.get(pos)), "video/mp4")
                                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                            }*/
                    }

                    @Override
                    public void onDoubleTap() {
                        int pos = imageHolder.getAdapterPosition();
                        Feeds feed = feedItems.get(pos);
                        if(feed.isLike==0){
                            feed.isLike = 1;
                            feed.likeCount = ++feed.likeCount;
                            apiForLikes(feeds);
                        }
                        notifyItemChanged(pos);
                    }

                    @Override
                    public void onLongPress() {

                    }
                }));

                imageHolder.weakRefViewPager.get().setAdapter(imageHolder.weakRefAdapter.get());

                if (feeds.feed.size() > 1) {
                    addBottomDots(imageHolder.ll_Dot, feeds.feed.size(), 0);
                    imageHolder.ll_Dot.setVisibility(View.VISIBLE);
                    imageHolder.weakRefViewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int pos) {
                            Feeds feed = feedItems.get(imageHolder.getAdapterPosition());
                            feed.viewPagerlastPos = pos;
                            addBottomDots(imageHolder.ll_Dot, feed.feed.size(), pos);
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });
                } else imageHolder.ll_Dot.setVisibility(View.GONE);

                imageHolder.weakRefViewPager.get().setCurrentItem(feeds.viewPagerlastPos);
            }
            break;


            case "video":

                final FeedVideoHolder videoHolder = ((FeedVideoHolder) holder);
                /*if (!TextUtils.isEmpty(feeds.profileImage)) {
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
                videoHolder.likeIcon.setChecked(feeds.isLike==1);*/
                //videoHolder.btnLike.setImageResource(feeds.isLike==1? R.drawable.active_like_ico : R.drawable.inactive_like_ico);

                if(!TextUtils.isEmpty(feeds.videoThumbnail)){
                    Picasso.with(videoHolder.ivFeedCenter.getContext())
                            .load(feeds.videoThumbnail)
                            .placeholder(R.drawable.gallery_placeholder)
                            .into(videoHolder.ivFeedCenter);
                }else  Picasso.with(videoHolder.ivFeedCenter.getContext())
                        .load(R.drawable.gallery_placeholder)
                        .into(videoHolder.ivFeedCenter);

               /* if(!TextUtils.isEmpty(feeds.caption)){
                    videoHolder.tv_text.setVisibility(View.VISIBLE);
                    videoHolder.tv_text.setText(feeds.caption);
                }else videoHolder.tv_text.setVisibility(View.GONE);
*/
                break;
        }
        // }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    private void setupTextFeedClickableViews(final FeedTextHolder holder) {
        holder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        holder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        holder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {

                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        holder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                goHashTag(charSequence);
                return null;
            }
        });

        holder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                MyToast.getInstance(mContext).showDasuAlert(mContext.getString(R.string.under_development));
                return null;
            }
        });

        holder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if(listener!=null){
                    listener.onCommentBtnClick(feed, pos);
                }
            }
        });

        holder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if(listener!=null){
                    listener.onLikeListClick(feed);
                }
            }
        });

        holder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike==1?0:1;
                feed.likeCount = feed.isLike==1?++feed.likeCount:--feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
            }
        });

        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // int adapterPosition = holder.getAdapterPosition();
               // Feeds feed = feedItems.get(adapterPosition);
                // shareDialog(feed, 0);
            }
        });

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // holder.btnFollow.setEnabled(false);
                int adapterPosition = holder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
            }
        });

        holder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    int adapterPosition = holder.getAdapterPosition();
                    Feeds feed = feedItems.get(adapterPosition);
                    listener.onClickProfileImage(feed, holder.ivProfile);
                }
            }
        });
    }

    private void setupFeedVideoClickableViews(final FeedVideoHolder videoHolder) {
        videoHolder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        videoHolder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        videoHolder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        videoHolder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                goHashTag(charSequence);
                return null;
            }
        });


        videoHolder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                MyToast.getInstance(mContext).showDasuAlert(mContext.getString(R.string.under_development));
                return null;
            }
        });

        videoHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if(listener!=null){
                    listener.onCommentBtnClick(feed, pos);
                }
            }
        });

        videoHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if(listener!=null){
                    listener.onLikeListClick(feed);
                }
            }
        });

        videoHolder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike==1?0:1;
                feed.likeCount = feed.isLike==1?++feed.likeCount:--feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
            }
        });

        videoHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                int innerPosition = 0;
                //  shareDialog(feed, innerPosition);*/
            }
        });

        videoHolder.ivFeedCenter.setOnTouchListener(new MyOnDoubleTapListener(mContext, videoHolder));

        videoHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
            }
        });

        videoHolder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    int adapterPosition = videoHolder.getAdapterPosition();
                    Feeds feed = feedItems.get(adapterPosition);
                    listener.onClickProfileImage(feed, videoHolder.ivProfile);
                }
            }
        });

    }

    private void setupClickableViews(final CellFeedViewHolder cellFeedViewHolder) {
        cellFeedViewHolder.tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        cellFeedViewHolder.tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        cellFeedViewHolder.tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(browserIntent);
                return null;
            }
        });

        cellFeedViewHolder.tv_text.setOnHashtagClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                goHashTag(charSequence);
                return null;
            }
        });


        cellFeedViewHolder.tv_text.setOnMentionClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                MyToast.getInstance(mContext).showDasuAlert(mContext.getString(R.string.under_development));
                return null;
            }
        });

        cellFeedViewHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(pos);
                if(listener!=null){
                    listener.onCommentBtnClick(feed, pos);
                }
            }
        });


        cellFeedViewHolder.rl_imageView.setOnTouchListener(new MyOnDoubleTapListener(mContext, cellFeedViewHolder));

        cellFeedViewHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                if(listener!=null){
                    listener.onLikeListClick(feed);
                }
            }
        });

        cellFeedViewHolder.likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                feed.isLike = feed.isLike==1?0:1;
                feed.likeCount = feed.isLike==1?++feed.likeCount:--feed.likeCount;
                notifyItemChanged(adapterPosition);
                apiForLikes(feed);
            }
        });

        cellFeedViewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                int innerPosition = 0;
                if (cellFeedViewHolder.weakRefViewPager != null)
                    innerPosition = cellFeedViewHolder.weakRefViewPager.get().getCurrentItem();
                // shareDialog(feed, innerPosition);*/
            }
        });

        cellFeedViewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cellFeedViewHolder.btnFollow.setEnabled(false);
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                Feeds feed = feedItems.get(adapterPosition);
                followUnfollow(feed, adapterPosition);
            }
        });

        cellFeedViewHolder.ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                    Feeds feed = feedItems.get(adapterPosition);
                    listener.onClickProfileImage(feed, cellFeedViewHolder.ivProfile);
                }
            }
        });
    }

    private void goHashTag(CharSequence charSequence) {
        Intent intent = new Intent(mContext, SearchFeedActivity.class);
        String tag = charSequence.toString().replace("#","");
        ExSearchTag e = new ExSearchTag();
        e.title = tag;
        e.id = 0;
        e.type = ExSearchTag.SearchType.HASH_TAG;
        intent.putExtra("searchKey",e);
        mContext.startActivity(intent);
    }

    private void addBottomDots(LinearLayout ll_dots, int totalSize, int currentPage) {
        TextView[] dots = new TextView[totalSize];
        ll_dots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(mContext);
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setTextSize(25);
            dots[i].setTextColor(Color.parseColor("#999999"));
            ll_dots.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(Color.parseColor("#212121"));
    }

/*
    private void shareDialog(final Feeds feed, final int innerPosition) {

        new com.app.mualab.share.ShareDialog(mContext, new com.app.mualab.share.ShareDialog.Listner() {
            @Override
            public void onClick(String string) {

                if (string.equalsIgnoreCase("facebook"))

                    if(feed.isShare.contains("facebook")){
                        new DataShare().shareToFacebook(mContext, innerPosition, feed);
                    }else {
                        Toasty.error(mContext, "This post is not shareable.").show();
                    }

                else if (string.equalsIgnoreCase("twitter")) {

                    if(feed.isShare.contains("twitter")){
                        if (feed.feedType.equalsIgnoreCase("text")) {
                            new DataShare(mContext, feed.caption).ShareText(feed);
                        } else if (feed.feedType.equalsIgnoreCase("image")) {
                            new DataShare(mContext, feed.caption).ShareAndLoadImage(feed.feed.get(innerPosition));
                        } else if (feed.feedType.equalsIgnoreCase("video")) {

                            new DataShare(mContext, feed.caption).shareVideoToTwitter(mContext,feed.feed.get(0));
                            // new DataShare(mContext, feed.caption).ShareAndLoadImage(feed.videoThumbnail);
                        }
                    }else {
                        Toasty.error(mContext, "This post is not shareable.").show();
                    }

                } else if (string.equalsIgnoreCase("mualab")) {
                    Toast.makeText(mContext, mContext.getString(R.string.under_development), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDismis(Dialog dialog) {
                dialog.dismiss();
            }
        }).show();
    }
*/

    private void apiForLikes(final Feeds feed) {
        Map<String, String> map = new HashMap<>();
        map.putAll(Mualab.feedBasicInfo);
        map.put("feedId", ""+feed._id);
        map.put("likeById", ""+Mualab.currentUser.id);
        map.put("userId", ""+feed.userId);
        map.put("type", "feed");// feed or comment
        Mualab.getInstance().getRequestQueue().cancelAll("like"+feed._id);
        new HttpTask(new HttpTask.Builder(mContext, "like", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        })
        .setParam(map)).execute("like"+feed._id);

    }

    static class Holder extends RecyclerView.ViewHolder {
        protected CheckBox likeIcon;
        protected ImageView ivLike;
        protected ImageView ivProfile, ivShare, ivComments; //btnLike
        protected LinearLayout ly_like_count, ly_comments;
        protected TextView tvUserName, tvUserLocation, tvPostTime;
        protected TextView tv_like_count, tv_comments_count;
        protected SocialTextView tv_text;
        protected AppCompatButton btnFollow;

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
        }
    }

    static class FeedTextHolder extends Holder {
        private FeedTextHolder(View itemView) {
            super(itemView);
        }
    }

    public static class FeedVideoHolder extends Holder {
        public ImageView  ivFeedCenter;

        private FeedVideoHolder(View itemView) {
            super(itemView);

            ivFeedCenter =  itemView.findViewById(R.id.ivFeedCenter);
        }
    }

    static class CellFeedViewHolder extends Holder {
        private LinearLayout ll_Dot;
        private RelativeLayout rl_imageView;
        private WeakReference<ViewPager> weakRefViewPager;
        private WeakReference<ViewPagerAdapter> weakRefAdapter;

        private CellFeedViewHolder(View itemView) {
            super(itemView);

            ll_Dot =  itemView.findViewById(R.id.ll_Dot);
            rl_imageView = itemView.findViewById(R.id.rl_imageView);
            weakRefViewPager = new WeakReference<>((ViewPager) itemView.findViewById(R.id.viewpager));
        }
    }

    private class MyOnDoubleTapListener extends OnDoubleTapListener {
        private CellFeedViewHolder holder;
        private FeedVideoHolder feedVieoHolder;

        private MyOnDoubleTapListener(Context c, CellFeedViewHolder holder) {
            super(c);
            this.holder = holder;
        }

        private MyOnDoubleTapListener(Context c, FeedVideoHolder feedVieoHolder) {
            super(c);
            this.feedVieoHolder = feedVieoHolder;
        }

        @Override
        public void onClickEvent(MotionEvent e) {
            int adapterPosition = getPosition();
            Feeds feed = feedItems.get(adapterPosition);
            if (feed.feedType.equalsIgnoreCase("image")) {
               /* mContext.startActivity(new Intent(mContext, PreviewImageActivity.class)
                        .setData(Uri.parse(feed.feed.get(0)))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));*/
                /*Intent intent = new Intent(mContext, PreviewImageActivity.class);
                List<String> list = new ArrayList<>();
                for(Feeds.Feed tmp: feed.feedData){
                    list.add(tmp.feedPost);
                }
                intent.putExtra("imageArray", (Serializable) list);
                intent.putExtra("startIndex", 0);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);*/

               // listener.onFeedClick(feed, adapterPosition);

            } else if (feed.feedType.equalsIgnoreCase("video")) {
                if(feed.feedThumb!=null && feed.feedThumb.size()>0){
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.parse(feed.feed.get(0)), "video/mp4")
                            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                }
            }
        }

        @Override
        public void onDoubleTap(MotionEvent e) {
            int adapterPosition = getPosition();
            Feeds feed = feedItems.get(adapterPosition);
            if(feed.isLike==0){
                feed.isLike = 1;
                feed.likeCount = ++feed.likeCount;
                apiForLikes(feed);
            }
            notifyItemChanged(adapterPosition);
        }

        private int getPosition(){
            if(holder!=null){
                return holder.getAdapterPosition();

            }else if(feedVieoHolder!=null){
                return feedVieoHolder.getAdapterPosition();
            }
            return 0;
        }
    }


    private void followUnfollow(final Feeds feeds, final int position){

        if(feeds.followingStatus==1){
            new UnfollowDialog(mContext, feeds, new UnfollowDialog.UnfollowListner() {
                @Override
                public void onUnfollowClick(Dialog dialog) {
                    dialog.dismiss();
                    apiForFollowUnFollow(feeds, position);
                }
            });
        }else apiForFollowUnFollow(feeds, position);

    }

    private void apiForFollowUnFollow(final Feeds feeds, final int position) {
        Map<String, String> map = new HashMap<>();
        map.put("userId", ""+Mualab.currentUser.id);
        map.put("followerId", ""+feeds.userId);

        new HttpTask(new HttpTask.Builder(mContext, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (feeds.followingStatus==0) {
                            feeds.followingStatus = 1;
                        } else if (feeds.followingStatus==1) {
                            feeds.followingStatus = 0;
                        }
                    }
                    notifyItemChanged(position);
                } catch (JSONException e) {
                    e.printStackTrace();
                    notifyItemChanged(position);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                notifyItemChanged(position);
            }
        }).setParam(map)).execute("followFollowing");
    }
}
