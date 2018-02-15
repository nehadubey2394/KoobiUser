package com.mualab.org.user.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mualab.org.user.activity.authentication.LoginActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.session.Session;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);
        Session session = Mualab.getInstance().getSessionManager();

        if(session.isLoggedIn()){
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }else {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }finish();
    }
}
