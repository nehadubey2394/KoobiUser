package com.mualab.org.user.dialogs;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.mualab.org.user.R;

/**
 * Created by dharmraj on 27/12/17.
 **/

public class ForgotPassword implements View.OnClickListener{

    private Dialog dialog;
    private Context mContext;
    private Listner listner;

    public ForgotPassword(Context context, final Listner listner) {
        this.listner = listner;
        this.mContext = context;
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_forgot_password);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        AppCompatButton btnSubmit =  dialog.findViewById(R.id.btnSubmit);
        ImageView ivBg =  dialog.findViewById(R.id.ivBg);
        LinearLayout llView =  dialog.findViewById(R.id.llView);
        ImageView ivBack =  dialog.findViewById(R.id.ivBack);

        btnSubmit.setOnClickListener(this);
        ivBg.setOnClickListener(this);
        llView.setOnClickListener(this);
        ivBack.setOnClickListener(this);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                View view = dialog.getWindow().getDecorView();
                //for enter from left
                //ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();

                //for enter from bottom
                ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }

        });
    }


    public void show() {
        if (dialog != null)
            dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btnSubmit:
                EditText edEmail =  dialog.findViewById(R.id.edEmail);
                String email = edEmail.getText().toString().trim();
                if(TextUtils.isEmpty(email)){
                    showToast(mContext.getString(R.string.error_required_field));
                }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showToast(mContext.getString(R.string.error_invalid_email));
                } else{
                    ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.VISIBLE);
                   // progressBar.getProgressDrawable().setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                    listner.onSubmitClick(dialog, email);
                }
                break;

            case R.id.ivBg:
               // listner.onDismis(dialog);
                break;

            case R.id.ivBack:
                listner.onDismis(dialog);
                break;
        }
    }


    private void showToast(String str){
        MyToast.getInstance(mContext).showSmallCustomToast(str);
        //Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }


    public interface Listner {
        void onSubmitClick(Dialog dialog, String string);
        void onDismis(Dialog dialog);
    }
}
