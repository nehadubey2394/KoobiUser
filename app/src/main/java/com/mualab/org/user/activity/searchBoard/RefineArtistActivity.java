package com.mualab.org.user.activity.searchBoard;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.mualab.org.user.R;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.helper.MyToast;
import com.mualab.org.user.listner.DatePickerListener;
import com.mualab.org.user.util.DatePickerFragment;

import static com.mualab.org.user.constants.Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE;

public class RefineArtistActivity extends AppCompatActivity implements View.OnClickListener,DatePickerListener {
    private ExpandableListView lvService;
    private boolean isServiceOpen = false;
    private ImageView ivPrice,ivDistance;
    private TextView tv_refine_dob,tv_refine_loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refine_artist);
        setViewId();
    }

    private void init(){

    }

    private void setViewId(){
        ImageView ivBack = findViewById(R.id.ivHeaderBack);
        TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText(R.string.titel_refine);
        ivBack.setVisibility(View.VISIBLE);

        ivPrice = findViewById(R.id.ivPrice);
        ivDistance = findViewById(R.id.ivDistance);

        tv_refine_dob = findViewById(R.id.tv_refine_dob);
        tv_refine_loc = findViewById(R.id.tv_refine_loc);

        AppCompatButton btnApply = findViewById(R.id.btnApply);
        AppCompatButton btnClear = findViewById(R.id.btnClear);

        lvService = findViewById(R.id.lvService);
        RelativeLayout rlService = findViewById(R.id.rlService);
        RelativeLayout rlPrice = findViewById(R.id.rlPrice);
        RelativeLayout rlDistance = findViewById(R.id.rlDistance);
        RelativeLayout rlRefineLocation = findViewById(R.id.rlRefineLocation);
        RelativeLayout rlDob = findViewById(R.id.rlDob);

        RadioGroup rdgOrder =  findViewById(R.id.rdgOrder);
        final AppCompatRadioButton rbAscending =  findViewById(R.id.rbAscending);
        final RadioButton rbDescending =  findViewById(R.id.rbDescending);

        rdgOrder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (rbAscending.isChecked()) {
                    rbDescending.setTextColor(getResources().getColor(R.color.text_color));
                    rbAscending.setTextColor(getResources().getColor(R.color.colorPrimary));

                }
                if (rbDescending.isChecked()){
                    rbAscending.setTextColor(getResources().getColor(R.color.text_color));
                    rbDescending.setTextColor(getResources().getColor(R.color.colorPrimary));


                }
            }
        });
        MyToast.getInstance(RefineArtistActivity.this).showSmallCustomToast("Under developement");

        ivBack.setOnClickListener(this);
        rlService.setOnClickListener(this);
        rlPrice.setOnClickListener(this);
        rlDistance.setOnClickListener(this);
        rlDob.setOnClickListener(this);
        rlRefineLocation.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnApply.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ivHeaderBack:
                finish();
                break;

            case R.id.rlRefineLocation:
                getAddress();
                break;

            case R.id.rlDob:
                DatePickerFragment datePickerFragment = new DatePickerFragment(Constant.CALENDAR_DAY_PAST, true,false);
                datePickerFragment.setDateListener(this);
                datePickerFragment.show(getSupportFragmentManager(), "");
                break;

            case R.id.rlService:
                if (!isServiceOpen){
                    isServiceOpen = true;
                    lvService.setVisibility(View.VISIBLE);
                } else {
                    isServiceOpen = false;
                    lvService.setVisibility(View.GONE);
                }
                break;
            case R.id.rlPrice:
                ivPrice.setImageResource(R.drawable.active_price_ico);
                ivDistance.setImageResource(R.drawable.route_ico);
                break;
            case R.id.rlDistance:
                ivPrice.setImageResource(R.drawable.price_ico);
                ivDistance.setImageResource(R.drawable.active_route_ico);
                break;
            case R.id.btnClear :
                tv_refine_loc.setText("");
                tv_refine_dob.setText("");
                break;

            case R.id.btnApply :
                break;
        }
    }

    private void getAddress() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(RefineArtistActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                tv_refine_loc.setText(place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("", status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
    @Override
    public void onDateSet(int year, int month, int day, int cal_type) {
        tv_refine_dob.setText(day + "/" + (month + 1) + "/" + year);
    }
}
