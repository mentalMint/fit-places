package ru.nsu.fit.web;

import ru.nsu.fit.web.placeinfo.interestingplaces.InterestingPlaces;
import ru.nsu.fit.web.placeinfo.location.Locations;
import ru.nsu.fit.web.placeinfo.location.LocationsData;
import ru.nsu.fit.web.placeinfo.place.Place;
import ru.nsu.fit.web.placeinfo.weather.Weather;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;

public class Model implements Flow.Publisher<Object> {
    private State state = State.NOT_STARTED;
    private LocationsData locationsData = null;
    private Locations locations;
    private Weather weather;
    private InterestingPlaces interestingPlaces;
    private Place place;
    private String placeName;

    public enum State {
        NOT_STARTED,
        START,
        LOCATIONS,
        WEATHER,
        INTERESTING_PLACES,
        CERTAIN_PLACE,
        FINISH,
        LOADING
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    private final ArrayList<Flow.Subscriber<? super Object>> subscribes = new ArrayList<>();

    public State getState() {
        return state;
    }

    public LocationsData getLocationData() {
        return locationsData;
    }

    public Weather getWeather() {
        return weather;
    }

    public InterestingPlaces getInterestingPlaces() {
        return interestingPlaces;
    }

    public Place getPlace() {
        return place;
    }

    public void searchPlaceInfo(int placeNumber) throws ExecutionException, InterruptedException {
        String xid = interestingPlaces.getInterestingPlacesData()[placeNumber].xid;
        CompletableFuture<Place> future1 =
                CompletableFuture.supplyAsync(() -> {
                            Place place = new Place(xid);
                            try {
                                place.initialize();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return place;
                        }
                );

        place = future1.get();
        state = State.CERTAIN_PLACE;
        notifySubscribers();

        state = State.FINISH;
        notifySubscribers();
    }

    public void searchInfo(int placeNumber) throws ExecutionException, InterruptedException {
//        searchWeather(placeNumber);

        CompletableFuture<Weather> future1 =
                CompletableFuture.supplyAsync(() -> {
                            LocationsData locationInfo = locations.getLocationInfo();
                            Weather weather = new Weather(
                                    locationInfo.hits[placeNumber].point.lng,
                                    locationInfo.hits[placeNumber].point.lat);
                            try {
                                weather.initialize();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return weather;
                        }
                );

        weather = future1.get();
        state = State.WEATHER;
        notifySubscribers();

        CompletableFuture<InterestingPlaces> future2 =
                CompletableFuture.supplyAsync(() -> {
                            LocationsData locationInfo = locations.getLocationInfo();
                            InterestingPlaces interestingPlaces = new InterestingPlaces(
                                    locationInfo.hits[placeNumber].point.lng,
                                    locationInfo.hits[placeNumber].point.lat);
                            try {
                                interestingPlaces.initialize();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return interestingPlaces;
                        }
                );

        interestingPlaces = future2.get();
        state = State.INTERESTING_PLACES;
        notifySubscribers();
    }

    private static String convertToHex(String name) {
        String hexRaw = HexFormat.of().formatHex(name.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hexRaw.length(); i += 2) {
            builder.append('%').append(hexRaw, i, i + 2);
        }
        return builder.toString().toUpperCase(Locale.ROOT);
    }

    public void start() throws ExecutionException, InterruptedException {
        state = State.START;
        notifySubscribers();

        locations = new Locations(convertToHex(placeName));
        CompletableFuture<Void> completableFuture
                = CompletableFuture.runAsync(() -> {
            try {
                locations.initialize();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        state = State.LOADING;
        notifySubscribers();
        completableFuture.get();
        locationsData = locations.getLocationInfo();
        state = State.LOCATIONS;
        notifySubscribers();
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Object> subscriber) {
        subscribes.add(subscriber);
    }

    private void notifySubscribers() {
        subscribes.forEach(subscriber -> subscriber.onNext(null));
    }
}
