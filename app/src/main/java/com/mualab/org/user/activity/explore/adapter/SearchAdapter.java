package com.mualab.org.user.activity.explore.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.explore.model.ExSearchTag;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.squareup.picasso.Picasso;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dharmraj on 2/4/18.
 **/

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean showLoader;
    private final int FEED_TYPE = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private Context mContext;
    private List<ExSearchTag> feedItems;
    private Listener listener;

    public interface Listener{
        void onItemClick(ExSearchTag searchTag, int index);
    }

    public SearchAdapter(Context mContext, List<ExSearchTag> feedItems, Listener listener) {
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
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_explore_top, parent, false));
            case VIEW_TYPE_LOADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        ExSearchTag feed = feedItems.get(position);
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

        final ExSearchTag searchTag = feedItems.get(position);
        final Holder h = ((Holder) holder);
        h.tvDesc.setVisibility(View.VISIBLE);
        h.tvHeader.setText(searchTag.title);
        h.tvDesc.setText(searchTag.desc);

        switch (searchTag.type){
            case 0:
            case 1:
                if(!TextUtils.isEmpty(searchTag.imageUrl)){
                    Picasso.with(mContext).load(searchTag.imageUrl).fit().into(h.ivProfile);
                }else Picasso.with(mContext).load(R.drawable.defoult_user_img).into(h.ivProfile);
                break;

            case 2:
                Picasso.with(mContext).load(R.drawable.hag_tag_ico).fit().into(h.ivProfile);
                break;

            case 4:
                h.tvDesc.setVisibility(View.GONE);
                Picasso.with(mContext).load(R.drawable.ic_location_tag).fit().into(h.ivProfile);
                break;
        }
    }

    private class Holder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private CircleImageView ivProfile;
        private TextView tvHeader;
        private TextView tvDesc;

        private Holder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvHeader = itemView.findViewById(R.id.tvHeader);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if(feedItems.size()>pos){
                ExSearchTag searchTag = feedItems.get(pos);
                listener.onItemClick(searchTag, pos);
            }
        }
    }
}
