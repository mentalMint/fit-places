package ru.nsu.fit.web;

public class LocationInfo {
    public Place[] hits;
    public class Place {
        public Point point;

        public class Point {
            public String lat;
            public String lng;
        }
    }
}
