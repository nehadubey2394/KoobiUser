package com.mualab.org.user.activity.booking;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.fragment.booking.BookingFragment1;
import com.mualab.org.user.fragment.booking.BookingFragment2;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.squareup.picasso.Picasso;

public class BookingActivity extends AppCompatActivity implements View.OnClickListener{
    private ArtistsSearchBoard item;
    private String mParam1;
    public static TextView title_booking;
    public static LinearLayout lyReviewPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        Intent i = getIntent();
        item =  i.getParcelableExtra("item");
        mParam1 = i.getStringExtra("mParam");

        initView();
    }

    private void initView(){

        title_booking = findViewById(R.id.tvHeaderTitle2);
        lyReviewPost = findViewById(R.id.lyReviewPost);
        lyReviewPost.setVisibility(View.VISIBLE);
        TextView tvArtistName = findViewById(R.id.tvArtistName);
        TextView tvOpeningTime = findViewById(R.id.tvOpeningTime);
        RatingBar rating = findViewById(R.id.rating);
        ImageButton ibtnChat2 = findViewById(R.id.ibtnChat2);
        ImageView ivHeaderUser2 = findViewById(R.id.ivHeaderUser2);
        ImageView ivHeaderBack2 = findViewById(R.id.ivHeaderBack2);
        ImageView ivHeaderProfile = findViewById(R.id.ivHeaderProfile);
        title_booking.setText(getString(R.string.title_booking));
        rating.setRating(3);
        tvArtistName.setText(item.userName);
        if (!item.profileImage.equals(""))
            Picasso.with(BookingActivity.this).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivHeaderProfile);

        tvOpeningTime.setOnClickListener(this);
        if (mParam1.equals("1")){
            addFragment(new BookingFragment2(), false, R.id.flBookingContainer);
        }else {
            addFragment(new BookingFragment1(), false, R.id.flBookingContainer);
        }

        ivHeaderBack2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivHeaderBack2 :
                onBackPressed();
                break;
        }
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_in,0,0);
            transaction.add(containerId, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }

    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        while (i > 0) {
            fm.popBackStackImmediate();
            i--;
        }
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }


    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
