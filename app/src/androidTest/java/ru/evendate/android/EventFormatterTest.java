package ru.evendate.android;

import android.test.AndroidTestCase;
import android.util.Log;

import org.mockito.Mockito;

import java.util.ArrayList;

import ru.evendate.android.sync.models.Date;
import ru.evendate.android.sync.models.EventDetail;
import ru.evendate.android.sync.models.EventFormatter;

import static org.mockito.Mockito.when;

/**
 * Created by Dmitry on 13.02.2016.
 */
public class EventFormatterTest extends AndroidTestCase {
    //TODO Work only on ru device
    public void testFormatDate(){
        Log.d("EventFormatter", EventFormatter.formatDate(getEventDetail(0)));
        assertEquals("3, 5, 6, 8, 11-13, 17, 18, 27 ноября; 1 декабря", EventFormatter.formatDate(getEventDetail(0)));
        assertEquals("1 декабря", EventFormatter.formatDate(getEventDetail(1)));
        assertEquals("27 ноября; 1 декабря", EventFormatter.formatDate(getEventDetail(2)));
    }
    public static EventDetail getEventDetail(int num){
        EventDetail event = Mockito.mock(EventDetail.class);
        ArrayList<Date> dates = getDates(num);
        when(event.getDataList()).thenReturn(dates);
        return event;
    }
    private static ArrayList<Date> getDates(int num){
        ArrayList<Date> dateList = new ArrayList<>();
        for(long date : getIntDates(num)){
            dateList.add(new Date(date));
        }
        return dateList;
    }

    private static long[] getIntDates(int num){
        switch (num){
            case 0:{
                long[] str = {
                        1446508800,
                        1446681600,
                        1446768000,
                        1446940800,
                        1447200000,
                        1447286400,
                        1447372800,
                        1447718400,
                        1447804800,
                        1448582400,
                        1448928000
                };
                return str;
            }
            case 1:{
                long[] str = {
                        1448928000
                };
                return str;
            }
            case 2:{
                long[] str = {
                        1448582400,
                        1448928000
                };
                return str;
            }
        }
        return null;
    }
}
