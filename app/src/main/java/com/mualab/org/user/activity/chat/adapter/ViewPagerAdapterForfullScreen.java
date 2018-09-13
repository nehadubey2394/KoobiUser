package com.mualab.org.user.activity.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mualab.org.user.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

class ViewPagerAdapterForfullScreen extends PagerAdapter {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private List<String> imagesList;


    ViewPagerAdapterForfullScreen(Context context, List<String> imagesList) {
        this.context = context;
        this.imagesList = imagesList;
        this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imagesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {


        View itemView = mLayoutInflater.inflate(R.layout.viewpager_zooming_layout, container, false);
        final ProgressBar progress_bar = itemView.findViewById(R.id.progress_bar);
        //final LinearLayout ll_Dot = itemView.findViewById(R.id.ll_Dot);
        final ImageView photoView = itemView.findViewById(R.id.photo_view);
        //Picasso.with(context).load(imagesList.get(position)).into(new ImageViewTarget(photoView, progress_bar));

        final String url = String.valueOf(imagesList.get(position));
        Picasso.with(context)
                .load(url)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progress_bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(url)
                                .fit()
                                .placeholder(R.drawable.gallery_placeholder)
                                .error(R.drawable.gallery_placeholder).into(photoView);
                        progress_bar.setVisibility(View.GONE);
                    }
                });

        container.addView(itemView);
        return itemView;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

}