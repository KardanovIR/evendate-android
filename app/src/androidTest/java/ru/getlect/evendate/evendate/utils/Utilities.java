package ru.getlect.evendate.evendate.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 04.09.2015.
 */
public class Utilities extends AndroidTestCase {

    public static ContentValues createEvent(long organization_id) {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(EvendateContract.EventEntry.COLUMN_TITLE, "пришли посидеть");
        testValues.put(EvendateContract.EventEntry.COLUMN_EVENT_ID, 1);
        testValues.put(EvendateContract.EventEntry
                .COLUMN_DESCRIPTION, "образовательные занятия на 4 академических часа");
        testValues.put(EvendateContract.EventEntry.COLUMN_LOCATION_TEXT, "bnlalbla");
        testValues.put(EvendateContract.EventEntry.COLUMN_LOCATION_URI, "link_location");
        testValues.put(EvendateContract.EventEntry.COLUMN_START_DATE, "2015-09-03");
        testValues.put(EvendateContract.EventEntry.COLUMN_NOTIFICATIONS, 1);
        testValues.put(EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID, organization_id);
        testValues.put(EvendateContract.EventEntry.COLUMN_LATITUDE, 55.451);
        testValues.put(EvendateContract.EventEntry.COLUMN_LONGITUDE, 65.154);
        testValues.put(EvendateContract.EventEntry.COLUMN_END_DATE, "2015-09-06");
        testValues.put(EvendateContract.EventEntry.COLUMN_IMAGE_VERTICAL_URL, "link_vertical");
        testValues.put(EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL, "link_detail");
        testValues.put(EvendateContract.EventEntry.COLUMN_BEGIN_TIME, "00:00:00");
        testValues.put(EvendateContract.EventEntry.COLUMN_END_TIME, "23:44:44");
        testValues.put(EvendateContract.EventEntry.COLUMN_IMAGE_HORIZONTAL_URL, "link_horizontal");
        testValues.put(EvendateContract.EventEntry.COLUMN_LOCATION_JSON, "blalblalblalblalblablala");
        testValues.put(EvendateContract.EventEntry.COLUMN_CAN_EDIT, 0);
        testValues.put(EvendateContract.EventEntry.COLUMN_CAN_EDIT, "activity");
        testValues.put(EvendateContract.EventEntry.COLUMN_IS_FAVORITE, 0);

        return testValues;
    }

    public static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    public static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }
}
