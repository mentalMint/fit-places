package ru.nsu.fit.web;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class Weather {
    private final String  longitude;
    private final String  latitude;
    private WeatherData weatherData;

    public Weather(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getStringInfo() {
        String key;
        try (InputStream keyStream = Main.class.getResourceAsStream("weather-key.txt")) {
            if (keyStream == null) {
                System.err.println("Can't find weather-key.txt");
                throw new NullPointerException();
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = keyStream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            key = result.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException();
        }

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
            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException();
        }
    }

    public WeatherData getWeatherData() {
        return weatherData;
    }
}
