package com.mualab.org.user.activity.make_booking.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.data.model.SearchBoard.ArtistsSearchBoard;
import com.squareup.picasso.Picasso;


public class BookingFragmentMain extends Fragment implements View.OnClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Context mContext;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private ArtistsSearchBoard item;


    public BookingFragmentMain() {
        // Required empty public constructor
    }

    public static BookingFragmentMain newInstance(String param1, ArtistsSearchBoard item) {
        BookingFragmentMain fragment = new BookingFragmentMain();
        Bundle args = new Bundle();
        args.putString("mParam1", param1);
        args.putSerializable("item", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString("mParam");
            item = (ArtistsSearchBoard) getArguments().getSerializable("item");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_booking_main, container, false);
        initView(rootView);
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    private void initView(View rootView){
        // MainActivity.rlHeader1.setVisibility(View.GONE);
        TextView title_booking = rootView.findViewById(R.id.tvHeaderTitle2);
        TextView tvArtistName = rootView.findViewById(R.id.tvArtistName);
        TextView tvOpeningTime = rootView.findViewById(R.id.tvOpeningTime);
        RatingBar rating = rootView.findViewById(R.id.rating);
        ImageButton ibtnChat2 = rootView.findViewById(R.id.ibtnChat2);
        ImageView ivHeaderUser2 = rootView.findViewById(R.id.ivHeaderUser2);
        ImageView ivHeaderProfile = rootView.findViewById(R.id.ivHeaderProfile);
        title_booking.setText(getString(R.string.title_booking));

        tvArtistName.setText(item.userName);
        if (!item.profileImage.equals(""))
            Picasso.with(mContext).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                    fit().into(ivHeaderProfile);

        tvOpeningTime.setOnClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
      /*  if (mParam1.equals("1")){
            getChildFragmentManager().beginTransaction().replace(R.id.flBookingContainer,
                    BookingFragment1.newInstance("","")).commit();
        }else {
            getChildFragmentManager().beginTransaction().replace(R.id.flBookingContainer,
                    BookingFragment2.newInstance("","")).commit();
        }*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tvOpeningTime :
                break;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // MainActivity.rlHeader1.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //  MainActivity.rlHeader1.setVisibility(View.VISIBLE);
    }
}
