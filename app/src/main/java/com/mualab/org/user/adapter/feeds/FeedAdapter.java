package com.mualab.org.user.adapter.feeds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.SocialTextView;
import com.mualab.org.user.R;
import com.mualab.org.user.listner.OnDoubleTapListener;
import com.mualab.org.user.model.feeds.AllFeeds;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;


/**
 * Created by Dharmraj Acharya on 10/8/17.
 **/

@SuppressWarnings("WeakerAccess")
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String ACTION_LIKE_BUTTON_CLICKED = "action_like_button_button";
    public static final String ACTION_LIKE_IMAGE_CLICKED = "action_like_image_button";
    public static final int VIEW_TYPE_DEFAULT = 1;
    protected boolean showLoader;
    private final int TEXT_TYPE = 0;
    private final int IMAGE_TYPE = 1;
    private final int VIDEO_TYPE = 2;
    private final int VIEW_TYPE_LOADING = 3;

    private Context mContext;
    private List<AllFeeds> feedItems;

    private Listener listener;

    public interface Listener{
        void onCommentBtnClick(AllFeeds feed, int pos);
    }


    public FeedAdapter(Context mContext, List<AllFeeds> feedItems) {
        this.mContext = mContext;
        this.feedItems = feedItems;
    }

    public FeedAdapter(Context mContext, List<AllFeeds> feedItems, Listener listener) {
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

        AllFeeds feed = feedItems.get(position);
        if(feed== null){
            return  VIEW_TYPE_LOADING;
        }else {
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
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
//        if (holder instanceof LoadingViewHolder) {
//            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
//            loadingViewHolder.progressBar.setIndeterminate(true);
//        }
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
        final AllFeeds feeds = feedItems.get(position);
        String fullName = feeds.fullName.substring(0, 1).toUpperCase() + feeds.fullName.substring(1);

        switch (feeds.feedType) {
            case "text":
                final FeedTextHolder textHolder= ((FeedTextHolder) holder);

                if (!TextUtils.isEmpty(feeds.profileImage)) {
                    Picasso.with(mContext)
                            .load(feeds.profileImage)
                            .fit()
                            .into(textHolder.ivProfile);
                }

                textHolder.tvUserName.setText(fullName);
                textHolder.tvPostTime.setText(feeds.crd);
                if (!feeds.city.equals("") || feeds.city!=null){
                    textHolder.tvUserLocation.setText(feeds.city);
                }else {
                    textHolder.tvUserLocation.setText("NA");
                }


                textHolder.tv_like_count.setText(String.valueOf(feeds.likeCount));
                textHolder.tv_comments_count.setText(String.valueOf(feeds.commentCount));
                textHolder.btnLike.setImageResource(feeds.likeStatus.equals("1") ? R.drawable.active_like_ico : R.drawable.inactive_like_ico);
                textHolder.tv_text.setText(feeds.caption);
                break;

            case "image":{

                final CellFeedViewHolder imageHolder = ((CellFeedViewHolder) holder);

                if (!TextUtils.isEmpty(feeds.profileImage)) {
                    Picasso.with(imageHolder.ivProfile.getContext())
                            .load(feeds.profileImage)
                            .fit()
                            .into(imageHolder.ivProfile);
                }

                imageHolder.tvUserName.setText(fullName);
                imageHolder.tvPostTime.setText(feeds.crd);
                if (!feeds.city.equals("") || feeds.city!=null){
                    imageHolder.tvUserLocation.setText(feeds.city);
                }else {
                    imageHolder.tvUserLocation.setText("NA");
                }


                imageHolder.tv_like_count.setText(String.valueOf(feeds.likeCount));
                imageHolder.tv_comments_count.setText(String.valueOf(feeds.commentCount));
                imageHolder.btnLike.setImageResource(feeds.likeStatus.equals("1") ? R.drawable.active_like_ico : R.drawable.inactive_like_ico);

                if(!TextUtils.isEmpty(feeds.caption)){
                    imageHolder.tv_text.setVisibility(View.VISIBLE);
                    imageHolder.tv_text.setText(feeds.caption);
                }else imageHolder.tv_text.setVisibility(View.GONE);

                imageHolder.weakRefAdapter = new WeakReference<>(new ViewPagerAdapter(mContext, feeds.feed, new ViewPagerAdapter.Listner() {
                    @Override
                    public void onSingleTap() {
                           /* int pos = imageHolder.weakRefViewPager.get().getCurrentItem();
                            if (feeds.feedType.equalsIgnoreCase("image")) {
                                Intent intent = new Intent(mContext, ImageViewActivity.class);
                                intent.putExtra("imageArray", (Serializable) feeds.feed);
                                intent.putExtra("startIndex", pos);
                                mContext.startActivity(intent);
                            } else if (feeds.feedType.equalsIgnoreCase("video")) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(Uri.parse(feeds.feed.get(pos)), "video/mp4")
                                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                            }*/
                    }

                    @Override
                    public void onDoubleTap() {
                        if (feeds.likeStatus.equals("0")) {
                            feeds.likeCount++;
                            feeds.likeStatus = "1";
                        } /*else {
                                feeds.likeCount--;
                                feeds.likeStatus = "0";
                            }*/
                        notifyItemChanged(holder.getAdapterPosition(), ACTION_LIKE_IMAGE_CLICKED);
                        //apiForLikes(feeds);
                    }
                }));


          /*          imageHolder.viewPagerAdapter. = new ViewPagerAdapter(mContext, feeds.feed, new ViewPagerAdapter.Listner() {
                        @Override
                        public void onSingleTap() {
                            int pos = imageHolder.viewPager.get().getCurrentItem();
                            if (feeds.feedType.equalsIgnoreCase("image")) {
                                Intent intent = new Intent(mContext, ImageViewActivity.class);
                                intent.putExtra("imageArray", (Serializable) feeds.feed);
                                intent.putExtra("startIndex", pos);
                                mContext.startActivity(intent);
                            } else if (feeds.feedType.equalsIgnoreCase("video")) {
                                mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                                        .setDataAndType(Uri.parse(feeds.feed.get(pos)), "video/mp4")
                                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                            }
                        }

                        @Override
                        public void onDoubleTap() {
                            if (feeds.likeStatus.equals("0")) {
                                feeds.likeCount++;
                                feeds.likeStatus = "1";
                            } else {
                                feeds.likeCount--;
                                feeds.likeStatus = "0";
                            }
                            notifyItemChanged(position, ACTION_LIKE_IMAGE_CLICKED);
                            apiForLikes(feeds, position);
                        }
                    });*/

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
                            AllFeeds feed = feedItems.get(imageHolder.getAdapterPosition());
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

                final FeedVideoHolder vedioHolder = ((FeedVideoHolder) holder);
                if (!TextUtils.isEmpty(feeds.profileImage)) {
                    Picasso.with(vedioHolder.ivProfile.getContext())
                            .load(feeds.profileImage)
                            .fit()
                            .into(vedioHolder.ivProfile);
                }

                vedioHolder.tvUserName.setText(fullName);
                vedioHolder.tvPostTime.setText(feeds.crd);
                if (!feeds.city.equals("") || feeds.city!=null){
                    vedioHolder.tvUserLocation.setText(feeds.city);
                }else {
                    vedioHolder.tvUserLocation.setText("NA");
                }


                vedioHolder.tv_like_count.setText(String.valueOf(feeds.likeCount));
                vedioHolder.tv_comments_count.setText(String.valueOf(feeds.commentCount));
                vedioHolder.btnLike.setImageResource(feeds.likeStatus.equals("1") ? R.drawable.active_like_ico : R.drawable.inactive_like_ico);


                if(feeds.feedThumb!=null && feeds.feedThumb.size()>0){
                    Picasso.with(vedioHolder.ivFeedCenter.getContext())
                            .load(feeds.feedThumb.get(0))
                            .fit()
                            .placeholder(R.drawable.gallery_placeholder)
                            .into(vedioHolder.ivFeedCenter);
                }

                if(!TextUtils.isEmpty(feeds.caption)){
                    vedioHolder.tv_text.setVisibility(View.VISIBLE);
                    vedioHolder.tv_text.setText(feeds.caption);
                }else vedioHolder.tv_text.setVisibility(View.GONE);

                break;
        }
        // }
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    private void setupTextFeedClickableViews(final FeedTextHolder holder) {

        //SetFont.setfontRagular(holder.tv_text, mContext);
        holder.tv_text.setHashtagColorRes(R.color.colorPrimary);
        holder.tv_text.setMentionColorRes(R.color.black);
        holder.tv_text.setHyperlinkColorRes(R.color.blue);

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

        holder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());

                int pos = holder.getAdapterPosition();
                AllFeeds feed = feedItems.get(pos);
                if(listener!=null){
                    listener.onCommentBtnClick(feed, pos);
                }

                /*int adapterPosition = holder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("feed_id", feed.fId);
                intent.putExtra("feed", feed);
                intent.putExtra("feedPosition", adapterPosition);
                mContext.startActivity(intent);*/
            }
        });

        holder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
               /* AllFeeds feed = feedItems.get(adapterPosition);
                Intent intent = new Intent(mContext, LikeActivity.class);
                intent.putExtra("FeedId", feed.fId);
                intent.putExtra("myUserId", feed.userId);
                mContext.startActivity(intent);*/
            }
        });

        holder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);

                if (feed.likeStatus.equals("0")) {
                    feedItems.get(adapterPosition).likeCount++;
                    feed.likeStatus = "1";
                } else {
                    feedItems.get(adapterPosition).likeCount--;
                    feed.likeStatus = "0";
                }

                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                //apiForLikes(feed);
            }
        });

        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);
                // shareDialog(feed, 0);
            }
        });
    }

    private void setupFeedVideoClickableViews(final FeedVideoHolder videoHolder) {

        //SetFont.setfontRagular(videoHolder.tv_text, mContext);
        videoHolder.tv_text.setHashtagColorRes(R.color.colorPrimary);
        videoHolder.tv_text.setHyperlinkColorRes(R.color.blue);
        videoHolder.tv_text.setMentionColorRes(R.color.black);

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

        videoHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onFeedItemClickListener.onCommentsClick(view, videoHolder.getAdapterPosition());

                int pos = videoHolder.getAdapterPosition();
                AllFeeds feed = feedItems.get(pos);
                if(listener!=null){
                    listener.onCommentBtnClick(feed, pos);
                }

               /* int adapterPosition = videoHolder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("feed_id", feed.fId);
                intent.putExtra("feed", feed);
                intent.putExtra("feedPosition", adapterPosition);
                mContext.startActivity(intent);*/
            }
        });


        videoHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
               /* AllFeeds feed = feedItems.get(adapterPosition);
                Intent intent = new Intent(mContext, LikeActivity.class);
                intent.putExtra("FeedId", feed.fId);
                intent.putExtra("myUserId", feed.userId);
                mContext.startActivity(intent);*/
            }
        });

        videoHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);

                if (feed.likeStatus.equals("0")) {
                    feedItems.get(adapterPosition).likeCount++;
                    feed.likeStatus = "1";
                } else {
                    feedItems.get(adapterPosition).likeCount--;
                    feed.likeStatus = "0";
                }

                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                //  apiForLikes(feed);
            }
        });

        videoHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = videoHolder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);
                int innerPosition = 0;
                //  shareDialog(feed, innerPosition);
            }
        });

        videoHolder.ivFeedCenter.setOnTouchListener(new MyOnDoubleTapListener(mContext, videoHolder));
    }

    private void setupClickableViews(final CellFeedViewHolder cellFeedViewHolder) {

        // SetFont.setfontRagular(cellFeedViewHolder.tv_text, mContext);
        cellFeedViewHolder.tv_text.setHashtagColorRes(R.color.colorPrimary);
        cellFeedViewHolder.tv_text.setMentionColorRes(R.color.black);
        cellFeedViewHolder.tv_text.setHyperlinkColorRes(R.color.blue);

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

        cellFeedViewHolder.ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onFeedItemClickListener.onCommentsClick(view, cellFeedViewHolder.getAdapterPosition());

                int pos = cellFeedViewHolder.getAdapterPosition();
                AllFeeds feed = feedItems.get(pos);
                if(listener!=null){
                    listener.onCommentBtnClick(feed, pos);
                }

               /* Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("feed_id", feed.fId);
                intent.putExtra("feed", feed);
                intent.putExtra("feedPosition", adapterPosition);
                mContext.startActivity(intent);*/
            }
        });


        cellFeedViewHolder.rl_imageView.setOnTouchListener(new MyOnDoubleTapListener(mContext, cellFeedViewHolder));

        cellFeedViewHolder.ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
              /*  AllFeeds feed = feedItems.get(adapterPosition);
                Intent intent = new Intent(mContext, LikeActivity.class);
                intent.putExtra("FeedId", feed.fId);
                intent.putExtra("myUserId", feed.userId);
                mContext.startActivity(intent);*/
            }
        });

        cellFeedViewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);

                if (feed.likeStatus.equals("0")) {
                    feedItems.get(adapterPosition).likeCount++;
                    feed.likeStatus = "1";
                } else {
                    feedItems.get(adapterPosition).likeCount--;
                    feed.likeStatus = "0";
                }

                notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
                // apiForLikes(feed);
            }
        });

        cellFeedViewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = cellFeedViewHolder.getAdapterPosition();
                AllFeeds feed = feedItems.get(adapterPosition);
                int innerPosition = 0;
                if (cellFeedViewHolder.weakRefViewPager != null)
                    innerPosition = cellFeedViewHolder.weakRefViewPager.get().getCurrentItem();
                // shareDialog(feed, innerPosition);
            }
        });
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
    private void shareDialog(final AllFeeds feed, final int innerPosition) {

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

/*
    private void apiForLikes(final AllFeeds feed) {

        Map<String, String> map = new HashMap<>();
        map.put("feedId", feed.fId);
        WebServiceAPI api = new WebServiceAPI(mContext, "Img", new ResponseApi.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
               */
/* Log.d("Responce",response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if(status.equalsIgnoreCase("success")){

                        if(feed.likeStatus.equals("0")){
                            feed.likeStatus = "1";
                            feed.likeCount = String.valueOf(Integer.parseInt(feed.likeCount) + 1);
                            feed.likeCount++;

                        } else if(feed.likeStatus.equals("1")){
                            feed.likeStatus = "0";
                            feed.likeCount=   String.valueOf(Integer.parseInt(feed.likeCount) - 1);
                            feed.likeCount++;
                        }
                        notifyItemChanged(position);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }*//*

            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        });
        api.enableProgressBar(false);
        api.callApi("user/likes", Request.Method.POST, map);

    }
*/

    static class FeedTextHolder extends RecyclerView.ViewHolder {
        public View vBgLike;
        public ImageView ivLike;
        public ImageView ivProfile, ivShare, btnLike, ivComments, ivFeedCenter;
        private LinearLayout ly_like_count, ly_comments;
        private TextView tvUserName, tvUserLocation, tvPostTime;
        private TextView tv_like_count, tv_comments_count;
        private SocialTextView tv_text;

        private FeedTextHolder(View itemView) {
            super(itemView);

            ivProfile =  itemView.findViewById(R.id.iv_user_image);
            ivShare =  itemView.findViewById(R.id.iv_share);
            btnLike = itemView.findViewById(R.id.btnLike);
            ivComments = itemView.findViewById(R.id.iv_comments);
            ivFeedCenter = itemView.findViewById(R.id.ivFeedCenter);

            tv_text = itemView.findViewById(R.id.tv_text);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserLocation = itemView.findViewById(R.id.tv_location);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            tv_comments_count = itemView.findViewById(R.id.tv_comments_count);

            ly_like_count = itemView.findViewById(R.id.ly_like_count);
            ly_comments = itemView.findViewById(R.id.ly_comments);

            vBgLike = itemView.findViewById(R.id.vBgLike);
            ivLike = itemView.findViewById(R.id.ivLike);
        }
    }

    public static class FeedVideoHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfile, ivShare, btnLike, ivComments, ivFeedCenter;
        public View vBgLike;
        public ImageView ivLike;
        private LinearLayout ly_like_count, ly_comments;
        private TextView tvUserName, tvUserLocation, tvPostTime;
        private TextView tv_like_count, tv_comments_count;
        private SocialTextView tv_text;

        private FeedVideoHolder(View itemView) {
            super(itemView);

            ivProfile =  itemView.findViewById(R.id.iv_user_image);
            ivShare =  itemView.findViewById(R.id.iv_share);
            btnLike =  itemView.findViewById(R.id.btnLike);
            ivComments =  itemView.findViewById(R.id.iv_comments);
            ivFeedCenter =  itemView.findViewById(R.id.ivFeedCenter);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserLocation = itemView.findViewById(R.id.tv_location);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            tv_text = itemView.findViewById(R.id.tv_text);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            ly_like_count = itemView.findViewById(R.id.ly_like_count);
            ly_comments = itemView.findViewById(R.id.ly_comments);
            tv_comments_count = itemView.findViewById(R.id.tv_comments_count);

            vBgLike = itemView.findViewById(R.id.vBgLike);
            ivLike =  itemView.findViewById(R.id.ivLike);
        }
    }

    static class CellFeedViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfile, ivShare, btnLike, ivComments;
        public View vBgLike;
        public ImageView ivLike;
        private LinearLayout ly_like_count, ly_comments, ll_Dot;
        private RelativeLayout rl_imageView;
        private TextView tvUserName, tvUserLocation, tvPostTime;
        private TextView tv_like_count, tv_comments_count;
        private SocialTextView tv_text;

        private WeakReference<ViewPager> weakRefViewPager;
        private WeakReference<ViewPagerAdapter> weakRefAdapter;

        private CellFeedViewHolder(View itemView) {
            super(itemView);

            ivProfile =  itemView.findViewById(R.id.iv_user_image);
            ivShare =  itemView.findViewById(R.id.iv_share);
            btnLike =  itemView.findViewById(R.id.btnLike);
            ivComments =  itemView.findViewById(R.id.iv_comments);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserLocation = itemView.findViewById(R.id.tv_location);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            tv_text = itemView.findViewById(R.id.tv_text);
            tv_like_count = itemView.findViewById(R.id.tv_like_count);
            ly_like_count =  itemView.findViewById(R.id.ly_like_count);
            ly_comments =  itemView.findViewById(R.id.ly_comments);
            ll_Dot =  itemView.findViewById(R.id.ll_Dot);
            tv_comments_count = itemView.findViewById(R.id.tv_comments_count);
            rl_imageView = itemView.findViewById(R.id.rl_imageView);
            weakRefViewPager = new WeakReference<>((ViewPager) itemView.findViewById(R.id.viewpager));

            vBgLike = itemView.findViewById(R.id.vBgLike);
            ivLike =  itemView.findViewById(R.id.ivLike);
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
            AllFeeds feed = feedItems.get(adapterPosition);
         /*   if (feed.feedType.equalsIgnoreCase("image")) {
                mContext.startActivity(new Intent(mContext, ImageViewActivity.class)
                        .setData(Uri.parse(feed.feed.get(0)))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } else if (feed.feedType.equalsIgnoreCase("video")) {
                if(feed.feedThumb!=null && feed.feedThumb.size()>0){
                    mContext.startActivity(new Intent(Intent.ACTION_VIEW)
                            .setDataAndType(Uri.parse(feed.feed.get(0)), "video/mp4")
                            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                }
            }*/
        }

        @Override
        public void onDoubleTap(MotionEvent e) {
            int adapterPosition = getPosition();
            AllFeeds feed = feedItems.get(adapterPosition);
            if (feed.likeStatus.equals("0")) {
                feedItems.get(adapterPosition).likeCount++;
                feed.likeStatus = "1";
            }
            else {
                feedItems.get(adapterPosition).likeCount--;
                feed.likeStatus = "0";
            }

            notifyItemChanged(adapterPosition, ACTION_LIKE_IMAGE_CLICKED);
            //apiForLikes(feed);
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
}
