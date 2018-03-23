package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mualab.org.user.R;
import com.mualab.org.user.listner.OnDoubleTapListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import com.mualab.org.user.util.ScreenUtils;

/**
 */

public class ViewPagerAdapter extends PagerAdapter {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private List<String> ImagesList;
    private Listner listner;
    private MyOnDoubleTapListener tapListener;
    private static int widthPixels;


    public ViewPagerAdapter(Context context, List<String> imagesList, Listner listner) {
        this.context = context;
        this.ImagesList = imagesList;
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
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_layout, container, false);
        itemView.setOnTouchListener(tapListener);
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        ImageView postImages = itemView.findViewById(R.id.post_image);

        Picasso.with(context)
                .load(ImagesList.get(position))
               // .fit()
                .resize(widthPixels,widthPixels)
                .centerCrop()
                .onlyScaleDown() //
                .placeholder(R.drawable.gallery_placeholder)
                .into(postImages);
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



   public Point getDisplaySize(DisplayMetrics displayMetrics) {
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return new Point(width, height);
    }
}
