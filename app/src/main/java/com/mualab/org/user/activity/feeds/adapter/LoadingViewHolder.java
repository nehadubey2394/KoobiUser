package com.mualab.org.user.activity.feeds.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.mualab.org.user.R;

/**
 */

public class LoadingViewHolder extends RecyclerView.ViewHolder{
    public ProgressBar progressBar;
    public LoadingViewHolder(View view) {
        super(view);
        progressBar = view.findViewById(R.id.ProgressBar);
    }
}
