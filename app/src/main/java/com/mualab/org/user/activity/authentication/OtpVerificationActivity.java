package com.mualab.org.user.activity.authentication;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mualab.org.user.R;
import com.mualab.org.user.broadcast.OnSmsCatchListener;
import com.mualab.org.user.broadcast.SmsVerifyCatcher;
import com.mualab.org.user.dialogs.Progress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtpVerificationActivity extends AppCompatActivity {

    SmsVerifyCatcher smsVerifyCatcher;
    private TextView etFirst, etSecand, etThird, etFourth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        initView();
        //final CustomEntryEdittext otpView = findViewById(R.id.otpView);

       /* otpView.setOnFinishListerner(new CustomEntryEdittext.onFinishListerner() {
            @Override
            public void onFinish(String enteredText) {

            }
        });*/

        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String code = parseCode(message);//Parse verification code
                if(message.contains("Mualab") || message.contains("mualab")){
                    etFirst.setText(String.format("%s", code.charAt(0)));
                    etSecand.setText(String.format("%s", code.charAt(1)));
                    etThird.setText(String.format("%s", code.charAt(2)));
                    etFourth.setText(String.format("%s", code.charAt(3)));
                    Toast.makeText(OtpVerificationActivity.this, "code is:"+code, Toast.LENGTH_LONG).show();
                }

            }
        });


        findViewById(R.id.btn_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Progress.show(OtpVerificationActivity.this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Progress.hide(OtpVerificationActivity.this);
                    }
                },5000);
            }
        });
    }

    public void initView() {
        etFirst = findViewById(R.id.ed_first);
        etSecand = findViewById(R.id.ed_secand);
        etThird = findViewById(R.id.ed_third);
        etFourth = findViewById(R.id.ed_fourth);
    }

    /**
     * Parse verification code
     *
     * @param message sms message
     * @return only four numbers from massage string
     */
    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{4}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }


    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
