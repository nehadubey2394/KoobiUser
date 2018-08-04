package com.mualab.org.user.activity.people_tag.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.make_booking.listner.TimeSlotClickListener;
import com.mualab.org.user.activity.people_tag.instatag.TagToBeTagged;
import com.mualab.org.user.activity.people_tag.listner.TagListClickListener;
import com.mualab.org.user.data.model.booking.BookingTimeSlot;

import java.util.ArrayList;
import java.util.HashMap;


public class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.ViewHolder> {
    private Context context;
    private String s = "";
    private ArrayList<TagToBeTagged> taggedArrayList;
    private TagListClickListener customButtonListener = null;
    private HashMap<Integer,ArrayList<TagToBeTagged>> taggedImgMap;

    public void setCustomListener(TagListClickListener customButtonListener){
        this.customButtonListener = customButtonListener;
    }

    // Constructor of the class
    public TagListAdapter(Context context, ArrayList<TagToBeTagged> taggedArrayList,
                          HashMap<Integer,ArrayList<TagToBeTagged>> taggedImgMap) {
        this.context = context;
        this.taggedArrayList = taggedArrayList;
        this.taggedImgMap = taggedImgMap;
    }

    // get the size of the list
    @Override
    public int getItemCount() {
        return taggedArrayList == null ? 0 : taggedArrayList.size();
    }


    // specify the row layout file and click for each row
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_list, parent, false);
        return new ViewHolder(view);
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int listPosition) {
        TagToBeTagged tag = taggedArrayList.get(listPosition);
        holder.tvTagName.setText(tag.getUnique_tag_id());

    }

    // Static inner class to initialize the views of rows
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTagName;
        RelativeLayout rlTag;
        ImageView ivRemoveTag;

        ViewHolder(View itemView) {
            super(itemView);
            tvTagName =  itemView.findViewById(R.id.tvTagName);
            ivRemoveTag =  itemView.findViewById(R.id.ivRemoveTag);
            rlTag = itemView.findViewById(R.id.rlTag);

            ivRemoveTag.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ivRemoveTag:
                    TagToBeTagged tag = taggedArrayList.get(getAdapterPosition());
                    if (customButtonListener != null) {
                        customButtonListener.onItemRemoveClick(getAdapterPosition(),tag);
                    }
                    break;
            }
        }
    }

}
