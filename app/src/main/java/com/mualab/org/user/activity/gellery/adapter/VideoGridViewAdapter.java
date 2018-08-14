package com.mualab.org.user.activity.gellery.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.activity.gellery.model.Media;
import com.mualab.org.user.data.model.feeds.Feeds;
import com.squareup.picasso.Picasso;

import java.util.List;


public class VideoGridViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Media> albumList;
    private Listener listener;

    public interface Listener{
        void onViewClick(Media media, int index);
    }

    public void setListener(Listener listener){
        this.listener = listener;

    }

    public VideoGridViewAdapter(Context mContext, List<Media> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    public void clear(){
        final int size = albumList.size();
        albumList.clear();
        notifyItemRangeRemoved(0, size);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_vediolist_gridview, parent, false));
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        final Media media = albumList.get(position);
        final Holder h = ((Holder) holder);

        h.videoIcon.setVisibility(View.VISIBLE);

        if (media.thumbImage!=null){
            h.imageView.setImageBitmap(media.thumbImage);
        }else
            h.imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.gallery_placeholder));

    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imageView, videoIcon;

        private Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            videoIcon = itemView.findViewById(R.id.videoIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Media media = albumList.get(pos);
            if (listener!=null)
                listener.onViewClick(media, pos);
        }
    }
}
