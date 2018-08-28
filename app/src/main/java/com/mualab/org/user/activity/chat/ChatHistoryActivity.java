package com.mualab.org.user.activity.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
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
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView tv_no_chat;
    private ProgressBar progress_bar;
    private RecyclerView rvChatHistory;
    private ChatHistoryAdapter historyAdapter;
    private SearchView searchview;
    private List<ChatHistory> chatHistories;
    private List<ChatHistory> newList;
    private Map<String, ChatHistory> listmap;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chat_history").
                child(String.valueOf(Mualab.currentUser.id));

        init();
    }

    private void init(){
        newList = new ArrayList<>();
        chatHistories = new ArrayList<>();
        listmap = new HashMap<>();

        historyAdapter = new ChatHistoryAdapter(ChatHistoryActivity.this,chatHistories);

        tv_no_chat = findViewById(R.id.tv_no_chat);
        progress_bar = findViewById(R.id.progress_bar);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ic_add_chat = findViewById(R.id.ic_add_chat);
        ImageView ivChatReq = findViewById(R.id.ivChatReq);
        ic_add_chat.setVisibility(View.VISIBLE);
        ivChatReq.setVisibility(View.VISIBLE);
        rvChatHistory = findViewById(R.id.rvChatHistory);
        rvChatHistory.setAdapter(historyAdapter);
        historyAdapter.notifyDataSetChanged();

        searchview = findViewById(R.id.searchview);
        KeyboardUtil.hideKeyboard(searchview, ChatHistoryActivity.this);

        btnBack.setOnClickListener(this);

        getHistoryList();

    }

    private void getHistoryList() {

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()== null){
                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        chatHistories.clear();
        newList.clear();

        databaseReference.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);

                if (messageOutput != null) {

                    if (messageOutput.type.equals("user")){
                        listmap.put(dataSnapshot.getKey(), messageOutput);
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

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                ChatHistory messageOutput = dataSnapshot.getValue(ChatHistory.class);
                // map.put(dataSnapshot.getKey(),messageOutput);

                if (messageOutput != null) {
                    listmap.remove(dataSnapshot.getKey());
                    Collection<ChatHistory> demoValues = listmap.values();
                    newList.clear();
                    chatHistories.clear();
                    newList.addAll(demoValues);
                    chatHistories.addAll(newList);
                    shorting();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
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

    public void shorting() {

        Collections.sort(newList, new Comparator<ChatHistory>() {
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
        progress_bar.setVisibility(View.GONE);
        historyAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
        }
    }
}
