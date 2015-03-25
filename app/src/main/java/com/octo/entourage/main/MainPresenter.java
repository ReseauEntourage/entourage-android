package com.octo.entourage.main;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Locale;

/**
 * Presenter controlling the main activity
 */
public class MainPresenter {
    private final MainActivity activity;

    public MainPresenter(final MainActivity activity) {
        this.activity = activity;
    }


    public void start() {
        activity.sayHello(DateTimeFormat.fullTime().withLocale(Locale.FRANCE).print(DateTime.now()));
    }
}
