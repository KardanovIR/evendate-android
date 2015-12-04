package ru.getlect.evendate.evendate.sync.models;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.getlect.evendate.evendate.R;

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
