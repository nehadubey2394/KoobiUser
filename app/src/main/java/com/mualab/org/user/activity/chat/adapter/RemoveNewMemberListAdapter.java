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
import com.mualab.org.user.activity.chat.listner.OnMemberSelectListener;
import com.mualab.org.user.activity.chat.listner.OnUserClickListener;
import com.mualab.org.user.activity.chat.model.FirebaseUser;
import com.mualab.org.user.activity.chat.model.GroupMember;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class RemoveNewMemberListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private  long mLastClickTime = 0;
    private Context mContext;
    private List<GroupMember> groupMember;
    private OnMemberSelectListener onUserClickListener = null;

    // Constructor of the class
    public RemoveNewMemberListAdapter(Context mContext, List<GroupMember> groupMember) {
        this.mContext = mContext;
        this.groupMember = groupMember;
    }

    public void setListener(OnMemberSelectListener onUserClickListener){
        this.onUserClickListener = onUserClickListener;
    }

    @Override
    public int getItemCount() {
        return groupMember.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_staff_item_layout, parent, false);
        // return new ViewHolder(view);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_userlist_chat, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final GroupMember member = groupMember.get(position);

        if (member.profilePic!=null && !member.profilePic.equals("")){
            Picasso.with(mContext).load(member.profilePic).
                    placeholder(R.drawable.defoult_user_img).fit().into(holder.ivProfilePic);
        }

        holder.tvUserName.setText(member.userName);

        if (member.isChecked){
            holder.chat_checkbox.setImageResource(R.drawable.ic_active_check_ico);

        }else {
            holder.chat_checkbox.setImageResource(R.drawable.ic_inactive_check_ico);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private CircleImageView ivProfilePic;
        private TextView tvUserName;
        private ImageView chat_checkbox;
        private RelativeLayout rlItemMain;


        private ViewHolder(View itemView)
        {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            chat_checkbox = itemView.findViewById(R.id.chat_checkbox);
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
                case R.id.rlItemMain:
                    GroupMember user = groupMember.get(getAdapterPosition());
                    if (onUserClickListener != null) {
                        if (!user.isChecked){
                            user.isChecked = true;
                        }else {
                            user.isChecked = false;
                        }
                        notifyItemChanged(getAdapterPosition());
                        onUserClickListener.onMemberSelect(user, getAdapterPosition());
                    }
                    break;
            }

        }
    }

    public void filterList(List<GroupMember> groupMember) {
        this.groupMember = groupMember;
        notifyDataSetChanged();
    }
}