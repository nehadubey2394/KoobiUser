package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.my_profile.activity.UserProfileActivity;
import com.mualab.org.user.activity.people_tag.instatag.InstaTag;
import com.mualab.org.user.activity.people_tag.instatag.TagToBeTagged;
import com.mualab.org.user.activity.people_tag.models.TagDetail;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.data.model.feeds.Feeds;
import com.mualab.org.user.listner.OnDoubleTapListener;
import com.mualab.org.user.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPagerAdapter extends PagerAdapter  {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private List<String> ImagesList;
    private HashMap<Integer,ArrayList<TagToBeTagged>> taggedImgMap;
    private Listner listner;
    private MyOnDoubleTapListener tapListener;
    private static int widthPixels;
    private Feeds feeds;
    private boolean isShow,isFromFeed;

    public ViewPagerAdapter(Context context, Feeds feeds, boolean isFromFeed,Listner listner) {
        this.context = context;
        this.feeds = feeds;
        this.isFromFeed = isFromFeed;
        this.ImagesList = feeds.feed;
        this.taggedImgMap = feeds.taggedImgMap;
        this.listner = listner;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tapListener = new MyOnDoubleTapListener(context);
        widthPixels = ScreenUtils.getScreenWidth(context);
        widthPixels = widthPixels>=1080?1080:widthPixels;
    }

    @Override
    public int getCount() {
        return ImagesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_layout, container, false);
        // ImageView postImages = itemView.findViewById(R.id.post_image);
        final InstaTag postImages = itemView.findViewById(R.id.post_image);

        isShow = false;

        postImages.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        postImages.setRootWidth(postImages.getMeasuredWidth());
        postImages.setRootHeight(postImages.getMeasuredHeight());

        //    final ImageView ivTag = itemView.findViewById(R.id.ivTag);

        itemView.setOnTouchListener(tapListener);

        postImages.setTouchListnerDisable();

        postImages.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!postImages.isShowTags()) {
                    isShow = true;
                    postImages.showTags();
                }
                else {
                    isShow = false;
                    postImages.hideTags();
                }

                return false;
            }
        });

        Glide.with(context).load(ImagesList.get(position)).placeholder(R.drawable.gallery_placeholder)
                .centerCrop().skipMemoryCache(false).into(postImages.getTagImageView());


        if (taggedImgMap.size()!=0){
            for(Map.Entry map  :  taggedImgMap.entrySet() ) {
                int i = (int) map.getKey();
                if (i == position){
                    ArrayList<TagToBeTagged>tags = (ArrayList<TagToBeTagged>) map.getValue();
                    postImages.addTagViewFromTagsToBeTagged(tags,false);
                    postImages.hideTags();
                    // postImages.showTags();
                }/*else {
                        postImages.hideTags();
                    }*/
            }
        }

        postImages.setListener(new InstaTag.Listener() {
            @Override
            public void onTagCliked(final TagDetail tagDetail) {
                if (isFromFeed){
                    if(tagDetail!=null){
                        if (tagDetail.userType!=null && !tagDetail.userType.equals("")){
                            if (tagDetail.tagId!=null && !tagDetail.tagId.equals("")){
                                if (tagDetail.userType.equals("user")){
                                    Intent intent = new Intent(context, UserProfileActivity.class);
                                    intent.putExtra("userId",tagDetail.tagId);
                                    context.startActivity(intent);
                                }else {
                                    ArtistsSearchBoard item = new ArtistsSearchBoard();
                                    item._id = tagDetail.tagId;
                                    Intent intent2 = new Intent(context, ArtistProfileActivity.class);
                                    intent2.putExtra("item",item);
                                    context.startActivity(intent2);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onTagRemoved(TagDetail tagDetail) {
               /* if (taggedImgMap.size()!=0){
                    // taggedImgMap.remove(position);
                    for(Map.Entry map  :  taggedImgMap.entrySet() ) {
                        int key = (int) map.getKey();
                        if (key == position){
                            ArrayList<TagToBeTagged>tags = (ArrayList<TagToBeTagged>) map.getValue();

                            if(tagDetail!=null){
                                for (TagToBeTagged tagToBeTagged : tags){
                                    if (tagToBeTagged.getUnique_tag_id().
                                            equals(tagDetail.title)){
                                        tags.remove(tagToBeTagged);
                                    }
                                }
                            }
                        }
                    }
                }*/
            }
        });
        //  }

       /* ivTag.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (feeds.peopleTagList.size() != 0) {
                    if (!mTaggedPhotoTagsVisibilityStatusHelper.contains(feeds.feed.get(position))) {
                        postImages.showTags();
                        mTaggedPhotoTagsVisibilityStatusHelper.add(feeds.feed.get(position));
                    } else {
                        postImages.hideTags();
                        mTaggedPhotoTagsVisibilityStatusHelper.remove(feeds.feed.get(position));
                    }
                }
            }
        });
*/
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }


    public interface Listner {
        void onSingleTap();
        void onDoubleTap();
        void onLongPress();
    }

    private class MyOnDoubleTapListener extends OnDoubleTapListener {
        private MyOnDoubleTapListener(Context c) {
            super(c);
        }

        @Override
        public void onClickEvent(MotionEvent e) {
            if (listner != null)
                listner.onSingleTap();
        }

        @Override
        public void onDoubleTap(MotionEvent e) {
            if (listner != null)
                listner.onDoubleTap();
        }
    }

/*    private InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {
        @Override
        public void singleTapConfirmedAndRootIsInTouch(int x, int y) {

        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }
    };*/


    public Point getDisplaySize(DisplayMetrics displayMetrics) {
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return new Point(width, height);
    }
}
