package ru.nsu.fit.web.utilities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

public class JSONGetter {
    public static String get(String uri) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.
                newBuilder(new URI(uri)).
                GET().
                timeout(Duration.of(5, SECONDS)).
                build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
