package ru.nsu.fit.web.placeinfo.interestingplaces;

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

public class InterestingPlaces implements Info {
    private final String  longitude;
    private final String  latitude;
    private InterestingPlacesData[] interestingPlacesData;

    public InterestingPlaces(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public InterestingPlacesData[] getInterestingPlacesData() {
        return interestingPlacesData;
    }

    @Override
    public void initialize() throws IOException {
        String key = KeyReader.readKey("tripmap-key.txt");

        double lon = Double.parseDouble(longitude);
        double lat = Double.parseDouble(latitude);

        double radius = 0.007;
        double lon_min = lon - radius;
        double lat_min = lat - radius;
        double lon_max = lon + radius;
        double lat_max = lat + radius;


        try {
            HttpRequest request = HttpRequest.
                    newBuilder(new URI(
                                    "http://api.opentripmap.com/0.1/ru/places/bbox?" +
                                            "lon_min=" + lon_min +
                                            "&lat_min=" + lat_min +
                                            "&lon_max=" + lon_max +
                                            "&lat_max=" + lat_max +
                                            "&format=json" +
                                            "&limit=15" +
                                            "&apikey=" + key)).
                    GET().
                    timeout(Duration.of(5, SECONDS)).
                    build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            interestingPlacesData = new Gson().fromJson(response.body(), InterestingPlacesData[].class);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException();
        }
    }
}
