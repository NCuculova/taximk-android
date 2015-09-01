package com.example.ncuculova.taxinadica;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by ncuculova on 23.8.15.
 */
public class CityAdapter extends BaseAdapter{

    List<City> cities;
    private LayoutInflater mInflater;

    public CityAdapter(Context context) {
        cities = new ArrayList<>();
        mInflater =LayoutInflater.from(context);
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
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
            holder = new TaxiHolder();
            holder.txtName = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (TaxiHolder) convertView.getTag();
        }

        holder.txtName.setText(item.getName());
        return convertView;
    }

    public void setCities(List<City> cities) {
        Collections.sort(cities);
        this.cities = cities;
    }

}
