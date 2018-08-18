package com.mualab.org.user.activity.gellery.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.gellery.model.Media;
import com.mualab.org.user.listner.GalleryOnClickListener;

import java.util.List;

/**
 * Created by mindiii on 8/9/17.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private List<Media> mediaList;
    private Context mContext;
    private boolean isSupportMultipal;
    private GalleryOnClickListener listener;

    public GalleryAdapter(List<Media> mediaList, Context mContext, GalleryOnClickListener listener) {
        this.mediaList = mediaList;
        this.mContext = mContext;
        this.listener = listener;
    }

    public void setEnableMultipal(boolean bool){
        isSupportMultipal = bool;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_adapter_view, parent, false);
        ViewHolder holder = new ViewHolder(v);
        setUpClick(v, holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Media media = mediaList.get(position);
        holder.ivSelected.setVisibility(media.isSelected?View.VISIBLE:View.GONE);

        if(isSupportMultipal){

            if(media.isSelected)
                holder.ivSelected.setVisibility(View.VISIBLE);
        }else {
            holder.ivSelected.setVisibility(View.GONE);
        }

     /*   RequestOptions requestOptions =
                new RequestOptions()
                        .placeholder(0)
                        .fallback(0)
                        .centerCrop()
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);*/

        Glide.with(mContext).load(media.uri)
                .placeholder(0).fallback(0).centerCrop()
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.iv_gallery_item);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }


    private void setUpClick(View view, final ViewHolder holder){

        holder.ivSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Media media = mediaList.get(position);
                listener.OnClick(media, position);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                Media media = mediaList.get(position);
                listener.OnClick(media, position);
            }
        });
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView iv_gallery_item, ivSelected;
        public ViewHolder(View itemView) {
            super(itemView);
            iv_gallery_item = itemView.findViewById(R.id.iv_gallery_item);
            ivSelected = itemView.findViewById(R.id.ivSelected);
        }
    }
}
