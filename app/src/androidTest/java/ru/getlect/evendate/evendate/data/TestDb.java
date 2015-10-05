package ru.getlect.evendate.evendate.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

import ru.getlect.evendate.evendate.utils.Utilities;

/**
 * Created by Dmitry on 02.09.2015.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(EvendateDBHelper.DATABASE_NAME);
    }

    public void setUp() {
        deleteTheDatabase();
    }
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(EvendateContract.EventEntry.TABLE_NAME);
        tableNameHashSet.add(EvendateContract.EventTagEntry.TABLE_NAME);
        tableNameHashSet.add(EvendateContract.OrganizationEntry.TABLE_NAME);
        tableNameHashSet.add(EvendateContract.TagEntry.TABLE_NAME);
        tableNameHashSet.add(EvendateContract.UserEntry.TABLE_NAME);
        tableNameHashSet.add(EvendateContract.UserEventEntry.TABLE_NAME);

        mContext.deleteDatabase(EvendateDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new EvendateDBHelper(this.mContext)
                .getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain needed tables
        assertTrue("Error: Your database was created without needed tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + EvendateContract.EventEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> eventColumnHashSet = new HashSet<String>();
        eventColumnHashSet.add(EvendateContract.EventEntry._ID);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_TITLE);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_DESCRIPTION);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_LOCATION_TEXT);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_LOCATION_URI);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_LOCATION_JSON);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_LATITUDE);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_LONGITUDE);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_START_DATE);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_END_DATE);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_NOTIFICATIONS);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_IMAGE_VERTICAL_URL);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_IMAGE_HORIZONTAL_URL);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_BEGIN_TIME);
        eventColumnHashSet.add(EvendateContract.EventEntry.COLUMN_END_TIME);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            eventColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required event
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                eventColumnHashSet.isEmpty());
        c.close();
        db.close();
    }
    static ContentValues createOrganization() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(EvendateContract.OrganizationEntry.COLUMN_NAME, "Государственный Университет Управления");
        testValues.put(EvendateContract.OrganizationEntry.COLUMN_IMG_URL, "www.guushechka1.ru");
        testValues.put(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME, "ГУУ");
        testValues.put(EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION, "так и живем");
        testValues.put(EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME, "образовач");
        testValues.put(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED, 1);

        return testValues;
    }
    public long insertOrganization() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        EvendateDBHelper dbHelper = new EvendateDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = createOrganization();

        // Third Step: Insert ContentValues into database and get a row ID back
        long locationRowId;
        locationRowId = db.insert(EvendateContract.OrganizationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                EvendateContract.OrganizationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        Utilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from location query",
                cursor.moveToNext());

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return locationRowId;
    }
    public void testOrganizationTable(){
        insertOrganization();
    }
    public void testEventsTable() {
        long locationRowId = insertOrganization();
        // Make sure we have a valid row ID.
        assertFalse("Error: Location Not Inserted Correctly", locationRowId == -1L);

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SQLiteDatabase db = new EvendateDBHelper(this.mContext)
                .getWritableDatabase();

        // Second Step
        ContentValues eventsValues = Utilities.createEvent(locationRowId);

        long eventsRowId = db.insert(EvendateContract.EventEntry.TABLE_NAME, null, eventsValues);
        assertTrue(eventsRowId != -1);
        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor eventsCursor = db.query(
                EvendateContract.EventEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from location query", eventsCursor.moveToFirst() );

        // Fifth Step: Validate the location Query
        Utilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                eventsCursor, eventsValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from weather query",
                eventsCursor.moveToNext());

        // Sixth Step: Close cursor and database
        eventsCursor.close();
        db.close();
    }
}
