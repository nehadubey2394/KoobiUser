package com.mualab.org.user.dialogs;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 14/11/17.
 **/

public class SelectableDialog {

    private android.app.Dialog dialog;
    private Context context;
    private Listener listener;
    private String title;


   public SelectableDialog(Context context, Listener listener){
        this.context = context;
        this.listener = listener;
    }

    public void setTitle(String title){
        this.title = title;
    }


    public interface Listener{
        void onGalleryClick();
        void onCameraClick();
        void onCancel();
    }

    private void build(){
        dialog = new android.app.Dialog(context);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.setContentView(R.layout.dialog_select_videos);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView tvFromCamera = dialog.findViewById(R.id.tv_video_from_camera);
        TextView tvFromGallery = dialog.findViewById(R.id.tv_video_from_gallery);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        dialog.setCancelable(false);

        if(!TextUtils.isEmpty(title))
            tvTitle.setText(title);

        tvFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if(listener!=null)
                    listener.onCameraClick();
            }
        });
        tvFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(listener!=null)
                    listener.onGalleryClick();
            }
        });

        dialog.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(listener!=null)
                    listener.onCancel();
            }
        });

        dialog.show();
    }

    public void show(){
        build();
    }
}
