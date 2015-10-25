package com.ncuculova.taximk;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ncuculova on 2.9.15.
 */
public class CheckTaxiVersion extends AsyncTask<Void, Void, Integer> {

    Integer version = null;
    OnVersionChecked mOnVersionChecked;
    Context mContext;

    public CheckTaxiVersion(Context context) {
        mContext = context;
    }

    public void setOnVersionChecked(OnVersionChecked onVersionChecked) {
        this.mOnVersionChecked = onVersionChecked;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            URL url = new URL(Constants.VERSION_URI);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream input = new BufferedInputStream(connection.getInputStream());
            String content = IOUtils.toString(input);
            JSONObject jsonObject = new JSONObject(content);
            version = jsonObject.getInt("version");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return version;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
        if (mOnVersionChecked != null) {
            mOnVersionChecked.onChecked(integer);
        }
    }
}

interface OnVersionChecked {
    void onChecked(Integer result);
}