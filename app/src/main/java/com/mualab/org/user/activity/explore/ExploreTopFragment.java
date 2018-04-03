package com.mualab.org.user.activity.explore;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mualab.org.user.R;

/**
 * Dharmraj Acharya
 */
public class ExploreTopFragment extends Fragment {


    public ExploreTopFragment() {
        // Required empty public constructor
    }


    public static ExploreTopFragment newInstance() {
        ExploreTopFragment fragment = new ExploreTopFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_top, container, false);
    }

}
