package com.mualab.org.user.activity.feeds.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.application.Mualab;


public class AddFeedFragment extends BaseFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;


    public AddFeedFragment() {
        // Required empty public constructor
    }


    public static AddFeedFragment newInstance(String param1, String param2) {
        AddFeedFragment fragment = new AddFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_feeds, container, false);
        initView(rootView);
        // Inflate the layout for this fragment
        return rootView;
    }

    private void initView(View rootView){
        rootView.findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Mualab.getInstance().getSessionManager().logout();
            }
        });
    }

}
