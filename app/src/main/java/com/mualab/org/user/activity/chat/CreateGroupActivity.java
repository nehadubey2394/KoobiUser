package com.mualab.org.user.activity.chat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.image.cropper.CropImage;
import com.image.cropper.CropImageView;
import com.image.picker.ImagePicker;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.adapter.UsersListAdapter;
import com.mualab.org.user.activity.chat.listner.OnUserClickListener;
import com.mualab.org.user.activity.chat.model.ChatHistory;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.activity.chat.model.GroupMember;
import com.mualab.org.user.activity.chat.model.Groups;
import com.mualab.org.user.activity.story.camera.util.ImageUtil;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.KeyboardUtil;
import com.mualab.org.user.utils.constants.Constant;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener,
        OnUserClickListener {
    private EditText etGroupName, etGroupDescr;
    private TextView tv_no_chat;
    private ImageView ivGroupPic;
    private ChildEventListener childEventListener;
    private List<FirebaseUser>userList;
    private Map<String,FirebaseUser> map;
    private ProgressBar progress_bar;
    private UsersListAdapter userListAdapter;
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
        userListAdapter = new UsersListAdapter(this,userList);
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

        LinearLayout llGroupInfo = findViewById(R.id.llGroupInfo);

        tv_no_chat = findViewById(R.id.tv_no_chat);

        AppCompatButton btnCreate = findViewById(R.id.btnCreate);
        RelativeLayout rlGroupImg = findViewById(R.id.rlGroupImg);
        ImageView btnBack = findViewById(R.id.btnBack);
        TextView tvChatTitle = findViewById(R.id.tvChatTitle);
        tvChatTitle.setText(getString(R.string.create_group));

        new Thread(new Runnable() {
            @Override
            public void run() {
                getUserList();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                getGroupNodeInfo();
            }
        }).start();

        rlGroupImg.setOnClickListener(this);
        btnCreate.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        llGroupInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtil.hideSoftKeyboard(CreateGroupActivity.this);
                return false;
            }
        });
    }

    private void getUserList(){
        childEventListener = mFirebaseUserDbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    assert user != null;
                    if (user.uId!=Mualab.currentUser.id) {
                        getDataInMap(dataSnapshot.getKey(), user);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    FirebaseUser user = dataSnapshot.getValue(FirebaseUser.class);
                    if (user.uId!=Mualab.currentUser.id) {
                        getDataInMap(dataSnapshot.getKey(), user);
                    }
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

                if (!ConnectionDetector.isConnected()) {
                    new NoConnectionDialog(CreateGroupActivity.this, new NoConnectionDialog.Listner() {
                        @Override
                        public void onNetworkChange(Dialog dialog, boolean isConnected) {
                            if(isConnected){
                                dialog.dismiss();
                                createGroup();
                            }
                        }
                    }).show();
                }else {
                    createGroup();
                }
                break;
        }

    }

    private void createGroup(){
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
                    MyToast.getInstance(CreateGroupActivity.this).showDasuAlert("Please select group member");
                }

            }else {
                MyToast.getInstance(CreateGroupActivity.this).showDasuAlert("Please enter group description");
            }
        }else {
            MyToast.getInstance(CreateGroupActivity.this).showDasuAlert("Please enter group name");
        }
    }

    private void updateChatHistory(){
        for (Map.Entry<String,GroupMember> entry : tempSelectedList.entrySet()) {
            GroupMember groupMemberVal = tempSelectedList.get(entry.getKey());
            try {
                // MyGroup myGroup = new MyGroup();
                //myGroup.groupName = myGroupId;
                myGroupRef.child(String.valueOf(groupMemberVal.memberId)).child(myGroupId).
                        setValue(myGroupId);
                //   myGroupRef.child(myUid).setValue(myGroup);

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

                chatHitoryRef.child(String.valueOf(groupMemberVal.memberId)).
                        child(myGroupId).setValue(chatHistory);


                chatHistory.memberType = "admin";

                chatHitoryRef.child(myUid).child(myGroupId).setValue(chatHistory);


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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 234) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Uri imageUri = ImagePicker.getImageURIFromResult(CreateGroupActivity.this, requestCode, resultCode, data);
                        if (imageUri != null) {
                            DisplayMetrics displayMetrics = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                            int width = displayMetrics.widthPixels;
                            int height = (int) (width*0.74);
                            CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE).
                                    setAspectRatio(width, height).start(CreateGroupActivity.this);
                        } else {
                            MyToast.getInstance(CreateGroupActivity.this).showDasuAlert(getString(R.string.msg_some_thing_went_wrong));
                        }
                    }
                }).start();
                //Bitmap bitmap = ImagePicker.getImageFromResult(this, requestCode, resultCode, data);


            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                try {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (result != null) {
                        Uri imgUri = result.getUri();

                        String  mystring =compressImage(imgUri.toString());
                        ivGroupPic.setImageURI(Uri.parse(mystring));

                        uploadImage(imgUri);
                    }

                } catch (Exception  | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(

                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private void uploadImage(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        if (imageUri != null) {
            Progress.show(CreateGroupActivity.this);

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
                    });
        }
    }

    @Override
    public void onBackPressed() {
        KeyboardUtil.hideKeyboard(etGroupDescr,CreateGroupActivity.this);
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
