package ru.evendate.android;

import android.test.AndroidTestCase;

import org.mockito.Mockito;

import java.util.ArrayList;

import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventDate;
import ru.evendate.android.ui.utils.DateUtils;
import ru.evendate.android.ui.utils.EventFormatter;
import ru.evendate.android.utils.Utilities;

import static org.mockito.Mockito.when;

/**
 * Created by Dmitry on 13.02.2016.
 */
public class EventFormatterTest extends AndroidTestCase {
    private static Event getEventDetail(int num) {
        Event event = Mockito.mock(Event.class);
        ArrayList<EventDate> dates = getDates(num);
        when(event.getDateList()).thenReturn(dates);
        return event;
    }

    private static ArrayList<EventDate> getDates(int num) {
        ArrayList<EventDate> dateList = new ArrayList<>();
        for (int date : getIntDates(num)) {
            EventDate dateModel = Mockito.mock(EventDate.class);
            when(dateModel.getEventDate()).thenReturn(DateUtils.date(date));
            dateList.add(dateModel);
        }
        return dateList;
    }

    private static int[] getIntDates(int num) {
        switch (num) {
            case 0: {
                return new int[]{
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
            }
            case 1: {
                return new int[]{
                        1448928000
                };
            }
            case 2: {
                return new int[]{
                        1448582400,
                        1448928000
                };
            }
            case 3: {
                return new int[]{
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
            }
            case 4: {
                return new int[]{
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
            }
            case 5: {
                return new int[]{
                        1446508800,
                        1448928000,
                        1449014400,
                        1449100800
                };
            }
            case 6: {
                return new int[]{
                        1455321600,
                        1455408000
                };
            }
            default:
                return new int[]{};
        }
    }

    public void testFormatDate() {
        Utilities.newInstance(getContext()).setRuLocale();
        //Log.d("EventFormatter", EventFormatter.formatDateInterval(getEventDetail(0)));
        assertEquals("3, 5, 6, 8, 11-13, 17, 18, 27 ноября; 1 декабря", EventFormatter.formatDateInterval(getEventDetail(0)));
        assertEquals("1 декабря", EventFormatter.formatDateInterval(getEventDetail(1)));
        assertEquals("27 ноября; 1 декабря", EventFormatter.formatDateInterval(getEventDetail(2)));
        assertEquals("25-29 февраля; 1-10 марта", EventFormatter.formatDateInterval(getEventDetail(3)));
        assertEquals("1-10 марта", EventFormatter.formatDateInterval(getEventDetail(4)));
        assertEquals("3 ноября; 1-3 декабря", EventFormatter.formatDateInterval(getEventDetail(5)));
        assertEquals("13, 14 февраля", EventFormatter.formatDateInterval(getEventDetail(6)));

        Utilities.newInstance(getContext()).setEnLocale();
        assertEquals("3, 5, 6, 8, 11-13, 17, 18, 27 November; 1 December", EventFormatter.formatDateInterval(getEventDetail(0)));
    }
}
