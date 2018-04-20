package com.mualab.org.user.activity.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.MainActivity;

/**
 * Created by dharmraj on 19/3/18.
 */

public class BaseFragment extends Fragment {

    public static final String ARGS_INSTANCE = "com.mualab.org.user";

    private BaseActivity mActivity;
    protected Callback mFragmentNavigation;
    protected Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) context;
            this.mActivity = activity;
            activity.onFragmentAttached();
        }

        if (context instanceof Callback) {
            mFragmentNavigation = (Callback) context;
        }

        if(context instanceof MainActivity)
            ((MainActivity)context).setBgColor(R.color.white);
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    public BaseActivity getBaseActivity() {
        return mActivity;
    }


    public void hideKeyboard() {
        if (mActivity != null) {
            mActivity.hideKeyboard();
        }
    }

    public boolean isNetworkConnected() {
        return mActivity != null && mActivity.isNetworkConnected();
    }

    public void openActivityOnTokenExpire() {
        if (mActivity != null) {
            mActivity.openActivityOnTokenExpire();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public interface Callback {

        void onFragmentAttached();

        void onFragmentDetached(String tag);

        void pushFragment(Fragment fragment);
    }
}
