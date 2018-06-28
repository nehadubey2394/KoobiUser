package com.mualab.org.user.activity.authentication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.ForgotPassword;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.dialogs.MySnackBar;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.local.prefs.SharedPreferanceUtils;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.StatusBarUtil;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextView ed_username, ed_password;
   // private TextInputLayout input_layout_UserName, input_layout_password;
    private ImageView ivFacebook, ivInstragram;
    private SharedPreferanceUtils sp;
    private Session session;
    //private FirebaseAuth mAuth;
    //private DatabaseReference mDatabase;

    private boolean isRemind = true;
    private boolean doubleBackToExitPressedOnce;
    private Runnable runnable;

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    private void initView(){
        ed_username = findViewById(R.id.ed_username);
        ed_password = findViewById(R.id.ed_password);
        /*input_layout_UserName = findViewById(R.id.input_layout_UserName);
        input_layout_password = findViewById(R.id.input_layout_password);*/
        ivFacebook = findViewById(R.id.ivFacebook);
        ivInstragram = findViewById(R.id.ivInstragram);
       // ed_password.addTextChangedListener(new MyTextWatcher(ed_password));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));
        sp = new SharedPreferanceUtils(this);
        session = Mualab.getInstance().getSessionManager();
        initView();


        if ((Boolean) sp.getParam(Constant.isLoginReminder, Boolean.FALSE)) {
            isRemind = true;
            ed_username.setText(String.valueOf(sp.getParam(Constant.USER_ID, "")));
            ed_password.setText(String.valueOf(sp.getParam(Constant.USER_PASSWORD, "")));
        }


        ivFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(getString(R.string.under_development));
            }
        });

        ivInstragram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(getString(R.string.under_development));
            }
        });


        findViewById(R.id.tvCustomerApp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPackageName = "com.mualab.org.biz";//getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    showToast(getString(R.string.under_development));
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }


            }
        });


        findViewById(R.id.tvForgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ForgotPassword(LoginActivity.this, new ForgotPassword.Listner() {
                    @Override
                    public void onSubmitClick(final Dialog dialog, final String emailId) {
                        forgotPassword(dialog, emailId);
                        /*Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toast.makeText(LoginActivity.this, string, Toast.LENGTH_SHORT).show();
                            }
                        }, 4000);*/
                    }

                    @Override
                    public void onDismis(Dialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });


        final AppCompatButton btn_login =  findViewById(R.id.btn_login);
        //SetFont.setfontRagular(btn_login, this);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtil.hideKeyboard(btn_login, LoginActivity.this);
                loginProcess();
            }
        });



        findViewById(R.id.createNewAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,  RegistrationActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });


        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        switch (resultCode) {
            case 0:
                //mAuth = FirebaseAuth.getInstance();
                //mDatabase = FirebaseDatabase.getInstance().getReference();
                break;
            case 2:
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 0);
                if (dialog != null)
                    dialog.show();
        }

    }

    private void loginProcess(){
        boolean isValidInput = true;
        String username = ed_username.getText().toString().trim();
        String password = ed_password.getText().toString().trim();
        String deviceToken = FirebaseInstanceId.getInstance().getToken();//"androidTest";

        if (!validateName() || !validatePassword()) {
            isValidInput = false;
        }

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(LoginActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        loginProcess();
                    }

                }
            }).show();

            isValidInput = false;
        }

        if (isValidInput) {
            Map<String, String> params = new HashMap<>();
            params.put("userName", username);
            params.put("password", password);
            params.put("deviceToken",deviceToken);
            params.put("firebaseToken", deviceToken);
            params.put("deviceType", "2");
            params.put("appType", "user");
            params.put("userType", "user");

            new HttpTask(new HttpTask.Builder(this, "userLogin", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {
                            Gson gson = new Gson();
                            JSONObject userObj = js.getJSONObject("users");
                            User user = gson.fromJson(String.valueOf(userObj), User.class);
                            session.createSession(user);
                            session.setPassword(user.password);
                            checkUserRember(user);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("user", user);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        }
                        showToast(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                }})
                    .setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                    .setProgress(true))
                    .execute(this.getClass().getName());
        }
    }


    private void forgotPassword(final Dialog dialog, String emailId){

        Map<String, String> map = new HashMap<>();
        map.put("email", emailId);
        new HttpTask(new HttpTask.Builder(this, "forgotPassword", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    dialog.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");

                    if(status.equalsIgnoreCase("success")){
                        MyToast.getInstance(LoginActivity.this).showDasuAlert(status, message);
                        dialog.dismiss();
                    }else {
                        showToast(message);
                    }
                } catch (JSONException e) {
                    showToast(getString(R.string.msg_some_thing_went_wrong));
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                dialog.findViewById(R.id.progress_bar).setVisibility(View.GONE);
                showToast(getString(R.string.msg_some_thing_went_wrong));
            }
        }).setParam(map).setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED)).execute("forgotPassword");
    }

    private void checkUserRember(User user){
        if (isRemind) {
            sp.setParam(Constant.isLoginReminder, true);
            sp.setParam(Constant.USER_ID, user.userName);
            sp.setParam(Constant.USER_PASSWORD, ed_password.getText().toString());
        } else {
            sp.setParam(Constant.isLoginReminder, false);
            sp.setParam(Constant.USER_ID, "");
            sp.setParam( Constant.USER_PASSWORD, "");
        }
    }

    private boolean validateName() {
        if (ed_username.getText().toString().trim().isEmpty()) {
            //input_layout_UserName.setError(getString(R.string.error_email_or_username_required));
            ed_username.requestFocus();
            showToast(getString(R.string.error_email_or_username_required));
            return false;
        } /*else {
            input_layout_UserName.setErrorEnabled(false);
        }*/

        return true;
    }

    private boolean validatePassword() {
        String password = ed_password.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            //input_layout_password.setError(getString(R.string.error_password_required));
            showToast(getString(R.string.error_password_required));
            ed_password.requestFocus();
            return false;
        } else if (password.length() < 8) {
            //input_layout_password.setError(getString(R.string.error_invalid_password_length));
            showToast(getString(R.string.error_invalid_password_length));
            ed_password.requestFocus();
            return false;
        } /*else {
            input_layout_password.setErrorEnabled(false);
        }*/
        return true;
    }


    @Override
    public void onBackPressed() {
        Handler handler = new Handler();
        if (!doubleBackToExitPressedOnce) {
            doubleBackToExitPressedOnce = true;
            MySnackBar.showSnackbar(this, findViewById(R.id.snackBarView), "Click again to exit");
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        } else {
            handler.removeCallbacks(runnable);
            super.onBackPressed();
        }
    }

    private void showToast(String msg) {
        if(!TextUtils.isEmpty(msg))
            MyToast.getInstance(this).showDasuAlert(msg);
        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


   /* public void firebaseLoginRegistration(final User user) {

        mAuth.signInWithEmailAndPassword(user.email, "123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user.fireBaseId = task.getResult().getUser().getUid();
                            session.createSession(user, true);
                            goNextActivity();
                        } else if (!task.isSuccessful()) {
                            fireBaseRegistrationTask(user);
                        }

                    }
                });

    }

    public void fireBaseRegistrationTask(final User user) {

        mAuth.createUserWithEmailAndPassword(user.email, "123456")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user.fireBaseId = task.getResult().getUser().getUid();
                            session.createSession(user, true);
                            session.setPassword(ed_password.getText().toString().trim());
                            mDatabase.child("users").child(user.fireBaseId).setValue(user);
                            goNextActivity();

                        } *//*else {
                            Toast.makeText(LoginActivity.this, "in firebase already register with this email id.", Toast.LENGTH_SHORT).show();
                        }*//*
                    }
                });
    }*/

   /* private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.ed_username:
                    validateName();
                    break;
                case R.id.ed_password:
                    validatePassword();
                    break;
            }
        }
    }*/
}
