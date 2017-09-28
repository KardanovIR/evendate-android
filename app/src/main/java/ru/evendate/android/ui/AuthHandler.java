package ru.evendate.android.ui;

import io.reactivex.Observable;

/**
 * Created by dmitry on 11.09.17.
 */
public interface AuthHandler {
    Observable<String> requestAuth();
}