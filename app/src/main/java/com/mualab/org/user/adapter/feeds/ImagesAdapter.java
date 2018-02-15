package com.mualab.org.user.adapter.feeds;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.mualab.org.user.R;
import com.mualab.org.user.constants.Constant;
import com.mualab.org.user.model.feeds.AllFeeds;

import java.util.List;

/**
 * Created by mindiii on 9/8/17.
 **/

public class ImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<AllFeeds> feedsArrayList;
    private int type;
    private final int VIEW_FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;

    public ImagesAdapter(Context mContext, List<AllFeeds> feedsArrayList, int type) {
        this.mContext = mContext;
        this.feedsArrayList = feedsArrayList;
        this.type = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==VIEW_TYPE_LOADING){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
            return new LoadingViewHolder(v);

        }else if(viewType == VIEW_FEED_TYPE){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_grid_item, parent, false);
            ViewHolder holder = new ViewHolder(v);
            setupClick(holder);
            return holder;
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return feedsArrayList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_FEED_TYPE;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder h, final int position) {
        //final AllFeeds feeds = feedsArrayList.get(position);
        final AllFeeds feeds = feedsArrayList.get(position);
        if (h instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) h;
            loadingViewHolder.progressBar.setIndeterminate(true);

        }else  if (h instanceof ViewHolder) {

            final ViewHolder holder = (ViewHolder) h;
            if (feeds.feed != null && feeds.feed.size() > 0) {
                String url = (type == Constant.IMAGE_STATE) ? feeds.feed.get(0) : (type == Constant.VIDEO_STATE) ? feeds.feedThumb.get(0) : "";
                if (!TextUtils.isEmpty(url)) {
                    if(position==0){
                        holder.iv_gridFirstPos.setVisibility(View.VISIBLE);
                        holder.iv_grid.setVisibility(View.GONE);
                        Glide.with(holder.iv_gridFirstPos.getContext())
                                .load(url)
                                .placeholder(R.drawable.gallery_placeholder)
                                .into(holder.iv_gridFirstPos);
                    }else {
                        holder.iv_gridFirstPos.setVisibility(View.GONE);
                        holder.iv_grid.setVisibility(View.VISIBLE);

                        Glide.with(holder.iv_grid.getContext())
                                .load(url)
                                .placeholder(R.drawable.gallery_placeholder)
                                .into(holder.iv_grid);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return feedsArrayList == null ? 0 : feedsArrayList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_typeBg;
        RoundedImageView iv_grid, iv_gridFirstPos;
        //ImageView iv_grid;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_grid =  itemView.findViewById(R.id.iv_grid);
            iv_gridFirstPos = itemView.findViewById(R.id.iv_gridFirstPos);
            iv_typeBg = itemView.findViewById(R.id.iv_typeBg);
        }
    }

    private void setupClick(final ViewHolder holder) {
        if(type == Constant.IMAGE_STATE)
            holder.iv_typeBg.setVisibility(View.GONE);
        else holder.iv_typeBg.setVisibility(View.VISIBLE);

    /*    holder.iv_gridFirstPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof MainActivity)
                    ((MainActivity) mContext)
                            .addFragment(GridDetaildFragment.newInstance(
                                    feedsArrayList,
                                    holder.getAdapterPosition()),
                                    true,
                                    R.id.fragment_place);
            }
        });

        holder.iv_grid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext instanceof MainActivity)
                    ((MainActivity) mContext)
                            .addFragment(GridDetaildFragment.newInstance(
                                    feedsArrayList,
                                    holder.getAdapterPosition()),
                                    true,
                                    R.id.fragment_place);
            }
        });*/
    }

}
