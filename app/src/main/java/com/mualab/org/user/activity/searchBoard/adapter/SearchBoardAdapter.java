package com.mualab.org.user.activity.searchBoard.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.activity.ArtistProfileActivity;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.mualab.org.user.utils.Util;
import com.squareup.picasso.Picasso;

import java.util.List;


public class SearchBoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ArtistsSearchBoard> artistsList;
    private Util utility;
    private boolean showLoader;

    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    // Constructor of the class
    public SearchBoardAdapter(Context context, List<ArtistsSearchBoard> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
        utility = new Util(context);
    }

    @Override
    public int getItemCount() {
        return artistsList.size();
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchboard_item_layout, parent, false);
                return new ViewHolder(view);

            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public int getItemViewType(int position) {
        if(position==artistsList.size()-1){
            return showLoader?VIEWTYPE_LOADER:VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loaderViewHolder = (LoadingViewHolder) viewHolder;
            if (showLoader) {
                loaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                loaderViewHolder.progressBar.setVisibility(View.GONE);
            }
            return;
        }

        final ViewHolder holder = ((ViewHolder) viewHolder);
        final ArtistsSearchBoard item = artistsList.get(position);

        holder.tvArtistName.setText(item.userName);
        holder.rating.setRating(Float.parseFloat(item.ratingCount));

        double d = Double.parseDouble(item.distance);
        item.distance = String.format("%.2f", d);
        holder.tvDistance.setText(String.format("%s miles away", item.distance));

        long rCount = Long.parseLong(item.reviewCount);

        holder.tvRating.setText(String.format("(%s)", utility.roundRatingWithSuffix(rCount)));

        //  holder.ivProfile.setImageResource(item.profilePic);

        if (!item.profileImage.equals("")){
            Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivProfile);
        }else {
            holder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
        }

        if (item.isFav)
            holder.ivFav.setVisibility(View.VISIBLE);
        else
            holder.ivFav.setVisibility(View.GONE);

        holder.tvServices.setText(item.categoryName);
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvDistance,tvServices,tvArtistName,tvRating;
        ImageView ivProfile,ivFav;
        AppCompatButton btnBook;
        RatingBar rating;
        RelativeLayout lyContainer;
        private ViewHolder(View itemView)
        {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivFav = itemView.findViewById(R.id.ivFav);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tvServices = itemView.findViewById(R.id.tvServices);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnBook = itemView.findViewById(R.id.btnBook);
            rating = itemView.findViewById(R.id.rating);
            lyContainer = itemView.findViewById(R.id.lyContainer);

            btnBook.setOnClickListener(this);
          //  ivProfile.setOnClickListener(this);
            lyContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ArtistsSearchBoard item = artistsList.get(getAdapterPosition());
            switch (v.getId()){
                case R.id.btnBook:
                    Intent intent = new Intent(context, BookingActivity.class);
                    intent.putExtra("item",item);
                    context.startActivity(intent);
                    break;

              /*  case R.id.ivProfile:
                    Intent intent2 = new Intent(context, ArtistProfileActivity.class);
                    intent2.putExtra("item",item);
                    //intent2.putExtra("artistId",item._id);
                    context.startActivity(intent2);*/

                case R.id.lyContainer:
                    Intent intent3 = new Intent(context, ArtistProfileActivity.class);
                    intent3.putExtra("item",item);
                    //intent2.putExtra("artistId",item._id);
                    context.startActivity(intent3);
                    break;
            }
        }
    }

}