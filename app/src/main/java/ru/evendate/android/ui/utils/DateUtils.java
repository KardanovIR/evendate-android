package ru.evendate.android.ui.utils;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by Aedirn on 06.02.17.
 */

public class DateUtils {
    /**
     * get date from server long cause Date need time in millis, but server return time in seconds
     *
     * @param date timestamp in seconds
     * @return Date
     */
    public static Date date(@NonNull Integer date) {
        return new Date((long) date * 1000);
    }
}
