package ru.evendate.android.ui;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatter {
    private static String LOG_TAG = DateFormatter.class.getSimpleName();

    private static DateFormat requestDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private static DateFormat requestDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static DateFormat registrationFormat = new SimpleDateFormat("d.MM.yyyy", Locale.getDefault());
    private static DateFormat calendarBottomSheetFormat = new SimpleDateFormat("cc, d MMMM", Locale.getDefault());
    private static DateFormat eventSingleDateFormat = new SimpleDateFormat("d MMMM", Locale.getDefault());
    private static DateFormat notificationDatetimeFormat = new SimpleDateFormat("d MMMM HH:mm", Locale.getDefault());
    private static DateFormat orderDatetimeFormat = new SimpleDateFormat("d MMM, HH:mm", Locale.getDefault());

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

    public static String formatNotification(Date date) {
        String formatted_date = notificationDatetimeFormat.format(date);
        logDate("formatNotification", formatted_date);
        return formatted_date;
    }

    public static String formatCalendarLabel(Date date) {
        String formatted_date = calendarBottomSheetFormat.format(date);
        logDate("formatCalendarLabel", formatted_date);
        return formatted_date;
    }

    public static String formatRegistrationDate(Date date) {
        String formatted_date = registrationFormat.format(date);
        logDate("formatRegistrationDate", formatted_date);
        return formatted_date;
    }

    public static String formatEventSingleDate(Date date) {
        String formatted_date = eventSingleDateFormat.format(date);
        logDate("formatEventSingleDate", formatted_date);
        return formatted_date;
    }

    public static String formatEventSingleTime(Date startDate, Date endDate) {
        return formatTime(startDate) + (endDate != null ? " - " + formatTime(endDate) : "");
    }

    public static String formatTime(Date date) {
        DateFormat dayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dayFormat.format(date);
    }

    public static String formatOrderDateTime(Date date) {
        String formatted_date = orderDatetimeFormat.format(date);
        logDate("formatOrderDateTime", formatted_date);
        return formatted_date;
    }

    private static void logDate(String name, String date) {
        Log.d(LOG_TAG, name + " formatted_date: " + date);
    }
}
