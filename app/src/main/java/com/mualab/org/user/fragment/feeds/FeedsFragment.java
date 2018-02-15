package com.mualab.org.user.fragment.feeds;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.adapter.feeds.FeedAdapter;
import com.mualab.org.user.adapter.feeds.FeedItemAnimator;
import com.mualab.org.user.adapter.feeds.ImagesAdapter;
import com.mualab.org.user.adapter.feeds.LiveUserAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.helper.MyToast;
import com.mualab.org.user.model.User;
import com.mualab.org.user.model.feeds.AllFeeds;
import com.mualab.org.user.model.feeds.LiveUserInfo;
import com.mualab.org.user.pagination.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.session.Session;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;
import com.mualab.org.user.util.ConnectionDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class FeedsFragment extends Fragment implements View.OnClickListener,FeedAdapter.Listener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    public static final int FEED_TAB = 1, SEARCH_TAB = 2;
    private int CURRENT_FEED_STATE = 0, CURRENT_FEED_URI_STATE = 0;
    private String mParam1;
    // ui components delecration
    private Context mContext;
    private TextView  tvImages, tvVideos, tvFeeds,tvNoData;
    private LinearLayout ll_header;
    private ImageView iv_profileImage;
    private LiveUserAdapter liveUserAdapter;
    private ArrayList<LiveUserInfo> liveUserList;
    private EditText edCaption;
    private String caption;
    private FeedAdapter feedAdapter;
    private ImagesAdapter imagesAdapter;
    private int lastFeedTypeId;
    private RecyclerView rvFeed,rvMyStory;
    private List<AllFeeds> allfeedsList;
    private boolean inProgressAPI;

    public FeedsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    public static FeedsFragment newInstance(String param1, String param2) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feeds, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View view){
        allfeedsList = new ArrayList<>();
        liveUserList = new ArrayList<>();
        liveUserAdapter = new LiveUserAdapter(mContext, liveUserList, FEED_TAB);

        ll_header = view.findViewById(R.id.ll_header);
        tvImages = view.findViewById(R.id.tvImages);
        tvVideos =  view.findViewById(R.id.tvVideos);
        tvFeeds = view.findViewById(R.id.tvFeeds);
        tvNoData = view.findViewById(R.id.tvNoData);
        rvMyStory = view.findViewById(R.id.recyclerView);
        rvFeed = view.findViewById(R.id.rvFeed);
        view.findViewById(R.id.ly_images).setOnClickListener(this);
        view.findViewById(R.id.ly_videos).setOnClickListener(this);
        view.findViewById(R.id.ly_feeds).setOnClickListener(this);

        liveUserList.clear();
        User user = Mualab.getInstance().getSessionManager().getUser();
        LiveUserInfo liveUserInfo = new LiveUserInfo();
        liveUserInfo.id = user.id;
        liveUserInfo.fullName = "Your Story";
        liveUserInfo.address = user.address;
        liveUserInfo.profileImage = user.profileImage;
        liveUserList.add(liveUserInfo);
        rvMyStory.setAdapter(liveUserAdapter);

        updateViewType(R.id.ly_feeds);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ly_feeds:
            case R.id.ly_images:
            case R.id.ly_videos:
                updateViewType(view.getId());
                break;
          /*  case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvVideos.setTextColor(getResources().getColor(R.color.text_color));
                tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
                MyToast.getInstance(getActivity()).showSmallCustomToast("Under developement");
                break;
            case R.id.ly_videos:
                tvImages.setTextColor(getResources().getColor(R.color.text_color));
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                tvFeeds.setTextColor(getResources().getColor(R.color.text_color));
                MyToast.getInstance(getActivity()).showSmallCustomToast("Under developement");
                break;
            case R.id.ly_feeds:
                tvImages.setTextColor(getResources().getColor(R.color.text_color));
                tvVideos.setTextColor(getResources().getColor(R.color.text_color));
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));
                MyToast.getInstance(getActivity()).showSmallCustomToast("Under developement");
                break;*/

            case R.id.tv_post:
                caption = edCaption.getText().toString().trim();
                break;
        }
    }

    private void addRemoveHeader(final boolean wantAddView){
        if(wantAddView && ll_header.getChildCount()==0){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            @SuppressLint("InflateParams")
            View inflatedLayout= inflater.inflate(R.layout.post_add_header_layout, null, false);
            ll_header.addView(inflatedLayout);

            iv_profileImage = ll_header.findViewById(R.id.iv_profileImage);
            iv_profileImage.setOnClickListener(this);
            ll_header.findViewById(R.id.ivWeb).setOnClickListener(this);
            ll_header.findViewById(R.id.iv_get_img).setOnClickListener(this);
            ll_header.findViewById(R.id.iv_video_popup).setOnClickListener(this);
            ll_header.findViewById(R.id.tv_post).setOnClickListener(this);
            edCaption = ll_header.findViewById(R.id.ed_caption);
            edCaption.setText("");

        }else if(ll_header!=null && ll_header.getChildCount()>0){
            ll_header.removeAllViews();
        }
    }

    private void updateViewType(int id) {
        tvVideos.setTextColor(getResources().getColor(R.color.text_color));
        tvImages.setTextColor(getResources().getColor(R.color.text_color));
        tvFeeds.setTextColor(getResources().getColor(R.color.text_color));

        final int numberOfColumns = 3;
        // GridLayoutManager gm = new GridLayoutManager(mContext, numberOfColumns, LinearLayoutManager.VERTICAL, false);
       /* gm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // 2 column size for first row
                return (position == 0 ? numberOfColumns : 1);
            }
        });*/
        LinearLayoutManager lm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);

        switch (id) {
            case R.id.ly_feeds:
                addRemoveHeader(true);
                tvFeeds.setTextColor(getResources().getColor(R.color.colorPrimary));
                feedAdapter = new FeedAdapter(mContext, allfeedsList, this);

                setPagination(lm);
                rvFeed.setLayoutManager(lm);
                rvFeed.setItemAnimator(new FeedItemAnimator());
                rvFeed.setAdapter(feedAdapter);

                if (lastFeedTypeId != R.id.ly_feeds){
                    allfeedsList.clear();
                    ParseAndUpdateUI(loadAllFeedJSONFromAsset(), Constant.FEED_STATE);
                    //apiForGetAllFeeds("", 0, "10", Constant.FEED_STATE);
                }
                break;

            case R.id.ly_images:
                tvImages.setTextColor(getResources().getColor(R.color.colorPrimary));
                addRemoveHeader(false);
                imagesAdapter = new ImagesAdapter(mContext, allfeedsList, Constant.IMAGE_STATE);
                rvFeed.setItemAnimator(new FeedItemAnimator());
                rvFeed.setLayoutManager(lm);
                rvFeed.setAdapter(imagesAdapter);

                if (lastFeedTypeId != R.id.ly_images){
                    allfeedsList.clear();
                    ParseAndUpdateUI(loadImageJSONFromAsset(), Constant.IMAGE_STATE);
                    //apiForGetAllFeeds("image", 0, "20", Constant.IMAGE_STATE);
                }

                break;

            case R.id.ly_videos:
                tvVideos.setTextColor(getResources().getColor(R.color.colorPrimary));
                addRemoveHeader(false);

                imagesAdapter = new ImagesAdapter(mContext, allfeedsList, Constant.VIDEO_STATE);
                setPagination(lm);
                rvFeed.setLayoutManager(lm);
                rvFeed.setItemAnimator(new FeedItemAnimator());
                rvFeed.setAdapter(imagesAdapter);

                if (lastFeedTypeId != R.id.ly_videos){
                    allfeedsList.clear();
                    ParseAndUpdateUI(loadVideoJSONFromAsset(), Constant.VIDEO_STATE);
                    // apiForGetAllFeeds("video", 0, "20", Constant.VIDEO_STATE);
                }
                break;
        }

        lastFeedTypeId = id;
    }

    public String loadAllFeedJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("all_feeds_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    public String loadImageJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("feeds_image_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
    public String loadVideoJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open("feeds_video_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void apiForGetAllFeeds(final String feedType, final int page, final String limit, final int type){
        Session session = Mualab.getInstance().getSessionManager();
        User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(mContext, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetAllFeeds(feedType,page,limit,type);
                    }

                }
            }).show();
        }


        Map<String, String> params = new HashMap<>();
        params.put("feedType", feedType);
        params.put("search", "");
        params.put("page", String.valueOf(page));
        params.put("limit", limit);
        params.put("type", "home");
        // params.put("appType", "user");

        new HttpTask(new HttpTask.Builder(mContext, "artistSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        removeProgress(type);
                        ParseAndUpdateUI(response, type);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    String str = error.getLocalizedMessage();
                    removeProgress(type);
                    if(str.equals("java.net.ConnectException: Connection refused")){
                        new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Connection Error")
                                .setContentText("Mualab encountered an error while try to connect to the server.")
                                .show();
                    }else {
                        MyToast.getInstance(mContext).showSmallCustomToast(error.getLocalizedMessage());
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }})
                .setAuthToken(user.authToken)
                .setParam(params)
                .setMethod(Request.Method.POST)
                .setProgress(true)
                .setBodyContentType(HttpTask.ContentType.APPLICATION_JSON))
                .execute(this.getClass().getName());
    }

    private void ParseAndUpdateUI(final String response, final int type) {
        AllFeeds allFeeds;
        try {
            JSONObject js = new JSONObject(response);
            String status = js.getString("status");
            // String message = js.getString("message");
            inProgressAPI = false;
            if (status.equalsIgnoreCase("success")) {
                rvFeed.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
                JSONArray array = js.getJSONArray("AllFeeds");
                for (int i = 0; i < array.length(); i++) {
                    Gson gson = new Gson();
                    JSONObject jsonObject = array.getJSONObject(i);
                    allFeeds = gson.fromJson(String.valueOf(jsonObject), AllFeeds.class);
                    allfeedsList.add(allFeeds);
                }

                if (type == Constant.IMAGE_STATE) {
                    imagesAdapter.notifyDataSetChanged();

                } else if (type == Constant.VIDEO_STATE) {
                    imagesAdapter.notifyDataSetChanged();

                } else if (type == Constant.FEED_STATE) {
                    feedAdapter.notifyDataSetChanged();
                }
                CURRENT_FEED_STATE = type;

            } else if (status.equals("fail") && allfeedsList.size()==0) {
                rvFeed.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
                if (type == Constant.IMAGE_STATE) {
                    imagesAdapter.notifyDataSetChanged();
                } else if (type == Constant.VIDEO_STATE) {
                    imagesAdapter.notifyDataSetChanged();
                } else if (type == Constant.FEED_STATE) {
                    feedAdapter.notifyDataSetChanged();
                }
                CURRENT_FEED_STATE = type;
            }

        } catch (JSONException e) {
            e.printStackTrace();
            inProgressAPI = false;
            MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.alert_something_wenjt_wrong));
        }
    }

    private void removeProgress(final int type){

        if(allfeedsList!=null && allfeedsList.size()>0 && allfeedsList.get(allfeedsList.size()-1)==null){
            int lastIndex = allfeedsList.size()-1;
            allfeedsList.remove(lastIndex);
            if (type == Constant.IMAGE_STATE) {
                imagesAdapter.notifyItemRemoved(lastIndex);
            } else if (type == Constant.VIDEO_STATE) {
                imagesAdapter.notifyItemRemoved(lastIndex);
            } else if (type == Constant.FEED_STATE) {
                feedAdapter.notifyItemRemoved(lastIndex);
            }
        }
    }

    private void setPagination(GridLayoutManager manager){
        EndlessRecyclerViewScrollListener listener = new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                //callAPI(page);
            }
        };
        listener.resetState();
        rvFeed.addOnScrollListener(listener);
    }

    private void setPagination(LinearLayoutManager manager){
        EndlessRecyclerViewScrollListener listener = new EndlessRecyclerViewScrollListener(manager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                //  apiForGetAllFeeds(page);
            }
        };
        listener.resetState();
        rvFeed.addOnScrollListener(listener);
    }

    @Override
    public void onCommentBtnClick(AllFeeds feed, int pos) {

    }
}
