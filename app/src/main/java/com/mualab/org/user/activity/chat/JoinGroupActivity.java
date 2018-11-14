package com.mualab.org.user.activity.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mualab.org.user.R;

public class JoinGroupActivity extends AppCompatActivity /*implements View.OnClickListener*/ {
    private TextView tv_no_chat;
    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
       // init();
    }
  /*  private void init(){
        tv_no_chat = findViewById(R.id.tv_no_chat);
        progress_bar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvChatTitle = findViewById(R.id.tvChatTitle);

        tvChatTitle.setText("Join New Group");
        btnBack.setOnClickListener(this);
    }*/

  /*  @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }*/
}
