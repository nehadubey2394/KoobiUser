package com.mualab.org.user.activity.explore.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.explore.ExplorSearchActivity;
import com.mualab.org.user.activity.explore.SearchFeedActivity;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.explore.adapter.SearchAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.listner.RecyclerViewScrollListener;
import com.mualab.org.user.listner.SearchViewListner;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.utils.KeyboardUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dharmraj Acharya
 **/
public class ExploreTopFragment extends BaseFragment implements SearchAdapter.Listener,
        SearchViewListner{
    //public static String TAG = ExploreTopFragment.class.getName();

    private Context mContext;
    private LinearLayout ll_loadingBox;
    private ProgressBar progress_bar;
    private TextView tv_msg;

    private SearchAdapter adapter;
    private RecyclerViewScrollListener endlesScrollListener;
    private List<ExSearchTag> list;
    private String exSearchType = "top";
    private String searchKeyWord = "";
    private boolean isViewCreated;
    private boolean isFirstTimeVisiable;


    public ExploreTopFragment() {
        // Required empty public constructor
    }

    public static ExploreTopFragment newInstance(String exSearchType) {
        ExploreTopFragment fragment = new ExploreTopFragment();
        Bundle args = new Bundle();
        args.putString("exSearchType", exSearchType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<>();
        if(getArguments()!=null){
            exSearchType = getArguments().getString("exSearchType");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_top, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ll_loadingBox = view.findViewById(R.id.ll_loadingBox);
        progress_bar = view.findViewById(R.id.progress_bar);
        tv_msg = view.findViewById(R.id.tv_msg);

        LinearLayoutManager lm = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        RecyclerView rvTopSearch = view.findViewById(R.id.rvTopSearch);
        rvTopSearch.setLayoutManager(lm);
        adapter = new SearchAdapter(mContext, list, this);
        rvTopSearch.setAdapter(adapter);

        endlesScrollListener = new RecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                adapter.showHideLoading(true);
                callSearchAPI(searchKeyWord, page);
            }

            @Override
            public void onScroll(RecyclerView view, int dx, int dy) {
                if (dy > 0 ) {
                    KeyboardUtil.hideKeyboard(view, mContext);
                }

            }
        };
        endlesScrollListener.resetState();
        rvTopSearch.addOnScrollListener(endlesScrollListener);


        showLoading();
        searchKeyWord = "";
        isViewCreated = true;
        if(getUserVisibleHint()){
            callSearchAPI("", 0);
        }
    }

    private void showLoading(){
        ll_loadingBox.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.VISIBLE);
        tv_msg.setText(getString(R.string.loading));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onItemClick(ExSearchTag searchTag, int index) {
        Intent intent = new Intent(mContext, SearchFeedActivity.class);
        intent.putExtra("searchKey",searchTag);
        startActivity(intent);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser){
            ExplorSearchActivity.searchViewListner = new SearchViewListner() {
                @Override
                public void onTextChange(String text) {
                    list.clear();
                    searchKeyWord = TextUtils.isEmpty(text)?"":text.trim();
                    endlesScrollListener.resetState();
                    showLoading();
                    callSearchAPI(searchKeyWord, 0);
                }
            };



            if(!ExplorSearchActivity.searchKeyword.equals(searchKeyWord) && list!=null){
                list.clear();
                adapter.notifyDataSetChanged();
                endlesScrollListener.resetState();
                searchKeyWord = ExplorSearchActivity.searchKeyword;
                showLoading();
                callSearchAPI(searchKeyWord, 0);
            }else if(isViewCreated && !isFirstTimeVisiable && list!=null){
                isFirstTimeVisiable = true;
                list.clear();
                showLoading();
                callSearchAPI(searchKeyWord, 0);
            }
        }
    }

    private void callSearchAPI(final String searchKeyWord, int pageNo){

        Map<String, String> params = new HashMap<>();
        params.put("userId", ""+Mualab.currentUser.id);
        params.put("type", exSearchType);
        params.put("page", ""+pageNo);
        params.put("limit", "20");
        params.put("search", searchKeyWord);
        //String tag = TAG + exSearchType;
        Mualab.getInstance().cancelPendingRequests(exSearchType);
        new HttpTask(new HttpTask.Builder(mContext, "exploreSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    progress_bar.setVisibility(View.GONE);
                    adapter.showHideLoading(false);
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

                                switch (exSearchType){

                                    case "top":
                                        searchTag.type = 0;
                                        searchTag.title = searchTag.uniTxt;
                                        searchTag.desc = searchTag.postCount+" post";
                                        break;

                                    case "people":
                                        searchTag.type = 1;
                                        searchTag.title = searchTag.uniTxt;
                                        searchTag.desc = searchTag.postCount+" post";
                                        break;

                                    case "hasTag":
                                        searchTag.type = 2;
                                        searchTag.title = "#"+searchTag.tag;
                                        searchTag.desc = searchTag.postCount+" public post";
                                        break;

                                    case "serviceTag":
                                        searchTag.type = 3;
                                        searchTag.title = searchTag.uniTxt;
                                        //NumberFormat.getNumberInstance(Locale.US).format(searchTag.postCount);
                                        searchTag.desc = searchTag.postCount+" public post";
                                        break;

                                    case "place":
                                        searchTag.type = 4;
                                        searchTag.title = searchTag.uniTxt;
                                        searchTag.desc = "0 Public post";
                                        break;
                                }

                                list.add(searchTag);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    if(list.size()==0){
                        tv_msg.setText(getString(R.string.no_res_found));
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
                adapter.showHideLoading(false);
            }})
                .setParam(params)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute(exSearchType);
    }

    @Override
    public void onTextChange(String text) {
        list.clear();
        adapter.notifyDataSetChanged();
        endlesScrollListener.resetState();
        callSearchAPI(text, 0);
    }
}
