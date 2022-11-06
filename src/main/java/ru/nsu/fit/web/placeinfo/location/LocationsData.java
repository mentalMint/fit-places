package ru.nsu.fit.web.placeinfo.location;

public class LocationsData {
    public Place[] hits;
    public class Place {
        public String name;
        public Point point;
        public String country;
        public String city;
        public String street;
        public String housenumber;

        public class Point {
            public String lat;
            public String lng;
        }
    }
}
