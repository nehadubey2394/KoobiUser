package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.model.feeds.Feeds;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by dharmraj on 2/4/18.
 **/

public class ExploreGridViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean showLoader;
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private Context mContext;
    private List<Feeds> feedItems;
    private Listener listener;

    public interface Listener{
        void onFeedClick(Feeds feed, int index, View v);
    }

    public ExploreGridViewAdapter(Context mContext, List<Feeds> feedItems, Listener listener) {
        this.mContext = mContext;
        this.feedItems = feedItems;
        this.listener = listener;
    }

    public void showHideLoading(boolean bool){
        showLoader = bool;
    }

    public void clear(){
        final int size = feedItems.size();
        feedItems.clear();
        notifyItemRangeRemoved(0, size);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case FEED_TYPE:
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_explore_gridview, parent, false));
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        Feeds feed = feedItems.get(position);
        return feed==null?VIEW_TYPE_LOADING: FEED_TYPE;
    }

    @Override
    public int getItemCount() {
        return feedItems.size();
    }

    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) holder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }

        final Feeds feeds = feedItems.get(position);
        final Holder h = ((Holder) holder);
        Picasso.with(mContext).load(feeds.profileImage).resize(200,200).into(h.imageView);
    }

    private static class Holder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);

        }
    }
}
