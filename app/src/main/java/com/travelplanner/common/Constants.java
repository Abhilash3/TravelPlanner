package com.travelplanner.common;

public class Constants {

    public static final String BLANK = "";

    private Constants() {
        throw new RuntimeException();
    }

    public static class Message {

        public static final String NO_DIRECTIONS = "Not able to provide route for options. Please reselect options";
        public static final String AUTOCOMPLETE_FAILURE = "Not able to provide autocomplete options. Please check your internet connection.";
        public static final String LOCATION_FAILURE = "Not able to retrieve your location. Please check your internet connection.";
        public static final String REQUEST_FAILURE = "Not able to complete your request. Please check your internet connection.";

        private Message() {
            throw new RuntimeException();
        }
    }
}
