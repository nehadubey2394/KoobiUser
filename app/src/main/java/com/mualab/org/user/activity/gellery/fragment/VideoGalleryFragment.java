package com.mualab.org.user.activity.gellery.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.FeedPostActivity;
import com.mualab.org.user.activity.gellery.Gallery2Activity;
import com.mualab.org.user.activity.gellery.GalleryActivity;
import com.mualab.org.user.activity.gellery.adapter.VideoGridViewAdapter;
import com.mualab.org.user.activity.gellery.model.Media;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.ScreenUtils;
import com.mualab.org.user.utils.WrapContentGridLayoutManager;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.utils.media.ImageVideoUtil;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class VideoGalleryFragment extends Fragment implements View.OnClickListener,
        VideoGridViewAdapter.Listener{

    CollapsingToolbarLayout collapsing_toolbar;
    private List<Media> albumList;
    private Context context;
    private Gallery2Activity  activity;
    //private ImageView rotateImage;
    private AppBarLayout appbar;
    //  private VideoGridViewAdapter videoAdapter;
    private VideoView videoView;
    private MediaUri mediaUri;
    private  Bitmap thumbImage = null;
    private ProgressBar progrss;
    private RecyclerView recyclerView;

    public VideoGalleryFragment() {
        // Required empty public constructor
    }


    public static VideoGalleryFragment newInstance() {
        VideoGalleryFragment fragment = new VideoGalleryFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }

        new GetVideoList().execute();

        initView(view);

        // Disable "Drag" for AppBarLayout (i.e. User can't scroll appBarLayout by directly touching appBarLayout - User can only scroll appBarLayout by only using scrollContent)
        if (appbar.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour = new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }
    }

    private void initView(View view){

        albumList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerView);
        videoView = view.findViewById(R.id.videoView);
        progrss = view.findViewById(R.id.progrss);
        //    mediaController= new MediaController(context);
        //  mediaController.setAnchorView(videoView);

        view.findViewById(R.id.tvNext).setOnClickListener(this);
        view.findViewById(R.id.tvClose).setOnClickListener(this);
        appbar = view.findViewById(R.id.appbar);

        collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);

        // rootLayout = view.findViewById(R.id.rootLayout);

        int mNoOfColumns = ScreenUtils.calculateNoOfColumns(context.getApplicationContext());
        WrapContentGridLayoutManager wgm = new WrapContentGridLayoutManager(context,
                mNoOfColumns<3?3:mNoOfColumns, LinearLayoutManager.VERTICAL, false);
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(wgm);
        // recyclerView.setHasFixedSize(true);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if(context instanceof Gallery2Activity)
            activity = (Gallery2Activity) context;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tvClose:
                ((Gallery2Activity)context).onBackPressed();
                break;

            case R.id.tvNext:
                if (mediaUri!=null){
                    Intent  intent = new Intent(context, FeedPostActivity.class);
                    intent.putExtra("caption", "");
                    intent.putExtra("mediaUri", mediaUri);
                    intent.putExtra("thumbImage", thumbImage);
                    intent.putExtra("feedType", mediaUri.mediaType);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, Constant.POST_FEED_DATA);
                    videoView.stopPlayback();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK && requestCode== Constant.POST_FEED_DATA){
            //  ((GalleryActivity)context).setResult(Activity.RESULT_OK);
            ((GalleryActivity)context).finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){

        }
    }

    private List<Media> getAllVideoPath() {
        List<Media> albumList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Video.VideoColumns.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        //int vidsCount = 0;
        if (cursor != null) {
            //vidsCount = cursor.getCount();
            //Log.d(TAG, "Total count of videos: " + vidsCount);
            while (cursor.moveToNext()) {
                Media media = new Media();
                media.uri= Uri.parse(cursor.getString(0));
                String filePath = ImageVideoUtil.generatePath(media.uri, context);
                media.thumbImage = ImageVideoUtil.getVidioThumbnail(filePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                if (!filePath.contains(".3gp") && media.thumbImage!=null)
                    albumList.add(media);
                //Log.d(TAG, cursor.getString(0));
            }
            cursor.close();
        }

        return albumList;
    }

    @Override
    public void onViewClick(Media media, int index) {
        String filePath = null;
        try {
            filePath = String.valueOf(media.uri);
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
                MyToast.getInstance(context).showSmallMessage("You can't upload more then 50mb.");
            }else {
                filePath = ImageVideoUtil.generatePath(media.uri, context);
                media.thumbImage = ImageVideoUtil.getVidioThumbnail(filePath); //ImageVideoUtil.getCompressBitmap();

                mediaUri = new MediaUri();
                mediaUri.uri = String.valueOf(media.uri);
                mediaUri.uriList.add(String.valueOf(media.uri));
                mediaUri.mediaType = Constant.VIDEO_STATE;
                mediaUri.isFromGallery = true;
                thumbImage = media.thumbImage;

                //  videoView.setMediaController(mediaController);
                videoView.setVideoURI(media.uri);
                videoView.requestFocus();
                videoView.start();
                expandToolbar();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

      /*  context.startActivity(new Intent(Intent.ACTION_VIEW)
                .setDataAndType(media.uri, "video/mp4")
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));*/

        /*videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //close the progress dialog when buffering is done
                progrss.setVisibility(View.GONE);
            }
        });*/
    }

    public void expandToolbar(){
        appbar.setExpanded(true, true);
    }

    private class GetVideoList extends AsyncTask<URI, Void, List<Media>> {

        @Override
        protected List<Media> doInBackground(URI... uris) {
            return getAllVideoPath();
        }

        protected void onPostExecute(List<Media> result) {
            albumList = result;
            VideoGridViewAdapter videoAdapter = new VideoGridViewAdapter(context, albumList);
            recyclerView.setAdapter(videoAdapter);
            videoAdapter.setListener(VideoGalleryFragment.this);

            if(albumList!=null && albumList.size()>0){
                Media media = albumList.get(0);
                //showImage.setUri(media.uri);
                try {
                    progrss.setVisibility(View.GONE);
                    // videoView.setMediaController(mediaController);
                    videoView.setVideoURI(media.uri);
                    videoView.requestFocus();
                    videoView.start();
                    //showImage.setImageBitmap(ImageVideoUtil.getBitmapFromUri(context,media.uri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            videoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaUri = null;
        albumList.clear();
        videoView = null;
        thumbImage = null;
    }
}
