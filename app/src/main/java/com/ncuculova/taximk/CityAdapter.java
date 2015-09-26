package com.ncuculova.taximk;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by ncuculova on 23.8.15.
 */
public class CityAdapter extends BaseAdapter {

    List<City> cities;
    private LayoutInflater mInflater;
    private Typeface mFont;
    private Context mContext;

    public CityAdapter(Context context) {
        cities = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mFont = FontHelper.getSansCondensed(context, true);
        mContext = context;
    }

    @Override
    public int getCount() {
        return cities.size();
    }

    @Override
    public Object getItem(int position) {
        return cities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return cities.get(position).getId();
    }

    static class TaxiHolder {
        TextView txtName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        City item = cities.get(position);
        TaxiHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new TaxiHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.text1);
            holder.txtName.setTextColor(ContextCompat.getColor(mContext, R.color.taxi_white));
            holder.txtName.setTypeface(mFont);
            convertView.setTag(holder);
        } else {
            holder = (TaxiHolder) convertView.getTag();
        }


        holder.txtName.setText(item.getName());
        return convertView;
    }

    public void setCities(List<City> cities) {
        Locale mk = new Locale("mk_MK");
        final Collator collator = Collator.getInstance(mk);
        Collections.sort(cities, new Comparator<City>() {
            @Override
            public int compare(City me, City you) {
                return collator.compare(me.getName(), you.getName());
            }
        });
        this.cities = cities;
    }

}
