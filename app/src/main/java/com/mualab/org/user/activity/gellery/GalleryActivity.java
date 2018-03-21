package com.mualab.org.user.activity.gellery;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.mualab.org.user.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private static int lastFragmentIndex;
    private ViewPager viewPager;
    private TabLayout tablayout;
    private FrameLayout vRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        viewPager = findViewById(R.id.pager);
        vRootView = findViewById(R.id.vRootView);
        tablayout = findViewById(R.id.tablayout);
        viewPager.setOffscreenPageLimit(3);
        setupViewPager(viewPager);


        tablayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                viewPager.getParent().requestDisallowInterceptTouchEvent(true);
                lastFragmentIndex = position;
            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(lastFragmentIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPager = null;
        vRootView = null;
        tablayout = null;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment(), "Image");
        adapter.addFragment(new GalleryFragment(), "Video");
        adapter.addFragment(new GalleryFragment(), "Camera");
        // adapter.addFragment(CameraRecFragment.newInstance("",""), "VIDEO");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
