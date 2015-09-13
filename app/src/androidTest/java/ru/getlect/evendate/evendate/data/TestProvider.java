package ru.getlect.evendate.evendate.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import ru.getlect.evendate.evendate.utils.Utilities;

/**
 * Created by Dmitry on 04.09.2015.
 */
public class TestProvider extends AndroidTestCase {
    public void testBasicWeatherQuery() {
        EvendateDBHelper dbHelper = new EvendateDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long locationRowId = 1;

        ContentValues eventValues = Utilities.createEvent(locationRowId);

        long eventRowId = db.insert(EvendateContract.EventEntry.TABLE_NAME, null, eventValues);
        assertTrue("Unable to Insert WeatherEntry into the Database", eventRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor eventsCursor = mContext.getContentResolver().query(
                EvendateContract.EventEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        Utilities.validateCursor("testBasicWeatherQuery", eventsCursor, eventValues);
    }
}
