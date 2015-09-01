package com.example.ncuculova.taxinadica;

import android.content.Context;
import android.os.AsyncTask;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

/**
 * Created by ncuculova on 26.8.15.
 */
public class EditTaxis extends AsyncTask<Taxi, Void, Void> {

    Context mContext;

    public EditTaxis(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Taxi... params) {
        try {
            DB snappydb = DBFactory.open(mContext, "taxi");
            Taxi taxi = params[0];
            snappydb.put(taxi.getKey(), taxi);
            System.out.println("Favourite: " + taxi.getName());
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return null;
    }
}
