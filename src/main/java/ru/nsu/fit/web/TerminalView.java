package ru.nsu.fit.web;

import ru.nsu.fit.web.location.LocationInfo;
import ru.nsu.fit.web.weather.WeatherData;

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
        if (model.getState() == Model.State.START) {
            System.out.print("Loading");
        } else if (model.getState() == Model.State.LOCATIONS) {
            LocationInfo.Place[] hits = model.getLocationInfo().hits;
            System.out.print("\r");
            for (int i = 0; i < hits.length; i++) {
                System.out.println(i + ". " + hits[i].name);
                System.out.println("Latency: " + hits[i].point.lat);
                System.out.println("Longency: " + hits[i].point.lng + "\n");
            }
            System.out.print("Choose place: ");
        } else if (model.getState() == Model.State.WEATHER){
            WeatherData weatherData = model.getWeather().getWeatherData();
            System.out.println(weatherData.weather[0].main);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
