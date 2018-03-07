package com.mualab.org.user.activity.booking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.model.booking.Services;
import com.mualab.org.user.model.booking.SubServices;
import com.mualab.org.user.session.Session;

import java.util.ArrayList;


public class ServiceExpandListAdapter extends BaseExpandableListAdapter {
    private Context activity;
    private ArrayList<Services> parentArrayList;
    private ArrayList<SubServices> childtems;

    public ServiceExpandListAdapter(Context activity, ArrayList<Services> parents) {
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
                childtems = parentArrayList.get(groupPosition).arrayList;

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
                childtems = parentArrayList.get(groupPosition).arrayList;

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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_main_service_list, null);
            holder = new ViewHolder();
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvMainSerName = convertView.findViewById(R.id.tvMainSerName);
        holder.ivDropDown = convertView.findViewById(R.id.ivDropDown);
        holder.bottomLine = convertView.findViewById(R.id.bottomLine);

        if (parentArrayList.size()!=0){
            try {
                Services services = parentArrayList.get(groupPosition);
                ArrayList<SubServices> arrayList = services.arrayList;
                if (arrayList.size()==0){
                    holder.ivDropDown.setVisibility(View.GONE);

                }else {
                    //  holder.ivChecbox.setVisibility(View.GONE);
                    holder.ivDropDown.setVisibility(View.VISIBLE);
                }

                if (isExpanded){
                    holder.ivDropDown.setRotation(90);
                    holder.bottomLine.setVisibility(View.VISIBLE);
                    holder.tvMainSerName.setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                }else {
                    holder.ivDropDown.setRotation(270);
                    holder.bottomLine.setVisibility(View.GONE);
                    holder.tvMainSerName.setTextColor(activity.getResources().getColor(R.color.text_color));
                }

                holder.tvMainSerName.setText(services.serviceName);

            }catch (Exception e){
                e.printStackTrace();
            }
        }


        //  Toast.makeText(activity, ingredients.itemName, Toast.LENGTH_SHORT).show();

        return convertView;

    }
    private class ViewHolder {
        ImageView ivDropDown;
        TextView tvMainSerName;
        View bottomLine;
    }


    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder2 holder;

        final SubServices subServices = (SubServices) getChild(groupPosition, childPosition);


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_sub_service_list, null);

            holder = new ViewHolder2();
        /*find layout components id*/

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder2) convertView.getTag();
        }

        holder.tvSubSerName = convertView.findViewById(R.id.tvSubSerName);
        holder.ivDropDown2 =  convertView.findViewById(R.id.ivDropDown2);
        holder.tvSubSerName.setText(subServices.subServiceName);


        return convertView;
    }

    private class ViewHolder2 {
        ImageView ivDropDown2;
        TextView tvSubSerName;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
