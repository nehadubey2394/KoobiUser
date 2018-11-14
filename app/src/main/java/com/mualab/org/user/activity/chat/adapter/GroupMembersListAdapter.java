package com.mualab.org.user.activity.chat.adapter;


import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.model.GroupMember;
import com.mualab.org.user.application.Mualab;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class GroupMembersListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private  long mLastClickTime = 0;
    private Context mContext;
    private List<GroupMember> userList;
    private  final int VIEWTYPE_ITEM = 1;

    // Constructor of the class
    public GroupMembersListAdapter(Context mContext, List<GroupMember> userList) {
        this.mContext = mContext;
        this.userList = userList;
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==userList.size()-1){
            return VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }


 /*   @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followers_layout, parent, false);
        return new ViewHolder(view);

    }*/

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_staff_item_layout, parent, false);
        // return new ViewHolder(view);
        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_memberlist_chat, parent, false);
                return new ViewHolder(view);
        }
        return null;

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final GroupMember member = userList.get(position);

        if (member.profilePic!=null && !member.profilePic.equals("")){
            Picasso.with(mContext).load(member.profilePic).
                    placeholder(R.drawable.defoult_user_img).fit().into(holder.ivProfilePic);
        }

        if (member.type!=null && member.type.equals("admin"))
            holder.tvAdmin.setVisibility(View.VISIBLE);
        else
            holder.tvAdmin.setVisibility(View.GONE);

        if (member.memberId==Mualab.currentUser.id)
            holder.tvUserName.setText("You");
        else
            holder.tvUserName.setText(member.userName);

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private CircleImageView ivProfilePic;
        private TextView tvUserName,tvAdmin;
        private RelativeLayout rlItemMain;


        private ViewHolder(View itemView)
        {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvAdmin = itemView.findViewById(R.id.tvAdmin);
            rlItemMain = itemView.findViewById(R.id.rlItemMain);

            rlItemMain.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
//            KeyboardUtil.hideSoftKeyboard((CreateGroupActivity)context);

            if (SystemClock.elapsedRealtime() - mLastClickTime < 400){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (view.getId()){
              /*  case R.id.rlItemMain:
                    FirebaseUser firebaseUser = firebaseUsers.get(getAdapterPosition());
                    if (onUserClickListener != null) {
                        if (!firebaseUser.isChecked){
                            firebaseUser.isChecked = true;
                        }else {
                            firebaseUser.isChecked = false;
                        }
                        notifyItemChanged(getAdapterPosition());
                        onUserClickListener.onUserClicked(firebaseUser, getAdapterPosition());
                    }
                    break;*/
            }

        }
    }

}