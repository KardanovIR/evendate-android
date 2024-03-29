package ru.evendate.android.network;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Dmitry on 13.11.2016.
 */

public class ServiceUtils {
    private static String LOG_TAG = ServiceUtils.class.getSimpleName();

    private static DateFormat requestDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static DateFormat requestDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static String formatDateForServer(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(d.getTime());
    }

    public static String constructEventBackgroundURL(String baseUrl, int width) {
        return baseUrl + "?width=" + String.valueOf(width);
    }

    public static String constructQrLink(String hostName, long eventId, String ticketUuid) {
        return hostName + "/api/v1/events/" + eventId
                + "/tickets/" + ticketUuid + "/qr?format=png&size=5";
    }

    public static String formatDateTimeRequest(Date date) {
        requestDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formatted_date = requestDateTimeFormat.format(date);
        logDate("formatDateTimeRequest", formatted_date);
        return formatted_date;
    }

    public static String formatDateRequest(Date date) {
        requestDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String formatted_date = requestDateFormat.format(date);
        logDate("formatDateRequest", formatted_date);
        return formatted_date;
    }

    public static String formatDateRequestNotUtc(Date date) {
        requestDateFormat.setTimeZone(TimeZone.getDefault());
        String formatted_date = requestDateFormat.format(date);
        logDate("formatDateRequestNotUtc", formatted_date);
        return formatted_date;
    }

    private static void logDate(String name, String date) {
        Log.d(LOG_TAG, name + " formatted_date: " + date);
    }

    public static String encloseFields(String fields) {
        return "{fields:'" + fields + "'}";
    }

    public static String encloseFields(String fields, String orderBy) {
        return "{fields:'" + fields + "',order_by:'" + orderBy + "'}";
    }
}
