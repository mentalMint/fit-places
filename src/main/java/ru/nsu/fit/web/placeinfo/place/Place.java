package ru.nsu.fit.web.placeinfo.place;

import com.google.gson.Gson;
import ru.nsu.fit.web.utilities.JSONGetter;
import ru.nsu.fit.web.utilities.KeyReader;
import ru.nsu.fit.web.placeinfo.Info;

import java.io.IOException;
import java.net.URISyntaxException;

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
    public void initialize() throws IOException, URISyntaxException, InterruptedException {
        String key = KeyReader.readKey("tripmap-key.txt");

        String json = JSONGetter.get("http://api.opentripmap.com/0.1/ru/places/" +
                "xid/" + placeID +
                "?apikey=" + key);
        placeData = new Gson().fromJson(json, PlaceData.class);

    }
}
