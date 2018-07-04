package com.mualab.org.user.activity.make_booking.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.data.model.booking.BusinessDay;
import com.mualab.org.user.data.model.booking.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class AdapterBusinessDays extends RecyclerView.Adapter<AdapterBusinessDays.ViewHolder>{

    private List<BusinessDay> businessDaysList;
    private Context mContext;

    public AdapterBusinessDays(Context mContext, List<BusinessDay> businessHours) {
        this.mContext = mContext;
        this.businessDaysList = businessHours;
    }

    @Override
    public AdapterBusinessDays.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_business_days, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AdapterBusinessDays.ViewHolder holder, int position) {
        BusinessDay day = businessDaysList.get(position);
        holder.tv_dayName.setText(day.dayName);
        holder.tv_workingStatus.setVisibility(day.isOpen?View.INVISIBLE:View.VISIBLE);
        holder.ll_addTimeSlot.setVisibility(day.isOpen?View.VISIBLE:View.INVISIBLE);
        holder.listView.setVisibility(day.isOpen?View.VISIBLE:View.GONE);
        holder.checkbox.setChecked(day.isOpen);
        holder.checkbox.setEnabled(false);
    /*    holder.lyDotsLine.setVisibility(position == businessDaysList.size()-1?View.GONE:View.VISIBLE);
*/
        if(day.isOpen){
            holder.lyDotsLine.setVisibility(View.GONE);
            AdapterTimeSlot adapterTimeSlot = new AdapterTimeSlot(mContext, day.slots);
            holder.listView.setAdapter(adapterTimeSlot);
            setListViewHeightBasedOnChildren(holder.listView);
        }
    }

    @Override
    public int getItemCount() {
        return businessDaysList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CheckBox checkbox;
        // ImageView ivAddTimeSlot;
        LinearLayout ll_addTimeSlot,lyDotsLine;
        ListView listView;
        TextView tv_dayName, tv_workingStatus;

        private ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox);
            tv_dayName = itemView.findViewById(R.id.tv_dayName);
            tv_workingStatus = itemView.findViewById(R.id.tv_workingStatus);
            //ivAddTimeSlot =  itemView.findViewById(R.id.ivAddTimeSlot);
            ll_addTimeSlot =  itemView.findViewById(R.id.ll_addTimeSlot);
            lyDotsLine =  itemView.findViewById(R.id.lyDotsLine);
            listView = itemView.findViewById(R.id.listView);
            checkbox.setOnClickListener(this);
            ll_addTimeSlot.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }

    public class AdapterTimeSlot extends ArrayAdapter<TimeSlot> {

        private ArrayList<TimeSlot> timeSlots = new ArrayList<>();

        private AdapterTimeSlot(Context context, ArrayList<TimeSlot> objects) {
            super(context,0, objects);
            timeSlots = objects;
        }

        @Override
        public int getCount() {
            return timeSlots.size();
        }

        @NonNull
        @Override
        public View getView(final int position, View v, @NonNull ViewGroup parent) {
            // Get the data item for this position
            final TimeSlot timeSlot = timeSlots.get(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (v == null) {
                v = LayoutInflater.from(getContext()).inflate(R.layout.adapter_business_hours, parent, false);
            }
            // Lookup view for data population

            TextView tv_from =  v.findViewById(R.id.tv_from);
            TextView tv_to =  v.findViewById(R.id.tv_to);
            View viewDivider = v.findViewById(R.id.viewDivider);
            LinearLayout lyDotsLineHours = v.findViewById(R.id.lyDotsLineHours);

            // Populate the data into the template view using the data object
            tv_from.setText(String.format("From: %s", timeSlot.startTime));
            tv_to.setText(String.format("To: %s", timeSlot.endTime));
            viewDivider.setVisibility(timeSlots.size()==1?View.GONE:View.VISIBLE);

            return v;
        }

    }

    private synchronized static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

}
