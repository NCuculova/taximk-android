package com.example.ncuculova.taxinadica;

import android.content.Context;
import android.os.AsyncTask;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ncuculova on 23.8.15.
 */
public class FetchTaxis extends AsyncTask<Void, Void, Data> {

    Context mContext;
    OnFetchedTaxis mOnFetchedTaxis;

    public FetchTaxis(Context context) {
        mContext = context;
    }

    public void setmOnFetchedTaxis(OnFetchedTaxis mOnFetchedTaxis) {
        this.mOnFetchedTaxis = mOnFetchedTaxis;
    }

    @Override
    protected Data doInBackground(Void... params) {
        List<Taxi> taxis = new ArrayList<>();
        Set<City> cities = new HashSet<>();
        try {
            DB snappydb = DBFactory.open(mContext, "taxi");
            String[] taxisKeys = snappydb.findKeys("taxi:");
            for (int i = 0; i < taxisKeys.length; i++) {
                taxis.add(snappydb.getObject(taxisKeys[i], Taxi.class));
            }
            String[] citiesKeys = snappydb.findKeys("city:");
            for (int i = 0; i < citiesKeys.length; i++) {
                cities.add(snappydb.getObject(citiesKeys[i], City.class));
            }

            snappydb.close();
            Data data = new Data();
            data.setTaxis(taxis);
            data.setCities(cities);

            return data;
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Data result) {
        super.onPostExecute(result);
        if (mOnFetchedTaxis != null) {
            mOnFetchedTaxis.onFetched(result);
        }
    }
}

interface OnFetchedTaxis {
    void onFetched(Data data);
}