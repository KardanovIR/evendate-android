package ru.evendate.android.models;

/**
 * Created by Aedirn on 16.03.17.
 */

public abstract class Params {
    static public String get(String[] paramList) {
        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (!result.toString().isEmpty())
                result.append(",");
            result.append(param);
        }
        return result.toString();
    }

}
