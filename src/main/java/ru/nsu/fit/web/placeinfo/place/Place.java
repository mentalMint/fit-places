package ru.nsu.fit.web.placeinfo.place;

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

public class Place implements Info {
    private final String placeID;
    private PlaceData placeData;

    public Place(String placeID) {
        this.placeID = placeID;
    }

    public PlaceData getPlaceData() {
        return placeData;
    }

    @Override
    public void initialize() throws IOException {
        String key = KeyReader.readKey("tripmap-key.txt");

        try {
            HttpRequest request = HttpRequest.
                    newBuilder(new URI(
                            "http://api.opentripmap.com/0.1/ru/places/" +
                                    "xid/" + placeID +
                                    "?apikey=" + key)).
                    GET().
                    timeout(Duration.of(5, SECONDS)).
                    build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            placeData = new Gson().fromJson(response.body(), PlaceData.class);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException();
        }
    }
}
