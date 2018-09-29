package com.mualab.org.user.activity.authentication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.image.cropper.CropImage;
import com.image.cropper.CropImageView;
import com.image.picker.ImagePicker;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.dialogs.DateDialogFragment;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.local.prefs.SharedPreferanceUtils;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.StatusBarUtil;

import org.json.JSONObject;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import de.hdodenhof.circleimageview.CircleImageView;
import views.calender.CalendarHelper;

public class Registration2Activity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = Registration2Activity.class.getName();
    private ViewSwitcher viewSwitcher;
    private View progressView3, progressView4;
    //Reg_View1
    private CircleImageView profile_image;
    //private TextInputLayout input_layout_firstName, input_layout_lastName, input_layout_userName;
    private EditText ed_firstName, ed_lastName, ed_userName;
    private TextView tv_dob;
    private RadioGroup radioGroup;

    //Reg_View2
    // private TextInputLayout input_layout_pwd, input_layout_cnfPwd;
    private EditText edPwd, edConfirmPwd;

    private int CURRENT_VIEW_STATE = 3;
    private User user;
    private Bitmap profileImageBitmap;

    private Session session;
    private boolean isRemind = true;
    //private WebServiceAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);
        StatusBarUtil.setColorNoTranslucent(this, getResources().getColor(R.color.colorPrimary));
        initViews();

        Intent intent = getIntent();
        if(intent.getExtras()!=null){
            user = (User) intent.getSerializableExtra(Constant.USER);
        }
        session = new Session(this);
    }

    private void setDateField() {
        Calendar now = Calendar.getInstance();
        //create new DateDialogFragment
        DateDialogFragment ddf = DateDialogFragment.newInstance(this, R.string.set_date, now);
        ddf.setDateDialogFragmentListener(new DateDialogFragment.DateDialogFragmentListener() {
            @Override
            public void dateDialogFragmentDateSet(Calendar calendar) {
                // update the fragment
                int year = calendar.get(Calendar.YEAR);
                int monthOfYear = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                //String date = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                String dateToShow = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                tv_dob.setText(dateToShow);
                //findViewById(R.id.tvHintDOB).setVisibility(View.VISIBLE);
            }
        });

        ddf.show(getSupportFragmentManager(), "date picker dialog fragment");
    }

    private void initViews(){
        viewSwitcher = findViewById(R.id.viewSwitcher);
        progressView3 = findViewById(R.id.progressView3);
        progressView4 = findViewById(R.id.progressView4);
        progressView3.setBackgroundColor(ContextCompat.getColor(this,R.color.colorAccent));

        /* view 1 */
        profile_image = findViewById(R.id.profile_image);
        profile_image.setOnClickListener(this);
        /*input_layout_firstName = findViewById(R.id.input_layout_firstName);
        input_layout_lastName = findViewById(R.id.input_layout_lastName);
        input_layout_userName = findViewById(R.id.input_layout_userName);*/
        ed_firstName = findViewById(R.id.ed_firstName);
        ed_lastName = findViewById(R.id.ed_lastName);
        ed_userName = findViewById(R.id.ed_userName);
        tv_dob = findViewById(R.id.tv_dob);
        radioGroup = findViewById(R.id.radioGroup);
        findViewById(R.id.btnContinue1).setOnClickListener(this);
        tv_dob.setOnClickListener(this);

        /* view 1 */
       /* input_layout_pwd = findViewById(R.id.input_layout_pwd);
        input_layout_cnfPwd = findViewById(R.id.input_layout_cnfPwd);*/
        edPwd = findViewById(R.id.edPwd);
        edConfirmPwd = findViewById(R.id.edConfirmPwd);
        findViewById(R.id.btnContinue2).setOnClickListener(this);
        findViewById(R.id.alreadyHaveAnAccount).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.alreadyHaveAnAccount:
                finish();
                break;

            case R.id.btnContinue1:
               /* if(profileImageBitmap==null){
                    showToast(getString(R.string.error_profile_image));
                }else*/ if(checkNotempty(ed_firstName/*, input_layout_firstName*/)
                    && checkNotempty(ed_lastName/*, input_layout_lastName*/)
                    && validUserName(ed_userName/*, input_layout_userName*/)
                    && checkDOB()){

                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton radioSexButton = findViewById(selectedId);

                user.userName = ed_userName.getText().toString().trim();
                user.firstName = ed_firstName.getText().toString().trim();
                user.lastName = ed_lastName.getText().toString().trim();
                user.fullName = user.firstName.concat(" ").concat(user.lastName);
                user.password = edPwd.getText().toString().trim();
                user.dob = tv_dob.getText().toString().trim();
                user.gender = radioSexButton.getText().toString();

                final Map<String, String> params = new HashMap<>();
                params.put("userName", user.userName);
                new HttpTask(new HttpTask.Builder(this, "checkUser", new HttpResponceListner.Listener() {
                    @Override
                    public void onResponse(String response, String apiName) {
                        Log.d("hfjas", response);
                        try {
                            JSONObject js = new JSONObject(response);
                            String status = js.getString("status");
                            //String message = js.getString("message");
                            if (status.equalsIgnoreCase("success")) {
                                nextScreen();
                            }else {
                                showToast(getString(R.string.error_user_name_exist));
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void ErrorListener(VolleyError error) {
                    }}).setBody(params, HttpTask.ContentType.APPLICATION_JSON)
                        .setMethod(Request.Method.POST)
                        .setProgress(true))
                        .execute(Registration2Activity.this.getClass().getName());
            }

                break;


            case R.id.btnContinue2:
                if(isValidPassword(edPwd /*, input_layout_pwd*/)
                        && isValidPassword(edConfirmPwd /*, input_layout_cnfPwd*/)
                        && matchPassword()){
                    user.password = edPwd.getText().toString().trim();
                    nextScreen();
                    //createMualabAccount();
                }
                break;

            case R.id.profile_image:
                getPermissionAndPicImage();
                break;

            case R.id.tv_dob:
                datePicker();
                //  setDateField();
                break;

        }
    }

    private void nextScreen(){
        progressView3.setBackgroundColor(ContextCompat.getColor(this,R.color.white));
        progressView4.setBackgroundColor(ContextCompat.getColor(this,R.color.white));

        switch (CURRENT_VIEW_STATE){
            case 3:
                CURRENT_VIEW_STATE = 4;
                viewSwitcher.showNext();
                ((ImageView)findViewById(R.id.iv_bg)).setImageResource(R.drawable.bg_registration3);
                progressView4.setBackgroundColor(ContextCompat.getColor(this,R.color.colorAccent));
                break;

            case 4:
                findViewById(R.id.btnContinue2).setEnabled(false);
                String deviceToken =  FirebaseInstanceId.getInstance().getToken();//"android without firebase";
                Map<String, String> params = new HashMap<>();
                params.put("userName", user.userName);
                params.put("firstName", user.firstName);
                params.put("lastName", user.lastName);
                params.put("email", user.email);
                params.put("password", user.password);
                params.put("countryCode", user.countryCode);
                params.put("contactNo", user.contactNo);
                params.put("businessName", user.businessName);
                params.put("gender", user.gender);
                params.put("dob", CalendarHelper.getStringYMDformatter(user.dob));
               /* params.put("address", address.stAddress1);
                params.put("city", address.city);
                params.put("state", address.state);
                params.put("country", address.country);
                params.put("businessPostCode", address.postalCode);
                params.put("latitude", address.latitude);
                params.put("longitude", address.longitude);*/
                params.put("userType", "user");
                params.put("businessType", "N/A");
                params.put("deviceType", "2");
                params.put("deviceToken", deviceToken);
                params.put("socialId", "");
                params.put("socialType", "");
                params.put("firebaseToken", deviceToken);

                //api.signUpTask(params, profileImageBitmap);
                HttpTask task = new HttpTask(new HttpTask.Builder(this, "userRegistration", new HttpResponceListner.Listener() {
                    @Override
                    public void onResponse(String response, String apiName) {
                        try {
                            JSONObject js = new JSONObject(response);
                            String status = js.getString("status");
                            String message = js.getString("message");
                            if (status.equalsIgnoreCase("success")) {
                                //Progress.hide(Registration2Activity.this);
                                Gson gson = new Gson();
                                JSONObject userObj = js.getJSONObject("users");
                                User user = gson.fromJson(String.valueOf(userObj), User.class);
                                user.id = userObj.getInt("_id");
                                session.createSession(user);
                                session.setPassword(user.password);
                                checkUserRember(user);
                                writeNewUser(user);
                            }else {
                                showToast(message);
                                findViewById(R.id.btnContinue2).setEnabled(true);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            findViewById(R.id.btnContinue2).setEnabled(true);
                        }
                    }

                    @Override
                    public void ErrorListener(VolleyError error) {
                        findViewById(R.id.btnContinue2).setEnabled(true);
                    }})
                        .setParam(params)
                        .setProgress(true));
                task.postImage("profileImage", profileImageBitmap);
                break;
        }

    }

    private void writeNewUser(User user) {
        DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = new FirebaseUser();
        firebaseUser.firebaseToken = FirebaseInstanceId.getInstance().getToken();;
        firebaseUser.isOnline = 1;
        firebaseUser.lastActivity = ServerValue.TIMESTAMP;
        if (user.profileImage.isEmpty())
            firebaseUser.profilePic = "http://koobi.co.uk:3000/uploads/default_user.png";
        else
            firebaseUser.profilePic = user.profileImage;

        firebaseUser.userName = user.userName;
        firebaseUser.uId = user.id;
        firebaseUser.authToken = user.authToken;
        firebaseUser.userType = user.userType;
        firebaseUser.banAdmin = 0;

        mDatabase.child("users").child(String.valueOf(user.id)).setValue(firebaseUser);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("user", user);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(400, 400).start(this);
                } else {
                    showToast(getString(R.string.msg_some_thing_went_wrong));
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());

                    if (profileImageBitmap != null) {
                        profile_image.setImageBitmap(profileImageBitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(Registration2Activity.this);
                } else showToast("YOUR  PERMISSION DENIED");
            }
            break;
        }
    }

    private boolean isValidPassword(EditText edPwd /*TextInputLayout inputLayout*/) {
        // Pattern regex = Pattern.compile("[$&+,:;=\\\\?@#|/'<>.^*()%!-]");
        String password = edPwd.getText().toString().trim();
        // Pattern specailCharPatten = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern UpperCasePatten = Pattern.compile("[A-Z ]");
        // Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Pattern digitCasePatten = Pattern.compile("[0-9 ]");

        if (TextUtils.isEmpty(password)) {
            //inputLayout.setError(getString(R.string.error_password_required));
            showToast(getString(R.string.error_password_required));
            edPwd.requestFocus();
            return false;
        } else if (password.length() < 8) {
            //inputLayout.setError(getString(R.string.error_password_vailidation));
            showToast(getString(R.string.error_password_vailidation));
            edPwd.requestFocus();
            return false;
        } else if (!UpperCasePatten.matcher(password).find()) {
            //inputLayout.setError(getString(R.string.error_password_vailidation));
            showToast(getString(R.string.error_password_vailidation));
            edPwd.requestFocus();
            return false;
        }else if (!digitCasePatten.matcher(password).find()) {
            //inputLayout.setError(getString(R.string.error_password_vailidation));
            showToast(getString(R.string.error_password_vailidation));
            edPwd.requestFocus();
            return false;
        }

       /* else {
            inputLayout.setErrorEnabled(false);
        }*/
        return true;
    }


    private boolean matchPassword(){
        if(!edPwd.getText().toString().equals(edConfirmPwd.getText().toString())){
            //input_layout_cnfPwd.setError(getString(R.string.error_confirm_password_not_match));
            showToast(getString(R.string.error_confirm_password_not_match));
            return false;
        }
        return true;
    }

    private boolean validInputField(EditText editText, /*TextInputLayout inputLayout,*/ int id) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            //inputLayout.setError(getString(R.string.error_required_field));
            showToast(getString(R.string.error_required_field));
            editText.requestFocus();
            return false;
        } else if (text.length() < 4) {
            //inputLayout.setError(getString(id));
            showToast(getString(id));
            editText.requestFocus();
            return false;
        }/* else {
            inputLayout.setErrorEnabled(false);
        }*/
        return true;
    }

    private boolean validUserName(EditText editText) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            showToast(getString(R.string.error_required_field));
            //inputLayout.setError(getString(R.string.error_required_field));
            editText.requestFocus();
            return false;
        } else if (text.contains(" ")) {
            showToast(getString(R.string.error_username_contain_space));
            //inputLayout.setError(getString(R.string.error_username_contain_space));
            editText.requestFocus();
            return false;
        } else if (text.length() < 4) {
            showToast(getString(R.string.error_username_length));
            //inputLayout.setError(getString(R.string.error_username_length));
            editText.requestFocus();
            return false;
        }
        return true;
    }

    private boolean checkNotempty(EditText editText/*, TextInputLayout inputLayout*/) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            //inputLayout.setError(getString(R.string.error_required_field));
            showToast(getString(R.string.error_required_field));
            editText.requestFocus();
            return false;
        }/*else {
            inputLayout.setErrorEnabled(false);
        }*/
        return true;
    }

    private boolean checkDOB(){
        if(tv_dob.getText().toString().equals(getString(R.string.date_of_birth))){
            showToast(getString(R.string.error_dob));
            return false;
        }
        return true;
    }

    private void datePicker(){
        // Get Current Date
        final Calendar c = GregorianCalendar.getInstance();
        int mYear = c.get(GregorianCalendar.YEAR);
        int mMonth = c.get(GregorianCalendar.MONTH);
        int mDay = c.get(GregorianCalendar.DAY_OF_MONTH);
        final int[] dayId = {c.get(GregorianCalendar.DAY_OF_WEEK) - 1};
        String weekday = new DateFormatSymbols().getShortWeekdays()[dayId[0]];

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Date date = new Date(year, monthOfYear, dayOfMonth-1);
                        dayId[0] = date.getDay()-1;
                        String sDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        tv_dob.setText(sDate);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showToast(String msg){
        if (!TextUtils.isEmpty(msg)){
            MyToast.getInstance(this).showDasuAlert(msg);
        }
    }

    @Override
    public void onBackPressed() {
        if (CURRENT_VIEW_STATE == 4) {
            CURRENT_VIEW_STATE = 3;
            viewSwitcher.showPrevious();
            ((ImageView)findViewById(R.id.iv_bg)).setImageResource(R.drawable.bg_registration2);
            edPwd.setText("");
            edConfirmPwd.setText("");
            // input_layout_pwd.setError(null);
            // input_layout_cnfPwd.setError(null);
            progressView4.setBackgroundColor(ContextCompat.getColor(this,R.color.white));
            progressView3.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));

        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void checkUserRember(User user){
        SharedPreferanceUtils sp = new SharedPreferanceUtils(this);
        if (isRemind) {
            sp.setParam(Constant.isLoginReminder, true);
            sp.setParam(Constant.USER_ID, user.userName);
        }
    }
}
