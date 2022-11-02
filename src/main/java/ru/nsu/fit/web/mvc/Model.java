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
    private ArrayList<Place> places;
    private String placeName;
    private final Object mutex = new Object();

    public enum State {
        NOT_STARTED,
        START,
        LOCATIONS,
        WEATHER,
        INTERESTING_PLACES,
        FINISH,
        LOADING,
        ERROR
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public ArrayList<Place> getPlaces() {
        return places;
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

    public void searchInfo(int placeNumber) throws ExecutionException, InterruptedException {
        LocationsData locationInfo = locations.getLocationsData();
        String longitude = locationInfo.hits[placeNumber].point.lng;
        String latitude = locationInfo.hits[placeNumber].point.lat;

        CompletableFuture<Void> future1 =
                CompletableFuture.supplyAsync(() -> {
                            weather = new Weather(
                                    longitude,
                                    latitude);
                            try {
                                weather.initialize();
                            } catch (IOException | URISyntaxException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            return weather;
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
                state = State.WEATHER;
            }
            notifySubscribers();
        }

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
                }).thenAccept(s -> {
                    places = new ArrayList<>();
                    ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
                    for (int i = 0; i < interestingPlaces.getInterestingPlacesData().length; i++) {
                        String xidi = interestingPlaces.getInterestingPlacesData()[i].xid;
                        Place placei = new Place(xidi);
                        places.add(placei);
                        futures.add(CompletableFuture.supplyAsync(() -> {
                                    try {
                                        placei.initialize();
                                    } catch (IOException | URISyntaxException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return null;
                                }
                        ).handle((a, t) -> {
                            if (t != null) {
                                state = State.ERROR;
                            }
                            return null;
                        }));
                    }

                    for (int i = 0; i < interestingPlaces.getInterestingPlacesData().length; i++) {
                        try {
                            futures.get(i).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        future2.get();
        synchronized (mutex) {
            if (state != State.ERROR) {
                state = State.INTERESTING_PLACES;
            }
            notifySubscribers();
        }


        state = State.FINISH;
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

    public void searchSuitablePlaces() throws ExecutionException, InterruptedException {
        state = State.START;
        notifySubscribers();

        locations = new Locations(convertToHex(placeName));
        CompletableFuture<Void> completableFuture
                = CompletableFuture.runAsync(() -> {
            try {
                locations.initialize();
            } catch (IOException | URISyntaxException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).handle((s, t) -> {
            if (t != null) {
                state = State.ERROR;
            }
            return null;
        });


        state = State.LOADING;
        notifySubscribers();

        completableFuture.get();
        synchronized (mutex) {
            if (state != State.ERROR) {
                state = State.LOCATIONS;
            }
            notifySubscribers();
        }
    }

    @Override
    public void subscribe(Flow.Subscriber<? super Object> subscriber) {
        subscribes.add(subscriber);
    }

    private void notifySubscribers() {
        subscribes.forEach(subscriber -> subscriber.onNext(null));
    }
}
