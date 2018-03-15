package com.mualab.org.user.dialogs;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.model.FeedLike;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dharmraj on 15/3/18.
 */

public class UnfollowDialog {


    private UnfollowListner listner;

    public interface UnfollowListner{
        void onUnfollowClick();
    }

    public UnfollowDialog(Context context, FeedLike feedLike, final UnfollowListner listner) {
        this.listner = listner;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_unfollow);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_cancel =  dialog.findViewById(R.id.tv_cancel);
        TextView tvUnfollow =  dialog.findViewById(R.id.tvUnfollow);
        TextView tv_user_name =  dialog.findViewById(R.id.tv_user_name);
        CircleImageView iv_profileImage =  dialog.findViewById(R.id.iv_profileImage);

        if(!TextUtils.isEmpty(feedLike.profileImage))
            Picasso.with(context).load(feedLike.profileImage).into(iv_profileImage);

        tv_user_name.setText(String.format(context.getString(R.string.unfollow_user), feedLike.userName));
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });


        tvUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.onUnfollowClick();
            }
        });

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

}
