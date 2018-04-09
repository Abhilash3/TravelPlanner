package com.travelplanner;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.travelplanner.api.GoogleApis;
import com.travelplanner.api.MapsApis;
import com.travelplanner.common.Constants;
import com.travelplanner.common.Utils;
import com.travelplanner.search.AutoCompleteAdapter;
import com.travelplanner.search.DelayAutoCompleteTextView;
import com.travelplanner.vo.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlanActivity extends Activity {

    private final static int PERMISSION_ACCESS_FINE_LOCATION = 1;

    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        container = findViewById(R.id.stops);

        applySearchBehaviour(findViewById(R.id.origin));
        applySearchBehaviour(findViewById(R.id.destination));

        checkForPermissions();
    }

    public void process(View view) {
        AsyncTask<String, Void, Address> originTask = locationTask(findViewById(R.id.origin));
        AsyncTask<String, Void, Address> destinationTask = locationTask(findViewById(R.id.destination));

        List<AsyncTask<String, Void, Address>> tasks = new ArrayList<>(container.getChildCount());
        for (int i = 0; i < container.getChildCount(); i++) {
            ConstraintLayout parent = (ConstraintLayout) container.getChildAt(i);
            AutoCompleteTextView edit = (AutoCompleteTextView) parent.getChildAt(0);
            tasks.add(locationTask(edit));
        }

        new Thread(() -> {
            List<Address> stops = tasks.stream().flatMap(a -> {
                try {
                    return Stream.of(a.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return Stream.empty();
            }).collect(Collectors.toList());

            try {
                Address origin = originTask.get();
                Address destination = destinationTask.get();
                if (Objects.isNull(destination)) {
                    destination = origin;
                }

                Intent intent = MapsApis.directionsAsync(origin, destination, stops).get();
                if (Objects.nonNull(intent)) {
                    startActivity(intent);
                } else {
                    Utils.toast(this, Constants.Message.NO_DIRECTIONS, Toast.LENGTH_LONG);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                Utils.toast(this, Constants.Message.REQUEST_FAILURE, Toast.LENGTH_LONG);
            }
        }).start();
    }

    private AsyncTask<String, Void, Address> locationTask(AutoCompleteTextView view) {
        return GoogleApis.geocodeAsync(view.getText().toString());
    }

    public void addStop(View view) {
        final ConstraintLayout constraintLayout = new ConstraintLayout(this);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT));
        constraintLayout.setId(constraintLayout.hashCode());

        AutoCompleteTextView edit = new DelayAutoCompleteTextView(this);
        edit.setLayoutParams(new ConstraintLayout.LayoutParams(
                0, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        applySearchBehaviour(edit);
        edit.setId(edit.hashCode());

        Button button = new Button(this);
        button.setLayoutParams(new ConstraintLayout.LayoutParams(
                100, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        button.setText(R.string.remove);
        button.setOnClickListener(v -> container.removeView(constraintLayout));
        button.setId(button.hashCode());

        constraintLayout.addView(edit);
        constraintLayout.addView(button);

        applyHorizontalConstraints(constraintLayout, edit, button);
        container.addView(constraintLayout, 0);
    }

    private void applyHorizontalConstraints(ConstraintLayout parent, View left, Button right) {
        ConstraintSet constraints = new ConstraintSet();
        constraints.clone(parent);

        constraints.connect(left.getId(), ConstraintSet.START, parent.getId(), ConstraintSet.START, 16);
        constraints.connect(left.getId(), ConstraintSet.TOP, parent.getId(), ConstraintSet.TOP, 16);
        constraints.connect(left.getId(), ConstraintSet.END, right.getId(), ConstraintSet.START, 10);

        constraints.connect(right.getId(), ConstraintSet.END, parent.getId(), ConstraintSet.END, 16);
        constraints.connect(right.getId(), ConstraintSet.START, left.getId(), ConstraintSet.END, 10);
        constraints.connect(right.getId(), ConstraintSet.BASELINE, left.getId(), ConstraintSet.BASELINE);

        constraints.setHorizontalChainStyle(left.getId(), ConstraintSet.CHAIN_SPREAD_INSIDE);

        constraints.applyTo(parent);
    }

    private void applySearchBehaviour(final AutoCompleteTextView editView) {
        editView.setAdapter(new AutoCompleteAdapter(this));
        editView.setOnItemClickListener((adapterView, view, position, id) ->
                editView.setText((String) adapterView.getItemAtPosition(position)));
    }

    private void checkForPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                LocationListener listener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        EditText view = PlanActivity.this.findViewById(R.id.origin);
                        runOnUiThread(() -> {
                            String text = Constants.BLANK;
                            try {
                                Address loc = GoogleApis.geocodeAsync(location.getLatitude(), location.getLongitude()).get();
                                if (Objects.nonNull(loc)) {
                                    text = loc.description();
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                                Utils.toast(PlanActivity.this,
                                        Constants.Message.LOCATION_FAILURE, Toast.LENGTH_LONG);
                            }
                            view.setText(text);
                        });
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };

                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkForPermissions();
                }
                break;
        }
    }
}
