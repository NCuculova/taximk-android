package com.example.ncuculova.taxinadica;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

/**
 * Created by ncuculova on 1.9.15.
 */
public class DownloadTaxiService extends IntentService {

    public DownloadTaxiService() {
        super("DownloadTaxiService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Data data = null;
        try {
            data = TaxiProcessor.getData();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        List<Taxi> taxis = data.getTaxis();
        Set<City> cities = data.getCities();

        try {
            DB snappydb = DBFactory.open(getApplicationContext(), "taxi");
            for (int i = 0; i < taxis.size(); i++) {
                Taxi t = taxis.get(i);
                t.setKey("taxi:" + i);
                snappydb.put(t.getKey(), t);
            }

            String[] keys = snappydb.findKeys("taxi:" + taxis.size());
            for (String key : keys) {
                snappydb.del(key);
            }

            int index = 0;
            for (City c : cities) {
                snappydb.put("city:" + index++, c);
            }
            String[] cityKeys = snappydb.findKeys("city:" + cities.size());
            for (String key : cityKeys) {
                snappydb.del(key);
            }
                /*
                 * Creates a new Intent containing a Uri object
                 * BROADCAST_ACTION is a custom Intent action
                 */
            Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } catch (SnappydbException e) {
            e.printStackTrace();
        }

    }
}
