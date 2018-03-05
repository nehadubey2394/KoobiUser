package com.mualab.org.user.activity.feeds.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
/*
 * Dharmraj acharya
 * */
public class FeedsFragment extends Fragment implements View.OnClickListener,FeedAdapter.Listener {

    private int CURRENT_FEED_STATE = 0;
    // ui components delecration
    private Context mContext;
    private TextView  tvImages, tvVideos, tvFeeds,tv_msg;
    private LinearLayout ll_header;

    private LiveUserAdapter liveUserAdapter;
    private ArrayList<LiveUserInfo> liveUserList;

    private EditText edCaption;
    private ImageView iv_selectedImage;
    private LinearLayout ll_progress;
    private EndlessRecyclerViewScrollListener endlesScrollListener;
    private FeedAdapter feedAdapter;
    //private ImagesAdapter imagesAdapter;
    private RecyclerView rvFeed;
    private List<Feeds> feeds;

    private String caption;
    private String feedType = "feeds";
  //  private boolean inProgressAPI;
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

        LiveUserInfo me = new LiveUserInfo();
        me.id = user.id;
        me.userName = "My Story";
        me.profileImage = user.profileImage;
        me.storyCount = 0;
        liveUserList.add(me);
        Progress.hide(mContext);
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
        RecyclerView rvMyStory = view.findViewById(R.id.recyclerView);
        rvFeed = view.findViewById(R.id.rvFeed);
        tv_msg = view.findViewById(R.id.tv_msg);
        ll_progress = view.findViewById(R.id.ll_progress);

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
                //apiForGetAllFeeds(page, 100, false);
            }
        };

        rvFeed.setAdapter(feedAdapter);
        rvFeed.addOnScrollListener(endlesScrollListener);
        getStoryList();
        updateViewType(R.id.ly_feeds);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ly_feeds:
            case R.id.ly_images:
            case R.id.ly_videos:
                updateViewType(view.getId());
                break;

            case R.id.tv_post:
                caption = edCaption.getText().toString().trim();
                Intent intent = null;
                ActivityOptionsCompat options = null;
                if(mediaUri!=null && mediaUri.uriList.size()>0){
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", TextUtils.isEmpty(caption)?"":caption);
                    intent.putExtra("mediaUri", mediaUri);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                    options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(getActivity(), (View)iv_selectedImage, "profile");
                }else if (!TextUtils.isEmpty(caption)) {
                    /*intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", caption);
                    intent.putExtra("feedType", Constant.TEXT_STATE);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);*/
                    MyToast.getInstance(mContext).showSmallMessage(getString(R.string.under_development));
                }

                if (intent != null) {
                    if(options!=null)
                    startActivityForResult(intent, Constant.POST_FEED_DATA, options.toBundle());
                    else startActivityForResult(intent, Constant.POST_FEED_DATA);
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
                    apiForGetAllFeeds(0, 100, true);
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
                    apiForGetAllFeeds( 0, 100, true);
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
                    apiForGetAllFeeds( 0, 100, true);
                    //ParseAndUpdateUI(loadVideoJSONFromAsset());
                }
                break;
        }

        lastFeedTypeId = id;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //Progress.hide(mContext);
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
    }

    private void apiForGetAllFeeds(final int page, final int feedLimit, final boolean isEnableProgress){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetAllFeeds(page, feedLimit, isEnableProgress);
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
        Mualab.getInstance().cancelPendingRequests(this.getClass().getName());
        new HttpTask(new HttpTask.Builder(mContext, "getAllFeeds", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                ll_progress.setVisibility(View.GONE);
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
                   // MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                ll_progress.setVisibility(View.GONE);
                //MyToast.getInstance(mContext).showSmallMessage(getString(R.string.msg_some_thing_went_wrong));
            }})
                .setAuthToken(user.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute(this.getClass().getName());
        ll_progress.setVisibility(isEnableProgress?View.VISIBLE:View.GONE);
    }

    private void ParseAndUpdateUI(final String response) {

        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");
           // inProgressAPI = false;
            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                JSONArray array = js.getJSONArray("AllFeeds");
                for (int i = 0; i < array.length(); i++) {
                    Gson gson = new Gson();
                    JSONObject jsonObject = array.getJSONObject(i);
                    Feeds feed = gson.fromJson(String.valueOf(jsonObject), Feeds.class);

                    /*tmp get data and set into actual json format*/
                    Feeds.User user = feed.userInfo.get(0);
                    feed.userName = user.userName;
                    feed.fullName = user.firstName+" "+user.lastName;
                    feed.profileImage = user.profileImage;
                    feed.userId = user._id;
                    feed.crd =feed.timeElapsed;


                    if(feed.feedData!=null){

                        feed.feed = new ArrayList<>();
                        feed.feedThumb = new ArrayList<>();
                        for(Feeds.Feed tmp :feed.feedData){
                            feed.feed.add(tmp.feedPost);

                            if(!TextUtils.isEmpty(feed.feedData.get(0).videoThumb))
                                feed.feedThumb.add(tmp.feedPost);
                        }

                    }
                        feed.videoThumbnail = feed.feedData.get(0).videoThumb;



                    feeds.add(feed);
                }

                feedAdapter.notifyDataSetChanged();

            } else if (status.equals("fail") && feeds.size()==0) {
                rvFeed.setVisibility(View.GONE);
                tv_msg.setVisibility(View.VISIBLE);
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
            //inProgressAPI = false;
            feedAdapter.notifyDataSetChanged();
            //MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }finally {
            //Progress.hide(mContext);
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
                                me.userName = "You";

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
                .execute("getMyStoryUser");
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
       // String filePath;

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
                            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                                    BitmapFactory.decodeFile(
                                            ImageVideoUtil.generatePath(picUri, mContext)), 150, 150);
                            updatePostImageUI(ThumbImage);
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
                            Bitmap ThumbImage = ThumbnailUtils
                                    .extractThumbnail(BitmapFactory.decodeFile(
                                            ImageVideoUtil.generatePath(tmpUri.get(0), mContext)), 100, 100);
                           // Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(mediaUri.uri));
                            updatePostImageUI(ThumbImage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;


                case Constant.POST_FEED_DATA:
                    resetView();
                    feeds.clear();
                    endlesScrollListener.resetState();
                    apiForGetAllFeeds(0, 100, true);
                    break;

                case Constant.REQUEST_VIDEO_CAPTURE:
                    try {
                        mediaUri = new MediaUri();
                        mediaUri.isFromGallery = false;
                        mediaUri.mediaType = Constant.VIDEO_STATE;
                        mediaUri.addUri(String.valueOf(data.getData()));
                        updatePostImageUI(ImageVideoUtil.getVideoToThumbnil(data.getData(), mContext));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case Constant.GALLERY_INTENT_CALLED:
                    mediaUri = new MediaUri();
                    mediaUri.isFromGallery = true;
                    mediaUri.mediaType = Constant.VIDEO_STATE;
                    mediaUri.addUri(String.valueOf(data.getData()));

                    try {
                        String filePath = PathUtil.getPath(mContext, Uri.parse(mediaUri.uri));
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
                            //filePath = ImageVideoUtil.generatePath(Uri.parse(mediaUri.uri), mContext);
                            //thumbBitmap = ImageVideoUtil.getVidioThumbnail(filePath); //ImageVideoUtil.getCompressBitmap();
                            updatePostImageUI(ImageVideoUtil.getVideoToThumbnil(data.getData(), mContext));
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
        tvImages.setTextColor(getResources().getColor(R.color.text_color));
        tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
        tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));
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
