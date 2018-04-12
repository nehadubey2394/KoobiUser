package com.mualab.org.user.activity.explore;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.BaseFragment;
import com.mualab.org.user.activity.explore.fragment.SearchFeedFragment;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.util.FragmentHistory;
import com.mualab.org.user.util.StatusBarUtil;

import views.fragnev.FragNavController;

public class SearchFeedActivity extends AppCompatActivity implements BaseFragment.FragmentNavigation,
        FragNavController.TransactionListener, FragNavController.RootFragmentListener{

    private FragNavController mNavController;
    private FragmentHistory fragmentHistory;
    private int fragCount;
    private ExSearchTag exSearchTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_feed);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));

        Intent intent = getIntent();
        if (intent != null) {
            exSearchTag = (ExSearchTag) intent.getExtras().getSerializable("searchKey");
        }


        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fragmentHistory = new FragmentHistory();
        mNavController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.container)
                .transactionListener(this)
                .rootFragmentListener(this, 1)
                .build();

       // switchTab(0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        TextView tvTitle = findViewById(R.id.tvTitle);
        if(exSearchTag.title!=null)
            tvTitle.setText(exSearchTag.type==ExSearchTag.SearchType.HASH_TAG?
                    "#"+exSearchTag.title.replace("#",""):exSearchTag.title);
    }

    private void switchTab(int position) {
        mNavController.switchTab(position);
        updateToolbarTitle("Explore");
    }

    public void updateToolbarTitle(String title){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mualab.getInstance().cancelAllPendingRequests();
    }

    @Override
    public void onBackPressed() {

        if (!mNavController.isRootFragment()) {
            mNavController.popFragment();
        } else {

            if (fragmentHistory.isEmpty()) {
                super.onBackPressed();
            } else {

                if (fragmentHistory.getStackSize() > 1) {
                    int position = fragmentHistory.popPrevious();
                    switchTab(position);

                } else {

                    switchTab(0);
                    fragmentHistory.emptyStack();
                }
            }

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (mNavController != null) {
            mNavController.pushFragment(fragment);
        }
    }


    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            updateToolbar();
        }
    }

    private void updateToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setDisplayShowHomeEnabled(!mNavController.isRootFragment());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_icon);
    }


    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && mNavController != null) {
            updateToolbar();
        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case FragNavController.TAB1:
                return SearchFeedFragment.newInstance(exSearchTag);
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

}
