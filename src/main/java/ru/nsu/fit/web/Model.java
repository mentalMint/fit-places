package ru.nsu.fit.web;

import ru.nsu.fit.web.location.Location;
import ru.nsu.fit.web.location.LocationInfo;
import ru.nsu.fit.web.weather.Weather;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;

public class Model implements Flow.Publisher<Object>{
    private State state = State.NOT_STARTED;
    private LocationInfo locationInfo = null;
    private Location location;
    private Weather weather;
    public enum State {
        NOT_STARTED,
        START,
        LOCATIONS,
        WEATHER
    }
    private final ArrayList<Flow.Subscriber<? super Object>> subscribes = new ArrayList<>();

    public State getState() {
        return state;
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public Weather getWeather() {
        return weather;
    }

    public void searchWeather(int placeNumber) throws ExecutionException, InterruptedException {
        CompletableFuture<Weather> completableFuture2 =
                CompletableFuture.supplyAsync(() -> {
                            LocationInfo locationInfo = location.getLocationInfo();
                            Weather weather = new Weather(locationInfo.hits[placeNumber].point.lng,
                                    locationInfo.hits[placeNumber].point.lat);
                            weather.initialize();
                            return weather;
                        }
                );

        weather = completableFuture2.get();
        state = State.WEATHER;
        notifySubscribers();
    }

    public void start(String placeName) throws ExecutionException, InterruptedException {
        state = State.START;
        location = new Location(placeName);
        CompletableFuture<Void> completableFuture
                = CompletableFuture.runAsync(location::initialize);
        notifySubscribers();
        completableFuture.get();
        locationInfo = location.getLocationInfo();
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
