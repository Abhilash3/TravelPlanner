package com.travelplanner.api;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.travelplanner.vo.Address;
import com.travelplanner.vo.Route;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MapsApis {

    private static final String GOOGLE_DIRECTIONS_BASE = "https://www.google.com/maps/dir/?api=1";
    private final int value;
    private final TimeUnit unit;

    private MapsApis(int value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static MapsApis withLimit(int value, TimeUnit unit) {
        return new MapsApis(value, unit);
    }

    public static AsyncTask<String, Void, Intent> directionsAsync(Address origin, Address destination, List<Address> stops) {
        AsyncTask<String, Void, Route> task = GoogleApis.directionsAsync(origin, destination, stops);

        return new AsyncTask<String, Void, Intent>() {

            @Override
            protected Intent doInBackground(String... strings) {
                try {
                    String url = GOOGLE_DIRECTIONS_BASE + "&origin=" + URLEncoder.encode(origin.description(), "UTF-8") +
                            "&destination=" + URLEncoder.encode(destination.description(), "UTF-8");
                    List<Integer> order = task.get().order();

                    if (!order.isEmpty()) {
                        StringBuilder builder = new StringBuilder("&waypoints=");
                        builder.append(URLEncoder.encode(stops.get(order.get(0)).description(), "UTF-8"));
                        for (int i = 1; i < order.size(); i++) {
                            builder.append("|").append(URLEncoder.encode(stops.get(order.get(i)).description(), "UTF-8"));
                        }
                        url += builder.toString();
                    }

                    return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                } catch (InterruptedException | ExecutionException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public Intent directions(Address origin, Address destination, List<Address> stops)
            throws InterruptedException, ExecutionException, TimeoutException {
        return directionsAsync(origin, destination, stops).get(value, unit);
    }
}
