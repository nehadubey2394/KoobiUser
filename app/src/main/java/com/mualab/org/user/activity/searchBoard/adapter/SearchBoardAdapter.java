package com.mualab.org.user.activity.searchBoard.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking.BookingActivity;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.util.Utility;
import com.mualab.org.user.model.SearchBoard.ArtistsSearchBoard;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class SearchBoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;
    private ArrayList<ArtistsSearchBoard> artistsList;
    private Utility utility;
    private boolean showLoader;

    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;
    // Constructor of the class
    public SearchBoardAdapter(Activity context, ArrayList<ArtistsSearchBoard> artistsList) {
        this.context = context;
        this.artistsList = artistsList;
        utility = new Utility(context);
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
        /*View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchboard_item_layout, parent, false);
        return new ViewHolder(view);*/
        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.searchboard_item_layout, parent, false);
                ViewHolder viewHolder = new ViewHolder(view);
                return viewHolder;

            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public int getItemViewType(int position) {

        if (position != 0 && position == getItemCount() - 1) {
            return VIEWTYPE_LOADER;
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

        double d = Double.parseDouble(item.distance);
        item.distance = String.format("%.2f", d);
        holder.tvDistance.setText(item.distance+" miles away");

        long rCount = Long.parseLong(item.reviewCount);

        holder.tvRating.setText("("+utility.roundRatingWithSuffix(rCount)+")");

        //  holder.ivProfile.setImageResource(item.profilePic);

        if (!item.profileImage.equals("")){
            Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(holder.ivProfile);
        }
        String services = "";
        if (item.service.size()!=0){
            for (int i=0; i<item.service.size(); i++){
                if (services.equals("")){
                    services = item.service.get(i).title;
                }else {
                    services = services + ", "+  item.service.get(i).title;
                }
            }
        }else {
            services = "NA";
        }

        holder.tvServices.setText(services);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistance,tvServices,tvArtistName,tvRating;
        ImageView ivProfile;
        AppCompatButton btnBook;
        private ViewHolder(View itemView)
        {
            super(itemView);

            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvArtistName = itemView.findViewById(R.id.tvArtistName);
            tvServices = itemView.findViewById(R.id.tvServices);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnBook = itemView.findViewById(R.id.btnBook);

            btnBook.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArtistsSearchBoard item = artistsList.get(getAdapterPosition());
                    Intent intent = new Intent(context, BookingActivity.class);
                    if (getAdapterPosition()==1){
                        intent.putExtra("item",item);
                        intent.putExtra("mParam","1");
                        context.startActivity(intent);
                        //    ((MainActivity)context).addFragment(BookingFragmentMain.newInstance("1",item), true, R.id.fragment_place);
                    }else {
                        intent.putExtra("item",item);
                        intent.putExtra("mParam","");
                        context.startActivity(intent);
                        //    ((MainActivity)context).addFragment(BookingFragmentMain.newInstance("",item), true, R.id.fragment_place);
                    }

                }
            });
        }
    }

}