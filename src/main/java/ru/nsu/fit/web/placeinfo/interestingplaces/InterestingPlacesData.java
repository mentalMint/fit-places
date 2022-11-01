package ru.nsu.fit.web.placeinfo.interestingplaces;

public class InterestingPlacesData {
    public String name;
    public String kind;
    public String xid;
    public Point point;

    public class Point {
        public String lat;
        public String lon;
    }
}
