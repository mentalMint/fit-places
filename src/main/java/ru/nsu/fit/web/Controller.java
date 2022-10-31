package ru.nsu.fit.web;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;

public class Controller implements Flow.Subscriber<Object> {
    private final Model model;

    public Controller() {
        this.model = new Model();
    }

    public void start()  {
        TerminalView terminalView = new TerminalView(model);
        model.subscribe(this);
    }

    public void search(String placeName) throws ExecutionException, InterruptedException {
        model.start(placeName);
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(Object item) {
        if (model.getState() == Model.State.LOCATIONS) {
            int placeNumber;
            Scanner scanner = new Scanner(System.in);
            placeNumber = Integer.parseInt(scanner.nextLine());
            try {
                model.searchWeather(placeNumber);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
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
