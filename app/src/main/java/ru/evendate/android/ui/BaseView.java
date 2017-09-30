package ru.evendate.android.ui;

import android.content.Context;

/**
 * Base view for MVP architecture
 */
public interface BaseView<T> {
    void setPresenter(T presenter);

    Context getContext();
}
