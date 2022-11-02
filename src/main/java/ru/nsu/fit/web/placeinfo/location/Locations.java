package ru.nsu.fit.web.placeinfo.location;

import com.google.gson.Gson;
import ru.nsu.fit.web.utilities.JSONGetter;
import ru.nsu.fit.web.utilities.KeyReader;
import ru.nsu.fit.web.placeinfo.Info;

import java.io.IOException;
import java.net.URISyntaxException;

public class Locations implements Info {
    private final String name;
    private LocationsData locationsData;

    public Locations(String name) {
        this.name = name;
    }

    public void initialize() throws IOException, URISyntaxException, InterruptedException {
        String key = KeyReader.readKey("geocoding-key.txt");

        String json = JSONGetter.get("https://graphhopper.com/api/1/geocode?q=" +
                name + "&locale=ru&limit=8&key=" + key);
        locationsData = new Gson().fromJson(json, LocationsData.class);
    }

    public LocationsData getLocationsData() {
        return locationsData;
    }

}
