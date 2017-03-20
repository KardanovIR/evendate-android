package ru.evendate.android.models;

/**
 * Created by Aedirn on 16.03.17.
 */

public abstract class Params {
    static public String get(String[] paramList) {
        String result = "";
        for (String param : paramList) {
            if (!result.isEmpty())
                result += ",";
            result += param;
        }
        return result;
    }

}
