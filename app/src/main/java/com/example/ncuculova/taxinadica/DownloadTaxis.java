package com.example.ncuculova.taxinadica;

import android.content.Context;
import android.os.AsyncTask;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

/**
 * Created by ncuculova on 23.8.15.
 */
class DownloadTaxis extends AsyncTask<Void, Void, Data>{

    OnDownloadedTaxis mOnDownloadedTaxis;
    Context mContext;

    public DownloadTaxis(Context context) {
        mContext = context;
    }

    public void setmOnDownloadedTaxis(OnDownloadedTaxis mOnDownloadedTaxis) {
        this.mOnDownloadedTaxis = mOnDownloadedTaxis;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Data doInBackground(Void... params) {
        try {
            Data data = TaxiProcessor.getData();
            List<Taxi> taxis = data.getTaxis();
            Set<City> cities = data.getCities();

            try {
                DB snappydb = DBFactory.open(mContext, "taxi");
                for (int i = 0; i < taxis.size() ; i++) {
                    Taxi t = taxis.get(i);
                    t.setKey("taxi:" + i);
                    snappydb.put(t.getKey(), t);
                }

                String [] keys = snappydb.findKeys("taxi:" + taxis.size());
                for(String key : keys){
                    snappydb.del(key);
                }

                int index = 0;
                for (City c : cities){
                    snappydb.put("city:" + index++, c);
                }
                String [] cityKeys = snappydb.findKeys("city:" + cities.size());
                for(String key : cityKeys){
                    snappydb.del(key);
                }

            } catch (SnappydbException e) {
                e.printStackTrace();
            }

            return data;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Data result) {
        super.onPostExecute(result);
        if (mOnDownloadedTaxis != null) {
            mOnDownloadedTaxis.onDownloaded(result);
        }
    }
}


interface OnDownloadedTaxis {
    void onDownloaded(Data result);
}