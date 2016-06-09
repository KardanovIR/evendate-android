package ru.evendate.android.sync;


/**
 * Created by Dmitry on 18.10.2015.
 */
public class EvendateServiceResponse {
    boolean status;
    String text;

    public boolean isOk() {
        return status;
    }
}
