package com.mualab.org.user.activity.gellery.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.image.picker.ImagePicker;
import com.image.picker.ImageRotator;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.FeedPostActivity;
import com.mualab.org.user.activity.feeds.enums.PermissionType;
import com.mualab.org.user.activity.gellery.GalleryActivity;
import com.mualab.org.user.data.model.MediaUri;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.constants.Constant;
import com.mualab.org.user.utils.media.ImageVideoUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.image.picker.ImagePicker.appManifestContainsPermission;
import static com.image.picker.ImagePicker.hasCameraAccess;
import static com.image.picker.ImageRotator.getTemporalFile;


public class CameraFragmentNew extends Fragment implements View.OnClickListener {
    private Context mContext;
    private PermissionType permissionType;

    public CameraFragmentNew() {
        // Required empty public constructor
    }

    public static CameraFragmentNew newInstance() {
        CameraFragmentNew fragment = new CameraFragmentNew();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //  fromConfirmBooking = getArguments().getBoolean("param1");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);


        initView();
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    private void initView() {
       /* if(Build.VERSION.SDK_INT>=23){
            if (mContext.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  ) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            }else dialog.show();
        }*/
        permissionType = PermissionType.IMAGE;

        checkPermissionAndPicImageOrVideo();
    }

    public void checkPermissionAndPicImageOrVideo() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
            }if (permissionType == PermissionType.IMAGE) {
                ImagePicker.pickImageFromCamera(CameraFragmentNew.this);
            } else if (permissionType==PermissionType.VIDEO){
                //mediaUri = null;
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    long maxVideoSize = 20 * 1024 * 1024; // 30 MB
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
                    startActivityForResult(intent, Constant.REQUEST_VIDEO_CAPTURE);
                }
            }
        }else {
            if (permissionType == PermissionType.IMAGE) {
                ImagePicker.pickImageFromCamera(CameraFragmentNew.this);
            } else if (permissionType==PermissionType.VIDEO){
                //mediaUri = null;
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                    long maxVideoSize = 20 * 1024 * 1024; // 30 MB
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                    intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
                    startActivityForResult(intent, Constant.REQUEST_VIDEO_CAPTURE);
                }
            }
        }
    }

    private void pickImageFromCamera() {

        if (!appManifestContainsPermission(mContext, Manifest.permission.CAMERA) || hasCameraAccess(mContext)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("*/*");
            Intent[] intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an action");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().
                            getPackageName() + ".provider",
                    getTemporalFile(mContext));

            chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);


           /* Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("return-data", true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            Uri uri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().
                            getPackageName() + ".provider",
                    getTemporalFile(mContext));

            intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                long maxVideoSize = 20*1024*1024; // 30 MB
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
                startActivityForResult(intent, Constant.REQUEST_VIDEO_CAPTURE);
            }*/
            startActivityForResult(chooserIntent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // String filePath;

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                case Constant.CAMERA_REQUEST:
                    try {
                        Bitmap bitmap = ImagePicker.getImageFromResult(mContext, requestCode, resultCode, data);

                        Uri picUri = ImagePicker.getImageURIFromResult(mContext, requestCode, resultCode, data);

                        if (bitmap != null && picUri != null) {

                            File imageFile = getTemporalFile(mContext);
                            Uri photoURI = Uri.fromFile(imageFile);

                            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(
                                    BitmapFactory.decodeFile(ImageVideoUtil.generatePath(photoURI, mContext)), 150, 150);


                            bitmap = ImagePicker.getImageResized(mContext, photoURI);
                            int rotation = ImageRotator.getRotation(mContext, photoURI, true);
                            bitmap = ImageRotator.rotate(bitmap, rotation);

                            File file = new File(mContext.getExternalCacheDir(), UUID.randomUUID() + ".jpg");
                            picUri = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName()
                                    + ".provider", file);


                            try {
                                OutputStream outStream;
                                outStream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 80, outStream);
                                // ThumbImage.compress(Bitmap.CompressFormat.PNG, 80, outStream);
                                outStream.flush();
                                outStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            MediaUri mediaUri = new MediaUri();
                            mediaUri.isFromGallery = false;
                            mediaUri.mediaType = Constant.IMAGE_STATE;
                            mediaUri.addUri(String.valueOf(picUri));

                            if (mediaUri != null && mediaUri.uriList.size() > 0) {
                                Intent intent = new Intent(mContext, FeedPostActivity.class);
                                intent.putExtra("caption", "");
                                intent.putExtra("mediaUri", mediaUri);
                                intent.putExtra("thumbImage", ThumbImage);
                                intent.putExtra("feedType", Constant.IMAGE_STATE);
                                intent.putExtra("requestCode", Constant.POST_FEED_DATA);

                                startActivityForResult(intent, Constant.POST_FEED_DATA);

                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case Constant.POST_FEED_DATA:
                    ((GalleryActivity) mContext).finish();
                    break;

                case Activity.RESULT_OK:
                    ((GalleryActivity) mContext).finish();
                    break;

            }
        } else  if(requestCode== Constant.POST_FEED_DATA){
            //  ((GalleryActivity)context).setResult(Activity.RESULT_OK);
            getActivity().getFragmentManager().popBackStack();
            ((GalleryActivity)mContext).finish();
            startActivity(new Intent(mContext, GalleryActivity.class));
        }
        else if(resultCode ==RESULT_CANCELED){

            getActivity().getFragmentManager().popBackStack();
            ((GalleryActivity)mContext).finish();
            startActivity(new Intent(mContext, GalleryActivity.class));

            // replaceFragment(GalleryFragment.newInstance(), false, R.id.flGalleryContainer);


        }


    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissionAndPicImageOrVideo();
                } else {
                    MyToast.getInstance(mContext).showSmallMessage("YOUR  PERMISSION DENIED ");
                }
            }
            break;
        }
    }


    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
       /* if(mContext instanceof BookingActivity) {
            ((BookingActivity) mContext).setLyArtistDetailVisibility(0);
            ((BookingActivity) mContext).setTitleVisibility(getString(R.string.title_booking));
        }
        Mualab.getInstance().cancelAllPendingRequests();*/
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }
}