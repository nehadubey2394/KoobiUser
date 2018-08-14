package com.mualab.org.user.activity.gellery;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.gellery.fragment.CameraFragmentNew;
import com.mualab.org.user.activity.gellery.fragment.GalleryFragment;
import com.mualab.org.user.activity.gellery.fragment.VideoGalleryFragment;
import com.mualab.org.user.utils.constants.Constant;

public class Gallery2Activity extends AppCompatActivity implements View.OnClickListener{
    private TextView tvImage,tvVideo,tvCamera;
    private int clickedView = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery2);

        tvImage = findViewById(R.id.tvImage);
        tvVideo = findViewById(R.id.tvVideo);
        tvCamera = findViewById(R.id.tvCamera);

        replaceFragment(GalleryFragment.newInstance(), false, R.id.flGalleryContainer);

        tvImage.setOnClickListener(this);
        tvVideo.setOnClickListener(this);
        tvCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvCamera:
                if (clickedView!=3){
                    clickedView = 3;
                    tvImage.setTextColor(getResources().getColor(R.color.gray));
                    tvVideo.setTextColor(getResources().getColor(R.color.gray));
                    tvCamera.setTextColor(getResources().getColor(R.color.colorPrimary));
                    replaceFragment(CameraFragmentNew.newInstance(), false, R.id.flGalleryContainer);

                }
                break;
            case R.id.tvVideo:
                if (clickedView!=2){
                    clickedView = 2;
                    tvImage.setTextColor(getResources().getColor(R.color.gray));
                    tvVideo.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvCamera.setTextColor(getResources().getColor(R.color.gray));
                    replaceFragment(VideoGalleryFragment.newInstance(), false, R.id.flGalleryContainer);
                }
                break;
            case R.id.tvImage:
                if (clickedView!=1){
                    clickedView = 1;
                    tvVideo.setTextColor(getResources().getColor(R.color.gray));
                    tvCamera.setTextColor(getResources().getColor(R.color.gray));
                    tvImage.setTextColor(getResources().getColor(R.color.colorPrimary));
                    replaceFragment(GalleryFragment.newInstance(), false, R.id.flGalleryContainer);

                }
                break;
        }
    }

    public void addFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in,0,0);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK && requestCode== Constant.POST_FEED_DATA){
            //  ((GalleryActivity)context).setResult(Activity.RESULT_OK);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
