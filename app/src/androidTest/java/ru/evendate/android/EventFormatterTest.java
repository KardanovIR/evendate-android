package ru.evendate.android;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.ArrayList;

import ru.evendate.android.models.DateFull;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFormatter;

import static org.mockito.Mockito.when;

/**
 * Created by Dmitry on 13.02.2016.
 */
public class EventFormatterTest extends AndroidTestCase {
    //TODO Work only on ru device
    public void testFormatDate() {
        //Log.d("EventFormatter", EventFormatter.formatDate(getEventDetail(0)));
        assertEquals("3, 5, 6, 8, 11-13, 17, 18, 27 ноября; 1 декабря", EventFormatter.formatDate(getEventDetail(0)));
        assertEquals("1 декабря", EventFormatter.formatDate(getEventDetail(1)));
        assertEquals("27 ноября; 1 декабря", EventFormatter.formatDate(getEventDetail(2)));
        assertEquals("25-29 февраля; 1-10 марта", EventFormatter.formatDate(getEventDetail(3)));
        assertEquals("1-10 марта", EventFormatter.formatDate(getEventDetail(4)));
        assertEquals("3 ноября; 1-3 декабря", EventFormatter.formatDate(getEventDetail(5)));
        assertEquals("13, 14 февраля", EventFormatter.formatDate(getEventDetail(6)));
    }

    public static EventDetail getEventDetail(int num) {
        EventDetail event = Mockito.mock(EventDetail.class);
        ArrayList<DateFull> dates = getDates(num);
        when(event.getDateList()).thenReturn(dates);
        return event;
    }

    private static ArrayList<DateFull> getDates(int num) {
        ArrayList<DateFull> dateList = new ArrayList<>();
        for (long date : getIntDates(num)) {
            DateFull dateModel = Mockito.mock(DateFull.class);
            when(dateModel.getEventDate()).thenReturn(date);
            dateList.add(dateModel);
        }
        return dateList;
    }

    private static long[] getIntDates(int num) {
        switch (num) {
            case 0: {
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
            case 1: {
                long[] str = {
                        1448928000
                };
                return str;
            }
            case 2: {
                long[] str = {
                        1448582400,
                        1448928000
                };
                return str;
            }
            case 3: {
                long[] str = {
                        1456358400,
                        1456444800,
                        1456531200,
                        1456617600,
                        1456704000,
                        1456790400,
                        1456876800,
                        1456963200,
                        1457049600,
                        1457136000,
                        1457222400,
                        1457308800,
                        1457395200,
                        1457481600,
                        1457568000

                };
                return str;
            }
            case 4: {
                long[] str = {
                        1456790400,
                        1456876800,
                        1456963200,
                        1457049600,
                        1457136000,
                        1457222400,
                        1457308800,
                        1457395200,
                        1457481600,
                        1457568000

                };
                return str;
            }
            case 5: {
                long[] str = {
                        1446508800,
                        1448928000,
                        1449014400,
                        1449100800
                };
                return str;
            }
            case 6: {
                long[] str = {
                        1455321600,
                        1455408000
                };
                return str;
            }
        }
        return null;
    }
}
