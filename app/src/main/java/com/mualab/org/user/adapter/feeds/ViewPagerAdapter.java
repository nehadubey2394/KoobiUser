package com.mualab.org.user.adapter.feeds;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mualab.org.user.R;
import com.mualab.org.user.listner.OnDoubleTapListener;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 */

public class ViewPagerAdapter extends PagerAdapter {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private List<String> ImagesList;
    private Listner listner;
    private MyOnDoubleTapListener tapListener;


    public ViewPagerAdapter(Context context, List<String> imagesList, Listner listner) {
        this.context = context;
        this.ImagesList = imagesList;
        this.listner = listner;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tapListener = new MyOnDoubleTapListener(context);
    }

    @Override
    public int getCount() {
        return ImagesList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_layout, container, false);
        itemView.setOnTouchListener(tapListener);
        ImageView postImages = (ImageView) itemView.findViewById(R.id.post_image);

        Picasso.with(context)
                .load(ImagesList.get(position))
                .fit()
                .placeholder(R.drawable.gallery_placeholder)
                .into(postImages);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    public interface Listner {
        void onSingleTap();
        void onDoubleTap();
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

}
