package com.mualab.org.user.activity.gellery.fragment;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.image.nocropper.BitmapUtils;
import com.image.nocropper.CropperCallback;
import com.image.nocropper.CropperView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.FeedPostActivity;
import com.mualab.org.user.activity.gellery.GalleryActivity;
import com.mualab.org.user.activity.gellery.adapter.GalleryAdapter;
import com.mualab.org.user.activity.gellery.model.Media;
import com.mualab.org.user.activity.gellery.model.PhotoLoader;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.dialogs.MySnackBar;
import com.mualab.org.user.listner.GalleryOnClickListener;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.utils.media.ImageVideoUtil;
import com.zhihu.matisse.internal.loader.AlbumLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class GalleryFragment extends Fragment implements View.OnClickListener {

    int numberOfColumns = 4;
    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;
    private CollapsingToolbarLayout collapsing_toolbar;
    private Bitmap thumbImage = null;
    private Context context;
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
    private ProgressBar ll_progress;

    public GalleryFragment() {
        // Required empty public constructor
    }

    public static GalleryFragment newInstance() {
        GalleryFragment fragment = new GalleryFragment();
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
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                albumList = getAlbums();
                ll_progress.setVisibility(View.VISIBLE);
                setImageList();
            }
        } else {
            albumList = getAlbums();
            ll_progress.setVisibility(View.VISIBLE);
            setImageList();
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
        // setImageList();
    }

    private void setImageList() {
        if (albumList != null && albumList.size() > 0) {
            Media media = albumList.get(0);
            lastSelectedUri = media.uri;
            mSelected.put(media.uri.toString(), media.uri);
            refreshUi(lastSelectedUri);
            //showImage.setUri(media.uri);
            try {

                showImage.setImageUri(media.uri);

                //showImage.setImageBitmap(ImageVideoUtil.getBitmapFromUri(context,media.uri));


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        galleryAdapter = new GalleryAdapter(albumList, context, new GalleryOnClickListener() {
            @Override
            public void OnClick(Media media, int index) {

                if (isSupportMultipal) {

                    int size = mSelected.size();
                    if (size < 10) {

                        if (!media.isSelected) {
                            media.isSelected = !media.isSelected;
                            mSelected.put(media.uri.toString(), media.uri);
                            lastSelectedUri = media.uri;
                            lastindex = index;

                        } else {

                            if (mSelected.size() > 1) {
                                media.isSelected = !media.isSelected;
                                mSelected.remove(media.uri.toString());
                                Map.Entry<String, Uri> last = null;
                                for (Map.Entry<String, Uri> e : mSelected.entrySet()) last = e;
                                lastSelectedUri = last.getValue();
                            }
                        }
                    } else if (media.isSelected && size > 1) {
                        media.isSelected = !media.isSelected;
                        mSelected.remove(media.uri.toString());
                        Map.Entry<String, Uri> last = null;
                        for (Map.Entry<String, Uri> e : mSelected.entrySet()) last = e;
                        if (last != null) {
                            lastSelectedUri = last.getValue();
                        }
                    } else if (size > 9) {
                        MySnackBar.showSnackbar(context, rootLayout, "You can select max 10 items");
                    }

                } else {
                    lastindex = index;
                    lastSelectedUri = media.uri;
                    mSelected.clear();
                    mSelected.put(media.uri.toString(), media.uri);
                    ll_progress.setVisibility(View.VISIBLE);
                }
                ll_progress.setVisibility(View.VISIBLE);
                refreshUi(lastSelectedUri);

                galleryAdapter.notifyItemChanged(index);

                if (mSelected.size() <= 2)
                    // recyclerView.smoothScrollToPosition(index);
                    expandToolbar();
            }
        });
        recyclerView.setAdapter(galleryAdapter);
    }

    private void initView(View view) {
        showImage = view.findViewById(R.id.showImage);
        ll_progress = view.findViewById(R.id.progrss);
        snap_button = view.findViewById(R.id.snap_button);
        ivImage = view.findViewById(R.id.ivImage);
        // rotateImage = view.findViewById(R.id.rotateImage);
        ivMultiSelection = view.findViewById(R.id.ivMultiSelection);
        view.findViewById(R.id.tvNext).setOnClickListener(this);
        view.findViewById(R.id.tvClose).setOnClickListener(this);
        appbar = view.findViewById(R.id.appbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        collapsing_toolbar = view.findViewById(R.id.collapsing_toolbar);
        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new GridLayoutManager(context, numberOfColumns));
        rootLayout = view.findViewById(R.id.rootLayout);
        snap_button.setOnClickListener(this);
        // rotateImage.setOnClickListener(this);
        ivMultiSelection.setOnClickListener(this);
    }

    private void refreshUi(Uri uri) {

        if (isSupportMultipal) {
            ivImage.setVisibility(View.VISIBLE);
            showImage.setVisibility(View.GONE);
            // ivImage.setImageURI(uri);

            Glide.with(context).load(uri)
                    .placeholder(0).fallback(0).centerCrop()
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivImage);

        } else {
            showImage.setVisibility(View.VISIBLE);
            ivImage.setVisibility(View.GONE);
            showImage.setImageUri(uri);
        }
    }

    public void expandToolbar() {
        appbar.setExpanded(true, true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tvClose:
                ((GalleryActivity) context).onBackPressed();
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
                if (lastSelectedUri != null)
                    mSelected.put(lastSelectedUri.toString(), lastSelectedUri);

                if (isSupportMultipal) {
                    for (Media tmp : albumList) tmp.isSelected = false;
                    ivMultiSelection.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_selected_bg));
                    snap_button.setVisibility(View.GONE);
                    //rotateImage.setVisibility(View.GONE);
                } else {
                    snap_button.setVisibility(View.VISIBLE);
                    // rotateImage.setVisibility(View.VISIBLE);
                    ivMultiSelection.setBackground(ContextCompat.getDrawable(context, R.drawable.selector_rounded_background));
                }

                albumList.get(lastindex).isSelected = true;
                galleryAdapter.setEnableMultipal(isSupportMultipal);

                galleryAdapter.notifyItemChanged(lastindex);
                // recyclerView.smoothScrollToPosition(lastindex);

                refreshUi(lastSelectedUri);

                // showImage.setImageUriAsync(media.uri);
                break;

            case R.id.tvNext:
                if (!isSupportMultipal) {
                    // cropImageAsync();


                    Intent intent = new Intent(context, FeedPostActivity.class);
                    if (mSelected != null && mSelected.size() > 0) {
                        MediaUri mediaUri = new MediaUri();
                        mediaUri.mediaType = Constant.IMAGE_STATE;
                        mediaUri.isFromGallery = true;
                        mediaUri.addAll(mSelected);

                        thumbImage = ThumbnailUtils
                                .extractThumbnail(BitmapFactory.decodeFile(
                                        ImageVideoUtil.generatePath(Uri.parse(mediaUri.uriList.get(0)), context)), 100, 100);


                        intent.putExtra("caption", "");
                        intent.putExtra("mediaUri", mediaUri);
                        intent.putExtra("thumbImage", thumbImage);
                        intent.putExtra("feedType", Constant.IMAGE_STATE);
                        intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                    } else {
                        intent = new Intent(context, FeedPostActivity.class);
                        intent.putExtra("caption", "");
                        intent.putExtra("feedType", Constant.TEXT_STATE);
                        intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, Constant.POST_FEED_DATA);
                    // else startActivityForResult(intent, Constant.POST_FEED_DATA);

                } else {
                    Intent intent = new Intent(context, FeedPostActivity.class);
                    if (mSelected != null && mSelected.size() > 0) {
                        MediaUri mediaUri = new MediaUri();
                        mediaUri.mediaType = Constant.IMAGE_STATE;
                        mediaUri.isFromGallery = true;
                        mediaUri.addAll(mSelected);

                        thumbImage = ThumbnailUtils
                                .extractThumbnail(BitmapFactory.decodeFile(
                                        ImageVideoUtil.generatePath(Uri.parse(mediaUri.uriList.get(0)), context)), 100, 100);


                        intent.putExtra("caption", "");
                        intent.putExtra("mediaUri", mediaUri);
                        intent.putExtra("thumbImage", thumbImage);
                        intent.putExtra("feedType", Constant.IMAGE_STATE);
                        intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                    } else {
                        intent = new Intent(context, FeedPostActivity.class);
                        intent.putExtra("caption", "");
                        intent.putExtra("feedType", Constant.TEXT_STATE);
                        intent.putExtra("requestCode", Constant.POST_FEED_DATA);
                    }

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(intent, Constant.POST_FEED_DATA);
                    // else startActivityForResult(intent, Constant.POST_FEED_DATA);

                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == Constant.POST_FEED_DATA) {
            ((GalleryActivity) context).finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            albumList = getAlbums();
            setImageList();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void cropImageAsync() {
        showImage.getCroppedBitmapAsync(new CropperCallback() {
            @Override
            public void onCropped(Bitmap bitmap) {
                if (bitmap != null) {

                    try {

                        File file = new File(context.getExternalCacheDir(), UUID.randomUUID()
                                + ".jpg");
                        Uri uri = FileProvider.getUriForFile(context, context.
                                getApplicationContext().getPackageName()
                                + ".provider", file);

                        mSelected = new LinkedHashMap<>();
                        mSelected.put(uri.toString(), uri);
                        BitmapUtils.writeBitmapToFile(bitmap, file, 75);
                        //MediaStore.Images.Media.insertImage(context.getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
                        thumbImage = ThumbnailUtils.extractThumbnail(bitmap, 150, 150);

                        Intent intent = null;
                        if (mSelected != null && mSelected.size() > 0) {
                            MediaUri mediaUri = new MediaUri();
                            mediaUri.mediaType = Constant.IMAGE_STATE;
                            mediaUri.isFromGallery = true;
                            mediaUri.addAll(mSelected);

                            intent = new Intent(context, FeedPostActivity.class);
                            intent.putExtra("caption", "");
                            intent.putExtra("mediaUri", mediaUri);
                            intent.putExtra("thumbImage", thumbImage);
                            intent.putExtra("feedType", Constant.IMAGE_STATE);
                            intent.putExtra("requestCode", Constant.POST_FEED_DATA);

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

    private void snapImage() {
        if (isSnappedToCenter) {
            showImage.cropToCenter();
        } else {
            showImage.fitToCenter();
        }
        isSnappedToCenter = !isSnappedToCenter;
    }

    public ArrayList<Media> getAlbums() {
        final AlbumLoader albumLoader = new AlbumLoader(context);
        final ArrayList<Media> photos = new ArrayList<>();
        try {
            final Cursor albumCursor = albumLoader.loadInBackground();
            if (albumCursor != null && albumCursor.moveToFirst()) {
                do {
                    PhotoLoader photoLoader = new PhotoLoader(albumLoader.getContext(), new String[]{albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))});
                    Cursor photoCursor = photoLoader.loadInBackground();
                    if (photoCursor != null && photoCursor.moveToFirst()) {
                        do {
                            Long id = photoCursor.getLong(photoCursor.getColumnIndex(MediaStore.Images.Media._ID));
                            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                            Media media = new Media();
                            media.uri = uri;
                            photos.add(media);

                        } while (photoCursor.moveToNext() /*&& photos.size() < 40*/);
                    }
                    photoCursor.close();

                } while (albumCursor.moveToNext() /*&& albums.size() < 10*/);
        /*        new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        do {
                            PhotoLoader photoLoader = new PhotoLoader(albumLoader.getContext(), new String[]{albumCursor.getString(albumCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))});
                            Cursor photoCursor = photoLoader.loadInBackground();
                            if (photoCursor != null && photoCursor.moveToFirst()) {
                                do {
                                    Long id = photoCursor.getLong(photoCursor.getColumnIndex(MediaStore.Images.Media._ID));
                                    Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                    Media media = new Media();
                                    media.uri = uri;

                                    Bitmap bitmap= null;
                                    try {
                                        bitmap = (ImageVideoUtil.getBitmapFromUri(context,media.uri));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if(bitmap.getHeight()>=2048||bitmap.getWidth()>=2048){
                                        photos.add(media);
                                    }
                                    //photos.add(media);

                                } while (photoCursor.moveToNext() *//*&& photos.size() < 40*//*);
                            }
                            photoCursor.close();

                        } while (albumCursor.moveToNext() *//*&& albums.size() < 10*//*);
                    }
                },5000);
*/
            }
            albumCursor.close();
            return photos;
        } catch (final Exception e) {
            return new ArrayList<>();
        }
    }
}