package com.mualab.org.user.activity.my_profile.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.activity.booking_histories.activity.BookingHisoryActivity;
import com.mualab.org.user.activity.chat.ChatHistoryActivity;
import com.mualab.org.user.activity.my_profile.model.NavigationItem;
import com.mualab.org.user.activity.payment.activity.PaymentHistoryActivity;
import com.mualab.org.user.activity.payment.modle.PaymentHistory;
import com.mualab.org.user.activity.searchBoard.fragment.SearchBoardFragment;
import com.mualab.org.user.application.Mualab;
import com.mualab.org.user.data.local.prefs.Session;
import com.mualab.org.user.dialogs.MyToast;

import java.util.List;


public class NavigationMenuAdapter  extends RecyclerView.Adapter<NavigationMenuAdapter.ViewHolder> {
private Activity context;
private List<NavigationItem> navigationItems;
private DrawerLayout drawer;
private Listener listener;
private String sSelect = "";
// Constructor of the class
public NavigationMenuAdapter(Activity context, List<NavigationItem> navigationItems, DrawerLayout drawer,Listener listener) {
        this.context = context;
        this.drawer = drawer;
        this.navigationItems = navigationItems;
        this.listener=listener;
        }

public interface Listener{
    void OnClick(int pos);
}

    @Override
    public int getItemCount() {
        return navigationItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nav_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int listPosition) {

        final NavigationItem item = navigationItems.get(listPosition);

        holder.tvMenuItemName.setText(item.itemName);
        holder.ivMenuItem.setImageResource(item.itemImg);

       /* if (!sSelect.equals("")){
            if (item.itemName.equals(sSelect)){
                holder.ivMenuItem.setColorFilter(getColor(context, R.color.white));
                holder.tvMenuItemName.setTextColor(getColor(context, R.color.white));
                holder.rlItem.setBackgroundColor(getColor(context, R.color.colorPrimary));
                holder.line.setVisibility(View.INVISIBLE);
            }else {
                holder.line.setVisibility(View.VISIBLE);
                holder.ivMenuItem.setColorFilter(getColor(context, R.color.colorPrimary));
                holder.tvMenuItemName.setTextColor(getColor(context, R.color.dark_grey));
                holder.rlItem.setBackgroundColor(getColor(context, R.color.white));
            }
        }*/

    }

class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    TextView tvMenuItemName;
    AppCompatImageView ivMenuItem;
    RelativeLayout rlItem;
    View line;
    private ViewHolder(View itemView)
    {
        super(itemView);

        tvMenuItemName =  itemView.findViewById(R.id.tvMenuItemName);
        ivMenuItem =  itemView.findViewById(R.id.ivMenuItem);
        rlItem = itemView.findViewById(R.id.rlItem);
        line = itemView.findViewById(R.id.line);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        drawer.closeDrawers();
        sSelect = "";
        NavigationItem item = navigationItems.get(getAdapterPosition());

        switch(getAdapterPosition()) {
            case 0:
                sSelect = item.itemName;
                MyToast.getInstance(context).showDasuAlert("Under development");
                break;

            case 1:
                sSelect = item.itemName;
                context.startActivity(new Intent(context,ChatHistoryActivity.class));
                MyToast.getInstance(context).showDasuAlert("Under development");
                break;

            case 2:
                sSelect = item.itemName;
                context.startActivity(new Intent(context,BookingHisoryActivity.class));
                break;

            case 3:
                sSelect = item.itemName;
                context.startActivity(new Intent(context,PaymentHistoryActivity.class));
                break;

            case 4:
                sSelect = item.itemName;
                MyToast.getInstance(context).showDasuAlert("Under development");
                break;

            case 5:
                sSelect = item.itemName;
                MyToast.getInstance(context).showDasuAlert("Under development");
                break;

            case 6:
                sSelect = item.itemName;
                MyToast.getInstance(context).showDasuAlert("Under development");
                break;

            case 7:
                Session session = new Session(context);
                sSelect = item.itemName;
                SearchBoardFragment.isFavClick = false;
                listener.OnClick(getAdapterPosition());

                break;
            default:
        }

    }


}

}