package ru.nsu.fit.web.mvc;

import ru.nsu.fit.web.placeinfo.interestingplaces.InterestingPlaces;
import ru.nsu.fit.web.placeinfo.location.Locations;
import ru.nsu.fit.web.placeinfo.location.LocationsData;
import ru.nsu.fit.web.placeinfo.place.Place;
import ru.nsu.fit.web.placeinfo.weather.Weather;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;

public class Model implements Flow.Publisher<Object> {
    private State state = State.NOT_STARTED;
    private Locations locations;
    private Weather weather;
    private InterestingPlaces interestingPlaces;
    private Place place;
    private String placeName;
    private final Object mutex = new Object();

    public enum State {
        NOT_STARTED,
        START,
        LOCATIONS,
        WEATHER,
        INTERESTING_PLACES,
        CERTAIN_PLACE,
        FINISH,
        LOADING,
        ERROR
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    private final ArrayList<Flow.Subscriber<? super Object>> subscribes = new ArrayList<>();

    public State getState() {
        return state;
    }

    public Locations getLocations() {
        return locations;
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
        CompletableFuture<Void> future1 =
                CompletableFuture.supplyAsync(() -> {
                            place = new Place(xid);
                            try {
                                place.initialize();
                            } catch (IOException | URISyntaxException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            return place;
                        }
                ).handle((s, t) -> {
                    if (t != null) {
                        state = State.ERROR;
                    }
                    return null;
                });


        future1.get();
        synchronized (mutex) {
            if (state != State.ERROR) {
                state = State.CERTAIN_PLACE;
            }
            notifySubscribers();
        }

        state = State.FINISH;
        notifySubscribers();
    }

    private void initWeather(String longitude, String latitude) {
        weather = new Weather(
                longitude,
                latitude);
        try {
            weather.initialize();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void searchWeatherAndPlacesInfo(int placeNumber) throws ExecutionException, InterruptedException {
        LocationsData locationInfo = locations.getLocationsData();
        String longitude = locationInfo.hits[placeNumber].point.lng;
        String latitude = locationInfo.hits[placeNumber].point.lat;

        CompletableFuture<Void> future1 =
                CompletableFuture.supplyAsync(() -> {
                            initWeather(longitude, latitude);
                            return null;
                        }
                ).handle((s, t) -> {
                    handleError(t, State.WEATHER);
                    return null;
                });

        future1.join();

        CompletableFuture<Void> future2 =
                CompletableFuture.supplyAsync(() -> {
                            interestingPlaces = new InterestingPlaces(
                                    longitude,
                                    latitude);
                            try {
                                interestingPlaces.initialize();
                            } catch (IOException | InterruptedException | URISyntaxException e) {
                                throw new RuntimeException(e);
                            }
                            return interestingPlaces;
                        }
                ).handle((s, t) -> {
                    if (t != null) {
                        state = State.ERROR;
                    }
                    return null;
                });

        future2.get();
        synchronized (mutex) {
            if (state != State.ERROR) {
                state = State.INTERESTING_PLACES;
            }
            notifySubscribers();
        }
    }

    private static String convertToHex(String name) {
        String hexRaw = HexFormat.of().formatHex(name.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hexRaw.length(); i += 2) {
            builder.append('%').append(hexRaw, i, i + 2);
        }
        return builder.toString().toUpperCase(Locale.ROOT);
    }

    private void initLocations() {
        try {
            locations.initialize();
        } catch (IOException | URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleError(Throwable t, State newState) {
        if (t != null) {
            synchronized (mutex) {
                state = State.ERROR;
                notifySubscribers();
            }
        } else {
            synchronized (mutex) {
                System.err.println("here");

                state = newState;
                notifySubscribers();
            }
        }
    }

    public void searchSuitablePlaces() throws ExecutionException, InterruptedException {
        state = State.START;
        notifySubscribers();

        locations = new Locations(convertToHex(placeName));
        CompletableFuture<Void> completableFuture
                = CompletableFuture.runAsync(this::initLocations).handle((s, t) -> {
            handleError(t, State.LOCATIONS);
            return null;
        });

        synchronized (mutex) {
            if (state == State.START) {
                state = State.LOADING;
                notifySubscribers();
            }
        }

        completableFuture.join();
//        synchronized (mutex) {
//            if (state != State.ERROR) {
//                state = State.LOCATIONS;
//            }
//            notifySubscribers();
//        }
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Object> subscriber) {
        subscribes.add(subscriber);
    }

    private void notifySubscribers() {
        subscribes.forEach(subscriber -> subscriber.onNext(null));
    }
}
