package com.mualab.org.user.activity.story;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.story.fragment.StoryFragment;
import com.mualab.org.user.model.feeds.LiveUserInfo;
import com.mualab.org.user.util.transformers.CubeTransformer;
import java.util.ArrayList;
import java.util.List;


public class StoreViewActivity extends AppCompatActivity implements StoryFragment.StoryListiner {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    public ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    public PagerAdapter mPagerAdapter;

    private List<Fragment> fragments = new ArrayList<>();
    private int currentIndex;
    private int totalIndex;
    private boolean isImmersive = true;

    private List<LiveUserInfo> liveUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_story_view);


        Bundle args = getIntent().getBundleExtra("BUNDLE");
        if (args != null) {
            liveUserList = (ArrayList<LiveUserInfo>) args.getSerializable("ARRAYLIST");
            currentIndex = args.getInt("position");
        } else finish();


        totalIndex = liveUserList.size();

        for(int i=0; i<totalIndex; i++){
            fragments.add(StoryFragment.newInstance(liveUserList.get(i), i));
        }

        mPager = findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setPageTransformer(false, new CubeTransformer());
        mPager.setAdapter(mPagerAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        mPager.canScrollHorizontally(0);
        mPager.setCurrentItem(currentIndex);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isImmersive && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    @Override
    public void onNext() {
        ++currentIndex;
        if(currentIndex>totalIndex){
            finish();
        }
        else mPager.setCurrentItem(currentIndex);
    }

    @Override
    public void onPrev() {
        if (currentIndex - 1 < 0) return;
        --currentIndex;
        mPager.setCurrentItem(currentIndex);
    }

    @Override
    public void onFinish() {
        finish();
    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}

