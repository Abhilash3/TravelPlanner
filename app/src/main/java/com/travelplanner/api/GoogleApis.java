package com.travelplanner.api;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.travelplanner.common.Constants;
import com.travelplanner.common.Utils;
import com.travelplanner.service.WebService;
import com.travelplanner.vo.Address;
import com.travelplanner.vo.Route;
import com.travelplanner.vo.Leg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class GoogleApis {

    private static final int AUTOCOMPLETE_MAX_RESULTS = 5;
    private static final String GOOGLE_API_BASE = "https://maps.googleapis.com/maps/api";
    private static final String PLACES_TYPE_AUTOCOMPLETE = "/place/autocomplete";
    private static final String DIRECTIONS = "/directions";
    private static final String GEOCODE = "/geocode";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyCZJmHTcPm5hF8X3vdf4upbyQ0NWHBIgIU";
    private final int value;
    private final TimeUnit unit;

    private GoogleApis(int value, TimeUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static GoogleApis withLimit(int value, TimeUnit unit) {
        return new GoogleApis(value, unit);
    }

    public static AsyncTask<String, Void, List<String>> placesAutoCompleteAsync(String searchQuery) {
        if (Objects.nonNull(searchQuery) && !Constants.BLANK.equals(searchQuery)) {
            try {
                String url = GOOGLE_API_BASE + PLACES_TYPE_AUTOCOMPLETE + OUT_JSON +
                        "?key=" + API_KEY + "&input=" + URLEncoder.encode(searchQuery, "UTF8");
                return placesAutoCompleteRequest(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return Utils.fakeRequest(Collections::emptyList);
    }

    private static AsyncTask<String, Void, List<String>> placesAutoCompleteRequest(String url) {
        return request(url, response -> {
            List<String> results = Collections.emptyList();
            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONArray predictions = jsonObj.getJSONArray("predictions");

                int limit = predictions.length() > AUTOCOMPLETE_MAX_RESULTS ? AUTOCOMPLETE_MAX_RESULTS : predictions.length();
                results = new ArrayList<>(limit);

                for (int i = 0; i < limit; i++) {
                    results.add(predictions.getJSONObject(i).getString("description"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return results;
        });
    }

    public static AsyncTask<String, Void, Address> geocodeAsync(String searchQuery) {
        if (Objects.nonNull(searchQuery) && !Constants.BLANK.equals(searchQuery)) {
            try {
                return geocodeRequest(GOOGLE_API_BASE + GEOCODE + OUT_JSON +
                        "?key=" + API_KEY + "&address=" + URLEncoder.encode(searchQuery, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return Utils.fakeRequest(() -> null);
    }

    public static AsyncTask<String, Void, Address> geocodeAsync(double latitude, double longitude) {
        return geocodeRequest(GOOGLE_API_BASE + GEOCODE + OUT_JSON +
                "?key=" + API_KEY + "&latlng=" + latitude + ", " + longitude);
    }

    private static AsyncTask<String, Void, Address> geocodeRequest(String url) {
        return request(url, response -> {
            Address address = null;
            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONArray results = jsonObj.getJSONArray("results");

                if (results.length() > 0) {
                    JSONObject result = results.getJSONObject(0);
                    String description = result.getString("formatted_address");
                    JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                    address = new Address(description, new LatLng(
                            location.getDouble("lat"), location.getDouble("lng")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return address;
        });
    }

    public static AsyncTask<String, Void, Route> directionsAsync(
            Address origin, Address destination, List<Address> stops) {
        if (Objects.nonNull(origin) && Objects.nonNull(destination)) {
            try {
                String url = GOOGLE_API_BASE + DIRECTIONS + OUT_JSON +
                        "?key=" + API_KEY + "&origin=" + URLEncoder.encode(origin.description(), "UTF8") +
                        "&destination=" + URLEncoder.encode(destination.description(), "UTF8");
                if (Objects.nonNull(stops) && stops.size() > 0) {
                    StringBuilder wayPoints = new StringBuilder("&waypoints=optimize:true");
                    for (Address stop : stops) {
                        wayPoints.append('|').append(URLEncoder.encode(stop.description(), "UTF8"));
                    }
                    url += wayPoints.toString();
                }

                return directionsRequest(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return Utils.fakeRequest(() -> null);
    }

    private static AsyncTask<String, Void, Route> directionsRequest(String url) {
        return request(url, response -> {
            Route route = null;

            try {
                JSONObject jsonObj = new JSONObject(response);
                JSONArray results = jsonObj.getJSONArray("routes");

                if (results.length() > 0) {
                    JSONObject result = results.getJSONObject(0);

                    JSONObject bounds = result.getJSONObject("bounds");

                    JSONObject northeast = bounds.getJSONObject("northeast");
                    LatLng northEastBound = new LatLng(northeast.getDouble("lat"), northeast.getDouble("lng"));

                    JSONObject southwest = bounds.getJSONObject("southwest");
                    LatLng southWestBound = new LatLng(southwest.getDouble("lat"), southwest.getDouble("lng"));

                    List<Integer> waypointOrder = new ArrayList<>();
                    JSONArray order = result.getJSONArray("waypoint_order");
                    for (int i = 0; i < order.length(); i++) {
                        waypointOrder.add(order.getInt(i));
                    }

                    List<Leg> legs = new ArrayList<>();
                    JSONArray legsJSON = result.getJSONArray("legs");
                    for (int i = 0; i < legsJSON.length(); i++) {

                        JSONObject leg = legsJSON.getJSONObject(i);

                        JSONObject startLocation = leg.getJSONObject("start_location");
                        Address start = new Address(
                                leg.getString("start_address"),
                                new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng")));

                        JSONObject endLocation = leg.getJSONObject("end_location");
                        Address end = new Address(
                                leg.getString("end_address"),
                                new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng")));

                        JSONArray steps = leg.getJSONArray("steps");
                        for (int j = 0; j < steps.length(); j++) {
                            JSONObject step = steps.getJSONObject(j);
                            legs.add(new Leg(start, end, decodePoly(step.getJSONObject("polyline").getString("points"))));
                        }
                    }
                    route = new Route(northEastBound, southWestBound, legs, waypointOrder);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return route;
        });
    }

    private static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }

    private static <E> AsyncTask<String, Void, E> request(String url, Function<String, E> callback) {
        AsyncTask<String, Void, String> task = new WebService(url).execute();

        return new AsyncTask<String, Void, E>() {
            @Override
            protected E doInBackground(String... strings) {
                try {
                    return callback.apply(task.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    public List<String> placesAutoComplete(String searchQuery) throws InterruptedException, ExecutionException, TimeoutException {
        return placesAutoCompleteAsync(searchQuery).get(value, unit);
    }

    public Address geocode(String searchQuery) throws InterruptedException, ExecutionException, TimeoutException {
        return geocodeAsync(searchQuery).get(value, unit);
    }

    public Address geocode(double latitude, double longitude) throws InterruptedException, ExecutionException, TimeoutException {
        return geocodeAsync(latitude, longitude).get(value, unit);
    }

    public Route directions(Address origin, Address destination, List<Address> stops)
            throws InterruptedException, ExecutionException, TimeoutException {
        return directionsAsync(origin, destination, stops).get(value, unit);
    }
}
