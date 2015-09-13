package ru.getlect.evendate.evendate.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.getlect.evendate.evendate.data.EvendateContract.EventEntry;
import ru.getlect.evendate.evendate.data.EvendateContract.EventTagEntry;
import ru.getlect.evendate.evendate.data.EvendateContract.OrganizationEntry;
import ru.getlect.evendate.evendate.data.EvendateContract.TagEntry;
import ru.getlect.evendate.evendate.data.EvendateContract.UserEntry;
import ru.getlect.evendate.evendate.data.EvendateContract.UserEventEntry;
/**
 * Created by Dmitry on 02.09.2015.
 */
public class EvendateDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "evendate.db";
    public EvendateDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_EVENTS_TABLE =
                "CREATE TABLE " + EventEntry.TABLE_NAME + " (" +
                EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EventEntry.COLUMN_EVENT_ID + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_TITLE + " TEXT, " +
                EventEntry.COLUMN_DESCRIPTION + " TEXT, " +
                EventEntry.COLUMN_LATITUDE + " REAL, " +
                EventEntry.COLUMN_LONGITUDE + " REAL, " +
                EventEntry.COLUMN_LOCATION_TEXT + " TEXT, " +
                EventEntry.COLUMN_LOCATION_URI + " TEXT, " +
                EventEntry.COLUMN_LOCATION_JSON + " TEXT, " +
                EventEntry.COLUMN_NOTIFICATIONS + " TEXT, " +
                EventEntry.COLUMN_START_DATE + " NUMERIC NOT NULL, " +
                EventEntry.COLUMN_BEGIN_TIME + " NUMERIC NOT NULL, " +
                EventEntry.COLUMN_END_TIME + " NUMERIC NOT NULL, " +
                EventEntry.COLUMN_END_DATE + " NUMERIC NOT NULL, " +
                EventEntry.COLUMN_ORGANIZATION_ID + " INTEGER NOT NULL, " +
                EventEntry.COLUMN_IMAGE_VERTICAL_URL + " TEXT, " +
                EventEntry.COLUMN_DETAIL_INFO_URL + " TEXT, " +
                EventEntry.COLUMN_IMAGE_HORIZONTAL_URL + " TEXT, " +
                EventEntry.COLUMN_CAN_EDIT + " INTEGER, " +
                EventEntry.COLUMN_EVENT_TYPE + " TEXT, " +
                EventEntry.COLUMN_IS_FAVORITE + " INTEGER, " +
                " FOREIGN KEY (" + EventEntry.COLUMN_ORGANIZATION_ID + ") REFERENCES " +
                OrganizationEntry.TABLE_NAME + " (" + OrganizationEntry._ID + ")" +
                " UNIQUE (" + EventEntry.COLUMN_EVENT_ID +
                ") ON CONFLICT REPLACE " +
                " );";
        final String SQL_CREATE_EVENTS_TAGS_TABLE =
                "CREATE TABLE " + EventTagEntry.TABLE_NAME + " (" +
                EventTagEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EventTagEntry.COLUMN_EVENT_ID + " INTEGER, " +
                EventTagEntry.COLUMN_TAG_ID + " INTEGER, " +
                " FOREIGN KEY (" + EventTagEntry.COLUMN_EVENT_ID + ") REFERENCES " +
                EventEntry.TABLE_NAME + " (" + EventEntry._ID + "), " +
                " FOREIGN KEY (" + EventTagEntry.COLUMN_TAG_ID + ") REFERENCES " +
                TagEntry.TABLE_NAME + " (" + TagEntry._ID + ")" +
                " UNIQUE (" + EventTagEntry.COLUMN_EVENT_ID + ", " + EventTagEntry.COLUMN_TAG_ID +
                ") ON CONFLICT REPLACE " +
                " );";
        final String SQL_CREATE_ORGANIZATIONS_TABLE =
                "CREATE TABLE " + OrganizationEntry.TABLE_NAME + " (" +
                OrganizationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                OrganizationEntry.COLUMN_ORGANIZATION_ID + " INTEGER, " +
                OrganizationEntry.COLUMN_NAME + " TEXT, " +
                OrganizationEntry.COLUMN_IMG_URL + " TEXT, " +
                OrganizationEntry.COLUMN_SHORT_NAME + " TEXT, " +
                OrganizationEntry.COLUMN_DESCRIPTION + " TEXT, " +
                OrganizationEntry.COLUMN_TYPE_NAME + " TEXT, " +
                OrganizationEntry.COLUMN_SUBSCRIBED + " INTEGER," +
                " UNIQUE (" + OrganizationEntry.COLUMN_ORGANIZATION_ID +
                ") ON CONFLICT REPLACE);";
        final String SQL_CREATE_TAGS_TABLE = "CREATE TABLE " + TagEntry.TABLE_NAME + " (" +
                TagEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TagEntry.COLUMN_TAG_ID + " INTEGER, " +
                TagEntry.COLUMN_NAME + " TEXT, " +
                " UNIQUE (" + TagEntry.COLUMN_TAG_ID +
                ") ON CONFLICT REPLACE " +
                " );";
        final String SQL_CREATE_USERS_TABLE =
                "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserEntry.COLUMN_USER_ID + " INTEGER, " +
                UserEntry.COLUMN_LAST_NAME + " TEXT, " +
                UserEntry.COLUMN_FIRST_NAME + " TEXT, " +
                UserEntry.COLUMN_MIDDLE_NAME + " TEXT, " +
                UserEntry.COLUMN_AVATAR_URL + " TEXT, " +
                UserEntry.COLUMN_FRIEND_UID + " INTEGER, " +
                UserEntry.COLUMN_TYPE + " TEXT, " +
                UserEntry.COLUMN_LINK + " TEXT, " +
                " UNIQUE (" + UserEntry.COLUMN_USER_ID +
                ") ON CONFLICT REPLACE " +
                " );";
        final String SQL_CREATE_USERS_EVENT_TABLE =
                "CREATE TABLE " + UserEventEntry.TABLE_NAME + " (" +
                UserEventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserEventEntry.COLUMN_USER_ID + " INTEGER, " +
                UserEventEntry.COLUMN_EVENT_ID + " INTEGER, " +
                " UNIQUE (" + UserEventEntry.COLUMN_USER_ID + ", " + UserEventEntry.COLUMN_EVENT_ID +
                ") ON CONFLICT REPLACE " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_ORGANIZATIONS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TAGS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_EVENTS_TAGS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_USERS_EVENT_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EvendateContract.EventEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EvendateContract.EventTagEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EvendateContract.OrganizationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EvendateContract.TagEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EvendateContract.UserEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EvendateContract.UserEventEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
