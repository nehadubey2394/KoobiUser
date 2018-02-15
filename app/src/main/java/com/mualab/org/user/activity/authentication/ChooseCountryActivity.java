package com.mualab.org.user.activity.authentication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mualab.org.user.R;
import com.mualab.org.user.model.Country;
import com.mualab.org.user.util.JsonUtils;
import com.mualab.org.user.util.decorator.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseCountryActivity extends AppCompatActivity {

    private List<Country> countryList;
    private CountryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_country);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        countryList = JsonUtils.loadCountries(this);
        adapter = new CountryAdapter();

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view_menu_item, menu);
        MenuItem searchViewItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchViewItem.getActionView();
        //final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        //int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
       /* ImageView v = searchView.findViewById(searchImgId);
        v.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP); */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    private void setResultCountry(Country country){
        Intent resultIntent = new Intent();
        // TODO Add extras or a data URI to this intent as appropriate.
        //resultIntent.putExtra("some_key", "String data");
        resultIntent.putExtra("country", country);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }


    class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {
        private List<Country> tmpList = new ArrayList<>();

        private CountryAdapter(){
            tmpList.addAll(countryList);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_country_code_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Country country = tmpList.get(position);
            holder.tvCountryname.setText(country.country_name);
            holder.tvCountryCode.setText(String.format("+%s", country.phone_code));
            if(!TextUtils.isEmpty(country.country_name2)){
                holder.tvCountrynameEn.setVisibility(View.VISIBLE);
                holder.tvCountrynameEn.setText(country.country_name2);
            }else holder.tvCountrynameEn.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return tmpList.size();
        }

        // Filter Class
        private void filter(String charText) {
            charText = charText.toLowerCase(Locale.getDefault());
            int offsetLength = charText.length();
            if (offsetLength == 0 && tmpList.size()!=countryList.size()) {
                tmpList.clear();
                tmpList.addAll(countryList);

            } else {
                tmpList.clear();
                for (Country country : countryList) {

                    String phoneCode = "";
                    String countryName = "";
                    String code = "";

                    if(country.country_name.equals("Indonesia") || country.country_name.equals("India")){
                        Log.d("in", "in");
                    }

                    if(country.phone_code.length()>=offsetLength)
                        phoneCode = country.phone_code.substring(0, offsetLength);

                    if(country.country_name.length()>=offsetLength)
                        countryName = country.country_name.substring(0, offsetLength);

                    if(country.code.length()>=offsetLength)
                        code = country.code.substring(0, offsetLength);



                    if (countryName.toLowerCase(Locale.getDefault()).contains(charText) ||
                            phoneCode.equals(charText) ||
                            code.equals(charText)) {
                        tmpList.add(country);
                    }
                }
            }
            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
            private TextView tvCountryname, tvCountrynameEn, tvCountryCode;
            private ViewHolder(View itemView) {
                super(itemView);
                tvCountryname =  itemView.findViewById(R.id.tvCountryname);
                tvCountrynameEn =  itemView.findViewById(R.id.tvCountrynameEn);
                tvCountryCode = itemView.findViewById(R.id.tvCountryCode);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Country country = tmpList.get(getAdapterPosition());
                setResultCountry(country);
            }
        }
    }
}
