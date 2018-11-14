package com.mualab.org.user.activity.chat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.adapter.AddNewMemberListAdapter;
import com.mualab.org.user.activity.chat.adapter.SelectedMemberListAdapter;
import com.mualab.org.user.activity.chat.listner.OnCancleMemberClickListener;
import com.mualab.org.user.activity.chat.listner.OnUserClickListener;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.activity.chat.model.GroupMember;
import com.mualab.org.user.activity.chat.model.Groups;
import com.mualab.org.user.activity.chat.model.WebNotification;
import com.mualab.org.user.activity.chat.notification_builder.FcmNotificationBuilder;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddMemberActivity extends AppCompatActivity implements View.OnClickListener,
        OnUserClickListener,OnCancleMemberClickListener {
    private List<FirebaseUser>groupMembers;
    private List<GroupMember>selectedMemberList;
    private AddNewMemberListAdapter membersListAdapter;
    private SelectedMemberListAdapter selectedMemberListAdapter;
    private Map<String,FirebaseUser> map;
    private String myUid,groupId;
    private ProgressBar progress_bar;
    private DatabaseReference mFirebaseUserDbRef,mFirebaseGroupRef,chatHitoryRef,myGroupRef,
            chatRefWebnotif,groupMsgDeleteRef;
    private SearchView searchview;
    private RecyclerView rvSelectedMember;
    private Groups groups;
    private TextView tv_no_chat;
    private View vSaperater;
    private List<String>fbTokenListForMobile,fbTokenListForWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_member);
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
        // action = intent.getStringExtra("action");
        myUid = String.valueOf(Mualab.currentUser.id);

        mFirebaseGroupRef = FirebaseDatabase.getInstance().getReference().child("group")
                .child(groupId);
        mFirebaseUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
        chatHitoryRef = FirebaseDatabase.getInstance().getReference().child("chat_history");
        myGroupRef = FirebaseDatabase.getInstance().getReference().child("myGroup");
        chatRefWebnotif = FirebaseDatabase.getInstance().getReference().child("webnotification");
        groupMsgDeleteRef = FirebaseDatabase.getInstance().getReference().child("group_msg_delete");

        init();
    }

    private void init() {
        groupMembers = new ArrayList<>();
        selectedMemberList = new ArrayList<>();
        map = new HashMap<>();
        fbTokenListForMobile = new ArrayList<>();
        fbTokenListForWeb = new ArrayList<>();

        membersListAdapter = new AddNewMemberListAdapter(this,groupMembers);
        selectedMemberListAdapter = new SelectedMemberListAdapter(this,selectedMemberList);
        membersListAdapter.setListener(this);
        selectedMemberListAdapter.setListener(this);

        RecyclerView rvMembers = findViewById(R.id.rvMembers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(AddMemberActivity.this,
                LinearLayoutManager.VERTICAL, false);
        rvMembers.setLayoutManager(layoutManager);
        rvMembers.setAdapter(membersListAdapter);

        vSaperater = findViewById(R.id.vSaperater);
        rvSelectedMember = findViewById(R.id.rvSelectedMember);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(AddMemberActivity.this,
                LinearLayoutManager.HORIZONTAL, false);
        rvSelectedMember.setLayoutManager(layoutManager2);
        rvSelectedMember.setAdapter(selectedMemberListAdapter);

        progress_bar = findViewById(R.id.progress_bar);
        tv_no_chat = findViewById(R.id.tv_no_chat);

        AppCompatButton btnAddMem = findViewById(R.id.btnAddMem);
        AppCompatButton btnRemoveMem = findViewById(R.id.btnRemoveMem);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvChatTitle = findViewById(R.id.tvChatTitle);

        tvChatTitle.setText("Add New Members");
        btnRemoveMem.setVisibility(View.GONE);
        btnAddMem.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
            }
        },2000);

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(AddMemberActivity.this, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        getUserListForAdd();
                        //getting group data from group table
                        new Thread(new Runnable(){
                            @Override
                            public void run(){
                                getGroupDetail();
                            }
                        }).start();
                    }
                }
            }).show();
        }else {
            //getting group data from group table
            getUserListForAdd();
            //getting group data from group table
            new Thread(new Runnable(){
                @Override
                public void run(){
                    getGroupDetail();
                }
            }).start();
        }
        searchview = findViewById(R.id.searchview);
        KeyboardUtil.hideKeyboard(searchview, AddMemberActivity.this);
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

        btnAddMem.setOnClickListener(this);
        btnBack.setOnClickListener(this);

    }

    private void filter(String text) {
        List<FirebaseUser> filterdNames = new ArrayList<>();

        for (FirebaseUser s : groupMembers) {
            if (s.userName.toLowerCase(Locale.getDefault()).contains(text)) {
                filterdNames.add(s);
            }
        }

      /*  if (filterdNames.size()==0)
            tv_no_chat.setVisibility(View.VISIBLE);
        else
            tv_no_chat.setVisibility(View.GONE);*/
        membersListAdapter.filterList(filterdNames);


    }

    private void getUserListForAdd(){
        mFirebaseUserDbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    final FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    assert user != null;
                    if (user.uId!=Mualab.currentUser.id) {
                        mFirebaseGroupRef.child("member").child(String.valueOf(user.uId)).
                                addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String key = dataSnapshot.getKey();
                                        if (dataSnapshot.getValue() == null || !dataSnapshot.exists()) {
                                            try {
                                                getDataInMap(dataSnapshot.getKey(), user);

                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }else {
                                            if (groupMembers.size()==0)
                                                tv_no_chat.setVisibility(View.VISIBLE);
                                            else
                                                tv_no_chat.setVisibility(View.GONE);

                                            progress_bar.setVisibility(View.GONE);
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progress_bar.setVisibility(View.GONE);

                                    }
                                });

                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    final FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    assert user != null;
                    if (user.uId!=Mualab.currentUser.id) {
                        mFirebaseGroupRef.child("member").child(String.valueOf(user.uId)).
                                addListenerForSingleValueEvent(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String key = dataSnapshot.getKey();
                                        if (dataSnapshot.getValue() == null || !dataSnapshot.exists()) {
                                            try {
                                                getDataInMap(dataSnapshot.getKey(), user);

                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }

                                        }
                                        progress_bar.setVisibility(View.GONE);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progress_bar.setVisibility(View.GONE);
                                    }
                                });

                    }
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

    private void getGroupDetail(){
        mFirebaseGroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        groups = dataSnapshot.getValue(Groups.class);

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getDataInMap(String key, FirebaseUser user) {
        if (user != null) {
            user.isChecked = false;
            map.put(key, user);
            groupMembers.clear();
            Collection<FirebaseUser> values = map.values();
            groupMembers.addAll(values);
            membersListAdapter.notifyDataSetChanged();

            if (groupMembers.size()==0)
                tv_no_chat.setVisibility(View.VISIBLE);
            else
                tv_no_chat.setVisibility(View.GONE);

            progress_bar.setVisibility(View.GONE);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btnAddMem:

                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(AddMemberActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                                addMembers();
                            }
                        }
                    }).show();
                }else {
                    addMembers();
                }
                break;
        }
    }

    private void addMembers(){
        if (selectedMemberList.size()!=0){
            for (int i=0;i<selectedMemberList.size();i++){
                final GroupMember groupMemberVal = selectedMemberList.get(i);

                mFirebaseGroupRef.child("member").child(String.valueOf(groupMemberVal.
                        memberId)).setValue(groupMemberVal);

                myGroupRef.child(String.valueOf(groupMemberVal.memberId)).child(groupId).
                        setValue(groupId);

                groupMsgDeleteRef.child(String.valueOf(groupMemberVal.memberId)).
                        child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() || dataSnapshot.getValue()!=null){
                            groupMsgDeleteRef.child(String.valueOf(groupMemberVal.memberId)).
                                    child(groupId).setValue(null);

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                ChatHistory chatHistory = new ChatHistory();
                chatHistory.favourite = 0;
                chatHistory.memberCount = groups.member.size()+selectedMemberList.size();
                chatHistory.memberType = "member";
                chatHistory.message = "";
                chatHistory.messageType = 0;
                chatHistory.profilePic = groups.groupImg;
                chatHistory.reciverId = groupId;
                chatHistory.senderId = myUid;
                chatHistory.type = "group";
                chatHistory.unreadMessage = 0;
                chatHistory.userName = groups.groupName;
                chatHistory.timestamp = ServerValue.TIMESTAMP;

                chatHitoryRef.child(String.valueOf(groupMemberVal.memberId)).
                        child(groupId).setValue(chatHistory);

                fbTokenListForMobile.add(groupMemberVal.firebaseToken);
                fbTokenListForWeb.add(groupMemberVal.firebaseToken);
            }
            sendPushNotificationToReceiver();
        }else {
            MyToast.getInstance(AddMemberActivity.this).showDasuAlert("Please select a group member");
        }
    }

    private void sendPushNotificationToReceiver() {

        for (int i=0;i<fbTokenListForWeb.size();i++) {

            WebNotification webNotification = new WebNotification();
            webNotification.body = Mualab.currentUser.userName+" added you";
            webNotification.title = Mualab.currentUser.userName;
            webNotification.url = "";
            chatRefWebnotif.child(fbTokenListForWeb.get(i)).push().setValue(webNotification);
        }

        FcmNotificationBuilder.initialize()
                .title(Mualab.currentUser.userName+" @ "+groups.groupName)
                .message(Mualab.currentUser.userName+" added you")
                .uid(groupId)
                .username(Mualab.currentUser.userName+" @ "+groups.groupName).
                adminId(String.valueOf(groups.adminId))
                .type("groupChat").clickAction("GroupChatActivity")
                .registrationId(fbTokenListForMobile).send();

        finish();

    }

    @Override
    public void onUserClicked(FirebaseUser user, int position) {
        if (selectedMemberList.size()!=0){
            if(!user.isChecked) {
                for (int i=0;i<selectedMemberList.size();i++){
                    GroupMember groupMember = selectedMemberList.get(i);
                    if (user.uId==groupMember.memberId){
                        selectedMemberList.remove(i);
                        break;
                    }
                }
            }else {
                GroupMember groupMember = new GroupMember();
                groupMember.createdDate = ServerValue.TIMESTAMP;
                groupMember.firebaseToken = user.firebaseToken;
                groupMember.memberId = user.uId;
                groupMember.mute = 0;
                groupMember.profilePic = user.profilePic;
                groupMember.type = "member";
                groupMember.userName = user.userName;
                selectedMemberList.add(groupMember);
            }
        }else {
            GroupMember groupMember = new GroupMember();
            groupMember.createdDate = ServerValue.TIMESTAMP;
            groupMember.firebaseToken = user.firebaseToken;
            groupMember.memberId = user.uId;
            groupMember.mute = 0;
            groupMember.profilePic = user.profilePic;
            groupMember.type = "member";
            groupMember.userName = user.userName;
            selectedMemberList.add(groupMember);
        }

        if (selectedMemberList.size()==0) {
            rvSelectedMember.setVisibility(View.GONE);
            vSaperater.setVisibility(View.GONE);
        }
        else {
            rvSelectedMember.setVisibility(View.VISIBLE);
            vSaperater.setVisibility(View.VISIBLE);
        }

        selectedMemberListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMemberClicked(GroupMember user, int position) {
        if (groupMembers.size()!=0){
            for (int i=0;i<groupMembers.size();i++){
                FirebaseUser firebaseUser = groupMembers.get(i);
                if (firebaseUser.uId==user.memberId){
                    firebaseUser.isChecked = false;
                    membersListAdapter.notifyItemChanged(i);
                    break;
                }
            }
            if (selectedMemberList.size()==0)
                vSaperater.setVisibility(View.GONE);
            else
                vSaperater.setVisibility(View.VISIBLE);
        }
    }
}
