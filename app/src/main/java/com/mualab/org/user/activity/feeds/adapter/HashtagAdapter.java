package com.mualab.org.user.activity.feeds.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import com.hendraanggrian.widget.FilteredAdapter;
import com.mualab.org.user.R;


/**
 * Created by dharmraj on 13/4/18.
 **/

public class HashtagAdapter extends FilteredAdapter<String> {

    final Filter filter = new SocialFilter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((String) resultValue);
        }
    };

    public HashtagAdapter(Context context) {
        super(context, R.layout.item_tag, R.id.textViewName);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tag, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String item = getItem(position);
        if (item != null) {
            holder.textView.setText(String.format("#%s", item));
        }
        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }

    private class ViewHolder {
        final TextView textView;

        ViewHolder(@NonNull View view) {
            this.textView = view.findViewById(R.id.textViewName);
            // SetFont.setfontRagular(this.textView, FeedPostActivity.this);
        }
    }
}
