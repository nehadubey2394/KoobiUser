package com.mualab.org.user.activity.artist_profile.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.image.picker.ImagePicker;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.adapter.CertificatesListAdapter;
import com.mualab.org.user.activity.artist_profile.listner.OnCertificateClickListener;
import com.mualab.org.user.activity.artist_profile.model.Certificate;
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
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.zoomage.ZoomageView;

public class CertificateActivity extends AppCompatActivity implements OnCertificateClickListener{
    private List<Certificate> certificates;
    private CertificatesListAdapter certificatesListAdapter;
    private RecyclerView rycvCertificates;
    private TextView tvNoData;
    private String artistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);
        Intent i = getIntent();
        artistId =  i.getStringExtra("artistId");
        certificates = new ArrayList<>();
        certificatesListAdapter = new CertificatesListAdapter(CertificateActivity.this,certificates);
        setViewId();
    }

    private void setViewId(){
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(getString(R.string.text_certificate));
        tvNoData = findViewById(R.id.tvNoData);
        rycvCertificates = findViewById(R.id.rycvCertificates);

        GridLayoutManager linearLayoutManager = new GridLayoutManager(CertificateActivity.this,
                2);
        rycvCertificates.setItemAnimator(new DefaultItemAnimator());
        rycvCertificates.setLayoutManager(linearLayoutManager);
        rycvCertificates.setAdapter(certificatesListAdapter);
        certificatesListAdapter.setListner(CertificateActivity.this);

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        apiForGetCertificates();
    }

    private void apiForGetCertificates(){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(CertificateActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetCertificates();
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", artistId);
        params.put("type", "user");

        HttpTask task = new HttpTask(new HttpTask.Builder(CertificateActivity.this, "getAllCertificate", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        rycvCertificates.setVisibility(View.VISIBLE);
                        tvNoData.setVisibility(View.GONE);
                        certificates.clear();
                        JSONArray jsonArray = js.getJSONArray("allCertificate");

                        for(int i=0; i<jsonArray.length(); i++){
                            Gson gson = new Gson();
                            JSONObject cObj = (JSONObject) jsonArray.get(i);
                            Certificate item = gson.fromJson(String.valueOf(cObj), Certificate.class);
                            item.status = cObj.getInt("status");
                            item.id = cObj.getInt("_id");
                            certificates.add(item);
                        }
                        certificatesListAdapter.notifyDataSetChanged();
                    }else {
                        rycvCertificates.setVisibility(View.GONE);
                        tvNoData.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Progress.hide(CertificateActivity.this);
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
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    @Override
    public void onCertificateClick(int position) {
        Certificate certificate = certificates.get(position);
        showLargeImage(certificate);
    }

    @Override
    public void onRemoveImage(int index) {
        //apiForRemoveCertificate(index);
    }

    @Override
    public void onUpdateIndex(int index) {

    }
    // check permission or Get image from camera or gallery
    public void getPermissionAndPicImage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            } else {
                ImagePicker.pickImage(this);
            }
        } else {
            ImagePicker.pickImage(this);
        }
    }

    private void showLargeImage(Certificate certificate){
        View dialogView = View.inflate(CertificateActivity.this, R.layout.dialog_view_certificate, null);
        final Dialog dialog = new Dialog(CertificateActivity.this,android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.InOutAnimation;
        dialog.setContentView(dialogView);

        ZoomageView ivCertificate = dialogView.findViewById(R.id.post_image);
        ImageView btnBack = dialogView.findViewById(R.id.btnBack);

        Picasso.with(CertificateActivity.this).load(certificate.certificateImage).
                priority(Picasso.Priority.HIGH).noPlaceholder().into(ivCertificate);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        dialog.show();
    }

  /*  private void apiForRemoveCertificate(final int index){
        final  Certificate certificate = certificates.get(index);
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(CertificateActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForRemoveCertificate(index);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("artistId", String.valueOf(user.id));
        params.put("certificateId",""+certificate.id);

        HttpTask task = new HttpTask(new HttpTask.Builder(CertificateActivity.this, "deleteCertificate", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        certificates.remove(certificate);
                        certificatesListAdapter.notifyDataSetChanged();
                    }else {
                        MyToast.getInstance(CertificateActivity.this).showDasuAlert(message);
                    }
                } catch (Exception e) {
                    Progress.hide(CertificateActivity.this);
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
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

    private void uploadCertificateIntoServer(final Bitmap bitmap){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(CertificateActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        uploadCertificateIntoServer(bitmap);
                    }
                }
            }).show();
        }

        HttpTask task = new HttpTask(new HttpTask.Builder(CertificateActivity.this, "addArtistCertificate", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d("log", response);
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {

                        JSONObject jsonObject = js.getJSONObject("certificate");
                        Certificate certificate = new Certificate();
                        Gson gson = new Gson();
                        Certificate item = gson.fromJson(String.valueOf(jsonObject), Certificate.class);
                        item.status = jsonObject.getInt("status");
                        item.id = jsonObject.getInt("_id");
                        certificates.add(item);
                        certificatesListAdapter.notifyDataSetChanged();
                    }else {
                        MyToast.getInstance(CertificateActivity.this).showDasuAlert(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }})
                .setAuthToken(user.authToken)
                .setProgress(true));

        task.postImage("certificateImage", bitmap);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                Bitmap bitmap = ImagePicker.getImageFromResult(CertificateActivity.this, requestCode, resultCode, data);
                //Uri imageUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data);
                if (bitmap != null) {
                    uploadCertificateIntoServer(bitmap);

                } else {
                    MyToast.getInstance(CertificateActivity.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getPermissionAndPicImage();
                } else MyToast.getInstance(CertificateActivity.this).showDasuAlert("YOUR  PERMISSION DENIED");
            }
            break;
        }
    }
*/


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
