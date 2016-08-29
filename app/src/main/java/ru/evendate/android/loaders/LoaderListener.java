package ru.evendate.android.loaders;

/**
 * Created by Dmitry on 02.02.2016.
 */
@Deprecated
public interface LoaderListener<Type> {
    void onLoaded(Type subList);

    void onError();
}
