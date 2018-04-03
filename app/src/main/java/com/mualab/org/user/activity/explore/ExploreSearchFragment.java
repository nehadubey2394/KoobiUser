package com.mualab.org.user.activity.explore;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.BaseListner;
import com.mualab.org.user.util.KeyboardUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Dharmraj Acharya
 */
public class ExploreSearchFragment extends Fragment {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    private TabLayout tabLayout;
    private SearchView searchview;
    private Context mContext;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private BaseListner baseListner;

    private List<MyViews> views = new ArrayList<>();

    class MyViews{
        Fragment fragment;
        String title;

        MyViews(String title, Fragment fragment){
            this.title = title;
            this.fragment = fragment;
        }
    }



    public ExploreSearchFragment() {
        // Required empty public constructor
    }


    public static ExploreSearchFragment newInstance() {
        ExploreSearchFragment fragment = new ExploreSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views.add(new MyViews("Top",ExploreTopFragment.newInstance()));
        views.add(new MyViews("People",ExploreTopFragment.newInstance()));
        views.add(new MyViews("HashTag",ExploreTopFragment.newInstance()));
        views.add(new MyViews("ServiceTag",ExploreTopFragment.newInstance()));
        views.add(new MyViews("Location",ExploreTopFragment.newInstance()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchview = view.findViewById(R.id.searchview);
        KeyboardUtil.showKeyboard(searchview, getContext());
        searchview.requestFocus();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = view.findViewById(R.id.viewpager);
        tabLayout  = view.findViewById(R.id.tablayout);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
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


        view.findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(baseListner!=null){
                    KeyboardUtil.hideKeyboard(searchview, getContext());
                    baseListner.backPress();
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchview.clearFocus();
        KeyboardUtil.hideKeyboard(searchview, getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if(context instanceof BaseListner){
            baseListner = (BaseListner) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        baseListner = null;
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
