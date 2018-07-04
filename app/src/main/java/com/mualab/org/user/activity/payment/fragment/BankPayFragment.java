package com.mualab.org.user.activity.payment.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class BankPayFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "BankPayFragment";
    private RelativeLayout mainLayout;

    private EditText accNo,holderName,routNo;
    private String totalPrice,bookingId;
    private Context mContext;

    public static BankPayFragment newInstance(String bookingId,String totalPrice) {
        BankPayFragment fragment = new BankPayFragment();
        Bundle args = new Bundle();
        args.putString("bookingId", bookingId);
        args.putString("totalPrice", totalPrice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View   view = inflater.inflate(R.layout.bank_pay_stripe, container, false);
        view.setFocusableInTouchMode(true);
        view.setClickable(true);
        view.requestFocus();
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
    }

    private void initializeView(View view) {
        mainLayout = view.findViewById(R.id.mainLayout);

        accNo = view.findViewById(R.id.accNo);
        holderName = view.findViewById(R.id.holderName);
        routNo = view.findViewById(R.id.routNo);

        TextView showTxt = view.findViewById(R.id.showTxt);
        //    spinnHolderType = (Spinner) view.findViewById(R.id.spinnHolderType);
        Button payBtn = view.findViewById(R.id.payBtn);

        payBtn.setText("Pay £"+totalPrice);
        payBtn.setOnClickListener(this);
        //  dobTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.payBtn:
                if (isValidData()) {
                    apiForPaymentByBank();
                }
                break;
            /*case R.id.dobTxt:
                setDateField();
                break;*/
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("us.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private boolean isValidData() {
        String msg = "Please fill all required fields.";
        if (TextUtils.isEmpty(holderName.getText().toString().trim()))
        {
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        } else if (TextUtils.isEmpty(accNo.getText().toString().trim())) {
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        } else if (TextUtils.isEmpty(routNo.getText().toString().trim())) {
            MyToast.getInstance(mContext).showDasuAlert(msg);
            return false;
        }
        return true;
    }

    private void apiForPaymentByBank(){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();
        String routingnumber = routNo.getText().toString().trim();
        String accountNo = accNo.getText().toString().trim();
        String sHolderName = holderName.getText().toString().trim();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForPaymentByBank();
                    }
                }
            }).show();
        }

   /*     Map<String, String> params = new HashMap<>();
        // params.put("userId", String.valueOf(user.id));
        params.put("routingnumber", routingnumber);
        params.put("accountNo", accountNo);
        params.put("holderName", sHolderName);
        // params.put("ssnLast", expireYear);
        //params.put("postalcode", expireYear);
        params.put("id", bookingId);*/
        String url = Uri.parse("bankPayment")
                .buildUpon()
                .appendQueryParameter("routingnumber", routingnumber)
                .appendQueryParameter("accountNo", accountNo)
                .appendQueryParameter("holderName", sHolderName)
                .appendQueryParameter("id", bookingId)
                .build().toString();

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
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }


}
