package ru.nsu.fit.web.mvc;

import ru.nsu.fit.web.placeinfo.interestingplaces.InterestingPlacesData;
import ru.nsu.fit.web.placeinfo.location.LocationsData;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;

public class Controller implements Flow.Subscriber<Object> {
    private final Model model;

    public Controller() {
        this.model = new Model();
    }

    public void start() throws ExecutionException, InterruptedException {
        TerminalView terminalView = new TerminalView(model);
        model.subscribe(this);
        model.searchSuitablePlaces();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    private int readInt(int length) {
        int placeNumber = -1;
        Scanner scanner = new Scanner(System.in);
        try {
            placeNumber = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NoSuchElementException | IllegalArgumentException ignored) {
        }

        while (placeNumber < 0 || placeNumber >= length) {
            System.out.print("Try again: ");
            scanner = new Scanner(System.in);
            try {
                placeNumber = Integer.parseInt(scanner.nextLine()) - 1;
            } catch (NumberFormatException ignored) {
            }
        }
        return placeNumber;
    }

    @Override
    public void onNext(Object item) {
        switch (model.getState()) {
            case FINISH, ERROR -> {
                try {
                    model.searchSuitablePlaces();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            case START -> {
                Scanner scanner = new Scanner(System.in);
                String placeName = scanner.nextLine();
                model.setPlaceName(placeName);
            }

            case LOCATIONS -> {
                LocationsData.Place[] hits = model.getLocations().getLocationsData().hits;
                if (hits != null && hits.length != 0) {
                    int placeNumber = readInt(hits.length);
                    try {
                        model.searchWeatherAndPlacesInfo(placeNumber);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        model.searchSuitablePlaces();
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            case INTERESTING_PLACES -> {
                InterestingPlacesData[] interestingPlacesData = model.getInterestingPlaces().getInterestingPlacesData();

                if (interestingPlacesData != null && interestingPlacesData.length != 0) {
                    int placeNumber = readInt(interestingPlacesData.length);
                    try {
                        model.searchPlaceInfo(placeNumber);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        model.searchSuitablePlaces();
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
