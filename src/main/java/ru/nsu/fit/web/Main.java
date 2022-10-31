package ru.nsu.fit.web;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        if (args.length == 0) {
            System.err.println("Wrong amount of arguments");
            System.out.println("Usage:\texe PLACE\nWhere:\tPLACE  := { Name of the place }");
            return;
        }

        Location location = new Location();
        CompletableFuture<Weather> completableFuture
                = CompletableFuture.supplyAsync(() -> location.getStringInfoByName(args[0])).
                thenCompose(s -> CompletableFuture.supplyAsync(() -> {
                            LocationInfo locationInfo = location.getLocationInfo();
                            Weather weather = new Weather(locationInfo.hits[0].point.lng, locationInfo.hits[0].point.lat);
                            weather.getStringInfo();
                            return weather;
                        }
                ));


        WeatherData weatherData = completableFuture.get().getWeatherData();
        System.out.println(weatherData.weather[0].main);
    }
}
