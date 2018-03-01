package com.mualab.org.user.activity.feeds.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.image.picker.ImagePicker;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.FeedPostActivity;
import com.mualab.org.user.activity.feeds.adapter.FeedAdapter;
import com.mualab.org.user.activity.feeds.adapter.FeedItemAnimator;
import com.mualab.org.user.activity.feeds.adapter.LiveUserAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.SelectableDialog;
import com.mualab.org.user.enums.PermissionType;
import com.mualab.org.user.model.MediaUri;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.model.feeds.LiveUserInfo;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;
import com.mualab.org.user.util.media.ImageVideoUtil;
import com.mualab.org.user.util.media.PathUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class FeedsFragment extends Fragment implements View.OnClickListener,FeedAdapter.Listener {

    private int CURRENT_FEED_STATE = 0;
    // ui components delecration
    private Context mContext;
    private TextView  tvImages, tvVideos, tvFeeds,tvNoData;
    private LinearLayout ll_header;

    private LiveUserAdapter liveUserAdapter;
    private ArrayList<LiveUserInfo> liveUserList;

    private EditText edCaption;
    private ImageView iv_selectedImage;
    private EndlessRecyclerViewScrollListener endlesScrollListener;
    private FeedAdapter feedAdapter;
    //private ImagesAdapter imagesAdapter;
    private RecyclerView rvFeed;
    private List<Feeds> feeds;

    private String caption;
    private String feedType = "feeds";
    private boolean inProgressAPI;
    private int lastFeedTypeId;
    private PermissionType permissionType;
    private User user;

    private MediaUri mediaUri;

    public FeedsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    public static FeedsFragment newInstance(String param1) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = Mualab.getInstance().getSessionManager().getUser();
        feeds = new ArrayList<>();
        liveUserList = new ArrayList<>();
        liveUserList.clear();
        User user = Mualab.getInstance().getSessionManager().getUser();
        LiveUserInfo me = new LiveUserInfo();
        me.id = user.id;
        me.fullName = "My Story";
        me.profileImage = user.profileImage;
        me.storyCount = 0;
        liveUserList.add(me);
        //loadLiveUserJSONFromAsset();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feeds, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View view){
        ll_header = view.findViewById(R.id.ll_header);
        tvImages = view.findViewById(R.id.tvImages);
        tvVideos =  view.findViewById(R.id.tvVideos);
        tvFeeds = view.findViewById(R.id.tvFeeds);
        tvNoData = view.findViewById(R.id.tvNoData);
        RecyclerView rvMyStory = view.findViewById(R.id.recyclerView);
        rvFeed = view.findViewById(R.id.rvFeed);

        view.findViewById(R.id.ly_images).setOnClickListener(this);
        view.findViewById(R.id.ly_videos).setOnClickListener(this);
        view.findViewById(R.id.ly_feeds).setOnClickListener(this);
        addRemoveHeader(true);

        liveUserAdapter = new LiveUserAdapter(mContext, liveUserList);
        rvMyStory.setAdapter(liveUserAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager lm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        rvFeed.setItemAnimator(new FeedItemAnimator());
        rvFeed.setLayoutManager(lm);
        rvFeed.setHasFixedSize(true);

        feedAdapter = new FeedAdapter(mContext, feeds, this);
        endlesScrollListener = new EndlessRecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                apiForGetAllFeeds(page, 100);
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);

        getStoryList();

        Progress.show(mContext);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateViewType(R.id.ly_feeds);
            }
        }, 1000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ly_feeds:
            case R.id.ly_images:
            case R.id.ly_videos:
                updateViewType(view.getId());
                break;
            /*case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvVideos.setTextColor(getResources().getColor(R.color.text_color));
                tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
                updateViewType(view.getId());
                break;
            case R.id.ly_videos:
                tvImages.setTextColor(getResources().getColor(R.color.text_color));
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
                updateViewType(view.getId());
                break;
            case R.id.ly_feeds:
                tvImages.setTextColor(getResources().getColor(R.color.text_color));
                tvVideos.setTextColor(getResources().getColor(R.color.text_color));
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));
                updateViewType(view.getId());
                break;*/

            case R.id.tv_post:

                caption = edCaption.getText().toString().trim();
                Intent intent = null;
                if(mediaUri!=null && mediaUri.uriList.size()>0){
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", TextUtils.isEmpty(caption)?"":caption);
                    intent.putExtra("mediaUri", mediaUri);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                }else if (!TextUtils.isEmpty(caption)) {
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", caption);
                    intent.putExtra("feedType", Constant.TEXT_STATE);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                }

                if (intent != null) {
                    startActivityForResult(intent, Constant.POST_FEED_DATA);
                }
                break;

            case R.id.iv_video_popup:
                permissionType = PermissionType.VIDEO;
                checkPermissionAndPicImageOrVideo("Select Video");
                break;


            case R.id.iv_get_img:
                permissionType = PermissionType.IMAGE;
                checkPermissionAndPicImageOrVideo("Select Image");
                break;
        }
    }

    private void addRemoveHeader(final boolean wantAddView){
        if(wantAddView && ll_header.getChildCount()==0){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            @SuppressLint("InflateParams")
            View inflatedLayout= inflater.inflate(R.layout.post_add_header_layout, null, false);
            ll_header.addView(inflatedLayout);

            iv_selectedImage = ll_header.findViewById(R.id.iv_selectedImage);
            iv_selectedImage.setOnClickListener(this);
            //ll_header.findViewById(R.id.ivWeb).setOnClickListener(this);
            ll_header.findViewById(R.id.iv_get_img).setOnClickListener(this);
            ll_header.findViewById(R.id.iv_video_popup).setOnClickListener(this);
            ll_header.findViewById(R.id.tv_post).setOnClickListener(this);
            edCaption = ll_header.findViewById(R.id.ed_caption);
            edCaption.setText("");

        }else if(ll_header!=null && ll_header.getChildCount()>0){
            ll_header.removeAllViews();
        }
    }

    private void updateViewType(int id) {
        tvVideos.setTextColor(getResources().getColor(R.color.text_color));
        tvImages.setTextColor(getResources().getColor(R.color.text_color));
        tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
        endlesScrollListener.resetState();

        switch (id) {
            case R.id.ly_feeds:
                //addRemoveHeader(true);
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));

                if (lastFeedTypeId != R.id.ly_feeds){
                    feeds.clear();
                    feedType = "";
                    CURRENT_FEED_STATE = Constant.FEED_STATE;
                    apiForGetAllFeeds(0, 100);
                    //ParseAndUpdateUI(loadAllFeedJSONFromAsset());
                }
                break;

            case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
               // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_images){
                    feeds.clear();
                    feedType = "image";
                    CURRENT_FEED_STATE = Constant.IMAGE_STATE;
                    apiForGetAllFeeds( 0, 100);
                    //ParseAndUpdateUI(loadImageJSONFromAsset());
                }

                break;

            case R.id.ly_videos:
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
               // addRemoveHeader(false);
                if (lastFeedTypeId != R.id.ly_videos){
                    feeds.clear();
                    feedType = "video";
                    CURRENT_FEED_STATE = Constant.VIDEO_STATE;
                    apiForGetAllFeeds( 0, 100);
                    //ParseAndUpdateUI(loadVideoJSONFromAsset());
                }
                break;
        }

        lastFeedTypeId = id;
    }

    /*public String loadAllFeedJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("all_feeds_data.json");
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
    public String loadImageJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("feeds_image_data.json");
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
    public String loadVideoJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("feeds_video_data.json");
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
    }*/


    private void apiForGetAllFeeds(final int page, final int feedLimit){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetAllFeeds(page, feedLimit);
                    }

                }
            }).show();
        }


        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", String.valueOf(feedLimit));
        params.put("type", "home");
        // params.put("appType", "user");

        new HttpTask(new HttpTask.Builder(mContext, "getAllFeeds", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        removeProgress();
                        ParseAndUpdateUI(response);
                    }else MyToast.getInstance(mContext).showSmallMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                //MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }})
                .setAuthToken(user.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(true)
                .setBodyContentType(HttpTask.ContentType.APPLICATION_JSON))
                .execute(this.getClass().getName());
    }

    private void ParseAndUpdateUI(final String response) {

        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");
            inProgressAPI = false;
            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                JSONArray array = js.getJSONArray("Feeds");
                for (int i = 0; i < array.length(); i++) {
                    Gson gson = new Gson();
                    JSONObject jsonObject = array.getJSONObject(i);
                    Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);
                    feeds.add(feed);
                }

                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size()==0) {
                rvFeed.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
                feedAdapter.notifyDataSetChanged();
               /* if (type == Constant.IMAGE_STATE) {
                    imagesAdapter.notifyDataSetChanged();
                } else if (type == Constant.VIDEO_STATE) {
                    imagesAdapter.notifyDataSetChanged();
                } else if (type == Constant.FEED_STATE) {
                    feedAdapter.notifyDataSetChanged();
                }*/
            }

        } catch (JSONException e) {
            e.printStackTrace();
            inProgressAPI = false;
            MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }finally {
            Progress.hide(mContext);
        }
    }

    private void removeProgress(){

        if(feeds!=null && feeds.size()>0 && feeds.get(feeds.size()-1)==null){
            int lastIndex = feeds.size()-1;
            feeds.remove(lastIndex);
            feedAdapter.notifyDataSetChanged();
            /*if (type == Constant.IMAGE_STATE) {
                imagesAdapter.notifyItemRemoved(lastIndex);
            } else if (type == Constant.VIDEO_STATE) {
                imagesAdapter.notifyItemRemoved(lastIndex);
            } else if (type == Constant.FEED_STATE) {
                feedAdapter.notifyItemRemoved(lastIndex);
            }*/
        }
    }


    @Override
    public void onCommentBtnClick(Feeds feed, int pos) {

    }


    private void getStoryList(){
        Map<String, String> params = new HashMap<>();
       // params.put("feedType", feedType);

        new HttpTask(new HttpTask.Builder(mContext, "getMyStoryUser", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    Progress.hide(mContext);
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray array = js.getJSONArray("myStoryList");

                        for (int i = 0; i < array.length(); i++) {
                            Gson gson = new Gson();
                            JSONObject jsonObject = array.getJSONObject(i);
                            LiveUserInfo live = gson.fromJson(String.valueOf(jsonObject), LiveUserInfo.class);

                            if(live.id == user.id){
                                LiveUserInfo me = liveUserList.get(0);
                                me.firstName = live.firstName;
                                me.lastName = live.firstName;
                                me.fullName = live.firstName+" "+live.lastName;
                                me.storyCount = live.storyCount;

                            }else liveUserList.add(live);
                        }
                        liveUserAdapter.notifyDataSetChanged();
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    Progress.hide(mContext);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
            }})
                .setAuthToken(user.authToken)
                .setParam(params)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.APPLICATION_JSON))
                .execute(this.getClass().getName());
    }



    public void checkPermissionAndPicImageOrVideo(String title) {
        SelectableDialog dialog = new SelectableDialog(mContext, new SelectableDialog.Listener() {
            @Override
            public void onGalleryClick() {

                if(permissionType == PermissionType.IMAGE){
                    Matisse.from(FeedsFragment.this)
                            .choose(MimeType.allOf())
                            .countable(true)
                            .maxSelectable(10)
                            //  .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                            .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .imageEngine(new GlideEngine())
                            .forResult(Constant.REQUEST_CODE_CHOOSE);
                }else {
                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Video"),
                            Constant.GALLERY_INTENT_CALLED);
                }
            }

            @Override
            public void onCameraClick() {
                if(permissionType == PermissionType.IMAGE){
                    ImagePicker.pickImageFromCamera(FeedsFragment.this);
                }else {
                    //mediaUri = null;
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        long maxVideoSize = 10*1024*1024; // 10 MB
                        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10000);
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
                        startActivityForResult(intent, Constant.REQUEST_VIDEO_CAPTURE);
                    }
                }
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.setTitle(title);
        if(Build.VERSION.SDK_INT>=23){
            if (mContext.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            }else dialog.show();
        }
        else dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap thumbBitmap;
        String filePath;

        if (resultCode == RESULT_OK) {

            switch (requestCode){
                case Constant.CAMERA_REQUEST:
                    try {

                        Bitmap bitmap = ImagePicker.getImageFromResult(mContext, requestCode,resultCode,data);
                        Uri picUri = ImagePicker.getImageURIFromResult(mContext,requestCode,resultCode,data);
                        if(bitmap!=null && picUri!=null){
                            mediaUri = new MediaUri();
                            mediaUri.isFromGallery = false;
                            mediaUri.mediaType = Constant.IMAGE_STATE;
                            mediaUri.addUri(String.valueOf(picUri));
                            updatePostImageUI(bitmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case Constant.REQUEST_CODE_CHOOSE:
                    List<Uri> tmpUri = Matisse.obtainResult(data);
                    if (tmpUri.size() > 0) {
                        mediaUri = new MediaUri();
                        mediaUri.isFromGallery = true;
                        mediaUri.mediaType = Constant.IMAGE_STATE;

                        List<String>uriList = new ArrayList<>();
                        for(Uri tmp : tmpUri){
                            uriList.add(String.valueOf(tmp));
                        }
                        mediaUri.addUri(uriList);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(mediaUri.uri));
                            updatePostImageUI(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case Constant.POST_FEED_DATA:
                    resetView();
                   // setPagination(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
                    //callAPI("", 0, "10", Constant.FEED_STATE);
                    break;

                case Constant.REQUEST_VIDEO_CAPTURE:
                    try {
                        mediaUri = new MediaUri();
                        mediaUri.isFromGallery = false;
                        mediaUri.mediaType = Constant.VIDEO_STATE;
                        mediaUri.addUri(String.valueOf(data.getData()));
                        thumbBitmap = ImageVideoUtil.getVideoToThumbnil(Uri.parse(mediaUri.uri), mContext); //ImageVideoUtil.getCompressBitmap();
                        updatePostImageUI(thumbBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case Constant.GALLERY_INTENT_CALLED:
                    mediaUri = new MediaUri();
                    mediaUri.isFromGallery = true;
                    mediaUri.mediaType = Constant.VIDEO_STATE;
                    mediaUri.uri = String.valueOf(data.getData());
                    mediaUri.addUri(String.valueOf(data.getData()));

                    try {
                        filePath = PathUtil.getPath(mContext, Uri.parse(mediaUri.uri));
                        assert filePath != null;
                        File file = new File(filePath);
                        // Get length of file in bytes
                        long fileSizeInBytes = file.length();
                        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                        long fileSizeInKB = fileSizeInBytes / 1024;
                        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                        long fileSizeInMB = fileSizeInKB / 1024;

                        if(fileSizeInMB>50){
                            mediaUri = null;
                            updatePostImageUI(null);
                            MyToast.getInstance(mContext).showSmallMessage("You can't upload more then 50mb.");
                        }else {
                            filePath = ImageVideoUtil.generatePath(Uri.parse(mediaUri.uri), mContext);
                            thumbBitmap = ImageVideoUtil.getVidioThumbnail(filePath); //ImageVideoUtil.getCompressBitmap();
                            updatePostImageUI(thumbBitmap);
                        }
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    break;

                case Constant.ACTIVITY_COMMENT:
                    if(CURRENT_FEED_STATE == Constant.FEED_STATE){
                        int pos = data.getIntExtra("feedPosition",0);
                        Feeds feed = (Feeds) data.getSerializableExtra("feed");
                       // allfeedsList.get(pos).commentCount = feed.commentCount;
                        feedAdapter.notifyItemChanged(pos);
                    }
                    break;
            }

        } else {

            switch (requestCode){
                case Constant.CAMERA_REQUEST:
                case Constant.REQUEST_CODE_CHOOSE:
                case Constant.REQUEST_VIDEO_CAPTURE:
                case Constant.GALLERY_INTENT_CALLED:
                case Constant.GALLERY_KITKAT_INTENT_CALLED:
                    resetView();
                    break;

                case Constant.POST_FEED_DATA:
                case Constant.ACTIVITY_COMMENT:
                    break;
            }
        }
    }

    private void resetView(){
        mediaUri = null;
        caption = "";
        updatePostImageUI(null);
    }

    private void updatePostImageUI(Bitmap bitmap) {
        if (bitmap != null) {
            iv_selectedImage.setVisibility(View.VISIBLE);
            iv_selectedImage.setImageBitmap(bitmap);
        } else {
            iv_selectedImage.setImageBitmap(null);
            iv_selectedImage.setVisibility(View.GONE);
            edCaption.setText("");
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionAndPicImageOrVideo("Select Image");
                } else {
                    MyToast.getInstance(mContext).showSmallMessage("YOUR  PERMISSION DENIED ");
                }
            }
            break;
        }
    }
}
