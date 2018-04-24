package com.mualab.org.user.activity.gellery;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 19/3/18.
 */

public class BaseGalleryFragment extends Fragment{

   // protected CoordinatorLayout.Behavior behavior;
    protected Context context;
    protected GalleryActivity activity;
    private OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if(context instanceof GalleryActivity)
            activity = (GalleryActivity) context;

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
/*
        if(behavior != null)
            return;
        FrameLayout layout = activity.findViewById(R.id.vRootView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layout.getLayoutParams();
        behavior = params.getBehavior();
        params.setBehavior(null);*/
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*if(behavior != null)
            return;
        FrameLayout layout = activity.findViewById(R.id.vRootView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layout.getLayoutParams();
        behavior = params.getBehavior();
        params.setBehavior(null);*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
     /*   if(behavior == null)
            return;

        FrameLayout layout = activity.findViewById(R.id.vRootView);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layout.getLayoutParams();
        params.setBehavior(behavior);
        layout.setLayoutParams(params);
        behavior = null;*/
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
