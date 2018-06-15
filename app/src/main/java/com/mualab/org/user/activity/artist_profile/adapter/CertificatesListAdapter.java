package com.mualab.org.user.activity.artist_profile.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.artist_profile.listner.OnCertificateClickListener;
import com.mualab.org.user.activity.artist_profile.model.Certificate;
import com.squareup.picasso.Picasso;

import java.util.List;


public class CertificatesListAdapter extends RecyclerView.Adapter<CertificatesListAdapter.ViewHolder> {
    private Context context;
    private List<Certificate> certificateList;
    private OnCertificateClickListener certificateClickListener = null;
    // Constructor of the class
    public CertificatesListAdapter(Context context, List<Certificate> certificateList) {
        this.context = context;
        this.certificateList = certificateList;
    }

    public void setListner(OnCertificateClickListener certificateClickListener){
        this.certificateClickListener = certificateClickListener;
    }
    // get the size of the list
    @Override
    public int getItemCount() {
        return certificateList.size();
    }


    // specify the row layout file and click for each row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate_list, parent, false);
        return new ViewHolder(view);
    }

    // load data in each row element
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int listPosition) {

        Certificate certificate =  certificateList.get(listPosition);
        if (certificate.status==0) {
            holder.tvStatus.setText("Under Review");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.text_color_yellow));
        }
        else {
            holder.tvStatus.setText("Varified");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.text_color_green));

        }

        if (!certificate.certificateImage.equals("")){
            Picasso.with(context).load(certificate.certificateImage).placeholder(R.drawable.gallery_placeholder).fit().into(holder.ivCertificate);
        }else {
            holder.ivCertificate.setImageDrawable(context.getResources().getDrawable(R.drawable.gallery_placeholder));
        }

    }

    // Static inner class to initialize the views of rows
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView ivCertificate;
        private TextView tvStatus;
        private ViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            tvStatus =  itemView.findViewById(R.id.tvStatus);
            ivCertificate =  itemView.findViewById(R.id.ivCertificate);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                default:
                    if (certificateClickListener!=null){
                        certificateClickListener.onCertificateClick(getAdapterPosition());
                    }
                    break;
            }
        }
    }

}
