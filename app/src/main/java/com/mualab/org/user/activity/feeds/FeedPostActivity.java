package com.mualab.org.user.activity.feeds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.model.MediaUri;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.task.UploadImage;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.KeyboardUtil;
import com.mualab.org.user.util.LocationDetector;
import com.mualab.org.user.util.media.ImageVideoUtil;
import com.mualab.org.user.videocompressor.video.MediaController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class FeedPostActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = FeedPostActivity.class.getName();

    private ArrayList<String> tagList = new ArrayList<>();
    private TextView tvLoaction;
    private ImageView ivShareFbOn, ivShareTwitterOn, iv_postimage;
    private Session session;

    private boolean isFbShared = true, isTwitteron = true;
    private Double lat, lng;
    private TagAdapter tagAdapter;
    private SocialAutoCompleteTextView edCaption;
    private TextView tvMediaSize;
    private List<String> hashTags = new ArrayList<>();

    private int feedType;
    private String caption;
    private String lastTxt;
    private String isShare = "", tages = "", address;

    private MediaUri mediaUri;
    private AlertDialog mAlertDialog;

    private Boolean mDeleteCompressedMedia = false;
    private String mUploadUri = null;

    private File tempFile;
    private Bitmap videoThumb;

    /*private BroadcastReceiver receiverUpComplete = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TAG", "onReceive: Call");
            //showLikedSnackbar("Video Has been Uploaded");
        }
    };
*/
    @Override
    protected void onResume() {
        super.onResume();
       // FeedPostActivity.this.registerReceiver(receiverUpComplete, new IntentFilter("FILTER"));
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
        iv_postimage = findViewById(R.id.iv_selectedImage);
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
                   /* Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            Uri.parse(mediaUri.uriList.get(mediaUri.uriList.size()-1)));*/

                    Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                            BitmapFactory.decodeFile(
                                    ImageVideoUtil.generatePath(Uri.parse(mediaUri.uriList.get(mediaUri.uriList.size()-1)),
                                            this)), 200, 200);
                    iv_postimage.setImageBitmap(ThumbImage);

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(mediaUri.mediaType == Constant.VIDEO_STATE){

                feedType = Constant.VIDEO_STATE;
                if (mediaUri.isFromGallery) {
                    String filePath = ImageVideoUtil.generatePath(Uri.parse(mediaUri.uriList.get(0)), this);
                    videoThumb = ImageVideoUtil.getVidioThumbnail(filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                } else {
                    videoThumb = ImageVideoUtil.getVideoToThumbnil(Uri.parse(mediaUri.uriList.get(0)),
                            this,
                            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND); //ImageVideoUtil.getCompressBitmap();
                   /* SuziLoader loader = new SuziLoader(); //Create it for once
                    loader.with(this) //Context
                            .load(mediaUri.uriList.get(0)) //Video path
                            .into(iv_postimage) // imageview to load the thumbnail
                            .type("mini") // mini or micro
                            .show();*/ // to show the thumbnail
                }

                if (videoThumb != null)
                    iv_postimage.setImageBitmap(videoThumb);
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
        tagList = null;
        tvLoaction = null;
        ivShareFbOn = null;
        ivShareTwitterOn= null;
        iv_postimage = null;
        session = null;
        lat = lng = null;
        tagAdapter = null;
        edCaption = null;
        tvMediaSize = null;
        hashTags = null;
        caption = null;
        lastTxt = isShare = tages = address = null;
        mediaUri = null;
        mAlertDialog = null;
        mDeleteCompressedMedia = false;
        mUploadUri = null;
        tempFile = null;
        videoThumb = null;
    }

    @Override
    public void onClick(View v) {
        caption = edCaption.getText().toString().trim();
        if (TextUtils.isEmpty(caption))
            caption = "";
        else getAllTags();

        switch (v.getId()) {
            case R.id.iv_feedPost:
                findViewById(R.id.iv_feedPost).setEnabled(false);
                KeyboardUtil.hideKeyboard(this.getCurrentFocus(), this);
                if(ConnectionDetector.isConnected()){

                    if (feedType == Constant.TEXT_STATE)
                        apiUploadTextFeed();
                    if (feedType == Constant.VIDEO_STATE) {
                        //String uri = mediaUri.uriList.get(0);
                        if (mUploadUri == null) {
                            initProgressBar();
                            mDeleteCompressedMedia = true;
                            //saveTempAndCompress(uri);
                            uploadVideo(videoThumb);
                        }else {
                            initProgressBar();
                            uploadVideo(videoThumb);
                        }
                        //sendToBackGroundService();
                    } else if (feedType == Constant.IMAGE_STATE)
                        apiCallForUploadImages();
                }
                else {
                    findViewById(R.id.iv_feedPost).setEnabled(true);
                    MySnackBar.showSnackbar(FeedPostActivity.this, findViewById(R.id.activity_add_post),
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
                }break;
        }
    }


    private void initProgressBar(){
        LayoutInflater li = LayoutInflater.from(this);
        View layout = li.inflate(R.layout.layout_processing_dialog, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                150, FrameLayout.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(layout);

        //mAlertDialog.setTitle("Prepare data for uploading video..");
        mAlertDialog = alertDialogBuilder.create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mAlertDialog.show();
    }

    private void showProgressBar(){
        if(mAlertDialog != null){
            mAlertDialog.show();
        }
    }

    private void hideProgressBar(){
        if(mAlertDialog != null){
            mAlertDialog.dismiss();
        }
    }

    private void sendToBackGroundService() {
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

      /*  if (Constant.isVideoUploading) {
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

    @Override
    public void onBackPressed() {
        KeyboardUtil.hideKeyboard(this.getCurrentFocus(), this);
        super.onBackPressed();
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
        if(tempFile!=null){
            try{
                tempFile.delete();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        edCaption.setText("");
        iv_postimage.setImageBitmap(null);
        iv_postimage.setVisibility(View.GONE);
    }


    private void apiUploadTextFeed(){
        initProgressBar();
        Map<String, String> map = prepareCommonPostData();
        new HttpTask(new HttpTask.Builder(this, "addFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                findViewById(R.id.iv_feedPost).setEnabled(true);
                hideProgressBar();
                parseResponce(response);
            }

            @Override
            public void ErrorListener(VolleyError error) {
                findViewById(R.id.iv_feedPost).setEnabled(true);
                hideProgressBar();
            }})
                .setAuthToken(session.getAuthToken())
                .setProgress(false)
                .setParam(map))
               // .setBody(map, HttpTask.ContentType.FORM_DATA))
                .postImage(null,null);
    }


    private Map<String, String> prepareCommonPostData(){
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
        map.put("location", address);
        map.put("tags", tages);
        map.put("serviceTagId", tages);
        map.put("userId", ""+session.getUser().id);

        if (lat != null && lng != null) {
            map.put("latitude", "" + lat);
            map.put("longitude", "" + lng);
        } else {
            checkLocationPermisssion();
            map.put("latitude", "");
            map.put("longitude", "");
        }

        return map;
    }

    // uploadimage call
    private void apiCallForUploadImages() {
        initProgressBar();
        Map<String, String> map = prepareCommonPostData();
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
                findViewById(R.id.iv_feedPost).setEnabled(true);
                hideProgressBar();
                parseResponce(responce);
            }

            @Override
            public void onError(String error) {
                findViewById(R.id.iv_feedPost).setEnabled(true);
                hideProgressBar();
                MyToast.getInstance(FeedPostActivity.this).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }
        }).execute();
    }

    private void parseResponce(String responce){
        try {
            JSONObject js = new JSONObject(responce);
            String status = js.getString("status");
             String message = js.getString("message");
            if (status.equalsIgnoreCase("success")) {
                resetView();
                setResult(Activity.RESULT_OK);
                finish();
            }else {
                MyToast.getInstance(this).showSmallMessage(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

   /* private void unregisterUploadReceiver() {
        if (receiverUpComplete != null) {
            FeedPostActivity.this.unregisterReceiver(receiverUpComplete);
        }
    }*/

  /*  @Override
    public void onPause() {
        super.onPause();
        unregisterUploadReceiver();
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
                        address = "Indore";
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

    private void uploadVideo(Bitmap videoThumb){

        Map<String, String> map = prepareCommonPostData();
        String uri = mediaUri.uriList.get(0);
        String path = ImageVideoUtil.generatePath(Uri.parse(uri), this);
        tempFile = new File(path);

        new HttpTask(new HttpTask.Builder(this, "addFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d(apiName, response);
                findViewById(R.id.iv_feedPost).setEnabled(true);
                hideProgressBar();
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        resetView();
                        setResult(Activity.RESULT_OK);
                        finish();
                    }else {
                        MyToast.getInstance(FeedPostActivity.this).showSmallMessage(message);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                Log.d("fdashgf", "dfaew");
                findViewById(R.id.iv_feedPost).setEnabled(true);
                hideProgressBar();
            }})
                .setAuthToken(session.getAuthToken())
                .setParam(map)
                .setProgress(false))
                .postFile("feed", tempFile, videoThumb);
    }

    private void deleteOutputFile(@Nullable String uri) {
        if (uri != null)
            new File(Uri.parse(uri).getPath()).delete();
    }

    private void saveTempAndCompress(String uri){
        String path = ImageVideoUtil.generatePath(Uri.parse(uri), this);
        tempFile = new File(path); //com.mualab.org.user.util.media.FileUtils.getFile(this, Uri.parse(uri));
        new VideoCompressor().execute();
    }

    class VideoCompressor extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
            Log.d(TAG,"Start video compression");
        }

        @Override
        protected String doInBackground(Void... voids) {
            return MediaController.getInstance().convertVideo(tempFile.getPath());
        }

        @Override
        protected void onPostExecute(String filePath) {
            super.onPostExecute(filePath);
            if(!filePath.equals("")){
                mUploadUri = filePath;
                Log.d(TAG,"Compression successfully!");
                if(mDeleteCompressedMedia){
                    uploadVideo(videoThumb);
                }
            }else {
                hideProgressBar();
            }
        }
    }
}
