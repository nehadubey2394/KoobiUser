package com.mualab.org.user.activity.payment.activity;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.model.BookingHistory;
import com.mualab.org.user.activity.payment.fragment.BankPayFragment;
import com.mualab.org.user.activity.payment.fragment.CardPayFragment;
import com.mualab.org.user.utils.KeyboardUtil;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout btnCard,btnBank;
    //  private BookingHistory bookingHistory;
    private TextView tvCard,tvBank;
    private int clickId = 1;
    private String bookingId,totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Intent intent = getIntent();
        if (intent!=null){
            bookingId =  intent.getStringExtra("bookingId");
            totalPrice =  intent.getStringExtra("totalPrice");
        }
       /* Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bookingHistory = (BookingHistory) extras.getSerializable("itemDetail");
        }*/
        init();
    }

    private void init() {
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.payment_checkout));
        tvCard = findViewById(R.id.tvCard);
        tvBank = findViewById(R.id.tvBank);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnCard = findViewById(R.id.btnCard);
        btnCard.setOnClickListener(this);
        btnBank = findViewById(R.id.btnBank);
        btnBank.setOnClickListener(this);

        replaceFragment(CardPayFragment.newInstance(bookingId,totalPrice), false);

    }

    public void addFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fragmentManager = getSupportFragmentManager();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_in,0,0);
            transaction.add(R.id.containerFrm, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }

    }

    public void replaceFragment(Fragment fragment, boolean addToBackStack) {
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        while (i > 0) {
            fm.popBackStackImmediate();
            i--;
        }
        boolean fragmentPopped = getFragmentManager().popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.containerFrm, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        FragmentManager fm = getSupportFragmentManager();
        if(getCurrentFocus()!=null) {
            KeyboardUtil.hideKeyboard(getCurrentFocus(),PaymentActivity.this);
        }
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCard:
                if (clickId!=1){
                    clickId = 1;
                    btnCard.setBackgroundResource(R.drawable.bg_tab_selected);
                    btnBank.setBackgroundResource(R.drawable.bg_tab_unselected);
                    tvCard.setTextColor(getResources().getColor(R.color.white));
                    tvBank.setTextColor(getResources().getColor(R.color.colorPrimary));
                    replaceFragment(CardPayFragment.newInstance(bookingId,totalPrice), false);
                }
                break;

            case R.id.btnBank:
                if (clickId!=2) {
                    clickId = 2;
                    btnBank.setBackgroundResource(R.drawable.bg_second_tab_selected);
                    btnCard.setBackgroundResource(R.drawable.bg_second_tab_unselected);
                    tvCard.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tvBank.setTextColor(getResources().getColor(R.color.white));
                    replaceFragment(BankPayFragment.newInstance(bookingId,totalPrice), false);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
