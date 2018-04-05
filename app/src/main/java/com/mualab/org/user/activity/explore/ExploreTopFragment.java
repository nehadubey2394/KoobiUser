package com.mualab.org.user.activity.explore;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.adapter.SearchAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.listner.EndlessRecyclerViewScrollListener;
import com.mualab.org.user.listner.SearchViewListner;
import com.mualab.org.user.task.HttpResponceListner;
import com.mualab.org.user.task.HttpTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dharmraj Acharya
 **/
public class ExploreTopFragment extends Fragment implements SearchAdapter.Listener,
        SearchViewListner{

    public static String TAG = ExploreTopFragment.class.getName();
    private Context mContext;
    private SearchAdapter adapter;
    private EndlessRecyclerViewScrollListener endlesScrollListener;
    private List<ExSearchTag> list;
    private String exSearchType = "top";

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
        LinearLayoutManager lm = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        RecyclerView rvTopSearch = view.findViewById(R.id.rvTopSearch);
        rvTopSearch.setLayoutManager(lm);
        adapter = new SearchAdapter(mContext, list, this);
        rvTopSearch.setAdapter(adapter);

        endlesScrollListener = new EndlessRecyclerViewScrollListener(lm) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                adapter.showHideLoading(true);
                callSearchAPI("", page);
            }
        };
        endlesScrollListener.resetState();
        callSearchAPI("", 0);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onFeedClick(ExSearchTag searchTag, int index) {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser){
            ExploreSearchFragment.searchViewListner = new SearchViewListner() {
                @Override
                public void onTextChange(String text) {
                    endlesScrollListener.resetState();
                    callSearchAPI(text, 0);
                }
            };
        }
    }

    private void callSearchAPI(final String searchKeyWord, int pageNo){

        Map<String, String> params = new HashMap<>();
         params.put("userId", ""+Mualab.currentUser.id);
         params.put("type", exSearchType);
         params.put("page", ""+pageNo);
         params.put("limit", "20");
         params.put("search", searchKeyWord);

         Mualab.getInstance().cancelPendingRequests(TAG + exSearchType);
        new HttpTask(new HttpTask.Builder(mContext, "exploreSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
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
                                        searchTag.desc = "105 Post";
                                        break;

                                    case "people":
                                        searchTag.type = 1;
                                        searchTag.title = searchTag.uniTxt;
                                        searchTag.desc = "105 Post";
                                        break;

                                    case "hasTag":
                                        searchTag.type = 2;
                                        searchTag.title = searchTag.tag;
                                        searchTag.desc = "5124015 Public post";
                                        break;

                                    case "serviceTag":
                                        searchTag.type = 3;
                                        searchTag.title = searchTag.uniTxt;
                                        searchTag.desc = "5124015 Public post";
                                        break;

                                    case "place":
                                        searchTag.type = 4;
                                        searchTag.title = searchTag.uniTxt;
                                        searchTag.desc = "5124015 Public post";
                                        break;
                                }

                                list.add(searchTag);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                adapter.showHideLoading(false);
            }})
                .setParam(params)
                .setProgress(false)
                .setBodyContentType(HttpTask.ContentType.X_WWW_FORM_URLENCODED))
                .execute(TAG);
    }

    @Override
    public void onTextChange(String text) {
        list.clear();
        adapter.notifyDataSetChanged();
        endlesScrollListener.resetState();
        callSearchAPI(text, 0);
    }
}
