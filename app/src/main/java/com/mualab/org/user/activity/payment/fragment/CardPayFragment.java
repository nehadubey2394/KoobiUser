package com.mualab.org.user.activity.payment.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.payment.activity.PaymentActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class CardPayFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "CardPayFragment";
    private EditText /*cardHolderName,*/ oneTxt, twoTxt, threeTxt, fourTxt;
    private TextView expireDate;
    private EditText cvv;
    private Context mContext;
    private String expireMnth,expireYear,number,totalPrice,bookingId;
    // private Bundle bundle;
    private int width = 0, height = 0;

    public static CardPayFragment newInstance(String bookingId,String totalPrice) {
        CardPayFragment fragment = new CardPayFragment();
        Bundle args = new Bundle();
        args.putString("bookingId", bookingId);
        args.putString("totalPrice", totalPrice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_pay, container, false);
        view.setFocusableInTouchMode(true);
        view.setClickable(true);
        view.requestFocus();
        //  bundle = savedInstanceState;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            bookingId = getArguments().getString("bookingId");
            totalPrice = getArguments().getString("totalPrice");
        }

        initializeView(view);

        oneTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4) {
                    twoTxt.requestFocus();
                }
            }
        });

        twoTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4) {
                    threeTxt.requestFocus();
                }
            }
        });

        threeTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4) {
                    fourTxt.requestFocus();
                }
            }
        });
    }

    private void initializeView(View view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
        //cardHolderName = (EditText) view.findViewById(R.id.cardHolderName);
        expireDate = view.findViewById(R.id.expireDate);
        TextView showTxt = view.findViewById(R.id.showTxt);
        oneTxt = view.findViewById(R.id.oneTxt);
        twoTxt = view.findViewById(R.id.twoTxt);
        threeTxt = view.findViewById(R.id.threeTxt);
        cvv = view.findViewById(R.id.cvv);
        fourTxt = view.findViewById(R.id.fourTxt);
        Button addCardBtn = view.findViewById(R.id.addCardBtn);
        addCardBtn.setText("Pay £"+totalPrice);
        addCardBtn.setOnClickListener(this);
        expireDate.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addCardBtn:
                number = oneTxt.getText().toString().trim() + twoTxt.getText().toString().trim() + threeTxt.getText().toString().trim() + fourTxt.getText().toString().trim();
                if (isValidData()) {
                    apiForPaymentByCard();
                }
                break;
            case R.id.expireDate:
                expireMnth = "";
                expireYear = "";
                expireDate.setText("");
                showMonnthYearDialog();
                break;
        }
    }

    public void showMonnthYearDialog() {
        final Dialog d = new Dialog(mContext);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.year_month_dialog);
        d.getWindow().setLayout((width * 9) / 10, LinearLayout.LayoutParams.WRAP_CONTENT);
        Button set = (Button) d.findViewById(R.id.button1);
        Button cancel = (Button) d.findViewById(R.id.button2);
        final NumberPicker yearPicker = (NumberPicker) d.findViewById(R.id.yearPicker);
        final NumberPicker monthPicker = (NumberPicker) d.findViewById(R.id.monthPicker);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(month + 1);
        yearPicker.setMaxValue(year + 20);
        yearPicker.setMinValue(year);
        yearPicker.setWrapSelectorWheel(false);
        yearPicker.setValue(year);
        yearPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expireDate.setText(String.valueOf(monthPicker.getValue()) + "/" + String.valueOf(yearPicker.getValue()));
                expireMnth = String.valueOf(monthPicker.getValue());
                // expireYear = String.valueOf(yearPicker.getValue()).substring(2, 4);
                expireYear = String.valueOf(yearPicker.getValue());
                d.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private boolean isValidData() {
        String msg = "Please fill all required fields.";
        if (TextUtils.isEmpty(oneTxt.getText().toString().trim())) {
            oneTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        }  else if (oneTxt.getText().toString().trim().length() < 4) {
            oneTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert("Incorrect card number");
            return false;
        }
        else if (TextUtils.isEmpty(twoTxt.getText().toString().trim())) {
            twoTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        }else if (twoTxt.getText().toString().trim().length() < 4) {
            twoTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert("Incorrect card number");
            return false;
        }
        else if (TextUtils.isEmpty(threeTxt.getText().toString().trim())) {
            threeTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        }else if (threeTxt.getText().toString().trim().length() < 4) {
            threeTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert("Incorrect card number");
            return false;
        }
        else if (TextUtils.isEmpty(fourTxt.getText().toString().trim())) {
            fourTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        }else if (fourTxt.getText().toString().trim().length() < 4) {
            fourTxt.requestFocus();
            MyToast.getInstance(mContext).showDasuAlert("Incorrect card number");
            return false;
        } else if (TextUtils.isEmpty(expireDate.getText().toString().trim())) {
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        } else if (TextUtils.isEmpty(cvv.getText().toString().trim())) {
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        } else if (cvv.getText().toString().trim().length() < 3) {
            MyToast.getInstance(mContext).showDasuAlert("Incorrect CVV number.");
            return false;
        }
        return true;
    }

    private void apiForPaymentByCard(){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();
        String sCvv = cvv.getText().toString().trim();
        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForPaymentByCard();
                    }
                }
            }).show();
        }

     /*   Map<String, String> params = new HashMap<>();
        // params.put("userId", String.valueOf(user.id));
        params.put("number", number);
        params.put("cvv", sCvv);
        params.put("exp_month", expireMnth);
        params.put("exp_year", expireYear);
        params.put("id", bookingId);*/

        String url = Uri.parse("cardPayment")
                .buildUpon()
                .appendQueryParameter("number", number)
                .appendQueryParameter("cvv", sCvv)
                .appendQueryParameter("exp_month", expireMnth)
                .appendQueryParameter("exp_year", expireYear)
                .appendQueryParameter("id", bookingId)
                .build().toString();

        String sParam = "number="+number+"cvv"+sCvv+"exp_month"+expireMnth+"exp_year"+expireYear+"id"+bookingId;

        HttpTask task = new HttpTask(new HttpTask.Builder(mContext, url, new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                        Intent intent = new Intent();
                        intent.putExtra("isChanged", "true");
                        ((PaymentActivity)mContext).setResult(RESULT_OK, intent);
                        ((PaymentActivity)mContext).finish();
                    }else {
                        MyToast.getInstance(mContext).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(mContext);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setMethod(Request.Method.GET)
                /*.setBody(params, HttpTask.ContentType.APPLICATION_JSON)*/);

        task.execute(this.getClass().getName());
    }

}
