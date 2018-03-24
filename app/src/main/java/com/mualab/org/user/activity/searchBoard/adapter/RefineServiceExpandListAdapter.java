package com.mualab.org.user.activity.searchBoard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.model.SearchBoard.RefineServices;
import com.mualab.org.user.model.SearchBoard.RefineSubServices;

import java.util.ArrayList;


public class RefineServiceExpandListAdapter extends BaseExpandableListAdapter {
    private Context activity;
    private ArrayList<RefineServices> parentArrayList;
    private ArrayList<RefineSubServices> childtems;
    private boolean isChecked = false;

    public RefineServiceExpandListAdapter(Context activity, ArrayList<RefineServices> parents) {
        this.parentArrayList = parents;
        this.activity = activity;
    }

    public void setNewItems(Context activity, ArrayList<RefineServices> parents) {
        this.parentArrayList = parents;
        this.activity = activity;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return this.parentArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (parentArrayList.size()!=0){
            try {
                childtems = parentArrayList.get(groupPosition).getArrayList();

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
                childtems = parentArrayList.get(groupPosition).getArrayList();

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
            convertView = infalInflater.inflate(R.layout.item_refine_main_service_list, null);
            holder = new ViewHolder();
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvMainSerName = convertView.findViewById(R.id.tvMainSerName);
        holder.ivDropDown = convertView.findViewById(R.id.ivDropDown);
        holder.bottomLine = convertView.findViewById(R.id.bottomLine);
        holder.checkbox = convertView.findViewById(R.id.checkbox);

        if (parentArrayList.size()!=0){
            try {
                RefineServices services = parentArrayList.get(groupPosition);
                ArrayList<RefineSubServices> arrayList = services.getArrayList();
                if (arrayList.size()==0){
                    holder.ivDropDown.setVisibility(View.GONE);
                    if (services.isChecked.equals("0")){
                        holder.checkbox.setChecked(false);
                    }else {
                        holder.checkbox.setChecked(true);
                    }
                }else {
                    holder.checkbox.setVisibility(View.GONE);
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

                holder.tvMainSerName.setText(services.title);

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
        CheckBox checkbox;
        View bottomLine;
    }


    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final ViewHolder2 holder;

        final RefineSubServices subServices = (RefineSubServices) getChild(groupPosition, childPosition);


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_refine_sub_service_list, null);

            holder = new ViewHolder2();
        /*find layout components id*/

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder2) convertView.getTag();
        }

        holder.tvSubSerName = convertView.findViewById(R.id.tvSubSerName);
        //     holder.ivDropDown2 =  convertView.findViewById(R.id.ivDropDown2);
        holder.checkbox2 =  convertView.findViewById(R.id.checkbox2);
        holder.rlCheckBox =  convertView.findViewById(R.id.rlCheckBox);
        holder.tvSubSerName.setText(subServices.title);

        holder.checkbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    holder.checkbox2.setChecked(true);
                    subServices.isChecked = "1";
                    subServices.isSubItemChecked = true;
                }else {
                    holder.checkbox2.setChecked(false);
                    subServices.isChecked = "0";
                    subServices.isSubItemChecked = false;
                }
            }
        });

        holder.rlCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isChecked){
                    isChecked = true;
                    holder.checkbox2.setChecked(true);
                    subServices.isChecked = "1";
                    subServices.isSubItemChecked = true;
                }else {
                    isChecked = false;
                    holder.checkbox2.setChecked(false);
                    subServices.isChecked = "0";
                    subServices.isSubItemChecked = false;
                }
            }
        });

        if (subServices.isChecked.equals("1")){
            holder.checkbox2.setChecked(true);

        }else {
            holder.checkbox2.setChecked(false);
        }
        return convertView;
    }

    private class ViewHolder2 {
        //  ImageView checkbox2;
        TextView tvSubSerName;
        CheckBox checkbox2;
        RelativeLayout rlCheckBox;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
