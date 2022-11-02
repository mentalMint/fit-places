package ru.nsu.fit.web.mvc;

import ru.nsu.fit.web.placeinfo.interestingplaces.InterestingPlacesData;
import ru.nsu.fit.web.placeinfo.location.LocationsData;
import ru.nsu.fit.web.placeinfo.place.PlaceData;
import ru.nsu.fit.web.placeinfo.weather.WeatherData;

import java.util.concurrent.Flow;

public class TerminalView implements Flow.Subscriber<Object> {
    Model model;

    public TerminalView(Model model) {
        this.model = model;
        model.subscribe(this);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(Object item) {
        switch (model.getState()) {
            case START -> System.out.print("Enter place name: ");
            case LOADING -> System.out.print("Loading...");
            case LOCATIONS -> {
                LocationsData.Place[] hits = model.getLocations().getLocationsData().hits;
                System.out.print("\r");
                System.out.println("          ");
                if (hits != null) {
                    if (hits.length == 0) {
                        System.out.println("No such places\n");
                    } else {
                        System.out.println("===Available places===");
                        for (int i = 0; i < hits.length; i++) {
                            System.out.print((i + 1) + ". ");
                            LocationsData.Place hit = hits[i];
                            if (hit != null) {
                                if (hit.name != null) {
                                    System.out.println(hit.name);
                                } else {
                                    System.out.println();
                                }
                                if (hit.country != null) {
                                    System.out.println("\tCountry: " + hit.country);
                                }
                                if (hit.city != null) {
                                    System.out.println("\tCity: " + hit.city);
                                }
                                if (hit.street != null) {
                                    System.out.println("\tStreet: " + hit.street);
                                }
                                if (hit.housenumber != null) {
                                    System.out.println("\tHouse number " + hit.housenumber);
                                }
                            }
                            System.out.println();
                        }
                        System.out.println("=====================");
                        System.out.println();
                        System.out.print("Choose place: ");
                    }
                } else {
                    System.out.println("No such places\n");
                }
            }

            case WEATHER -> {
                WeatherData weatherData = model.getWeather().getWeatherData();
                System.out.println();
                if (weatherData != null && weatherData.weather != null && weatherData.weather.length > 0) {
                    System.out.println("===Weather here===");
                    System.out.println("Weather: " + weatherData.weather[0].main);
                    System.out.println("Description: " + weatherData.weather[0].description);
                    System.out.println("====================");
                } else {
                    System.out.println("No information about weather here\n");
                }
            }

            case INTERESTING_PLACES -> {
                InterestingPlacesData[] interestingPlacesData = model.getInterestingPlaces().getInterestingPlacesData();
                System.out.println();

                if (interestingPlacesData == null || interestingPlacesData.length == 0) {
                    System.out.println("Nothing interesting in here(\n");
                } else {
                    System.out.println("===Places of interest nearby===");
                    for (int i = 0; i < interestingPlacesData.length; i++) {
                        if (interestingPlacesData[i] != null) {
                            if (interestingPlacesData[i].name != null) {
                                System.out.println((i + 1) + ". " + interestingPlacesData[i].name);
                            }
//                            if (interestingPlacesData[i].kind != null) {
//                                System.out.println("Kind: " + interestingPlacesData[i].kind);
//                            }

                            PlaceData placeData = model.getPlaces().get(i).getPlaceData();
                            if (placeData != null) {
//                                System.out.println("===Information about this place===");
                                if (placeData.info != null) {
                                    if (placeData.info.descr != null) {
                                        System.out.println(placeData.info.descr);
                                    }
                                }
//                                if (placeData.name != null) {
//                                    System.out.println("Name: " + placeData.name);
//                                }
                                if (placeData.kinds != null) {
                                    System.out.println("Kind: " + placeData.kinds);
                                }
                                if (placeData.rate != null) {
                                    System.out.println("Rate: " + placeData.rate);
                                }
                                if (placeData.wikipedia != null) {
                                    System.out.println(placeData.wikipedia);
                                }
                                System.out.println("\n_______________________________\n");
                            } else {
                                System.out.println("No information about this place\n");
                            }

                        }
                    }
                    System.out.println("=============================\n");
                }
            }

            case ERROR -> System.out.println("\rError occurred\n");

        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
