package com.mualab.org.user.activity.artist_profile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.data.model.booking.BookingServices3;
import com.mualab.org.user.data.model.booking.SubServices;

import java.util.ArrayList;
import java.util.List;


public class SubServiceExpandListAdapter extends BaseExpandableListAdapter {
    private Context activity;
    private List<SubServices> parentArrayList;
    private ArrayList<BookingServices3> childtems;

    public SubServiceExpandListAdapter(Context activity, List<SubServices> parents) {
        this.parentArrayList = parents;
        this.activity = activity;
    }


    @Override
    public int getGroupCount() {
        return this.parentArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (parentArrayList.size()!=0){
            try {
                childtems = parentArrayList.get(groupPosition).artistservices;

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return childtems.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.parentArrayList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (parentArrayList.size()!=0){
            try {
                childtems = parentArrayList.get(groupPosition).artistservices;

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return childtems.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert infalInflater != null;
            convertView = infalInflater.inflate(R.layout.item_subservice_list, null);
            holder = new ViewHolder();
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvMainSerName = convertView.findViewById(R.id.tvMainSerName);
        holder.bottomLine = convertView.findViewById(R.id.bottomLine);

        if (parentArrayList.size()!=0){
            try {
                SubServices services = parentArrayList.get(groupPosition);
                ArrayList<BookingServices3> arrayList = services.artistservices;

                holder.bottomLine.setVisibility(View.VISIBLE);
                holder.tvMainSerName.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
               /* }else {
                    holder.ivDropDown.setRotation(270);
                    holder.bottomLine.setVisibility(View.GONE);
                    holder.tvMainSerName.setTextColor(activity.getResources().getColor(R.color.text_color));
                }*/

                holder.tvMainSerName.setText(services.subServiceName);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return convertView;

    }
    private class ViewHolder {
        TextView tvMainSerName;
        View bottomLine;
    }


    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder2 holder;

        final BookingServices3 subServices = (BookingServices3) getChild(groupPosition, childPosition);


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_artist_service_list, null);

            holder = new ViewHolder2();
            /*find layout components id*/

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder2) convertView.getTag();
        }

        holder.tvSubSerName = convertView.findViewById(R.id.tvSubSerName);

        holder.tvSubSerName.setText(subServices.title);


        return convertView;
    }

    private class ViewHolder2 {
        TextView tvSubSerName;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
