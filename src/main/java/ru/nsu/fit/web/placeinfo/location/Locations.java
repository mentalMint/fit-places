package ru.nsu.fit.web.placeinfo.location;

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

public class Locations implements Info {
    private final String name;
    private LocationsData locationInfo;

    public Locations(String name) {
        this.name = name;
    }

    public void initialize() throws IOException {
        String key = KeyReader.readKey("geocoding-key.txt");

//        System.out.println("Geocoding key = " + key);

        try {
            HttpRequest request = HttpRequest.
                    newBuilder(new URI("https://graphhopper.com/api/1/geocode?q=" + name + "&locale=ru&limit=8&key=" + key)).
                    GET().
                    timeout(Duration.of(5, SECONDS)).
                    build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            locationInfo = new Gson().fromJson(response.body(), LocationsData.class);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException();
        }

    }

    public LocationsData getLocationInfo() {
        return locationInfo;
    }

}
