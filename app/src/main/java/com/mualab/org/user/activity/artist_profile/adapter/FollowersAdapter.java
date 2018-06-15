package com.mualab.org.user.activity.artist_profile.adapter;


import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.model.Followers;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FollowersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<Followers> followersList;
    private boolean showLoader;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    private boolean isFollowers;

    // Constructor of the class
    public FollowersAdapter(Context context, List<Followers> staffList, boolean isFollowers) {
        this.context = context;
        this.followersList = staffList;
        this.isFollowers = isFollowers;
    }

    @Override
    public int getItemCount() {
        return followersList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==followersList.size()-1){
            return showLoader?VIEWTYPE_LOADER:VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

 /*   @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followers_layout, parent, false);
        return new ViewHolder(view);

    }*/

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_staff_item_layout, parent, false);
        // return new ViewHolder(view);
        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followers_layout, parent, false);
                return new ViewHolder(view);

            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final Followers item = followersList.get(position);

        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        holder.tvFollowerName.setText(item.firstName);

        if (isFollowers){
            if (item.followerStatus.equals("1")){
                holder.btnFollow.setText("Unfollow");
            }else {
                holder.btnFollow.setText("Follow");
            }
        }else {
            if (item.followerStatus.equals("1")){
                holder.btnFollow.setText("Unfollow");
            }else {
                holder.btnFollow.setText("Follow");
            }
        }

        if (user.id==Integer.parseInt(item.followerId)){
            holder.btnFollow.setVisibility(View.GONE);
        }else
            holder.btnFollow.setVisibility(View.VISIBLE);

        if (!item.profileImage.equals("")){
            Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvFollowerName;
        ImageView ivProfile;
        AppCompatButton btnFollow;
        private ViewHolder(View itemView)
        {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvFollowerName = itemView.findViewById(R.id.tvFollowerName);
            btnFollow = itemView.findViewById(R.id.btnFollow);

            btnFollow.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnFollow:
                    Followers followers = followersList.get(getAdapterPosition());
                    apiForGetFollowUnFollow(followers);
                    break;
            }

        }
    }

    private void apiForGetFollowUnFollow(final Followers followers){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(context, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGetFollowUnFollow(followers);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("followerId", followers.followerId);
       /* if (isFollowers) {
            params.put("userId", String.valueOf(user.id));
            params.put("followerId", followers.followerId);
        }else {
            params.put("userId", followers.userId);
            params.put("followerId", String.valueOf(user.id));

        }*/


        HttpTask task = new HttpTask(new HttpTask.Builder(context, "followFollowing", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        if (followers.followerStatus.equals("1")){
                            followers.followerStatus = "0";
                        }else {
                            followers.followerStatus = "1";
                        }
                        notifyDataSetChanged();
                    }else {
                        MyToast.getInstance(context).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(context);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

}