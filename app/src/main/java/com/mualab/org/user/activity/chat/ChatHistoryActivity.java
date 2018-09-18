package com.mualab.org.user.activity.chat;

import android.content.Context;
import android.graphics.Point;
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
import com.mualab.org.user.activity.chat.model.Typing;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.utils.CommonUtils;
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
    private DatabaseReference databaseReference,isOppTypingRef,blockUsersRef;
    private Thread thread,blockThread;
    private String myId;
    private ImageView ic_add_chat,ivFavChat;
    private long mLastClickTime = 0;
    private boolean isFavFilter = false;

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

        init();
    }

    private void init(){
        newList = new ArrayList<>();
        chatHistories = new ArrayList<>();
        listmap = new HashMap<>();
        blockUsersMap = new HashMap<>();

        historyAdapter = new ChatHistoryAdapter(ChatHistoryActivity.this,chatHistories);

        tv_no_chat = findViewById(R.id.tv_no_chat);
        progress_bar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btnBack);
        ivFavChat = findViewById(R.id.ivFavChat);
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

    }

    private void getTyppingUser(){

        final String myChild = myId+"_";

        isOppTypingRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println("typping===");

                if (!dataSnapshot.getKey().contains(myChild)){

                    if (!dataSnapshot.getKey().contains("broadcast_") ||
                            !dataSnapshot.getKey().contains("broadcast_group")){

                        final Typing typing = dataSnapshot.getValue(Typing.class);
                        String node = dataSnapshot.getKey();

                        assert typing != null;
                        String blockNode = typing.senderId+"_"+myId;
                        if (!blockUsersMap.containsKey(blockNode)){
                            if (node.contains(myId)){
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (chatHistories.size()!=0 ){
                                            for (int i=0;i<chatHistories.size();i++){
                                                ChatHistory chatHistory = chatHistories.get(i);
                                                if (typing.senderId.equals(chatHistory.senderId) || typing.senderId.
                                                        equals(chatHistory.reciverId)){
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
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                if (!dataSnapshot.getKey().contains(myChild)){

                    if (!dataSnapshot.getKey().contains("broadcast_") ||
                            !dataSnapshot.getKey().contains("broadcast_group")){

                        final Typing typing = dataSnapshot.getValue(Typing.class);
                        String node = dataSnapshot.getKey();

                        assert typing != null;
                        String blockNode = typing.senderId+"_"+myId;

                        if (!blockUsersMap.containsKey(blockNode)){
                            if (node.contains(myId)){
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (chatHistories.size()!=0 ){
                                            for (int i=0;i<chatHistories.size();i++){
                                                ChatHistory chatHistory = chatHistories.get(i);
                                                if (typing.senderId.equals(chatHistory.senderId) || typing.senderId.
                                                        equals(chatHistory.reciverId)){
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

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // thread.interrupt();
                Typing typing = dataSnapshot.getValue(Typing.class);
                if (dataSnapshot.exists()){
                    assert typing != null;
                    if (listmap.containsKey(typing.senderId)&& chatHistories.size()!=0){
                        for (int i=0;i<chatHistories.size();i++){
                            ChatHistory chatHistory = chatHistories.get(i);
                            if (typing.senderId.equals(chatHistory.senderId) || typing.senderId.
                                    equals(chatHistory.reciverId)){
                                historyAdapter.setTyping(false,i);
                                break;
                            }
                        }
                    }
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

                if(dataSnapshot.getValue()== null){
                    if (!dataSnapshot.exists()){
                        tv_no_chat.setVisibility(View.VISIBLE);
                    }else{
                        tv_no_chat.setVisibility(View.GONE);
                    }
                    progress_bar.setVisibility(View.GONE);
                    tv_no_chat.setVisibility(View.VISIBLE);
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

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
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

    private void getChatDataInMap(String key, ChatHistory messageOutput) {
        if (messageOutput != null) {
            if (messageOutput.type.equals("user")){
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

        historyAdapter.setTyping(false);

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

            case R.id.ic_add_chat:
                KeyboardUtil.hideKeyboard(searchview,ChatHistoryActivity.this);
                //rlOptionMenu.setVisibility(View.VISIBLE);

                int[] location = new int[2];

                // Get the x, y location and store it in the location[] array
                // location[0] = x, location[1] = y.
                ic_add_chat.getLocationOnScreen(location);

                //Initialize the Point with x, and y positions
                Display display = getWindowManager().getDefaultDisplay();
                Point p = new Point();
                display.getSize(p);
                p.x = location[0];
                p.y = location[1];

                initiatePopupWindow(p);

                break;
        }
    }

    private void favouriteFilter(){
        if (isFavFilter){
            isFavFilter = false;
            historyAdapter.filterList(chatHistories);
            ivFavChat.setImageResource(R.drawable.star_ico);
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

    private void initiatePopupWindow(Point p) {

        try {
            LayoutInflater inflater = (LayoutInflater) ChatHistoryActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View layout = inflater.inflate(R.layout.layout_popup_menu,(ViewGroup) findViewById(R.id.parent));
            PopupWindow popupWindow = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    true);
            int OFFSET_X = 500;
            int OFFSET_Y = 110;
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
                    switch (data){
                        case "Create new chat":

                            break;
                        case "Create new group":
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
