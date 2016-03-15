package ru.evendate.android.loaders;

/**
 * Created by Dmitry on 02.02.2016.
 */
public interface LoaderListener<Type> {
    void onLoaded(Type subList);
    void onError();
}
