package ru.evendate.android.ui;

/**
 * Base view for MVP architecture
 */
public interface BaseView<T> {
    void setPresenter(T presenter);
}
