package com.mualab.org.user.dialogs;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 24/3/18.
 */

public class DialogImageDetail implements View.OnClickListener{
    private Dialog dialog;
    private static boolean isDialogAlreadyCreate;
    //private Listner listner;

    public DialogImageDetail(Context context) {
        //this.listner = listner;

        if(isDialogAlreadyCreate)
            return;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_image_detail_view);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        AppCompatButton btnRetry =  dialog.findViewById(R.id.btnRetry);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View view = dialog.getWindow().getDecorView();
                //for enter from left
                ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });
    }


    public void show() {
        if (dialog != null && !isDialogAlreadyCreate){
            dialog.show();
            isDialogAlreadyCreate = true;
        }

    }

    public void dismiss() {
        if (dialog != null)
            dialog.dismiss();
        isDialogAlreadyCreate = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnRetry:
                dialog.dismiss();
                isDialogAlreadyCreate = false;
                // listner.onNetworkChange(dialog, ConnectionDetector.isConnected());
                break;
        }
    }

    public interface Listner {
        void onNetworkChange(Dialog dialog, boolean isConnected);
    }

}
