package com.mualab.org.user.activity.feeds.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.hendraanggrian.socialview.Mention;
import com.hendraanggrian.widget.SocialAutoCompleteTextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.base.BaseFragment;
import com.mualab.org.user.activity.feeds.adapter.HashtagAdapter;
import com.mualab.org.user.activity.feeds.adapter.UserSuggessionAdapter;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.model.feeds.Feeds;
import com.mualab.org.user.webservice.HttpResponceListner;
import com.mualab.org.user.webservice.HttpTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by dharmraj on 13/4/18.
 **/

public class FeedBaseFragment extends BaseFragment {

    protected HashtagAdapter tagAdapter;
    protected UserSuggessionAdapter mentionAdapter;
    protected SocialAutoCompleteTextView edCaption;
    protected ArrayList<String> hashTags = new ArrayList<>();
    protected String lastTxt;

    public FeedBaseFragment(){

    }


    protected void getDropDown(String tag, final String type) {
        Map<String, String> map = new HashMap<>();
        map.put("search", tag);
        map.put("type", type);
        map.put("page", "0");
        map.put("limit", "5");
        Mualab.getInstance().getRequestQueue().cancelAll("TAG_SEARCH");

        new HttpTask(new HttpTask.Builder(mContext, "tagSearch", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {

                try {
                    JSONObject object = new JSONObject(response);
                    hashTags.clear();
                    tagAdapter.clear();

                    if (object.has("allTags")) {
                        JSONArray array = object.getJSONArray("allTags");
                        if (type.equals("user")) {
                            mentionAdapter.clear();

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String fullname = obj.getString("firstName") + " " + obj.getString("lastName");
                                String username = obj.getString("userName");
                                String profileImage = obj.getString("profileImage");
                                mentionAdapter.add(new Mention(username, fullname, profileImage));
                            }
                            mentionAdapter.notifyDataSetChanged();
                        } else {

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                hashTags.add(obj.getString("tag").replace("#", ""));
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (hashTags.size() > 0) {
                    tagAdapter.addAll(hashTags);
                    tagAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {

            }
        })
                .setParam(map)
                .setProgress(false))
                .execute("TAG_SEARCH");
        lastTxt = tag;
    }


    /*Rj*/
    private Dialog builder;
    public void publicationQuickView(Feeds feeds, int index){
        @SuppressLint("InflateParams")
        View view = getLayoutInflater().inflate( R.layout.dialog_image_detail_view, null);

        ImageView postImage = view.findViewById(R.id.ivFeedCenter);
        ImageView profileImage =  view.findViewById(R.id.ivUserProfile);
        TextView tvUsername =  view.findViewById(R.id.txtUsername);
        tvUsername.setText(feeds.userName);

        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideQuickView();
            }
        });

        view.findViewById(R.id.tvUnfollow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyToast.getInstance(mContext).showSmallCustomToast(getString(R.string.under_development));
            }
        });

        Picasso.with(mContext).load(feeds.feed.get(index)).priority(Picasso.Priority.HIGH).noPlaceholder().into(postImage);

        if(TextUtils.isEmpty(feeds.profileImage))
            Picasso.with(mContext).load(R.drawable.defoult_user_img).noPlaceholder().into(profileImage);
        else Picasso.with(mContext).load(feeds.profileImage).noPlaceholder().into(profileImage);

        builder = new Dialog(mContext);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //noinspection ConstantConditions
        builder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        builder.setContentView(view);
        builder.setCancelable(true);
        builder.show();
    }

    public void hideQuickView(){
        if(builder != null) builder.dismiss();
    }
}
