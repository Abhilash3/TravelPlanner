package com.travelplanner.vo;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Objects;

public class Route {

    private final LatLng northEastBound;
    private final LatLng southWestBound;

    private final List<Leg> legs;
    private final List<Integer> order;

    public Route(LatLng northEastBound, LatLng southWestBound, List<Leg> legs, List<Integer> order) {
        this.northEastBound = northEastBound;
        this.southWestBound = southWestBound;
        this.legs = legs;
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Objects.equals(northEastBound, route.northEastBound) &&
                Objects.equals(southWestBound, route.southWestBound) &&
                Objects.equals(legs, route.legs) &&
                Objects.equals(order, route.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(northEastBound, southWestBound, legs, order);
    }

    public LatLng northEastBound() {
        return northEastBound;
    }

    public LatLng southWestBound() {
        return southWestBound;
    }

    public List<Leg> legs() {
        return legs;
    }

    public List<Integer> order() {
        return order;
    }
}
