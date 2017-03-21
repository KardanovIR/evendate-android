package ru.evendate.android.models;

import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.ui.DateFormatter;

/**
 * Created by Dmitry on 04.12.2015.
 */

public class EventFormatter {
    public static String formatDateInterval(Event event) {
        //10-13, 15, 20-31 december; 23 january
        if (event.getDateList().size() == 0) {
            //no dates -> error at server
            Tracker tracker = EvendateApplication.getTracker();
            tracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription("No dates for event. Id: " + event.getEntryId())
                    .setFatal(false)
                    .build());
            return "";
        }
        Collections.sort(event.getDateList());
        String firstDay = "";
        String firstMonth = "";
        String curDay = "";
        String curMonth = "";
        String prevDay = "";
        String prevMonth = "";
        String resStr = "";
        String curStr = "";
        for (Date date : event.getDateList()) {
            if (firstDay.equals("")) {
                firstDay = formatDay(date.getEventDate());
                firstMonth = formatMonth(date.getEventDate());
                curDay = firstDay;
                curMonth = firstMonth;
                curStr = curDay;
                continue;
            }
            prevDay = curDay;
            prevMonth = curMonth;
            curDay = formatDay(date.getEventDate());
            curMonth = formatMonth(date.getEventDate());
            if (Integer.parseInt(curDay) - 1 != Integer.parseInt(prevDay)) {
                resStr += curStr;
                curStr = "";
                if (Integer.parseInt(prevDay) == Integer.parseInt(firstDay)) {
                    if (!firstMonth.equals(curMonth)) {
                        resStr += " " + prevMonth;
                        curStr = "; ";
                    } else {
                        curStr += ", ";
                    }
                } else if (Integer.parseInt(prevDay) - 1 != Integer.parseInt(firstDay)) {
                    if (!firstMonth.equals(curMonth)) {
                        resStr += "-" + prevDay + " " + prevMonth;
                        curStr = "; ";
                    } else {
                        resStr += "-" + prevDay;
                        curStr = ", ";
                    }
                } else {
                    if (!firstMonth.equals(prevMonth)) {
                        resStr += ", " + prevDay + " " + prevMonth;
                        curStr = "; ";
                    } else {
                        resStr += ", " + prevDay;
                        curStr = ", ";
                    }
                }
                firstDay = curDay;
                firstMonth = curMonth;
                curStr += curDay;
            }
        }
        if (!prevDay.equals("") && Integer.parseInt(curDay) == Integer.parseInt(firstDay)) {
            if (!curMonth.equals(firstMonth)) {
                resStr += " " + prevMonth + "; ";
                resStr += curDay + " " + curMonth;
            } else {
                resStr += curStr + " " + curMonth;
            }
        } else if (!prevDay.equals("") &&
                Integer.parseInt(curDay) - 1 != Integer.parseInt(firstDay)) {
            if (!curMonth.equals(firstMonth)) {
                resStr += " " + prevMonth + "; " + curDay + " " + curMonth;
            } else {
                resStr += curStr + "-" + curDay + " " + curMonth;
            }
        } else {
            if (Integer.parseInt(curDay) - 1 == Integer.parseInt(firstDay))
                resStr += curStr + ", " + curDay + " " + curMonth;
            else
                resStr += curStr + " " + curMonth;
        }
        return resStr;
    }

    private static String formatDay(long date) {
        DateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        return dayFormat.format(new java.util.Date(date * 1000));
    }

    private static String formatMonth(long date) {
        DateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return monthFormat.format(new java.util.Date(date * 1000));
    }



    public static String formatEventTime(String startTime, String endTime) {
        return catSeconds(startTime) + (endTime != null ? " - " + catSeconds(endTime) : "");
    }

    public static String formatDate(long date) {
        return DateFormatter.formatEventSingleDate(DateUtils.date(date));
    }

    public static String formatEventTime(Context c, DateFull date) {
        if (date.getStartTime().equals(date.getEndTime()) && date.getStartTime().equals("00:00:00"))
            return c.getString(R.string.event_all_day);
        return catSeconds(date.getStartTime()) + " - " + catSeconds(date.getEndTime());
    }

    /**
     * remove seconds from time string HH:mm:ss
     */
    private static String catSeconds(String time) {
        return time.substring(0, time.lastIndexOf(":"));
    }

    public static String formatPrice(Context c, int price) {
        return c.getString(R.string.event_price_from) + " " + price + " " + c.getString(R.string.event_price_rub);
    }
}
