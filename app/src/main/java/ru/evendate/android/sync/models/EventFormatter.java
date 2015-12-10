package ru.evendate.android.sync.models;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.evendate.android.R;
import ru.evendate.android.utils.Utils;

/**
 * Created by Dmitry on 04.12.2015.
 */
public class EventFormatter {
    private Context mContext;

    public EventFormatter(Context context) {
        mContext = context;
    }

    public String formatDay(EventModel event){
        Date date = event.getActialDate();
        return formatDay(date);
    }
    private String formatDay(Date date){
        DateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        if(date == null)
            return null;
        String day = dateFormat.format(date);
        if(day.substring(0, 1).equals("0"))
            day = day.substring(1);
        return day;
    }
    public String formatMonth(EventModel event) {
        Date date = event.getActialDate();
        DateFormat dateFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        if(date == null)
            return null;
        return dateFormat.format(date);
    }
    public String formatDate(EventModel event) {
        //10-13, 15 december; 23 january
        //TODO ой говнокод
        String resDate = "";
        String currentMonth = null;
        String currentDates = "";
        String month = null;
        Date prevDate = null;
        Calendar calendar = Calendar.getInstance();
        DateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        boolean isInterval = false;
        for(String date : event.getDataRangeList()){
            Date parsedDate = parseDate(date);
            if(date == null)
                break;
            month = monthFormat.format(parsedDate);
            if(currentMonth == null)
                currentMonth = month;
            if(prevDate != null){
                calendar.setTime(parsedDate);
                calendar.add(Calendar.DATE, -1);
                if(calendar.getTime().equals(prevDate)){
                    isInterval = true;
                    prevDate = parsedDate;
                    continue;
                }
                else{
                    if(isInterval){
                        if(monthFormat.format(prevDate).equals(month))
                            currentDates += "-" + formatDay(parsedDate);
                        else
                            currentDates += "-" + formatDay(prevDate);
                        isInterval = false;
                    }
                }
            }
            if(!month.equals(currentMonth)){
                if(!resDate.equals(""))
                    resDate += "; " + currentDates + " " + currentMonth;
                else
                    resDate += currentDates + " " + currentMonth;
                currentMonth = month;
                currentDates = "";
                month = null;
            }
            if(currentDates.equals(""))
                currentDates += formatDay(parsedDate);
            else{
                if(!isInterval)
                    currentDates += ", " + formatDay(parsedDate);
            }
            prevDate = parsedDate;
        }
        if(month != null){
            if(!resDate.equals(""))
                resDate += "; " + currentDates + " " + currentMonth;
            else
                if(isInterval){
                    currentDates += "-" + formatDay(prevDate);
                    resDate += currentDates + " " + currentMonth;
                }
                else
                    resDate += currentDates + " " + currentMonth;
        }
        return resDate;
    }
    public Date parseDate(String date){
        Date dateStamp;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
        dateStamp = Utils.formatDate(date, format);
        if(dateStamp == null){
            dateStamp = Utils.formatDate(date, format2);
        }
        return dateStamp;
    }
    public String formatTime(EventModel event) {
        String time;
        if(event.isFullDay())
            time = mContext.getResources().getString(R.string.event_all_day);
        else{
            //cut off seconds
            //TODO temporary
            time = "";
            if(event.getBeginTime() != null && event.getEndTime() != null)
                time = event.getBeginTime().substring(0, 5) + " - " + event.getEndTime().substring(0, 5);
            else if(event.getBeginTime() != null)
                time = event.getBeginTime().substring(0, 5);
        }
        return time;
    }
    public String formatTags(EventModel event){
        String tags = null;
        for(TagModel tag : event.getTagList()){
            if(tags == null)
                tags = tag.getName();
            else
                tags += ", " + tag.getName();
        }
        return tags;
    }
}
