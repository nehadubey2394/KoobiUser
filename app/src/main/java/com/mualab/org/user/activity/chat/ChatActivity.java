package com.mualab.org.user.activity.chat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.model.UserProfileData;
import com.mualab.org.user.activity.chat.adapter.ChattingAdapter;
import com.mualab.org.user.activity.chat.model.Chat;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.ScreenUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText et_for_sendTxt;
    private TextView tv_no_chat;
    private ProgressBar progress_bar;
    private RecyclerView recycler_view;
    private RelativeLayout rlOptionMenu;
    private ChattingAdapter chattingAdapter;
    private List<Chat> chatList;
    private Map<String, Chat> map;
    private String myUid,otherUserId;
    private FirebaseUser otherUser;
    private LinearLayout llDots;
    private  PopupWindow popupWindow;
    private DatabaseReference mFirebaseDatabaseReference,chatRef,chatRef1,chatRef2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        otherUserId = intent.getStringExtra("userId");
        myUid = String.valueOf(Mualab.currentUser.id);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        chatRef = mFirebaseDatabaseReference.child("chat");
        init();
    }

    private void init(){
        // otherUserId = "17";
        chatList = new ArrayList<>();
        map = new HashMap<>();
        chattingAdapter = new ChattingAdapter(ChatActivity.this,chatList,myUid);

        et_for_sendTxt = findViewById(R.id.et_for_sendTxt);
        tv_no_chat = findViewById(R.id.tv_no_chat);
        progress_bar = findViewById(R.id.progress_bar);

        ImageView btnBack = findViewById(R.id.btnBack);

        TextView tv_for_send = findViewById(R.id.tv_for_send);
        final TextView tvUserName = findViewById(R.id.tvUserName);
        recycler_view = findViewById(R.id.recycler_view);
        final CircleImageView ivUserProfile = findViewById(R.id.ivUserProfile);
        AppCompatImageView iv_pickImage = findViewById(R.id.iv_pickImage);

        llDots = findViewById(R.id.llDots);
        rlOptionMenu = findViewById(R.id.rlOptionMenu);
        //  TextView tvClearChat = findViewById(R.id.tvClearChat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, false);
        // layoutManager.scrollToPositionWithOffset(0, 0);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(chattingAdapter);

        //getting user data from user table
        mFirebaseDatabaseReference.child("users").child(otherUserId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            try {
                                otherUser = dataSnapshot.getValue(FirebaseUser.class);
                                if (otherUser != null) {
                                    tvUserName.setText(otherUser.userName);
                                    if (otherUser.profilePic !=null && !otherUser.profilePic.isEmpty()) {
                                        Picasso.with(ChatActivity.this).load(otherUser.profilePic).placeholder(R.drawable.defoult_user_img).
                                                fit().into(ivUserProfile);
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }

                        chatRef1 = chatRef.child(myUid).child(otherUserId);
                        chatRef2 = chatRef.child(otherUserId).child(myUid);
                        getNodeInfo();

                        getMessageList();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        iv_pickImage.setOnClickListener(this);
        tv_for_send.setOnClickListener(this);
        llDots.setOnClickListener(this);
        //tvClearChat.setOnClickListener(this);
        btnBack.setOnClickListener(this);
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
                    tv_no_chat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getMessageList(){

        chatRef1.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat messageOutput = dataSnapshot.getValue(Chat.class);
               // map.put(dataSnapshot.getKey(),messageOutput);
                getChatDataInmap(dataSnapshot.getKey(),messageOutput);

                /*if (messageOutput != null) {
                    chatList.add(messageOutput);
                    map.put(dataSnapshot.getKey(),messageOutput);
                }*/
                if (chatList.size()==0) {
                    progress_bar.setVisibility(View.GONE);
                    tv_no_chat.setVisibility(View.VISIBLE);
                }else {
                    progress_bar.setVisibility(View.GONE);
                    tv_no_chat.setVisibility(View.GONE);
                }
                recycler_view.scrollToPosition(chatList.size() - 1);
                chattingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
              //  recycler_view.scrollToPosition(chatList.size() - 1);
                Chat messageOutput = dataSnapshot.getValue(Chat.class);
                getChatDataInmap(dataSnapshot.getKey(),messageOutput);
               // map.put(dataSnapshot.getKey(),messageOutput);
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
                chatList.addAll(values);
                recycler_view.scrollToPosition(map.size() - 1);
                chattingAdapter.notifyDataSetChanged();
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
        switch (v.getId()){
            case R.id.llDots:
                KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);
                //rlOptionMenu.setVisibility(View.VISIBLE);

                int[] location = new int[2];

                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                llDots.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Point p = new Point();
                p.x = location[0];
                p.y = location[1];

                initiatePopupWindow(p);
                break;

         /*   case R.id.tvClearChat:
                progress_bar.setVisibility(View.VISIBLE);
                KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chatRef1.removeValue();
                        //mapKey.put(dataSnapshot.getKey(),dataSnapshot.getKey());
                        mFirebaseDatabaseReference.child("chat_history").child(myUid).child(otherUserId).
                                removeValue();
                        chatList.clear();
                        chattingAdapter.notifyDataSetChanged();
                        rlOptionMenu.setVisibility(View.GONE);
                        progress_bar.setVisibility(View.GONE);
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }
                },400);

                break;
*/
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.tv_for_send:
                String txt = et_for_sendTxt.getText().toString().trim();
                if (!txt.equals("")) {
                    Chat chatModel1 = new Chat();
                    chatModel1.message = txt;
                    chatModel1.timestamp = ServerValue.TIMESTAMP;
                    chatModel1.reciverId = otherUserId;
                    chatModel1.senderId = myUid;
                    chatModel1.messageType = 0;
                    chatModel1.readStatus = 0;

                    Chat chatModel2 = new Chat();
                    chatModel2.message = txt;
                    chatModel2.timestamp = ServerValue.TIMESTAMP;
                    chatModel2.reciverId = otherUserId;
                    chatModel2.senderId = myUid;
                    chatModel2.messageType = 0;
                    chatModel2.readStatus = 2;

                    ChatHistory chatHistory = new ChatHistory();
                    ChatHistory chatHistory2 = new ChatHistory();
                    chatHistory.favourite = 0;
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

                    chatHistory2 = chatHistory;
                    chatHistory2.userName = Mualab.currentUser.userName;
                    chatHistory2.unreadMessage = 1;

                    writeToDBProfiles(chatModel1,chatModel2,chatHistory,chatHistory2);
                    tv_no_chat.setVisibility(View.GONE);

                } else {
                    MyToast.getInstance(ChatActivity.this).
                            showLongCustomToast(getResources().getString(R.string.enter_text));
                }

                break;
        }
    }

    private void writeToDBProfiles(Chat chatModel1,Chat chatModel2,
                                   ChatHistory chatHistory1,ChatHistory chatHistory2) {
        mFirebaseDatabaseReference.child("chat_history").child(myUid).child(otherUserId).
                setValue(chatHistory1);
        mFirebaseDatabaseReference.child("chat_history").child(otherUserId).child(myUid)
                .setValue(chatHistory2);

        chatRef1.push().setValue(chatModel1);
        chatRef2.push().setValue(chatModel2);
        et_for_sendTxt.setText("");
        //    FirebaseDatabase.getInstance().getReference().child("history").child(session.getUser().id).child(uID).setValue(chatModel);
        //  FirebaseDatabase.getInstance().getReference().child("history").child(uID).child(session.getUser().id).setValue(chatModel2);

    }

    private void initiatePopupWindow( Point p) {
        try {
            //We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) ChatActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Inflate the view from a predefined XML layout
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu,
                    (ViewGroup) findViewById(R.id.parent));
            // create a 300px width and 470px height PopupWindow
            popupWindow = new PopupWindow(layout, 300, 470, true);
            int OFFSET_X = 450;
            int OFFSET_Y = 60;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }
            // display the popup in the center
            //  popupWindow.showAtLocation(llDots,, (int) llDots.getPivotX(), (int) llDots.getPivotY());
            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);

            TextView tvClearChat = layout.findViewById(R.id.tvClearChat);
            tvClearChat.setOnClickListener(new TextView.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    showAlertDailog();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertDailog(){
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ChatActivity.this,
                R.style.MyDialogTheme);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Delete Conversation?");
        alertDialog.setMessage("You will not be able to recover this conversation.");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                dialog.cancel();
                progress_bar.setVisibility(View.VISIBLE);
                KeyboardUtil.hideKeyboard(et_for_sendTxt,ChatActivity.this);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        popupWindow.dismiss();
                        chatRef1.removeValue();
                        //mapKey.put(dataSnapshot.getKey(),dataSnapshot.getKey());
                        mFirebaseDatabaseReference.child("chat_history").child(myUid).child(otherUserId).
                                removeValue();
                        map.clear();
                        chatList.clear();
                        chattingAdapter.notifyDataSetChanged();
                        rlOptionMenu.setVisibility(View.GONE);
                        progress_bar.setVisibility(View.GONE);
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }
                },400);

            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();

    }

}
