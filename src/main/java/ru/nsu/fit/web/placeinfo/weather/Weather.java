package ru.nsu.fit.web.placeinfo.weather;

import com.google.gson.Gson;
import ru.nsu.fit.web.KeyReader;
import ru.nsu.fit.web.placeinfo.Info;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Weather implements Info {
    private final String longitude;
    private final String latitude;
    private WeatherData weatherData;

    public Weather(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void initialize() throws IOException {
        String key = KeyReader.readKey("weather-key.txt");

//        System.out.println("Weather key = " + key);

        try {
            HttpRequest request = HttpRequest.
                    newBuilder(new URI("http://api.openweathermap.org/data/2.5/weather?lat=" +
                            latitude + "&lon=" + longitude + "&appid=" + key)).
                    GET().
                    timeout(Duration.of(5, SECONDS)).
                    build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            weatherData = new Gson().fromJson(response.body(), WeatherData.class);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException();
        }
    }

    public WeatherData getWeatherData() {
        return weatherData;
    }
}
