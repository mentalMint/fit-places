package ru.nsu.fit.web.placeinfo.weather;

public class WeatherData {
    public Weather[] weather;
    public class Weather {
        public String main;
        public String description;
    }
}
