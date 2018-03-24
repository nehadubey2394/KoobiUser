package com.mualab.org.user.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.gellery.GalleryActivity;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.activity.feeds.fragment.AddFeedFragment;
import com.mualab.org.user.activity.feeds.fragment.FeedsFragment;
import com.mualab.org.user.activity.searchBoard.fragment.SearchBoardFragment;
import com.mualab.org.user.dialogs.MySnackBar;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.network.NetworkChangeReceiver;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider.REQUEST_CHECK_SETTINGS;


public class MainActivity extends BaseActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult> {

    public static String TAG = MainActivity.class.getName();

    private ImageButton ibtnLeaderBoard, ibtnFeed, ibtnAddFeed, ibtnSearch, ibtnNotification, ibtnChat;
    private int clickedId = 0;
    public ImageView ivHeaderBack, ivHeaderUser, ivAppIcon;
    public TextView tvHeaderTitle;
    public RelativeLayout rlHeader1;
    private static final int REQUEST_ADD_NEW_STORY = 8719;
    public RefineSearchBoard item;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusbarColor();

        Mualab.currentUser = Mualab.getInstance().getSessionManager().getUser();
        Mualab.feedBasicInfo.put("userId", "" + Mualab.currentUser.id);
        Mualab.feedBasicInfo.put("age", "25");
        Mualab.feedBasicInfo.put("gender", "Male");
        Mualab.feedBasicInfo.put("city", "indore");
        Mualab.feedBasicInfo.put("state", "MP");
        Mualab.feedBasicInfo.put("country", "India");

        final NoConnectionDialog network = new NoConnectionDialog(MainActivity.this, new NoConnectionDialog.Listner() {
            @Override
            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                if (isConnected) {
                    dialog.dismiss();
                }
            }
        });

        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setListner(new NetworkChangeReceiver.Listner() {
            @Override
            public void onNetworkChange(boolean isConnected) {
                if (isConnected && network != null) {
                    network.dismiss();
                } else network.show();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            item = (RefineSearchBoard) bundle.getSerializable("refineSearchBoard");
        }

        initView();
        addFragment(SearchBoardFragment.newInstance(item, ""), false);

        checkLocationPermissionAndGetLocation();

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }


    private void initView() {
        ibtnLeaderBoard = findViewById(R.id.ibtnLeaderBoard);
        ibtnFeed = findViewById(R.id.ibtnFeed);
        ibtnAddFeed = findViewById(R.id.ibtnAddFeed);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        ibtnNotification = findViewById(R.id.ibtnNotification);
        ibtnChat = findViewById(R.id.ibtnChat);

        ivAppIcon = findViewById(R.id.ivAppIcon);
        ivHeaderBack = findViewById(R.id.ivHeaderBack);
        ivHeaderUser = findViewById(R.id.ivHeaderUser);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rlHeader1 = findViewById(R.id.topLayout1);

        ibtnLeaderBoard.setOnClickListener(this);
        ibtnAddFeed.setOnClickListener(this);
        ibtnSearch.setOnClickListener(this);
        ibtnNotification.setOnClickListener(this);
        ibtnFeed.setOnClickListener(this);
        ivHeaderBack.setOnClickListener(this);
        ibtnChat.setOnClickListener(this);
        ivAppIcon.setOnClickListener(this);
        ibtnLeaderBoard.setImageResource(R.drawable.active_leaderboard_ico);
        tvHeaderTitle.setText(getString(R.string.title_searchboard));
        ivHeaderBack.setVisibility(View.GONE);
    }

    private final static int DEFAULT_BITRATE = 1024000;
    private static final int RESULT_START_CAMERA = 4567;
    private static final int RESULT_START_VIDEO = 4589;
    private static final int RESULT_ADD_NEW_STORY = 7891;

    public void openNewStoryActivity() {
        showToast(getString(R.string.under_development));
        /*Intent intent = new Intent(this, NewStoryActivity.class);
        startActivityForResult(intent, REQUEST_ADD_NEW_STORY);*/

       /* FilePaths filePaths = new FilePaths();
        File saveFolder = new File(filePaths.STORIES);
        try{
            if (!saveFolder.mkdirs());
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        //RjCamera camera = new RjCamera(this);
        new RjCamera(this)
                .allowRetry(true)                                  // Whether or not 'Retry' is visible during playback
                .autoSubmit(false)                                 // Whether or not user is allowed to playback videos after recording. This can affect other things, discussed in the next section.
                .saveDir(saveFolder)                               // The folder recorded videos are saved to
                .showPortraitWarning(false)                        // Whether or not a warning is displayed if the user presses record in portrait orientation
                .defaultToFrontFacing(false)                       // Whether or not the camera will initially show the front facing camera
                .retryExits(false)                                 // If true, the 'Retry' button in the playback screen will exit the camera instead of going back to the recorder
                .restartTimerOnRetry(false)                        // If true, the countdown timer is reset to 0 when the user taps 'Retry' in playback
                .continueTimerInPlayback(false)                    // If true, the countdown timer will continue to go down during playback, rather than pausing.
                .videoEncodingBitRate(DEFAULT_BITRATE * 5)         // Sets a custom bit rate for video recording.
                .audioEncodingBitRate(50000)                       // Sets a custom bit rate for audio recording.
                .videoFrameRate(30)                                // Sets a custom frame rate (FPS) for video recording.
                .videoPreferredHeight(720)                         // Sets a preferred height for the recorded video output.
                .videoPreferredAspect(16f / 9f)                    // Sets a preferred aspect ratio for the recorded video output.
                .maxAllowedFileSize(1024 * 1024 * 20)              // Sets a max file size of 20MB, recording will stop if file reaches this limit. Keep in mind, the FAT file system has a file size limit of 4GB.
                .iconRecord(R.drawable.mcam_action_capture)        // Sets a custom icon for the button used to start recording
                .iconStop(R.drawable.mcam_action_stop)             // Sets a custom icon for the button used to stop recording
                .iconFrontCamera(R.drawable.ic_camera_rear_white)  // Sets a custom icon for the button used to switch to the front camera
                .iconRearCamera(R.drawable.ic_camera_rear_white)   // Sets a custom icon for the button used to switch to the rear camera
                .iconPlay(R.drawable.evp_action_play)              // Sets a custom icon used to start playback
                .iconPause(R.drawable.evp_action_pause)            // Sets a custom icon used to pause playback
                .iconRestart(R.drawable.evp_action_restart)        // Sets a custom icon used to restart playback
                .labelRetry(R.string.mcam_retry)                   // Sets a custom button label for the button used to retry recording, when available
                .audioDisabled(false)                              // Set to true to record video without any audio.
                .countdownSeconds(60f)
                .stillShot()
                .start(REQUEST_ADD_NEW_STORY);*/

       /* startActivityForResult(new Intent(MainActivity.this, CameraActivity.class),
                REQUEST_ADD_NEW_STORY);*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_ADD_NEW_STORY) {

            /*if(resultCode == 7891){

                Uri uri = data.getData();
                String dataType  = data.getType();
                if(dataType.equals("image/jpeg")){
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),uri);
                        addMyStory(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/
        }

        if (requestCode == Constant.LOCATION_SETTINGS_REQUEST) {
            getLocation();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.ivHeaderBack:
                onBackPressed();
                break;

            case R.id.ibtnChat:
                openNewStoryActivity();
                //MyToast.getInstance(MainActivity.this).showSmallCustomToast("Under developement");
                break;

            case R.id.ivAppIcon:
                break;

            case R.id.ibtnLeaderBoard:
                if (clickedId != 1) {
                    setInactiveTab();
                    clickedId = 1;
                    tvHeaderTitle.setText(getString(R.string.title_searchboard));
                    ibtnLeaderBoard.setImageResource(R.drawable.active_leaderboard_ico);
                    ivHeaderBack.setVisibility(View.GONE);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);
                    replaceFragment(SearchBoardFragment.newInstance(item, ""), false);
                }
                break;

            case R.id.ibtnFeed:
                if (clickedId != 2) {
                    setInactiveTab();
                    clickedId = 2;
                    tvHeaderTitle.setText(getString(R.string.app_name));
                    ibtnFeed.setImageResource(R.drawable.active_feeds_ico);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    replaceFragment(FeedsFragment.newInstance(1), false);
                    ibtnChat.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.VISIBLE);
                    ivHeaderBack.setVisibility(View.GONE);
                }
                break;

            case R.id.ibtnAddFeed:
                startActivity(new Intent(MainActivity.this, GalleryActivity.class));
               /* if (clickedId!=3) {
                    setInactiveTab();
                    clickedId = 3;
                    tvHeaderTitle.setText(getString(R.string.title_searchboard));
                    ibtnAddFeed.setImageResource(R.drawable.active_add_ico);
                    tvHeaderTitle.setText(R.string.title_photo);
                    ivHeaderBack.setVisibility(View.GONE);
                    ivHeaderUser.setVisibility(View.GONE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    startActivity(new Intent(MainActivity.this, GalleryActivity.class));
                    //replaceFragment(new AddFeedFragment(), false);
                }*/
                break;

            case R.id.ibtnSearch:
                if (clickedId != 4) {
                    setInactiveTab();
                    clickedId = 4;
                    tvHeaderTitle.setText(R.string.title_explore);
                    ibtnSearch.setImageResource(R.drawable.active_search_ico);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);
                    replaceFragment(new AddFeedFragment(), false);
                }
                break;

            case R.id.ibtnNotification:
                if (clickedId != 5) {
                    setInactiveTab();
                    clickedId = 5;
                    tvHeaderTitle.setText(getString(R.string.title_searchboard));
                    ibtnNotification.setImageResource(R.drawable.active_notifications_ico);
                    tvHeaderTitle.setText(R.string.title_notification);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);
                    replaceFragment(new AddFeedFragment(), false);
                }
                break;
        }
    }

    private void setInactiveTab() {
        rlHeader1.setVisibility(View.VISIBLE);
        ibtnLeaderBoard.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_leaderboard_ico));
        ibtnFeed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_feeds_ico));
        ibtnAddFeed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_add_ico));
        ibtnSearch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_search_ico));
        ibtnNotification.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_notifications_ico));

    }


    private boolean doubleBackToExitPressedOnce;
    private Runnable runnable;

    @Override
    public void onBackPressed() {
          /* Handle double click to finish activity*/
        Handler handler = new Handler();
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else if (!doubleBackToExitPressedOnce) {

            doubleBackToExitPressedOnce = true;
            //Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
            MySnackBar.showSnackbar(this, findViewById(R.id.lyCoordinatorLayout), "Click again to exit");
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


    private void addMyStory(Bitmap bitmap) {

        if (ConnectionDetector.isConnected()) {
            Map<String, String> map = new HashMap<>();
            map.put("type", "image");

            HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {
                            showToast(message);
                            finish();
                        } else showToast(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    Log.d("res:", "" + error.getLocalizedMessage());
                }
            })
                    .setParam(map)
                    .setAuthToken(Mualab.getInstance().getSessionManager().getUser().authToken)
                    .setProgress(true));
            task.postImage("myStory", bitmap);
        } else showToast(getString(R.string.error_msg_network));
    }

    private void showToast(String str) {
        if (!TextUtils.isEmpty(str))
            MyToast.getInstance(this).showSmallCustomToast(str);
    }


    private void checkLocationPermissionAndGetLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constant.MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                getLocation();
            }
        } else {
            getLocation();
        }
    }

    /*Get Current Location*/
    private LocationManager locationManager;

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            assert locationManager != null;
            boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // check if enabled and if not send user to the GSP settings
            // Better solution would be to display a dialog and suggesting to
            // go to the settings
            if (!enabled) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, Constant.LOCATION_SETTINGS_REQUEST);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Mualab.currentLocation.lat = location.getLatitude();
        Mualab.currentLocation.lng = location.getLongitude();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        showToast("Please Enable GPS and Internet");
        //chageLocationSetting();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }


    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }


    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations, this can be null.
                        if (location != null) {
                            // Logic to handle location object
                        }
                    }
                });
    }


    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }
}
