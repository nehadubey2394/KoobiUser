/*
 * Copyright (c) 2017.  Joe
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.refreshview;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * Created by Dharmraj Acharya on 2018/3/24.
 * Email dharmrajacharya@gmail.com
 */

public class CircleHeaderView extends FrameLayout implements HeaderListener {

    public TouchCircleView getmHeader() {
        return mHeader;
    }

    private final TouchCircleView mHeader;

    public CircleHeaderView(Context context) {
        super(context);
        float density = context.getResources().getDisplayMetrics().density;
        mHeader = new TouchCircleView(context);
        addView(mHeader, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (density * 150)));
    }

    /**
     *
     * @param scrollY
     */
    @Override
    public void onRefreshBefore(int scrollY, int headerHeight) {
        //Log.e("TAG", "onRefreshBefore: " + scrollY);
        mHeader.handleOffset(-scrollY);
//        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(current));

    }

    /**
     *
     * @param scrollY
     */
    @Override
    public void onRefreshAfter(int scrollY, int headerHeight) {
//        mHeaderImageView.setBackgroundDrawable(mFrameAnimation.getFrame(mNumberOfFrames - 1));
    }

    /**
     *
     * @param scrollY
     */
    @Override
    public void onRefreshReady(int scrollY, int headerHeight) {
        mHeader.setRefresh(true);
    }

    /**
     *
     * @param scrollY
     */
    @Override
    public void onRefreshing(int scrollY, int headerHeight) {
        mHeader.setRefresh(true);
    }

    /**
     * this method will callback not only once.
     */
    @Override
    public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {
        if (isRefreshSuccess) {
            mHeader.setRefreshSuccess();
        } else {
            mHeader.setRefreshError();
        }

    }

    /**
     *
     * @param scrollY
     */
    @Override
    public void onRefreshCancel(int scrollY, int headerHeight) {

    }

    @Override
    public int getRefreshHeight() {
        return (int) (getMeasuredHeight()*0.8f);
    }

}
