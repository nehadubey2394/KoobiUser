package com.mualab.org.user.activity;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 19/3/18.
 */

public class BaseFragment extends Fragment {

    public static final String ARGS_INSTANCE = "com.mualab.org.user";

    protected FragmentNavigation mFragmentNavigation;
    protected Context mContext;

    /*@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof FragmentNavigation) {
            mFragmentNavigation = (FragmentNavigation) context;
        }

        if(context instanceof MainActivity)
            ((MainActivity)context).setBgColor(R.color.white);
    }

    public interface FragmentNavigation {
        void pushFragment(Fragment fragment);
    }
}
