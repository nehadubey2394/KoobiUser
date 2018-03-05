package com.mualab.org.user.activity.feeds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import views.scaleview.ImageSource;
import views.scaleview.ScaleImageView;

public class PreviewImageActivity extends AppCompatActivity {


    int startIndex;
    private LinearLayout ll_Dot;
    private List<String> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);

        Intent intent = getIntent();
        if (intent != null) {
            startIndex = intent.getIntExtra("startIndex", 0);
            // feedImages = (ArrayList<Feeds>) getIntent().getSerializableExtra("imageArray");
            images = (ArrayList<String>) getIntent().getSerializableExtra("imageArray");
        }

        ll_Dot = findViewById(R.id.ll_Dot);
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        ViewPager viewPager = findViewById(R.id.viewpager);
        ViewPagerAdapterForfullScreen viewPagerAdapter = new ViewPagerAdapterForfullScreen(this, images);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(startIndex);


        if (images.size() > 1) {
            addBottomDots(ll_Dot, images.size(), startIndex);
            ll_Dot.setVisibility(View.VISIBLE);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    addBottomDots(ll_Dot, images.size(), position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

        } else ll_Dot.setVisibility(View.GONE);
    }

    private void addBottomDots(LinearLayout ll_dots, int totalSize, int currentPage) {
        TextView[] dots = new TextView[totalSize];
        ll_dots.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("•"));
            dots[i].setTextSize(25);
            dots[i].setTextColor(Color.parseColor("#999999"));
            ll_dots.addView(dots[i]);
        }
        if (dots.length > 0)
            dots[currentPage].setTextColor(Color.parseColor("#212121"));
    }


    class ViewPagerAdapterForfullScreen extends PagerAdapter {

        LayoutInflater mLayoutInflater;
        Context context;
        List<String> imagesList;


        public ViewPagerAdapterForfullScreen(Context context, List<String> imagesList) {
            this.context = context;
            this.imagesList = imagesList;
            this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return imagesList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {


            View itemView = mLayoutInflater.inflate(R.layout.viewpager_zooming_layout, container, false);
            final ProgressBar progress_bar = itemView.findViewById(R.id.progress_bar);
            final LinearLayout ll_Dot = itemView.findViewById(R.id.ll_Dot);
            final ScaleImageView photoView = itemView.findViewById(R.id.photo_view);
            Picasso.with(context).load(imagesList.get(position)).into(new ImageViewTarget(photoView, progress_bar));

/*
            Picasso.with(context)
                    .load(String.valueOf(imagesList.get(position)))
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(photoView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            progress_bar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progress_bar.setVisibility(View.GONE);
                        }
                    });*/

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            container.addView(itemView);
            return itemView;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


        private class ImageViewTarget implements Target {

            private WeakReference<ScaleImageView> mImageViewReference;
            private WeakReference<ProgressBar> mProgressBarReference;

            public ImageViewTarget(ScaleImageView imageView, ProgressBar progressBar) {
                this.mImageViewReference = new WeakReference<>(imageView);
                this.mProgressBarReference = new WeakReference<>(progressBar);
            }


            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                //you can use this bitmap to load image in image view or save it in image file like the one in the above question.
                ScaleImageView imageView = mImageViewReference.get();
                if (imageView != null) {
                    imageView.setImage(ImageSource.bitmap(bitmap));
                }

                ProgressBar progressBar = mProgressBarReference.get();
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                ScaleImageView imageView = mImageViewReference.get();
                if (imageView != null) {
                    imageView.setImage(ImageSource.resource(R.drawable.logo_small));
                }

                ProgressBar progressBar = mProgressBarReference.get();
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

                ScaleImageView imageView = mImageViewReference.get();
                if (imageView != null) {
                    //imageView.setImage(null);
                }

                ProgressBar progressBar = mProgressBarReference.get();
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
