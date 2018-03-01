package com.mualab.org.user.activity.feeds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.FilteredAdapter;
import com.hendraanggrian.widget.SocialAutoCompleteTextView;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.MySnackBar;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.MediaUri;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.task.UploadImage;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.LocationDetector;
import com.mualab.org.user.util.media.ImageVideoUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class FeedPostActivity extends AppCompatActivity implements View.OnClickListener {

   // private static final int REQUEST_CODE_PICK = 1;
   //private static final int PROGRESS_BAR_MAX = 1000;
    public static String TAG = FeedPostActivity.class.getName();

    // private EditText edCaption;
    // private AutoCompleteTextView edCaption;
    ArrayList<String> tagList = new ArrayList<>();
    private TextView tvLoaction;
    private ImageView ivShareFbOn, ivShareTwitterOn, iv_postimage;
    private Session session;

    private boolean isFbShared = true, isTwitteron = true;
    private Double lat, lng;
    private TagAdapter tagAdapter;
    private SocialAutoCompleteTextView edCaption;
    private TextView tvMediaSize;
    private List<String> hashTags = new ArrayList<>();
   // private Future<Void> mFuture;

    private int feedType;
    private String caption;
    private String lastTxt;
    private String isShare = "", tages = "", address;

    private MediaUri mediaUri;

    private BroadcastReceiver receiverUpComplete = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TAG", "onReceive: Call");
            //showLikedSnackbar("Video Has been Uploaded");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        FeedPostActivity.this.registerReceiver(receiverUpComplete, new IntentFilter("FILTER"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_post);
        session = Mualab.getInstance().getSessionManager();

        Intent intent;
        if ((intent = getIntent()) != null) {
            caption = intent.getStringExtra("caption");
            mediaUri = (MediaUri) intent.getSerializableExtra("mediaUri");
            feedType = intent.getIntExtra("feedType", Constant.TEXT_STATE);
        }

        viewDidLoad();
        updateUi();

        ivShareFbOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFbShared) {
                    ivShareFbOn.setImageResource(R.drawable.ic_switch_off);
                    isFbShared = false;
                } else {
                    ivShareFbOn.setImageResource(R.drawable.ic_switch_on);
                    isFbShared = true;
                }

            }
        });

        ivShareTwitterOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTwitteron) {
                    ivShareTwitterOn.setImageResource(R.drawable.ic_switch_off);
                    isTwitteron = false;
                } else {
                    ivShareTwitterOn.setImageResource(R.drawable.ic_switch_on);
                    isTwitteron = true;
                }
            }
        });

        edCaption.setHashtagTextChangedListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence s) {
                Log.d("editing", s.toString());
                lastTxt = s.toString();
                if (lastTxt.length() > 1)
                    getDropDown(lastTxt);
                return null;
            }
        });

        iv_postimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mediaUri!=null && mediaUri.uriList!=null&& mediaUri.uriList.size()>0){

                    if(mediaUri.mediaType == Constant.IMAGE_STATE){

                        Intent intent = new Intent(FeedPostActivity.this, PreviewImageActivity.class);
                        intent.putExtra("imageArray", (Serializable) mediaUri.uriList);
                        intent.putExtra("startIndex", 0);
                        startActivity(intent);
                    }else if(mediaUri.mediaType == Constant.VIDEO_STATE){

                        startActivity(new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(Uri.parse(mediaUri.uriList.get(0)), "video/mp4")
                                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                    }
                }
            }
        });

        checkLocationPermisssion();

    }

    private void getDropDown(String tag) {
        Map<String, String> map = new HashMap<>();
        map.put("search", tag);
        map.put("page", "0");
        map.put("limit", "5");


        new HttpTask(new HttpTask.Builder(this, "user/allTag", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject object = new JSONObject(response);
                    hashTags.clear();
                    tagAdapter.clear();

                    if (object.has("tags")) {
                        JSONArray array = object.getJSONArray("tags");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            hashTags.add(obj.getString("tag").replace("#", ""));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (hashTags.size() > 0) {
                    tagAdapter.addAll(hashTags);
                    tagAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }})
                .setBody(map, HttpTask.ContentType.X_WWW_FORM_URLENCODED)
                .setProgress(false))
                .execute("TAG");
        Mualab.getInstance().getRequestQueue().cancelAll("TAG");
        lastTxt = tag;
    }


    private void viewDidLoad() {
        tvLoaction = findViewById(R.id.tv_loaction);
        ivShareFbOn = findViewById(R.id.iv_fb_on);
        ivShareTwitterOn = findViewById(R.id.iv_twitter_on);
        iv_postimage = findViewById(R.id.iv_postimage);
        edCaption = findViewById(R.id.edCaption);
        tvMediaSize = findViewById(R.id.tvMediaSize);
       // progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.iv_feedPost).setOnClickListener(this);
        findViewById(R.id.ly_location).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

        //hashtagAdapter = new HashtagAdapter(this);
        edCaption.setThreshold(1);
        edCaption.setHashtagEnabled(true);
        edCaption.setHyperlinkEnabled(true);

        tagAdapter = new TagAdapter(this);
        edCaption.setHashtagAdapter(tagAdapter);
    }


    @SuppressLint("DefaultLocale")
    private void updateUi() {

        if (!TextUtils.isEmpty(caption)) {
            edCaption.setText(caption);
            edCaption.setSelection(edCaption.getText().length());
        }


        if(mediaUri!=null && mediaUri.uriList!=null&& mediaUri.uriList.size()>0){

            if(mediaUri.mediaType == Constant.IMAGE_STATE){

                try {
                    feedType = Constant.IMAGE_STATE;
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            Uri.parse(mediaUri.uriList.get(mediaUri.uriList.size()-1)));
                    iv_postimage.setImageBitmap(bitmap);

                    if(mediaUri.uriList.size()>1){
                        tvMediaSize.setVisibility(View.VISIBLE);
                        tvMediaSize.setText(String.format("%d", mediaUri.uriList.size()));
                    }

               /* if(fromGallery){
                }else {
                    Bitmap  bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mSelectedImages.get(0));
                    bitmap = ImageRotator.rotateImageIfRequired(bitmap, mSelectedImages.get(0));
                    bitmap = ImageRotator.getResizedBitmap(bitmap, 500);
                    iv_postimage.setImageBitmap(bitmap);
                    //mSelectedImages.get(0) = Uri.parse(f.getPath());
                }*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(mediaUri.mediaType == Constant.VIDEO_STATE){

                Bitmap bitmap;
                feedType = Constant.VIDEO_STATE;
                if (mediaUri.isFromGallery) {
                    String filePath = ImageVideoUtil.generatePath(Uri.parse(mediaUri.uriList.get(0)), this);
                    bitmap = ImageVideoUtil.getVidioThumbnail(filePath);
                } else {
                    bitmap = ImageVideoUtil.getVideoToThumbnil(Uri.parse(mediaUri.uriList.get(0)), this); //ImageVideoUtil.getCompressBitmap();
                }

                if (bitmap != null)
                    iv_postimage.setImageBitmap(bitmap);
                // Bitmap bitmap = ImageVideoUtil.getVideoToThumbnil(Uri.parse(mSelectdVideo), this);
            }
        }else {
            feedType = Constant.TEXT_STATE;
            iv_postimage.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        caption = edCaption.getText().toString().trim();
        if (TextUtils.isEmpty(caption))
            caption = "";
        else getAllTags();


        switch (v.getId()) {

            case R.id.iv_feedPost:
                hideKeyboard();
                if(ConnectionDetector.isConnected()){

                    if (feedType == Constant.TEXT_STATE)
                        apiCallForUploadData();
                    if (feedType == Constant.VIDEO_STATE) {
                    /*Uri videoUri = Uri.parse(mSelectdVideo);

                    String path = ImageVideoUtil.generatePath(videoUri, FeedPostActivity.this);
                    File file = new File(path);

                    // Get length of file in bytes
                    long fileSizeInBytes = file.length();
                    // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                    long fileSizeInKB = fileSizeInBytes / 1024;
                    // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                    long fileSizeInMB = fileSizeInKB / 1024;

                    if(fileSizeInMB>10){
                        compressVideo(videoUri, file);
                    }else {
                        apiCallForUploadVideo(file);
                    }*/
                        sendToBackGroundService();
                    } else if (feedType == Constant.IMAGE_STATE)
                        apiCallForUploadData();
                }else {
                    MySnackBar.showSnackbar(FeedPostActivity.this,
                            findViewById(R.id.activity_add_post),
                            getString(R.string.error_msg_network));
                }

                break;

            case R.id.iv_back:
                onBackPressed();
                break;

            case R.id.ly_location:
                try {
                    findViewById(R.id.ly_location).setEnabled(false);
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(FeedPostActivity.this);
                    startActivityForResult(intent, Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
                break;
        }
    }

    private void sendToBackGroundService() {
        isShare = "";

        if (isFbShared && isTwitteron) {
            isShare = "facebook,twitter";
        } else if (isFbShared) {
            isShare = "facebook";
        } else if (isTwitteron) {
            isShare = "twitter";
        }

        address = TextUtils.isEmpty(address) ? "" : address;
        Map<String, String> map = new HashMap<>();
        map.put("feedType", "video");
        map.put("caption", caption);
        map.put("isShare", isShare);
        map.put("city", address);
        map.put("tags", tages);

        if (lat != null && lng != null) {
            map.put("latitude", "" + lat);
            map.put("longitude", "" + lng);
        } else {
            map.put("latitude", "");
            map.put("longitude", "");
        }

        /*if (Constant.isVideoUploading) {
            Toast.makeText(this, "Please wait until your upload is complete", Toast.LENGTH_SHORT).show();
        } else {
            Constant.isVideoUploading = true;
            Intent serviceIntent = new Intent(FeedPostActivity.this, ServiceUploadFile.class);
            serviceIntent.putExtra("videoUri", mSelectdVideo);
            serviceIntent.putExtra("map", (Serializable) map);
            // serviceIntent.putExtra("ID", id);
            startService(serviceIntent);
            Toast.makeText(this, "Uploading start..", Toast.LENGTH_SHORT).show();
            finish();
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            findViewById(R.id.ly_location).setEnabled(true);
            if (resultCode == -1) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                LatLng latLng = place.getLatLng();
                address = place.getAddress().toString();
                lat = latLng.latitude;
                lng = latLng.longitude;
                tvLoaction.setText(address);

                Log.i("Tag", "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i("tag", status.getStatusMessage());
            }
        }
    }


  /*  private void compressVideo(Uri uri, final File tmpFile) {

        final File file;
        try {
            File outputDir = new File(getExternalFilesDir(null), "outputs");
            //noinspection ResultOfMethodCallIgnored
            outputDir.mkdir();
            file = File.createTempFile("transcode_test", ".mp4", outputDir);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create temporary file.", e);
            Toast.makeText(this, "Failed to create temporary file.", Toast.LENGTH_LONG).show();
            return;
        }
        ContentResolver resolver = getContentResolver();
        final ParcelFileDescriptor parcelFileDescriptor;
        try {
            parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            Log.w("Could not open '" + uri.toString() + "'", e);
            Toast.makeText(FeedPostActivity.this, "File not found.", Toast.LENGTH_LONG).show();
            return;
        }
        final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        //final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setMax(PROGRESS_BAR_MAX);
        final long startTime = SystemClock.uptimeMillis();
        MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
               *//* if (progress < 0) {
                    progressBar.setIndeterminate(true);
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setProgress((int) Math.round(progress * PROGRESS_BAR_MAX));
                }
*//*
                progressBar.setProgress((int) Math.round(progress * PROGRESS_BAR_MAX));
            }

            @Override
            public void onTranscodeCompleted() {
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                onTranscodeFinished(true, "transcoded file placed on " + file, parcelFileDescriptor);
                //  Uri uri = FileProvider.getUriForFile(FeedPostActivity.this, Constant.FILE_PROVIDER_AUTHORITY, file);
                apiCallForUploadVideo(file);
            }

            @Override
            public void onTranscodeCanceled() {
                progressBar.setVisibility(View.GONE);
                onTranscodeFinished(false, "Compress canceled.", parcelFileDescriptor);
            }

            @Override
            public void onTranscodeFailed(Exception exception) {

                // Get length of file in bytes
                long fileSizeInBytes = tmpFile.length();
                // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                long fileSizeInKB = fileSizeInBytes / 1024;
                // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                long fileSizeInMB = fileSizeInKB / 1024;

                if (fileSizeInMB <= 30) {
                    apiCallForUploadVideo(tmpFile);
                }
                // onTranscodeFinished(false, "Transcoder error occurred.", parcelFileDescriptor);
            }
        };
        Log.d(TAG, "transcoding into " + file);
        mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
                // MediaFormatStrategyPresets.createAndroid720pStrategy(8000 * 1000, 128 * 1000, 1), listener);
                MediaFormatStrategyPresets.createExportPreset960x540Strategy(), listener);
        // switchButtonEnabled(true);
    }

    private void onTranscodeFinished(boolean isSuccess, String toastMessage, ParcelFileDescriptor parcelFileDescriptor) {
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        // switchButtonEnabled(false);
        Toast.makeText(FeedPostActivity.this, toastMessage, Toast.LENGTH_LONG).show();
        try {
            parcelFileDescriptor.close();
        } catch (IOException e) {
            Log.w("Error while closing", e);
        }
    }*/

    /*  @Override
      public void onBackPressed() {
          super.onBackPressed();
          setResult(Activity.RESULT_CANCELED);
      }
  */

    @Override
    public void onBackPressed() {
        hideKeyboard();
        super.onBackPressed();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getAllTags() {
        tagList.clear();
        String regexPattern = "(#\\w+)";
        Pattern p = Pattern.compile(regexPattern);
        Matcher m = p.matcher(caption);

        while (m.find()) {
            String hashtag = m.group(1);
            tagList.add(hashtag);  // Add hashtag to ArrayList
        }
        tages = TextUtils.join(",", tagList);
        tages = tages.replace("#", "");
    }

    private void resetView() {
        edCaption.setText("");
        iv_postimage.setImageBitmap(null);
        iv_postimage.setVisibility(View.GONE);
        hideKeyboard();
    }

    // uploadimage call
    private void apiCallForUploadData() {
        isShare = "";

        if (isFbShared && isTwitteron) {
            isShare = "facebook,twitter";
        } else if (isFbShared) {
            isShare = "facebook";
        } else if (isTwitteron) {
            isShare = "twitter";
        }


        address = TextUtils.isEmpty(address) ? "" : address;
        String feedTypetxt = "";
        if (feedType == Constant.TEXT_STATE)
            feedTypetxt = "text";
        else if (feedType == Constant.IMAGE_STATE)
            feedTypetxt = "image";
        else if (feedType == Constant.VIDEO_STATE)
            feedTypetxt = "video";

        Map<String, String> map = new HashMap<>();
        map.put("feedType", feedTypetxt);
        map.put("caption", caption);
        map.put("isShare", isShare);
        map.put("location", address);
        map.put("tags", tages);

        if (lat != null && lng != null) {
            map.put("latitude", "" + lat);
            map.put("longitude", "" + lng);
        } else {
            checkLocationPermisssion();
            map.put("latitude", "");
            map.put("longitude", "");
        }

        List<Uri>uris = new ArrayList<>();
        for(String uri: mediaUri.uriList)
            uris.add(Uri.parse(uri));

        new UploadImage(FeedPostActivity.this,
                session.getAuthToken(),
                map,
                uris,
                new UploadImage.Listner() {
            @Override
            public void onResponce(String responce) {
                if (!TextUtils.isEmpty(responce)){
                    Log.d("Responce", responce);
                    resetView();
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                Log.d("Responce", error);
                MyToast.getInstance(FeedPostActivity.this).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }
        }).execute();
    }

  /*  public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = FeedPostActivity.this.getContentResolver().query(contentUri,
                    proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }*/

    /*private void apiCallForUploadVideo(File file) {

        isShare = "";

        if (isFbShared && isTwitteron) {
            isShare = "facebook,twitter";
        } else if (isFbShared) {
            isShare = "facebook";
        } else if (isTwitteron) {
            isShare = "twitter";
        }

        address = TextUtils.isEmpty(address) ? "" : address;
        Map<String, String> map = new HashMap<>();
        map.put("feedType", "video");
        map.put("caption", caption);
        map.put("isShare", isShare);
        map.put("city", address);
        map.put("tags", tages);

        if (lat != null && lng != null) {
            map.put("latitude", "" + lat);
            map.put("longitude", "" + lng);
        } else {
            map.put("latitude", "");
            map.put("longitude", "");
        }
        Intent serviceIntent = new Intent(FeedPostActivity.this, ServiceUploadFile.class);
        serviceIntent.putExtra("videoUri", mSelectdVideo);
        serviceIntent.putExtra("map", (Serializable) map);
        startService(serviceIntent);
        Toast.makeText(this, "Uploading start..", Toast.LENGTH_SHORT).show();
        finish();
        WebServiceAPI api = new WebServiceAPI(this, "fds", new ResponseApi.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject object = new JSONObject(response);
                    String status = object.getString("status");
                    if (status.equals("success")) {
                        Toasty.success(FeedPostActivity.this,  "Post successfully upload", Toast.LENGTH_SHORT).show();
                        resetView();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                    // OTP =  object.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toasty.error(
                            FeedPostActivity.this,
                            getString(R.string.error_while_upload_video),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        });
        api.callMultiPartApiVideo("user/addFeeds", map, "feed[0]", file);
    }*/

   /* @Override
    public void onProgressChange(int current, int max) {
        progressBar.setProgress(current);
    }
*/
    private void unregisterUploadReceiver() {
        if (receiverUpComplete != null) {
            FeedPostActivity.this.unregisterReceiver(receiverUpComplete);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterUploadReceiver();
    }

    /*public void showLikedSnackbar(String string) {
        if (TextUtils.isEmpty(string))
            string = "Liked!";
        MyToast.getInstance(this).showSmallMessage(string);
        //MySnackBar.showSnackbarSort(this, findViewById(R.id.myCoordinatorLayout), string);
    }*/

    private void checkLocationPermisssion() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        Constant.MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                getLocation();
            }
        }else {
            getLocation();
        }
    }


    private void getLocation(){
        LocationDetector locationDetector = new LocationDetector();
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (locationDetector.isLocationEnabled(this) && locationDetector.checkLocationPermission(this)) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                }
            });

        }else {
            locationDetector.showLocationSettingDailod(this);
        }
    }

    private class TagAdapter extends FilteredAdapter<String> {
        final Filter filter = new SocialFilter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((String) resultValue);
            }
        };

        public TagAdapter(Context context) {
            super(context, R.layout.item_tag, R.id.textViewName);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tag, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String item = getItem(position);
            if (item != null) {
                holder.textView.setText(String.format("#%s", item));
            }
            return convertView;
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return filter;
        }

        private class ViewHolder {
            final TextView textView;

            ViewHolder(@NonNull View view) {
                this.textView = view.findViewById(R.id.textViewName);
               // SetFont.setfontRagular(this.textView, FeedPostActivity.this);
            }
        }
    }
}
