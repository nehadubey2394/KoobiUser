package com.mualab.org.user.activity.feeds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hendraanggrian.socialview.Mention;
import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.FilteredAdapter;
import com.hendraanggrian.widget.SocialAutoCompleteTextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.UserSuggessionAdapter;
import com.mualab.org.user.activity.main.MainActivity;
import com.mualab.org.user.activity.people_tag.activity.DemoTagActivity;
import com.mualab.org.user.activity.people_tag.activity.PeopleTagActivity;
import com.mualab.org.user.activity.people_tag.instatag.TagToBeTagged;
import com.mualab.org.user.activity.people_tag.models.TagDetail;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.data.model.booking.Address;
import com.mualab.org.user.data.remote.GioAddressTask;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.data.remote.UploadImage;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.LocationDetector;
import com.mualab.org.user.utils.LocationUtil;
import com.mualab.org.user.utils.media.ImageVideoUtil;
import com.mualab.org.user.videocompressor.video.MediaController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class FeedPostActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = FeedPostActivity.class.getName();

    private LinkedHashSet<String> tagList = new LinkedHashSet<>();
    private TextView tvLoaction;
    private ImageView ivShareFbOn, ivShareTwitterOn, iv_postimage;

    private boolean isFbShared = true, isTwitteron = true;
    //private Double lat, lng;
    private TagAdapter tagAdapter;
    private UserSuggessionAdapter mentionAdapter;
    private SocialAutoCompleteTextView edCaption;
    private TextView tvMediaSize,tvTagCount;
    private Bitmap thumbImage = null;
    private ArrayList<String> hashTags = new ArrayList<>();
    //  private HashMap<Integer,ArrayList<TagToBeTagged>> taggedImgMap = new HashMap<>();

    private int feedType;
    private String caption;
    private String lastTxt;
    private String tages = "";

    private MediaUri mediaUri;
    private AlertDialog mAlertDialog;

    private Boolean mDeleteCompressedMedia = false;
    private String mUploadUri = null;

    private File tempFile;
    private Bitmap videoThumb;

    private Address address;

    private Handler handler;
    private Runnable runnable;
    private  long mLastClickTime = 0;
    private String tagJson="",tagIdsArray="";
    private  List<ArrayList<TagToBeTagged>> listOfValues ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_post);
        setStatusbarColor();
        Intent intent = getIntent();
        if (intent!= null) {
            caption = intent.getStringExtra("caption");
            thumbImage = intent.getParcelableExtra("thumbImage");
            mediaUri = (MediaUri) intent.getSerializableExtra("mediaUri");
            feedType = intent.getIntExtra("feedType", Constant.IMAGE_STATE);

            /*file:///storage/emulated/0/Android/data/com.mualab.org.user/cache/tmp.mp4*/
        }

        listOfValues = new ArrayList<>();

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
                    getDropDown(lastTxt, "");
                return null;
            }
        });

        edCaption.setMentionTextChangedListener(new Function2<SocialView, CharSequence, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, CharSequence s) {
                Log.d("editing", s.toString());
                lastTxt = s.toString();
                if (lastTxt.length() > 1)
                    getDropDown(lastTxt, "user");
                return null;
            }
        });

        iv_postimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaUri != null && mediaUri.uriList != null && mediaUri.uriList.size() > 0) {

                    if (mediaUri.mediaType == Constant.IMAGE_STATE) {

                        Intent intent = new Intent(FeedPostActivity.this, PreviewImageActivity.class);
                        intent.putExtra("imageArray", (Serializable) mediaUri.uriList);
                        intent.putExtra("startIndex", 0);
                        startActivity(intent);
                    } else if (mediaUri.mediaType == Constant.VIDEO_STATE) {
                        startActivity(new Intent(Intent.ACTION_VIEW)
                                .setDataAndType(Uri.parse(mediaUri.uriList.get(0)), "video/mp4")
                                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                    }
                }
            }
        });


        handler = new Handler();
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                handler = null;
                runnable = null;
                //checkLocationPermisssion();
                initProgressBar();
            }
        }, 1000);
    }

    private void getDropDown(String tag, final String type) {
        Map<String, String> map = new HashMap<>();
        map.put("search", tag);
        map.put("type", type);
        map.put("page", "0");
        map.put("limit", "5");
        Mualab.getInstance().getRequestQueue().cancelAll("TAG_SEARCH");

        new HttpTask(new HttpTask.Builder(this, "tagSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject object = new JSONObject(response);
                    hashTags.clear();
                    tagAdapter.clear();

                    if (object.has("allTags")) {
                        JSONArray array = object.getJSONArray("allTags");
                        if (type.equals("user")) {
                            mentionAdapter.clear();

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String fullname = obj.getString("firstName") + " " + obj.getString("lastName");
                                String username = obj.getString("userName");
                                String profileImage = obj.getString("profileImage");
                                mentionAdapter.add(new Mention(username, fullname, profileImage));
                            }
                            mentionAdapter.notifyDataSetChanged();
                        } else {

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                hashTags.add(obj.getString("tag").replace("#", ""));
                            }
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

            }
        })
                .setParam(map)
                .setProgress(false))
                .execute("TAG_SEARCH");
        lastTxt = tag;
    }

    private void viewDidLoad() {
        tvLoaction = findViewById(R.id.tv_loaction);
        ivShareFbOn = findViewById(R.id.iv_fb_on);
        ivShareTwitterOn = findViewById(R.id.iv_twitter_on);
        iv_postimage = findViewById(R.id.iv_selectedImage);
        if (thumbImage!=null)
            iv_postimage.setImageBitmap(thumbImage);

        edCaption = findViewById(R.id.edCaption);
        tvMediaSize = findViewById(R.id.tvMediaSize);
        tvTagCount = findViewById(R.id.tvTagCount);
        // progressBar = findViewById(R.id.progress_bar);

        //findViewById(R.id.iv_feedPost).setOnClickListener(this);
        findViewById(R.id.ly_location).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.ll_tagPepole).setOnClickListener(this);
        findViewById(R.id.ll_tagService).setOnClickListener(this);
        findViewById(R.id.tv_post).setOnClickListener(this);

        //hashtagAdapter = new HashtagAdapter(this);
        edCaption.setThreshold(1);
        edCaption.setHashtagEnabled(true);
        edCaption.setHyperlinkEnabled(true);
        edCaption.setHashtagColor(ContextCompat.getColor(this, R.color.colorPrimary));
        edCaption.setMentionColor(ContextCompat.getColor(this, R.color.colorAccent));

        mentionAdapter = new UserSuggessionAdapter(this);
        mentionAdapter.clear();
        edCaption.setMentionAdapter(mentionAdapter);
        Resources.Theme theme = getResources().newTheme();
        theme.applyStyle(R.style.Theme_AppCompat_Light, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mentionAdapter.setDropDownViewTheme(theme);
        }
        tagAdapter = new TagAdapter(this);
        edCaption.setHashtagAdapter(tagAdapter);
    }

    @SuppressLint("DefaultLocale")
    private void updateUi() {
        if(mediaUri != null){
            if (mediaUri.mediaType == Constant.IMAGE_STATE && mediaUri.uriList.size()>0){
                findViewById(R.id.ll_tagPepole).setVisibility(View.VISIBLE);
            }else {
                findViewById(R.id.ll_tagPepole).setVisibility(View.GONE);
            }

        }


        if (!TextUtils.isEmpty(caption)) {
            edCaption.setText(caption);
            edCaption.setSelection(edCaption.getText().length());
        }


        if (mediaUri != null) {

            if (mediaUri.mediaType == Constant.VIDEO_STATE) {
                if (thumbImage!=null){
                    videoThumb = thumbImage;
                }else {
                    String filePath = ImageVideoUtil.generatePath(Uri.parse(mediaUri.uri), this);
                    videoThumb = ImageVideoUtil.getVidioThumbnail(filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                }
            }
        } else {
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
        ivShareTwitterOn = null;
        iv_postimage = null;
        tagAdapter = null;
        edCaption = null;
        tvMediaSize = null;
        hashTags = null;
        caption = null;
        lastTxt = tages = null;
        address = null;
        mediaUri = null;
        mAlertDialog = null;
        mDeleteCompressedMedia = false;
        mUploadUri = null;
        tempFile = null;
        videoThumb = null;
        PeopleTagActivity.taggedImgMap.clear();
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 600){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        caption = edCaption.getText().toString().trim();
        if (TextUtils.isEmpty(caption))
            caption = "";
        else getAllTags();

        switch (v.getId()) {

            case R.id.ll_tagPepole:
                if (feedType == Constant.IMAGE_STATE){
                    if (mediaUri != null && mediaUri.uriList != null && mediaUri.uriList.size() > 0) {

                        if (mediaUri.mediaType == Constant.IMAGE_STATE) {

                            Intent intent = new Intent(FeedPostActivity.this, PeopleTagActivity.class);
                            intent.putExtra("imageArray", (Serializable) mediaUri.uriList);
                            intent.putExtra("startIndex", 0);
                            intent.putExtra("mediaUri", mediaUri);
                            // intent.putExtra("hashmap",  taggedImgMap);
                            startActivityForResult(intent, 100);
                            //   MyToast.getInstance(FeedPostActivity.this).showDasuAlert("Under developement");

                        } else if (mediaUri.mediaType == Constant.VIDEO_STATE) {

                            startActivity(new Intent(Intent.ACTION_VIEW)
                                    .setDataAndType(Uri.parse(mediaUri.uriList.get(0)), "video/mp4")
                                    .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                        }
                    }
                }

                // startActivity(new Intent(FeedPostActivity.this, DemoTagActivity.class));
                break;
            case R.id.ll_tagService:

                break;

            /*case R.id.iv_feedPost:
                feedPostPrerareData();
                break;*/

            case R.id.tv_post:
                if (listOfValues.size()!=0){
                    Gson gson = new GsonBuilder().create();
                    ArrayList<String> tagIdsArrayList = new ArrayList<>();
                    for (int i = 0; i<listOfValues.size();i++){
                        for (TagToBeTagged tag : listOfValues.get(i)){
                            HashMap<String,TagDetail> tagDetails = tag.getTagDetails();
                            for(Map.Entry map  :  tagDetails.entrySet() ) {
                                TagDetail tagDetail = tagDetails.get(map.getKey());
                                tagIdsArrayList.add(tagDetail.tagId);
                            }
                        }
                    }
                    tagIdsArray = gson.toJson(tagIdsArrayList);
                }
                feedPostPrerareData();
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

    private void showToast(String text) {
        if (!TextUtils.isEmpty(text))
            MyToast.getInstance(this).showSmallMessage(text);
    }

    /*prepare data before post to server*/
    private void feedPostPrerareData() {

        findViewById(R.id.tv_post).setEnabled(false);
        KeyboardUtil.hideKeyboard(Objects.requireNonNull(getCurrentFocus()), this);

        if (address == null || TextUtils.isEmpty(address.latitude)) {
            findViewById(R.id.tv_post).setEnabled(true);
            checkLocationPermisssion();
            return;
        }

        if (ConnectionDetector.isConnected()) {

            if (feedType == Constant.TEXT_STATE) {
                if (!TextUtils.isEmpty(caption)) {
                    showProgressBar();
                    apiUploadTextFeed();
                } else {
                    Animation shake = AnimationUtils.loadAnimation(FeedPostActivity.this, R.anim.shake);
                    edCaption.startAnimation(shake);
                    findViewById(R.id.tv_post).setEnabled(true);
                }

            } else if (feedType == Constant.VIDEO_STATE) {
                showProgressBar();
                //String uri = mediaUri.uriList.get(0);
                if (mUploadUri == null) {
                    mDeleteCompressedMedia = true;
                    //saveTempAndCompress(uri);
                    uploadVideo(videoThumb);
                } else {
                    uploadVideo(videoThumb);
                }
                //sendToBackGroundService();
            } else if (feedType == Constant.IMAGE_STATE) {
                showProgressBar();
                apiCallForUploadImages();
            }

        } else {
            findViewById(R.id.tv_post).setEnabled(true);
            showToast( getString(R.string.error_msg_network));
          /*  MySnackBar.showSnackbar(FeedPostActivity.this, findViewById(R.id.activity_add_post),
                    getString(R.string.error_msg_network));*/
        }
    }

    private void initProgressBar() {

        if(mAlertDialog==null){
            LayoutInflater li = LayoutInflater.from(this);
            @SuppressLint("InflateParams") View layout = li.inflate(R.layout.layout_processing_dialog, null);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    150, FrameLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(params);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(layout);

            //mAlertDialog.setTitle("Prepare data for uploading video..");
            mAlertDialog = alertDialogBuilder.create();
            mAlertDialog.setCancelable(false);
            mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void showProgressBar() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    private void hideProgressBar() {
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    private void sendToBackGroundService() {
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
                address = new LocationUtil().getAddressDetails(place);

                if(TextUtils.isEmpty(place.getName())){
                    String city = ""+place.getLocale();
                    String country = place.getLocale().getCountry();
                    if(TextUtils.isEmpty(city))
                        city = "";

                    if(!TextUtils.isEmpty(country))
                        address.placeName = city + ", " + country;
                    else address.placeName = city;
                }
                tvLoaction.setText(address.placeName);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
            }
        }else if(requestCode == Constant.REQUEST_CHECK_SETTINGS){

            final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    getLocation();
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to

                default:
                    break;
            }
        }else  if (requestCode == 100 && resultCode != 0) {
            if (data != null) {
                tagJson = data.getStringExtra("tagJson");
                listOfValues = (List<ArrayList<TagToBeTagged>>) data.getSerializableExtra("listOfValues");
                String tagCount = data.getStringExtra("tagCount");
                tvTagCount.setText(tagCount);
            }
        }
    }

    @Override
    public void onBackPressed() {
        KeyboardUtil.hideKeyboard(this.getCurrentFocus(), this);
        if(handler!=null)
            handler.removeCallbacks(runnable);
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
        if (tempFile != null) {
            try {
                //  boolean bool = tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        edCaption.setText("");
        iv_postimage.setImageBitmap(null);
        iv_postimage.setVisibility(View.GONE);
    }

    private void apiUploadTextFeed() {
        Map<String, String> map = prepareCommonPostData();
        new HttpTask(new HttpTask.Builder(this, "addFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                findViewById(R.id.tv_post).setEnabled(true);
                hideProgressBar();
                parseResponce(response);
            }

            @Override
            public void ErrorListener(VolleyError error) {
                findViewById(R.id.tv_post).setEnabled(true);
                hideProgressBar();
            }
        })
                .setAuthToken(Mualab.currentUser.authToken)
                .setProgress(false)
                .setParam(map))
                // .setBody(map, HttpTask.ContentType.FORM_DATA))
                .postImage(null, null);
    }

    private Map<String, String> prepareCommonPostData() {
        //address = TextUtils.isEmpty(address) ? "" : address;
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
        map.put("tag", tages);
        map.put("serviceTagId", "");
        map.put("userId", "" + Mualab.currentUser.id);
        map.put("location", address.placeName);
        map.put("city", TextUtils.isEmpty(address.city)?"":address.city);
        map.put("country", TextUtils.isEmpty(address.country)?"":address.country);
        if (TextUtils.isEmpty(address.latitude) || TextUtils.isEmpty(address.longitude)) {
            map.put("latitude", "");
            map.put("longitude", "");
        } else {
            map.put("latitude", "" + address.latitude);
            map.put("longitude", "" + address.longitude);
        }
        Gson gson = new GsonBuilder().create();
        ArrayList<String> tagIdsArrayList = new ArrayList<>();
        String emptyArray = gson.toJson(tagIdsArrayList);

        if (tagJson!=null && !tagJson.equals(""))
            map.put("peopleTag",tagJson);
        else
            map.put("peopleTag",emptyArray);

        if (tagIdsArray!=null && !tagIdsArray.equals(""))
            map.put("tagData", tagIdsArray);
        else
            map.put("tagData", emptyArray);

        return map;
    }

    // uploadimage call
    private void apiCallForUploadImages() {
        Map<String, String> map = prepareCommonPostData();
        List<Uri> uris = new ArrayList<>();
        for (String uri : mediaUri.uriList)
            uris.add(Uri.parse(uri));

        new UploadImage(FeedPostActivity.this,
                Mualab.currentUser.authToken, map,
                uris,
                new UploadImage.Listner() {
                    @Override
                    public void onResponce(String responce) {
                        findViewById(R.id.tv_post).setEnabled(true);
                        hideProgressBar();
                        parseResponce(responce);
                    }

                    @Override
                    public void onError(String error) {
                        findViewById(R.id.tv_post).setEnabled(true);
                        hideProgressBar();
                        MyToast.getInstance(FeedPostActivity.this).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                    }
                }).execute();
    }

    private void parseResponce(String responce) {
        try {
            JSONObject js = new JSONObject(responce);
            String status = js.getString("status");
            String message = js.getString("message");
            if (status.equalsIgnoreCase("success")) {
                PeopleTagActivity.taggedImgMap.clear();
                resetView();
                // setResult(Activity.RESULT_OK);
                Intent i = new Intent(FeedPostActivity.this, MainActivity.class);
                i.putExtra("FeedPostActivity","FeedPostActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            } else {
                MyToast.getInstance(this).showSmallMessage(message);
            }
        } catch (Exception e) {
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
        } else {
            getLocation();
        }
    }

    private void getLocation() {

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(FeedPostActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    new GioAddressTask(FeedPostActivity.this,
                            new LatLng(location.getLatitude(), location.getLongitude()),
                            new GioAddressTask.LocationListner() {
                                @Override
                                public void onSuccess(Address adr) {
                                    address = adr;
                                    feedPostPrerareData();
                                }
                            }).execute();

                }else getCurrentLocation();
            }
        });


    }

    private void getCurrentLocation(){
        LocationDetector locationDetector = new LocationDetector();
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(FeedPostActivity.this);
        if (locationDetector.isLocationEnabled(FeedPostActivity.this) && locationDetector.checkLocationPermission(FeedPostActivity.this)) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    FeedPostActivity.this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                new GioAddressTask(FeedPostActivity.this,
                                        new LatLng(location.getLatitude(), location.getLongitude()),
                                        new GioAddressTask.LocationListner() {
                                            @Override
                                            public void onSuccess(Address adr) {
                                                address = adr;
                                                feedPostPrerareData();
                                            }
                                        }).execute();
                            }
                        }
                    });

        }else {
            locationDetector.showLocationSettingDailod(FeedPostActivity.this);
        }
    }

    private class TagAdapter extends FilteredAdapter<String> {

        final Filter filter = new SocialFilter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return ((String) resultValue);
            }
        };

        private TagAdapter(Context context) {
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
        /*/storage/emulated/0/DCIM/Camera/20180808_113328.mp4*/
        Map<String, String> map = prepareCommonPostData();
        String uri = mediaUri.uri;
        //  String path = ImageVideoUtil.generatePath(Uri.parse(uri), this);
        //tempFile = new File(path);

        if (mediaUri.videoFile!=null)
            tempFile = mediaUri.videoFile;


        new HttpTask(new HttpTask.Builder(this, "addFeed", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                Log.d(apiName, response);
                findViewById(R.id.tv_post).setEnabled(true);
                hideProgressBar();
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {
                        resetView();
                      //  setResult(Activity.RESULT_OK);
                        Intent i = new Intent(FeedPostActivity.this, MainActivity.class);
                        i.putExtra("FeedPostActivity","FeedPostActivity");
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
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
                findViewById(R.id.tv_post).setEnabled(true);
                hideProgressBar();
            }})
                .setAuthToken(Mualab.currentUser.authToken)
                .setParam(map)
                .setProgress(false))
                .postFile("feed", tempFile, videoThumb);


        /*AndroidNetworking.upload(API.BASE_URL+"addFeed")
                .addMultipartFile("feed",tempFile)
                .addMultipartFile("videoThumb", null)
                .addMultipartParameter(map)
                .setTag("uploadTest")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d(TAG, "onProgress: "+bytesUploaded);
                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject js) {
                        // do anything with response
                        findViewById(R.id.tv_post).setEnabled(true);
                        hideProgressBar();
                        try {
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
                    public void onError(ANError error) {
                        // handle error
                    }
                });*/

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

    @SuppressLint("StaticFieldLeak")
    private class VideoCompressor extends AsyncTask<Void, Void, String> {

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

    protected void setStatusbarColor(){
        Window window = this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        // finally change the color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
    }

}
