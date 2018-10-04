package com.mualab.org.user.activity.chat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.adapter.ChatHistoryAdapter;
import com.mualab.org.user.activity.chat.adapter.MenuAdapter;
import com.mualab.org.user.activity.chat.model.BlockUser;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.activity.chat.model.MuteUser;
import com.mualab.org.user.activity.chat.model.Typing;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.CommonUtils;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatHistoryActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tv_no_chat;
    private ProgressBar progress_bar;
    private ChatHistoryAdapter historyAdapter;
    private SearchView searchview;
    private List<ChatHistory> chatHistories,newList;
    private Map<String, ChatHistory> listmap;
    private Map<String, BlockUser> blockUsersMap;
    private Map<String, MuteUser> muteUsersMap;
    private DatabaseReference databaseReference,isOppTypingRef,blockUsersRef,chatRefMuteUser;
    private Thread thread,blockThread;
    private String myId;
    private ImageView ic_add_chat,ivFavChat,ivChatFilter;
    private long mLastClickTime = 0;
    private boolean isFavFilter = false,isReadFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        myId = String.valueOf(Mualab.currentUser.id);
        // final String myChild = Mualab.currentUser.id+"_"+myUid;

        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference = mFirebaseDatabaseReference.child("chat_history").
                child(String.valueOf(Mualab.currentUser.id));
        isOppTypingRef = mFirebaseDatabaseReference.child(Constant.IS_TYPING);
        blockUsersRef = mFirebaseDatabaseReference.child("block_users");
        chatRefMuteUser = mFirebaseDatabaseReference.child("mute_user").child(myId);
        init();
    }

    private void init(){
        newList = new ArrayList<>();
        chatHistories = new ArrayList<>();
        listmap = new HashMap<>();
        blockUsersMap = new HashMap<>();
        muteUsersMap = new HashMap<>();

        historyAdapter = new ChatHistoryAdapter(ChatHistoryActivity.this,chatHistories);

        tv_no_chat = findViewById(R.id.tv_no_chat);
        progress_bar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btnBack);
        ivFavChat = findViewById(R.id.ivFavChat);
        ivChatFilter = findViewById(R.id.ivChatFilter);
        ic_add_chat = findViewById(R.id.ic_add_chat);
        ImageView ivChatReq = findViewById(R.id.ivChatReq);
        ic_add_chat.setVisibility(View.VISIBLE);
        ivChatReq.setVisibility(View.VISIBLE);
        RecyclerView rvChatHistory = findViewById(R.id.rvChatHistory);
        rvChatHistory.setAdapter(historyAdapter);
        historyAdapter.notifyDataSetChanged();

        searchview = findViewById(R.id.searchview);
        KeyboardUtil.hideKeyboard(searchview, ChatHistoryActivity.this);
        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //  filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String str = newText.trim();
                filter(str);
                return true;
            }
        });

        btnBack.setOnClickListener(this);
        ic_add_chat.setOnClickListener(this);
        ivFavChat.setOnClickListener(this);
        ivChatFilter.setOnClickListener(this);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(ChatHistoryActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                    }
                }
            }).show();
        }

        getHistoryList();

        thread = new Thread(new Runnable(){
            @Override
            public void run(){
                getTyppingUser();
                //code to do the HTTP request
            }
        });
        thread.start();

        blockThread = new Thread(new Runnable(){
            @Override
            public void run(){
                getBlockUser();
            }
        });
        blockThread.start();

        new Thread(new Runnable(){
            @Override
            public void run(){
                getMutedUser();
            }
        }).start();

    }

    private void getTyppingUser(){

        final String myChild = myId+"_";

        isOppTypingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("typping===");

                try {

                    if (!dataSnapshot.getKey().contains(myChild)){

                        if (!dataSnapshot.getKey().contains("broadcast_") ||
                                !dataSnapshot.getKey().contains("broadcast_group")){

                            final Typing typing = dataSnapshot.getValue(Typing.class);
                            String node = dataSnapshot.getKey();

                            assert typing != null;
                            String blockNode = typing.senderId+"_"+myId;
                            if (!blockUsersMap.containsKey(blockNode)){
                                if (typing.senderId.equals(myId) || typing.reciverId.equals(myId)){
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (chatHistories.size()!=0 ){
                                                for (int i=0;i<chatHistories.size();i++){
                                                    ChatHistory chatHistory = chatHistories.get(i);
                                                    if (typing.senderId.equals(chatHistory.senderId) || typing.senderId.
                                                            equals(chatHistory.reciverId)){
                                                        chatHistory.isTyping = true;
                                                        historyAdapter.setTyping(true,i);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }, 400);
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    if (!dataSnapshot.getKey().contains(myChild)){

                        if (!dataSnapshot.getKey().contains("broadcast_") ||
                                !dataSnapshot.getKey().contains("broadcast_group")){

                            final Typing typing = dataSnapshot.getValue(Typing.class);
                            String node = dataSnapshot.getKey();

                            assert typing != null;
                            String blockNode = typing.senderId+"_"+myId;

                            if (!blockUsersMap.containsKey(blockNode)){
                                if (typing.senderId.equals(myId) || typing.reciverId.equals(myId)){
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (chatHistories.size()!=0 ){
                                                for (int i=0;i<chatHistories.size();i++){
                                                    ChatHistory chatHistory = chatHistories.get(i);
                                                    if (typing.senderId.equals(chatHistory.senderId) || typing.senderId.
                                                            equals(chatHistory.reciverId)){
                                                        chatHistory.isTyping = true;
                                                        historyAdapter.setTyping(true,i);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }, 400);
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {


                try {
                    // thread.interrupt();
                    Typing typing = dataSnapshot.getValue(Typing.class);
                    if (dataSnapshot.exists()){
                        assert typing != null;
                        if (listmap.containsKey(typing.senderId)&& chatHistories.size()!=0){
                            for (int i=0;i<chatHistories.size();i++){
                                ChatHistory chatHistory = chatHistories.get(i);
                                if (typing.senderId.equals(chatHistory.senderId) || typing.senderId.
                                        equals(chatHistory.reciverId)){
                                    chatHistory.isTyping = false;
                                    historyAdapter.setTyping(false,i);
                                    break;
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getHistoryList() {

        databaseReference.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    if(dataSnapshot.getValue()== null){
                        if (!dataSnapshot.exists()){
                            tv_no_chat.setVisibility(View.VISIBLE);
                        }else{
                            tv_no_chat.setVisibility(View.GONE);
                        }
                        progress_bar.setVisibility(View.GONE);
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
                tv_no_chat.setVisibility(View.VISIBLE);
            }
        });

        chatHistories.clear();
        newList.clear();

        databaseReference.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                try {
                    if (!key.contains("group_")){
                        ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);

                        if (messageOutput != null) {
                            getChatDataInMap(key,messageOutput);
                        }
                    }else {
                        progress_bar.setVisibility(View.GONE);
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }

                    if(chatHistories.size() == 0){
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }else{
                        tv_no_chat.setVisibility(View.GONE);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    String key = dataSnapshot.getKey();
                    if (!key.contains("group_")){
                        ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                        if (messageOutput != null) {
                            getChatDataInMap(key,messageOutput);
                        }
                    }else {
                        progress_bar.setVisibility(View.GONE);
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }

                    if(chatHistories.size() == 0){
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }else{
                        tv_no_chat.setVisibility(View.GONE);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    String key = dataSnapshot.getKey();

                    if (!key.contains("group_")){
                        ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                        listmap.remove(dataSnapshot.getKey());

                        for(int i = 0 ; i<chatHistories.size();i++){
                            if(chatHistories.get(i).senderId.equals(dataSnapshot.getKey()) | chatHistories.get(i).reciverId.equals(dataSnapshot.getKey())){
                                chatHistories.remove(i);
                                historyAdapter.notifyItemRemoved(i);
                                break;
                            }
                        }

                    }
                    progress_bar.setVisibility(View.GONE);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                progress_bar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });

    }

    private void getBlockUser(){
        blockUsersRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    BlockUser blockUser = dataSnapshot.getValue(BlockUser.class);
                    blockUsersMap.put(dataSnapshot.getKey(),blockUser);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    BlockUser blockUser = dataSnapshot.getValue(BlockUser.class);
                    blockUsersMap.put(dataSnapshot.getKey(),blockUser);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    blockUsersMap.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getMutedUser(){
        chatRefMuteUser.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (dataSnapshot.exists()) {
                        MuteUser muteUser = dataSnapshot.getValue(MuteUser.class);
                        muteUsersMap.put(dataSnapshot.getKey(),muteUser);

                        String key = dataSnapshot.getKey();
                        if (chatHistories!=null && chatHistories.size()!=0){
                            for (int i=0;i<chatHistories.size();i++){
                                ChatHistory chatHistory = chatHistories.get(i);
                                if (chatHistory.senderId.equals(key) || chatHistory.reciverId.
                                        equals(key) ){
                                    chatHistory.isMute = true;
                                    historyAdapter.setMute(i);
                                    break;
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    if (dataSnapshot.exists()) {
                        MuteUser muteUser = dataSnapshot.getValue(MuteUser.class);
                        muteUsersMap.put(dataSnapshot.getKey(),muteUser);
                        String key = dataSnapshot.getKey();
                        if (chatHistories!=null && chatHistories.size()!=0){
                            for (int i=0;i<chatHistories.size();i++){
                                ChatHistory chatHistory = chatHistories.get(i);
                                if (chatHistory.senderId.equals(key) || chatHistory.reciverId.
                                        equals(key) ){
                                    chatHistory.isMute = true;
                                    historyAdapter.setMute(i);
                                    break;
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()){
                        String key = dataSnapshot.getKey();
                        muteUsersMap.remove(key);
                        if (chatHistories!=null && chatHistories.size()!=0){
                            for (int i=0;i<chatHistories.size();i++){
                                ChatHistory chatHistory = chatHistories.get(i);
                                if (chatHistory.senderId.equals(key) || chatHistory.reciverId.
                                        equals(key) ){
                                    chatHistory.isMute = false;
                                    historyAdapter.setMute(i);
                                    break;
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getChatDataInMap(String key, ChatHistory messageOutput) {
        if (messageOutput != null) {
            if (messageOutput.type.equals("user")){
                messageOutput.isTyping = false;

                if (muteUsersMap.containsKey(messageOutput.senderId) || muteUsersMap.
                        containsKey(messageOutput.reciverId)){
                    messageOutput.isMute = true;
                }else
                    messageOutput.isMute = false;

                messageOutput.banner_date = CommonUtils.getDateBanner((Long) messageOutput.timestamp);
                listmap.put(key, messageOutput);
                tv_no_chat.setVisibility(View.GONE);
                Collection<ChatHistory> demoValues = listmap.values();
                newList.clear();
                chatHistories.clear();
                newList.addAll(demoValues);
                chatHistories.addAll(newList);
                shorting();
            }
        }
    }

    public void shorting() {

        Collections.sort(chatHistories, new Comparator<ChatHistory>() {
            @Override
            public int compare(ChatHistory a1, ChatHistory a2) {
                if (a1.timestamp == null || a2.timestamp == null) {
                    return -1;
                } else {
                    Long long1 = Long.parseLong(String.valueOf(a1.timestamp));
                    Long long2 = Long.parseLong(String.valueOf(a2.timestamp));
                    return long2.compareTo(long1);
                }
            }
        });

        //   historyAdapter.setTyping(false);

        progress_bar.setVisibility(View.GONE);
        historyAdapter.notifyDataSetChanged();
    }

    private void filter(String text) {
        //new array list that will hold the filtered data
        List<ChatHistory> filterdNames = new ArrayList<>();

        //looping through existing elements
        for (ChatHistory s : chatHistories) {
            //if the existing elements contains the search input
            if (s.userName.toLowerCase(Locale.getDefault()).contains(text)) {
                //adding the element to filtered list
                filterdNames.add(s);
            }
        }

        if (filterdNames.size()==0)
            tv_no_chat.setVisibility(View.VISIBLE);
        else
            tv_no_chat.setVisibility(View.GONE);
        //calling a method of the adapter class and passing the filtered list
        historyAdapter.filterList(filterdNames);


    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.ivFavChat:
                favouriteFilter();
                break;

            case R.id.ivChatFilter:
                KeyboardUtil.hideKeyboard(searchview,ChatHistoryActivity.this);
                //rlOptionMenu.setVisibility(View.VISIBLE);

                int[] point = new int[2];

                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                ivChatFilter.getLocationOnScreen(point);

                //Initialize the Point with x, and y positions
                Display display = getWindowManager().getDefaultDisplay();
                Point p = new Point();
                display.getSize(p);
                p.x = point[0];
                p.y = point[1];

                popupForHisyory(p);

                break;

            case R.id.ic_add_chat:
                KeyboardUtil.hideKeyboard(searchview,ChatHistoryActivity.this);

                int[] location = new int[2];

                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                ic_add_chat.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Display display2 = getWindowManager().getDefaultDisplay();
                Point p2 = new Point();
                display2.getSize(p2);
                p2.x = location[0];
                p2.y = location[1];

                popupWindowForAdd(p2);

                break;
        }
    }

    private void favouriteFilter(){
        if (isFavFilter){
            isFavFilter = false;
            historyAdapter.filterList(chatHistories);
            ivFavChat.setImageResource(R.drawable.star_ico);
            if (chatHistories.size()==0)
                tv_no_chat.setVisibility(View.VISIBLE);
            else
                tv_no_chat.setVisibility(View.GONE);
        }else {
            isFavFilter = true;
            List<ChatHistory> tempArrayList = new ArrayList<>();
            ivFavChat.setImageResource(R.drawable.fill_star_ico);
            ivFavChat.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));


            for (ChatHistory history : chatHistories) {
                //if the existing elements contains the search input
                if (history.favourite==1) {
                    tempArrayList.add(history);
                }
            }

            if (tempArrayList.size()==0)
                tv_no_chat.setVisibility(View.VISIBLE);
            else
                tv_no_chat.setVisibility(View.GONE);
            //calling a method of the adapter class and passing the filtered list
            historyAdapter.filterList(tempArrayList);
        }
    }

    private void readUnreadFilter(){
        if (isReadFilter){
            isReadFilter = false;
            List<ChatHistory> tempArrayList = new ArrayList<>();

            for (ChatHistory history : chatHistories) {
                //if the existing elements contains the search input
                if (history.unreadMessage==0) {
                    tempArrayList.add(history);
                }
            }

            if (tempArrayList.size()==0)
                tv_no_chat.setVisibility(View.VISIBLE);
            else
                tv_no_chat.setVisibility(View.GONE);
            //calling a method of the adapter class and passing the filtered list
            historyAdapter.filterList(tempArrayList);

        }else {
            isReadFilter = true;
            List<ChatHistory> tempArrayList = new ArrayList<>();

            for (ChatHistory history : chatHistories) {
                //if the existing elements contains the search input
                if (history.unreadMessage>0) {
                    tempArrayList.add(history);
                }
            }

            if (tempArrayList.size()==0)
                tv_no_chat.setVisibility(View.VISIBLE);
            else
                tv_no_chat.setVisibility(View.GONE);
            //calling a method of the adapter class and passing the filtered list
            historyAdapter.filterList(tempArrayList);
        }
    }

    private void allGroup(){
        List<ChatHistory> tempArrayList = new ArrayList<>();

        for (ChatHistory history : chatHistories) {
            //if the existing elements contains the search input
            if (history.type.equals("group")) {
                tempArrayList.add(history);
            }
        }

        if (tempArrayList.size()==0)
            tv_no_chat.setVisibility(View.VISIBLE);
        else
            tv_no_chat.setVisibility(View.GONE);
        //calling a method of the adapter class and passing the filtered list
        historyAdapter.filterList(tempArrayList);
    }

    private void popupWindowForAdd(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) ChatHistoryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu,(ViewGroup) findViewById(R.id.parent));
            final PopupWindow popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);


            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();

            int OFFSET_X;
            int OFFSET_Y;

            if (reqString.equals("motorola Moto G (4) 7.0 M")){
                OFFSET_X = 200;
                OFFSET_Y = 96;
            }else {
                OFFSET_X = 200;
                OFFSET_Y = 58;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }
            final ArrayList<String>arrayList = new ArrayList<>();
            arrayList.add("Create new chat");
            arrayList.add("Create new group");
            arrayList.add("Join new group");

            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);
            MenuAdapter menuAdapter=new MenuAdapter(ChatHistoryActivity.this, arrayList, new MenuAdapter.Listener() {
                @Override
                public void onMenuClick(int pos) {
                    String data=arrayList.get(pos);

                    if (!ConnectionDetector.isConnected()) {
                        new NoConnectionDialog(ChatHistoryActivity.this, new NoConnectionDialog.Listner() {
                            @Override
                            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                                if(isConnected){
                                    dialog.dismiss();
                                }
                            }
                        }).show();
                    }
                    popupWindow.dismiss();

                    switch (data){
                        case "Create new chat":
                            break;
                        case "Create new group":
                            startActivity(new Intent(ChatHistoryActivity.this,CreateGroupActivity.class));
                            break;

                        case "Join new group":
                            break;

                    }
                }
            });

            LinearLayoutManager layoutManager=new LinearLayoutManager(ChatHistoryActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void popupForHisyory(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) ChatHistoryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu,(ViewGroup) findViewById(R.id.parent));
            final PopupWindow popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setElevation(5);
            }

            String reqString = Build.MANUFACTURER
                    + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                    + " " + Build.VERSION_CODES.class.getFields()[android.os.Build.VERSION.SDK_INT].getName();


            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            popupWindow.setOutsideTouchable(true);
            popupWindow.setFocusable(true);

            int OFFSET_X;
            int OFFSET_Y;

            if (reqString.equals("motorola Moto G (4) 7.0 M")){
                OFFSET_X = 480;
                OFFSET_Y = 70;
            }else {
                OFFSET_X = 480;
                OFFSET_Y = 48;
            }

            final ArrayList<String>arrayList = new ArrayList<>();
            arrayList.add("All");
            arrayList.add("All Groups");
            arrayList.add("My Groups");
            arrayList.add("Read");
            arrayList.add("Unread");

            popupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
            RecyclerView recycler_view = layout.findViewById(R.id.recycler_view);
            MenuAdapter menuAdapter=new MenuAdapter(ChatHistoryActivity.this, arrayList, new MenuAdapter.Listener() {
                @Override
                public void onMenuClick(int pos) {
                    final String data=arrayList.get(pos);
                    popupWindow.dismiss();

                    if (!ConnectionDetector.isConnected()) {
                        new NoConnectionDialog(ChatHistoryActivity.this, new NoConnectionDialog.Listner() {
                            @Override
                            public void onNetworkChange(Dialog dialog, boolean isConnected) {
                                if(isConnected){
                                    dialog.dismiss();
                                }
                            }
                        }).show();
                    }
                    switch (data){
                        case "All":
                            popupWindow.dismiss();
                            historyAdapter.filterList(chatHistories);
                            if (chatHistories.size()==0)
                                tv_no_chat.setVisibility(View.VISIBLE);
                            else
                                tv_no_chat.setVisibility(View.GONE);

                            break;

                        case "All Groups":
                            popupWindow.dismiss();
                            allGroup();
                            break;

                        case "My Groups":
                            popupWindow.dismiss();
                            allGroup();
                            break;

                        case "Read":
                            isReadFilter = true;
                            popupWindow.dismiss();
                            readUnreadFilter();
                            break;

                        case "Unread":
                            isReadFilter = false;
                            popupWindow.dismiss();
                            readUnreadFilter();
                            break;

                    }

                }
            });

            LinearLayoutManager layoutManager=new LinearLayoutManager(ChatHistoryActivity.this);
            recycler_view.setLayoutManager(layoutManager);
            recycler_view.setAdapter(menuAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        KeyboardUtil.hideKeyboard(searchview,ChatHistoryActivity.this);
        isOppTypingRef.setValue(null);
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KeyboardUtil.hideKeyboard(searchview,ChatHistoryActivity.this);
        thread = null;
        blockThread = null;
    }
}
