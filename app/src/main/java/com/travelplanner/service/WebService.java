package com.travelplanner.service;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebService extends AsyncTask<String, Void, String> {

    private final String url;

    public WebService(String url) {
        this.url = url;
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection conn = null;
        final StringBuilder jsonResults = new StringBuilder();

        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults.toString();
    }
}
