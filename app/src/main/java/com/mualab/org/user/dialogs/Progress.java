package com.mualab.org.user.dialogs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 21/12/17.
 */

public class Progress extends Dialog{

    private View progressBarView;
    private  Context context;
    private static Progress progress;

    // final ProgressDialog customDialog = new ProgressDialog(LoginActivity.this);
    //    customDialog.show();

    private Progress(Context context) {
        super(context, android.R.style.Theme_Translucent);

        this.context = context;
        // This is the layout XML file that describes your Dialog layout
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.custom_progress_dialog_layout);
    }

 /*   private Progress(Activity activity) {
        progressBarView = LayoutInflater.from(activity).inflate(R.layout.custom_progress_dialog_layout, null);
        activity.addContentView(progressBarView, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT));
    }
*/


   /* public Progress setText(int resourceId) {
        ((TextView) progressBarView.findViewById(R.id.progress_bar_text)).setText(resourceId);
        return this;
    }

    public Progress setText(String text) {
        ((TextView) progressBarView.findViewById(R.id.progress_bar_text)).setText(text);
        return this;
    }*/

    public static void show(Context context) {
        //if(progress==null)
        progress = new Progress(context);
        progress.show();
        //  progress = new Progress((Activity) context);
        //   progress.progressBarView.setVisibility(View.VISIBLE);
    }

    public static void hide(Context context) {
        if(progress!=null)
            progress.dismiss();
        // progress.progressBarView.setVisibility(View.GONE);
    }

    public View getProgressBarView() {
        return progressBarView;
    }

    public void setProgressBarView(View progressBarView) {
        this.progressBarView = progressBarView;
    }

}
