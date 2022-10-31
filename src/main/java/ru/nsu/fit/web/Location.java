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

public class Location {
    private String name;
    private String stringInfo;
    private LocationInfo locationInfo;

    public Location() {
    }


    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public String getStringInfoByName(String name) {
        String key;
        try (InputStream keyStream = Main.class.getResourceAsStream("geocoding-key.txt")) {
            if (keyStream == null) {
                System.err.println("Can't find geocoding-key.txt");
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

//        System.out.println("Geocoding key = " + key);

        try {
            HttpRequest request = HttpRequest.
                    newBuilder(new URI("https://graphhopper.com/api/1/geocode?q=" + name + "&locale=de&key=" + key)).
                    GET().
                    timeout(Duration.of(5, SECONDS)).
                    build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            locationInfo = new Gson().fromJson(response.body(), LocationInfo.class);
            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException();
        }

    }
}
