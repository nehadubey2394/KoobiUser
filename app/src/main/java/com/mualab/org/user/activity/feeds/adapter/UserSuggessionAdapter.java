package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.hendraanggrian.socialview.Mention;
import com.hendraanggrian.widget.FilteredAdapter;
import com.mualab.org.user.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by dharmraj on 20/3/18.
 */

public class UserSuggessionAdapter extends FilteredAdapter<Mention> {

    public UserSuggessionAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }
    final Filter filter = new SocialFilter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Mention) resultValue).getUsername();
        }
    };

    public UserSuggessionAdapter(Context context) {
        super(context, R.layout.item_user_suggession, 0);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user_suggession, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Mention item = getItem(position);
        if (item != null) {
            holder.tv_name.setText(String.format("%s", item.getDisplayname()));
            holder.tv_user_name.setText(String.format("%s", item.getUsername()));
        }

        if(!TextUtils.isEmpty(item.getAvatar().toString()))
            Picasso.with(holder.iv_profileImage.getContext()).load(item.getAvatar().toString()).into(holder.iv_profileImage);
        else Picasso.with(holder.iv_profileImage.getContext()).load(R.drawable.defoult_user_img).into(holder.iv_profileImage);
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ViewHolder {
        final TextView tv_name;
        final TextView tv_user_name;
        final CircleImageView iv_profileImage;

        ViewHolder(@NonNull View view) {
            this.tv_name = view.findViewById(R.id.tv_name);
            this.tv_user_name = view.findViewById(R.id.tv_user_name);
            this.iv_profileImage = view.findViewById(R.id.iv_profileImage);
            // SetFont.setfontRagular(this.textView, FeedPostActivity.this);
        }
    }

}
