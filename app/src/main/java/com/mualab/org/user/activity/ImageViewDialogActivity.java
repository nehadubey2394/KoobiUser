package com.mualab.org.user.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.mualab.org.user.R;
import com.mualab.org.user.model.feeds.Feeds;
import com.squareup.picasso.Picasso;

public class ImageViewDialogActivity extends AppCompatActivity {

    private Feeds feed;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_dialog);


        Bundle bundle = getIntent().getExtras();

        if(bundle!=null){
            feed = (Feeds) bundle.getSerializable("feed");
            index =  bundle.getInt("index");
        }

        ImageView postImage = findViewById(R.id.ivFeedCenter);
        if(TextUtils.isEmpty(feed.profileImage))
            Picasso.with(this).load(R.drawable.defoult_user_img).noPlaceholder().into(postImage);
        else Picasso.with(this).load(feed.profileImage).noPlaceholder().into(postImage);

        findViewById(R.id.rootLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
