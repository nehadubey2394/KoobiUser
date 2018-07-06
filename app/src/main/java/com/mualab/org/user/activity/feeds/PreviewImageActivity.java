package com.mualab.org.user.activity.feeds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
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
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class PreviewImageActivity extends AppCompatActivity {

    private int startIndex;
    private LinearLayout ll_Dot;
    private List<String> images;
    private ViewPager viewPager;
    private ViewPagerAdapterForfullScreen viewPagerAdapter;

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


        viewPager = findViewById(R.id.viewpager);
        viewPagerAdapter = new ViewPagerAdapterForfullScreen(this, images);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ll_Dot = null;
        viewPager.destroyDrawingCache();
        viewPager = null;
        viewPagerAdapter = null;
    }

    class ViewPagerAdapterForfullScreen extends PagerAdapter {

        LayoutInflater mLayoutInflater;
        Context context;
        List<String> imagesList;


        private ViewPagerAdapterForfullScreen(Context context, List<String> imagesList) {
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


      /*  private class ImageViewTarget implements Target {

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
        }*/
    }
}
