package com.mualab.org.user.activity.explore;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.fragment.ExploreTopFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.listner.SearchViewListner;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.ScreenUtils;
import com.mualab.org.user.utils.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;


public class ExplorSearchActivity extends AppCompatActivity  {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    private TabLayout tabLayout;
    private SearchView searchview;
    public static SearchViewListner searchViewListner;
    public static String searchKeyword;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private List<MyViews> views = new ArrayList<>();
    int fragCount;

    class MyViews{
        Fragment fragment;
        String title;

        MyViews(String title, Fragment fragment){
            this.title = title;
            this.fragment = fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explor_search);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));
        searchKeyword = "";
        searchview = findViewById(R.id.searchview);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.viewpager);
        tabLayout  = findViewById(R.id.tablayout);

        views.add(new MyViews("Top", ExploreTopFragment.newInstance("top")));
        views.add(new MyViews("People",ExploreTopFragment.newInstance("people")));
        views.add(new MyViews("Hashtag",ExploreTopFragment.newInstance("hasTag")));
        views.add(new MyViews("Service Tag",ExploreTopFragment.newInstance("serviceTag")));
        views.add(new MyViews("Location",ExploreTopFragment.newInstance("place")));

        KeyboardUtil.hideKeyboard(searchview, this);
        //searchview.requestFocus();

        mPager.setOffscreenPageLimit(views.size());
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        tabLayout.setupWithViewPager(mPager);


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




    }

    @Override
    protected void onStart() {
        super.onStart();

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchKeyword = newText;
                if(searchViewListner!=null)
                    searchViewListner.onTextChange(newText);
                return false;
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtil.hideKeyboard(searchview, ExplorSearchActivity.this);
                onBackPressed();
            }
        });

        final View activityRootView = findViewById(R.id.activityRoot);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > ScreenUtils.convertDpToPixel(200, ExplorSearchActivity.this)) { // if more than 200 dp, it's probably a keyboard...
                    // ... do something here
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mualab.getInstance().cancelAllPendingRequests();
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
            return views.get(position).fragment;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return views.get(position).title;
        }
    }
}
