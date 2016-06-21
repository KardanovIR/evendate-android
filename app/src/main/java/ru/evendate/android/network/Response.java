package ru.evendate.android.network;


/**
 * Created by Dmitry on 18.10.2015.
 */
public class Response {
    boolean status;
    String text;

    public boolean isOk() {
        return status;
    }
}
