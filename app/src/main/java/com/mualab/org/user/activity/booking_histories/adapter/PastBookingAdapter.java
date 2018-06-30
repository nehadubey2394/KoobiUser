package com.mualab.org.user.activity.booking_histories.adapter;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.activity.BookingDetailActivity;
import com.mualab.org.user.activity.booking_histories.model.BookingHistory;
import com.mualab.org.user.activity.feeds.adapter.LoadingViewHolder;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.data.model.User;
import com.mualab.org.user.data.remote.HttpResponceListner;
import com.mualab.org.user.data.remote.HttpTask;
import com.mualab.org.user.dialogs.MyToast;
import com.mualab.org.user.dialogs.NoConnectionDialog;
import com.mualab.org.user.dialogs.Progress;
import com.mualab.org.user.utils.ConnectionDetector;
import com.mualab.org.user.utils.Helper;
import com.mualab.org.user.utils.KeyboardUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PastBookingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity context;
    private List<BookingHistory> pastBookings;
    private boolean showLoader;
    private  final int VIEWTYPE_ITEM = 1;
    private  final int VIEWTYPE_LOADER = 2;

    // Constructor of the class
    public PastBookingAdapter(Activity context, List<BookingHistory> pastBookings) {
        this.context = context;
        this.pastBookings = pastBookings;
    }

    @Override
    public int getItemCount() {
        return pastBookings.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==pastBookings.size()-1){
            return showLoader?VIEWTYPE_LOADER:VIEWTYPE_ITEM;
        }
        return VIEWTYPE_ITEM;
    }

    public void showLoading(boolean status) {
        showLoader = status;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist_staff_item_layout, parent, false);
        // return new ViewHolder(view);
        View view;
        switch (viewType) {
            case VIEWTYPE_ITEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_past_booking, parent, false);
                return new ViewHolder(view);

            case VIEWTYPE_LOADER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more_view, parent, false);
                return new LoadingViewHolder(view);
        }
        return null;

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

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
        final BookingHistory item = pastBookings.get(position);

        holder.tvtName.setText(item.userName);
        holder.tvDate.setText(item.bookingDate);
        holder.tvDate.setText(changeDateFormate(item.bookingDate));
        holder.tvPrice.setText("£"+item.totalPrice);
        holder.rating.setRating(Float.parseFloat(item.userRating));

        if (!item.isExpand){
            holder.ivDropDown.setRotation(360);
            holder.ll_show_review.setVisibility(View.GONE);
            holder.ll_rating_review.setVisibility(View.GONE);
        }
        if (!item.reviewByUser.equals(""))
            holder.tvReviewByUser.setText(item.reviewByUser);
        else
            holder.tvReviewByUser.setText("NA");

        if (!item.reviewByArtist.equals(""))
            holder.tvReviewByArtist.setText(item.reviewByArtist);
        else
            holder.tvReviewByArtist.setText("NA");

        if (item.reviewByUser.equals("")) {
            holder.userRating.setRating(0);
            holder.etComment.setText("");
            holder.btnReview.setVisibility(View.VISIBLE);
            holder.rating.setVisibility(View.GONE);
            holder.tvtName.setTextColor(context.getResources().getColor(R.color.colorPrimary));
            holder.lineView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            if (!item.profileImage.equals("")){
                Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                        fit().into(holder.ivProfile);
            }else
                holder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
            holder.ivProfile.setBackground(context.getResources().getDrawable(R.drawable.bg_pink_circle_profile));

        }
        else {
            holder.btnReview.setVisibility(View.GONE);
            holder.rating.setVisibility(View.VISIBLE);
            holder.tvtName.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.lineView.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            if (!item.profileImage.equals("")){
                Picasso.with(context).load(item.profileImage).placeholder(R.drawable.defoult_user_img).
                        fit().into(holder.ivProfile);
            }else {
                holder.ivProfile.setImageDrawable(context.getResources().getDrawable(R.drawable.defoult_user_img));
            }
            holder.ivProfile.setBackground(context.getResources().getDrawable(R.drawable.bg_blue_circle_profile));

        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView tvtName,tvDate,tvPrice,tvReviewByUser,tvReviewByArtist;
        View lineView;
        ImageView ivProfile,ivChat,ivDropDown;
        AppCompatButton btnReview,btnSubmit,btnRebook;
        RelativeLayout rlDropDown,rlContainer;
        AppCompatRatingBar rating,userRating;
        LinearLayout ll_rating_review,ll_show_review,linear;
        CardView mainCard;
        EditText etComment;

        private ViewHolder(View itemView)
        {
            super(itemView);
            lineView = itemView.findViewById(R.id.lineView);
            linear = itemView.findViewById(R.id.linear);
            mainCard = itemView.findViewById(R.id.mainCard);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ivChat = itemView.findViewById(R.id.ivChat);
            tvtName = itemView.findViewById(R.id.tvtName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvReviewByArtist = itemView.findViewById(R.id.tvReviewByArtist);
            tvReviewByUser = itemView.findViewById(R.id.tvReviewByUser);
            btnReview = itemView.findViewById(R.id.btnReview);
            rlDropDown = itemView.findViewById(R.id.rlDropDown);
            ivDropDown = itemView.findViewById(R.id.ivDropDown);
            rlContainer = itemView.findViewById(R.id.rlContainer);
            rating = itemView.findViewById(R.id.rating);
            userRating = itemView.findViewById(R.id.userRating);
            ll_rating_review = itemView.findViewById(R.id.ll_rating_review);
            ll_show_review = itemView.findViewById(R.id.ll_show_review);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
            btnRebook = itemView.findViewById(R.id.btnRebook);
            etComment = itemView.findViewById(R.id.etComment);

            rlDropDown.setOnClickListener(this);
            btnReview.setOnClickListener(this);
            btnSubmit.setOnClickListener(this);
            btnRebook.setOnClickListener(this);
            rlContainer.setOnClickListener(this);

//            userRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
//                @Override
//                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
//                    userRating.setRating(rating);
//                }
//            });


        }

        @Override
        public void onClick(View view) {
            long mLastClickTime = 0;
            if (SystemClock.elapsedRealtime() - mLastClickTime < 900) {
                return;
            }
            final BookingHistory item = pastBookings.get(getAdapterPosition());
            switch (view.getId()){

                case R.id.rlDropDown:
                    if (item.reviewByUser.equals("")) {
                        if (item.isExpand){
                            item.isExpand = false;
                            ivDropDown.setRotation(360);
                            ll_rating_review.setVisibility(View.GONE);
                        }else {
                            item.isExpand = true;
                            ivDropDown.setRotation(180);
                            ll_rating_review.setVisibility(View.VISIBLE);
                            slideToBottom(mainCard);
                            //ll_rating_review.setAlpha(0.0f);
                            //ll_rating_review.animate().alphaBy(1.0f).translationY(0);
                        }
                    }else {
                        if (item.isExpand){
                            item.isExpand = false;
                            ivDropDown.setRotation(360);
                            ll_show_review.setVisibility(View.GONE);
                        }else {
                            item.isExpand = true;
                            ivDropDown.setRotation(180);
                            ll_show_review.setVisibility(View.VISIBLE);
                            slideToBottom(mainCard);
                            //   ll_show_review.setAlpha(0.0f);
                            //  ll_show_review.animate().alphaBy(1.0f).translationY(0);

                        }
                    }

                    break;

                case R.id.btnReview:
                    ll_show_review.setVisibility(View.GONE);
                    if (item.reviewByUser.equals("")) {
                        if (item.isExpand){
                            item.isExpand = false;
                            ll_rating_review.setVisibility(View.GONE);
                            ivDropDown.setRotation(360);
                        }else {
                            item.isExpand = true;
                            ll_rating_review.setVisibility(View.VISIBLE);
                            ivDropDown.setRotation(180);
                            slideToBottom(mainCard);
                            // ll_rating_review.animate().alphaBy(1.0f).translationY(0);
                        }
                    }

                    break;

                case R.id.btnSubmit:
                    final BookingHistory history = pastBookings.get(getAdapterPosition());
                    //  String sReviewByArtist = tvReviewByArtist.getText().toString().trim();
                    String sReviewByUser = etComment.getText().toString().trim();
                    float rating = userRating.getRating();
                    if (!sReviewByUser.equals("")){
                        if (rating!=0){
                            KeyboardUtil.hideKeyboard(etComment,context);
                            history.reviewByUser = sReviewByUser;
                            history.userRating = String.valueOf(rating);
                            apiForGiveReview(history);
                        }else {
                            MyToast.getInstance(context).showDasuAlert("Please give rating");
                        }
                    }else {
                        MyToast.getInstance(context).showDasuAlert("Please enter some text");
                    }

                    break;

                case R.id.btnRebook:
                    MyToast.getInstance(context).showDasuAlert("Under developement");
//                    Intent intent = new Intent(context, BookingActivity.class);
//                    intent.putExtra("item",item);
//                    context.startActivity(intent);
                    break;

                case R.id.rlContainer:
                    final BookingHistory item2 = pastBookings.get(getAdapterPosition());
                    Intent intent = new Intent(context, BookingDetailActivity.class);
                    intent.putExtra("bookingId",  String.valueOf(item2._id));
                    intent.putExtra("artistName",  item2.userName);
                    intent.putExtra("artistProfile",  item2.profileImage);
                    context.startActivityForResult(intent, 2);
                    break;
            }

        }
    }

    private String changeDateFormate(String sDate){
        SimpleDateFormat inputDf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputtDf = new SimpleDateFormat("dd/MM/yyyy");
        Date formatedDate = null;
        String date = "";
        try {
            formatedDate = inputDf.parse(sDate);
            date =  outputtDf.format(formatedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    // To animate view slide out from top to bottom
    private void slideToBottom(View v){
      /*  TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);*/
        Animation a = AnimationUtils.loadAnimation(context,R.anim.item_anim_fall_down);
        if(a != null){
            a.reset();
            if(v != null){
                v.clearAnimation();
                v.startAnimation(a);
            }
        }

    }

    // To animate view slide out from bottom to top
    public void slideToTop(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,-view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    private void apiForGiveReview(final BookingHistory bookingHistory){
        Session session = Mualab.getInstance().getSessionManager();
        final User user = session.getUser();

        if (!ConnectionDetector.isConnected()) {
            new NoConnectionDialog(context, new NoConnectionDialog.Listner() {
                @Override
                public void onNetworkChange(Dialog dialog, boolean isConnected) {
                    if(isConnected){
                        dialog.dismiss();
                        apiForGiveReview(bookingHistory);
                    }
                }
            }).show();
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(user.id));
        params.put("artistId", bookingHistory.artistId);
        params.put("bookingId", String.valueOf(bookingHistory._id));
        params.put("rating", bookingHistory.userRating);
        params.put("reviewByUser", bookingHistory.reviewByUser);
        params.put("reviewByArtist", "");

        HttpTask task = new HttpTask(new HttpTask.Builder(context, "bookingReviewRating", new HttpResponceListner.Listener() {
            @Override
            public void onResponse(String response, String apiName) {
                try {
                    JSONObject js = new JSONObject(response);
                    String status = js.getString("status");
                    String message = js.getString("message");

                    if (status.equalsIgnoreCase("success")) {
                        for (BookingHistory pastBooking : pastBookings){
                            pastBooking.isExpand = false;
                        }

                        notifyDataSetChanged();
                    }else {
                        MyToast.getInstance(context).showDasuAlert(message);
                    }
                    //  showToast(message);
                } catch (Exception e) {
                    Progress.hide(context);
                    e.printStackTrace();
                }
            }

            @Override
            public void ErrorListener(VolleyError error) {
                try{
                    Helper helper = new Helper();
                    if (helper.error_Messages(error).contains("Session")){
                        Mualab.getInstance().getSessionManager().logout();
                        // MyToast.getInstance(BookingActivity.this).showDasuAlert(helper.error_Messages(error));
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }


            }})
                .setAuthToken(user.authToken)
                .setProgress(true)
                .setBody(params, HttpTask.ContentType.APPLICATION_JSON));
        //.setBody(params, "application/x-www-form-urlencoded"));

        task.execute(this.getClass().getName());
    }

}