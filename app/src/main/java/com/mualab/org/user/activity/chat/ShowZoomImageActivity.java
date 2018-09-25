package com.mualab.org.user.activity.chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ablanco.zoomy.Zoomy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mualab.org.user.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ShowZoomImageActivity extends AppCompatActivity {
    private  String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_zoom_image);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");

        final ProgressBar progress_bar = findViewById(R.id.progress_bar);
        final ImageView photoView = findViewById(R.id.photo_view);
        final RelativeLayout rlImage = findViewById(R.id.rlImage);

        Zoomy.Builder builder = new Zoomy.Builder(ShowZoomImageActivity.this).target(photoView);
        builder.register();

        final ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Zoomy.unregister(rlImage);
            }
        });

        Glide.with(this)
                .load(url).placeholder(R.drawable.gallery_placeholder)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progress_bar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progress_bar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(photoView);

       /* Picasso.with(ShowZoomImageActivity.this).load(url).into(photoView, new Callback() {
            @Override
            public void onSuccess() {
                progress_bar.setVisibility(View.GONE);
            }
            @Override
            public void onError() {
                Picasso.with(ShowZoomImageActivity.this).load(url)
                        .fit().placeholder(R.drawable.gallery_placeholder)
                        .error(R.drawable.gallery_placeholder).
                        into(photoView);
                progress_bar.setVisibility(View.GONE);
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
