package com.mualab.org.user.activity.story.core;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.story.camera.internal.CameraIntentKey;
import com.squareup.picasso.Picasso;

import java.io.File;


public class PhotoPreviewFragment extends Fragment implements View.OnClickListener{

  private static final String TAG = "StillshotPreviewFragmen";

  private ImageView mImageView;
  private String mOutputUri;

  /**
   * Reference to the bitmap, in case 'onConfigurationChange' event comes, so we do not recreate the
   * bitmap
   */
  private static Bitmap mBitmap;
  private BaseStoryInterface mIStoryInterface;

  public static PhotoPreviewFragment newInstance(
          String outputUri, boolean allowRetry, int primaryColor) {
    final PhotoPreviewFragment fragment = new PhotoPreviewFragment();
    fragment.setRetainInstance(true);
    Bundle args = new Bundle();
    args.putString("output_uri", outputUri);
    args.putBoolean(CameraIntentKey.ALLOW_RETRY, allowRetry);
    args.putInt(CameraIntentKey.PRIMARY_COLOR, primaryColor);
    fragment.setArguments(args);
    return fragment;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mIStoryInterface = (BaseStoryInterface) activity;
  }


  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.mcam_fragment_stillshot, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mOutputUri = getArguments().getString("output_uri");
    mImageView = view.findViewById(R.id.stillshot_imageview);
    RelativeLayout mRetry = view.findViewById(R.id.retry);
    RelativeLayout mSaveStory = view.findViewById(R.id.save_story);
    RelativeLayout mAddToStory = view.findViewById(R.id.add_to_story);

    mRetry.setVisibility(
            getArguments().getBoolean(CameraIntentKey.ALLOW_RETRY, true) ? View.VISIBLE : View.GONE);

    mRetry.setOnClickListener(this);
    mSaveStory.setOnClickListener(this);
    mAddToStory.setOnClickListener(this);

    mImageView
        .getViewTreeObserver()
        .addOnPreDrawListener(
            new ViewTreeObserver.OnPreDrawListener() {
              @Override
              public boolean onPreDraw() {
                setImageBitmap();
                mImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
              }
            });
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (mBitmap != null && !mBitmap.isRecycled()) {
      try {
        mBitmap.recycle();
        mBitmap = null;
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  /** Sets bitmap to ImageView widget */
  private void setImageBitmap() {
    Picasso.with(getActivity())
            .load(mOutputUri)
            .into(mImageView);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.retry) mIStoryInterface.onRetry(mOutputUri);
    else if (v.getId() == R.id.save_story) mIStoryInterface.useMedia(mOutputUri);
    else if(v.getId() == R.id.add_to_story) mIStoryInterface.addToStory(mOutputUri);
  }


  @Override
  public void onDestroy() {
    Log.d(TAG, "onDestroy: called.");
    super.onDestroy();
    if(mOutputUri != null){
      Log.d(TAG, "onDestroy: cleaning up files.");
      deleteOutputFile(mOutputUri);
      mOutputUri = null;
    }
  }


  private void deleteOutputFile(@Nullable String uri) {
    if (uri != null)
      //noinspection ResultOfMethodCallIgnored
      new File(Uri.parse(uri).getPath()).delete();
  }
}
















