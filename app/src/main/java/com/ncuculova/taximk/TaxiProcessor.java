package com.ncuculova.taximk;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ncuculova on 23.8.15.
 */
public class TaxiProcessor {

    private static Integer version = null;

    public static Data getData() throws MalformedURLException {
        URL url = new URL(Constants.URI);
        Data data = new Data();
        List<Taxi> taxis = new ArrayList<>();
        Set<City> citySet = new HashSet<>();
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream input = new BufferedInputStream(connection.getInputStream());
            String content = IOUtils.toString(input);
            //JSONObject jsonObject = new JSONObject(content);
            JSONArray dataArray = new JSONArray(content);
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject jsonData = (JSONObject) dataArray.get(i);
                Taxi taxi = new Taxi();
                City city = new City();
                JSONObject jsonPlace = jsonData.getJSONObject("place");
                city.setName(jsonPlace.getString("name"));
                city.setSlug(jsonPlace.getString("slug"));
                city.setId(jsonPlace.getInt("id"));
                citySet.add(city);
                taxi.setName(jsonData.getString("name"));
                taxi.setAddress(jsonData.isNull("address") ? "" : jsonData.getString("address"));
                taxi.setPhone(jsonData.getString("phone"));
                taxi.setPhone2(jsonData.isNull("phone2") ? "" : jsonData.getString("phone2"));
                taxi.setDescription(jsonData.getString("description"));
                taxi.setSlug(jsonData.getString("slug"));
                taxi.setCity(city);
                taxis.add(taxi);
            }

            data.setTaxis(taxis);
            data.setCities(citySet);
            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static Integer getVersion() {
        return version;
    }
}
