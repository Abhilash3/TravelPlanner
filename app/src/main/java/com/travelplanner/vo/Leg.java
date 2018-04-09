package com.travelplanner.vo;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Objects;

public class Leg {

    private final Address start;
    private final Address end;

    private final List<LatLng> steps;

    public Leg(Address start, Address end, List<LatLng> steps) {
        this.start = start;
        this.end = end;
        this.steps = steps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Leg leg = (Leg) o;
        return Objects.equals(start, leg.start) &&
                Objects.equals(end, leg.end) &&
                Objects.equals(this.steps, leg.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, steps);
    }

    public Address start() {
        return start;
    }

    public Address end() {
        return end;
    }

    public List<LatLng> steps() {
        return steps;
    }
}
