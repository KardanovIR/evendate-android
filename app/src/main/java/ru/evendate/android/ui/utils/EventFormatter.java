package ru.evendate.android.ui.utils;

import android.content.Context;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventDate;

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
        for (EventDate eventDate : event.getDateList()) {
            if (firstDay.equals("")) {
                firstDay = formatDay(eventDate.getEventDate());
                firstMonth = formatMonth(eventDate.getEventDate());
                curDay = firstDay;
                curMonth = firstMonth;
                curStr = curDay;
                continue;
            }
            prevDay = curDay;
            prevMonth = curMonth;
            curDay = formatDay(eventDate.getEventDate());
            curMonth = formatMonth(eventDate.getEventDate());
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

    private static String formatDay(Date date) {
        DateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        return dayFormat.format(date);
    }

    private static String formatMonth(Date date) {
        DateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return monthFormat.format(date);
    }

    public static String formatDate(Date date) {
        return DateFormatter.formatEventSingleDate(date);
    }

    public static String formatEventTime(Date startDateTime, Date endDateTime) {
        return DateFormatter.formatTime(startDateTime) + " - " +
                DateFormatter.formatTime(endDateTime);
    }

    public static String formatPrice(Context c, int price) {
        return c.getString(R.string.event_price_from) + " " + price + " " + c.getString(R.string.event_price_rub);
    }

    public static Date getNearestDateTime(Event event) {
        Date currentDate = new Date(System.currentTimeMillis());
        for (EventDate eventDate : event.getDateList()) {
            if (eventDate.getStartDateTime().getTime() <= currentDate.getTime()
                    && eventDate.getEndDateTime().getTime() >= currentDate.getTime()) {
                return currentDate;
            }
        }
        if (event.getNearestDateTime() == null) {
            return event.getLastDateTime();
        }
        return event.getNearestDateTime();
    }
}
