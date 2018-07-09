package com.mualab.org.user.activity.people_tag.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.people_tag.adapters.PeopleAdapter;
import com.mualab.org.user.activity.people_tag.instatag.InstaTag;
import com.mualab.org.user.activity.people_tag.instatag.TagImageView;
import com.mualab.org.user.activity.people_tag.interfaces.SomeOneClickListener;
import com.mualab.org.user.activity.people_tag.models.SomeOne;
import com.mualab.org.user.activity.people_tag.models.TaggedPhoto;
import com.mualab.org.user.activity.people_tag.utilities.CommonUtil;
import com.mualab.org.user.activity.people_tag.utilities.SomeOneData;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.listner.RecyclerViewScrollListener;
import com.mualab.org.user.utils.KeyboardUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoTagActivity extends AppCompatActivity implements View.OnClickListener,SomeOneClickListener {
    private InstaTag mInstaTag;
    private Uri mPhotoToBeTaggedUri;
    private RecyclerView mRecyclerViewSomeOneToBeTagged;
    private LinearLayout mHeaderSomeOneToBeTagged, mHeaderSearchSomeOne;
    // private TextView mTapPhotoToTagSomeOneTextView;
    private int mAddTagInX, mAddTagInY;
    //private EditText mEditSearchForSomeOne;
    private SearchView searchview;
    private PeopleAdapter mSomeOneAdapter;
    private final ArrayList<SomeOne> mSomeOnes = new ArrayList<>();
    private List<String> images;
    private int startIndex;
    private List<ExSearchTag> list;
    private LinearLayout ll_loadingBox;
    private ProgressBar progress_bar;
    private TextView tv_msg;
    private RecyclerViewScrollListener endlesScrollListener;
    private String searchKeyword = "";


    private RequestOptions requestOptions =
            new RequestOptions().placeholder(0)
                    .fallback(0).centerCrop()
                    .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_tag);

        list = new ArrayList<>();

        Intent intent = getIntent();
        if (intent != null) {
            startIndex = intent.getIntExtra("startIndex", 0);
            // feedImages = (ArrayList<Feeds>) getIntent().getSerializableExtra("imageArray");
            images = (ArrayList<String>) getIntent().getSerializableExtra("imageArray");
        }

        //  mPhotoToBeTaggedUri = getIntent().getData();
        mPhotoToBeTaggedUri = Uri.parse(String.valueOf(images.get(0)));

        ll_loadingBox = findViewById(R.id.ll_loadingBox);
        progress_bar = findViewById(R.id.progress_bar);
        tv_msg = findViewById(R.id.tv_msg);


        mInstaTag = findViewById(R.id.insta_tag);
        mInstaTag.setImageToBeTaggedEvent(taggedImageEvent);

        // final TextView cancelTextView = findViewById(R.id.cancel);
        final TagImageView doneImageView = findViewById(R.id.done);
        final TextView tvDone = findViewById(R.id.tvDone);
        final TagImageView backImageView = findViewById(R.id.get_back);

        mRecyclerViewSomeOneToBeTagged = findViewById(R.id.rv_some_one_to_be_tagged);
        //  mTapPhotoToTagSomeOneTextView = findViewById(R.id.tap_photo_to_tag_someone);
        mHeaderSomeOneToBeTagged = findViewById(R.id.header_tag_photo);
        mHeaderSearchSomeOne = findViewById(R.id.header_search_someone);
        searchview = findViewById(R.id.searchview);

        // mEditSearchForSomeOne = findViewById(R.id.searchview);
        // mEditSearchForSomeOne.addTextChangedListener(textWatcher);

        // cancelTextView.setOnClickListener(this);
        doneImageView.setOnClickListener(this);
        backImageView.setOnClickListener(this);
        tvDone.setOnClickListener(this);

        searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchKeyword = newText;
                list.clear();
                mSomeOneAdapter.notifyDataSetChanged();
                endlesScrollListener.resetState();
                callSearchPeopleAPI(newText, 0);
                return false;
            }
        });

        loadImage();

       /* mSomeOnes.addAll(SomeOneData.getDummySomeOneList());
        mSomeOneAdapter = new PeopleAdapter(mSomeOnes, this, this);*/
        mSomeOneAdapter = new PeopleAdapter(DemoTagActivity.this,list,this);
        mRecyclerViewSomeOneToBeTagged.setAdapter(mSomeOneAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mRecyclerViewSomeOneToBeTagged.setLayoutManager(lm);

        mSomeOneAdapter.setListener(this);

        endlesScrollListener = new RecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                mSomeOneAdapter.showHideLoading(true);
                callSearchPeopleAPI(searchKeyword, page);
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {
                if (dy > 0 ) {
                    KeyboardUtil.hideKeyboard(view, DemoTagActivity.this);
                }

            }
        };
        endlesScrollListener.resetState();
        mRecyclerViewSomeOneToBeTagged.addOnScrollListener(endlesScrollListener);
        if (list.size()==0)
            callSearchPeopleAPI("", 0);
    }

    private void loadImage() {
        Glide
                .with(this)
                .load(mPhotoToBeTaggedUri)
                .apply(requestOptions)
                .into(mInstaTag.getTagImageView());
    }

    private final InstaTag.TaggedImageEvent taggedImageEvent = new InstaTag.TaggedImageEvent() {
        @Override
        public void singleTapConfirmedAndRootIsInTouch(final int x, final int y) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAddTagInX = x;
                    mAddTagInY = y;
                    mHeaderSearchSomeOne.setVisibility(View.VISIBLE);
                    mRecyclerViewSomeOneToBeTagged.setVisibility(View.VISIBLE);
                    mHeaderSomeOneToBeTagged.setVisibility(View.GONE);
                    //  mTapPhotoToTagSomeOneTextView.setVisibility(View.GONE);
                    //mHeaderSearchSomeOne.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }
    };

/*
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mEditSearchForSomeOne.getText().toString().trim().equals("")) {
                mSomeOnes.clear();
                mSomeOnes.addAll(SomeOneData.getDummySomeOneList());
                mSomeOneAdapter.notifyDataSetChanged();
            } else {
                mSomeOnes.clear();
                mSomeOnes.addAll(SomeOneData.
                        getFilteredUser(mEditSearchForSomeOne.getText().toString().trim()));
                mSomeOneAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
          /*  case R.id.cancel:
                CommonUtil.hideKeyboard(this);
                mRecyclerViewSomeOneToBeTagged.scrollToPosition(0);
                mRecyclerViewSomeOneToBeTagged.setVisibility(View.GONE);
                //  mTapPhotoToTagSomeOneTextView.setVisibility(View.VISIBLE);
                mHeaderSearchSomeOne.setVisibility(View.GONE);
                mHeaderSomeOneToBeTagged.setVisibility(View.VISIBLE);
                break;*/
            case R.id.done:
                if (mInstaTag.getListOfTagsToBeTagged().isEmpty()) {
                    Toast.makeText(this,
                            "Please tag at least one user", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<TaggedPhoto> taggedPhotoArrayList = Mualab.getInstance().getTaggedPhotos();
                    taggedPhotoArrayList.add(
                            new TaggedPhoto(Calendar.getInstance().getTimeInMillis() + "",
                                    mPhotoToBeTaggedUri.toString(),
                                    mInstaTag.getListOfTagsToBeTagged()));
                    Mualab.getInstance().setTaggedPhotos(taggedPhotoArrayList);
                    Toast.makeText(this,
                            "Photo tagged successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

                case R.id.tvDone:
                if (mInstaTag.getListOfTagsToBeTagged().isEmpty()) {
                    Toast.makeText(this,
                            "Please tag at least one user", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayList<TaggedPhoto> taggedPhotoArrayList = Mualab.getInstance().getTaggedPhotos();
                    taggedPhotoArrayList.add(
                            new TaggedPhoto(Calendar.getInstance().getTimeInMillis() + "",
                                    mPhotoToBeTaggedUri.toString(),
                                    mInstaTag.getListOfTagsToBeTagged()));
                    Mualab.getInstance().setTaggedPhotos(taggedPhotoArrayList);
               //     Toast.makeText(this,"Photo tagged successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.get_back:
                finish();
                break;
        }
    }

    @Override
    public void onSomeOneClicked(final SomeOne someOne, int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CommonUtil.hideKeyboard(DemoTagActivity.this);
                mInstaTag.addTag(mAddTagInX, mAddTagInY, someOne.getUserName());
                mRecyclerViewSomeOneToBeTagged.setVisibility(View.GONE);
                // mTapPhotoToTagSomeOneTextView.setVisibility(View.VISIBLE);
                mHeaderSearchSomeOne.setVisibility(View.GONE);
                mHeaderSomeOneToBeTagged.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPeopleClicked(final ExSearchTag someOne, int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CommonUtil.hideKeyboard(DemoTagActivity.this);
                mInstaTag.addTag(mAddTagInX, mAddTagInY, someOne.title);
                mRecyclerViewSomeOneToBeTagged.setVisibility(View.GONE);
                // mTapPhotoToTagSomeOneTextView.setVisibility(View.VISIBLE);
                mHeaderSearchSomeOne.setVisibility(View.GONE);
                mHeaderSomeOneToBeTagged.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLoading(){
        ll_loadingBox.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tv_msg.setText(getString(R.string.loading));
    }

    private void callSearchPeopleAPI(final String searchKeyWord, int pageNo){

        Map<String, String> params = new HashMap<>();
        params.put("userId", ""+Mualab.currentUser.id);
        params.put("type", "people");
        params.put("page", ""+pageNo);
        params.put("limit", "20");
        params.put("search", searchKeyWord);
        //String tag = TAG + exSearchType;
        Mualab.getInstance().cancelPendingRequests("people");
        new HttpTask(new HttpTask.Builder(DemoTagActivity.this, "exploreSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    mSomeOneAdapter.showHideLoading(false);
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    //String message = js.getString("message");
                    if (status.equalsIgnoreCase("success")) {

                        Gson gson = new Gson();
                        JSONArray array=null;
                        if(js.has("topList"))
                            array= js.getJSONArray("topList");
                        else if(js.has("peopleList"))
                            array= js.getJSONArray("peopleList");
                        else if(js.has("placeList"))
                            array= js.getJSONArray("placeList");
                        else if(js.has("hasTagList"))
                            array= js.getJSONArray("hasTagList");

                        if(array!=null){
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);
                                ExSearchTag searchTag = gson.fromJson(String.valueOf(jsonObject), ExSearchTag.class);

                                searchTag.type = 1;
                                searchTag.title = searchTag.uniTxt;
                                searchTag.desc = searchTag.postCount+" post";

                                list.add(searchTag);
                            }
                        }
                        mSomeOneAdapter.notifyDataSetChanged();
                    }

                    if(list.size()==0){
                        tv_msg.setText(getString(R.string.no_data_found));
                    }else {
                        ll_loadingBox.setVisibility(View.GONE);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                    progress_bar.setVisibility(View.GONE);
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                progress_bar.setVisibility(View.GONE);
                mSomeOneAdapter.showHideLoading(false);
            }})
                .setParam(params)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute("people");
    }

}
