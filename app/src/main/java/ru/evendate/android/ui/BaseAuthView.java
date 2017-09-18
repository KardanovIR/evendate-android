package ru.evendate.android.ui;

import android.content.Context;

import io.reactivex.Observable;

/**
 * Created by dmitry on 11.09.17.
 */

public interface BaseAuthView<T> extends AuthHandler {
    void setPresenter(T presenter);

    Context getContext();

    @Override
    Observable<String> requestAuth();
}
