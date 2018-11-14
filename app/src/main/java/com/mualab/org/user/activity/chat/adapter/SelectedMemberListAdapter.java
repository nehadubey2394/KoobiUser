package com.mualab.org.user.activity.chat.adapter;


import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.chat.listner.OnCancleMemberClickListener;
import com.mualab.org.user.activity.chat.listner.OnUserClickListener;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.activity.chat.model.GroupMember;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class SelectedMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private  long mLastClickTime = 0;
    private Context mContext;
    private List<GroupMember> groupMemberList;
    private OnCancleMemberClickListener onCancleMemberClickListener = null;


    // Constructor of the class
    public SelectedMemberListAdapter(Context mContext, List<GroupMember> groupMemberList) {
        this.mContext = mContext;
        this.groupMemberList = groupMemberList;
    }

    public void setListener(OnCancleMemberClickListener onCancleMemberClickListener){
        this.onCancleMemberClickListener = onCancleMemberClickListener;
    }

    @Override
    public int getItemCount() {
        return groupMemberList.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_staff_item_layout, parent, false);
        // return new ViewHolder(view);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checked_member_list, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final GroupMember firebaseUser = groupMemberList.get(position);

        if (firebaseUser.profilePic!=null && !firebaseUser.profilePic.equals("")){
            Picasso.with(mContext).load(firebaseUser.profilePic).
                    placeholder(R.drawable.defoult_user_img).fit().into(holder.ivMemberPic);
        }

        holder.tv_user_name.setText(firebaseUser.userName);

    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private CircleImageView ivMemberPic;
        private ImageView ivCross;
        private TextView tv_user_name;

        private ViewHolder(View itemView)
        {
            super(itemView);
            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            ivMemberPic = itemView.findViewById(R.id.ivMemberPic);
            ivCross = itemView.findViewById(R.id.ivCross);
            ivCross.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
//            KeyboardUtil.hideSoftKeyboard((CreateGroupActivity)context);

            if (SystemClock.elapsedRealtime() - mLastClickTime < 400){
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();

            switch (view.getId()){
                case R.id.ivCross:
                    if (onCancleMemberClickListener != null) {
                        GroupMember groupMember = groupMemberList.get(getAdapterPosition());
                        groupMemberList.remove(getAdapterPosition());
                        notifyDataSetChanged();
                        onCancleMemberClickListener.onMemberClicked(groupMember, getAdapterPosition());
                    }

                    break;
            }

        }
    }

}