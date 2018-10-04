package com.mualab.org.user.activity.chat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.image.cropper.CropImage;
import com.image.cropper.CropImageView;
import com.image.picker.ImagePicker;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.adapter.UserListAdapter;
import com.mualab.org.user.activity.chat.listner.OnUserClickListener;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.activity.chat.model.GroupMember;
import com.mualab.org.user.activity.chat.model.Groups;
import com.mualab.org.user.activity.chat.model.MyGroup;
import com.mualab.org.user.activity.story.camera.util.ImageUtil;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.constants.Constant;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener,
        OnUserClickListener {
    private EditText etGroupName, etGroupDescr;
    private TextView tv_no_chat;
    private ImageView ivGroupPic;
    private Bitmap groupImgBitmap;
    private ChildEventListener childEventListener;
    private List<FirebaseUser>userList;
    private Map<String,FirebaseUser> map;
    private ProgressBar progress_bar;
    private UserListAdapter userListAdapter;
    private String sProfileImgUrl = "",myGroupId,sGroupName="",sGroupDes,myUid;
    private HashMap<String,GroupMember>tempSelectedList;
    private DatabaseReference mFirebaseUserDbRef,mFirebaseGroupRef,myGroupRef,chatHitoryRef;
    private LinkedList<String>lastGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        mFirebaseGroupRef = FirebaseDatabase.getInstance().getReference().child("group");
        myGroupRef = FirebaseDatabase.getInstance().getReference().child("myGroup");
        mFirebaseUserDbRef = FirebaseDatabase.getInstance().getReference().child("users");
        chatHitoryRef = FirebaseDatabase.getInstance().getReference().child("chat_history");
        init();
    }

    private void init() {
        userList = new ArrayList<>();
        tempSelectedList = new HashMap<>();
        lastGroupName = new LinkedList<>();
        map = new HashMap<>();
        userListAdapter = new UserListAdapter(this,userList);
        myUid = String.valueOf(Mualab.currentUser.id);
        RecyclerView rycUserList = findViewById(R.id.rycUserList);

        userListAdapter.setListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(CreateGroupActivity.this,
                LinearLayoutManager.VERTICAL, false);
        rycUserList.setLayoutManager(layoutManager);
        rycUserList.setAdapter(userListAdapter);

        etGroupName = findViewById(R.id.etGroupName);
        etGroupDescr = findViewById(R.id.etGroupDescr);
        ivGroupPic = findViewById(R.id.ivGroupPic);
        progress_bar = findViewById(R.id.progress_bar);

        tv_no_chat = findViewById(R.id.tv_no_chat);

        AppCompatButton btnCreate = findViewById(R.id.btnCreate);
        RelativeLayout rlGroupImg = findViewById(R.id.rlGroupImg);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvChatTitle = findViewById(R.id.tvChatTitle);
        tvChatTitle.setText(getString(R.string.create_group));

        new Thread(new Runnable() {
            @Override
            public void run() {
                getGroupNodeInfo();
            }
        }).start();

        rlGroupImg.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        getUserList();
    }

    private void getUserList(){
        childEventListener = mFirebaseUserDbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    assert user != null;
                    if (user.uId!=Mualab.currentUser.id)
                        getDataInMap(dataSnapshot.getKey(),user);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    getDataInMap(dataSnapshot.getKey(),user);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                progress_bar.setVisibility(View.GONE);
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

    private void getDataInMap(String key, FirebaseUser user) {
        if (user != null) {
            user.isChecked = false;
            map.put(key, user);
            userList.clear();
            Collection<FirebaseUser> values = map.values();
            userList.addAll(values);
            userListAdapter.notifyDataSetChanged();
        }

        if (userList.size()==0) {
            progress_bar.setVisibility(View.GONE);
            tv_no_chat.setVisibility(View.VISIBLE);
        }else {
            progress_bar.setVisibility(View.GONE);
            tv_no_chat.setVisibility(View.GONE);
        }
    }

    private void getGroupNodeInfo() {
        lastGroupName.clear();
        mFirebaseGroupRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                lastGroupName.add(key);
                progress_bar.setVisibility(View.GONE);
                /*if (snapshot.hasChild(myUid)) {
                    progress_bar.setVisibility(View.GONE);
                }else {
                    progress_bar.setVisibility(View.GONE);
                    // tv_no_chat.setVisibility(View.VISIBLE);
                }*/
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.rlGroupImg:
                getPermissionAndPicImage();
                break;

            case R.id.btnCreate:
                sGroupName = etGroupName.getText().toString().trim();
                sGroupDes = etGroupDescr.getText().toString().trim();
                if (!sGroupName.isEmpty()){
                    if (!sGroupDes.isEmpty()){
                        if (sProfileImgUrl.equals(""))
                            sProfileImgUrl = "http://koobi.co.uk:3000/uploads/default_group.png";

                        if (tempSelectedList.size()!=0){
                            progress_bar.setVisibility(View.VISIBLE);

                            if (lastGroupName.size()!=0) {
                                String lGroupName = lastGroupName.getLast();
                                String[] namesList = lGroupName.split("_");
                                int groupId = Integer.parseInt(namesList[1]);
                                myGroupId = "group_" + (groupId + 1);
                            }else {
                                myGroupId = "group_1";
                            }

                            GroupMember groupMember = new GroupMember();
                            groupMember.createdDate = ServerValue.TIMESTAMP;
                            groupMember.firebaseToken = FirebaseInstanceId.getInstance().getToken();
                            groupMember.memberId = Mualab.currentUser.id;
                            groupMember.mute = 0;
                            groupMember.profilePic = Mualab.currentUser.profileImage;
                            groupMember.type = "admin";
                            groupMember.userName = Mualab.currentUser.userName;

                            tempSelectedList.put(myUid,groupMember);


                            Groups group = new Groups();
                            Map<String,Object> params = new HashMap<>();
                            for (Map.Entry<String,GroupMember> entry : tempSelectedList.entrySet()) {
                                GroupMember groupMemberVal = tempSelectedList.get(entry.getKey());
                                try {
                                    // JSONObject jsonObject = new JSONObject();
                                    Map<String,Object> jsonObject = new HashMap<>();
                                    jsonObject.put("createdDate", groupMemberVal.createdDate);
                                    jsonObject.put("firebaseToken", groupMemberVal.firebaseToken);
                                    jsonObject.put("memberId", groupMemberVal.memberId);
                                    jsonObject.put("mute", groupMemberVal.mute);
                                    jsonObject.put("profilePic", groupMemberVal.profilePic);
                                    jsonObject.put("type", groupMemberVal.type);
                                    jsonObject.put("userName", groupMemberVal.userName);

                                    params.put(String.valueOf(groupMemberVal.memberId),jsonObject);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            // group.member = new JSONObject(params);
                            group.member.putAll(params);
                            group.adminId = Integer.parseInt(myUid);
                            group.banAdmin = 0;
                            group.freezeGroup = 0;
                            group.groupDescription = sGroupDes;
                            group.groupImg = sProfileImgUrl;
                            group.groupName = sGroupName;

                            mFirebaseGroupRef.child(myGroupId).setValue(group);
                            updateChatHistory();
                        }else {
                            MyToast.getInstance(CreateGroupActivity.this).showDasuAlert("Please select atleast one group member");
                        }

                    }else {
                        MyToast.getInstance(CreateGroupActivity.this).showDasuAlert("Please enter group description");
                    }
                }else {
                    MyToast.getInstance(CreateGroupActivity.this).showDasuAlert("Please enter group name");
                }
                break;
        }

    }

    private void updateChatHistory(){
        for (Map.Entry<String,GroupMember> entry : tempSelectedList.entrySet()) {
            GroupMember groupMemberVal = tempSelectedList.get(entry.getKey());
            try {
                MyGroup myGroup = new MyGroup();
                myGroup.groupName = myGroupId;
                myGroupRef.child(String.valueOf(groupMemberVal.memberId)).setValue(myGroup);

                ChatHistory chatHistory = new ChatHistory();
                chatHistory.favourite = 0;
                chatHistory.memberCount = tempSelectedList.size();
                chatHistory.memberType = "member";
                chatHistory.message = "";
                chatHistory.messageType = 0;
                chatHistory.profilePic = sProfileImgUrl;
                chatHistory.reciverId = myGroupId;
                chatHistory.senderId = myUid;
                chatHistory.type = "group";
                chatHistory.unreadMessage = 0;
                chatHistory.userName = sGroupName;
                chatHistory.timestamp = ServerValue.TIMESTAMP;

             /*   ChatHistory myChatHistory = new ChatHistory();
                myChatHistory.favourite = 0;
                myChatHistory.memberCount = tempSelectedList.size();
                myChatHistory.memberType = "admin";
                myChatHistory.message = "";
                myChatHistory.messageType = 0;
                myChatHistory.profilePic = sProfileImgUrl;
                myChatHistory.reciverId = myGroupId;
                myChatHistory.senderId = String.valueOf(myUid);
                myChatHistory.type = "group";
                myChatHistory.unreadMessage = 0;
                myChatHistory.userName = sGroupName;
                myChatHistory.timestamp = ServerValue.TIMESTAMP;
*/
                chatHitoryRef.child(String.valueOf(groupMemberVal.memberId)).
                        child(myGroupId).setValue(chatHistory);

                chatHistory.memberType = "admin";

                chatHitoryRef.child(myUid).
                        child(myGroupId).setValue(chatHistory);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        progress_bar.setVisibility(View.GONE);
        finish();
    }

    public void getPermissionAndPicImage() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) +
                    checkSelfPermission(android.Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY);

            } else {
                ImagePicker.pickImage(this);
            }

        } else {
            ImagePicker.pickImage(this);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case Constant.MY_PERMISSIONS_REQUEST_CEMERA_OR_GALLERY: {
                boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                if (cameraPermission && readExternalFile) {
                    ImagePicker.pickImage(CreateGroupActivity.this);
                } else
                    MyToast.getInstance(CreateGroupActivity.this).showDasuAlert("Your  Permission Denied");
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);
                Uri imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data);
                if (imageUri != null) {
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(400, 400).start(this);
                } else {
                    MyToast.getInstance(this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                try {
                    if (result != null)
                        groupImgBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());

                    if (groupImgBitmap != null) {
                        Uri imageUri = ImageUtil.getImageUri(CreateGroupActivity.this,
                                groupImgBitmap);
                        ivGroupPic.setImageBitmap(groupImgBitmap);
                        uploadImage(imageUri);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (imageUri != null) {
            Progress.show(CreateGroupActivity.this);
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

                            sProfileImgUrl = fireBaseUri.toString();

                            Progress.hide(CreateGroupActivity.this);
                            //  tv_no_chat.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //  progressDialog.dismiss();
                            Progress.hide(CreateGroupActivity.this);
                            Toast.makeText(CreateGroupActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabase.getInstance().getReference().removeEventListener(childEventListener);

    }

    @Override
    public void onUserClicked(FirebaseUser user, int position) {
        GroupMember groupMember = new GroupMember();
        groupMember.createdDate = ServerValue.TIMESTAMP;
        groupMember.firebaseToken = user.firebaseToken;
        groupMember.memberId = user.uId;
        groupMember.mute = 0;
        groupMember.profilePic = user.profilePic;
        groupMember.type = "member";
        groupMember.userName = user.userName;

        if (user.isChecked) {
            tempSelectedList.put(String.valueOf(user.uId),groupMember);
        }
        else {
            tempSelectedList.remove(String.valueOf(user.uId));
        }
    }
}
