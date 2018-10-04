package com.mualab.org.user.activity.chat.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String> arrayList;
    private Listener listener;

    public MenuAdapter(Context context, ArrayList<String> arrayList,Listener listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener=listener;

    }
    public interface Listener{
        void onMenuClick(int pos);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.popup_adapter, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txt_item.setText(arrayList.get(position));
        if (position==arrayList.size()-1){
            holder.line.setVisibility(View.GONE);
            holder.line2.setVisibility(View.VISIBLE);
        }else {
            holder.line.setVisibility(View.VISIBLE);
            holder.line2.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_item;
        View line,line2;
        public ViewHolder(View itemView) {
            super(itemView);
            txt_item=itemView.findViewById(R.id.txt_item);
            line=itemView.findViewById(R.id.line);
            line2=itemView.findViewById(R.id.line2);
            txt_item.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.txt_item:
                    listener.onMenuClick(getAdapterPosition());
                    break;
            }

        }
    }
}
