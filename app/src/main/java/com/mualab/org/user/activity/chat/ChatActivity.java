package com.mualab.org.user.activity.chat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.image.cropper.CropImage;
import com.image.cropper.CropImageView;
import com.image.picker.ImagePicker;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.Util.TimeAgo;
import com.mualab.org.user.activity.chat.adapter.ChattingAdapter;
import com.mualab.org.user.activity.chat.adapter.MenuAdapter;
import com.mualab.org.user.activity.chat.listner.DateTimeScrollListner;
import com.mualab.org.user.activity.chat.model.BlockUser;
import com.mualab.org.user.activity.chat.model.Chat;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.activity.chat.model.MuteUser;
import com.mualab.org.user.activity.chat.model.Typing;
import com.mualab.org.user.activity.chat.model.WebNotification;
import com.mualab.org.user.activity.chat.notification_builder.FcmNotificationBuilder;
import com.mualab.org.user.activity.story.camera.util.ImageUtil;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.CommonUtils;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.SoftKeyboard;
import com.mualab.org.user.utils.constants.Constant;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,
        SoftKeyboard.SoftKeyboardChanged ,DateTimeScrollListner {
    private EditText et_for_sendTxt;
    private TextView tv_no_chat, tvOnlineStatus,tv_chat_date,tvUserName;
    private ProgressBar progress_bar;
    private RecyclerView recycler_view;
    private ChattingAdapter chattingAdapter;
    private List<Chat> chatList;
    private Map<String, Chat> map;
    private boolean isFromGallery;
    private String myUid,otherUserId,blockUserNode,blockedById="",onlineStatus="";
    private FirebaseUser otherUser;
    private LinearLayout llDots;
    private PopupWindow popupWindow;
    private ArrayList<String>arrayList;
    private SoftKeyboard softKeyboard;
    private Boolean isTyping = false,isOtherTypping = false,isActivityOpen = false;
    private Handler handler = new Handler();
    private Bitmap bmChatImg;
    private DatabaseReference mFirebaseDatabaseReference,chatRef,otherChatRef,myChatRef,isOppTypingRef,
            blockUsersRef,myChatHistoryRef,otherChatHistoryRef,chatRefWebnotif,chatRefMuteUser;
    private long mLastClickTime = 0;
    private LinearLayoutManager layoutManager;
    private int unreadMsgCount = 1,isMyFavourite = 0,isOtherFavourite = 0,isMute=0,
            isOtherMute=0;
    private ValueEventListener otherUserDetail;
    private ChildEventListener childEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("opponentChatId");
        myUid = String.valueOf(Mualab.currentUser.id);

        Mualab.currentChatUserId = otherUserId;

        isActivityOpen = true;

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        chatRef = mFirebaseDatabaseReference.child("chat");
        blockUsersRef = mFirebaseDatabaseReference.child("block_users");

        String myChild = myUid+"_"+otherUserId;

        chatRefWebnotif = mFirebaseDatabaseReference.child("webnotification");
        isOppTypingRef = mFirebaseDatabaseReference.child(Constant.IS_TYPING).child(myChild);

        otherChatHistoryRef = mFirebaseDatabaseReference.child("chat_history").
                child(otherUserId).child(myUid);

        myChatHistoryRef = mFirebaseDatabaseReference.child("chat_history").
                child(myUid).child(otherUserId);

        chatRefMuteUser = mFirebaseDatabaseReference.child("mute_user");

        init();

    }

    private void initKeyboard() {

        RelativeLayout mainLayout = findViewById(R.id.rlMain); // You must use your parent layout
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        softKeyboard = new SoftKeyboard(mainLayout, im);
        softKeyboard.setSoftKeyboardCallback(this);

        final String myChild = otherUserId+"_"+myUid;
        //Log.e("node",myChild);

        mFirebaseDatabaseReference.child(Constant.IS_TYPING).child(myChild).
                addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (!dataSnapshot.getKey().contains("broadcast_") ||
                                !dataSnapshot.getKey().contains("broadcast_group")) {

                            System.out.println("typping............");

                            if (dataSnapshot.exists()){
                                isOtherTypping = true;
                                // = "typing...";
                                tvOnlineStatus.setText("typing...");
                                tvOnlineStatus.setTextColor(getResources().getColor(R.color.chatbox_blue));
                            }else {
                                isOtherTypping = false;
                                tvOnlineStatus.setTextColor(getResources().getColor(R.color.grey));
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()){
                            System.out.println("typping............");

                            isOtherTypping = true;
                            //   onlineStatus = "typing...";
                            tvOnlineStatus.setText("typing...");
                            tvOnlineStatus.setTextColor(getResources().getColor(R.color.chatbox_blue));
                        }else {
                            isOtherTypping = false;
                            tvOnlineStatus.setTextColor(getResources().getColor(R.color.grey));
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            isOtherTypping = false;
                            tvOnlineStatus.setText("Online");
                            tvOnlineStatus.setTextColor(getResources().getColor(R.color.gray));
                        }
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()){
                            System.out.println("typping............");
                            isOtherTypping = true;
                            //   onlineStatus = "typing...";
                            tvOnlineStatus.setText("typing...");
                            tvOnlineStatus.setTextColor(getResources().getColor(R.color.chatbox_blue));
                        }else {
                            isOtherTypping = false;
                            tvOnlineStatus.setTextColor(getResources().getColor(R.color.grey));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void init(){
        // otherUserId = "17";
        chatList = new ArrayList<>();
        map = new HashMap<>();
        arrayList=new ArrayList<>();
        chattingAdapter = new ChattingAdapter(ChatActivity.this,chatList,myUid,this);

        et_for_sendTxt = findViewById(R.id.et_for_sendTxt);
        tv_no_chat = findViewById(R.id.tv_no_chat);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);
        progress_bar = findViewById(R.id.progress_bar);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView iv_capture_image = findViewById(R.id.iv_capture_image);

        TextView tv_for_send = findViewById(R.id.tv_for_send);
        tvUserName = findViewById(R.id.tvUserName);
        recycler_view = findViewById(R.id.recycler_view);
        AppCompatImageView iv_pickImage = findViewById(R.id.iv_pickImage);
        tv_chat_date = findViewById(R.id.tv_chat_date);

        llDots = findViewById(R.id.llDots);
        //  TextView tvClearChat = findViewById(R.id.tvClearChat);

        layoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false);
        // layoutManager.scrollToPositionWithOffset(0, 0);
        //  layoutManager.setStackFromEnd(true);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(chattingAdapter);

        myChatRef = chatRef.child(myUid).child(otherUserId);
        otherChatRef = chatRef.child(otherUserId).child(myUid);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ChatActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                    }
                }
            }).show();
        }

        //getNodeInfo();

        getMessageList();

        Thread thread4 = new Thread(new Runnable() {
            @Override
            public void run() {
                getOpponentChatInfo();
            }
        });
        thread4.start();

        if (Integer.parseInt(myUid)>Integer.parseInt(otherUserId)){
            blockUserNode = otherUserId+"_"+myUid;
        }else {
            blockUserNode = myUid+"_"+otherUserId;
        }

        //getting user data from user table
        Thread  thread1 = new Thread(new Runnable(){
            @Override
            public void run(){
                getOtherUserDetail();
                //code to do the HTTP request
            }
        });
        thread1.start();

        Thread thread2 = new Thread(new Runnable(){
            @Override
            public void run(){
                myChatHistoryRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String key = dataSnapshot.getKey();
                            if (!key.contains("group_")) {
                                ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                                assert messageOutput != null;
                                myChatHistoryRef.child("unreadMessage").setValue(0);
                                isMyFavourite = messageOutput.favourite;

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //code to do the HTTP request
            }
        });
        thread2.start();

        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                getBlockUser();
            }
        });
        thread3.start();

        getMutedUser();

        otherChatHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    String key = dataSnapshot.getKey();
                    if (!key.contains("group_")) {
                        ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                        assert messageOutput != null;
                        unreadMsgCount = messageOutput.unreadMessage+1;
                        isOtherFavourite = messageOutput.favourite;
                        //chatHistory2.unreadMessage = messageOutput.unreadMessage+1;
                        //  otherChatHistoryRef.child("unreadMessage").setValue(count);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        initKeyboard();

        iv_pickImage.setOnClickListener(this);
        iv_capture_image.setOnClickListener(this);
        tv_for_send.setOnClickListener(this);
        llDots.setOnClickListener(this);
        //tvClearChat.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void getOtherUserDetail(){
        progress_bar.setVisibility(View.VISIBLE);

        otherUserDetail = mFirebaseDatabaseReference.child("users").child(otherUserId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null) {
                            try {
                                otherUser = dataSnapshot.getValue(FirebaseUser.class);
                                if (otherUser != null) {

                                    System.out.println("typping other user data ====");

                                    CircleImageView ivUserProfile = findViewById(R.id.ivUserProfile);
                                    tvUserName.setText(otherUser.userName);

                                    if (otherUser.profilePic !=null && !otherUser.profilePic.isEmpty()) {
                                        Picasso.with(ChatActivity.this).load(otherUser.profilePic).
                                                placeholder(R.drawable.defoult_user_img).
                                                fit().into(ivUserProfile);
                                    }
                                    // SimpleDateFormat sd = new SimpleDateFormat("hh:mm a");
                                    // onlineStatus = sd.format(new Date((Long) otherUser.lastActivity));
                                    if (otherUser.isOnline==1 && !isOtherTypping) {
                                        //   onlineStatus = "Online";
                                        tvOnlineStatus.setText("Online");
                                    }

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            tvOnlineStatus.setTextColor(getResources().getColor(R.color.grey));
                                            onlineStatus = TimeAgo.timeAgo((Long) otherUser.lastActivity);
                                            if (otherUser.isOnline==1 && !isOtherTypping) {
                                                //  onlineStatus = "Online";
                                                tvOnlineStatus.setText("Online");
                                            }else if (otherUser.isOnline==1){
                                                tvOnlineStatus.setText("Online");
                                            }
                                            else {
                                                tvOnlineStatus.setText(onlineStatus);
                                            }
                                        }
                                    }, 1000);

                                    if (otherUser.isOnline==1){
                                        for (int i = 0; i<chatList.size();i++){
                                            Chat chat = chatList.get(i);
                                            if (chat.readStatus==1) {
                                                chat.readStatus = 0;
                                                chattingAdapter.notifyItemChanged(i);
                                            }
                                        }
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            progress_bar.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getBlockUser(){
        blockUsersRef.child(blockUserNode).child("blockedBy").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                blockedById = "";
                try {
                    if (dataSnapshot.exists()) {
                        //if (dataSnapshot.hasChild("6_3")) {
                        blockedById = dataSnapshot.getValue(String.class);

                        tvOnlineStatus.setVisibility(View.GONE);

                        //  }
                    }else tvOnlineStatus.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMutedUser(){

        new Thread(new Runnable(){
            @Override
            public void run(){
                chatRefMuteUser.child(myUid).child(otherUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.getValue() != null) {
                                            MuteUser muteUser = dataSnapshot.getValue(MuteUser.class);
                                            assert muteUser != null;
                                            isMute = muteUser.mute;
                                        }
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

                chatRefMuteUser.child(otherUserId).child(myUid)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.getValue() != null) {
                                        MuteUser muteUser = dataSnapshot.getValue(MuteUser.class);
                                        assert muteUser != null;
                                        isOtherMute = muteUser.mute;
                                    }
                                }else
                                    isOtherMute = 0;
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                //code to do the HTTP request
            }


        }).start();
    }

    private void getNodeInfo() {
        chatList.clear();

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(myUid)) {
                    progress_bar.setVisibility(View.GONE);
                }else {
                    progress_bar.setVisibility(View.GONE);
                    // tv_no_chat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getOpponentChatInfo() {
        childEventListener = otherChatRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (isActivityOpen){
                        Chat messageOutput = dataSnapshot.getValue(Chat.class);
                        if (messageOutput != null && (messageOutput.readStatus == 0 || messageOutput.readStatus == 1)) {
                            otherChatRef.child(dataSnapshot.getKey()).child("readStatus").setValue(2);
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    if (isActivityOpen){
                        Chat messageOutput = dataSnapshot.getValue(Chat.class);
                        if (messageOutput != null && (messageOutput.readStatus == 0 || messageOutput.readStatus == 1)) {
                            otherChatRef.child(dataSnapshot.getKey()).child("readStatus").setValue(2);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    int start = 0;
    private void getMessageList(){
        Query query = myChatRef.orderByKey()/*.limitToLast(10) .startAt(start)*/;

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    Chat messageOutput = dataSnapshot.getValue(Chat.class);
                    // map.put(dataSnapshot.getKey(),messageOutput);
                    getChatDataInmap(dataSnapshot.getKey(),messageOutput);

                    // chattingAdapter.notifyDataSetChanged();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    //  recycler_view.scrollToPosition(chatList.size() - 1);
                    Chat messageOutput = dataSnapshot.getValue(Chat.class);
                    getChatDataInmap(dataSnapshot.getKey(),messageOutput);
                    // map.put(dataSnapshot.getKey(),messageOutput);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getChatDataInmap(String key, Chat chat) {
        if (chat != null) {
            map.put(key, chat);
            chatList.clear();
            Collection<Chat> values = map.values();
            chat.banner_date = CommonUtils.getDateBanner((Long) chat.timestamp);
            chatList.addAll(values);
            chattingAdapter.notifyDataSetChanged();
            recycler_view.scrollToPosition(map.size() - 1);
            progress_bar.setVisibility(View.GONE);
           /* if (chatList.size()==0) {
                progress_bar.setVisibility(View.GONE);
                //  tv_no_chat.setVisibility(View.VISIBLE);
            }else {
                progress_bar.setVisibility(View.GONE);
                //   tv_no_chat.setVisibility(View.GONE);
            }*/
        }
        shortList();
    }

    private void shortList() {
        Collections.sort(chatList, new Comparator<Chat>() {

            @Override
            public int compare(Chat a1, Chat a2) {

                if (a1.timestamp == null || a2.timestamp == null)
                    return -1;
                else {
                    Long long1 = Long.valueOf(String.valueOf(a1.timestamp));
                    Long long2 = Long.valueOf(String.valueOf(a2.timestamp));
                    return long1.compareTo(long2);
                }
            }
        });
        chattingAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()){
            case R.id.llDots:
                KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);
                //rlOptionMenu.setVisibility(View.VISIBLE);

                int[] location = new int[2];

                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                llDots.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Display display = getWindowManager().getDefaultDisplay();
                Point p = new Point();
                display.getSize(p);
                p.x = location[0];
                p.y = location[1];
                arrayList.clear();

                popupWindow(p);

                break;

            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.iv_capture_image:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(ChatActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                            }
                        }
                    }).show();
                }else {
                    isFromGallery = false;

                    KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);
                    if (blockedById.equals(myUid)) {
                        showAlert("You have blocked this user, you can't send messages");
                        // Toast.makeText(ChatActivity.this,"You have blocked this user, you can't send messages",Toast.LENGTH_SHORT).show();
                        // MyToast.getInstance(ChatActivity.this).showCustomAlert("You have blocked this user, you can't send messages");
                    }else if (blockedById.equals(otherUserId)) {
                        showAlert("You have blocked this user, you can't send messages");
                    }else if (blockedById.equalsIgnoreCase("Both")){
                        showAlert("You have blocked this user, you can't send messages");
                    }
                    else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (checkSelfPermission(android.Manifest.permission.CAMERA) !=
                                    PackageManager.PERMISSION_GRANTED){
                                requestPermissions(new String[]{android.Manifest.permission.CAMERA,
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
                            }
                            else {
                                takePhotoFromCamera();
                            }
                        }else {
                            takePhotoFromCamera();
                        }
                    }
                }
                break;

            case R.id.iv_pickImage:
                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(ChatActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                            }
                        }
                    }).show();
                }else {

                    KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);
                    isFromGallery = true;

                    if (blockedById.equals(myUid)) {
                        showAlert("You have blocked this user, you can't send messages");
                        //  MyToast.getInstance(ChatActivity.this).showCustomAlert("You have blocked this user, you can't send messages");

                    }else if (blockedById.equals(otherUserId)) {
                        showAlert("You are blocked by this user, you can't send messages");
                        //  MyToast.getInstance(ChatActivity.this).showCustomAlert("You are blocked by this user, you can't send messages");

                    }else if (blockedById.equalsIgnoreCase("Both")){
                        showAlert("You have blocked this user, you can't send messages");
                        // MyToast.getInstance(ChatActivity.this).showCustomAlert("You have blocked this user, you can't send messages");
                    }
                    else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED){
                                requestPermissions(new String[]{
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);
                            }else {
                                choosePhotoFromGallary();
                            }
                        }else {
                            choosePhotoFromGallary();
                        }
                    }
                }

                break;

            case R.id.tv_for_send:
                assert blockedById != null;
                if (blockedById.equals(myUid)) {
                    showAlert("You have blocked this user, you can't send messages");
                }else if (blockedById.equals(otherUserId)) {
                    showAlert("You are blocked by this user, you can't send messages");
                }else if (blockedById.equalsIgnoreCase("Both")){
                    showAlert("You have blocked this user, you can't send messages");
                    // MyToast.getInstance(ChatActivity.this).showCustomAlert("You have blocked this user, you can't send messages");
                }
                else {
                    String txt = et_for_sendTxt.getText().toString().trim();
                    if (!txt.equals("")) {
                        if (otherUser!=null){
                            Chat chatModel1 = new Chat();
                            chatModel1.message = txt;
                            chatModel1.timestamp = ServerValue.TIMESTAMP;
                            chatModel1.reciverId = otherUserId;
                            chatModel1.senderId = myUid;
                            chatModel1.messageType = 0;
                            chatModel1.readStatus = 2;

                            Chat chatModel2 = new Chat();
                            chatModel2.message = txt;
                            chatModel2.timestamp = ServerValue.TIMESTAMP;
                            chatModel2.reciverId = otherUserId;
                            chatModel2.senderId = myUid;
                            chatModel2.messageType = 0;
                            if (tvOnlineStatus.getText().equals("Online"))
                                chatModel2.readStatus = 0;
                            else
                                chatModel2.readStatus = 1;

                            ChatHistory chatHistory = new ChatHistory();
                            chatHistory.favourite = isMyFavourite;
                            chatHistory.memberCount = 0;
                            chatHistory.message = txt;
                            chatHistory.messageType = 0;
                            chatHistory.profilePic = otherUser.profilePic;
                            chatHistory.reciverId = otherUserId;
                            chatHistory.senderId = myUid;
                            chatHistory.type = "user";
                            chatHistory.unreadMessage = 0;
                            chatHistory.userName = otherUser.userName;
                            chatHistory.timestamp = ServerValue.TIMESTAMP;

                            ChatHistory chatHistory2 = new ChatHistory();
                            chatHistory2.favourite = isOtherFavourite;
                            chatHistory2.memberCount = 0;
                            chatHistory2.message = txt;
                            chatHistory2.messageType = 0;
                            chatHistory2.profilePic = Mualab.currentUser.profileImage;
                            chatHistory2.reciverId = otherUserId;
                            chatHistory2.senderId = myUid;
                            chatHistory2.type = "user";
                            chatHistory2.userName = Mualab.currentUser.userName;
                            chatHistory2.timestamp = ServerValue.TIMESTAMP;
                            chatHistory2.unreadMessage = unreadMsgCount;

                            writeToDBProfiles(chatModel1,chatModel2,chatHistory,chatHistory2);
                        }
                        //  tv_no_chat.setVisibility(View.GONE);

                    }

                }

                break;
        }
    }

    private void writeToDBProfiles(final Chat chatModel1, final Chat chatModel2,
                                   final ChatHistory chatHistory1, final ChatHistory chatHistory2) {

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ChatActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();

                        otherChatRef.push().setValue(chatModel1);
                        myChatRef.push().setValue(chatModel2);

                        mFirebaseDatabaseReference.child("chat_history").child(myUid).
                                child(otherUserId).setValue(chatHistory1);
                        // mFirebaseDatabaseReference.child("chat_history").child(otherUserId).child(myUid).setValue(chatHistory2);

                        otherChatHistoryRef.setValue(chatHistory2);
                        //    FirebaseDatabase.getInstance().getReference().child("history").child(session.getUser().id).child(uID).setValue(chatModel);
                        //  FirebaseDatabase.getInstance().getReference().child("history").child(uID).child(session.getUser().id).setValue(chatModel2);
                        et_for_sendTxt.setText("");

                        isTyping = false;
                        handler.removeCallbacks(runnable);
                        isOppTypingRef.setValue(null);

                        if (isOtherMute!=1){
                            if (chatHistory1.messageType == 0)
                                sendPushNotificationToReceiver(chatModel1.message,"chat");//type = chat/groupChat
                            else
                                sendPushNotificationToReceiver("Image","chat");
                        }
                    }
                }
            }).show();
        }else {
            otherChatRef.push().setValue(chatModel1);
            myChatRef.push().setValue(chatModel2);

            mFirebaseDatabaseReference.child("chat_history").child(myUid).child(otherUserId).
                    setValue(chatHistory1);
            // mFirebaseDatabaseReference.child("chat_history").child(otherUserId).child(myUid).setValue(chatHistory2);

            otherChatHistoryRef.setValue(chatHistory2);
            //    FirebaseDatabase.getInstance().getReference().child("history").child(session.getUser().id).child(uID).setValue(chatModel);
            //  FirebaseDatabase.getInstance().getReference().child("history").child(uID).child(session.getUser().id).setValue(chatModel2);
            et_for_sendTxt.setText("");

            isTyping = false;
            handler.removeCallbacks(runnable);
            isOppTypingRef.setValue(null);

            if (isOtherMute!=1){
                if (chatHistory1.messageType == 0)
                    sendPushNotificationToReceiver(chatModel1.message,"chat");//type = chat/groupChat
                else
                    sendPushNotificationToReceiver("Image","chat");
            }
        }

    }

    private void sendPushNotificationToReceiver(String message,String type) {

        if (otherUser.firebaseToken.isEmpty()){
            WebNotification webNotification = new WebNotification();
            webNotification.body = message;
            webNotification.title = Mualab.currentUser.userName;
            webNotification.url = "'/chat?uId="+myUid;
            chatRefWebnotif.child(otherUserId).push().setValue(webNotification);
        }else {
            FcmNotificationBuilder.initialize()
                    .title(Mualab.currentUser.userName)
                    .message(message).uid(myUid)
                    .username(Mualab.currentUser.userName)
                    .type(type).clickAction("ChatActivity")
                    .firebaseToken(FirebaseInstanceId.getInstance().getToken())
                    .receiverFirebaseToken(otherUser.firebaseToken).send();
        }
    }

    private void popupWindow(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) ChatActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu,(ViewGroup) findViewById(R.id.parent));
            popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

            int OFFSET_X = 450;
            int OFFSET_Y;

            if (reqString.equals("motorola Moto G (4) 7.0 M")){
                OFFSET_Y = 110;
            }else {
                OFFSET_Y = 70;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }

            if (isMyFavourite==0)
                arrayList.add("Add To Favourite");
            else
                arrayList.add("Unfavourite");

            arrayList.add("Clear Chat");

            if (isMute==0)
                arrayList.add("Mute Chat");
            else
                arrayList.add("Unmute Chat");

            arrayList.add("Block User");

            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);

            if (blockedById.equals(myUid)){
                arrayList.set(3,"Unblock User");
            }else if (blockedById.equalsIgnoreCase("Both")){
                arrayList.set(3,"Unblock User");
            } else {
                arrayList.set(3,"Block User");
            }

            MenuAdapter menuAdapter=new MenuAdapter(ChatActivity.this, arrayList, new MenuAdapter.Listener() {
                @Override
                public void onMenuClick( int pos) {
                    final String data=arrayList.get(pos);
                    popupWindow.dismiss();

                    if (!ConnectionDetector.isConnected()) {
                        new NoConnectionDialog(ChatActivity.this, new NoConnectionDialog.Listner() {
                            @Override
                            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                                if(isConnected){
                                    dialog.dismiss();
                                }
                            }
                        }).show();
                    }
                    switch (data){
                        case "Add To Favourite":
                        case "Unfavourite":

                            if (isMyFavourite==0){
                                myChatHistoryRef.child("favourite").setValue(1);
                                isMyFavourite = 1;
                            }else {
                                myChatHistoryRef.child("favourite").setValue(0);
                                isMyFavourite = 0;
                            }

                            break;
                        case "Mute Chat":
                        case "Unmute Chat":
                            popupWindow.dismiss();
                            if (isMute==0) {
                                isMute = 1;
                                chatRefMuteUser.child(myUid).child(otherUserId).child("mute").
                                        setValue(1);
                            }else {
                                isMute = 0;
                                chatRefMuteUser.child(myUid).child(otherUserId).removeValue();
                            }

                            break;
                        case "Clear Chat":
                            popupWindow.dismiss();
                            showAlertDeleteChat();
                            break;

                        case "Unblock User":
                        case "Block User":
                            popupWindow.dismiss();
                            if (blockedById==null || blockedById.equals("")){
                                popupWindow.dismiss();
                                showAlertBlock();
                            }else if (blockedById.equals(myUid)){
                                popupWindow.dismiss();
                                blockUsersRef.child(blockUserNode).setValue(null);
                            }else if (blockedById.equals(otherUserId)){
                                showAlertBlock();
                            } else if (blockedById.equalsIgnoreCase("Both")){
                                popupWindow.dismiss();
                                BlockUser blockUser = new BlockUser();
                                blockUser.blockedBy = otherUserId;
                                blockUsersRef.child(blockUserNode).setValue(blockUser);
                            }

                            break;
                    }
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertDeleteChat(){
        final Dialog dialog = new Dialog(ChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_delete_chat);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                progress_bar.setVisibility(View.VISIBLE);
                KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                        myChatRef.removeValue();
                        //mapKey.put(dataSnapshot.getKey(),dataSnapshot.getKey());
                        mFirebaseDatabaseReference.child("chat_history").child(myUid).child(otherUserId).
                                removeValue();
                        map.clear();
                        chatList.clear();
                        chattingAdapter.notifyDataSetChanged();
                        progress_bar.setVisibility(View.GONE);
                        //    tv_no_chat.setVisibility(View.VISIBLE);
                    }
                },400);
            }
        });

        Button btn_no=dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View view = dialog.getWindow().getDecorView();
                //for enter from left
                ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    private void showAlertBlock(){
        final Dialog dialog = new Dialog(ChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_block_chat);

        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_user_name_chat = dialog.findViewById(R.id.tv_user_name_chat);
        tv_user_name_chat.setText(otherUser.userName+"?");

        dialog.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress_bar.setVisibility(View.VISIBLE);
                KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                        progress_bar.setVisibility(View.GONE);

                        if (blockedById.equals(otherUserId)){
                            BlockUser blockUser2 = new BlockUser();
                            blockUser2.blockedBy = "Both";
                            blockUsersRef.child(blockUserNode).setValue(blockUser2);
                            dialog.dismiss();
                        }else {
                            BlockUser blockUser = new BlockUser();
                            blockUser.blockedBy = myUid;
                            blockedById = myUid;
                            blockUsersRef.child(blockUserNode).setValue(blockUser);
                            dialog.dismiss();
                        }

                    }
                },300);
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View view = dialog.getWindow().getDecorView();
                //for enter from left
                ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });


        dialog.show();
    }

    private void showAlert(String msg){
        final Dialog dialog = new Dialog(ChatActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.new_alert_dialog);

        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView tv_alert_msg = dialog.findViewById(R.id.tv_alert_msg);
        tv_alert_msg.setText(msg);

        dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View view = dialog.getWindow().getDecorView();
                //for enter from left
                ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), 0.0f).start();
                //for enter from bottom
                //ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0.0f).start();
            }
        });

        dialog.show();
    }

    @Override
    public void onSoftKeyboardHide() {
        isTyping = false;
        isOppTypingRef.setValue(null);
    }

    @Override
    public void onSoftKeyboardShow() {
        recycler_view.scrollToPosition(chatList.size() - 1);
        et_for_sendTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (blockedById.equals("")) {
                    if (charSequence.length()!=0)
                        setIsTypingStatus();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isTyping = false;
            isOppTypingRef.setValue(null);
            // Log.e("Chat","set is typing to false");
        }
    };

    private void setIsTypingStatus() {
        System.out.println("printing......."+isTyping);
        if (!isTyping){
            // Log.e("Chat","set is typing to fcm");
            Typing typing = new Typing();
            typing.isTyping = 1;
            typing.reciverId = otherUserId;
            typing.senderId = myUid;
            //set isTyping to fcm
            isOppTypingRef.setValue(typing);
        }
        isTyping = true;
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 3000);

    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (imageUri != null) {
            Progress.show(ChatActivity.this);
           /* final ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this,R.style.AppCompatProgrssDialogStyle);
            progressDialog.setTitle(getString(R.string.loading));
            progressDialog.show();
            progressDialog.setCancelable(false);*/

            StorageReference storageReference = storage.getReference();

            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  progressDialog.dismiss();
                            Uri fireBaseUri = taskSnapshot.getDownloadUrl();
                            assert fireBaseUri != null;

                            Chat chatModel1 = new Chat();
                            chatModel1.message = fireBaseUri.toString();
                            chatModel1.timestamp = ServerValue.TIMESTAMP;
                            chatModel1.reciverId = otherUserId;
                            chatModel1.senderId = myUid;
                            chatModel1.messageType = 1;
                            chatModel1.readStatus = 2;

                            Chat chatModel2 = new Chat();
                            chatModel2.message = fireBaseUri.toString();
                            chatModel2.timestamp = ServerValue.TIMESTAMP;
                            chatModel2.reciverId = otherUserId;
                            chatModel2.senderId = myUid;
                            chatModel2.messageType = 1;
                            if (onlineStatus.equals("Online"))
                                chatModel2.readStatus = 0;
                            else
                                chatModel2.readStatus = 1;

                            ChatHistory chatHistory = new ChatHistory();
                            chatHistory.favourite = isMyFavourite;
                            chatHistory.memberCount = 0;
                            chatHistory.message = fireBaseUri.toString();
                            chatHistory.messageType = 1;
                            chatHistory.profilePic = otherUser.profilePic;
                            chatHistory.reciverId = otherUserId;
                            chatHistory.senderId = myUid;
                            chatHistory.type = "user";
                            chatHistory.unreadMessage = 0;
                            chatHistory.userName = otherUser.userName;
                            chatHistory.timestamp = ServerValue.TIMESTAMP;

                            ChatHistory chatHistory2 = new ChatHistory();
                            chatHistory2.favourite = isOtherFavourite;
                            chatHistory2.memberCount = 0;
                            chatHistory2.message = fireBaseUri.toString();
                            chatHistory2.messageType = 1;
                            chatHistory2.profilePic = Mualab.currentUser.profileImage;
                            chatHistory2.reciverId = otherUserId;
                            chatHistory2.senderId = myUid;
                            chatHistory2.type = "user";
                            chatHistory2.userName = Mualab.currentUser.userName;
                            chatHistory2.timestamp = ServerValue.TIMESTAMP;
                            chatHistory2.unreadMessage = unreadMsgCount;

                            writeToDBProfiles(chatModel1,chatModel2,chatHistory,chatHistory2);
                            Progress.hide(ChatActivity.this);
                            //  tv_no_chat.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //  progressDialog.dismiss();
                            Progress.hide(ChatActivity.this);
                            Log.e("TAG", "onFailure: " + e.getMessage());
                            Toast.makeText(ChatActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            //   progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    public void choosePhotoFromGallary() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, Constant.CAMERA_REQUEST);
    }

    private void takePhotoFromCamera() {
        ImagePicker.pickImageFromCamera(this);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isFromGallery)
                        choosePhotoFromGallary();
                    else
                        takePhotoFromCamera();
                } else
                    MyToast.getInstance(ChatActivity.this).showDasuAlert("Your  Permission Denied");

            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if (resultCode == RESULT_OK) {

            if (requestCode == Constant.CAMERA_REQUEST) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(400, 400).start(this);
                } else {
                    MyToast.getInstance(ChatActivity.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }

            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null) {
                        uri = result.getUri();
                        uploadImage(uri);
                    }
                      /*  bmChatImg = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());

                    if (bmChatImg != null) {
                        Uri imageUri = ImageUtil.getImageUri(ChatActivity.this,bmChatImg);
                        uploadImage(imageUri);
                    }*/
                } catch (Exception | OutOfMemoryError e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOppTypingRef.setValue(null);
        isActivityOpen = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isActivityOpen = false;
        softKeyboard.unRegisterSoftKeyboardCallback();
        KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);
        FirebaseDatabase.getInstance().getReference().removeEventListener(otherUserDetail);
        FirebaseDatabase.getInstance().getReference().removeEventListener(childEventListener);

        Glide.get(this).clearMemory();
    }

    @Override
    public void onBackPressed() {
        KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);
        isActivityOpen = false;
        isTyping = false;
        handler.removeCallbacks(runnable);
        isOppTypingRef.setValue(null);

        myChatHistoryRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    String key = dataSnapshot.getKey();
                    if (!key.contains("group_")) {
                        ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                        assert messageOutput != null;
                        myChatHistoryRef.child("unreadMessage").setValue(0);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

      /*  mFirebaseDatabaseReference.removeEventListener(otherUserDetail);
        mFirebaseDatabaseReference.removeEventListener(childEventListener);

        otherChatRef = null;*/

        super.onBackPressed();
        finish();

        startActivity(new Intent(ChatActivity.this,ChatHistoryActivity.class));
    }

    @Override
    public void onScrollChange(final int position, final Object timestamp) {

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    tv_chat_date.setVisibility(View.GONE);
                }
               /* int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                if (firstVisiblePosition==0 | firstVisiblePosition==-1)
                    chattingAdapter.setDateLableVisible(true);
                else
                    chattingAdapter.setDateLableVisible(false);
                //tv_chat_date.setVisibility(View.VISIBLE);*/
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                if (layoutManager.findFirstVisibleItemPosition() != -1) {
                    tv_chat_date.setText(chatList.get(firstVisiblePosition).banner_date);
                    if (!chatList.get(firstVisiblePosition).banner_date.equals(""))
                        tv_chat_date.setVisibility(View.VISIBLE);
                }
            }
        });

    }
}
