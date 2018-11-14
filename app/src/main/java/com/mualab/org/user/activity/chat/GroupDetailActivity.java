package com.mualab.org.user.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.mualab.org.user.activity.chat.adapter.GroupMembersListAdapter;
import com.mualab.org.user.activity.chat.model.GroupMember;
import com.mualab.org.user.activity.chat.model.Groups;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.utils.KeyboardUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GroupDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView ivMuteToggle,ivGroupImage;
    private TextView tvGroupName,tvGroupDesc,tvGroupMembers,tvMuteGroup;
    private boolean isMuteClicked = false;
    private DatabaseReference groupRef;
    private String myUid;
    private Groups groups;
    private int isMute;
    private  ArrayList<GroupMember> userList;
    private long mLastClickTime = 0;
    private ProgressBar progress_bar;
    private GroupMembersListAdapter userListAdapter;
    private Map<String,GroupMember> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        Intent intent = getIntent();
        String groupId = intent.getStringExtra("groupId");
        myUid = String.valueOf(Mualab.currentUser.id);

        groupRef = FirebaseDatabase.getInstance().getReference().child("group").child(groupId);

        initView();
    }

    private void initView(){
        userList = new ArrayList<>();
        map = new HashMap<>();
        userListAdapter = new GroupMembersListAdapter(GroupDetailActivity.this,userList);
        ivGroupImage = findViewById(R.id.ivGroupImage);
        ivMuteToggle = findViewById(R.id.ivMuteToggle);
        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupDesc = findViewById(R.id.tvGroupDesc);
        tvMuteGroup = findViewById(R.id.tvMuteGroup);
        tvGroupMembers = findViewById(R.id.tvGroupMembers);
        RecyclerView rycGroupMembers = findViewById(R.id.rycGroupMembers);
        progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);
        ImageView btnBack = findViewById(R.id.btnBack);
        rycGroupMembers.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(GroupDetailActivity.this,
                LinearLayoutManager.VERTICAL, false);
        rycGroupMembers.setLayoutManager(layoutManager);
        rycGroupMembers.setAdapter(userListAdapter);

        //   userListAdapter = new UsersListAdapter(this,userList);
        ivMuteToggle.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progress_bar.setVisibility(View.GONE);
            }
        },4000);

        getGroupDetail();
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()){
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.ivMuteToggle:
                if (isMuteClicked) {
                    isMuteClicked = false;
                    ivMuteToggle.setImageResource(R.drawable.ic_switch_off);
                    groupRef.child("member").child(myUid).child("mute").
                            setValue(0);
                } else {
                    ivMuteToggle.setImageResource(R.drawable.ic_switch_on);
                    isMuteClicked = true;
                    groupRef.child("member").child(myUid).child("mute").
                            setValue(1);
                }
                break;

        }
    }

    private void getGroupDetail(){
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        groups = dataSnapshot.getValue(Groups.class);
                        if (groups != null) {
                            tvGroupName.setText(groups.groupName);

                            if (groups.groupImg !=null) {
                                Picasso.with(GroupDetailActivity.this).load(groups.groupImg).
                                        placeholder(R.drawable.gallery_placeholder).fit().into(ivGroupImage);
                            }
                            // GroupMember member = (GroupMember) groups.member.get(myUid);
                            // isMute = member.mute;
                            Map<String,GroupMember> hashMap = (Map<String, GroupMember>)
                                    groups.member.get(myUid);

                            if (hashMap!=null){
                                isMute = Integer.valueOf(String.valueOf(hashMap.get("mute")));
                                if (isMute==1) {
                                    // tvMuteGroup.setText("Unmute Group");
                                    ivMuteToggle.setImageResource(R.drawable.ic_switch_on);
                                    isMuteClicked = true;
                                    groupRef.child("member").child(myUid).child("mute").
                                            setValue(1);
                                } else {
                                    isMuteClicked = false;
                                    //   tvMuteGroup.setText(getString(R.string.mute_group));
                                    ivMuteToggle.setImageResource(R.drawable.ic_switch_off);
                                    groupRef.child("member").child(myUid).child("mute").
                                            setValue(0);
                                }
                            }

                            tvGroupMembers.setText(groups.member.size()+" Members");
                            tvGroupDesc.setText(groups.groupDescription);

                            getMemberList();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        progress_bar.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress_bar.setVisibility(View.GONE);
            }
        });
    }

    private void getMemberList(){
        groupRef.child("member").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null) {
                    GroupMember member = dataSnapshot.getValue(GroupMember.class);
                    getDataInMap(dataSnapshot.getKey(),member);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot!=null) {
                    GroupMember member = dataSnapshot.getValue(GroupMember.class);
                    getDataInMap(dataSnapshot.getKey(),member);

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GroupMember member = dataSnapshot.getValue(GroupMember.class);
                assert member != null;
                if (member.memberId == Mualab.currentUser.id)
                    finish();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getDataInMap(String key, GroupMember user) {
        if (user != null) {
            user.isChecked = false;
            map.put(key, user);
            userList.clear();
            Collection<GroupMember> values = map.values();
            userList.addAll(values);
        }

        shortList();
        progress_bar.setVisibility(View.GONE);
    }

    private void shortList() {
        Collections.sort(userList, new Comparator<GroupMember>() {

            @Override
            public int compare(GroupMember a1, GroupMember a2) {

                if (a1.memberId == 0 || a2.memberId == 0)
                    return -1;
                else {
                    Long long1 = Long.valueOf(String.valueOf(a1.memberId));
                    Long long2 = Long.valueOf(String.valueOf(a2.memberId));

                    if (a1.memberId==Mualab.currentUser.id && !a1.type.equals("admin")) {
                        //   int itemPos = userList.indexOf(user);
                        userList.remove(a1);
                        userList.add(0,a1);
                        return a1.memberId;
                    }
                    else
                        return long1.compareTo(long2);
                }

               /* if (a1.memberId==Mualab.currentUser.id || a2.memberId==Mualab.currentUser.id) {
                    int itemPos = userList.indexOf(user);
                    userList.remove(user);
                    userList.add(0,user);
                }*/
            }
        });
        userListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
