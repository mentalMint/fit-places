package ru.nsu.fit.web.location;

public class LocationInfo {
    public Place[] hits;
    public class Place {
        public String name;
        public Point point;

        public class Point {
            public String lat;
            public String lng;
        }
    }
}
