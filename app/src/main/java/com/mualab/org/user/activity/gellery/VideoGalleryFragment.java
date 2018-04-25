package com.mualab.org.user.activity.gellery;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.image.nocropper.BitmapUtils;
import com.image.nocropper.CropperCallback;
import com.image.nocropper.CropperView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.FeedPostActivity;
import com.mualab.org.user.activity.gellery.adapter.GalleryAdapter;
import com.mualab.org.user.activity.gellery.model.Media;
import com.mualab.org.user.activity.gellery.model.PhotoLoader;
import com.mualab.org.user.activity.gellery.model.VideoLoader;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.MySnackBar;
import com.mualab.org.user.listner.GalleryOnClickListener;
import com.mualab.org.user.model.MediaUri;
import com.mualab.org.user.util.media.ImageVideoUtil;
import com.zhihu.matisse.internal.loader.AlbumLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class VideoGalleryFragment extends BaseGalleryFragment implements View.OnClickListener{

    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    CollapsingToolbarLayout collapsing_toolbar;
    int numberOfColumns = 4;
    View view;


    private CropperView showImage;
    private ImageView snap_button;
    //private ImageView rotateImage;
    private ImageView ivMultiSelection, ivImage;
    private AppBarLayout appbar;
    private CoordinatorLayout rootLayout;
    private boolean isSupportMultipal;
    private boolean isSnappedToCenter;


    private int lastindex;
    private Uri lastSelectedUri;
    private LinkedHashMap<String, Uri> mSelected;
    private List<Media> albumList;

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
        albumList = new ArrayList<>();
        mSelected = new LinkedHashMap<>();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        initView(view);
        return view;
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


        galleryAdapter = new GalleryAdapter(albumList, context, new GalleryOnClickListener() {
            @Override
            public void OnClick(Media media , int index) {

                if(isSupportMultipal){

                    int size = mSelected.size();
                    if(size<10){

                        if(!media.isSelected){
                            media.isSelected = !media.isSelected;
                            mSelected.put(media.uri.toString(), media.uri);
                            lastSelectedUri = media.uri;
                            lastindex = index;

                        } else{

                            if(mSelected.size()>1){
                                media.isSelected = !media.isSelected;
                                mSelected.remove(media.uri.toString());
                                Map.Entry<String, Uri> last = null;
                                for (Map.Entry<String, Uri> e : mSelected.entrySet()) last = e;
                                lastSelectedUri = last.getValue();
                            }
                        }
                    }else if(media.isSelected && size>1){
                        media.isSelected = !media.isSelected;
                        mSelected.remove(media.uri.toString());
                        Map.Entry<String, Uri> last = null;
                        for (Map.Entry<String, Uri> e : mSelected.entrySet()) last = e;
                        lastSelectedUri = last.getValue();
                    } else if(size>9){
                        MySnackBar.showSnackbar(context,rootLayout,"You can select max 10 items");
                    }

                }else {
                    lastindex = index;
                    lastSelectedUri = media.uri;
                    mSelected.clear();
                    mSelected.put(media.uri.toString(), media.uri);
                }

                refreshUi(lastSelectedUri);

                galleryAdapter.notifyItemChanged(index);
                if(mSelected.size()<=2)
                    recyclerView.smoothScrollToPosition(index);
                expandToolbar();
            }
        });
        recyclerView.setAdapter(galleryAdapter);
    }

    private void initView(View view){
        showImage = view.findViewById(R.id.showImage);
        snap_button = view.findViewById(R.id.snap_button);
        ivImage = view.findViewById(R.id.ivImage);
        // rotateImage = view.findViewById(R.id.rotateImage);
        ivMultiSelection = view.findViewById(R.id.ivMultiSelection);
        view.findViewById(R.id.tvNext).setOnClickListener(this);
        view.findViewById(R.id.ivClose).setOnClickListener(this);
        appbar = view.findViewById(R.id.appbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(context, numberOfColumns));
        rootLayout = view.findViewById(R.id.rootLayout);
        snap_button.setOnClickListener(this);
        // rotateImage.setOnClickListener(this);
        ivMultiSelection.setOnClickListener(this);
        albumList = getAlbums();

        if(albumList!=null && albumList.size()>0){
            Media media = albumList.get(0);
            lastSelectedUri = media.uri;
            mSelected.put(media.uri.toString(), media.uri);
            refreshUi(lastSelectedUri);
            //showImage.setUri(media.uri);
            try {
                showImage.setImageBitmap(ImageVideoUtil.getBitmapFromUri(context,media.uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void refreshUi(Uri uri){

        if(isSupportMultipal){
            ivImage.setVisibility(View.VISIBLE);
            showImage.setVisibility(View.GONE);
            ivImage.setImageURI(uri);

        } else {
            showImage.setVisibility(View.VISIBLE);
            ivImage.setVisibility(View.GONE);
            //showImage.setUri(uri);
            try {
                showImage.setImageBitmap(ImageVideoUtil.getBitmapFromUri(context,uri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public void expandToolbar(){
        appbar.setExpanded(true, true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.ivClose:
                getActivity().onBackPressed();
                break;

            case R.id.snap_button:
                snapImage();
               /* Bitmap bitmap = showImage.getCroppedImage();
                if (bitmap != null)
                    Toast.makeText(mContext, "Croping Done", Toast.LENGTH_SHORT).show();*/
                break;

            case R.id.ivMultiSelection:
                //getView().findViewById(R.id.ivMultiSelection).setBackground(R.);
                isSupportMultipal = !isSupportMultipal;

                mSelected.clear();
                if(lastSelectedUri!=null) mSelected.put(lastSelectedUri.toString(), lastSelectedUri);

                if(isSupportMultipal){
                    for (Media tmp:albumList) tmp.isSelected = false;
                    ivMultiSelection.setBackground(ContextCompat.getDrawable(context,R.drawable.circle_selected_bg));
                    snap_button.setVisibility(View.GONE);
                    //rotateImage.setVisibility(View.GONE);
                }else {
                    snap_button.setVisibility(View.VISIBLE);
                    // rotateImage.setVisibility(View.VISIBLE);
                    ivMultiSelection.setBackground(ContextCompat.getDrawable(context,R.drawable.selector_rounded_background));
                }

                albumList.get(lastindex).isSelected = true;
                galleryAdapter.setEnableMultipal(isSupportMultipal);

                galleryAdapter.notifyItemChanged(lastindex);
                recyclerView.smoothScrollToPosition(lastindex);
                refreshUi(lastSelectedUri);

                // showImage.setImageUriAsync(media.uri);
                break;

            case R.id.tvNext:

                if(!isSupportMultipal){
                    cropImageAsync();
                }else {

                    Intent intent = null;
                    if (mSelected != null && mSelected.size() > 0) {
                        MediaUri mediaUri = new MediaUri();
                        mediaUri.mediaType = Constant.IMAGE_STATE;
                        mediaUri.isFromGallery = true;
                        mediaUri.addAll(mSelected);
                        intent = new Intent(context, FeedPostActivity.class);
                        intent.putExtra("caption", "");
                        intent.putExtra("feedType", Constant.IMAGE_STATE);
                        intent.putExtra("mediaUri", mediaUri);
                        intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                    } /*else if(videoUri!=null){
                    intent = new Intent(mContext, FeedPostActivity.class);
                    intent.putExtra("caption", "");
                    intent.putExtra("feedType", Constant.VIDEO_STATE);
                    intent.putExtra("videoUri", videoUri.toString());
                    intent.putExtra("fromGallery", false);
                    intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                }*/

                    if (intent != null) {
                        // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(intent, Constant.POST_FEED_DATA);
                    }

                }
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode== Activity.RESULT_OK && requestCode== Constant.POST_FEED_DATA){
            getActivity().finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE){
            galleryAdapter.notifyDataSetChanged();
        }
    }


    private void cropImageAsync() {
        showImage.getCroppedBitmapAsync(new CropperCallback() {
            @Override
            public void onCropped(Bitmap bitmap) {
                if (bitmap != null) {

                    try {
                        File file = getTemporalFile(context);
                        Uri uri = FileProvider.getUriForFile(context,
                                context.getApplicationContext().getPackageName() + ".provider", file);

                        mSelected = new LinkedHashMap<>();
                        mSelected.put(uri.toString(), uri);
                        BitmapUtils.writeBitmapToFile(bitmap, file, 75);
                        //MediaStore.Images.Media.insertImage(context.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());

                        Intent intent = null;
                        if (mSelected != null && mSelected.size() > 0) {
                            MediaUri mediaUri = new MediaUri();
                            mediaUri.mediaType = Constant.IMAGE_STATE;
                            mediaUri.isFromGallery = true;
                            mediaUri.addAll(mSelected);
                            intent = new Intent(context, FeedPostActivity.class);
                            intent.putExtra("caption", "");
                            intent.putExtra("feedType", Constant.IMAGE_STATE);
                            intent.putExtra("mediaUri", mediaUri);
                            intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                            // intent.putParcelableArrayListExtra("imageUri", new ArrayList<Parcelable>(mSelected));
                            //intent.putParcelableArrayListExtra("imageUri", mediaUri);
                        }

                        if (intent != null) {
                            // intent.setDataAndType(uri, mimeType);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(intent, Constant.POST_FEED_DATA);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onOutOfMemoryError() {

            }
        });
    }

    private static File getTemporalFile(Context context) {
        File myFile = new File(context.getExternalCacheDir(), "tempImage.jpg");
        if(myFile.exists())
            myFile.delete();
        return myFile;
    }

    private void snapImage() {
        if (isSnappedToCenter) {
            showImage.cropToCenter();
        } else {
            showImage.fitToCenter();
        }
        isSnappedToCenter = !isSnappedToCenter;
    }

   /* public ArrayList<Media> getAlbums() {
        ArrayList<Media> photos = new ArrayList<>();
        try {

            VideoLoader photoLoader = new VideoLoader(context);
            Cursor photoCursor = photoLoader.loadInBackground();

            if(photoCursor!=null){
                photoCursor.moveToFirst();
                do {
                    Long id = photoCursor.getLong(photoCursor.getColumnIndex(MediaStore.Images.Media._ID));
                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    Media media = new Media();
                    media.uri = uri;
                    photos.add(media);
                }while (photoCursor.moveToNext());

                photoCursor.close();
            }

            return photos;
        } catch (final Exception e) {
            return new ArrayList<>();
        }
    }*/


    public ArrayList<Media> getAlbums() {
        AlbumLoader albumLoader = new AlbumLoader(context);
        ArrayList<Media> photos = new ArrayList<>();
        try {
            Cursor albumCursor = albumLoader.loadInBackground();
            if (albumCursor.moveToFirst()) {

                do {
                    VideoLoader photoLoader = new VideoLoader(albumLoader.getContext(), new String[]{albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))});
                    Cursor photoCursor = photoLoader.loadInBackground();

                    if (photoCursor.moveToFirst()) {
                        do {
                            Long id = photoCursor.getLong(photoCursor.getColumnIndex(MediaStore.Images.Media._ID));
                            Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                            int duration = photoCursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION);
                            Media media = new Media();
                            media.uri= uri;
                            /*Photo photo = new Photo();
                            photo.id = id;
                            photo.uri = uri;
                            photo.isSelected = photos.isEmpty();
                            photos.add(photo);*/
                            photos.add(media);
                        } while (photoCursor.moveToNext() /*&& photos.size() < 40*/);
                    }
                    photoCursor.close();
                    /*Album album = new Album();
                    album.bucketId = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
                    album.name = albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    album.photos = photos;
                    album.isSelected = albums.isEmpty();
                    albums.add(album);*/
                } while (albumCursor.moveToNext() /*&& albums.size() < 10*/);
            }
            albumCursor.close();
            return photos;
        } catch (final Exception e) {
            return new ArrayList<>();
        }
    }
}
