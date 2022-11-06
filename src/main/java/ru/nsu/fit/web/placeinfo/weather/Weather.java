package ru.nsu.fit.web.placeinfo.weather;

import com.google.gson.Gson;
import ru.nsu.fit.web.utilities.JSONGetter;
import ru.nsu.fit.web.utilities.KeyReader;
import ru.nsu.fit.web.placeinfo.Info;

import java.io.IOException;
import java.net.URISyntaxException;

public class Weather implements Info {
    private final String longitude;
    private final String latitude;
    private WeatherData weatherData;

    public Weather(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void initialize() throws IOException, URISyntaxException, InterruptedException {
        String key = KeyReader.readKey("weather-key.txt");

        String json = JSONGetter.get("http://api.openweathermap.org/data/2.5/weather?lat=" +
                latitude + "&lon=" + longitude + "&appid=" + key);
        weatherData = new Gson().fromJson(json, WeatherData.class);
    }

    public WeatherData getWeatherData() {
        return weatherData;
    }
}
