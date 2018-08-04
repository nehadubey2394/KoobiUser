package com.mualab.org.user.activity.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseActivity;
import com.mualab.org.user.activity.explore.ExploreFragment;
import com.mualab.org.user.activity.gellery.Gallery2Activity;
import com.mualab.org.user.activity.gellery.GalleryActivity;
import com.mualab.org.user.activity.my_profile.activity.UserProfileActivity;
import com.mualab.org.user.activity.notification.fragment.NotificationFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.activity.feeds.fragment.FeedsFragment;
import com.mualab.org.user.activity.searchBoard.fragment.SearchBoardFragment;
import com.mualab.org.user.dialogs.MySnackBar;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.data.model.SearchBoard.RefineSearchBoard;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.network.NetworkChangeReceiver;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton ibtnLeaderBoard,ibtnFeed,ibtnAddFeed,ibtnSearch,ibtnNotification;
    private int clickedId = 0;
    public ImageView ivHeaderBack,ivHeaderUser,ivAppIcon,ibtnChat;
    public TextView tvHeaderTitle;
    public RelativeLayout rootLayout;
    public CardView rlHeader1;
    private static final int REQUEST_ADD_NEW_STORY = 8719;
    public RefineSearchBoard item;
    private  long mLastClickTime = 0;

    public void setBgColor(int color){
        if(rlHeader1!=null)
            rlHeader1.setBackgroundColor(getResources().getColor(color));
        if(rootLayout!=null){
            rootLayout.setBackgroundColor(getResources().getColor(color));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //  setStatusbarColor();

        // FirebaseCrash.logcat(Log.ERROR, "Build Date:", "16/04/2018");
        // FirebaseCrash.report(new Throwable("Build Date: 16/04/2018"));

        Mualab.currentUser = Mualab.getInstance().getSessionManager().getUser();
        Mualab.feedBasicInfo.put("userId", ""+ Mualab.currentUser.id);
        Mualab.feedBasicInfo.put("age", "25");
        Mualab.feedBasicInfo.put("gender", "male");
        Mualab.feedBasicInfo.put("city", "indore");
        Mualab.feedBasicInfo.put("state", "MP");
        Mualab.feedBasicInfo.put("country", "India");

        final NoConnectionDialog network =  new NoConnectionDialog(MainActivity.this, new NoConnectionDialog.Listner() {
            @Override
            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                if(isConnected){
                    dialog.dismiss();
                }
            }
        });

        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setListner(new NetworkChangeReceiver.Listner() {
            @Override
            public void onNetworkChange(boolean isConnected) {
                if(isConnected && network!=null){
                    network.dismiss();
                }else network.show();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle!=null){
            item = (RefineSearchBoard) bundle.getSerializable("refineSearchBoard");
        }

        initView();
        addFragment(SearchBoardFragment.newInstance(item,""), false);
    }

    private void initView() {
        ibtnLeaderBoard = findViewById(R.id.ibtnLeaderBoard);
        ibtnFeed = findViewById(R.id.ibtnFeed);
        ibtnAddFeed = findViewById(R.id.ibtnAddFeed);
        ibtnSearch = findViewById(R.id.ibtnSearch);
        ibtnNotification = findViewById(R.id.ibtnNotification);
        ibtnChat = findViewById(R.id.ivChat);

        ivAppIcon = findViewById(R.id.ivAppIcon);
        ivHeaderBack = findViewById(R.id.btnBack);
        ivHeaderUser = findViewById(R.id.ivUserProfile);
        ivHeaderUser.setVisibility(View.VISIBLE);
        User user = Mualab.getInstance().getSessionManager().getUser();

        if (!user.profileImage.isEmpty()){
            Picasso.with(MainActivity.this).load(user.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivHeaderUser);
        }

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rlHeader1 = findViewById(R.id.topLayout1);
        rootLayout = findViewById(R.id.rootLayout);

        ibtnLeaderBoard.setOnClickListener(this);
        ibtnAddFeed.setOnClickListener(this);
        ibtnSearch.setOnClickListener(this);
        ibtnNotification.setOnClickListener(this);
        ibtnFeed.setOnClickListener(this);
        ivHeaderBack.setOnClickListener(this);
        ibtnChat.setOnClickListener(this);
        ivAppIcon.setOnClickListener(this);
        ivHeaderUser.setOnClickListener(this);
        ibtnLeaderBoard.setImageResource(R.drawable.active_leaderboard_ico);
        tvHeaderTitle.setText(getString(R.string.title_searchboard));
        ivHeaderBack.setVisibility(View.GONE);
    }

    private final static int DEFAULT_BITRATE = 1024000;
    private static final int RESULT_START_CAMERA = 4567;
    private static final int RESULT_START_VIDEO = 4589;
    private static final int RESULT_ADD_NEW_STORY = 7891;
    public void openNewStoryActivity(){
        showToast(getString(R.string.under_development));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode==REQUEST_ADD_NEW_STORY){

            /*if(resultCode == 7891){

                Uri uri = data.getData();
                String dataType  = data.getType();
                if(dataType.equals("image/jpeg")){
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),uri);
                        addMyStory(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }*/
        }
    }

    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 700){
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (view.getId()){

            case R.id.ivHeaderBack :
                onBackPressed();
                break;

            case R.id.ibtnChat :
                openNewStoryActivity();
                //MyToast.getInstance(MainActivity.this).showSmallCustomToast("Under developement");
                break;

            case R.id.ivAppIcon :
                break;

            case R.id.ivUserProfile :
                User user = Mualab.getInstance ().getSessionManager().getUser();
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                intent.putExtra("userId",String.valueOf(user.id));
                startActivity(intent);
                break;

            case R.id.ibtnLeaderBoard :
                if (clickedId!=1){
                    setInactiveTab();
                    clickedId = 1;
                    tvHeaderTitle.setText(getString(R.string.title_searchboard));
                    ibtnLeaderBoard.setImageResource(R.drawable.active_leaderboard_ico);
                    ivHeaderBack.setVisibility(View.GONE);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);
                    replaceFragment(SearchBoardFragment.newInstance(item, ""), false);
                }
                break;

            case R.id.ibtnFeed :
                if (clickedId!=2) {
                    setInactiveTab();
                    clickedId = 2;
                    tvHeaderTitle.setText(getString(R.string.app_name));
                    ibtnFeed.setImageResource(R.drawable.active_feeds_ico);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    replaceFragment(FeedsFragment.newInstance(1), false);
                    ibtnChat.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.VISIBLE);
                    ivHeaderBack.setVisibility(View.GONE);
                }
                break;

            case R.id.ibtnAddFeed :
                // if (clickedId!=6) {
                // setInactiveTab();
                //    clickedId = 6;
                //   ibtnAddFeed.setImageResource(R.drawable.active_add_ico);
                   /* tvHeaderTitle.setText(R.string.title_explore);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);*/
                startActivity(new Intent(MainActivity.this, Gallery2Activity.class));

                //    }

                break;

            case R.id.ibtnSearch :
                if (clickedId!=4) {
                    setInactiveTab();
                    clickedId = 4;
                    tvHeaderTitle.setText(R.string.title_explore);
                    ibtnSearch.setImageResource(R.drawable.active_search_ico);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);
                    replaceFragment(ExploreFragment.newInstance(), false);
                }
                break;

            case R.id.ibtnNotification :
                if (clickedId!=5) {
                    setInactiveTab();
                    clickedId = 5;
                    tvHeaderTitle.setText(getString(R.string.title_searchboard));
                    ibtnNotification.setImageResource(R.drawable.active_notifications_ico);
                    tvHeaderTitle.setText(R.string.title_notification);
                    ivHeaderUser.setVisibility(View.VISIBLE);
                    tvHeaderTitle.setVisibility(View.VISIBLE);
                    ibtnChat.setVisibility(View.GONE);
                    ivAppIcon.setVisibility(View.GONE);
                    replaceFragment(new NotificationFragment(), false);
                }
                break;
        }
    }

    private void setInactiveTab(){
        rlHeader1.setVisibility(View.VISIBLE);
        ibtnLeaderBoard.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_leaderboard_ico));
        ibtnFeed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_feeds_ico));
        ibtnAddFeed.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_add_ico));
        ibtnSearch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_search_ico));
        ibtnNotification.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.inactive_notifications_ico));

    }

    private boolean doubleBackToExitPressedOnce;
    private Runnable runnable;
    @Override
    public void onBackPressed() {
        /* Handle double click to finish activity*/
        Handler handler = new Handler();
        FragmentManager fm = getSupportFragmentManager();
        int i = fm.getBackStackEntryCount();
        if (i > 0) {
            fm.popBackStack();
        } else if (!doubleBackToExitPressedOnce) {

            doubleBackToExitPressedOnce = true;
            //Toast.makeText(this, "Click again to exit", Toast.LENGTH_SHORT).show();
            MySnackBar.showSnackbar(this, findViewById(R.id.lyCoordinatorLayout), "Click again to exit");
            handler.postDelayed(runnable = new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);

        } else {
            handler.removeCallbacks(runnable);
            super.onBackPressed();
        }
    }

    private void addMyStory( Bitmap bitmap){

        if(ConnectionDetector.isConnected()){
            Map<String,String> map = new HashMap<>();
            map.put("type", "image");

            HttpTask task = new HttpTask(new HttpTask.Builder(this, "addMyStory", new HttpResponceListner.Listener() {
                @Override
                public void onResponse(String response, String apiName) {
                    try {
                        JSONObject js = new JSONObject(response);
                        String status = js.getString("status");
                        String message = js.getString("message");
                        if (status.equalsIgnoreCase("success")) {
                            showToast(message);
                            finish();
                        }
                        else showToast(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void ErrorListener(VolleyError error) {
                    Log.d("res:", ""+error.getLocalizedMessage());
                }})
                    .setParam(map)
                    .setAuthToken(Mualab.getInstance().getSessionManager().getUser().authToken)
                    .setProgress(true));
            task.postImage("myStory", bitmap);
        }else showToast(getString(R.string.error_msg_network));
    }

    private void showToast(String str){
        if(!TextUtils.isEmpty(str))
            MyToast.getInstance(this).showSmallCustomToast(str);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Mualab.getInstance().getSessionManager().setIsOutCallFilter(false);
    }
}
