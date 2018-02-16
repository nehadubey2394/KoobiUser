package com.mualab.org.user.dialogs;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 21/12/17.
 */

public class Progress {

    private View progressBarView;

    private static Progress progress;

    public Progress(Activity activity) {
        progressBarView = LayoutInflater.from(activity).inflate(R.layout.dialog_progress, null);
        activity.addContentView(progressBarView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
    }

    public Progress setText(int resourceId) {
        ((TextView) progressBarView.findViewById(R.id.progress_bar_text)).setText(resourceId);
        return this;
    }

    public Progress setText(String text) {
        ((TextView) progressBarView.findViewById(R.id.progress_bar_text)).setText(text);
        return this;
    }

    public static void show(Context context) {
        //if(progress==null)
        progress = new Progress((Activity) context);
        progress.progressBarView.setVisibility(View.VISIBLE);
        //progressBarView.setVisibility(View.VISIBLE);
    }

    public static void hide(Context context) {
        if(progress!=null)
            progress.progressBarView.setVisibility(View.GONE);
        //progressBarView.setVisibility(View.GONE);
    }

    public View getProgressBarView() {
        return progressBarView;
    }

    public void setProgressBarView(View progressBarView) {
        this.progressBarView = progressBarView;
    }

}
