package com.mualab.org.user.activity.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.CommentAdapter;
import com.mualab.org.user.data.model.feeds.Feeds;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String> arrayList;
    private LayoutInflater inflter;
    private Listener listener;

    public MenuAdapter(Context context, ArrayList<String> arrayList,Listener listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener=listener;
        inflter = (LayoutInflater.from(context));

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

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_item;
        public ViewHolder(View itemView) {
            super(itemView);
            txt_item=itemView.findViewById(R.id.txt_item);
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
