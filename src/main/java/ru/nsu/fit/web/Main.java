package ru.nsu.fit.web;

import ru.nsu.fit.web.mvc.Controller;

import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        if (args.length == 0) {
            System.err.println("Wrong amount of arguments");
            System.out.println("Usage:\texe PLACE\nWhere:\tPLACE  := { Name of the place }");
            return;
        }

        Controller controller = new Controller();
        controller.start();
    }
}
