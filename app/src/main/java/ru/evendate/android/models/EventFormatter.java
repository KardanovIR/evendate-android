package ru.evendate.android.models;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

import ru.evendate.android.EvendateApplication;

/**
 * Created by Dmitry on 04.12.2015.
 */

public class EventFormatter {
    public static String formatDate(EventDetail event) {
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
                firstDay = formatDay(date);
                firstMonth = formatMonth(date);
                curDay = firstDay;
                curMonth = firstMonth;
                curStr = curDay;
                continue;
            }
            prevDay = curDay;
            prevMonth = curMonth;
            curDay = formatDay(date);
            curMonth = formatMonth(date);
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

    public static String formatTags(EventDetail event) {
        String tags = null;
        for (Tag tag : event.getTagList()) {
            if (tags == null)
                tags = tag.getName();
            else
                tags += ", " + tag.getName();
        }
        return tags;
    }

    public static String formatMonth(Date date) {
        return formatMonth(date.getEventDate());
    }

    public static String formatMonth(long date) {
        DateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return monthFormat.format(new java.util.Date(date * 1000));
    }

    public static String formatDay(Date date) {
        return formatDay(date.getEventDate());
    }

    public static String formatDay(long date) {
        DateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        return dayFormat.format(new java.util.Date(date * 1000));
    }

    public static String formatTime(Date date) {
        return formatTime(date.getEventDate());
    }

    public static String formatTime(long date) {
        DateFormat dayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dayFormat.format(new java.util.Date(date * 1000));
    }

    public static String formatTime(String date) {
        return date.substring(0, date.lastIndexOf(":"));
    }

    public static String formatDate(Date date) {
        return formatDay(date.getEventDate()) + " " + formatMonth(date.getEventDate());
    }

    public static String formatDate(long date) {
        return formatDay(date) + " " + formatMonth(date);
    }
}
