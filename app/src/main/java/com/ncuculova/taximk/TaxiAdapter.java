package com.ncuculova.taximk;

import android.content.Context;
import android.graphics.Typeface;
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
public class TaxiAdapter extends BaseAdapter {
    public static enum ViewType {
        MOBILE_MOBILE_ES,
        MOBILE_PHONE_ES,
        PHONE_MOBILE_ES,
        PHONE_PHONE_ES,
        MOBILE_MOBILE_FS,
        MOBILE_PHONE_FS,
        PHONE_MOBILE_FS,
        PHONE_PHONE_FS
    }

    List<Taxi> taxis;
    List<Taxi> filtered;
    List<Taxi> favoriteTaxis;
    boolean isFiltered;
    boolean isFav;
    private Typeface mTfNormal;
    private Typeface mTfBold;
    private Typeface mTfRegular;

    private LayoutInflater mInflater;

    public TaxiAdapter(Context context) {
        taxis = new ArrayList<>();
        filtered = new ArrayList<>();
        favoriteTaxis = new ArrayList<>();
        isFiltered = false;
        mInflater = LayoutInflater.from(context);
        mTfNormal = FontHelper.getSansCondensed(context, false);
        mTfBold = FontHelper.getSansCondensed(context, true);
        mTfRegular = FontHelper.getSansRegular(context);
    }

    @Override
    public int getCount() {
        if (isFiltered) {
            return filtered.size();
        } else if (isFav) {
            return favoriteTaxis.size();
        }
        return taxis.size();
    }

    @Override
    public Object getItem(int position) {
        if (isFiltered) {
            return filtered.get(position);
        } else if (isFav) {
            return favoriteTaxis.get(position);
        }
        return taxis.get(position);
    }

    public Taxi getTaxi(int position) {
        if (isFiltered) {
            return filtered.get(position);
        } else if (isFav) {
            return favoriteTaxis.get(position);
        }
        return taxis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        Taxi taxi = getTaxi(position);
        String phone = taxi.getPhone();
        String phone2 = taxi.getPhone2();
        if (!Taxi.isEmpty(phone) && !Taxi.isEmpty(phone2)) {
            // two numbers
            if (Taxi.phoneIsMobile(phone) && Taxi.phoneIsMobile(phone2)) {
                if (taxi.isFav()) {
                    return ViewType.MOBILE_MOBILE_FS.ordinal();
                } else return ViewType.MOBILE_MOBILE_ES.ordinal();
            }
            if (!Taxi.phoneIsMobile(phone) && Taxi.phoneIsMobile(phone2)) {
                if (taxi.isFav()) {
                    return ViewType.PHONE_MOBILE_FS.ordinal();
                } else return ViewType.PHONE_MOBILE_ES.ordinal();
            }
            if (Taxi.phoneIsMobile(phone) && !Taxi.phoneIsMobile(phone2)) {
                if (taxi.isFav()) {
                    return ViewType.MOBILE_PHONE_FS.ordinal();
                } else return ViewType.MOBILE_PHONE_ES.ordinal();
            }
            if (!Taxi.phoneIsMobile(phone) && !Taxi.phoneIsMobile(phone2)) {
                if (taxi.isFav()) {
                    return ViewType.PHONE_PHONE_FS.ordinal();
                } else return ViewType.PHONE_PHONE_ES.ordinal();
            }
        }
        if (Taxi.phoneIsMobile(phone)) {
            if (taxi.isFav()) {
                return ViewType.MOBILE_MOBILE_FS.ordinal();
            } else return ViewType.MOBILE_MOBILE_ES.ordinal();

        } else {
            if (taxi.isFav()) {
                return ViewType.PHONE_PHONE_FS.ordinal();
            } else return ViewType.PHONE_PHONE_ES.ordinal();
        }
    }

    static class TaxiHolder {
        TextView txtName;
        TextView txtNumber;
        TextView txtNumber2;
        TextView txtAddress;
        TextView txtCity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Taxi item = null;
        if (isFiltered) {
            item = filtered.get(position);
        } else if (isFav) {
            item = favoriteTaxis.get(position);
        } else item = taxis.get(position);
        TaxiHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.taxi_item, null);
            holder = new TaxiHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            holder.txtName.setTypeface(mTfBold);
            holder.txtNumber = (TextView) convertView.findViewById(R.id.txtNumber);
            holder.txtNumber.setTypeface(mTfRegular);
            holder.txtNumber2 = (TextView) convertView.findViewById(R.id.txtNumber2);
            holder.txtNumber2.setTypeface(mTfRegular);
            holder.txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
            holder.txtAddress.setTypeface(mTfNormal);
            holder.txtCity = (TextView) convertView.findViewById(R.id.txtCity);
            holder.txtCity.setTypeface(mTfBold);
            convertView.setTag(holder);
        } else {
            holder = (TaxiHolder) convertView.getTag();
        }

        holder.txtName.setText(item.getName());
        holder.txtNumber.setText(item.getPhone());
        holder.txtNumber2.setText(item.getPhone2());
        holder.txtNumber.setCompoundDrawablesWithIntrinsicBounds(Taxi.phoneIsMobile(item.getPhone())
                ? R.drawable.ic_action_phone_gray : R.drawable.ic_action_phone_start_gray, 0, 0, 0);
        if (!item.getPhone2().isEmpty()) {
            holder.txtNumber2.setCompoundDrawablesWithIntrinsicBounds(Taxi.phoneIsMobile(item.getPhone2())
                    ? R.drawable.ic_action_phone_gray : R.drawable.ic_action_phone_start_gray, 0, 0, 0);
        }else {
            holder.txtNumber2.setCompoundDrawables(null, null, null, null);
        }
        holder.txtCity.setText(item.getCity().getName());
        holder.txtAddress.setText(item.getAddress() != "" ? " - " + item.getAddress() : "");
        return convertView;
    }

    public void setTaxis(List<Taxi> taxis) {
        Locale mk = new Locale("mk_MK");
        final Collator collator = Collator.getInstance(mk);
        Collections.sort(taxis, new Comparator<Taxi>() {
            @Override
            public int compare(Taxi me, Taxi you) {
                return collator.compare(me.getName(), you.getName());
            }
        });
        this.taxis = taxis;
    }

    public List<Taxi> getTaxis() {
        return taxis;
    }

    public void setFilteredTaxis(long id) {
        filtered = new ArrayList<>();
        for (Taxi t : taxis) {
            if (t.getCity().getId().longValue() == id) {
                filtered.add(t);
            }
        }
    }

    public void setFavoriteTaxis() {
        favoriteTaxis = new ArrayList<>();
        for (Taxi t : taxis) {
            if (t.isFav()) {
                favoriteTaxis.add(t);
            }
        }
    }

    public void setFilter() {
        isFiltered = true;
    }

    public void clearFilter() {
        isFiltered = false;
    }

    public void setFavModeOff() {
        isFav = false;
    }

    public void setFavMode() {
        isFav = true;
    }
}
