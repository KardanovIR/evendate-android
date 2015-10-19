package ru.getlect.evendate.evendate.sync;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventFriendEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventTagEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.FriendEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.TagEntry;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class LocalDataFetcher {
    ContentResolver mContentResolver;

    public LocalDataFetcher(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    public ArrayList<DataEntry> getOrganizationDataFromDB(){
        ArrayList<DataEntry> resList = new ArrayList<>();

        // Define a variable to contain a content resolver instance
        final String[] ORGANIZATION_PROJECTION = new String[] {
                EvendateContract.OrganizationEntry._ID,
                EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID,
                EvendateContract.OrganizationEntry.COLUMN_NAME,
                EvendateContract.OrganizationEntry.COLUMN_IMG_URL,
                EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
                EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION,
                EvendateContract.OrganizationEntry.COLUMN_TYPE_NAME,
                EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT,
                EvendateContract.OrganizationEntry.COLUMN_IS_SUBSCRIBED
        };
        // Constants representing column positions from PROJECTION.
        final int COLUMN_ID = 0;
        final int COLUMN_ORGANIZATION_ID = 1;
        final int COLUMN_NAME = 2;
        final int COLUMN_IMG_URL = 3;
        final int COLUMN_SHORT_NAME = 4;
        final int COLUMN_DESCRIPTION = 5;
        final int COLUMN_TYPE_NAME = 6;
        final int COLUMN_SUBSCRIBED_COUNT = 7;
        final int COLUMN_IS_SUBSCRIBED = 8;

        Uri uri = EvendateContract.OrganizationEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, ORGANIZATION_PROJECTION, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            OrganizationEntry entry = new OrganizationEntry(
                    c.getInt(COLUMN_ORGANIZATION_ID),
                    c.getString(COLUMN_NAME),
                    c.getString(COLUMN_IMG_URL),
                    c.getString(COLUMN_SHORT_NAME),
                    c.getString(COLUMN_DESCRIPTION),
                    c.getString(COLUMN_TYPE_NAME),
                    c.getInt(COLUMN_SUBSCRIBED_COUNT),
                    c.getInt(COLUMN_IS_SUBSCRIBED) != 0
            );
            entry.setId(c.getInt(COLUMN_ID));
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataEntry> getTagsDataFromDB(){
        ArrayList<DataEntry> resList = new ArrayList<>();

        // Define a variable to contain a content resolver instance
        final String[] PROJECTION = new String[] {
                EvendateContract.TagEntry._ID,
                EvendateContract.TagEntry.COLUMN_TAG_ID,
                EvendateContract.TagEntry.COLUMN_NAME,
        };
        // Constants representing column positions from PROJECTION.
        final int COLUMN_ID = 0;
        final int COLUMN_TAG_ID = 1;
        final int COLUMN_NAME = 2;

        Uri uri = EvendateContract.TagEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            TagEntry entry = new TagEntry(
                    c.getInt(COLUMN_TAG_ID),
                    c.getString(COLUMN_NAME)
            );
            entry.setId(c.getInt(COLUMN_ID));
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataEntry> getUserDataFromDB(){
        ArrayList<DataEntry> resList = new ArrayList<>();

        // Define a variable to contain a content resolver instance
        final String[] ORGANIZATION_PROJECTION = new String[] {
                EvendateContract.UserEntry._ID,
                EvendateContract.UserEntry.COLUMN_USER_ID,
                EvendateContract.UserEntry.COLUMN_LAST_NAME,
                EvendateContract.UserEntry.COLUMN_FIRST_NAME,
                EvendateContract.UserEntry.COLUMN_MIDDLE_NAME,
                EvendateContract.UserEntry.COLUMN_AVATAR_URL,
                EvendateContract.UserEntry.COLUMN_FRIEND_UID,
                EvendateContract.UserEntry.COLUMN_TYPE,
                EvendateContract.UserEntry.COLUMN_LINK
        };
        // Constants representing column positions from PROJECTION.
        final int COLUMN_ID = 0;
        final int COLUMN_USER_ID = 1;
        final int COLUMN_LAST_NAME = 2;
        final int COLUMN_FIRST_NAME = 3;
        final int COLUMN_MIDDLE_NAME = 4;
        final int COLUMN_AVATAR_URL = 5;
        final int COLUMN_FRIEND_UID = 6;
        final int COLUMN_TYPE = 7;
        final int COLUMN_LINK = 8;

        Uri uri = EvendateContract.UserEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, ORGANIZATION_PROJECTION, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            FriendEntry entry = new FriendEntry(
                    c.getInt(COLUMN_USER_ID),
                    c.getString(COLUMN_LAST_NAME),
                    c.getString(COLUMN_FIRST_NAME),
                    c.getString(COLUMN_MIDDLE_NAME),
                    c.getString(COLUMN_AVATAR_URL),
                    c.getString(COLUMN_TYPE),
                    c.getInt(COLUMN_FRIEND_UID),
                    c.getString(COLUMN_LINK)
            );
            entry.setId(c.getInt(COLUMN_ID));
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataEntry> getEventDataFromDB(){
        ArrayList<DataEntry> resList = new ArrayList<>();

        // Define a variable to contain a content resolver instance
        final String[] ORGANIZATION_PROJECTION = new String[] {
                EvendateContract.EventEntry._ID,
                EvendateContract.EventEntry.COLUMN_EVENT_ID,
                EvendateContract.EventEntry.COLUMN_TITLE,
                EvendateContract.EventEntry.COLUMN_DESCRIPTION,
                EvendateContract.EventEntry.COLUMN_LATITUDE,
                EvendateContract.EventEntry.COLUMN_LONGITUDE,
                EvendateContract.EventEntry.COLUMN_LOCATION_TEXT,
                EvendateContract.EventEntry.COLUMN_LOCATION_URI,
                EvendateContract.EventEntry.COLUMN_LOCATION_JSON,
                EvendateContract.EventEntry.COLUMN_NOTIFICATIONS,
                EvendateContract.EventEntry.COLUMN_START_DATE,
                EvendateContract.EventEntry.COLUMN_BEGIN_TIME,
                EvendateContract.EventEntry.COLUMN_END_TIME,
                EvendateContract.EventEntry.COLUMN_END_DATE,
                EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID,
                EvendateContract.EventEntry.COLUMN_IMAGE_VERTICAL_URL,
                EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL,
                EvendateContract.EventEntry.COLUMN_IMAGE_HORIZONTAL_URL,
                EvendateContract.EventEntry.COLUMN_CAN_EDIT,
                EvendateContract.EventEntry.COLUMN_EVENT_TYPE,
                EvendateContract.EventEntry.COLUMN_IS_FAVORITE
        };
        // Constants representing column positions from PROJECTION.
        final int COLUMN_ID = 0;
        final int COLUMN_EVENT_ID = 1;
        final int COLUMN_TITLE = 2;
        final int COLUMN_DESCRIPTION = 3;
        final int COLUMN_LATITUDE = 4;
        final int COLUMN_LONGITUDE = 5;
        final int COLUMN_LOCATION_TEXT = 6;
        final int COLUMN_LOCATION_URI = 7;
        final int COLUMN_LOCATION_JSON = 8;
        final int COLUMN_NOTIFICATIONS = 9;
        final int COLUMN_START_DATE = 10;
        final int COLUMN_BEGIN_TIME = 11;
        final int COLUMN_END_TIME = 12;
        final int COLUMN_END_DATE = 13;
        final int COLUMN_ORGANIZATION_ID = 14;
        final int COLUMN_IMAGE_VERTICAL_URL = 15;
        final int COLUMN_DETAIL_INFO_URL = 16;
        final int COLUMN_IMAGE_HORIZONTAL_URL = 17;
        final int COLUMN_CAN_EDIT = 18;
        final int COLUMN_EVENT_TYPE = 19;
        final int COLUMN_IS_FAVORITE = 20;

        Uri uri = EvendateContract.EventEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, ORGANIZATION_PROJECTION, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            EventEntry entry = new EventEntry(
                    c.getInt(COLUMN_EVENT_ID),
                    c.getString(COLUMN_TITLE),
                    c.getString(COLUMN_DESCRIPTION),
                    c.getString(COLUMN_LOCATION_TEXT),
                    c.getString(COLUMN_LOCATION_URI),
                    c.getString(COLUMN_START_DATE),
                    c.getString(COLUMN_NOTIFICATIONS),
                    c.getInt(COLUMN_ORGANIZATION_ID),
                    c.getLong(COLUMN_LATITUDE),
                    c.getLong(COLUMN_LONGITUDE),
                    c.getString(COLUMN_END_DATE),
                    c.getString(COLUMN_DETAIL_INFO_URL),
                    c.getString(COLUMN_BEGIN_TIME),
                    c.getString(COLUMN_END_TIME),
                    c.getString(COLUMN_LOCATION_JSON),
                    c.getInt(COLUMN_CAN_EDIT) != 0,
                    c.getString(COLUMN_EVENT_TYPE),
                    c.getInt(COLUMN_IS_FAVORITE),
                    c.getString(COLUMN_IMAGE_HORIZONTAL_URL),
                    c.getString(COLUMN_IMAGE_VERTICAL_URL),
                    0
            );
            entry.setId(c.getInt(COLUMN_ID));
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataEntry> getEventTagDataFromDB(int eventId){
        ArrayList<DataEntry> resList = new ArrayList<>();

        // Define a variable to contain a content resolver instance
        final String[] PROJECTION = new String[] {
                EvendateContract.TagEntry.TABLE_NAME + "." + EvendateContract.TagEntry._ID,
                EvendateContract.TagEntry.TABLE_NAME + "." + EvendateContract.TagEntry.COLUMN_TAG_ID,
                EvendateContract.TagEntry.TABLE_NAME + "." + EvendateContract.TagEntry.COLUMN_NAME,
        };
        // Constants representing column positions from PROJECTION.
        final int COLUMN_ID = 0;
        final int COLUMN_TAG_ID = 1;
        final int COLUMN_NAME = 2;

        Uri uri = EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(eventId)).appendPath(EvendateContract.PATH_TAGS).build(); // Get all entries
        Cursor c = mContentResolver.query(uri, PROJECTION, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            EventTagEntry entry = new EventTagEntry(
                    eventId,
                    c.getInt(COLUMN_TAG_ID),
                    c.getString(COLUMN_NAME)
            );
            entry.setId(c.getInt(COLUMN_ID));
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataEntry> getEventFriendDataFromDB(int eventId){
        ArrayList<DataEntry> resList = new ArrayList<>();

        // Define a variable to contain a content resolver instance
        final String[] ORGANIZATION_PROJECTION = new String[] {
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry._ID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_USER_ID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_LAST_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_FIRST_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_MIDDLE_NAME,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_AVATAR_URL,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_FRIEND_UID,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_TYPE,
                EvendateContract.UserEntry.TABLE_NAME + "." + EvendateContract.UserEntry.COLUMN_LINK
        };
        // Constants representing column positions from PROJECTION.
        final int COLUMN_ID = 0;
        final int COLUMN_USER_ID = 1;
        final int COLUMN_LAST_NAME = 2;
        final int COLUMN_FIRST_NAME = 3;
        final int COLUMN_MIDDLE_NAME = 4;
        final int COLUMN_AVATAR_URL = 5;
        final int COLUMN_FRIEND_UID = 6;
        final int COLUMN_TYPE = 7;
        final int COLUMN_LINK = 8;

        Uri uri = EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(eventId)).appendPath(EvendateContract.PATH_USERS).build();
        Cursor c = mContentResolver.query(uri, ORGANIZATION_PROJECTION, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            EventFriendEntry entry = new EventFriendEntry(
                    eventId,
                    c.getInt(COLUMN_USER_ID),
                    c.getString(COLUMN_LAST_NAME),
                    c.getString(COLUMN_FIRST_NAME),
                    c.getString(COLUMN_MIDDLE_NAME),
                    c.getString(COLUMN_AVATAR_URL),
                    c.getString(COLUMN_TYPE),
                    c.getInt(COLUMN_FRIEND_UID),
                    c.getString(COLUMN_LINK)
            );
            entry.setId(c.getInt(COLUMN_ID));
            resList.add(entry);
        }
        c.close();
        return resList;
    }

    public HashMap<Integer, File> getEventImages() {
        File file = new File(Environment.getExternalStorageDirectory(), EvendateContract.PATH_EVENT_IMAGES);
        File[] pictures = file.listFiles();
        if(pictures == null)
            return null;
        HashMap<Integer, File> files = new HashMap<>();

        for (int i = 0; i <= pictures.length; i++) {

            files.put(Integer.getInteger(pictures[i].getName()), pictures[i]);
            Log.e("FILE:", pictures[i].getAbsolutePath());
        }
        return files;
    }
    public HashMap<Integer, File> getOrganizationsImages() {
        File file = new File(Environment.getExternalStorageDirectory(), EvendateContract.PATH_ORGANIZATION_IMAGES);
        File[] pictures = file.listFiles();
        if(pictures == null)
            return null;
        HashMap<Integer, File> files = new HashMap<>();

        for (int i = 0; i <= pictures.length; i++) {

            files.put(Integer.getInteger(pictures[i].getName()), pictures[i]);
            Log.e("FILE:", pictures[i].getAbsolutePath());
        }
        return files;
    }
}
