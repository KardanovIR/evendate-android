package ru.evendate.android.sync.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Dmitry on 04.12.2015.
 */

public class EventFormatter {
    public static String formatDate(EventDetail event) {
        //10-13, 15, 20-31 december; 23 january
        String firstDay = "";
        String firstMonth = "";
        String curDay = "";
        String curMonth = "";
        String prevDay = "";
        String prevMonth = "";
        String resStr = "";
        String curStr = "";
        for(Date date : event.getDataList()){
            if(firstDay.equals("")){
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
            if(Integer.parseInt(curDay) - 1 != Integer.parseInt(prevDay)){
                resStr += curStr;
                curStr = "";
                if(Integer.parseInt(prevDay) == Integer.parseInt(firstDay)){
                    if(!firstMonth.equals(prevMonth)){
                        resStr += " " + prevMonth;
                        curStr = "; ";
                    }
                    else{
                        curStr += ", ";
                    }
                }
                else if(Integer.parseInt(prevDay) - 1 != Integer.parseInt(firstDay)){
                    if(!firstMonth.equals(prevMonth)){
                        resStr += "-" + prevDay + " " + prevMonth;
                        curStr = "; ";
                    }
                    else{
                        resStr += "-" + prevDay;
                        curStr = ", ";
                    }
                }else {
                    if(!firstMonth.equals(prevMonth)) {
                        resStr += ", " + prevDay + " " + prevMonth;
                        curStr = "; ";
                    }else{
                        resStr += ", " + prevDay;
                        curStr = ", ";
                    }
                }
                firstDay = curDay;
                firstMonth = curMonth;
                curStr += curDay;
            }
        }
        if(!prevDay.equals("") && Integer.parseInt(prevDay) == Integer.parseInt(firstDay)){
            if(!curMonth.equals(prevMonth)){
                resStr += " " + prevMonth + "; ";
                resStr += curDay + " " + curMonth;
            }
            else{
                resStr += curDay + " " + curMonth + "; ";
            }
        }
        else if(!prevDay.equals("") &&
                Integer.parseInt(prevDay) - 1 != Integer.parseInt(firstDay)){
            if(!curMonth.equals(prevMonth)){
                resStr += " " + prevMonth + "; " + curDay + " " + curMonth;
            }
            else{
                resStr += "-" + curDay + " " + curMonth;
            }
        }else {
                resStr += curStr + " " + curMonth;
        }
        return resStr;
    }
    public static String formatTags(EventDetail event){
        String tags = null;
        for(TagModel tag : event.getTagList()){
            if(tags == null)
                tags = tag.getName();
            else
                tags += ", " + tag.getName();
        }
        return tags;
    }
    public static String formatMonth(Date date){
        DateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        return monthFormat.format(new java.util.Date(date.getEventDate() * 1000));
    }
    public static String formatDay(Date date){
        DateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        return dayFormat.format(new java.util.Date(date.getEventDate() * 1000));
    }

}
