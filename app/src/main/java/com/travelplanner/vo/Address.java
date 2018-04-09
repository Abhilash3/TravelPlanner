package com.travelplanner.vo;

import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

public class Address {

    private final String description;
    private final LatLng coordinates;

    public Address(String description, LatLng coordinates) {
        this.description = description;
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(description, address.description) &&
                Objects.equals(coordinates, address.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, coordinates);
    }

    public String description() {
        return description;
    }

    public LatLng coordinates() {
        return coordinates;
    }
}
