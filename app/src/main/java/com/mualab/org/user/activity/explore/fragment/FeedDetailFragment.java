package com.mualab.org.user.activity.explore.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
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
import com.mualab.org.user.activity.BaseFragment;
import com.mualab.org.user.activity.BaseListner;
import com.mualab.org.user.activity.feeds.CommentsActivity;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.adapter.ViewPagerAdapter;
import com.mualab.org.user.activity.feeds.fragment.LikeFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.UnfollowDialog;
import com.mualab.org.user.listner.FeedsListner;
import com.mualab.org.user.listner.OnDoubleTapListener;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import views.refreshview.CircleHeaderView;
import views.refreshview.OnRefreshListener;
import views.refreshview.RjRefreshLayout;

public class FeedDetailFragment extends Fragment {

    private Context mContext;
    private CheckBox likeIcon;
    private ImageView ivLike;
    private ImageView ivProfile, ivShare, ivComments; //btnLike
    private LinearLayout ly_like_count, ly_comments;
    private TextView tvUserName, tvUserLocation, tvPostTime;
    private TextView tv_like_count, tv_comments_count;
    private SocialTextView tv_text;
    private AppCompatButton btnFollow;

    private LinearLayout ll_Dot;
    private RelativeLayout rl_imageView;
    private WeakReference<ViewPager> weakRefViewPager;
    private WeakReference<ViewPagerAdapter> weakRefAdapter;

    private Feeds feed;
    private Uri feedId;
    private FeedsListner feedsListner;


    public FeedDetailFragment() {
        // Required empty public constructor
    }


    public static FeedDetailFragment newInstance(String feedId, Feeds feed) {
        FeedDetailFragment fragment = new FeedDetailFragment();
        Bundle args = new Bundle();
        args.putString("dhar", feedId);
        args.putSerializable("feed", feed);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        
        if(context instanceof FeedsListner){
            feedsListner = (FeedsListner) context;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            feed = (Feeds) getArguments().getSerializable("feed");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewDidload(view);
        if(feed!=null){
            setupView();
            setupClicks();
        }
    }

    private void viewDidload(View view){
        ivProfile =  view.findViewById(R.id.iv_user_image);
        ivShare =  view.findViewById(R.id.iv_share);
        ivComments = view.findViewById(R.id.iv_comments);
        tv_text = view.findViewById(R.id.tv_text);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserLocation = view.findViewById(R.id.tv_location);
        tvPostTime = view.findViewById(R.id.tv_post_time);
        tv_like_count = view.findViewById(R.id.tv_like_count);
        tv_comments_count = view.findViewById(R.id.tv_comments_count);
        ly_like_count = view.findViewById(R.id.ly_like_count);
        ly_comments = view.findViewById(R.id.ly_comments);
        ivLike = view.findViewById(R.id.ivLike);
        btnFollow = view.findViewById(R.id.btnFollow);
        likeIcon = view.findViewById(R.id.likeIcon);
        ll_Dot =  view.findViewById(R.id.ll_Dot);
        rl_imageView = view.findViewById(R.id.rl_imageView);
        weakRefViewPager = new WeakReference<>((ViewPager) view.findViewById(R.id.viewpager));
    }

    private void setupClicks(){
        tv_text.setHashtagColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        tv_text.setMentionColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        tv_text.setOnHyperlinkClickListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence charSequence) {
                String url = charSequence.toString();
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
                return null;
            }
        });

        ly_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("feed_id", feed._id);
                intent.putExtra("feedPosition", 0);
                intent.putExtra("feed", feed);
                startActivityForResult(intent, Constant.ACTIVITY_COMMENT);
            }
        });


        rl_imageView.setOnTouchListener(new MyOnDoubleTapListener(mContext));

        ly_like_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(feedsListner!=null) 
                    feedsListner.addFragment(LikeFragment.newInstance(feed._id, Mualab.currentUser.id), true);
            }
        });

        likeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feed.isLike = feed.isLike==1?0:1;
                feed.likeCount = feed.isLike==1?++feed.likeCount:--feed.likeCount;
                if(feedsListner!=null)
                    feedsListner.apiForLikes(feed);
            }
        });

        ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               feedsListner.showToast(getString(R.string.under_development));
            }
        });

        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followUnfollow(feed, 0);
            }
        });

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(mContext, ImageViewDialogActivity.class);
        intent.putExtra("feed", feed);
        intent.putExtra("index", 0);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation((Activity) mContext, v, "image");
        startActivityForResult(intent, Constant.POST_FEED_DATA, options.toBundle());*/
            }
        });
    }

    private void setupView(){

        if (!TextUtils.isEmpty(feed.profileImage)) {
            Picasso.with(ivProfile.getContext())
                    .load(feed.profileImage)
                    .fit()
                    .into(ivProfile);
        }else  Picasso.with(mContext)
                .load(R.drawable.defoult_user_img)
                .into(ivProfile);

        if (feed.followingStatus == 1) {
            btnFollow.setBackgroundResource(R.drawable.btn_bg_blue_broder);
            btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            btnFollow.setText(R.string.following);
        } else {
            btnFollow.setBackgroundResource(R.drawable.button_effect_invert);
            btnFollow.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            btnFollow.setText(R.string.follow);
        }

        btnFollow.setText(feed.followingStatus==1?"Following":"Follow");
        tvUserName.setText(feed.userName);
        tvPostTime.setText(feed.crd);
        tvUserLocation.setText(TextUtils.isEmpty(feed.location)?"N/A":feed.location);


        tv_like_count.setText(String.valueOf(feed.likeCount));
        tv_comments_count.setText(String.valueOf(feed.commentCount));
        likeIcon.setChecked(feed.isLike==1);
        //btnLike.setImageResource(feed.isLike==1? R.drawable.active_like_ico : R.drawable.inactive_like_ico);

        if(!TextUtils.isEmpty(feed.caption)){
            tv_text.setVisibility(View.VISIBLE);
            tv_text.setText(feed.caption);
        }else tv_text.setVisibility(View.GONE);

        weakRefAdapter = new WeakReference<>(new ViewPagerAdapter(mContext, feed.feed, new ViewPagerAdapter.Listner() {
            @Override
            public void onSingleTap() {

            }

            @Override
            public void onDoubleTap() {
                if(feed.isLike==0){
                    feed.isLike = 1;
                    feed.likeCount = ++feed.likeCount;
                    if(feedsListner!=null) feedsListner.apiForLikes(feed);
                }
            }

            @Override
            public void onLongPress() {

            }
        }));

        weakRefViewPager.get().setAdapter(weakRefAdapter.get());

        if (feed.feed.size() > 1) {
            addBottomDots(ll_Dot, feed.feed.size(), 0);
            ll_Dot.setVisibility(View.VISIBLE);
            weakRefViewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int pos) {
                    addBottomDots(ll_Dot, feed.feed.size(), pos);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        } else ll_Dot.setVisibility(View.GONE);

        weakRefViewPager.get().setCurrentItem(feed.viewPagerlastPos);
    }


    private class MyOnDoubleTapListener extends OnDoubleTapListener {

        private MyOnDoubleTapListener(Context c) {
            super(c);
        }


        @Override
        public void onClickEvent(MotionEvent e) {

            if (feed.feedType.equalsIgnoreCase("image")) {

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
            if(feed.isLike==0){
                feed.isLike = 1;
                feed.likeCount = ++feed.likeCount;
                if(feedsListner!=null) feedsListner.apiForLikes(feed);
            }
        }
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }
        }).setParam(map)).execute("followFollowing");
    }

}
