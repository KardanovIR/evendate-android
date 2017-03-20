package ru.evendate.android.models;

import java.util.Date;

/**
 * Created by Aedirn on 06.02.17.
 */

public class DateUtils {
    /**
     * get date from server long cause Date need time in millis, but server return time in seconds
     * @param date timestamp in seconds
     * @return Date
     */
    public static java.util.Date date(long date) {
        return new Date(date * 1000);
    }
}
