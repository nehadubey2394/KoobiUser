package com.mualab.org.user.activity.story.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.feeds.LiveUserInfo;
import com.mualab.org.user.data.model.feeds.Story;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import views.story.StoriesProgressView;


public class StoryFragment extends Fragment implements StoriesProgressView.StoriesListener {

    @IntDef({NODIR, UP, DOWN, LEFT, RIGHT})
    public @interface AnimationDirection {}
    public static final int NODIR = 0;
    public static final int UP    = 1;
    public static final int DOWN  = 2;
    public static final int LEFT  = 3;
    public static final int RIGHT = 4;

    private LiveUserInfo userInfo;
   // private StoryStatusView storyStatusView;
    private StoriesProgressView storyStatusView;

    private Context mContext;
    private ImageView ivPhoto, ivUserImg;
    private ProgressBar progress_bar;
    private Target target;


    private TextView tvUserName;
    private VideoView videoView;
    private MediaController vidControl;

    private List<Story> storyList = new ArrayList<>();
    private long statusDuration = 3000L;
    private long DURATION = 500L;
    private int counter = 0;

    private StoryListiner mListener;
    private boolean isRunningStory, isViewCreated;

    public StoryFragment() {
        // Required empty public constructor
    }

    public interface StoryListiner{
        void onNext();
        void onPrev();
        void onFinish();
    }


    public static StoryFragment newInstance(LiveUserInfo userInfo, int position) {
        StoryFragment fragment = new StoryFragment();
        Bundle args = new Bundle();
        args.putSerializable("userInfo", userInfo);
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userInfo = (LiveUserInfo) getArguments().getSerializable("userInfo");
        }
    }


    @Override
    public void onNext() {
        storyStatusView.pause();
        ++counter;
        //get image url
        String imageUrl = storyList.get(counter).myStory;

        //ImageViewTarget is the implementation of Target interface.
        //code for this ImageViewTarget is in the end
        //target = new ImageViewTarget(ivPhoto, progress_bar);
        progress_bar.setVisibility(View.VISIBLE);
        Picasso.with(ivPhoto.getContext())
                .load(imageUrl)
                .error(R.drawable.bg_splash)
                .into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        storyStatusView.resume();
                        ivPhoto.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        storyStatusView.pause();
                        progress_bar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onPrev() {
        if (counter - 1 < 0) return;
        storyStatusView.pause();
        --counter;

        //get image url
        String imageUrl = storyList.get(counter).myStory;
        //ImageViewTarget is the implementation of Target interface.
        //code for this ImageViewTarget is in the end
        //target = new ImageViewTarget(ivPhoto, progress_bar);
        progress_bar.setVisibility(View.VISIBLE);
        Picasso.with(ivPhoto.getContext())
                .load(imageUrl)
                .error(R.drawable.bg_splash)
                .into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        storyStatusView.resume();
                        ivPhoto.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        storyStatusView.pause();
                        progress_bar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onComplete() {
        if(mListener!=null)
            mListener.onNext();
    }


    private void startStories(){
        isRunningStory = true;
        storyStatusView.setStoriesCount(storyList.size());
        storyStatusView.setStoryDuration(statusDuration);
        // or
        // statusView.setStoriesCountWithDurations(statusResourcesDuration);
        //storyStatusView.setUserInteractionListener(this);
        // storyStatusView.playStories();
        //target = new ImageViewTarget(ivPhoto, progress_bar);
        storyStatusView.setStoriesListener(this);
       // storyStatusView.startStories();
        //storyStatusView.pause();
        progress_bar.setVisibility(View.VISIBLE);
        String imageUrl = storyList.get(counter).myStory;
        Picasso.with(ivPhoto.getContext())
                .load(imageUrl)
                .error(R.drawable.bg_splash)
                .into(ivPhoto, new Callback() {
                    @Override
                    public void onSuccess() {
                        storyStatusView.startStories();
                        //storyStatusView.resume();
                        ivPhoto.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        storyStatusView.pause();
                        progress_bar.setVisibility(View.GONE);
                    }
                });
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_story, container, false);
        ivPhoto = view.findViewById(R.id.ivPhoto);
        progress_bar = view.findViewById(R.id.imageProgressBar);
        storyStatusView = view.findViewById(R.id.stories);
        videoView = view.findViewById(R.id.videoView);
        ivUserImg = view.findViewById(R.id.iv_user_image);
        tvUserName =  view.findViewById(R.id.tv_user_name);

        vidControl = new MediaController(getContext());
        vidControl.setAnchorView(videoView);
        videoView.setMediaController(vidControl);
        storyStatusView = view.findViewById(R.id.storiesStatus);

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyStatusView.skip();
            }
        });


        // bind reverse view
        view.findViewById(R.id.reverse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyStatusView.reverse();
            }
        });

        // bind skip view
        view.findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storyStatusView.skip();
            }
        });


        view.findViewById(R.id.actions).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {

                    if(isRunningStory)
                        storyStatusView.pause();
                } else {
                    storyStatusView.resume();
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        final int position = getArguments().getInt("position");
        view.setTag(position);

        updateView();
        getStories();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser && isViewCreated){
            startStories();
        }
    }

    private void updateView(){
        tvUserName.setText(String.format("%s %s", userInfo.firstName, userInfo.lastName));
        if(TextUtils.isEmpty(userInfo.profileImage)){
            Picasso.with(mContext).load(R.drawable.defoult_user_img).fit().into(ivUserImg);
        }else Picasso.with(mContext).load(userInfo.profileImage).fit().into(ivUserImg);
    }




    @Override
    public void onDestroy() {
        storyStatusView.destroy();
        super.onDestroy();
    }

   /* @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        switch (getArguments().getInt("direction")){
            case LEFT:
                return CubeAnimation.create(CubeAnimation.LEFT, enter, DURATION);
            case RIGHT:
                return CubeAnimation.create(CubeAnimation.RIGHT, enter, DURATION);

                default: return super.onCreateAnimation(transit, enter, nextAnim);
        }
    }*/


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;

        if (context instanceof StoryListiner) {
            mListener = (StoryListiner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void getStories() {
        Map<String, String> map = new HashMap<>();
        // map.put("userId", Mualab.getInstance().getSessionManager().getUser().id);
        map.put("userId", ""+userInfo.id);

        new HttpTask(new HttpTask.Builder(mContext, "myStory", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");
                    storyList.clear();

                    if (status.equalsIgnoreCase("success") && !message.equalsIgnoreCase("No results found right now")) {
                        JSONArray array = js.getJSONArray("allMyStory");
                        counter=0;
                        Gson gson = new Gson();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            Story story = gson.fromJson(String.valueOf(jsonObject), Story.class);
                            storyList.add(story);
                        }

                       if(getUserVisibleHint() && !isRunningStory){
                           startStories();
                       }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }})
                .setAuthToken(Mualab.getInstance().getSessionManager().getUser().authToken)
                .setBody(map , HttpTask.ContentType.APPLICATION_JSON))
                .execute("StoryAPI");
    }



    private class ImageViewTarget implements Target {

        private WeakReference<ImageView> mImageViewReference;
        private WeakReference<ProgressBar> mProgressBarReference;

        public ImageViewTarget(ImageView imageView, ProgressBar progressBar) {
            this.mImageViewReference = new WeakReference<>(imageView);
            this.mProgressBarReference = new WeakReference<>(progressBar);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            storyStatusView.resume();
            //you can use this bitmap to load image in image view or save it in image file like the one in the above question.
            ImageView imageView = mImageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }

            ProgressBar progressBar = mProgressBarReference.get();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            storyStatusView.pause();
            ImageView imageView = mImageViewReference.get();
            if (imageView != null) {
                imageView.setImageDrawable(errorDrawable);
            }

            ProgressBar progressBar = mProgressBarReference.get();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            storyStatusView.pause();
            ImageView imageView = mImageViewReference.get();
            if (imageView != null) {
                imageView.setImageDrawable(placeHolderDrawable);
            }

            ProgressBar progressBar = mProgressBarReference.get();
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    }
}
